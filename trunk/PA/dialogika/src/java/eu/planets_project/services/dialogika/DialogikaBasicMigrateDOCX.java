/**
 * 
 */
package eu.planets_project.services.dialogika;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dialogika.planets.planets_webservice.genericmigration.ArrayOfParameter;
import de.dialogika.planets.planets_webservice.genericmigration.GenericMigration;
import de.dialogika.planets.planets_webservice.genericmigration.GenericMigrationSoap;
import de.dialogika.planets.planets_webservice.genericmigration.MigrateOneBinaryResult;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;

/**
 * A simple service to wrap the Dialogika services.
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 */
@WebService(name = DialogikaBasicMigrateDOCX.NAME, serviceName = Migrate.NAME, targetNamespace = PlanetsServices.NS, endpointInterface = "eu.planets_project.services.migrate.BasicMigrateOneBinary")
public class DialogikaBasicMigrateDOCX implements Migrate {

    public static final String NAME = "DialogikaBasicMigrateDOCX";

    private static Log log = LogFactory.getLog(DialogikaBasicMigrateDOCX.class);

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject,
     *      java.net.URI, java.net.URI, java.util.List)
     */
    public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, List<Parameter> parameters) {
        byte[] binary = FileUtils.writeInputStreamToBinary(digitalObject
                .getContent().read());
        byte[] result = basicMigrateOneBinary(binary);
        DigitalObject resultObject = new DigitalObject.Builder(Content
                .byValue(result)).build();
        return new MigrateResult(resultObject, new ServiceReport(Type.INFO,
                Status.SUCCESS, "OK"));
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.PlanetsService#describe()
     */
    public ServiceDescription describe() {
        return new ServiceDescription.Builder("DialogikaBasicMigrateDOCX",
                Migrate.class.getName()).build();
    }

    private byte[] basicMigrateOneBinary(byte[] binary) {
        GenericMigration mob;
        log.info("Initialising GenericMigration...");
        try {
            mob = new GenericMigration(
                    new URL(
                            "http://www.dialogika.de/planets/planets.webservice/GenericMigration.asmx?outtype=docx&WSDL"),
                    new QName(
                            "http://www.dialogika.de/Planets/planets.webservice/GenericMigration",
                            "GenericMigration"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        log.info("Initialised GenericMigration.");
        ArrayOfParameter pars = new ArrayOfParameter();
        GenericMigrationSoap gms = mob.getGenericMigrationSoap();
        log.info("Got SOAP implementation.  Invoking...");
        MigrateOneBinaryResult res = gms.migrateOneBinary(binary, pars);
        log.info("Got result. Returning.");
        return res.getBinary();
    }

}
