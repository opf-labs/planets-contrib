package eu.planets_project.services.migration.ps2pdf;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.*;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;
import eu.planets_project.services.utils.cli.CliMigrationPaths;
import org.xml.sax.SAXException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingType;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * The class migrates between a number of formats
 * @author Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@WebService(
        name = Ps2PdfMigration.NAME,
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class Ps2PdfMigration implements Migrate, Serializable {

    PlanetsLogger log = PlanetsLogger.getLogger(Ps2PdfMigration.class);

    /**
     * The service name.
     */
    static final String NAME = "Ps2PdfMigration";
    static final String configfile = "ps2pdf.paths.xml";


    private CliMigrationPaths migrationPaths = null;

    /**
     * {@inheritDoc}
     *
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate(final DigitalObject digitalObject,
                                 URI inputFormat, URI outputFormat, List<Parameter> parameters) {


        ServiceReport report = new ServiceReport();


        String command = migrationPaths.findMigrationCommand(inputFormat,outputFormat);

        if (command == null){
            report.setError("Could not find a migrationPath for the input and output formats");
            return fail(report);
        }


        //log.info("Using ps2pdf application name: "+this.ps2pdf_app_name);


        /*
         * We just return a new digital object with the same required arguments
         * as the given:
         */


        InputStream psfile = digitalObject.getContent().read();



        ProcessRunner runner = new ProcessRunner();

        runner.setCommand(Arrays.asList("/bin/sh","-c",command));

        runner.setInputStream(psfile);
        runner.setCollection(true);
        runner.setOutputCollectionByteSize(-1);



        runner.run();
        int return_code = runner.getReturnCode();



        if (return_code != 0){
            report.setErrorState(return_code);
            report.setError(runner.getProcessOutputAsString()+"\n"+runner.getProcessErrorAsString());
            return fail(report);
        }
        InputStream newFileStream = runner.getProcessOutput();
        byte[] outbytes = FileUtils.writeInputStreamToBinary(newFileStream);

        DigitalObject pdfFile = new DigitalObject
                .Builder(digitalObject)
                .content(Content.byValue(outbytes))
                .format(outputFormat)
                .build();
        return new MigrateResult(pdfFile,report);

    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     * @return ServiceDescription
     */
    public ServiceDescription describe() {

        ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());
        builder.paths(migrationPaths.getAsPlanetsPaths());

        builder.author("Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Converts between a number of image formats");

        builder.version("0.1");

        return builder.build();

    }

    public Ps2PdfMigration()  {

        try {
            if (migrationPaths == null){
                migrationPaths = CliMigrationPaths.initialiseFromFile(configfile);
            }
        } catch (ParserConfigurationException e) {
            throw new Error("Problem invoking xml parser",e);
        } catch (IOException e) {
            throw new Error("Unable to read the properties file",e);
        } catch (SAXException e) {
            throw new Error("Unable to parse the xml in the properties file",e);
        } catch (URISyntaxException e) {
            throw new Error("Invalid URI in the properties fiel",e);
        }
    }


    private MigrateResult fail(ServiceReport report){
        if (report.getErrorState() == 0){
            report.setErrorState(1);
        }
        return new MigrateResult(null,report);
    }
}