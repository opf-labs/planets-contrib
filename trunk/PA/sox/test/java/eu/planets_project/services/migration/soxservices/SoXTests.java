package eu.planets_project.services.migration.soxservices;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ImmutableContent;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;

public class SoXTests {
	
	static Migrate sox;
	
    /* The location of this service when deployed. */
	static String wsdlLocation = "/pserv-pa-sox/SoX?wsdl";
	
	/**
	 * test output
	 */
	public static String TEST_OUT = null;
	
	public static MigrationPath[] migrationPaths;
	
	@BeforeClass
    public static void setup() {
		TEST_OUT = SoXTestsHelper.SoX_LOCAL_OUT;
		
    	System.out.println("Running SoX LOCAL tests...");
    	System.out.println("**************************");
    	
    	System.setProperty("pserv.test.context", "local");
    	
//    	sox = ServiceCreator.createTestService(Migrate.QNAME, SoX.class, wsdlLocation);
    	sox = new SoX();
    	
    	migrationPaths = sox.describe().getPaths().toArray(new MigrationPath[]{});
    }

	@Test
	public void testDescribe() {
		ServiceDescription desc = sox.describe();
        System.out.println("Recieved service description: " + desc.toXmlFormatted());
        assertTrue("The ServiceDescription should not be NULL.", desc != null );
	}
	
	@Test
	public void testUnsupportedFormats() throws URISyntaxException, InterruptedException {
		URI inputFormat = new URI("planets:fmt/ext/mp3");
		URI outputFormat = new URI("planets:fmt/ext/wav");
		System.out.println("Testing unsupported INPUT format: " + inputFormat.toASCIIString() + "...service should fail!");
		testMigrate(inputFormat, outputFormat, null);
		wait(2500000);
		
		inputFormat = new URI("planets:fmt/ext/wav");
		outputFormat = new URI("planets:fmt/ext/mp3");
		System.out.println("Testing unsupported OUTPUT format: " + outputFormat.toASCIIString() + "...service should fail!");
		testMigrate(inputFormat, outputFormat, null);
		wait(2500000);
		
		inputFormat = new URI("planets:fmt/ext/raw");
		outputFormat = new URI("planets:fmt/ext/mp3");
		System.out.println("Testing unsupported migration path: [" 
							+ inputFormat.toASCIIString() 
							+ " --> " + outputFormat.toASCIIString() + "]...service should fail!");
		testMigrate(inputFormat, outputFormat, null);
	}
	
	@Test
	public void testAllPossibleMigrationPathways() {
		for(int i=0;i<migrationPaths.length;i++) {
			MigrationPath path = migrationPaths[i];
			URI inputFormat = path.getInputFormat();
			URI outputFormat = path.getOutputFormat();
			
			System.out.println("-------------------------------------------------------------");
			System.out.println("Testing migrationPath: [" + inputFormat.toASCIIString() + " --> " + outputFormat.toASCIIString() + "]");
			
			System.out.println("ShowProgress = TRUE, Verbosity level: 0");
			System.out.println("------------------");
			List<Parameter> parameters = createParameters(true, false, "0");
			testMigrate(inputFormat, outputFormat, parameters);
			System.out.println("*******************");
			
			System.out.println("ShowProgress = TRUE, Verbosity level: 4");
			System.out.println("------------------");
			parameters = createParameters(true, false, "4");
			testMigrate(inputFormat, outputFormat, parameters);
			System.out.println("*******************");
			System.out.println();
			
			System.out.println("ShowProgress = FALSE, Verbosity level: 4");
			System.out.println("------------------");
			parameters = createParameters(false, false, "4");
			testMigrate(inputFormat, outputFormat, parameters);
			System.out.println("*******************");
			System.out.println();
			
//			System.out.println("ShowProgress = FALSE, NoShowProgress = TRUE, Verbosity level: 4");
//			System.out.println("------------------");
//			parameters = createParameters(false, true, "4");
//			testMigrate(inputFormat, outputFormat, parameters);
//			System.out.println("*******************");
//			System.out.println();
			
//			System.out.println("ShowProgress = TRUE, NoShowProgress = TRUE, Verbosity level: 4");
//			System.out.println("------------------");
//			parameters = createParameters(true, true, "4");
//			testMigrate(inputFormat, outputFormat, parameters);
//			System.out.println("*******************");
//			System.out.println();
			
			System.out.println("-------------------------------------------------------------\n\n");
		}
	}
	
	private void wait(int millisecondsToWait) {
		for(int i=0; i<=millisecondsToWait;i++) {
			// do nothing but wait ;-)
		}
	}
	
	private void testMigrate(URI inputFormat, URI outputFormat, List<Parameter> parameters) {
		String extension = FormatRegistryFactory.getFormatRegistry()
                .getFirstExtension(inputFormat);
        DigitalObject digObj = createDigitalObject(extension);
		
		MigrateResult mr = sox.migrate(digObj, inputFormat, outputFormat, parameters);

		ServiceReport sr = mr.getReport();
		
		if(sr.getType() == Type.ERROR) {
			System.err.println("FAILED: " + sr);
		}
		else {
			System.out.println("Got Report: " + sr);
        
			DigitalObject doOut = mr.getDigitalObject();

			assertTrue("Resulting digital object is null.", doOut != null);

			System.out.println("Result: " + doOut);
			System.out.println("Result.content: " + doOut.getContent());
		}
	}
	
	
	
	
	private DigitalObject createDigitalObject(String srcExtension) {
		
		File inputFile = getTestFile(srcExtension);
    	
        DigitalObject input = null;
        
		try {
			input = new DigitalObject.Builder(ImmutableContent.byValue(inputFile)).permanentUrl(new URL("http://soxMigrationsTest.eu")).build();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return input;
	}
	
	private File getTestFile(String srcExtension) {
    	
    	if (srcExtension.equalsIgnoreCase("AIFF")) {
    		return SoXTestsHelper.AIFF_INPUT;
		}
    	
    	if (srcExtension.equalsIgnoreCase("FLAC")) {
    		return SoXTestsHelper.FLAC_INPUT;
		}
    	
    	
    	if (srcExtension.equalsIgnoreCase("MP3")) {
    		return SoXTestsHelper.MP3_INPUT;
		}
    	
    	if (srcExtension.equalsIgnoreCase("OGG")) {
    		return SoXTestsHelper.OGG_INPUT;
		}
    	
    	if (srcExtension.equalsIgnoreCase("RAW")) {
    		return SoXTestsHelper.RAW_INPUT;
		}
    	
    	if (srcExtension.equalsIgnoreCase("WAV")) {
    		return SoXTestsHelper.WAV_INPUT;
		}
    	return null;
    }
	
	
	private List<Parameter> createParameters(boolean showProgressFlag, boolean noShowProgressFlag, String verbosityLevelValue) {
    	List<Parameter> parameterList = new ArrayList<Parameter>();
    	
    	if(showProgressFlag==true) {
    		Parameter showProgress = new Parameter(
                    "showProgress",
                    "-S",
                    null,
                    "Display input file format/header information, and processing "
                            + "progress as input file(s) percentage complete, elapsed time, and remaining time "
                            + "(if known; shown in brackets), and the number of samples written to the output file. "
                            + "Also shown is a VU meter, and an indication if clipping has occurred.");
            parameterList.add(showProgress);
    	}
    	
        if(noShowProgressFlag==true) {
        	Parameter noShowProgress = new Parameter(
                    "noShowProgress",
                    "-q",
                    null,
                    "Run in quiet mode when SoX wouldn't otherwise do so; this is the opposite of the -S option.");
            parameterList.add(noShowProgress);
        }
        
        Parameter verbosityLevel = new Parameter(
                "verbosityLevel",
                verbosityLevelValue,
                null,
                "This should be an int value between: 0 - 4.\n"
                        + "0: No messages are shown at all; use the exit status to determine if an error has occurred.\n"
                        + "1: Only error messages are shown. These are generated if SoX cannot complete the requested commands.\n"
                        + "2: Warning messages are also shown. These are generated if SoX can complete the requested commands, but not exactly according to the requested command parameters, or if clipping occurs.\n"
                        + "3: Descriptions of SoX's processing phases are also shown. Useful for seeing exactly how SoX is processing your audio.\n"
                        + "4: and above: Messages to help with debugging SoX are also shown.\n"
                        + "By default, the verbosity level is set to 2; "
                        + "each occurrence of the -V option increases the verbosity level by 1. "
                        + "Alternatively, the verbosity level can be set to an absolute number by "
                        + "specifying it immediately after the -V; e.g. -V0 sets it to 0. ");
        parameterList.add(verbosityLevel);
        
		return parameterList;
    }
	

	
	

}
