/**
 * 
 */
package eu.planets_project.services.dialogika;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import eu.planets_project.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * A simple service to wrap the Dialogika services, to fill the gap until the Planets Migrate interface stabilises.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
@SuppressWarnings("deprecation")
public class DialogikaBasicMigrateDOCXTest {
    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-dialogika/DialogikaBasicMigrateDOCX?wsdl";

    /* */
    BasicMigrateOneBinary bmob = null;
    
    /**
     * 
     */
    @Before
    public void setUp() {

        bmob = ServiceCreator.createTestService(BasicMigrateOneBinary.QNAME, DialogikaBasicMigrateDOCX.class, wsdlLoc );
        System.out.println("Connected to service, got: "+bmob);

    }
    
    /**
     * 
     */
    @Test
    public void testInvoke() {
        System.out.println("The HTTP Proxy host is: "+System.getProperty("http.proxyHost"));

        byte[] input = ByteArrayHelper.read(new File("PA/dialogika/test/resources/test.doc"));
        byte[] output = bmob.basicMigrateOneBinary(input);
        assertTrue("The byte[] output should not be NULL.", output != null );
    }

}
