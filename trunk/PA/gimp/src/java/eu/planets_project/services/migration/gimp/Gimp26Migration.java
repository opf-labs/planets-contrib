package eu.planets_project.services.migration.gimp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import eu.planets_project.ifr.core.techreg.impl.formats.FormatRegistryImpl;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * The Gimp26Migration class migrates various image file formats. 
 * This class is a wrapper for The GIMP version 2.6.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService(/* name = Gimp26Migration.NAME ,*/serviceName = Migrate.NAME,
targetNamespace = PlanetsServices.NS,
endpointInterface = "eu.planets_project.services.migrate.Migrate")
public final class Gimp26Migration implements Migrate, Serializable {

    PlanetsLogger log = PlanetsLogger.getLogger(Gimp26Migration.class);
    private static Logger logger = Logger.getLogger(Gimp26Migration.class.getName());
    public String gimp_install_dir;
    public String gimp_app_name;
    private File tmpInFile;
    private File tmpOutFile;
    
    private String gimpFuScriptCmdStr;
    /***/
    static final String NAME = "Gimp26Migration";
    /***/
    private static final long serialVersionUID = 2127494848765937613L;
    
    List<String> inputFormats = null;
    HashMap  outputFormats = null;
    HashMap  formatMapping = null;
    HashMap defaultParameters = null;
    
    String inputFmtExt = null;
    String outputFmtExt = null;
    
    private void init()
    {
        gimpFuScriptCmdStr = null;
        
        // input formats
        inputFormats = new ArrayList<String>();
        inputFormats.add("GIF");
        inputFormats.add("EPS");
        inputFormats.add("JPEG");
        inputFormats.add("PNG");
        inputFormats.add("PS");
        inputFormats.add("TIFF");
        inputFormats.add("BMP");
        
        // Disambiguation of extensions, e.g. {"JPG","JPEG"} to {"JPEG"}
        // FIXIT This should be supported by the FormatRegistryImpl class, but
        // it does not provide the complete set at the moment.
        formatMapping = new HashMap();
        formatMapping.put("JPG","JPEG");
        formatMapping.put("TIF","TIFF");
    }
    
    private void getExtensions(URI inputFormat, URI outputFormat)
    {
        inputFmtExt = getFormatExt( inputFormat, false );
        outputFmtExt = getFormatExt( outputFormat, true );
    }
    
    /**
     * Create a white space separated parameter string which can be passed
     * over to the gimp script (e.g.  1 0 256 0 0 for migration to GIF).
     * 
     * @param paramNameValuePair The data structure containing the parameter
     * value pairs
     * @return
     */
    private String getParameterString(HashMap paramNameValuePair)
    {
        StringBuffer paramStrBuff = new StringBuffer();
        Collection values = paramNameValuePair.values();
        Iterator itr = values.iterator();
        while(itr.hasNext()) {
            paramStrBuff.append(" ");
            paramStrBuff.append((String) itr.next());
        }
        return paramStrBuff.toString();
    }
    
    private void setParameters(Parameters parameters)
    {
        // output formats and associated output parameters
        outputFormats = new HashMap();
        
        // main parameters hashmap
        // e.g. 
        // { "GIF" -> { "gif-interlace", "gif-dither", ... } }
        // { "JPEG" -> { "jpeg-quality", "jpeg-smoothing", ... } }
        // ...
        defaultParameters = new HashMap();
        /*
        // Define parameters and default values
        // GIF
        List<Parameter> gifParameterList = new ArrayList<Parameter>();
        Parameter gifInterlaceParam = new new Parameter("gif-interlace", "1", "xsd:integer { minLength=\"0\" maxLength=\"1\" }");
        gifParameterList.add(gifInterlaceParam);
        gifParameterList.add(new Parameter("gif-dither", "1", "xsd:integer { minLength=\"0\" maxLength=\"1\" }"));
        gifParameterList.add(new Parameter("gif-numcolors", "256", "xsd:integer { minLength=\"0\" maxLength=\"256\" }"));
        gifParameterList.add(new Parameter("gif-dither", "1", "xsd:integer { minLength=\"0\" maxLength=\"1\" }"));
        gifParameterList.add(new Parameter("gif-alphadither", "1", "xsd:integer { minLength=\"0\" maxLength=\"1\" }"));
        gifParameterList.add(new Parameter("gif-removeunused", "1", "xsd:integer { minLength=\"0\" maxLength=\"1\" }"));
        defaultParameters.put("GIF", gifParameterList);
        */
        // EPS
        HashMap epsNameValuePair = new HashMap();
        epsNameValuePair.put("eps-width", "0");
        epsNameValuePair.put("eps-height", "0");
        epsNameValuePair.put("eps-xoffset", "0");
        epsNameValuePair.put("eps-yoffset", "0");
        epsNameValuePair.put("eps-unit", "0");
        epsNameValuePair.put("eps-keepratio", "1");
        epsNameValuePair.put("eps-rotation", "0");
        epsNameValuePair.put("eps-preview", "0");
        epsNameValuePair.put("eps-level", "2");
        defaultParameters.put("EPS", epsNameValuePair);
        
        // JPEG
        HashMap jpegNameValuePair = new HashMap();
        jpegNameValuePair.put("jpeg-quality", "0.1");
        jpegNameValuePair.put("jpeg-smoothing", "0");
        jpegNameValuePair.put("jpeg-optimize", "1");
        jpegNameValuePair.put("jpeg-progressive", "1");
        defaultParameters.put("JPEG", jpegNameValuePair);
        
        // PNG
        HashMap pngNameValuePair = new HashMap();
        pngNameValuePair.put("png-interlace", "1");
        pngNameValuePair.put("png-compression", "1");
        defaultParameters.put("PNG", pngNameValuePair);
        
        // PS
        HashMap psNameValuePair = new HashMap();
        psNameValuePair.put("ps-width", "0");
        psNameValuePair.put("ps-height", "0");
        psNameValuePair.put("ps-xoffset", "0");
        psNameValuePair.put("ps-yoffset", "0");
        psNameValuePair.put("ps-unit", "0");
        psNameValuePair.put("ps-keepratio", "1");
        psNameValuePair.put("ps-rotation", "0");
        psNameValuePair.put("ps-preview", "0");
        psNameValuePair.put("ps-level", "2");
        defaultParameters.put("PS", psNameValuePair);
        
        // TIFF
        HashMap tiffNameValuePair = new HashMap();
        tiffNameValuePair.put("tiff-compressiontype", "1");
        defaultParameters.put("TIFF", tiffNameValuePair);
        
        // BMP
        HashMap bmpNameValuePair = new HashMap();
        bmpNameValuePair.put("bmp-dummy", "");
        defaultParameters.put("BMP", bmpNameValuePair);
        
        // Change default parameters according to the parameters defined by the
        // user
        List<Parameter> paramList = parameters.getParameters(); // User parameters
        Iterator itr = paramList.iterator(); 
        while(itr.hasNext()) {
            Parameter param = (Parameter) itr.next();
            String paramName = param.name;
            String paramValue = param.value;
            System.out.println("Set parameter: " + paramName + " with value: " + paramValue);
            HashMap hm = (HashMap)defaultParameters.get(outputFmtExt);
            // is the parameter permitted?
            if( hm.containsKey(paramName) )
            {
                hm.put(paramName, paramValue); // override default parameter
            }
        }
        
        // options: interlace dither palette num-colors alpha-dither remove-unused
        // defaults: 1 0 256 0 0
        //outputFormats.put("GIF",getParameterString(gifNameValuePair)); 
        // options:  width height x-offset y-offset unit keep-ratio rotation 
        // preview level
        // defaults: 0 0 0 0 0 1 0 0 2
        outputFormats.put("EPS",getParameterString(epsNameValuePair)); 
        // options:  quality smoothing optimize progressive
        // defaults: 0.1 0 1 1
        outputFormats.put("JPEG",getParameterString(jpegNameValuePair)); 
        // options: interlace compression
        // defaults:  1 1
        outputFormats.put("PNG",getParameterString(pngNameValuePair)); 
        // options:  width height x-offset y-offset unit keep-ratio rotation 
        // preview level
        // default: 0 0 0 0 0 1 0 0 2
        outputFormats.put("PS",getParameterString(psNameValuePair)); 
        // options: compression Compression type: {None (0), LZW (1), PACKBITS(2), 
        // DEFLATE (3), JPEG (4), CCITT G3 Fax (5), CCITT G4 Fax (6)}
        // defaults:  1
        outputFormats.put("TIFF",getParameterString(tiffNameValuePair));
        // options: none
        // defaults: 
        outputFormats.put("BMP",getParameterString(bmpNameValuePair)); 
    }
    /**
     * {@inheritDoc}
     * 
     * @see eu.planets_project.ifr.core.common.services.migrate.MigrateOneDigitalObject#migrate(eu.planets_project.ifr.core.common.services.datatypes.DigitalObject)
     */
    public MigrateResult migrate(final DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, Parameters parameters) {
        
        // initialise input formats (GIF, JPEG, EPS, ...) and output formats list 
        // and (GIF, JPEG, EPS, ...) and apply disambiguation ("JPG" -> "JPEG"}
        init();
        
         // set input and output extensions based upon input and output format
        // e.g. 
        // inputFormat="planets:fmt/ext/tif" -> inputFmtExt="TIFF"
        // outputFormat="planets:fmt/ext/jpg" -> outputFmtExt="JPEG"
        getExtensions(inputFormat,outputFormat);
        
        // Set the parameterString depending on the desired output format
        // which is first initialised by default values which are then
        // overridden by user parameters of the service call.
        setParameters(parameters);
        
        // configuration parameters
        Properties props = new Properties();
        try {
            // @FIXIT Does not load the properties 
            String strRsc = "/eu/planets_project/services/migration/gimp/gimp26.properties";
            props.load(this.getClass().getResourceAsStream(strRsc));
            this.gimp_install_dir = props.getProperty("gimp.install.dir");
            this.gimp_app_name = props.getProperty("gimp.app.name");
        } catch (Exception e) {
            this.gimp_install_dir = "/home/georg/Projects/PLANETS/";
            this.gimp_app_name = "gimp";
        }
        log.info("Using gimp install directory: " + this.gimp_install_dir);
        log.info("Using gimp application name: " + this.gimp_app_name);
        log.info("Using gimp application name: " + this.gimp_app_name);
        
        // get binary data from digital object
        byte[] binary = digitalObject.getContent().getValue();

       
        // write binary array to temporary file
        tmpInFile = ByteArrayHelper.write(binary);
        System.out.println("tmpInFile: " + tmpInFile.getAbsolutePath());

        // Create inputstream from binary array
        InputStream inputStream = new ByteArrayInputStream(binary);

        // set gimp fu-script command
        gimpFuScriptCmdStr = getFuScriptMigrationStr();
        System.out.println("GIMP Fu-Script command "+gimpFuScriptCmdStr);
        if( gimpFuScriptCmdStr != null )
        {
            // commands string array
            String[] commands = new String[]{
                    gimp_app_name, 
                    "--verbose",
                    "-c",
                    "-i",
                    "-d",
                    "-b",
                    gimpFuScriptCmdStr, // Migration Fu-Script function call
                    "-b",
                    "(gimp-quit 0)"
                };
            
            // temporary outfile, outfile name = infilename + ".out"
            String outFileStr = tmpInFile.getAbsolutePath() + ".out";
            tmpOutFile = new File(outFileStr);

            // Create process runner and execute commands
            ProcessRunner runner = new ProcessRunner();
            runner.setCommand(Arrays.asList(commands));
            runner.setInputStream(inputStream);
            runner.run();
            System.out.println(runner.getProcessErrorAsString());
            int return_code = runner.getReturnCode();
            if (return_code != 0) {
                log.error("Gimp conversion error code: " + Integer.toString(return_code));
            }

            // read byte array from temporary file
            if (tmpOutFile.isFile() && tmpOutFile.canRead()) {
                binary = ByteArrayHelper.read(tmpOutFile);
            } else {
                log.error("Error: Unable to read temporary file " + tmpInFile.getPath() + tmpInFile.getName());
            }
        }
        
        // digital object output
        DigitalObject newDO = null;
        ServiceReport report = new ServiceReport();
        newDO = new DigitalObject.Builder(Content.byValue(binary)).build();
        return new MigrateResult(newDO, report);
    }

    /* (non-Javadoc)
     * @see eu.planets_project.ifr.core.common.services.migrate.MigrateOneDigitalObject#describe()
     */
    public ServiceDescription describe() {

        // parameters
        // gif
        // options: interlace dither palette num-colors alpha-dither remove-unused
        List<Parameter> parameterList = new ArrayList<Parameter>();
        Parameter gifInterlaceParam = new Parameter("gif-interlace", "true/false");
        gifInterlaceParam.setDescription("GIF-Parameter: Boolean value true/false indicating if interlacing should be used.");
        parameterList.add(gifInterlaceParam);
        Parameter gifDitherParam = new Parameter("gif-dither", "true/false");
        gifDitherParam.setDescription("GIF-Parameter: Boolean value true/false indicating if dither should be used.");
        parameterList.add(gifDitherParam);
        Parameter gifNumcolorsParam = new Parameter("gif-dither", "0-256");
        gifNumcolorsParam.setDescription("GIF-Parameter: Integer value in the range from 0 to 256 indicating the number of colors to be used.");
        parameterList.add(gifNumcolorsParam);
        Parameter gifAlphaditherParam = new Parameter("gif-alphadither", "true/false");
        gifAlphaditherParam.setDescription("GIF-Parameter: Boolean value true/false indicating if alpha dither should be used.");
        parameterList.add(gifAlphaditherParam);
        Parameter gifRemunusedParam = new Parameter("gif-removeunused", "true/false");
        gifRemunusedParam.setDescription("GIF-Parameter: Boolean value true/false indicating if unused colors should be removed");
        parameterList.add(gifRemunusedParam);

        // eps
        Parameter epsPostscriptLevel2Param = new Parameter("epsPostscriptLevel2", "true/false");
        epsPostscriptLevel2Param.setDescription("EPS-Parameter: Boolean value true/false indicating if the target format should be postscript level 2.");
        parameterList.add(epsPostscriptLevel2Param);
        Parameter epsEmbeddedPostscriptParam = new Parameter("epsEmbeddedPostscript", "true/false");
        epsEmbeddedPostscriptParam.setDescription("EPS-Parameter: Boolean value true/false indicating if the target format should be embedded postscript.");
        parameterList.add(epsEmbeddedPostscriptParam);
        Parameter epsWithPreviewParam = new Parameter("epsWithPreview", "true/false");
        epsWithPreviewParam.setDescription("EPS-Parameter: Boolean value true/false indicating if a preview image is used.");
        parameterList.add(epsWithPreviewParam);
        Parameter epsPreviewSizeParam = new Parameter("epsPreviewSize", "0-1024");
        epsPreviewSizeParam.setDescription("EPS-Parameter: Integer value in the range 0-1024 representing the size of the preview image.");
        parameterList.add(epsPreviewSizeParam);

        // jpeg
        Parameter jpgCompressionRateParam = new Parameter("jpgCompressionRate", "{0,100}");
        jpgCompressionRateParam.setDescription("JPG-Parameter: Integer value in the range 0-100 representing the compression rate.");
        parameterList.add(jpgCompressionRateParam);
        Parameter jpgOptimizeParam = new Parameter("jpgOptimize", "true/false");
        jpgOptimizeParam.setDescription("JPG-Parameter: Boolean value true/false indicating if the Jpeg should be optimized.");
        parameterList.add(jpgOptimizeParam);
        Parameter jpgProgressiveParam = new Parameter("jpgProgressive", "true/false");
        jpgProgressiveParam.setDescription("JPG-Parameter: Boolean value true/false indicating if progressive should be used.");
        parameterList.add(jpgProgressiveParam);
        Parameter jpgSmoothingParam = new Parameter("jpgSmoothing", "[0,1]");
        jpgSmoothingParam.setDescription("JPG-Parameter: Floating point number indicating the smoothing grade.");
        parameterList.add(jpgSmoothingParam);

        Parameters parameters = new Parameters();
        parameters.setParameters(parameterList);

        ServiceDescription mds = new ServiceDescription.Builder(NAME, Migrate.class.getName())
                .author("Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at>")
                .classname(this.getClass().getCanonicalName())
                .description("A wrapper for file migrations using GIMP version 2.6" +
                "This service accepts input and target formats of the form: " +
                "'planets:fmt/ext/[extension]'\n" +
                "e.g. 'planets:fmt/ext/tiff' or 'planets:fmt/ext/tif'")
                .version("0.1")
                .parameters(parameters)
                .paths(createMigrationPathwayMatrix(inputFormats, outputFormats))
                .build();
        
        return mds;
    }
    
    /**
     * Gets one extension from a set of possible extensions for the incoming
     * request planets URI (e.g. planets:fmt/ext/jpeg) which matches with
     * one format of the set of GIMP's supported input/output formats. If
     * isOutput is false, it checks against the gimp input formats ArrayList,
     * otherwise it checks against the gimp output formats HashMap.
     * 
     * @param formatUri Planets URI (e.g. planets:fmt/ext/jpeg) 
     * @param isOutput Is the format an input or an output format
     * @return Format extension (e.g. "JPEG")
     */
    private String getFormatExt( URI formatUri, boolean isOutput  )
    {
        String fmtStr = null;
        // status variable which indicates if an input/out format has been found 
        // while iterating over possible matches
        boolean fmtFound = false;
        // Extensions which correspond to the format
        // planets:fmt/ext/jpg -> { "JPEG", "JPG" }
        // or can be found in the list of GIMP supported formats
        FormatRegistryImpl fmtRegImpl = new FormatRegistryImpl();
        Format uriFormatObj = fmtRegImpl.getFormatForURI(formatUri);
        Set<String> reqInputFormatExts = uriFormatObj.getExtensions();
        Iterator itrReq = reqInputFormatExts.iterator(); 
        // Iterate either over input formats ArrayList or over output formats
        // HasMap
        Iterator itrGimp = (isOutput)?outputFormats.keySet().iterator():inputFormats.iterator();
        // Iterate over possible extensions that correspond to the request
        // planets uri.
        while(itrReq.hasNext()) {
            // Iterate over the different extensions of the planets:fmt/ext/jpg
            // format URI, note that the relation of Planets-format-URI to
            // extensions is 1 : n.
            String reqFmtExt = normalizeExt((String) itrReq.next());
            while(itrGimp.hasNext()) {
                // Iterate over the formats that GIMP offers either as input or
                // as output format.
                // See input formats in the this.init() method to see the
                // GIMP input/output formats offered by this service.
                String gimpFmtStr = (String) itrGimp.next();
                if( reqFmtExt.equalsIgnoreCase(gimpFmtStr) )
                {
                    // select the gimp supported format
                    fmtStr = gimpFmtStr;
                    fmtFound = true;
                    break;
                }
                if( fmtFound )
                    break;
            }
        }
        return fmtStr;
    }
    
    private String normalizeExt(String ext)
    {
        String normExt = ext.toUpperCase();
        return ((formatMapping.containsKey(normExt))?
            (String)formatMapping.get(normExt):normExt);
    }
    
    /**
     * Create fu-script command string.
     * 
     * @param inputFormat Planets input format URI (e.g. planets:fmt/ext/jpg)
     * @param outputFormat Planets output format URI (e.g. planets:fmt/ext/jpg)
     * @return Null if the input or output format is not supported, fu-script 
     * command string otherwise
     */
    private String getFuScriptMigrationStr()
    {
        // fu-script string of the planets migration command
        String fuScriptMigrString = null;
        
        if( inputFmtExt != null && outputFmtExt != null )
        {
            // build fu-script command of the form e.g.
            // planetsMigrateJPEGtoPNG(infile.ext outfile.ext 1 1 1)
            fuScriptMigrString = 
                            "(planetsMigrate" + 
                            inputFmtExt +
                            "to" +
                            outputFmtExt +
                            " \"" + tmpInFile.getAbsolutePath() + "\"" +
                            " \"" + tmpInFile.getAbsolutePath() + ".out\" " +
                            outputFormats.get(outputFmtExt) + 
                            ")";
        }
        return fuScriptMigrString;
    }

    private MigrationPath[] createMigrationPathwayMatrix(List<String> inputFormats, HashMap outputFormats) {
        List<MigrationPath> paths = new ArrayList<MigrationPath>();

        for (Iterator iterator = inputFormats.iterator(); iterator.hasNext();) {
            String input = (String) iterator.next();

            for (Iterator iterator2 = outputFormats.keySet().iterator(); iterator2.hasNext();) {
                String output = (String) iterator2.next();
                MigrationPath path = new MigrationPath(Format.extensionToURI(input),
                        Format.extensionToURI(output), null);

                paths.add(path);
            }
        }
        return paths.toArray(new MigrationPath[]{});
    }
}
