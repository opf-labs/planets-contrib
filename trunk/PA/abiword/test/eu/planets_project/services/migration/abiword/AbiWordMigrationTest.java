package eu.planets_project.services.migration.abiword;

import eu.planets_project.ifr.core.techreg.api.formats.Format;

import org.junit.Test;

import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;
import java.io.File;
import java.io.IOException;
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
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
public final class AbiWordMigrationTest extends TestCase {

    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-abiword/AbiWordMigration?wsdl";

    /* A holder for the object to be tested */
    Migrate dom = null;

    List<String> formats = null;
    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        formats = new ArrayList<String>();
        formats.add("doc");
        formats.add("rtf");
        formats.add("html");
        formats.add("pdf");
        formats.add("txt");
        dom = ServiceCreator.createTestService(Migrate.QNAME, AbiWordMigration.class, wsdlLoc);
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
        // that the abiword service wrapper supports:
        // demonstration.jpg, demonstration.jp2
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
        String inTestFileName = "PA/abiword/test/testfiles/demonstration." + origExt.toLowerCase();
        // Output file name
        //String outTestFileName = "PA/abiword/test/testfiles/generatedfiles/planetsMigrate"+origExt+"to"+destExt+String.valueOf(cycle)+"."+destExt.toLowerCase();
        String resFileDir = "PA/abiword/test/testfiles/generatedfiles/";
        String resFileName = "planetsMigrate"+origExt.toUpperCase()+"to"+destExt.toUpperCase()+"."+destExt.toLowerCase();
        byte[] binary = ByteArrayHelper.read(new File(inTestFileName));
        DigitalObject input = new DigitalObject.Builder(Content.byValue(binary)).build();
        MigrateResult mr = dom.migrate(input, Format.extensionToURI(origExt), Format.extensionToURI(destExt), params);
        assertTrue("Migration result is null is null for planetsMigrate"+origExt+"to"+destExt+".", mr != null);
        DigitalObject doOut = mr.getDigitalObject();
        assertTrue("Resulting digital object is null for planetsMigrate"+origExt+"to"+destExt+".", doOut != null);
        FileUtils.writeInputStreamToFile(doOut.getContent().read(), new File( resFileDir), resFileName);
        File resultFile = new File(resFileDir+resFileName);
        assertTrue("Result file was not created successfully!", resultFile.exists());
    }
}
