package eu.planets_project.services.migration.pdf2pdfamaycomputer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * Local and client tests of the digital object migration functionality.
 *
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
public final class Pdf2PdfaMayComputerMigrationTest extends TestCase {

    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-pdf2pdfamaycomputer/Pdf2PdfaMayComputerMigration?wsdl";

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
        formats.add("dvi");
        formats.add("ps");
        dom = ServiceCreator.createTestService(Migrate.QNAME, Pdf2PdfaMayComputerMigration.class, wsdlLoc);
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
        // Tests will be executed for 1 test file of the formats
        // that the pdf2pdfamaycomputer service wrapper supports:
        // demonstration.dvi
        for (Iterator<String> itr1 = formats.iterator(); itr1.hasNext();) {
            origExt = (String) itr1.next();
            for (Iterator<String> itr2 = formats.iterator(); itr2.hasNext();)
            {
                destExt = (String) itr2.next();
                // do the migration only if original file extension differs
                // from destination file extension
                if( origExt.equalsIgnoreCase("dvi") && !origExt.equalsIgnoreCase(destExt) )
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
        String inTestFileName = "PA/pdf2pdfamaycomputer/test/testfiles/demonstration." + origExt.toLowerCase();
        // Output file name
        //String outTestFileName = "PA/pdf2pdfamaycomputer/test/testfiles/generatedfiles/planetsMigrate"+origExt+"to"+destExt+String.valueOf(cycle)+"."+destExt.toLowerCase();
        String resFileDir = "PA/pdf2pdfamaycomputer/test/testfiles/generatedfiles/";
        String resFileName = "planetsMigrate"+origExt.toUpperCase()+"to"+destExt.toUpperCase()+"."+destExt.toLowerCase();
        byte[] binary = FileUtils.readFileIntoByteArray(new File(inTestFileName));
        DigitalObject input = new DigitalObject.Builder(Content.byValue(binary)).build();
        MigrateResult mr = dom.migrate(input, Format.extensionToURI(origExt), Format.extensionToURI(destExt), params);
        DigitalObject doOut = mr.getDigitalObject();
        assertTrue("Resulting digital object is null for planetsMigrate"+origExt+"to"+destExt+".", doOut != null);
        FileUtils.writeInputStreamToFile(doOut.getContent().read(), new File( resFileDir), resFileName);
        File resultFile = new File(resFileDir+resFileName);
        assertTrue("Result file was not created successfully!", resultFile.exists());
    }
}
