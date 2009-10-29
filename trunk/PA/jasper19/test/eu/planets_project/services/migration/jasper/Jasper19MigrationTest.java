package eu.planets_project.services.migration.jasper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.Parameter;
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
public final class Jasper19MigrationTest extends TestCase {
    
    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-jasper19/Jasper19Migration?wsdl";

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
        assertTrue("The ServiceDescription should not be NULL.", desc != null );
        System.out.println("Recieved service description: \n\n" + desc.toXmlFormatted());
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
                    doMigration(origExt,destExt,"", null);
                }
            }
        }
    }
    
    @Test
    public void testWithParam_reversible()  throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm = new Parameter.Builder("rate", "1.0").build();
        params.add(parm);
        doMigration("jpg","jp2", "_param_rate", params);
    }


    @Test
    public void testWithParam_all()  throws IOException {
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter parm1 = new Parameter.Builder("rate", "1.0").build();
        params.add(parm1);
        Parameter parm2 = new Parameter.Builder("numrlvls", "6").build();
        params.add(parm2);
        Parameter parm3 = new Parameter.Builder("imgareatlx", "100").build();
        params.add(parm3);
        Parameter parm4 = new Parameter.Builder("imgareatly", "100").build();
        params.add(parm4);
        Parameter parm5 = new Parameter.Builder("tilegrdtlx", "100").build();
        params.add(parm5);
        Parameter parm6 = new Parameter.Builder("tilegrdtly", "100").build();
        params.add(parm6);
        Parameter parm7 = new Parameter.Builder("tilewidth", "100").build();
        params.add(parm7);
        Parameter parm8 = new Parameter.Builder("tileheight", "100").build();
        params.add(parm8);
        Parameter parm9 = new Parameter.Builder("prcwidth", "32768").build();
        params.add(parm9);
        Parameter parm10 = new Parameter.Builder("prcheight", "32768").build();
        params.add(parm10);
        Parameter parm11 = new Parameter.Builder("cdblkwidth", "64").build();
        params.add(parm11);
        Parameter parm12 = new Parameter.Builder("cdblkheight", "64").build();
        params.add(parm12);
        Parameter parm13 = new Parameter.Builder("prg", "lrcp").build();
        params.add(parm13);
        Parameter parm14 = new Parameter.Builder("nomct", "true").build();
        params.add(parm14);
        Parameter parm15 = new Parameter.Builder("sop", "true").build();
        params.add(parm15);
        Parameter parm16 = new Parameter.Builder("eph", "true").build();
        params.add(parm16);
        Parameter parm17 = new Parameter.Builder("lazy", "true").build();
        params.add(parm17);
        Parameter parm18 = new Parameter.Builder("termall", "true").build();
        params.add(parm18);
        Parameter parm19 = new Parameter.Builder("segsym", "true").build();
        params.add(parm19);
        Parameter parm20 = new Parameter.Builder("vcausal", "true").build();
        params.add(parm20);
        Parameter parm21 = new Parameter.Builder("pterm", "true").build();
        params.add(parm21);
        Parameter parm22 = new Parameter.Builder("resetprob", "true").build();
        params.add(parm22);
        Parameter parm23 = new Parameter.Builder("numgbits", "6").build();
        params.add(parm23);
        doMigration("jpg","jp2", "_param_all", params);
    }

    private void doMigration(String origExt, String destExt, String suffix, List<Parameter> params) throws IOException
    {
        // Test file name
        String inTestFileName = "PA/jasper19/test/testfiles/demonstration." + origExt.toLowerCase();
        // Output file name
        //String outTestFileName = "PA/jasper19/test/testfiles/generatedfiles/planetsMigrate"+origExt+"to"+destExt+String.valueOf(cycle)+"."+destExt.toLowerCase();
        String resFileDir = "PA/jasper19/test/testfiles/generatedfiles/";
        String resFileName = "planetsMigrate"+origExt.toUpperCase()+"to"+destExt.toUpperCase() + suffix+"."+destExt.toLowerCase();
        byte[] binary = FileUtils.readFileIntoByteArray(new File(inTestFileName));
        DigitalObject input = new DigitalObject.Builder(Content.byValue(binary)).build();
        FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
        MigrateResult mr = dom.migrate(input, format.createExtensionUri(origExt), format.createExtensionUri(destExt), params);
        DigitalObject doOut = mr.getDigitalObject();
        assertTrue("Resulting digital object is null for planetsMigrate"+origExt+"to"+destExt+".", doOut != null);
        FileUtils.writeInputStreamToFile(doOut.getContent().read(), new File( resFileDir), resFileName);
        File resultFile = new File(resFileDir+resFileName);
        assertTrue("Result file was not created successfully!", resultFile.exists());
    }
}
