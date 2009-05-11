/*
 * 
 */
package eu.planets_project.services.migration.jasper;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;



/**
 * The Jasper19Migration migrates JPEG files to JP2 files and vice versa.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService( name = Jasper19Migration.NAME ,
        serviceName = Migrate.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate" )
public final class Jasper19Migration implements Migrate {
    
    PlanetsLogger log = PlanetsLogger.getLogger(Jasper19Migration.class);
    
    
    /** The dvi ps installation dir */
    public String jasper19_install_dir;
    /** The jasper19 application name */
    public String jasper19_app_name;
    /** The output file extension */
    //public String jasper19_outfile_ext;
    private File tmpInFile;
    private File tmpOutFile;

    String inputFmtExt = null;
    String outputFmtExt = null;
    
    /***/
    static final String NAME = "Jasper19Migration";


    List<String> inputFormats = null;
    List<String> outputFormats = null;
    HashMap<String, String>  formatMapping = null;
    
    /***/
    private static final long serialVersionUID = 2127494848765937613L;

    private void init()
    {

        // input formats
        inputFormats = new ArrayList<String>();
        inputFormats.add("jpg");
        inputFormats.add("jp2");

        // output formats and associated output parameters
        outputFormats = new ArrayList<String>();
        outputFormats.add("jpg");
        outputFormats.add("jp2");

        // Disambiguation of extensions, e.g. {"JPG","JPEG"} to {"JPEG"}
        // FIXIT This should be supported by the FormatRegistryImpl class, but
        // it does not provide the complete set at the moment.
        formatMapping = new HashMap<String, String>();
        formatMapping.put("jpeg","jpg");
    }

    /**
     * {@inheritDoc}
     * 
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate( final DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, List<Parameter> parameters) {
        
        Properties props = new Properties();
        try {
            
            String strRsc = "/eu/planets_project/services/migration/jasper19/jasper19.properties";
            props.load( this.getClass().getResourceAsStream(strRsc));
            // config vars
            this.jasper19_install_dir = props.getProperty("jasper19.install.dir");
            this.jasper19_app_name = props.getProperty("jasper19.app.name");
            //this.jasper19_outfile_ext = props.getProperty("jasper19.outfile.ext");
             
        } catch( Exception e ) {
            // // config vars
            this.jasper19_install_dir  = "/usr/bin";
            this.jasper19_app_name = "jasper";
            //this.jasper19_outfile_ext = "jp2";
        }
        log.info("Using jasper19 install directory: "+this.jasper19_install_dir);
        log.info("Using jasper19 application name: "+this.jasper19_app_name);
        //log.info("Using jasper19 outfile extension: "+this.jasper19_outfile_ext);

        init();
        getExtensions(inputFormat,outputFormat);
        
        /*
         * We just return a new digital object with the same required arguments
         * as the given:
         */
        byte[] binary = null;
        InputStream inputStream = digitalObject.getContent().read();
            
            // write input stream to temporary file
            tmpInFile = FileUtils.writeInputStreamToTmpFile(inputStream, "planets", inputFmtExt);
            if( !(tmpInFile.exists() && tmpInFile.isFile() && tmpInFile.canRead() ))
            {
                log.error("[Jasper19Migration] Unable to create temporary input file!");
                return null;
            }
            log.info("[Jasper19Migration] Temporary input file created: "+tmpInFile.getAbsolutePath());

            // outfile name 
            String outFileStr = tmpInFile.getAbsolutePath()+"."+outputFmtExt;
            log.info("[Jasper19Migration] Output file name: "+outFileStr);

            // run command
            ProcessRunner runner = new ProcessRunner();
            List<String> command = new ArrayList<String>();
            // setting up command
            // Example: jasper --input testin.jpeg --input-format jpg --output-format jp2 --output testout.jp2
            // Example (short version): jasper -f testin.jpg -t jpg -F testin.jp2 -T jp2
            command.add(this.jasper19_app_name);
            command.add("--input");
            command.add(tmpInFile.getAbsolutePath());
            command.add("--input-format");
            command.add(inputFmtExt);
            command.add("--output");
            command.add(outFileStr);
            command.add("--output-format");
            command.add(outputFmtExt);
            runner.setCommand(command);
            runner.setInputStream(inputStream);
            log.info("[Jasper19Migration] Executing command: "+command.toString() +" ...");
            runner.run();
            int return_code = runner.getReturnCode();
            if (return_code != 0){
                log.error("[Jasper19Migration] Jasper conversion error code: " + Integer.toString(return_code));
                log.error("[Jasper19Migration] " + runner.getProcessErrorAsString());
                //log.error("[Jasper19Migration] Output: "+runner.getProcessOutputAsString());
                return null;
            }
            
            tmpOutFile = new File(outFileStr);
            // read byte array from temporary file
            if( tmpOutFile.isFile() && tmpOutFile.canRead() )
                binary = FileUtils.readFileIntoByteArray(tmpOutFile);
            else
                log.error( "Error: Unable to read temporary file "+tmpOutFile.getAbsolutePath() );

        DigitalObject newDO = null;
        
        ServiceReport report = new ServiceReport(Type.INFO, Status.SUCCESS, "OK");
        
        newDO = new DigitalObject.Builder(ImmutableContent.byValue(binary)).build();
        
        return new MigrateResult(newDO, report);
    }

    private void getExtensions(URI inputFormat, URI outputFormat)
    {
        if( inputFormat != null && outputFormat != null )
        {
            inputFmtExt = getFormatExt( inputFormat, false );
            outputFmtExt = getFormatExt( outputFormat, true );
        }
    }

        /**
     * Gets one extension from a set of possible extensions for the incoming
     * request planets URI (e.g. planets:fmt/ext/jpeg) which matches with
     * one format of the set of jasper's supported input/output formats. If
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
        // or can be found in the list of supported formats
        Set<String> reqInputFormatExts = FormatRegistryFactory
                .getFormatRegistry().getExtensions(formatUri);
        Iterator<String> itrReq = reqInputFormatExts.iterator();
        // Iterate either over input formats ArrayList or over output formats
        // HasMap
        Iterator<String> itrJasper = (isOutput)?outputFormats.iterator():inputFormats.iterator();
        // Iterate over possible extensions that correspond to the request
        // planets uri.
        while(itrReq.hasNext()) {
            // Iterate over the different extensions of the planets:fmt/ext/jpg
            // format URI, note that the relation of Planets-format-URI to
            // extensions is 1 : n.
            String reqFmtExt = normalizeExt((String) itrReq.next());
            while(itrJasper.hasNext()) {
                // Iterate over the formats that jasper offers either as input or
                // as output format.
                // See input formats in the this.init() method to see the
                // jasper input/output formats offered by this service.
                String gimpFmtStr = (String) itrJasper.next();
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
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
        ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());

        builder.author("Sven Schlarb <shsschlarb-planets@yahoo.de>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Simple service for Jasper Transcoder Version 1.900.1 for JPG to JP2 (JPEG2000)"+
                "and, vice versa, JP2 to JPG conversion."+
                "Jasper is a file format converter specialized in JPEG-2000 encoding"+
                "Copyright (c) 1999-2000 Image Power, Inc. and the University of"+
                "British Columbia."+
                "All rights reserved."+
                "For more information about this software, please visit the following"+
                "web sites/pages:"+
                "http://www.ece.uvic.ca/~mdadams/jasper"+
                "http://www.jpeg.org/software");
        FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
        MigrationPath[] mPaths = new MigrationPath []{
            new MigrationPath(format.createExtensionUri("jpg"), format.createExtensionUri("jp2"),null),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("jpg"),null)};
        builder.paths(mPaths);
        builder.classname(this.getClass().getCanonicalName());
        builder.version("0.1");

        ServiceDescription mds =builder.build();
        
        return mds;
    }
}
