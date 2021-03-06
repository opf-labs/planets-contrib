/*
 *
 */
package eu.planets_project.services.migration.inkscape;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
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

import org.apache.commons.io.FileUtils;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.ProcessRunner;



/**
 * The InkscapeMigration migrates JPEG files to JP2 files and vice versa.
 *
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService( name = InkscapeMigration.NAME ,
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate" )
public final class InkscapeMigration implements Migrate {

    private static Logger log = Logger.getLogger(InkscapeMigration.class.getName());


    /** The dvi ps installation dir */
    public String inkscape_install_dir;
    /** The inkscape application name */
    public String inkscape_app_name;
    /** The output file extension */
    //public String inkscape_outfile_ext;
    private File tmpInFile;
    private File tmpOutFile;
    private final FormatRegistry format = FormatRegistryFactory.getFormatRegistry();

    String inputFmtExt = null;
    String outputFmtExt = null;

    /***/
    static final String NAME = "InkscapeMigration";


    List<String> inputFormats = null;
    List<String> outputFormats = null;
    HashMap<String, String>  formatMapping = null;

    /***/
    private static final long serialVersionUID = 2127494848765937613L;

    private void init()
    {
        // input formats
        inputFormats = new ArrayList<String>();
        inputFormats.add("svg");

        // output formats and associated output parameters
        outputFormats = new ArrayList<String>();
        outputFormats.add("ps");
        outputFormats.add("eps");
        outputFormats.add("pdf");
        outputFormats.add("png");

        formatMapping = new HashMap<String, String>();
        formatMapping.put("svg","svg");
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

            String strRsc = "/eu/planets_project/services/migration/inkscape/inkscape.properties";
            props.load( this.getClass().getResourceAsStream(strRsc));
            // config vars
            this.inkscape_install_dir = props.getProperty("inkscape.install.dir");
            this.inkscape_app_name = props.getProperty("inkscape.app.name");

        } catch( Exception e ) {
            // // config vars
            this.inkscape_install_dir  = "/usr/bin";
            this.inkscape_app_name = "inkscape";
        }
        log.info("Using inkscape install directory: "+this.inkscape_install_dir);
        log.info("Using inkscape application name: "+this.inkscape_app_name);

        init();
        getExtensions(inputFormat,outputFormat);

        /*
         * We just return a new digital object with the same required arguments
         * as the given:
         */
        byte[] binary = null;
        InputStream inputStream = digitalObject.getContent().getInputStream();

             // write input object to temporary file        
            try { 
                /* TODO If extension not really needed, use DigitalObjectUtils.toFile(DigitalObject). */
                String suffix = inputFmtExt.startsWith(".") ? inputFmtExt : "." + inputFmtExt;
                tmpInFile = File.createTempFile("planets", suffix);
                DigitalObjectUtils.toFile(digitalObject, tmpInFile);
            } catch (IOException x) {
                throw new IllegalStateException("Could not create temp file.", x);
            }
            if( !(tmpInFile.exists() && tmpInFile.isFile() && tmpInFile.canRead() ))
            {
                log.severe("[InkscapeMigration] Unable to create temporary input file!");
                return null;
            }
            log.info("[InkscapeMigration] Temporary input file created: "+tmpInFile.getAbsolutePath());

            // outfile name
            String outFileStr = tmpInFile.getAbsolutePath()+"."+outputFmtExt;
            log.info("[InkscapeMigration] Output file name: "+outFileStr);

            // run command
            ProcessRunner runner = new ProcessRunner();
            List<String> command = new ArrayList<String>();
            // setting up command
            // Example: inkscape -z -A out.pdf testfile.svg
            // Exportparameter -P for *.ps
            // Exportparameter -E for *.eps
            // Exportparameter -A for *.pdf
            // Exportparameter -e for *.png
            String exportParameter = null;
            if( outputFmtExt.equalsIgnoreCase("ps") )
                exportParameter = "-P";
            else if( outputFmtExt.equalsIgnoreCase("eps") )
                exportParameter = "-E";
            else if( outputFmtExt.equalsIgnoreCase("pdf") )
                exportParameter = "-A";
            else if( outputFmtExt.equalsIgnoreCase("png") )
                exportParameter = "-e";
            command.add(this.inkscape_app_name);
            // use command line, not gui
            command.add("-z");
            if(exportParameter != null)
                command.add(exportParameter);
            command.add(outFileStr);
            command.add(tmpInFile.getAbsolutePath());
            runner.setCommand(command);
            runner.setInputStream(inputStream);
            log.info("[InkscapeMigration] Executing command: "+command.toString() +" ...");
            runner.run();
            int return_code = runner.getReturnCode();
            if (return_code != 0){
                log.severe("[InkscapeMigration] Inkscape conversion error code: " + Integer.toString(return_code));
                log.severe("[InkscapeMigration] " + runner.getProcessErrorAsString());
                //log.severe("[InkscapeMigration] Output: "+runner.getProcessOutputAsString());
                return null;
            }

            tmpOutFile = new File(outFileStr);
            // read byte array from temporary file
            if( tmpOutFile.isFile() && tmpOutFile.canRead() )
                try {
                    binary = FileUtils.readFileToByteArray(tmpOutFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            else
                log.severe( "Error: Unable to read temporary file "+tmpOutFile.getAbsolutePath() );

        DigitalObject newDO = null;

        ServiceReport report = new ServiceReport(Type.INFO, Status.SUCCESS, "OK");

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
     * one format of the set of inkscape's supported input/output formats. If
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
        Set<String> reqInputFormatExts = format.getExtensions(formatUri);
        Iterator<String> itrReq = reqInputFormatExts.iterator();
        // Iterate either over input formats ArrayList or over output formats
        // HasMap
        Iterator<String> itrInkscape = (isOutput)?outputFormats.iterator():inputFormats.iterator();
        // Iterate over possible extensions that correspond to the request
        // planets uri.
        while(itrReq.hasNext()) {
            // Iterate over the different extensions of the planets:fmt/ext/jpg
            // format URI, note that the relation of Planets-format-URI to
            // extensions is 1 : n.
            String reqFmtExt = normalizeExt((String) itrReq.next());
            while(itrInkscape.hasNext()) {
                // Iterate over the formats that inkscape offers either as input or
                // as output format.
                // See input formats in the this.init() method to see the
                // inkscape input/output formats offered by this service.
                String gimpFmtStr = (String) itrInkscape.next();
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
        builder.description("Inkscape is an SVG (Scalable Vector Graphics) editing program. It provides migration from" +
                "SVG to PS, EPS, PDF, and PNG.");
        MigrationPath[] mPaths = new MigrationPath []{
            new MigrationPath(format.createExtensionUri("svg"), format.createExtensionUri("ps"),null),
            new MigrationPath(format.createExtensionUri("svg"), format.createExtensionUri("eps"),null),
            new MigrationPath(format.createExtensionUri("svg"), format.createExtensionUri("pdf"),null),
            new MigrationPath(format.createExtensionUri("svg"), format.createExtensionUri("png"),null)
        };
        builder.paths(mPaths);
        builder.classname(this.getClass().getCanonicalName());
        builder.version("0.1");

        ServiceDescription mds =builder.build();

        return mds;
    }
}
