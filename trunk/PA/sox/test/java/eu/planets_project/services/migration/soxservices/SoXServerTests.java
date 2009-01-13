package eu.planets_project.services.migration.soxservices;

import org.junit.BeforeClass;

import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.utils.test.ServiceCreator;

public class SoXServerTests extends SoXTests {

	@BeforeClass
    public static void setup() {
		TEST_OUT = SoXTestsHelper.SoX_SERVER_OUT;
		
    	System.out.println("Running SoX SERVER tests...");
    	System.out.println("**************************");
    	
    	System.setProperty("pserv.test.context", "server");
        System.setProperty("pserv.test.host", "localhost");
        System.setProperty("pserv.test.port", "8080");
    	
    	sox = ServiceCreator.createTestService(Migrate.QNAME, SoX.class, wsdlLocation);
    	
    	migrationPaths = sox.describe().getPaths().toArray(new MigrationPath[]{});
    }
}
