package eu.planets_project.services.migration.jasper;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import java.net.MalformedURLException;

import org.junit.Test;

import eu.planets_project.services.migration.jasper.Jasper19Migration;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * Local and client tests of the digital object migration functionality.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at>
 */
public final class Jasper19MigrationTest extends TestCase {
    
    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-jasper19/Jasper19Migration?wsdl";

    /* A holder for the object to be tested */
    Migrate dom = null;
    private File fTmpInFile;
    private File fTmpOutFile;


    List<String> formats = null;
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        formats = new ArrayList<String>();
        formats.add("jpg");
        formats.add("jp2");
        dom = ServiceCreator.createTestService(Migrate.QNAME, Jasper19Migration.class, wsdlLoc);
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
    @Test
    public void testDescribe() {
        ServiceDescription desc = dom.describe();
        System.out.println("Recieved service description: \n\n" + desc.toXmlFormatted());
        assertTrue("The ServiceDescription should not be NULL.", desc != null );
    }

    /**
     * test for all migrations
     * @throws IOException
     */
    @Test
    public void testMigrateAll() throws IOException {

        String origExt = null;
        String destExt = null;
        // Tests will be executed for 1 set of test files of the formats
        // that the jasper19 service wrapper supports:
        // demonstration1.jpg, demonstration1.jp2
            for (Iterator<String> itr1 = formats.iterator(); itr1.hasNext();) {
                origExt = (String) itr1.next();
                for (Iterator<String> itr2 = formats.iterator(); itr2.hasNext();)
                {
                    destExt = (String) itr2.next();
                    // do the migration only if original file extension differs
                    // from destination file extension
                    if( !origExt.equalsIgnoreCase(destExt) )
                    {
                        System.out.println("Do migration test from "+origExt+" to "+destExt);
                        doMigration(origExt,destExt, null);
                    }
                }
            }
        
    }

    private void doMigration(String origExt, String destExt, Parameters params) throws IOException
    {
        // Test file name
        String inTestFileName = "PA/jasper19/test/testfiles/demonstration." + origExt.toLowerCase();
        // Output file name
        //String outTestFileName = "PA/jasper19/test/testfiles/generatedfiles/planetsMigrate"+origExt+"to"+destExt+String.valueOf(cycle)+"."+destExt.toLowerCase();
        String resFileDir = "PA/jasper19/test/testfiles/generatedfiles/";
        String resFileName = "planetsMigrate"+origExt.toUpperCase()+"to"+destExt.toUpperCase()+"."+destExt.toLowerCase();
        byte[] binary = this.readByteArrayFromFile(inTestFileName);
        DigitalObject input = new DigitalObject.Builder(Content.byValue(binary)).build();
        MigrateResult mr = dom.migrate(input, Format.extensionToURI(origExt), Format.extensionToURI(destExt), params);
        DigitalObject doOut = mr.getDigitalObject();
        assertTrue("Resulting digital object is null for planetsMigrate"+origExt+"to"+destExt+".", doOut != null);
        FileUtils.writeInputStreamToFile(doOut.getContent().read(), new File( resFileDir), resFileName);
        File resultFile = new File(resFileDir+resFileName);
        assertTrue("Result file was not created successfully!", resultFile.exists());
    }

    /**
     * Test the pass-thru migration.
     */
//    @Test
//    public void testMigrateJPEGtoJP2() throws IOException {
//        try {
//            /*
//             * To test usability of the digital object instance in web services,
//             * we simply pass one into the service and expect one back:
//             */
//            byte[] binary = this.readByteArrayFromFile();
//            DigitalObject input = new DigitalObject.Builder( Content.byValue(binary))
//                    .build();
//
//            MigrateResult mr = dom.migrate(input, Format.extensionToURI("jpg"), Format.extensionToURI("jp2"), null);
//            assertTrue("Migration result object is null.", mr != null);
//            DigitalObject doOut = mr.getDigitalObject();
//
//            assertTrue("Resulting digital object is null.", doOut != null);
//
//            //String strOutFile = "PA/jasper19/test/testfiles/testout.jp2";
//            //File file = new File(strOutFile);
//            InputStream is = doOut.getContent().read();
//            //writeByteArrayToFile(doOut.getContent().getValue());
//            String resFileDir = "PA/jasper19/test/testfiles/";
//            String resFileName = "testout.jp2";
//            FileUtils.writeInputStreamToFile(is, new File( resFileDir), resFileName);
//            File resFile = new File( resFileDir+resFileName);
//            assertTrue("Result file has not been created successfully.", resFile.exists());
//
//
//        } catch (MalformedURLException e) {
//            fail("Malformed URL exception: "+e.toString());
//        }
//
//    }
    
    synchronized byte[] readByteArrayFromFile() 
            throws IOException {
        byte[] binary = new byte[0];
        
        String strOutFile = "PA/jasper19/test/testfiles/testin.jpg";
        fTmpOutFile = new File(strOutFile);
        assertTrue("JPG input file "+fTmpOutFile.getAbsolutePath()+" does not exist.", fTmpOutFile.exists());
        try {
            if( fTmpOutFile.isFile() && fTmpOutFile.canRead())
            {
                binary = new byte[(int)fTmpOutFile.length()];
                FileInputStream fis = new FileInputStream(fTmpOutFile);
                fis.read(binary);
                System.out.println("Read file: " + fTmpOutFile.getAbsolutePath());
                fis.close();
            }
            else
            {
                fail("Unable to read file: "+fTmpOutFile.getAbsolutePath());
            }
        } catch(IOException ex) {
            fail("IO Error: "+ex.toString());
        }
        return binary;
    }

        private synchronized byte[] readByteArrayFromFile(String strInFile)
            throws IOException {
        byte[] binary = new byte[0];
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
    
    synchronized void writeByteArrayToFile( byte[] binary )
            throws IOException {
        try {
            String strOutFile = "PA/jasper19/test/testfiles/testout.jp2";
            //String strOutFile = "test/testfiles/testout.ps";
            this.fTmpInFile = new File(strOutFile);
            System.out.println();
            BufferedOutputStream fos = 
                            new BufferedOutputStream(
                            new FileOutputStream(fTmpInFile));
            fos.write(binary);
            fos.close();
            assertTrue("Output file has not been created correctly. ", fTmpInFile.exists() && fTmpInFile.length() > 0);
        } catch(IOException ex) {
            fail("IO Error: "+ex.toString());
        }
    }
}
