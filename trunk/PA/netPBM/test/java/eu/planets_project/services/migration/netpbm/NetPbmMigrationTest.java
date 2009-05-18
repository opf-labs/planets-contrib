package eu.planets_project.services.migration.netpbm;

import eu.planets_project.services.datatypes.DigitalObjectContent;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.ps2pdf.Ps2PdfMigration;
import eu.planets_project.services.utils.ByteString;
import eu.planets_project.services.utils.Checksums;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * TODO abr forgot to document this class
 */
public class NetPbmMigrationTest extends TestCase {

    /**
     * The location of this service when deployed.
     */
    private String wsdlLoc = "/pserv-pa-pdf2html/Pdf2HtmlMigration?wsdl";

    /**
     * A holder for the object to be tested.
     */
    private Migrate dom = null;

    /**
     * A test file object.
     */

    private File testjpeg = new File("PA/netPBM/test/resources/JPEG_example_JPG_RIP_050.jpg");

    /*
    * (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
    @Override
    protected void setUp() throws Exception {
        dom = ServiceCreator.createTestService(Migrate.QNAME,
                Ps2PdfMigration.class, wsdlLoc);
    }

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    /**
     * Test the Description method.
     */
    @Test
    public void testDescribe() {
        ServiceDescription desc = dom.describe();
        System.out.println("Recieved service description: " + desc.toXmlFormatted());
        assertTrue("The ServiceDescription should not be NULL.", desc != null);
    }

    /**
     * Test the pass-thru migration.
     */
    @Test
    public void testMigrate() throws IOException {
        System.out.println(testjpeg.getCanonicalPath());

        try {
/*
        * To test usability of the digital object instance in web services,
* we simply pass one into the service and expect one back:
*/

            DigitalObject input =
                    new DigitalObject.Builder(Content.byValue(testjpeg))
                            .format(new URI("planets:fmt/ext/jpeg"))
                            .title("test.jpeg").
                            build();
            System.out.println("Input: " + input);

            MigrateResult mr = dom.migrate(input, new URI("planets:fmt/ext/jpeg"), new URI("planets:fmt/ext/pnm"), null);
            DigitalObject doOut = mr.getDigitalObject();

            assertTrue("Resulting digital object is null.", doOut != null);

            System.out.println("Output: " + doOut);

            DigitalObjectContent content = doOut.getContent();

            File workfolder = FileUtils.createWorkFolderInSysTemp("netpbm_test");

            File resultHtml = FileUtils.writeInputStreamToFile(content.read(), workfolder, "netpbm_result.html");

            System.out.println("Please find the result HTML file here: \n" + resultHtml.getAbsolutePath());

            assertTrue("Resulting digital object not equal to the original.",
                    !input.equals(doOut));

            InputStream convertedFile = doOut.getContent().read();
            String md5 = ByteString.toHex(Checksums.md5(convertedFile)).toLowerCase();
            assertEquals("The file was not converted correctly",md5,"da37d73c2be96c21e6d7628d71204e23");


        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

}


