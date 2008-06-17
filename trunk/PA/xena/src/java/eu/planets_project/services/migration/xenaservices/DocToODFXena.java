/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package eu.planets_project.services.migration.xenaservices;

import java.net.URI;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.Properties;

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
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.star.beans.PropertyValue;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;

import eu.planets_project.ifr.core.common.logging.PlanetsLogger;
import eu.planets_project.ifr.core.common.services.PlanetsServices;
import eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary;

/**
 * Convert office documents to the Xena office (i.e. OpenOffice.org flat) file
 * format.
 *
 */
@Stateless
@Local(BasicMigrateOneBinary.class)
@LocalBinding(jndiBinding = "planets-project.eu/XenaService/BasicMigrateOneBinary/DocToODF")
@Remote(BasicMigrateOneBinary.class)
@RemoteBinding(jndiBinding = "planets-project.eu/XenaService/BasicMigrateOneBinaryDocToODF")

// Web Service Annotations, copied from the inherited interface.
@WebService(
        name = "DocToODFXena", 
        serviceName= BasicMigrateOneBinary.NAME, 
        targetNamespace = PlanetsServices.NS )
@SOAPBinding(
        parameterStyle = SOAPBinding.ParameterStyle.BARE,
        style = SOAPBinding.Style.RPC)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public class DocToODFXena implements BasicMigrateOneBinary {
    
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
            byte[] binary ) {

        XenaMigrations xena = new XenaMigrations();
        xena.setOoffice_import_filter(XenaMigrations.IMPORT_FILTER_DOC);
        xena.setOoffice_export_filter(XenaMigrations.EXPORT_FILTER_NONE);
        
        return xena.basicMigrateOneBinary(binary);
    }

}
