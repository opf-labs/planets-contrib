package eu.planets_project.services.migration.netpbm;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.MTOM;

import eu.planets_project.ifr.core.common.conf.Configuration;
import eu.planets_project.ifr.core.common.conf.ServiceConfig;
import eu.planets_project.ifr.core.services.migration.genericwrapper2.GenericMigrationWrapper;
import eu.planets_project.ifr.core.services.migration.genericwrapper2.exceptions.MigrationInitialisationException;
import eu.planets_project.ifr.core.services.migration.genericwrapper2.utils.DocumentLocator;
import org.xml.sax.SAXException;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.ServiceUtils;

/**
 * The class migrates between a number of formats
 * @author Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>
 */
@Stateless
@MTOM
@StreamingAttachment( parseEagerly=true, memoryThreshold=ServiceUtils.JAXWS_SIZE_THRESHOLD )
@WebService(
        name = NetPbmMigration.NAME,
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class NetPbmMigration implements Migrate, Serializable {

    private static Logger log = Logger.getLogger(NetPbmMigration.class.getName());

    /**
     * The file name of the static configuration for the generic wrapping
     * framework.
     **/
    private static final String SERVICE_CONFIG_FILE_NAME = "netpbmwrapper.xml";

    /** The file name of the dynamic run-time configuration **/
    private static final String RUN_TIME_CONFIGURATION_FILE_NAME = "pserv-pa-netpbm";


    /**
     * The service name.
     */
    static final String NAME = "NetPbmMigration";
    static final String configfile = "netpbmwrapper.xml";




    public MigrateResult migrate(final DigitalObject digitalObject,
                                 URI inputFormat, URI outputFormat, List<Parameter> parameters) {


        try {
            final DocumentLocator documentLocator
                    = new DocumentLocator(
                    SERVICE_CONFIG_FILE_NAME);

            final Configuration runtimeConfiguration = ServiceConfig
                    .getConfiguration(RUN_TIME_CONFIGURATION_FILE_NAME);

            GenericMigrationWrapper genericWrapper
                    = new GenericMigrationWrapper(
                    documentLocator.getDocument(), runtimeConfiguration, this
                            .getClass().getCanonicalName());

            return genericWrapper.migrate(digitalObject, inputFormat,
                    outputFormat, parameters);

/*            MigrateResult migrationResult;

            migrationResult = genericWrapper.migrate(digitalObject,
                    inputFormat, outputFormat, parameters);*/
        } catch (Exception e) {
            log.log(Level.SEVERE,"Migration failed for object with title '"
                    + digitalObject.getTitle()
                    + "' from input format URI: " + inputFormat
                    + " to output format URI: " + outputFormat,e);
            return new MigrateResult(
                    null,
                    new ServiceReport(Type.ERROR,
                            Status.TOOL_ERROR,
                            "Failed to migrate, "+e.getMessage()));

        }


    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     * @return ServiceDescription
     */
    public ServiceDescription describe() {

        final DocumentLocator documentLocator = new DocumentLocator(
                SERVICE_CONFIG_FILE_NAME);
        try {
            final Configuration runtimeConfiguration = ServiceConfig
                    .getConfiguration(RUN_TIME_CONFIGURATION_FILE_NAME);

            GenericMigrationWrapper genericWrapper = new GenericMigrationWrapper(
                    documentLocator.getDocument(), runtimeConfiguration, this
                            .getClass().getCanonicalName());

            return genericWrapper.describe();
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    "Failed getting service description for service: "
                            + this.getClass().getCanonicalName(), e);

            // FIXME! Report failure in a proper way. Should we return a service
            // description anyway? If so, then how?
            ServiceDescription.Builder serviceDescriptionBuilder = new ServiceDescription.Builder(
                    NAME, Migrate.class.getCanonicalName());
            return serviceDescriptionBuilder.build();
        }

    }
}