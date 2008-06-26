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

import eu.planets_project.ifr.core.common.api.PlanetsException;
import eu.planets_project.ifr.core.common.logging.PlanetsLogger;
import eu.planets_project.ifr.core.common.services.PlanetsServices;
import eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary;

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
@WebService(
        name = "ODFToPDFXena", 
        serviceName = BasicMigrateOneBinary.NAME, 
        targetNamespace = PlanetsServices.NS )
@SOAPBinding(
        parameterStyle = SOAPBinding.ParameterStyle.BARE,
        style = SOAPBinding.Style.RPC)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public class ODFToPDFXena implements BasicMigrateOneBinary {
    
    PlanetsLogger log = PlanetsLogger.getLogger(DocToODFXena.class);

    /* (non-Javadoc)
     * @see eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary#basicMigrateOneBinary(byte[])
     */
    @WebMethod(
            operationName = BasicMigrateOneBinary.NAME, 
            action = PlanetsServices.NS + "/" + BasicMigrateOneBinary.NAME)
    @WebResult(
            name = BasicMigrateOneBinary.NAME+"Result", 
            targetNamespace = PlanetsServices.NS + "/" + BasicMigrateOneBinary.NAME, 
            partName = BasicMigrateOneBinary.NAME + "Result")
    public byte[] basicMigrateOneBinary ( 
            @WebParam(
                    name = "binary", 
                    targetNamespace = PlanetsServices.NS + "/" + BasicMigrateOneBinary.NAME, 
                    partName = "binary")
            byte[] binary )  throws PlanetsException {

        XenaMigrations xena = new XenaMigrations();
        xena.setOoffice_import_filter(XenaMigrations.IMPORT_FILTER_NONE);
        xena.setOoffice_export_filter(XenaMigrations.EXPORT_FILTER_PDF);
        
        return xena.basicMigrateOneBinary(binary);
    }

}
