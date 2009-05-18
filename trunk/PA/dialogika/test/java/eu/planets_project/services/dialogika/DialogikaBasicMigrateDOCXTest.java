/**
 * 
 */
package eu.planets_project.services.dialogika;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * A simple service to wrap the Dialogika services, to fill the gap until the Planets Migrate interface stabilises.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class DialogikaBasicMigrateDOCXTest {
    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-dialogika/DialogikaBasicMigrateDOCX?wsdl";

    /* */
    Migrate bmob = null;
    
    /**
     * 
     */
    @Before
    public void setUp() {
        System.out.println("Attempting to create a test service... ");
        bmob = ServiceCreator.createTestService(Migrate.QNAME, DialogikaBasicMigrateDOCX.class, wsdlLoc );
        System.out.println("Connected to service, got: "+bmob);

    }
    
    /**
     * 
     */
    @Test
    public void testInvoke() {
        /*
        #http-proxy-host = bspcache.bl.uk
        #http-proxy-port = 8080
        #http-proxy-host = loncache.bl.uk
        #http-proxy-host = anjackson.net
        #http-proxy-port = 38080
        */
/*        
        System.setProperty("http.proxyHost","bspcache.bl.uk");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("file.encoding","utf-8");
        System.out.println("The HTTP Proxy host is: "+System.getProperty("http.proxyHost"));
*/
        System.out.println("Reading in the input...");
        byte[] input = FileUtils.readFileIntoByteArray(new File("PA/dialogika/test/resources/test.doc"));
        System.out.println("Invoking the service...");
        DigitalObject inputObject = new DigitalObject.Builder(Content.byValue(input)).build();
        MigrateResult output = bmob.migrate(inputObject,null,null,null);
        System.out.println("Checking the result...");
        assertTrue("The byte[] output should not be NULL.", output != null );
    }

}
