package eu.planets_project.services.migration.xenaservices;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import org.junit.Test;

import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.xenaservices.XenaOOMigration.supportedOdfFormats;
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

    @Before
    public void setUp() throws Exception {
        dom = ServiceCreator.createTestService(Migrate.QNAME, XenaOOMigration.class, wsdlLoc);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDescribe() {
        ServiceDescription desc = dom.describe();
        System.out.println("Recieved service description: \n\n" + desc.toXmlFormatted());
        assertTrue("The ServiceDescription should not be NULL.", desc != null);
    }

    @Test
    public void testMigrate() throws IOException {

        for (supportedOdfFormats odfExt : supportedOdfFormats.values()) {
            migrate(odfExt.toString(), "pdf");
        }
        migrate("odt", "doc");
    }

    private void migrate(String from, String to) {
        byte[] binary = ByteArrayHelper.read(new File("PA/xena/test/testfiles/testin." + from));
        DigitalObject input = new DigitalObject.Builder(Content.byValue(binary)).build();
        MigrateResult mr = dom.migrate(input, Format.extensionToURI(from), Format.extensionToURI(to), null);
        DigitalObject doOut = mr.getDigitalObject();
        assertTrue("Resulting digital object is null.", doOut != null);
        InputStream inputStream_odf = doOut.getContent().read();
        FileUtils.writeInputStreamToFile(inputStream_odf, new File("PA/xena/test/testfiles/out"), "testout_" + from + "." + to);
    }
}
