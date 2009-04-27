/*
 *
 */
package eu.planets_project.services.migration.pdf2pdfamaycomputer;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
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
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;



/**
 * The Pdf2PdfaMayComputerMigration migrates JPEG files to JP2 files and vice versa.
 *
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService( name = Pdf2PdfaMayComputerMigration.NAME ,
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate" )
public final class Pdf2PdfaMayComputerMigration implements Migrate, Serializable {

    PlanetsLogger log = PlanetsLogger.getLogger(Pdf2PdfaMayComputerMigration.class);


    /** The dvi ps installation dir */
    public String pdf2pdfamaycomputer_install_dir;
    /** The pdf2pdfamaycomputer application name */
    public String pdf2pdfamaycomputer_app_name;
    /** The output file extension */
    //public String pdf2pdfamaycomputer_outfile_ext;
    private File tmpInFile;
    private File tmpOutFile;

    String inputFmtExt = null;
    String outputFmtExt = null;

    /***/
    static final String NAME = "Pdf2PdfaMayComputerMigration";


    List<String> inputFormats = null;
    List<String> outputFormats = null;
    HashMap<String, String>  formatMapping = null;

    /***/
    private static final long serialVersionUID = 2127494848765937613L;

    private void init()
    {

        // input formats
        inputFormats = new ArrayList<String>();
        inputFormats.add("pdf");

        // output formats and associated output parameters
        outputFormats = new ArrayList<String>();
        outputFormats.add("pdf");

        // Disambiguation of extensions, e.g. {"JPG","JPEG"} to {"JPEG"}
        // FIXIT This should be supported by the FormatRegistryImpl class, but
        // it does not provide the complete set at the moment.
        formatMapping = new HashMap<String, String>();
        formatMapping.put("pdfa","pdf");
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

            String strRsc = "/eu/planets_project/services/migration/pdf2pdfamaycomputer/pdf2pdfamaycomputer.properties";
            props.load( this.getClass().getResourceAsStream(strRsc));
            // config vars
            this.pdf2pdfamaycomputer_install_dir = props.getProperty("pdf2pdfamaycomputer.install.dir");
            this.pdf2pdfamaycomputer_app_name = props.getProperty("pdf2pdfamaycomputer.app.name");

        } catch( Exception e ) {
            // // config vars
            this.pdf2pdfamaycomputer_install_dir  = "C:\\Programme\\PDFAConverter\\";
            this.pdf2pdfamaycomputer_app_name = "PDFAConverter.exe";
        }
        log.info("Using pdf2pdfamaycomputer install directory: "+this.pdf2pdfamaycomputer_install_dir);
        log.info("Using pdf2pdfamaycomputer application name: "+this.pdf2pdfamaycomputer_app_name);

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
                log.error("[Pdf2PdfaMayComputerMigration] Unable to create temporary input file!");
                return null;
            }
            log.info("[Pdf2PdfaMayComputerMigration] Temporary input file created: "+tmpInFile.getAbsolutePath());

            // outfile name
            String outFileStr = tmpInFile.getAbsolutePath()+"."+outputFmtExt;
            log.info("[Pdf2PdfaMayComputerMigration] Output file name: "+outFileStr);

            // run command
            ProcessRunner runner = new ProcessRunner();
            List<String> command = new ArrayList<String>();
            // setting up command
            command.add(this.pdf2pdfamaycomputer_app_name);
            command.add("dst=\""+outFileStr+"\"");
            command.add("src=\""+tmpInFile.getAbsolutePath()+"\"");
            command.add("log=\""+outFileStr+".log\"");
            runner.setCommand(command);
            runner.setInputStream(inputStream);
            log.info("[Pdf2PdfaMayComputerMigration] Executing command: "+command.toString() +" ...");
            runner.run();
            int return_code = runner.getReturnCode();
            if (return_code != 0){
                log.error("[Pdf2PdfaMayComputerMigration] Jasper conversion error code: " + Integer.toString(return_code));
                log.error("[Pdf2PdfaMayComputerMigration] " + runner.getProcessErrorAsString());
                //log.error("[Pdf2PdfaMayComputerMigration] Output: "+runner.getProcessOutputAsString());
                return null;
            }

            tmpOutFile = new File(outFileStr);
            // read byte array from temporary file
            if( tmpOutFile.isFile() && tmpOutFile.canRead() )
                binary = FileUtils.readFileIntoByteArray(tmpOutFile);
            else
                log.error( "Error: Unable to read temporary file "+tmpOutFile.getAbsolutePath() );

        DigitalObject newDO = null;

        ServiceReport report = new ServiceReport();

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
     * one format of the set of pdf2pdfamaycomputer's supported input/output formats. If
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
        Iterator<String> itrDvips = (isOutput)?outputFormats.iterator():inputFormats.iterator();
        // Iterate over possible extensions that correspond to the request
        // planets uri.
        while(itrReq.hasNext()) {
            // Iterate over the different extensions of the planets:fmt/ext/jpg
            // format URI, note that the relation of Planets-format-URI to
            // extensions is 1 : n.
            String reqFmtExt = normalizeExt((String) itrReq.next());
            while(itrDvips.hasNext()) {
                // Iterate over the formats that pdf2pdfamaycomputer offers either as input or
                // as output format.
                // See input formats in the this.init() method to see the
                // pdf2pdfamaycomputer input/output formats offered by this service.
                String gimpFmtStr = (String) itrDvips.next();
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
        builder.description("Simple service for pdf2pdfamaycomputer.");
        FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
        MigrationPath[] mPaths = new MigrationPath []{
            new MigrationPath(format.createExtensionUri("pdf"), format.createExtensionUri("pdf"),null)};
        builder.paths(mPaths);
        builder.classname(this.getClass().getCanonicalName());
        builder.version("0.1");

        ServiceDescription mds =builder.build();

        return mds;
    }
}
