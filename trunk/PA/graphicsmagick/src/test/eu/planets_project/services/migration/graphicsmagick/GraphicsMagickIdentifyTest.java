/**
 * 
 */
package eu.planets_project.services.migration.graphicsmagick;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Agent;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Event;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.identification.graphicsmagick.GraphicsMagickIdentify;
import eu.planets_project.services.identification.imagemagick.ImageMagickIdentify;
import eu.planets_project.services.identification.imagemagick.ImageMagickIdentifyTestHelper;
import eu.planets_project.services.identification.imagemagick.ImageMagickIdentifyTests;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.utils.ServiceUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * @author melmsp
 *
 */
public class GraphicsMagickIdentifyTest {
	
	static Identify identify;

    static String WSDL = "/pserv-pa-graphicsmagick/GraphicsMagickIdentify?wsdl";

    static FormatRegistry fr = FormatRegistryFactory.getFormatRegistry();
    
    @BeforeClass
    public static void setUp() throws Exception {
        printTestTitle("Running tests for GraphicsMagickIdentify");
        identify = ServiceCreator.createTestService(Identify.QNAME, GraphicsMagickIdentify.class, WSDL);
    }
    
    @Test
    public void testDescribe() {
        ServiceDescription desc = identify.describe();
        assertTrue("The ServiceDescription should not be NULL.", desc != null );
        System.out.println("Recieved service description: " + desc.toXmlFormatted());
    }
    
    @Test
    public void testIdentify() throws MalformedURLException {
        HashMap<File, String> files = ImageMagickIdentifyTestHelper.getTestFiles();

        Set<File> fileSet = files.keySet();

        String id = Long.toString(GraphicsMagickIdentify.serialVersionUID);
        String name = this.getClass().getCanonicalName();
        String type = "JUnit Testcase";

        String datetime = ServiceUtils.getSystemDateAndTimeFormatted();
        String summary = "This is just a short test of the Event construct...using GraphicsMagickIdentify...";


        Agent agent = new Agent(id,name,type);
        Event event = new Event(summary,datetime,0d,agent,null);

        for (File file : fileSet) {
            String ext = files.get(file);
            DigitalObject digObj = new DigitalObject.Builder(Content.byValue(file))
                    .title(file.getName())
                    .events(event)
                    .build();
            System.out.println("Testing identification of " + ext.toUpperCase() + ": " + file.getName());
            IdentifyResult ir = identify.identify(digObj, null);
            validateResult(ir);
        }
    }
    
    private static void printTestTitle(String title) {
		for(int i=0;i<title.length()+4;i++) {
			System.out.print("*");
		}
		System.out.println();
		System.out.println("* " + title + " *");
		for(int i=0;i<title.length()+4;i++) {
			System.out.print("*");
		}
		System.out.println();
	}
    
    protected void validateResult(IdentifyResult identifyResult) {
        ServiceReport sr = identifyResult.getReport();

        if(sr.getType() == Type.ERROR) {
            System.err.println("FAILED: " + sr);
        }
        else {
            System.out.println("SUCCESS! Got Report: " + sr);
            List<URI> types = identifyResult.getTypes();
            assertTrue("List of types should not be null or of length 0", types.size()>0);
            System.out.println("Received file type URIs: ");
            for (URI uri : types) {
                System.out.println(uri.toASCIIString());
            }
        }
    }


}
