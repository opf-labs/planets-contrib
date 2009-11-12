package eu.planets_project.services.migration.ps2pdf;

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
import eu.planets_project.ifr.core.services.migration.genericwrapper1.GenericMigrationWrapper;
import eu.planets_project.ifr.core.services.migration.genericwrapper1.exceptions.MigrationInitialisationException;
import eu.planets_project.ifr.core.services.migration.genericwrapper1.utils.DocumentLocator;

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

    /**
	 * 
	 */
	private static final long serialVersionUID = 4329827996540798228L;

	PlanetsLogger log = PlanetsLogger.getLogger(Ps2PdfMigration.class);

    /**
     * The service name.
     */
    static final String NAME = "Ps2PdfMigration";
    static final String configfile = "ps2pdf.gwrap.path.xml";
    GenericMigrationWrapper genericWrapper;



    /**
     * {@inheritDoc}
     *
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
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

    public Ps2PdfMigration()  {


        final DocumentLocator documentLocator =  new DocumentLocator(configfile);

        try {
            genericWrapper = new GenericMigrationWrapper(
                    documentLocator.getDocument(), this.getClass().getCanonicalName());
        } catch (MigrationInitialisationException e) {
            log.error("Failed to parse the config file",e);
        } catch (IOException e) {
            log.error("Could not read the config file",e);
        } catch (SAXException e) {
            log.error("Could not parse the config file as valid xml",e);
        }
    }
}