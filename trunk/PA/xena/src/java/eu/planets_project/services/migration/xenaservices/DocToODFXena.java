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
@LocalBinding(jndiBinding = "planets-project.eu/XenaService/BasicMigrateOneBinary")
@Remote(BasicMigrateOneBinary.class)
@RemoteBinding(jndiBinding = "planets-project.eu/XenaService/BasicMigrateOneBinary")

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

        File input = new File( "C:/tmp.doc" );
        try {
            FileOutputStream fos = new FileOutputStream(input);
            fos.write(binary);
            fos.flush();
            fos.close();
        } catch( FileNotFoundException e ) {
            log.error("Creating "+input.getAbsolutePath()+" :: " +e);
            return null;
        } catch( IOException e ) {
            log.error("Creating "+input.getAbsolutePath()+" :: " +e);
            return null;
        }
        
        File output = new File( "C:/tmp.odt" );

        try {
            this.transform(input.toURI(), output.toURI());
        } catch( SAXException e ) {
            log.error("Transforming "+input.getAbsolutePath()+" :: " +e);
            return null;
        } catch( IOException e ) {
            log.error("Transforming "+input.getAbsolutePath()+" :: " +e);
            return null;
        }
        
        
        byte[] result = null;
        try {
            result = this.getByteArrayFromFile(output);
        } catch( IOException e ) {
            log.error("Returning "+input.getAbsolutePath()+" :: " +e);
            return null;
        } 
        
        return result;
    }
    
    // FIXME Refactor this into common.
    private static byte[] getByteArrayFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // throw new
            // IllegalArgumentException("getBytesFromFile@JpgToTiffConverter::
            // The file is too large (i.e. larger than 2 GB!");
            System.out.println("Datei ist zu gross (e.g. groesser als 2GB)!");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
    
    /*
     * Code from the Xena project.
     */

    public static String OPENOFFICE_INSTALL_DIR = "C:/Program Files/OpenOffice.org 2.3";
    // Values like PDF might work, writer_pdf_Export should work.
    //  - http://www.oooforum.org/forum/viewtopic.phtml?p=167815
    //  - List of filters: http://www.oooforum.org/forum/viewtopic.phtml?t=3549
    public final static String ODF_WRITER_EXPORT_FILTER = "";
    public final static String OPEN_DOCUMENT_PREFIX = "opendocument";
	private final static String OPEN_DOCUMENT_URI = "http://preservation.naa.gov.au/odf/1.0";
	public final static String DOCUMENT_TYPE_TAG_NAME = "type";
	public final static String DOCUMENT_EXTENSION_TAG_NAME = "extension";
	public final static String PROCESS_DESCRIPTION_TAG_NAME = "description";
	private final static String OS_X_ARCHITECTURE_NAME = "mac os x";

	private static Logger logger = Logger.getLogger(DocToODFXena.class.getName());

	private final static String DESCRIPTION =
	    "The following data is a MIME-compliant (RFC 1421) PEM base64 (RFC 1421) representation of an Open Document Format "
	            + "(ISO 26300, Version 1.0) document, produced by Open Office version 2.0.";

	public DocToODFXena() {
		// Nothing to do
	}

	private XComponent loadDocument(InputStream is, String extension, boolean visible ) throws Exception {
		File input = File.createTempFile("input", "." + extension);
		try {
			input.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(input);
			byte[] buf = new byte[4096];
			int n;
			while (0 < (n = is.read(buf))) {
				fos.write(buf, 0, n);
			}
			fos.close();
			XComponent rtn = loadDocument(input, visible );
			return rtn;
		} finally {
			input.delete();
		}
	}

	static XComponent loadDocument(File input, boolean visible) throws Exception {
		/*
		 * Bootstraps a servicemanager with the jurt base components registered
		 */
		XMultiServiceFactory xmultiservicefactory = com.sun.star.comp.helper.Bootstrap.createSimpleServiceManager();

		/*
		 * Creates an instance of the component UnoUrlResolver which supports the services specified by the factory.
		 */
		Object objectUrlResolver = xmultiservicefactory.createInstance("com.sun.star.bridge.UnoUrlResolver");

		// Create a new url resolver
		XUnoUrlResolver xurlresolver = (XUnoUrlResolver) UnoRuntime.queryInterface(XUnoUrlResolver.class, objectUrlResolver);

		// Resolves an object that is specified as follow:
		// uno:<connection description>;<protocol description>;<initial object name>
		Object objectInitial = null;
		String address = "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";
		try {
			objectInitial = xurlresolver.resolve(address);
		} catch (com.sun.star.connection.NoConnectException ncex) {
			// Could not connect to OpenOffice, so start it up and try again
			try {
				startOpenOffice();
				objectInitial = xurlresolver.resolve(address);
			} catch (Exception ex) {
				// If it fails again for any reason, just bail
				throw new Exception("Could not start OpenOffice", ex);
			}
		} catch (com.sun.star.uno.RuntimeException rtex) {
			// Could not connect to OpenOffice, so start it up and try again
			try {
				startOpenOffice();
				objectInitial = xurlresolver.resolve(address);
			} catch (Exception ex) {
				// If it fails again for any reason, just bail
				throw new Exception("Could not start OpenOffice", ex);
			}
		}

		// Create a service manager from the initial object
		xmultiservicefactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, objectInitial);

		/*
		 * A desktop environment contains tasks with one or more frames in which components can be loaded. Desktop is
		 * the environment for components which can instanciate within frames.
		 */
		XComponentLoader xcomponentloader =
		    (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, xmultiservicefactory.createInstance("com.sun.star.frame.Desktop"));

		PropertyValue[] loadProperties = null;
		if (visible) {
			loadProperties = new PropertyValue[0];
		} else {
			loadProperties = new PropertyValue[1];
			loadProperties[0] = new PropertyValue();
			loadProperties[0].Name = "Hidden";
			loadProperties[0].Value = new Boolean(true);
		}
		return xcomponentloader.loadComponentFromURL("file:///" + input.getAbsolutePath().replace('\\', '/'), "_blank", 0, loadProperties);
	}

	private static void startOpenOffice() throws Exception, InterruptedException {
//		PropertiesManager propManager = pluginManager.getPropertiesManager();
//		String fname = propManager.getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME, OfficeProperties.OOO_DIR_PROP_NAME);
		String fname = OPENOFFICE_INSTALL_DIR;
		
		if (fname == null || fname.equals("")) {
			throw new Exception("OpenOffice.org location not configured.");
		}

		// NeoOffice/OpenOffice on OS X has a different program structure than that for Windows and Linux, so we
		// need a special case...
		File sofficeProgram;
		if (System.getProperty("os.name").toLowerCase().equals(OS_X_ARCHITECTURE_NAME)) {
			sofficeProgram = new File(new File(fname, "Contents/MacOS"), "soffice.bin");
		} else {
			sofficeProgram = new File(new File(fname, "program"), "soffice");
		}
		List<String> commandList = new ArrayList<String>();
		commandList.add(sofficeProgram.getAbsolutePath());
		commandList.add("-nologo");
		commandList.add("-nodefault");
		commandList.add("-accept=socket,port=8100;urp;");
		String[] commandArr = commandList.toArray(new String[0]);
		try {
			logger.finest("Starting OpenOffice process");
			Runtime.getRuntime().exec(commandArr);
		} catch (IOException x) {
			throw new Exception("Cannot start OpenOffice.org. Try Checking Office Properties. " + sofficeProgram.getAbsolutePath(), x);
		}

		try {
			int sleepSeconds = 50;
//			    Integer.parseInt(propManager.getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME, OfficeProperties.OOO_SLEEP_PROP_NAME));
			Thread.sleep(1000 * sleepSeconds);
		} catch (NumberFormatException nfex) {
			throw new Exception("Cannot start OpenOffice.org due to invalid startup sleep time. " + "Try Checking Office Properties. ", nfex);
		}
	}

	public void transform(URI input, URI output) throws SAXException, IOException {
		
		FileInputStream inputStream = new FileInputStream( new File( input ));

		try {
			boolean visible = false;

			// Open our office document...
			XComponent objectDocumentToStore =
			    loadDocument(inputStream, "doc", visible );

			// Getting an object that will offer a simple way to store a document to a URL.
			XStorable xstorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, objectDocumentToStore);

			if (xstorable == null) {
				throw new SAXException("Cannot connect to OpenOffice.org - possibly something wrong with the input file");
			}

			// Preparing properties for converting the document
			PropertyValue propertyvalue[] = new PropertyValue[2];
			// Setting the flag for overwriting
			propertyvalue[0] = new PropertyValue();
			propertyvalue[0].Name = "Overwrite";
			propertyvalue[0].Value = new Boolean(true);

			// Setting the filter name
			/*
			propertyvalue[1] = new PropertyValue();
			propertyvalue[1].Name = "FilterName";

			propertyvalue[1].Value = ODF_WRITER_EXPORT_FILTER;
*/
			
			// Storing and converting the document
			try {
				xstorable.storeToURL(output.toURL().toString(), propertyvalue);
			} catch (Exception e) {
				throw new Exception(
				                        "Cannot convert to open document format. Maybe your OpenOffice.org installation does not have installed: "
				                                + ODF_WRITER_EXPORT_FILTER
				                                + " or maybe the document is password protected or has some other problem. Try opening in OpenOffice.org manually.",
				                        e);
			}
			// Getting the method dispose() for closing the document
			XComponent xcomponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xstorable);
			// Closing the converted document
			xcomponent.dispose();
/*			if (output.length() == 0) {
				throw new Exception("OpenOffice open document file is empty. Do you have OpenOffice Java integration installed?");
			}*/
		} catch (Exception e) {
			logger.log(Level.FINEST, "Problem normalisting office document", e);
			throw new SAXException(e);
		}
		// Check file was created successfully by opening up the zip and checking for at least one entry
		// Base64 encode the file and write out to content handler
		try {
/*
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			String tagURI = OPEN_DOCUMENT_URI;
			String tagPrefix = OPEN_DOCUMENT_PREFIX;
			ZipFile openDocumentZip = new ZipFile(output);
			// Not sure if this is even possible, but worth checking I guess...
			if (openDocumentZip.size() == 0) {
				throw new IOException("An empty document was created by OpenOffice");
			}
			att.addAttribute(OPEN_DOCUMENT_URI, PROCESS_DESCRIPTION_TAG_NAME, PROCESS_DESCRIPTION_TAG_NAME, "CDATA", DESCRIPTION);
			att.addAttribute(OPEN_DOCUMENT_URI, DOCUMENT_TYPE_TAG_NAME, DOCUMENT_TYPE_TAG_NAME, "CDATA", type.getName());
			att.addAttribute(OPEN_DOCUMENT_URI, DOCUMENT_EXTENSION_TAG_NAME, DOCUMENT_EXTENSION_TAG_NAME, "CDATA", officeType.fileExtension());

			InputStream is = new FileInputStream(output);
			ch.startElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix, att);
			InputStreamEncoder.base64Encode(is, ch);
			ch.endElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix);
		} catch (ZipException ex) {
			throw new IOException("OpenOffice could not create the open document file");
            */
		} finally {
//			output.delete();
		}
	}
}
