package eu.planets_project.services.migration.ghostscript;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.DigitalObjectContent;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * Tests of the digital object migration functionality
 * for the GhostscriptMigration service.
 * @author <a href="mailto:cjen@kb.dk">Claus Jensen</a>
 */
public class GhostscriptMigrationTest extends TestCase {

    /**
     * The location of the GhostscriptMigration service when deployed.
     */
    private static final String wsdlLoc = "/pserv-pa-ghostscript"
            + "/GhostscriptMigration?wsdl";

    /**
     * A holder for the object to be tested.
     */
    private Migrate dom = null;

    /**
     * A test PostScript file object.
     */
    private final File testps =
        new File("PA/ghostscript/test/resources/test.ps");

    /**
     * Set up test folder.
     */
    private File workfolder = FileUtils
        .createWorkFolderInSysTemp("ghostscript_test");

    /**
     * Remove the test folder or not.
     */
    private boolean removeTestFolder = false;

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected final void setUp() throws Exception {
        dom = ServiceCreator.createTestService(Migrate.QNAME,
                GhostscriptMigration.class, wsdlLoc);

        // Sets the removeTestFolder to clean up temporary files and folders.
        setRemoveTestFolder(true);
        }

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected final void tearDown() throws Exception {
        super.tearDown();
        if (isRemoveTestFolder()) {
            FileUtils.deleteTempFiles(workfolder);
        }
    }

    /**
     * Test the Description method.
     */
    @Test
    public final void testDescribe() {
        final ServiceDescription desc = dom.describe();
        System.out.println("Recieved service description: "
            + desc.toXmlFormatted());
        assertNotNull("The ServiceDescription should not be NULL.", desc);
    }

    /**
     * Test PS to PDF migration.
     * Testing the web services by calling it with
     * a digital object instance containing a PS
     * and expect the service to return a PDF file.
     * @throws IOException Throws java.io.IOException.
     */
    @Test
    public final void testPS2PDFMigration() throws IOException {
        System.out.println(testps.getCanonicalPath());

        try {
            //final URI formatPS = new URI("info:pronom/x-fmt/408");
            //final URI formatPDF = new URI("info:pronom/fmt/18");

            final URI formatPS = new URI("planets:fmt/ext/ps");
            final URI formatPDF = new URI("planets:fmt/ext/pdf");

            final DigitalObject doInput =
                new DigitalObject.Builder(
                    Content.byReference((testps).toURI().toURL()))
                    .permanentUri(URI.create("http://example.com/test.ps"))
                    .title("test.ps")
                    .build();
            System.out.println("Input " + doInput);

            final MigrateResult mr = dom.migrate(doInput, formatPS,
                formatPDF, this.createParameters(true));
            final DigitalObject doOutput = mr.getDigitalObject();

            assertNotNull("Resulting digital object is null, error was "
                          + mr.getReport().getMessage(), doOutput);

            System.out.println("Output" + doOutput);

            final DigitalObjectContent content = doOutput.getContent();

            this.workfolder = FileUtils
                .createWorkFolderInSysTemp("ghostscript_test");

            final File resultText = FileUtils.writeInputStreamToFile(
                content.read(), this.workfolder, "ps2pdf_result.pdf");

            assertTrue("Result file was not created successfully!",
                        resultText.exists());

            if (!isRemoveTestFolder()) {
                System.out.println("Please find the result text file here: \n"
                                   + resultText.getAbsolutePath());
            }

            assertFalse("Resulting digital object equal to the original.",
                    doInput.equals(doOutput));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create test parameters for migrate method.
     * @param noPlatFontsFlag Should noPlatFonts parameter be created.
     * @return List of parameters.
     */
    private List<Parameter> createParameters(final boolean noPlatFontsFlag) {
        List<Parameter> parameterList = new ArrayList<Parameter>();

        if (noPlatFontsFlag) {
            Parameter noPlatFonts = new Parameter.Builder("noPlatFonts",
                    "-dNOPLATFONTS")
                    .description(
                            "Disables the use of fonts supplied by "
                                    + "the underlying platform (for instance X Windows). "
                                    + "This may be needed if the platform fonts look "
                                    + "undesirably different from the scalable fonts.")
                    .build();
            parameterList.add(noPlatFonts);
        }

        return parameterList;
    }

    /**
      * @param removeTestFolder the removeTestFolder to set
    */
    private void setRemoveTestFolder(final boolean removeTestFolder) {
        this.removeTestFolder = removeTestFolder;
    }

    /**
      * @return the removeTestFolder
    */
    private boolean isRemoveTestFolder() {
        return removeTestFolder;
    }
}
