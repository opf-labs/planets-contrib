package eu.planets_project.services.migration.imagemagick;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;


/**
 * @author Peter Melms
 *
 */
@Stateless()
@Local(Migrate.class)
@Remote(Migrate.class)

@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@WebService(
        name = Im4JavaImageMagickMigrate.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class Im4JavaImageMagickMigrate implements Migrate, Serializable {

	private static final long serialVersionUID = 5719023841822556481L;

	private static Log plogger = LogFactory.getLog(Im4JavaImageMagickMigrate.class);
    
    /**
     * the service name
     */
    public static final String NAME = "Im4JavaImageMagickMigrate";


    /**
     * default no arg constructor
     */
    public Im4JavaImageMagickMigrate() {
        plogger.info("Hello! Initializing Im4JavaImageMagickMigrate service...");
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
    	return CoreImageMagick.describeIm4JavaMigrate(NAME, this.getClass().getCanonicalName());
    }
    

    /**
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, List<Parameter> parameters) {
    	CoreImageMagick coreIM = new CoreImageMagick();
        return coreIM.doIm4JavaMigration(digitalObject, inputFormat, outputFormat, parameters);
    }
}
