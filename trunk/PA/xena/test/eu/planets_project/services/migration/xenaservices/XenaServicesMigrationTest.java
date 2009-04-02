package eu.planets_project.services.migration.xenaservices;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import org.junit.Test;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

public final class XenaServicesMigrationTest extends TestCase {

    private enum OdfFormat {

        ODT, ODS, ODG, ODF;

        public static OdfFormat toOddFormat(String str) {
            try {
                return valueOf(str);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    };

    private enum MSOfficeFormat {

        DOC, XLS;

        public static MSOfficeFormat toMSOfficeFormat(String str) {
            try {
                return valueOf(str);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    };

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

        for (OdfFormat odfExt : OdfFormat.values()) {
            migrate(odfExt.toString().toLowerCase(), "fmt/95");
        }

        for (MSOfficeFormat officeExt : MSOfficeFormat.values()) {
            migrate(officeExt.toString().toLowerCase(), "fmt/95");
        }

        for (OdfFormat odfExt : OdfFormat.values()) {
            migrate(odfExt.toString().toLowerCase(), "fmt/18");
        }

        for (MSOfficeFormat officeExt : MSOfficeFormat.values()) {
            migrate(officeExt.toString().toLowerCase(), "fmt/18");
        }

    }

    private void migrate(String from, String to) {
        byte[] binary = FileUtils.readFileIntoByteArray(new File("PA/xena/test/testfiles/testin." + from));
        DigitalObject input = new DigitalObject.Builder(Content.byValue(binary)).build();
        MigrateResult mr = dom.migrate(input, Format.extensionToURI(from), Format.pronomIdToURI(to), null);
        DigitalObject doOut = mr.getDigitalObject();
        assertTrue("Resulting digital object is null.", doOut != null);
        InputStream inputStream_odf = doOut.getContent().read();
        if (to.equals("fmt/18")) {
            FileUtils.writeInputStreamToFile(inputStream_odf, new File("PA/xena/test/testfiles/out"), "testout_" + from + ".pdf");
        } else if (to.equals("fmt/95")) {
            FileUtils.writeInputStreamToFile(inputStream_odf, new File("PA/xena/test/testfiles/out"), "testout_" + from + "A.pdf");
        }
    }
}
