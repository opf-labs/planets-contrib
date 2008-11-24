package eu.planets_project.services.migration.dvips;

import java.net.MalformedURLException;

import org.junit.Test;

import eu.planets_project.services.migration.gimp.Gimp26Migration;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.test.ServiceCreator;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * Local and client tests of the digital object migration functionality.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at>
 */
public final class Gimp26MigrationTest {
    
    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-gimp/Gimp26MigrationNew?wsdl";

    /* A holder for the object to be tested */
    Migrate dom = null;
    private File fTmpInFile;
    private File fTmpOutFile;
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        dom = ServiceCreator.createTestService(Migrate.QNAME, DviPsMigration.class, wsdlLoc );
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
        System.out.println("Recieved service description: " + desc);
        assertTrue("The ServiceDescription should not be NULL.", desc != null );
    }

    /**
     * Test the pass-thru migration.
     */
    @Test
    public void testMigrate() throws IOException {
        try {
            /*
             * To test usability of the digital object instance in web services,
             * we simply pass one into the service and expect one back:
             */
            byte[] binary = this.readByteArrayFromFile();
            DigitalObject input = new DigitalObject.Builder( Content.byValue(binary))
                    .build();

            MigrateResult mr = dom.migrate(input, null, null, null);
            DigitalObject doOut = mr.getDigitalObject();

            assertTrue("Resulting digital object is null.", doOut != null);

            
            writeByteArrayToFile(doOut.getContent().getValue());
            

        } catch (MalformedURLException e) {
            fail("Malformed URL exception: "+e.toString());
        }

    }
    
    synchronized byte[] readByteArrayFromFile() 
            throws IOException {
        byte[] binary = new byte[0];
        
        String strOutFile = "PA/dvips/test/testfiles/testin.dvi";
        //String strOutFile = "test/testfiles/testin.dvi";
        fTmpOutFile = new File(strOutFile);
        assertTrue("DVI input file "+fTmpOutFile.getAbsolutePath()+" does not exist.", fTmpOutFile.exists());
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
    
    synchronized void writeByteArrayToFile( byte[] binary )
            throws IOException {
        try {
            String strOutFile = "PA/dvips/test/testfiles/testout.ps";
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
