package eu.planets_project.services.migration.netpbm;

import eu.planets_project.services.datatypes.DigitalObjectContent;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

//    private File testjpeg = new File("PA/netPBM/test/resources/JPEG_example_JPG_RIP_050.jpg");
    private File testpng = new File("tests/test-files/images/bitmap/test_png/2274192346_4a0a03c5d6.png");
    private int testPngToGifLength = 200037;

                    File workfolder;
    /*
    * (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
    @Override
    protected void setUp() throws Exception {
        dom = ServiceCreator.createTestService(Migrate.QNAME,
               NetPbmMigration.class, wsdlLoc);


        workfolder = FileUtils.createWorkFolderInSysTemp("netpbm_test");
    }

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
        workfolder.delete();
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
    public void testMigratePngToGif() throws IOException, URISyntaxException {
//        System.out.println(testjpeg.getCanonicalPath());

/*
        * To test usability of the digital object instance in web services,
* we simply pass one into the service and expect one back:
*/

        DigitalObject input =
                new DigitalObject.Builder(Content.byValue(testpng))
                        .title("test.png").
                        build();
        System.out.println("Input: " + input);

        MigrateResult mr = dom.migrate(input, new URI("info:pronom/fmt/12"), new URI("info:pronom/fmt/3"), null);
        DigitalObject doOut = mr.getDigitalObject();

        assertTrue("Resulting digital object is null.", doOut != null);

        System.out.println("Output: " + doOut);

        DigitalObjectContent content = doOut.getContent();



        File result = FileUtils.writeInputStreamToFile(content.getInputStream(), workfolder, "test.png.gif");

        System.out.println("Please find the result HTML file here: \n" + result.getAbsolutePath());

        assertTrue("Resulting digital object not equal to the original.",
                !input.equals(doOut));
        assertEquals("Length of converted file wrong",result.length(),testPngToGifLength);

/*
            InputStream convertedFile = doOut.getContent().read();
            String md5 = ByteString.toHex(Checksums.md5(convertedFile)).toLowerCase();
            assertEquals("The file was not converted correctly",md5,"da37d73c2be96c21e6d7628d71204e23");
*/




    }

}


