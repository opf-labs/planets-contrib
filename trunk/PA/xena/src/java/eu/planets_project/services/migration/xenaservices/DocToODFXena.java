/**
 * 
 */
package eu.planets_project.services.migration.xenaservices;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.BindingType;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;

import eu.planets_project.services.PlanetsException;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.services.utils.PlanetsLogger;

/**
 * Convert Microsoft DOC to ODF, using Open Office.
 *
 */
@Stateless
@Local(BasicMigrateOneBinary.class)
@LocalBinding(jndiBinding = "planets-project.eu/XenaService/BasicMigrateOneBinary/DocToODF")
@Remote(BasicMigrateOneBinary.class)
@RemoteBinding(jndiBinding = "planets-project.eu/XenaService/BasicMigrateOneBinaryDocToODF")

// Web Service Annotations, copied in from the inherited interface.
@WebService(
        name = "DocToODFXena", 
        serviceName = BasicMigrateOneBinary.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.BasicMigrateOneBinary" )
public class DocToODFXena implements BasicMigrateOneBinary {
    
    PlanetsLogger log = PlanetsLogger.getLogger(DocToODFXena.class);

	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary#basicMigrateOneBinary(byte[])
     */
    public byte[] basicMigrateOneBinary ( 
            byte[] binary ) {

        XenaMigrations xena = new XenaMigrations();
        xena.setOoffice_import_filter(XenaMigrations.IMPORT_FILTER_DOC);
        xena.setOoffice_export_filter(XenaMigrations.EXPORT_FILTER_NONE);
        
        return xena.basicMigrateOneBinary(binary);
    }

}
