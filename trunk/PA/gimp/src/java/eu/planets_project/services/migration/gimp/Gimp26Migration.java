/*
 * 
 */
package eu.planets_project.services.migration.gimp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
    public String gimp_outfile_ext;
    private File tmpInFile;
    private File tmpOutFile;
    /***/
    static final String NAME = "Gimp26Migration";
    /***/
    private static final long serialVersionUID = 2127494848765937613L;

    /**
     * {@inheritDoc}
     * 
     * @see eu.planets_project.ifr.core.common.services.migrate.MigrateOneDigitalObject#migrate(eu.planets_project.ifr.core.common.services.datatypes.DigitalObject)
     */
    public MigrateResult migrate(final DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, Parameters parameters) {

        System.out.println(inputFormat.toString());
        System.out.println(outputFormat.toString());

        Properties props = new Properties();
        try {
            String strRsc = "/eu/planets_project/services/migration/gimp/gimp26.properties";
            props.load(this.getClass().getResourceAsStream(strRsc));
            // config vars
            this.gimp_install_dir = props.getProperty("gimp.install.dir");
            this.gimp_app_name = props.getProperty("gimp.app.name");
            this.gimp_outfile_ext = props.getProperty("gimp.outfile.ext");

        } catch (Exception e) {
            // // config vars
            this.gimp_install_dir = "/home/georg/Projects/PLANETS/";
            this.gimp_app_name = "dmmConvertPNGtoJPG.sh";
            this.gimp_outfile_ext = "tmp";
        }
        log.info("Using gimp install directory: " + this.gimp_install_dir);
        log.info("Using gimp application name: " + this.gimp_app_name);
        log.info("Using gimp application name: " + this.gimp_app_name);

        /*
         * We just return a new digital object with the same required arguments
         * as the given:
         */
        byte[] binary = digitalObject.getContent().getValue();

        try {

            // write binary array to temporary file
            writeByteArrayToTmpFile(binary);

            // outfile name = infilename + ".ps" extension
            String outFileStr = tmpInFile.getAbsolutePath() + "." + gimp_outfile_ext;

            // temporary outfile
            tmpOutFile = new File(outFileStr);

            InputStream inputStream = new ByteArrayInputStream(binary);

            ProcessRunner runner = new ProcessRunner();
            List<String> command = new ArrayList<String>();

            if (inputFormat.equals(Format.extensionToURI("PNG")) && outputFormat.equals(Format.extensionToURI("JPG"))) {
                command.add(gimp_install_dir + gimp_app_name);
                command.add(tmpInFile.getAbsolutePath());
                command.add(tmpInFile.getAbsolutePath() + ".jpg");
            } else if (inputFormat.equals(Format.extensionToURI("JPG")) && outputFormat.equals(Format.extensionToURI("PNG"))) {
                command.add(gimp_install_dir + "dmmConvertJPGtoPNG.sh");
                command.add(tmpInFile.getAbsolutePath());
                command.add(tmpInFile.getAbsolutePath() + ".png");
            }

            runner.setCommand(command);
            runner.setInputStream(inputStream);

            runner.run();

            int return_code = runner.getReturnCode();

            if (return_code != 0) {
                log.error("Gimp conversion error code: " + Integer.toString(return_code));
            }

            // read byte array from temporary file
            if (tmpInFile.isFile() && tmpInFile.canRead()) {
                binary = readByteArrayFromTmpFile();
            } else {
                log.error("Error: Unable to read temporary file " + tmpInFile.getPath() + tmpInFile.getName());
            }
        } catch (IOException e) {
            log.error("IO Error:" + e.toString());
        } finally {
        }
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
        List<Parameter> parameterList = new ArrayList<Parameter>();
        Parameter compressionTypeParam = new Parameter("compressionType", "0-10");
        compressionTypeParam.setDescription("Allowed int values: 0 - 10");
        parameterList.add(compressionTypeParam);

        Parameter compressionLevelParam = new Parameter("compressionQuality", "0-100");
        compressionLevelParam.setDescription("This should be an int value between: 0 - 100, representing the compression quality in percent.");
        parameterList.add(compressionLevelParam);

        Parameters parameters = new Parameters();
        parameters.setParameters(parameterList);

        // input formats
        List<String> inputFormats = new ArrayList<String>();
        inputFormats.add("JPEG");
        inputFormats.add("TIFF");
        inputFormats.add("GIF");
        inputFormats.add("PNG");
        inputFormats.add("BMP");
        inputFormats.add("RAW");
        inputFormats.add("PCX");
        inputFormats.add("TGA");
        inputFormats.add("PCD");
        inputFormats.add("PDF");

        // output formats
        List<String> outputFormats = new ArrayList<String>();
        outputFormats.add("TIFF");
        outputFormats.add("PNG");
        outputFormats.add("JPEG");
        outputFormats.add("GIF");
        outputFormats.add("PDF");

        ServiceDescription mds = new ServiceDescription.Builder(NAME, Migrate.class.getName()).author("Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at>").classname(this.getClass().getCanonicalName()).description("Wrapper for GIMP version 2.6").version("0.1").parameters(parameters).paths(createMigrationPathwayMatrix(inputFormats, outputFormats)).build();
        return mds;
    }

    /* (non-Javadoc)
     */
    synchronized void writeByteArrayToTmpFile(byte[] binary) throws IOException {
        tmpInFile = ByteArrayHelper.write(binary);
        if (tmpInFile.exists()) {
            log.info("Temporary input file created: " + tmpInFile.getAbsolutePath());
        } else {
            log.error("Unable to create temp file");
        }
    }

    /* (non-Javadoc)
     */
    synchronized byte[] readByteArrayFromTmpFile() throws IOException {
        String strOutFile = tmpInFile.getAbsolutePath() + "." + gimp_outfile_ext;
        tmpOutFile = new File(strOutFile);
        byte[] binary = ByteArrayHelper.read(tmpInFile);
        return binary;
    }

    private MigrationPath[] createMigrationPathwayMatrix(List<String> inputFormats, List<String> outputFormats) {
        List<MigrationPath> paths = new ArrayList<MigrationPath>();

        for (Iterator iterator = inputFormats.iterator(); iterator.hasNext();) {
            String input = (String) iterator.next();

            for (Iterator iterator2 = outputFormats.iterator(); iterator2.hasNext();) {
                String output = (String) iterator2.next();
                MigrationPath path = new MigrationPath(Format.extensionToURI(input),
                        Format.extensionToURI(output), null);

                paths.add(path);
            }
        }
        return paths.toArray(new MigrationPath[]{});
    }
}
