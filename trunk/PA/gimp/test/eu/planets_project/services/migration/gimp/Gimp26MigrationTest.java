package eu.planets_project.services.migration.gimp;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.test.ServiceCreator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Local and client tests of the digital object migration functionality.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at>
 */
public final class Gimp26MigrationTest {

    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-gimp/Gimp26Migration?wsdl";

    /* A holder for the object to be tested */
    Migrate dom = null;
    private File fTmpInFile;
    private File fTmpOutFile;
    // Input and output formats are based on the same set
    List<String> formats = null;
    
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */

    @Before
    public void setUp() throws Exception {
        formats = new ArrayList<String>();
        formats.add("GIF");
        formats.add("EPS");
        formats.add("JPEG");
        formats.add("PNG");
        formats.add("PS");
        formats.add("TIFF");
        formats.add("BMP");
        dom = ServiceCreator.createTestService(Migrate.QNAME, Gimp26Migration.class, wsdlLoc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test the Description method.
     */
    /*
    @Test
    public void testDescribe() {
        System.out.println("Test description");
        ServiceDescription desc = dom.describe();
        System.out.println("Received service description: ");
        System.out.println(desc.toXml());
        assertTrue("The ServiceDescription should not be NULL.", desc != null);
    }
    */
    @Test
    public void testMigrateAll() throws IOException {
        
        String origExt = null;
        String destExt = null;
        // Tests will be executed for 3 sets of test files of the formats
        // that the GIMP service wrapper supports:
        // demonstration1.bmp, demonstration1.gif, demonstration1.eps ...,
        // demonstration2.bmp, demonstration2.gif, demonstration2.eps ...,
        // demonstration3.bmp, demonstration3.gif, demonstration3.eps ...
        for(int i = 1; i < 4; i++)
        {
            for (Iterator itr1 = formats.iterator(); itr1.hasNext();) {
                origExt = (String) itr1.next();
                for (Iterator itr2 = formats.iterator(); itr2.hasNext();)
                {
                    destExt = (String) itr2.next();
                    // do the migration only if original file extension differs
                    // from destination file extension
                    if( !origExt.equalsIgnoreCase(destExt) )
                    {
                        System.out.println("Do migration test from "+origExt+" to "+destExt);
                        doMigration(origExt,destExt, i, null);
                    }
                }
            }
        }
    }
    /*
    @Test
    public void testMigrateWithParams() throws IOException {
        String origExt = "TIFF";
        String destExt = "GIF";
        System.out.println("Do migration test from "+origExt+" to "+destExt);
        Parameters parameters = new Parameters();
        parameters.add("gif-interlace", "1");
        parameters.add("gif-numcolors", "2");
        doMigration(origExt,destExt, 4, parameters);
    }
     */
    /*
    @Test
    public void testMigrateHugeFiles() throws IOException {
        
        String origExt = "TIFF";
        String destExt = "JPEG";
        System.out.println("Do migration test from "+origExt+" to "+destExt);
        doMigration(origExt,destExt, 5, null);
        System.out.println("Do migration test from "+origExt+" to "+destExt);
        doMigration(origExt,destExt, 6, null);
    }
    */
    private void doMigration(String origExt, String destExt, int cycle, Parameters params) throws IOException
    {
        // Test file name
        String inTestFileName = "PA/gimp/test/testfiles/demonstration"+String.valueOf(cycle)+"." + origExt.toLowerCase();
        // Output file name
        String outTestFileName = "PA/gimp/test/testfiles/planetsMigrate"+origExt+"to"+destExt+String.valueOf(cycle)+"."+destExt.toLowerCase();
        byte[] binary = this.readByteArrayFromFile(inTestFileName);
        DigitalObject input = new DigitalObject.Builder(Content.byValue(binary)).build();
        MigrateResult mr = dom.migrate(input, Format.extensionToURI(origExt), Format.extensionToURI(destExt), params);
        DigitalObject doOut = mr.getDigitalObject();
        assertTrue("Resulting digital object is null for planetsMigrate"+origExt+"to"+destExt+".", doOut != null);
        writeByteArrayToFile(doOut.getContent().getValue(), outTestFileName);
    }
    private synchronized byte[] readByteArrayFromFile(String strInFile)
            throws IOException {
        byte[] binary = new byte[0];

        //String strOutFile = "PA/gimp/test/testfiles/demonstration.png";
        //String strOutFile = "test/testfiles/testin.dvi";
        fTmpOutFile = new File(strInFile);
        assertTrue("input file " + fTmpOutFile.getAbsolutePath() + " does not exist.", fTmpOutFile.exists());
        try {
            if (fTmpOutFile.isFile() && fTmpOutFile.canRead()) {
                binary = new byte[(int) fTmpOutFile.length()];
                FileInputStream fis = new FileInputStream(fTmpOutFile);
                fis.read(binary);
                System.out.println("Read file: " + fTmpOutFile.getAbsolutePath());
                fis.close();
            } else {
                fail("Unable to read file: " + fTmpOutFile.getAbsolutePath());
            }
        } catch (IOException ex) {
            fail("IO Error: " + ex.toString());
        }
        return binary;
    }

    private synchronized void writeByteArrayToFile(byte[] binary, String strOutFile)
            throws IOException {
        try {
            //String strOutFile = "PA/gimp/test/testfiles/demonstration.jpg";
            //String strOutFile = "test/testfiles/testout.ps";
            this.fTmpInFile = new File(strOutFile);
            System.out.println();
            BufferedOutputStream fos =
                    new BufferedOutputStream(
                    new FileOutputStream(fTmpInFile));
            fos.write(binary);
            fos.close();
            assertTrue("Output file has not been created correctly. ", fTmpInFile.exists() && fTmpInFile.length() > 0);
        } catch (IOException ex) {
            fail("IO Error: " + ex.toString());
        }
    }
}
