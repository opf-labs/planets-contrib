package eu.planets_project.services.jj2000;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import eu.planets_project.services.datatypes.ImmutableContent;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.jj2000.JJ2000MigrateService;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * Local and client tests of the digital object migration functionality.
 * @author Fabian Steeg
 */
public final class JJ2000MigrateTest extends TestCase {

    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-jj2000/JJ2000MigrateService?wsdl";

    /* A holder for the object to be tested */
    Migrate dom = null;

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Use a helper function to set up the testable class:
        dom = ServiceCreator.createTestService(Migrate.QNAME,
                JJ2000MigrateService.class, wsdlLoc);

    }

    /**
     * Test the Description method.
     */
    @Test
    public void testDescribe() {
        System.out.println("Asking for description:");
        ServiceDescription desc = dom.describe();
        assertTrue("The ServiceDescription should not be NULL.", desc != null);
        System.out.println("Recieved service description: "
                + desc.toXmlFormatted());
    }

    /**
     * Test the pass-thru migration.
     * @throws IOException
     */
    @Test
    public void testMigrate() throws IOException {
        this.doMigrate("PA/jj2000/src/test/resources/Descartes.ppm", "PA/jj2000/src/test/results/test.jp2", "ppm", "jp2");
        this.doMigrate("PA/jj2000/src/test/resources/Descartes.jp2", "PA/jj2000/src/test/results/test.ppm", "jp2", "ppm");
    }

    /**
     * @param inFile
     * @param outFile
     * @param inExt
     * @param outExt
     * @throws IOException 
     */
    private void doMigrate(String inFile, String outFile, String inExt, String outExt) throws IOException {
        DigitalObject input = new DigitalObject.Builder(
                ImmutableContent
                .byReference(new File( inFile )
                .toURI().toURL())).permanentUrl(
                        new URL("http://some")).build();
        System.out.println("Input: " + input);

        MigrateResult mr = dom.migrate(input, Format.extensionToURI( inExt ),
                Format.extensionToURI(outExt), null);

        ServiceReport sr = mr.getReport();
        System.out.println("Got Report: " + sr);

        DigitalObject doOut = mr.getDigitalObject();

        assertTrue("Resulting digital object is null.", doOut != null);

        System.out.println("Output: " + doOut);
        System.out.println("Output.content: " + doOut.getContent());
        System.out.println("Output.content.read().available(): "
                + doOut.getContent().read().available());

        File out = new File(outFile);
        writeInStreamToOutStream(doOut.getContent().read(), new FileOutputStream(out));

    }
    
    private static void writeInStreamToOutStream(InputStream in, OutputStream op) throws IOException {
        byte[] bbuf = new byte[2*1024];
        int length = 0;

        try {
            while ((in != null) && ((length = in.read(bbuf)) != -1))
                {
                op.write(bbuf,0,length);
                }
        } finally {
                if (in != null) {
                    in.close();
                }
                op.flush();
                op.close();
        }
    }

}
