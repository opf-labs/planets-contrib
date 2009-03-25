package eu.planets_project.services.migration.avidemux;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * Local and client tests of the digital object migration functionality.
 *
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
public final class AvidemuxMigrationTest extends TestCase {

    /* The location of this service when deployed. */
    String wsdlLoc = "/pserv-pa-avidemux/AvidemuxMigration?wsdl";

    PlanetsLogger log = PlanetsLogger.getLogger(AvidemuxMigrationTest.class);

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
        formats.add("mpeg");
        formats.add("avi");
        dom = ServiceCreator.createTestService(Migrate.QNAME, AvidemuxMigration.class, wsdlLoc);
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
        log.info("Recieved service description: \n\n" + desc.toXmlFormatted());
        assertTrue("The ServiceDescription should not be NULL.", desc != null );
    }

    /**
     * test for all migrations
     * @throws IOException
     */
    @Test
    public void testMigrateDefault() throws IOException {
        doMigration("mpeg","avi", "1" , "1_DefaultParameters",null);
        doMigration("mpeg","avi", "2" , "2_DefaultParameters",null);
    }
    
    public void tParameter(String paramName,String codecName) throws IOException {
        Parameters parameters = new Parameters();
        parameters.add(paramName, codecName);
        doMigration("mpeg","avi", "2" , "2_TestParameter_"+paramName+"_"+codecName,parameters);
    }
    @Test
    public void testParameterVideoCodec_XVID4() throws IOException
    {
        tParameter("video-codec","XVID4");
    }
    @Test
    public void testParameterVideoCodec_X264() throws IOException
    {
        tParameter("video-codec","X264");
    }
    @Test
    public void testParameterVideoCodec_FFMPEG4() throws IOException
    {
        tParameter("video-codec","FFMPEG4");
    }
    @Test
    public void testParameterAudioCodec_MP2() throws IOException
    {
        tParameter("audio-codec","MP2");
    }
    @Test
    public void testParameterAudioCodec_MP3() throws IOException
    {
        tParameter("audio-codec","MP3");
    }
    @Test
    public void testParameterAudioCodec_AC3() throws IOException
    {
        tParameter("audio-codec","AC3");
    }
    @Test
    public void testParameterFPS() throws IOException
    {
        tParameter("fps","5"); // Should make the video "studdering" and desynchronize audio and video
    }

    private void doMigration(String origExt, String destExt, String origSuffix, String destSuffix, Parameters params) throws IOException
    {
        // Test file name
        String inTestFileName = "PA/avidemux/test/testfiles/demonstration"+origSuffix+"." + origExt.toLowerCase();
        // Output file name
        //String outTestFileName = "PA/avidemux/test/testfiles/generatedfiles/planetsMigrate"+origExt+"to"+destExt+String.valueOf(cycle)+"."+destExt.toLowerCase();
        String resFileDir = "PA/avidemux/test/testfiles/generatedfiles/";
        String resFileName = "planetsMigrate"+origExt.toUpperCase()+"to"+destExt.toUpperCase()+destSuffix+"."+destExt.toLowerCase();
        byte[] binary = FileUtils.readFileIntoByteArray(new File(inTestFileName));
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
