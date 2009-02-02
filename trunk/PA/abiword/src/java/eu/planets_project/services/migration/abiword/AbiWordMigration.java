/*
 * 
 */
package eu.planets_project.services.migration.abiword;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import eu.planets_project.ifr.core.techreg.impl.formats.FormatRegistryImpl;
import java.io.Serializable;
import java.net.URI;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 * The AbiWordMigration migrates JPEG files to JP2 files and vice versa.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService( name = AbiWordMigration.NAME ,
        serviceName = Migrate.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate" )
public final class AbiWordMigration implements Migrate, Serializable {
    
    PlanetsLogger log = PlanetsLogger.getLogger(AbiWordMigration.class);
    
    
    /** The dvi ps installation dir */
    public String abiword_install_dir;
    /** The abiword application name */
    public String abiword_app_name;
    private File tmpInFile;
    private File tmpOutFile;

    String inputFmtExt = null;
    String outputFmtExt = null;
    
    /***/
    static final String NAME = "AbiWordMigration";


    List<String> inputFormats = null;
    List<String> outputFormats = null;
    HashMap<String, String>  formatMapping = null;
    
    /***/
    private static final long serialVersionUID = 2127494848765937613L;

    private void init()
    {

        // input formats
        inputFormats = new ArrayList<String>();
        inputFormats.add("doc");
        inputFormats.add("html");
        inputFormats.add("pdf");
        inputFormats.add("rtf");
        inputFormats.add("txt");

        // output formats and associated output parameters
        outputFormats = new ArrayList<String>();
        outputFormats.add("doc");
        outputFormats.add("html");
        outputFormats.add("pdf");
        outputFormats.add("rtf");
        outputFormats.add("txt");

        // Disambiguation of extensions, e.g. {"JPG","JPEG"} to {"JPEG"}
        // FIXIT This should be supported by the FormatRegistryImpl class, but
        // it does not provide the complete set at the moment.
        formatMapping = new HashMap<String, String>();
        formatMapping.put("htm","html");
    }

    /**
     * {@inheritDoc}
     * 
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameters)
     */
    public MigrateResult migrate( final DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, Parameters parameters) {
        
        Properties props = new Properties();
        try {
            
            String strRsc = "/eu/planets_project/services/migration/abiword/abiword.properties";
            props.load( this.getClass().getResourceAsStream(strRsc));
            // config vars
            this.abiword_install_dir = props.getProperty("abiword.install.dir");
            this.abiword_app_name = props.getProperty("abiword.app.name");
             
        } catch( Exception e ) {
            // // config vars
            this.abiword_install_dir  = "/usr/bin";
            this.abiword_app_name = "abiword";
        }
        log.info("Using abiword install directory: "+this.abiword_install_dir);
        log.info("Using abiword application name: "+this.abiword_app_name);

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
                log.error("[AbiWordMigration] Unable to create temporary input file!");
                return null;
            }
            log.info("[AbiWordMigration] Temporary input file created: "+tmpInFile.getAbsolutePath());

            // outfile name 
            String outFileStr = tmpInFile.getAbsolutePath()+"."+outputFmtExt;
            log.info("[AbiWordMigration] Output file name: "+outFileStr);

            // run command
            ProcessRunner runner = new ProcessRunner();
            List<String> command = new ArrayList<String>();
            // setting up command
            command.add(this.abiword_app_name);
            command.add("--to="+outFileStr);
            command.add(tmpInFile.getAbsolutePath());
            runner.setCommand(command);
            runner.setInputStream(inputStream);
            log.info("[AbiWordMigration] Executing command: "+command.toString() +" ...");
            runner.run();
            int return_code = runner.getReturnCode();
            if (return_code != 0){
                log.error("[AbiWordMigration] Abiword conversion error code: " + Integer.toString(return_code));
                log.error("[AbiWordMigration] " + runner.getProcessErrorAsString());
                //log.error("[AbiWordMigration] Output: "+runner.getProcessOutputAsString());
                return null;
            }
            
            tmpOutFile = new File(outFileStr);
            // read byte array from temporary file
            if( tmpOutFile.isFile() && tmpOutFile.canRead() )
                binary = ByteArrayHelper.read(tmpOutFile);
            else
                log.error( "Error: Unable to read temporary file "+tmpOutFile.getAbsolutePath() );

        DigitalObject newDO = null;
        
        ServiceReport report = new ServiceReport();
        
        newDO = new DigitalObject.Builder(Content.byValue(binary)).build();
        
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
     * one format of the set of abiword's supported input/output formats. If
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
        FormatRegistryImpl fmtRegImpl = new FormatRegistryImpl();
        Format uriFormatObj = fmtRegImpl.getFormatForURI(formatUri);
        Set<String> reqInputFormatExts = uriFormatObj.getExtensions();
        Iterator<String> itrReq = reqInputFormatExts.iterator();
        // Iterate either over input formats ArrayList or over output formats
        // HasMap
        Iterator<String> itrAbiword = (isOutput)?outputFormats.iterator():inputFormats.iterator();
        // Iterate over possible extensions that correspond to the request
        // planets uri.
        while(itrReq.hasNext()) {
            // Iterate over the different extensions of the planets:fmt/ext/jpg
            // format URI, note that the relation of Planets-format-URI to
            // extensions is 1 : n.
            String reqFmtExt = normalizeExt((String) itrReq.next());
            while(itrAbiword.hasNext()) {
                // Iterate over the formats that abiword offers either as input or
                // as output format.
                // See input formats in the this.init() method to see the
                // abiword input/output formats offered by this service.
                String gimpFmtStr = (String) itrAbiword.next();
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
        builder.description("Simple service for Abiword (Version: GNOME AbiWord-2.4 2.4.6) "+
                "document conversions between the formats doc, html, pdf, rtf, txt (all directions)."+
                "AbiWord can convert documents in one format (it doesn't have to be "+
                "AbiWord, just a format AbiWord supports) to OpenOffice.org Writer, "+
                "or Word, or any of a heck of a lot other formats. Even better, you "+
                "can do these conversions via the command line. For instance, let's "+
                "say you have a Word document named foo.doc, and you want to convert "+
                "it to AbiWord format without having to open the program. This is the" +
                "functionality used by this wrapper. The main strength of Abiword is"+
                "that you can write additional so called exporters which add new"+
                "migration functionality. Therefore, this service really extensible" +
                "in the functionality it offers.");
        MigrationPath[] mPaths = new MigrationPath []{
            new MigrationPath(Format.extensionToURI("doc"), Format.extensionToURI("html"),null),
            new MigrationPath(Format.extensionToURI("doc"), Format.extensionToURI("pdf"),null),
            new MigrationPath(Format.extensionToURI("doc"), Format.extensionToURI("rtf"),null),
            new MigrationPath(Format.extensionToURI("doc"), Format.extensionToURI("txt"),null),
            new MigrationPath(Format.extensionToURI("html"), Format.extensionToURI("doc"),null),
            new MigrationPath(Format.extensionToURI("html"), Format.extensionToURI("pdf"),null),
            new MigrationPath(Format.extensionToURI("html"), Format.extensionToURI("rtf"),null),
            new MigrationPath(Format.extensionToURI("html"), Format.extensionToURI("txt"),null),
            new MigrationPath(Format.extensionToURI("pdf"), Format.extensionToURI("doc"),null),
            new MigrationPath(Format.extensionToURI("pdf"), Format.extensionToURI("html"),null),
            new MigrationPath(Format.extensionToURI("pdf"), Format.extensionToURI("rtf"),null),
            new MigrationPath(Format.extensionToURI("pdf"), Format.extensionToURI("txt"),null),
            new MigrationPath(Format.extensionToURI("rtf"), Format.extensionToURI("doc"),null),
            new MigrationPath(Format.extensionToURI("rtf"), Format.extensionToURI("html"),null),
            new MigrationPath(Format.extensionToURI("rtf"), Format.extensionToURI("pdf"),null),
            new MigrationPath(Format.extensionToURI("rtf"), Format.extensionToURI("txt"),null),
            new MigrationPath(Format.extensionToURI("txt"), Format.extensionToURI("doc"),null),
            new MigrationPath(Format.extensionToURI("txt"), Format.extensionToURI("html"),null),
            new MigrationPath(Format.extensionToURI("txt"), Format.extensionToURI("pdf"),null),
            new MigrationPath(Format.extensionToURI("txt"), Format.extensionToURI("rtf"),null)
        };
        builder.paths(mPaths);
        builder.classname(this.getClass().getCanonicalName());
        builder.version("0.1");

        ServiceDescription mds =builder.build();
        
        return mds;
    }
}
