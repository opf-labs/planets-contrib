/**
 * 
 */
package eu.planets_project.services.migration.dioscuri;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * @author melmsp
 *
 */
public class DioscuriPnmToPngMigrationTest {
	
	public static String wsdlLoc = "/pserv-pa-dioscuri-migrate/DioscuriPnmToPngMigration?wsdl"; 
	
	public static Migrate DIOSCURI_MIGRATE = null;
	
	public static File DIOSCURI_TEST_OUT = FileUtils.createWorkFolderInSysTemp("DIOSCURI_TEST_OUT");
	
	public static File PNM_TEST_FILE = new File("tests/test-files/images/bitmap/test_pnm/BASI0G02.PNM"); 
	public static File PNG_TEST_FILE = null;
	
	public static FormatRegistry format = FormatRegistryFactory.getFormatRegistry();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
      	Logger.getLogger(DioscuriPnmToPngMigration.class.getName()).setLevel(Level.FINE);		
		System.setProperty("pserv.test.context", "server");
        System.setProperty("pserv.test.host", "localhost");
        System.setProperty("pserv.test.port", "8080");
		DIOSCURI_MIGRATE = ServiceCreator.createTestService(Migrate.QNAME, DioscuriPnmToPngMigration.class, wsdlLoc);
	}
	
	@Test
	public void testDescribe() {
		ServiceDescription sd = DIOSCURI_MIGRATE.describe();
		assertTrue("The ServiceDescription should not be NULL.", sd != null );
    	System.out.println("test: describe()");
    	System.out.println("--------------------------------------------------------------------");
    	System.out.println();
    	System.out.println("Received ServiceDescription from: " + DIOSCURI_MIGRATE.getClass().getName());
    	System.out.println(sd.toXmlFormatted());
    	System.out.println("--------------------------------------------------------------------");
	}
	
	@Test
	public void testMigrate() {
		printTestTitle("Testing Doscuri PNM to PNG migration");
		DigitalObject inputDigOb = new DigitalObject.Builder(Content.byReference(PNM_TEST_FILE)).title(PNM_TEST_FILE.getName()).format(format.createExtensionUri(FileUtils.getExtensionFromFile(PNM_TEST_FILE))).build();
		MigrateResult result = DIOSCURI_MIGRATE.migrate(inputDigOb, format.createExtensionUri(FileUtils.getExtensionFromFile(PNM_TEST_FILE)), format.createExtensionUri("PNG"), null);
		
		assertTrue("MigrateResult should not be NULL", result!=null);
		System.out.println("DEBUG ServiceReport: " + result.getReport() );
		assertTrue("ServiceReport should be SUCCESS", result.getReport().getStatus()==Status.SUCCESS);
		
		System.out.println(result.getReport());
		
		File resultFile = new File(DIOSCURI_TEST_OUT, result.getDigitalObject().getTitle());
		FileUtils.writeInputStreamToFile(result.getDigitalObject().getContent().getInputStream(), resultFile);
		
		System.out.println("Please find the converted file here: " + resultFile.getAbsolutePath());
		
		PNG_TEST_FILE = resultFile;
		printTestTitle("Testing Doscuri PNG to PNM migration");
		inputDigOb = new DigitalObject.Builder(Content.byReference(PNG_TEST_FILE)).title(PNG_TEST_FILE.getName()).format(format.createExtensionUri(FileUtils.getExtensionFromFile(PNG_TEST_FILE))).build();
		result = DIOSCURI_MIGRATE.migrate(inputDigOb, format.createExtensionUri(FileUtils.getExtensionFromFile(PNG_TEST_FILE)), format.createExtensionUri("PNM"), null);
		
		assertTrue("MigrateResult should not be NULL", result!=null);
		assertTrue("ServiceReport should be SUCCESS", result.getReport().getStatus()==Status.SUCCESS);
		
		System.out.println(result.getReport());
		
		resultFile = new File(DIOSCURI_TEST_OUT, result.getDigitalObject().getTitle());
		FileUtils.writeInputStreamToFile(result.getDigitalObject().getContent().getInputStream(), resultFile);
		
		System.out.println("Please find the converted file here: " + resultFile.getAbsolutePath());
	}
	
	private void printTestTitle(String title) {
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

}
