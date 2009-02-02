package eu.planets_project.services.migration.xenaservices;

import org.junit.Test;

import eu.planets_project.services.migration.xenaservices.XenaOOMigration;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * Local and client tests of the digital object migration functionality.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at>
 */
public final class XenaServicesMigrationTest extends TestCase {

    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-xena/XenaOOMigration?wsdl";

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
        dom = ServiceCreator.createTestService(Migrate.QNAME, XenaOOMigration.class, wsdlLoc);
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
        assertTrue("The ServiceDescription should not be NULL.", desc != null);
    }

    /**
     * Test the pass-thru migration.
     */
    @Test
    public void testMigrate() throws IOException {


        byte[] binary_odt = ByteArrayHelper.read(new File("PA/xena/test/testfiles/testin.odt"));
        byte[] binary_odf = ByteArrayHelper.read(new File("PA/xena/test/testfiles/testin.odf"));

        DigitalObject input_odt = new DigitalObject.Builder(Content.byValue(binary_odt)).build();
        DigitalObject input_odf = new DigitalObject.Builder(Content.byValue(binary_odf)).build();

        MigrateResult mr_odt = dom.migrate(input_odt, null, null, null);
        DigitalObject doOut_odt = mr_odt.getDigitalObject();

        assertTrue("Resulting digital object is null.", doOut_odt != null);


        InputStream inputStream_odt = doOut_odt.getContent().read();

        FileUtils.writeInputStreamToFile(inputStream_odt, new File("PA/xena/test/testfiles"), "testout_odt.pdf");
        
        MigrateResult mr_odf = dom.migrate(input_odf, null, null, null);
        DigitalObject doOut_odf = mr_odf.getDigitalObject();

        assertTrue("Resulting digital object is null.", doOut_odf != null);


        InputStream inputStream_odf = doOut_odf.getContent().read();

        FileUtils.writeInputStreamToFile(inputStream_odf, new File("PA/xena/test/testfiles"), "testout_odf.pdf");

    }
}
