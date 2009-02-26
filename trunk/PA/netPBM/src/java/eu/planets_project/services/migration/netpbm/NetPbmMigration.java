package eu.planets_project.services.migration.netpbm;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.*;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The Pdf2HtmlMigration migrates between its own formats and a number of formats
 *
 * @author Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@WebService(
        name = NetPbmMigration.NAME,
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class NetPbmMigration implements Migrate, Serializable {

    PlanetsLogger log = PlanetsLogger.getLogger(NetPbmMigration.class);

    /**
     * The service name.
     */
    static final String NAME = "NetPBMMigration";

    /**
     * {@inheritDoc}
     *
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameters)
     */
    public MigrateResult migrate(final DigitalObject digitalObject,
                                 URI inputFormat, URI outputFormat, Parameters parameters) {

        java.util.Properties props = new java.util.Properties();
        try {

            String strRsc = "/eu/planets_project/services/migration/netpbm/netpbm.properties";
            props.load( this.getClass().getResourceAsStream(strRsc));
            // config vars


        } catch( Exception e ) {
            // // config vars

        }

        ServiceReport report = new ServiceReport();
        //log.info("Using ps2pdf application name: "+this.ps2pdf_app_name);


        /*
         * We just return a new digital object with the same required arguments
         * as the given:
         */

        NetPbmFormat in = null;
        try {
            in = NetPbmFormat.loopup(inputFormat);
        } catch (NoSuchFormatException e) {
            report.setError("The input format is not on the known list");
            return fail(report);
        }
        NetPbmFormat out = null;
        try {
            out = NetPbmFormat.loopup(outputFormat);
        } catch (NoSuchFormatException e) {
            report.setError("The output format is not on the known list");
            return fail(report);
        }
        String[] migrationPath = null;

        try {
            migrationPath = NetPbmMigrationPath.lookup(in,out).getTool();
        } catch (NoSuchMigrationPathException e) {
            report.setError("No conversionpath for these formats");
            return fail(report);

        }


        InputStream psfile = digitalObject.getContent().read();



        ProcessRunner runner = new ProcessRunner();
        List<String> command = new ArrayList<String>();
        for (String element : migrationPath){
            command.add(element);
        }


        runner.setCommand(command);
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

        DigitalObject pdfFile = new DigitalObject.Builder(Content.byValue(outbytes)).build();
        return new MigrateResult(pdfFile,report);

    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     * @return ServiceDescription
     */
    public ServiceDescription describe() {

        ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());

        builder.author("Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Converts between a number of image formats");

        NetPbmMigrationPath[] mypaths = NetPbmMigrationPath.values();
        MigrationPath[] paths = new MigrationPath[mypaths.length];
        int i=0;
        for (NetPbmMigrationPath mypath: mypaths){
            paths[i] = new MigrationPath(mypath.getIn().getFormat(),mypath.getOut().getFormat(),null);
        }

        builder.paths(paths);
        builder.version("0.1");

        return builder.build();

    }


    private MigrateResult fail(ServiceReport report){
        return new MigrateResult(null,report);
    }
}