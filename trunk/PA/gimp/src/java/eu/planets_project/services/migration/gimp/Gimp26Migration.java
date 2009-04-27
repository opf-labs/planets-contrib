package eu.planets_project.services.migration.gimp;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ImmutableContent;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;

/**
 * The Gimp26Migration class migrates various image file formats. 
 * This class is a wrapper for The GIMP version 2.6.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService(name = Gimp26Migration.NAME ,serviceName = Migrate.NAME,
targetNamespace = PlanetsServices.NS,
endpointInterface = "eu.planets_project.services.migrate.Migrate")
public final class Gimp26Migration implements Migrate, Serializable {

    PlanetsLogger log = PlanetsLogger.getLogger(Gimp26Migration.class);
    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Gimp26Migration.class.getName());
    /** The GIMP install directory */
    public String gimp_install_dir;
    /** The application name */
    public String gimp_app_name;
    private File tmpInFile;
    private File tmpOutFile;
    
    private String gimpFuScriptCmdStr;
    /***/
    static final String NAME = "Gimp26Migration";
    /***/
    private static final long serialVersionUID = 2127494848765937613L;
    
    List<String> inputFormats = null;
    List<String> outputFormats = null;
    HashMap<String, String>  formatMapping = null;
    HashMap<String, List<Parameter>> defaultParameters = null;
    
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
        
        // output formats and associated output parameters
        outputFormats = new ArrayList<String>();
        // options: interlace dither palette num-colors alpha-dither remove-unused
        // defaults: 1 0 256 0 0
        outputFormats.add("GIF"); 
        // options:  width height x-offset y-offset unit keep-ratio rotation 
        // preview level
        // defaults: 0 0 0 0 0 1 0 0 2
        outputFormats.add("EPS"); 
        // options:  quality smoothing optimize progressive
        // defaults: 0.1 0 1 1
        outputFormats.add("JPEG"); 
        // options: interlace compression
        // defaults:  1 1
        outputFormats.add("PNG"); 
        // options:  width height x-offset y-offset unit keep-ratio rotation 
        // preview level
        // default: 0 0 0 0 0 1 0 0 2
        outputFormats.add("PS"); 
        // options: compression Compression type: {None (0), LZW (1), PACKBITS(2), 
        // DEFLATE (3), JPEG (4), CCITT G3 Fax (5), CCITT G4 Fax (6)}
        // defaults:  1
        outputFormats.add("TIFF");
        // options: none
        // defaults: 
        outputFormats.add("BMP"); 
        
        // Disambiguation of extensions, e.g. {"JPG","JPEG"} to {"JPEG"}
        // FIXIT This should be supported by the FormatRegistryImpl class, but
        // it does not provide the complete set at the moment.
        formatMapping = new HashMap<String, String>();
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
     * @param ext Extension for which we retrieve the parameter list.
     * @return Paramter string
     */
    private String getParameterString(String ext)
    {
        StringBuffer paramStrBuff = new StringBuffer();
        List<Parameter> fmtParameterList = (List<Parameter>) defaultParameters.get(ext);
        Iterator<Parameter> itr = fmtParameterList.iterator();
        while(itr.hasNext()) {
            Parameter param = (Parameter) itr.next();
            if( param.value != null )
            {
                paramStrBuff.append(" ");
                paramStrBuff.append(param.value);
            }
        }
        return paramStrBuff.toString();
    }
    
    /**
     * Initialize the parameters list for all migration file formats. Every
     * parameter has a default value which is overridden where requested
     * from the user (parameters contains the parameters passed to the
     * service by the user).
     */
    private void initParameters()
    {
        // main parameters hashmap
        // e.g. 
        // { "GIF" -> { Parameter("gif-interlace","1"), Parameter("gif-dither","1"), ... } }
        // { "JPEG" -> { Parameter("jpeg-quality","0.9"), Parameter("jpeg-smoothing"),"0.9"), ... } }
        // ...
        defaultParameters = new HashMap<String, List<Parameter>>();
        
        // Define parameters and default values
        // GIF - 6 parameters
        List<Parameter> gifParameterList = new ArrayList<Parameter>();
        Parameter gifInterlaceParam = new Parameter("gif-interlace", "1");
        gifInterlaceParam.setDescription("GIF-Parameter: Boolean integer 0/1 indicating if interlacing should be used.");
        gifParameterList.add(gifInterlaceParam);
        Parameter gifDitherParam = new Parameter("gif-dither", "1");
        gifDitherParam.setDescription("GIF-Parameter: Boolean integer 0/1 indicating if interlacing should be used.");
        gifParameterList.add(gifDitherParam);
        Parameter gifPaletteParam = new Parameter("gif-palette", "0");
        gifPaletteParam.setDescription("GIF-Parameter: Integer indicating the palette to be used (MAKE-PALETTE (0), WEB-PALETTE (2), MONO-PALETTE (3), CUSTOM-PALETTE (4)).");
        gifParameterList.add(gifPaletteParam);
        Parameter gifNumcolorsParam = new Parameter("gif-numcolors", "256");
        gifNumcolorsParam.setDescription("GIF-Parameter: Integer between 0 and 256 indicating how many colors should be used.");
        gifParameterList.add(gifNumcolorsParam);
        Parameter gifAlphaditherParam = new Parameter("gif-alphadither", "1");
        gifAlphaditherParam.setDescription("GIF-Parameter: Boolean integer 0/1 indicating if alpha dither should be used.");
        gifParameterList.add(gifAlphaditherParam);
        Parameter gifRemoveunusedParam = new Parameter("gif-removeunused", "1");
        gifRemoveunusedParam.setDescription("GIF-Parameter: Boolean integer 0/1 indicating if unused colors should be removed.");
        gifParameterList.add(gifRemoveunusedParam);
        defaultParameters.put("GIF", gifParameterList);
        
        // EPS - 9 parameters
        List<Parameter> epsParameterList = new ArrayList<Parameter>();
        Parameter epsInterlaceParam = new Parameter("eps-width", "0");
        epsInterlaceParam.setDescription("EPS-Parameter: Positive integer value indicating the width.");
        epsParameterList.add(epsInterlaceParam);
        Parameter epsDitherParam = new Parameter("eps-height", "0");
        epsDitherParam.setDescription("EPS-Parameter: Positive integer value indicating the height.");
        epsParameterList.add(epsDitherParam);
        Parameter epsNumcolorsParam = new Parameter("eps-xoffset", "0");
        epsNumcolorsParam.setDescription("EPS-Parameter: Positive integer value indicating the x-offset.");
        epsParameterList.add(epsNumcolorsParam);
        Parameter epsAlphaditherParam = new Parameter("eps-yoffset", "0");
        epsAlphaditherParam.setDescription("EPS-Parameter: Positive integer value indicating the y-offset.");
        epsParameterList.add(epsAlphaditherParam);
        Parameter epsRemoveunusedParam = new Parameter("eps-unit", "0");
        epsRemoveunusedParam.setDescription("EPS-Parameter: Unit parameter.");
        epsParameterList.add(epsRemoveunusedParam);
        Parameter epsKeepratioParam = new Parameter("eps-keepratio", "1");
        epsKeepratioParam.setDescription("EPS-Parameter: Boolean integer 0/1 indicating if the ratio should be maintained.");
        epsParameterList.add(epsKeepratioParam);
        Parameter epsRotationParam = new Parameter("eps-rotation", "0");
        epsRotationParam.setDescription("EPS-Parameter: Boolean integer 0/1 indicating if the image should be rotated.");
        epsParameterList.add(epsRotationParam);
        Parameter epsPreviewParam = new Parameter("eps-preview", "0");
        epsPreviewParam.setDescription("EPS-Parameter: Boolean integer 0/1 indicating if a preview image should be created.");
        epsParameterList.add(epsPreviewParam);
        Parameter epsLevelParam = new Parameter("eps-level", "2");
        epsLevelParam.setDescription("EPS-Parameter: Positive integer value 1 or 2 indicating the postscript level.");
        epsParameterList.add(epsLevelParam);
        defaultParameters.put("EPS", epsParameterList);
        
        // JPEG - 4 parameters
        List<Parameter> jpegParameterList = new ArrayList<Parameter>();
        Parameter jpegQualityParam = new Parameter("quality-width", "0.1");
        jpegQualityParam.setDescription("JPEG-Parameter: Float value in the range from 0 to 1 (step size 0.1) indicating the image quality. 0.1 low quality, 1 high quality.");
        jpegParameterList.add(jpegQualityParam);
        Parameter jpegSmoothingParam = new Parameter("quality-smoothing", "0.1");
        jpegSmoothingParam.setDescription("JPEG-Parameter: Float value in the range from 0 to 1 (step size 0.1) indicating the smoothing intensity. 0 no smoothing, 1 strong smoothing.");
        jpegParameterList.add(jpegSmoothingParam);
        Parameter jpegOptimizeParam = new Parameter("quality-optimize", "0");
        jpegOptimizeParam.setDescription("JPEG-Parameter: Boolean integer 0/1 indicating if the image should be optimized.");
        jpegParameterList.add(jpegOptimizeParam);
        Parameter jpegProgressiveParam = new Parameter("quality-progressive", "0");
        jpegProgressiveParam.setDescription("JPEG-Parameter: Boolean integer 0/1 indicating if progressive storage should be used.");
        jpegParameterList.add(jpegProgressiveParam);
        defaultParameters.put("JPEG", jpegParameterList);
        
        // PNG - 2 parameters
        List<Parameter> pngParameterList = new ArrayList<Parameter>();
        Parameter pngInterlaceParam = new Parameter("png-interlace", "1");
        pngInterlaceParam.setDescription("PNG-Parameter:  Boolean integer 0/1 indicating if interlacing should be used.");
        pngParameterList.add(pngInterlaceParam);
        Parameter pngCompressionParam = new Parameter("png-compression", "1");
        pngCompressionParam.setDescription("PNG-Parameter: Positive integer in the range 0 to 9 (step size 1) indicating the compression grade.");
        pngParameterList.add(pngCompressionParam);
        defaultParameters.put("PNG", pngParameterList);
        
        // PS - 9 parameters
        List<Parameter> psParameterList = new ArrayList<Parameter>();
        Parameter psInterlaceParam = new Parameter("ps-width", "0");
        psInterlaceParam.setDescription("PS-Parameter: Positive integer value indicating the width. 0 indicates to take the size from the original.");
        psParameterList.add(psInterlaceParam);
        Parameter psDitherParam = new Parameter("ps-height", "0");
        psDitherParam.setDescription("PS-Parameter: Positive integer value indicating the height. 0 indicates to take the size from the original.");
        psParameterList.add(psDitherParam);
        Parameter psNumcolorsParam = new Parameter("ps-xoffset", "0");
        psNumcolorsParam.setDescription("PS-Parameter: Positive integer value indicating the x-offset.");
        psParameterList.add(psNumcolorsParam);
        Parameter psAlphaditherParam = new Parameter("ps-yoffset", "0");
        psAlphaditherParam.setDescription("PS-Parameter: Positive integer value indicating the y-offset.");
        psParameterList.add(psAlphaditherParam);
        Parameter psRemoveunusedParam = new Parameter("ps-unit", "0");
        psRemoveunusedParam.setDescription("PS-Parameter: Unit parameter.");
        psParameterList.add(psRemoveunusedParam);
        Parameter psKeepratioParam = new Parameter("ps-keepratio", "1");
        psKeepratioParam.setDescription("PS-Parameter: Boolean integer 0/1 indicating if the ratio should be maintained.");
        psParameterList.add(psKeepratioParam);
        Parameter psRotationParam = new Parameter("ps-rotation", "0");
        psRotationParam.setDescription("PS-Parameter: Boolean integer 0/1 indicating if the image should be rotated.");
        psParameterList.add(psRotationParam);
        Parameter psPreviewParam = new Parameter("ps-preview", "0");
        psPreviewParam.setDescription("PS-Parameter: Boolean integer 0/1 indicating if a preview image should be created.");
        psParameterList.add(psPreviewParam);
        Parameter psLevelParam = new Parameter("ps-level", "2");
        psLevelParam.setDescription("PS-Parameter: Positive integer value 1 or 2 indicating the postscript level.");
        psParameterList.add(psLevelParam);
        defaultParameters.put("PS", psParameterList);
        
        // TIFF - 1 parameter
        List<Parameter> tiffParameterList = new ArrayList<Parameter>();
        Parameter tiffCompressiontypeParam = new Parameter("tiff-compressiontype", "0");
        tiffCompressiontypeParam.setDescription("TIFF-Parameter: Positive integer for the compression type to be used. Possible compression types: {None (0), LZW (1), PACKBITS(2), DEFLATE (3), JPEG (4), CCITT G3 Fax (5), CCITT G4 Fax (6)}");
        tiffParameterList.add(tiffCompressiontypeParam);
        defaultParameters.put("TIFF", tiffParameterList);
        
        
        // BMP
        List<Parameter> bmpParameterList = new ArrayList<Parameter>();
        Parameter pngDummyParam = new Parameter("bmp-dummy", "");
        pngDummyParam.setDescription("BMP-Parameter: BMP Conversion has no parameters");
        bmpParameterList.add(pngDummyParam);
        defaultParameters.put("BMP", bmpParameterList);
    }
    
    private void overrideDefaultParamets(List<Parameter> userParams)
    {
        // Change default parameters according to the parameters defined by the
        // user
        if( userParams != null )
        {
            Iterator<Parameter> userParmsItr = userParams.iterator(); 
            while(userParmsItr.hasNext()) {
                Parameter userParam = (Parameter) userParmsItr.next();
                System.out.println("Set parameter: " + userParam.name + " with value: " + userParam.value);
                // get hashmap of the desired output format
                List<Parameter> defaultParamList = (List<Parameter>)defaultParameters.get(outputFmtExt);
                Iterator<Parameter> defParmsItr = defaultParamList.iterator();
                int index = 0;
                while( defParmsItr.hasNext() )
                {
                    Parameter defParam = (Parameter) defParmsItr.next();
                    if( userParam.name.equalsIgnoreCase(defParam.name) )
                    {
                        defaultParamList.set(index, userParam);
                        break;
                    }
                    index++;
                    //hm.put(param.name, param.value); // override default parameter
                }
                
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate(final DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, List<Parameter> parameters) {
        
        // read global gimp configuration parameters from properties file
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
        
        // initialise input formats (GIF, JPEG, EPS, ...) and output formats list 
        // (GIF, JPEG, EPS, ...) and apply disambiguation ("JPG" -> "JPEG"}
        init();
        
         // set input and output extensions based upon input and output format
        // e.g. 
        // inputFormat="planets:fmt/ext/tif" -> inputFmtExt="TIFF"
        // outputFormat="planets:fmt/ext/jpg" -> outputFmtExt="JPEG"
        getExtensions(inputFormat,outputFormat);
        
        // Initialise parameters with default values
        initParameters();
        
        // override the default parameters initialised above  by the parameters 
        // passed by the user
        overrideDefaultParamets(parameters);
        
        // get binary data from digital object
        byte[] binary = null;
        InputStream inputStream = digitalObject.getContent().read();
       
        // write binary array to temporary file
        //tmpInFile = ByteArrayHelper.write(binary);
        tmpInFile = FileUtils.writeInputStreamToTmpFile(inputStream, "planets", inputFmtExt);

        System.out.println("tmpInFile: " + tmpInFile.getAbsolutePath());

        // Create inputstream from binary array
        //InputStream inputStream = new ByteArrayInputStream(binary);

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
                binary = FileUtils.readFileIntoByteArray(tmpOutFile);
            } else {
                log.error("Error: Unable to read temporary file " + tmpInFile.getPath() + tmpInFile.getName());
            }
        }
        
        // digital object output
        DigitalObject newDO = null;
        ServiceReport report = new ServiceReport();
        newDO = new DigitalObject.Builder(ImmutableContent.byValue(binary)).build();
        return new MigrateResult(newDO, report);
    }
   
    private List<Parameter> getParameters()
    {
        List<Parameter> paramList = new ArrayList<Parameter>();
        Iterator<String> itr = defaultParameters.keySet().iterator();
        while( itr.hasNext() )
        {
            String key = (String)itr.next();
            List<Parameter> parameterList = (List<Parameter>) defaultParameters.get(key);
            Iterator<Parameter> itr2 = parameterList.iterator();
            while( itr2.hasNext() )
            {
                paramList.add((Parameter) itr2.next());                
            }
        }
        return paramList;
    }
    
    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
        init();
        initParameters();
        List<Parameter> parameters = getParameters();
        ServiceDescription mds = new ServiceDescription.Builder(NAME, Migrate.class.getName())
                .author("Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at>")
                .classname(this.getClass().getCanonicalName())
//                .description(description)
                .description("This service provides file format migrations using \"The GIMP\" (GNU image manipulation program) version 2.6. " +
                            "GIMP is  the  GNU Image Manipulation Program. It is used to edit and manipulate images. It can load and save a variety of image"+
                            "formats and can be used to convert between formats."+
                            "GIMP can also be used as a paint program. It features a set of drawing and painting tools such as airbrush, clone,  pencil,  and"+
                            "paint  brush.  Painting and drawing tools can be applied to an image with a variety of paint modes.  It also offers an extensive"+
                            "array of selection tools like rectangle, ellipse, fuzzy select, bezier select, intelligent scissors, and select by color."+
                            "GIMP offers a variety of plug-ins that perform a variety of image manipulations.  Examples include bumpmap, edge  detect,  gaus-"+
                            "sian  blur, and many others. In addition, GIMP has several scripting extension which allow for advanced non-interactive process-"+
                            "ing and creation of images."+
                            "GIMP ships with a second binary called gimp-console. This binary is a console-only version and behaves as  if  gimp  was  called"+
                            "with the --no-interface command-line option. The gimp-console is the program used in this wrapper."+
                            "Currently, this service only supports migration from one file format to another, not single file format migrations. "+
                            "For example, planets:fmt/ext/tiff to planets:fmt/ext/tiff could migrate a TIFF image without compression to a TIFF image using the LZW compression type. "+
                            "Furthermore, only the conversion to GIF format supports indexed colour (applying fu-script function gimp-convert-indexed). This is not provided for the file formats BMP, PNG, and TIFF which in principle do support indexed colours as well. "+
                            "Alpha Channel transparency exists for some file formats, like GIF, PNG, BMP. If an alpha channel is encountered, the image is flattened using the gimp-image-flatten function. "+
                            "It accepts input and target formats of the form: " +
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
        Set<String> reqInputFormatExts = FormatRegistryFactory.getFormatRegistry().getExtensions(formatUri);
        Iterator<String> itrReq = reqInputFormatExts.iterator(); 
        // Iterate either over input formats ArrayList or over output formats
        // HasMap
        Iterator<String> itrGimp = (isOutput)?outputFormats.iterator():inputFormats.iterator();
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
    
    /**
     * Disambiguation (e.g. JPG -> JPEG) according to the formatMapping
     * datas structure defined in this class.
     * 
     * @param ext
     * @return Uppercase disambiguized extension string
     */
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
                            getParameterString(outputFmtExt) + 
                            ")";
        }
        return fuScriptMigrString;
    }

    private MigrationPath[] createMigrationPathwayMatrix(List<String> inputFormats, List<String> outputFormats) {
        List<MigrationPath> paths = new ArrayList<MigrationPath>();

        for (Iterator<String> iterator = inputFormats.iterator(); iterator.hasNext();) {
            String input = (String) iterator.next();

            for (Iterator<String> iterator2 = outputFormats.iterator(); iterator2.hasNext();) {
                String output = (String) iterator2.next();
                FormatRegistry registry = FormatRegistryFactory.getFormatRegistry();
                URI inFmt = registry.createExtensionUri(input);
                URI outFmt = registry.createExtensionUri(output);
                MigrationPath path = new MigrationPath(inFmt,outFmt, null);
                if( !(inFmt.toString().equals(outFmt.toString())) )
                    paths.add(path);
            }
        }
        return paths.toArray(new MigrationPath[]{});
    }
}
