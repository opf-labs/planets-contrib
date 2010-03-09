package eu.planets_project.services.migration.ffmpeg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.MTOM;

import org.xml.sax.SAXException;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.ProcessRunner;
import eu.planets_project.services.utils.ServiceUtils;
import eu.planets_project.services.utils.cli.CliMigrationPaths;

/**
 * The class migrates between a number of formats
 * @author Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>
 */
@Stateless
@WebService(
        name = FFMpegMigration.NAME,
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
@MTOM
@StreamingAttachment( parseEagerly=true, memoryThreshold=ServiceUtils.JAXWS_SIZE_THRESHOLD )
public class FFMpegMigration implements Migrate {

    private static Logger log = Logger.getLogger(FFMpegMigration.class.getName());

    /**
     * The service name.
     */
    static final String NAME = "FFMpegMigration";
    static final String configfile = "ffmpeg.paths.xml";


    private CliMigrationPaths migrationPaths = null;

    /**
     * {@inheritDoc}
     *
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate(final DigitalObject digitalObject,
                                 URI inputFormat, URI outputFormat, List<Parameter> parameters) {


        ServiceReport report = new ServiceReport(Type.INFO, Status.SUCCESS, "OK");
        try {
            init();
        } catch (URISyntaxException e) {
            log.severe("Invalid URI in the paths file"+": " + e.getMessage());
            return fail(null);
        }


        String command = migrationPaths.findMigrationCommand(inputFormat, outputFormat);

        if (command == null){
            report = new ServiceReport(Type.ERROR, Status.TOOL_ERROR,
                    "Could not find a migrationPath for the input and output formats");
            return fail(report);
        }


        //log.info("Using ps2pdf application name: "+this.ps2pdf_app_name);


        /*
         * We just return a new digital object with the same required arguments
         * as the given:
         */


        InputStream psfile = digitalObject.getContent().getInputStream();



        ProcessRunner runner = new ProcessRunner();

        runner.setCommand(Arrays.asList("/bin/sh","-c",command));

        runner.setInputStream(psfile);
        runner.setCollection(true);
        runner.setOutputCollectionByteSize(-1);



        runner.run();
        int return_code = runner.getReturnCode();



        if (return_code != 0){
            report = new ServiceReport(Type.ERROR, Status.INSTALLATION_ERROR,
                    runner.getProcessOutputAsString() + "\n"
                            + runner.getProcessErrorAsString());
            return fail(report);
        }
        InputStream newFileStream = runner.getProcessOutput();
        byte[] outbytes = FileUtils.writeInputStreamToBinary(newFileStream);

        DigitalObject pdfFile = new DigitalObject.Builder(Content.byValue(outbytes)).build();
        return new MigrateResult(pdfFile,report);

    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     * @return ServiceDescription
     */
    public ServiceDescription describe() {

        ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());
        try {
            init();
            builder.paths(migrationPaths.getAsPlanetsPaths());
        } catch (URISyntaxException e) {
            log.warning("Invalid URI in the paths file"+": "+e.getMessage());
        }

        builder.author("Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Converts between a number of image formats");

        builder.version("0.1");

        return builder.build();

    }

    private void init() throws URISyntaxException {
        try {
            if (migrationPaths == null){
                migrationPaths = CliMigrationPaths.initialiseFromFile(configfile);
            }
        } catch (ParserConfigurationException e) {
            throw new Error("Not supposed to happen",e);
        } catch (IOException e) {
            throw new Error("Not supposed to happen",e);
        } catch (SAXException e) {
            throw new Error("Not supposed to happen",e);
        }
    }


    private MigrateResult fail(ServiceReport report){
        return new MigrateResult(null,report);
    }
}