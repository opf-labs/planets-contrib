/**
 * 
 */
package eu.planets_project.services.migration.xenaservices;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.services.utils.PlanetsLogger;

/**
 * Convert ODF Writer documents to PDF, using Open Office.
 *
 */
@Stateless
@Local(BasicMigrateOneBinary.class)
@LocalBinding(jndiBinding = "planets-project.eu/XenaService/BasicMigrateOneBinary/ODFtoPDF")
@Remote(BasicMigrateOneBinary.class)
@RemoteBinding(jndiBinding = "planets-project.eu/XenaService/BasicMigrateOneBinary/ODFtoPDF")

// Web Service Annotations, copied from the inherited interface.
@WebService( name = "ODFToPDFXena", 
        serviceName = BasicMigrateOneBinary.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.BasicMigrateOneBinary")
public class ODFToPDFXena implements BasicMigrateOneBinary {
    
    PlanetsLogger log = PlanetsLogger.getLogger(DocToODFXena.class);

    /**
     * @see eu.planets_project.services.migrate.BasicMigrateOneBinary#basicMigrateOneBinary(byte[])
     */
    public byte[] basicMigrateOneBinary ( 
            byte[] binary ) {

        XenaMigrations xena = new XenaMigrations();
        xena.setOoffice_import_filter(XenaMigrations.IMPORT_FILTER_NONE);
        xena.setOoffice_export_filter(XenaMigrations.EXPORT_FILTER_PDF);
        
        return xena.basicMigrateOneBinary(binary);
    }

}
