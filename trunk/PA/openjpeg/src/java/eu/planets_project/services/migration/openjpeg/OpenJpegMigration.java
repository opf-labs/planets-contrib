package eu.planets_project.services.migration.openjpeg;

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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 * The OpenJpegMigration migrates JPEG files to JP2 files.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
@Local(Migrate.class)
@Remote(Migrate.class) 
@Stateless
@WebService( name = OpenJpegMigration.NAME ,
        serviceName = Migrate.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate" )
public final class OpenJpegMigration implements Migrate, Serializable {
    
    PlanetsLogger log = PlanetsLogger.getLogger(OpenJpegMigration.class);
    
    
    /** The dvi ps installation dir */
    public String openjpeg_install_dir;
    /** The openjpeg application name */
    public String openjpeg_app_name;
    /** The output file extension */
    public String openjpeg_outfile_ext;
    private File tmpInFile;
    private File tmpOutFile;

    String inputFmtExt = null;
    String outputFmtExt = null;
    
    /***/
    static final String NAME = "OpenJpegMigration";


    List<String> inputFormats = null;
    List<String> outputFormats = null;
    HashMap<String, String>  formatMapping = null;
    
    /***/
    private static final long serialVersionUID = 2127494848765937613L;

    private void init()
    {

        // input formats
        inputFormats = new ArrayList<String>();
        inputFormats.add("tif");
        inputFormats.add("jp2");

        // output formats and associated output parameters
        outputFormats = new ArrayList<String>();
        outputFormats.add("tif");
        outputFormats.add("jp2");

        // Disambiguation of extensions, e.g. {"JPG","JPEG"} to {"JPEG"}
        // FIXIT This should be supported by the FormatRegistryImpl class, but
        // it does not provide the complete set at the moment.
        formatMapping = new HashMap<String, String>();
        formatMapping.put("tiff","tif");
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
            
            String strRsc = "/eu/planets_project/services/migration/openjpeg/openjpeg.properties";
            props.load( this.getClass().getResourceAsStream(strRsc));
            // config vars
            this.openjpeg_install_dir = props.getProperty("openjpeg.install.dir");
            this.openjpeg_app_name = props.getProperty("openjpeg.app.name");
            this.openjpeg_outfile_ext = props.getProperty("openjpeg.outfile.ext");
             
        } catch( Exception e ) {
            // // config vars
            this.openjpeg_install_dir  = "/usr/bin";
            this.openjpeg_app_name = "image_to_j2k";
            this.openjpeg_outfile_ext = "jp2";
        }
        log.info("Using openjpeg install directory: "+this.openjpeg_install_dir);
        log.info("Using openjpeg application name: "+this.openjpeg_app_name);
        log.info("Using openjpeg outfile extension: "+this.openjpeg_outfile_ext);

        init();
        getExtensions(inputFormat,outputFormat);
        
        /*
         * We just return a new digital object with the same required arguments
         * as the given:
         */
       
        // 29.01.09: replaced deprecated method getValue()
        //byte[] binary = digitalObject.getContent().getValue();
        byte[] binary = null;
        InputStream inputStream = digitalObject.getContent().read();
        try {
            
            // write binary array to temporary file
            tmpInFile = FileUtils.writeInputStreamToTmpFile(inputStream, "planets", inputFmtExt);
            if( !(tmpInFile.exists() && tmpInFile.isFile() && tmpInFile.canRead() ))
            {
                System.out.println("Error: corrupt file!");
                return null;
            }
            
            // outfile name = infilename + extension
            String outFileStr = tmpInFile.getAbsolutePath()+"."+outputFmtExt;
            // temporary outfile
            tmpOutFile = new File(outFileStr);
            
            //InputStream inputStream = new ByteArrayInputStream(binary);
            
            ProcessRunner runner = new ProcessRunner();
            List<String> command = new ArrayList<String>();
            // setting up command
            // Example: image_to_j2k -i testin.tif -o neutest.jp2
            if( inputFmtExt.equalsIgnoreCase("jp2"))
                this.openjpeg_app_name = "j2k_to_image";
            else if(inputFmtExt.equalsIgnoreCase("tif"))
                this.openjpeg_app_name = "image_to_j2k";
            command.add(this.openjpeg_app_name);
            command.add("-i");
            command.add(tmpInFile.getAbsolutePath());
            command.add("-o");
            command.add(tmpOutFile.getAbsolutePath());
            
            runner.setCommand(command);


            runner.setInputStream(inputStream);
            
            runner.run();
            
            int return_code = runner.getReturnCode();

            if (return_code != 0){
                System.out.println("OpenJpeg conversion error code: " + Integer.toString(return_code));
                System.out.println(runner.getProcessErrorAsString());
                System.out.println(runner.getProcessOutputAsString());
                return null;
            }


            // read byte array from temporary file
            if( tmpOutFile.isFile() && tmpOutFile.canRead() )
                binary = readByteArrayFromTmpFile(); 
            else
                System.out.println( "Error: Unable to read temporary file "
                        +tmpOutFile.getAbsolutePath() );
            
        } catch(IOException e) {
            log.error( "IO Error:" + e.toString() );
        } finally {
            
        }
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
     * one format of the set of OpenJpegs's supported input/output formats. If
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
        Iterator<String> itrOpenJpeg = (isOutput)?outputFormats.iterator():inputFormats.iterator();
        // Iterate over possible extensions that correspond to the request
        // planets uri.
        while(itrReq.hasNext()) {
            // Iterate over the different extensions of the planets:fmt/ext/jpg
            // format URI, note that the relation of Planets-format-URI to
            // extensions is 1 : n.
            String reqFmtExt = normalizeExt((String) itrReq.next());
            while(itrOpenJpeg.hasNext()) {
                // Iterate over the formats that openjpeg offers either as input or
                // as output format.
                // See input formats in the this.init() method to see the
                // openjpeg input/output formats offered by this service.
                String gimpFmtStr = (String) itrOpenJpeg.next();
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
        builder.description("Simple service for OpenJpeg for TIF to JP2 (JPEG2000) and, vice versa, JP2 to TIF conversion.");
        MigrationPath[] mPaths = new MigrationPath []{
            new MigrationPath(Format.extensionToURI("tif"), Format.extensionToURI("jp2"),null),
            new MigrationPath(Format.extensionToURI("jp2"), Format.extensionToURI("tif"),null)};
        builder.paths(mPaths);
        builder.classname(this.getClass().getCanonicalName());
        builder.version("0.1");

        ServiceDescription mds =builder.build();
        
        return mds;
    }
    
    /* (non-Javadoc)
     */
    synchronized void writeByteArrayToTmpFile( byte[] binary ) throws IOException {
            tmpInFile = ByteArrayHelper.write(binary);
            if( tmpInFile.exists() )
                log.info("Temporary input file created: " + tmpInFile.getAbsolutePath());
            else
                log.error("Unable to create temp file");
    }

    

    /* (non-Javadoc)
     */
    synchronized byte[] readByteArrayFromTmpFile() throws IOException {
        byte[] binary = ByteArrayHelper.read(tmpOutFile);
        return binary;
    }



}