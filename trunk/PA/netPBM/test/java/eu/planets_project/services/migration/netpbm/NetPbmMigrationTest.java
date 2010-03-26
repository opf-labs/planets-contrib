package eu.planets_project.services.migration.netpbm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import eu.planets_project.services.utils.ByteString;
import eu.planets_project.services.utils.Checksums;
import junit.framework.TestCase;

import org.junit.Test;

import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.DigitalObjectContent;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * TODO abr forgot to document this class
 */
public class NetPbmMigrationTest extends TestCase {

    /**
     * The location of this service when deployed.
     */
    private String wsdlLoc = "/pserv-pa-netPbm/NetPbmMigration?wsdl";

    /**
     * A holder for the object to be tested.
     */
    private Migrate dom = null;

    /**
     * A test file object.
     */

//    private File testjpeg = new File("PA/netPBM/test/resources/JPEG_example_JPG_RIP_050.jpg");
    private File testpng = new File("tests/test-files/images/bitmap/test_png/2274192346_4a0a03c5d6.png");
    private int testPngToGifLength = 200055;
    private long testPngToJpegLength = 57596;//TODO
    private File testjpeg = new File("tests/test-files/images/bitmap/test_jpg/2277759451_2e4cd93544.jpg");
    private long testJpegToGifLength = 144914;
    private File testgif  = new File("tests/test-files/images/bitmap/test_gif/2303419094_1cc67a1f70.gif");
    private long testGifToPngLength = 102802;

    /*
    * (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
    @Override
    protected void setUp() throws Exception {
        dom = ServiceCreator.createTestService(Migrate.QNAME,
                NetPbmMigration.class, wsdlLoc);


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
        assertTrue("The ServiceDescription should not be NULL.", desc != null);
        //  System.out.println("Recieved service description: " + desc.toXmlFormatted());
    }

    @Test
    public void testMigrateGifToPng() throws IOException, URISyntaxException {
//        System.out.println(testjpeg.getCanonicalPath());

/*
        * To test usability of the digital object instance in web services,
* we simply pass one into the service and expect one back:
*/


        DigitalObject input =
                new DigitalObject.Builder(Content.byValue(testgif))
                        .title("test.png").
                        build();
        System.out.println("Input: " + input);

        MigrateResult mr = dom.migrate(input, new URI("info:pronom/fmt/4"), new URI("http://www.w3.org/TR/PNG/"), null);
        DigitalObject doOut = mr.getDigitalObject();

        if (doOut == null){
            System.out.println(mr.getReport().toString());
        }
        assertTrue("Resulting digital object is null.", doOut != null);

        System.out.println("Output: " + doOut);


        File result = DigitalObjectUtils.toFile(doOut);

        System.out.println("Please find the result HTML file here: \n" + result.getAbsolutePath());

        assertTrue("Resulting digital object not equal to the original.",
                !input.equals(doOut));
        assertEquals("Length of converted file wrong",testGifToPngLength,result.length());

        /*
                InputStream convertedFile = doOut.getContent().getInputStream();
                String md5 = ByteString.toHex(Checksums.md5(convertedFile)).toLowerCase();
                assertEquals("The file was not converted correctly",md5,"da37d73c2be96c21e6d7628d71204e23");
        */

    }


    @Test
    public void testMigratePngToGif89() throws IOException, URISyntaxException {
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

        MigrateResult mr = dom.migrate(input, new URI("info:pronom/fmt/12"), new URI("info:pronom/fmt/4"), null);
        DigitalObject doOut = mr.getDigitalObject();

        if (doOut == null){
            System.out.println(mr.getReport().toString());
        }
        assertTrue("Resulting digital object is null.", doOut != null);


        System.out.println("Output: " + doOut);


        File result = DigitalObjectUtils.toFile(doOut);

        System.out.println("Please find the result HTML file here: \n" + result.getAbsolutePath());

        assertTrue("Resulting digital object not equal to the original.",
                !input.equals(doOut));
        assertEquals("Length of converted file wrong",testPngToGifLength,result.length());

        /*
        InputStream convertedFile = doOut.getContent().getInputStream();
        String md5 = ByteString.toHex(Checksums.md5(convertedFile)).toLowerCase();
        assertEquals("The file was not converted correctly",md5,"da37d73c2be96c21e6d7628d71204e23");
          */

    }



    @Test
    public void testMigrateJpegToGif89() throws IOException, URISyntaxException {
//        System.out.println(testjpeg.getCanonicalPath());

/*
        * To test usability of the digital object instance in web services,
* we simply pass one into the service and expect one back:
*/

        DigitalObject input =
                new DigitalObject.Builder(Content.byValue(testjpeg))
                        .title("test.png").
                        build();
        System.out.println("Input: " + input);

        MigrateResult mr = dom.migrate(input, new URI("info:pronom/fmt/44"), new URI("info:pronom/fmt/4"), null);
        DigitalObject doOut = mr.getDigitalObject();

        if (doOut == null){
            System.out.println(mr.getReport().toString());
        }
        assertTrue("Resulting digital object is null.", doOut != null);


        System.out.println("Output: " + doOut);


        File result = DigitalObjectUtils.toFile(doOut);

        System.out.println("Please find the result HTML file here: \n" + result.getAbsolutePath());

        assertTrue("Resulting digital object not equal to the original.",
                !input.equals(doOut));
        assertEquals("Length of converted file wrong",testJpegToGifLength,result.length());

        /*
        InputStream convertedFile = doOut.getContent().getInputStream();
        String md5 = ByteString.toHex(Checksums.md5(convertedFile)).toLowerCase();
        assertEquals("The file was not converted correctly",md5,"da37d73c2be96c21e6d7628d71204e23");
          */

    }



    @Test
    public void testMigratePngToJpeg() throws IOException, URISyntaxException {
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

        MigrateResult mr = dom.migrate(input, new URI("info:pronom/fmt/12"), new URI("info:pronom/fmt/44"), null);
        DigitalObject doOut = mr.getDigitalObject();

        if (doOut == null){
            System.out.println(mr.getReport().toString());
        }

        assertTrue("Resulting digital object is null.", doOut != null);

        System.out.println("Output: " + doOut);


        File result = DigitalObjectUtils.toFile(doOut);

        System.out.println("Please find the result HTML file here: \n" + result.getAbsolutePath());

        assertTrue("Resulting digital object not equal to the original.",
                !input.equals(doOut));
        assertEquals("Length of converted file wrong",testPngToJpegLength,result.length());

        /*
        InputStream convertedFile = doOut.getContent().getInputStream();
        String md5 = ByteString.toHex(Checksums.md5(convertedFile)).toLowerCase();
        assertEquals("The file was not converted correctly",md5,"da37d73c2be96c21e6d7628d71204e23");
          */

    }



}


