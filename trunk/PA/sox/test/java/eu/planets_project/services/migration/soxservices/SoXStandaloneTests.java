package eu.planets_project.services.migration.soxservices;

import org.junit.BeforeClass;

import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.utils.test.ServiceCreator;

public class SoXStandaloneTests extends SoXTests {
	
	@BeforeClass
    public static void setup() {
		TEST_OUT = SoXTestsHelper.SoX_STANDALONE_OUT;
		
    	System.out.println("Running SoX Standalone tests...");
    	System.out.println("**************************");
    	
    	System.setProperty("pserv.test.context", "Standalone");
    	
    	sox = ServiceCreator.createTestService(Migrate.QNAME, SoX.class, wsdlLocation);
    	
    	migrationPaths = sox.describe().getPaths().toArray(new MigrationPath[]{});
    }

}
