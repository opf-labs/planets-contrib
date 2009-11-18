package eu.planets_project.services.migration.kakadu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.planets_project.ifr.core.techreg.formats.api.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * Local and client tests of the digital object migration functionality.
 *
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
public final class KakaduCompressMigrationTest extends TestCase {

    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-kakadu/KakaduCompressMigration?wsdl";
    PlanetsLogger log = PlanetsLogger.getLogger(KakaduCompressMigrationTest.class);

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
        formats.add("tif");
        formats.add("jp2");
        dom = ServiceCreator.createTestService(Migrate.QNAME, KakaduCompressMigration.class, wsdlLoc);
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
        assertTrue("The ServiceDescription should not be NULL.", desc != null);
        log.info("Recieved service description: \n\n" + desc.toXmlFormatted());
    }

    /**
     * test for all migrations
     * @throws IOException
     */

    @Test
    public void testWithoutParam()  throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        doMigration("tif","jp2", "_withoutparam", params);
    }

    @Test
    public void testWithParam_reversible()  throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm = new Parameter.Builder("reversible", "true").build();
        params.add(parm);
        doMigration("tif","jp2", "_param_reversible", params);
    }

    @Test
    public void testWithParam_rate()  throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm = new Parameter.Builder("rate", "1.3,0.75,0.2").build();
        params.add(parm);
        doMigration("tif","jp2", "_param_rate", params);
    }

    @Test
    public void testWithParam_layers()  throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm = new Parameter.Builder("layers", "5").build();
        params.add(parm);
        doMigration("tif","jp2", "_param_layers", params);
    }
    @Test
    public void testWithParam_levels()  throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm = new Parameter.Builder("levels", "5").build();
        params.add(parm);
        doMigration("tif","jp2", "_param_levels", params);
    }
    @Test
    public void testWithParam_tiles() throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm = new Parameter.Builder("tiles", "100,100").build();
        params.add(parm);
        doMigration("tif", "jp2", "_param_tiles", params);
    }
    @Test
    public void testWithParam_cblk() throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm = new Parameter.Builder("cblk", "64,64").build();
        params.add(parm);
        doMigration("tif", "jp2", "_param_cblk", params);
    }
    @Test
    public void testWithParam_order() throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm = new Parameter.Builder("order", "LRCP").build();
        params.add(parm);
        doMigration("tif", "jp2", "_param_order", params);
    }

    @Test
    public void testWithParam_all() throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm1 = new Parameter.Builder("order", "LRCP").build();
        Parameter parm2 = new Parameter.Builder("cblk", "64,64").build();
        Parameter parm3 = new Parameter.Builder("tiles", "100,100").build();
        Parameter parm4 = new Parameter.Builder("levels", "5").build();
        Parameter parm5 = new Parameter.Builder("layers", "5").build();
        Parameter parm6 = new Parameter.Builder("reversible", "true").build();
        Parameter parm7 = new Parameter.Builder("rate", "1.3").build();
        params.add(parm1);
        params.add(parm2);
        params.add(parm3);
        params.add(parm4);
        params.add(parm5);
        params.add(parm6);
        params.add(parm7);
        doMigration("tif", "jp2", "_param_all", params);
    }

    private void doMigration(String origExt, String destExt, String suffix, List<Parameter> params) throws IOException {
        // Test file name
        String inTestFileName = "PA/kakadu/test/testfiles/demonstration." + origExt.toLowerCase();
        // Output file name
        //String outTestFileName = "PA/kakadu/test/testfiles/generatedfiles/planetsMigrate"+origExt+"to"+destExt+String.valueOf(cycle)+"."+destExt.toLowerCase();
        String resFileDir = "PA/kakadu/test/testfiles/generatedfiles/";
        String resFileName = "planetsMigrate" + origExt.toUpperCase() + "to" + destExt.toUpperCase() + suffix + "." + destExt.toLowerCase();
        byte[] binary = FileUtils.readFileIntoByteArray(new File(inTestFileName));
        DigitalObject input = new DigitalObject.Builder(Content.byValue(binary)).build();
        FormatRegistry formatRegistry = FormatRegistryFactory.getFormatRegistry();
        MigrateResult mr = dom.migrate(input, formatRegistry.createExtensionUri(origExt), formatRegistry.createExtensionUri(destExt), params);
        log.info("Service report: " + mr.getReport().getMessage());
        assertTrue("Migration result is null for planetsMigrate" + origExt + "to" + destExt + ".", mr != null);
        DigitalObject doOut = mr.getDigitalObject();
        assertTrue("Resulting digital object is null for planetsMigrate" + origExt + "to" + destExt + ".", doOut != null);
        FileUtils.writeInputStreamToFile(doOut.getContent().read(), new File(resFileDir), resFileName);
        File resultFile = new File(resFileDir + resFileName);
        assertTrue("Result file was not created successfully!", resultFile.exists());

    }
}
