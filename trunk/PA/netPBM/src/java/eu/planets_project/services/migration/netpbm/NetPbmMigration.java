package eu.planets_project.services.migration.netpbm;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import org.xml.sax.SAXException;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.ifr.core.services.migration.genericwrapper.utils.DocumentLocator;
import eu.planets_project.ifr.core.services.migration.genericwrapper.GenericMigrationWrapper;
import eu.planets_project.ifr.core.services.migration.genericwrapper.exceptions.MigrationInitialisationException;

/**
 * The class migrates between a number of formats
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
    static final String NAME = "NetPbmMigration";
    static final String configfile = "netpbmwrapper.xml";
    GenericMigrationWrapper genericWrapper;



    public MigrateResult migrate(final DigitalObject digitalObject,
                                 URI inputFormat, URI outputFormat, List<Parameter> parameters) {



        MigrateResult migrationResult;
        try {
            migrationResult = genericWrapper.migrate(digitalObject,
                                                     inputFormat, outputFormat, parameters);
        } catch (Exception e) {
            log
                    .error("Migration failed for object with title '"
                           + digitalObject.getTitle()
                           + "' from input format URI: " + inputFormat
                           + " to output format URI: " + outputFormat, e);
            return new MigrateResult(
                    null,
                    new ServiceReport(Type.ERROR,
                                      Status.TOOL_ERROR,
                                      "Failed to migrate, "+e.getMessage()));
            // TODO! Report failure in a proper way.
        }

        return migrationResult;
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     * @return ServiceDescription
     */
    public ServiceDescription describe() {
        return genericWrapper.describe();
    }

    public NetPbmMigration()  {


        final DocumentLocator documentLocator =  new DocumentLocator(configfile);

        try {
            genericWrapper = new GenericMigrationWrapper(
                    documentLocator.getDocument(),this.getClass().getCanonicalName());
        } catch (MigrationInitialisationException e) {
            log.error("Failed to parse the config file",e);
        } catch (IOException e) {
            log.error("Could not read the config file",e);
        } catch (SAXException e) {
            log.error("Could not parse the config file as valid xml",e);
        }
    }
}