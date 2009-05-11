package eu.planets_project.services.migration.ps2pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingType;

import org.xml.sax.SAXException;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ImmutableContent;
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
import eu.planets_project.services.utils.cli.CliMigrationPaths;

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


        String command = migrationPaths.findMigrationCommand(inputFormat,outputFormat);

        if (command == null){
            return returnWith(new ServiceReport(Type.ERROR, Status.TOOL_ERROR,
                    "Could not find a migrationPath for the input and output formats"));
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
            return returnWith(new ServiceReport(Type.ERROR, Status.INSTALLATION_ERROR,
                    runner.getProcessOutputAsString()+"\n"+runner.getProcessErrorAsString()));
        }
        InputStream newFileStream = runner.getProcessOutput();
        byte[] outbytes = FileUtils.writeInputStreamToBinary(newFileStream);

        DigitalObject pdfFile = new DigitalObject
                .Builder(digitalObject)
                .content(ImmutableContent.byValue(outbytes))
                .format(outputFormat)
                .build();
        return new MigrateResult(pdfFile, new ServiceReport(Type.INFO,
                Status.SUCCESS, runner.getProcessOutputAsString()));

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
        builder.description("Converts Ps to PDF Files and vice versa.");

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


    private MigrateResult returnWith(ServiceReport report){
        return new MigrateResult(null,report);
    }
}