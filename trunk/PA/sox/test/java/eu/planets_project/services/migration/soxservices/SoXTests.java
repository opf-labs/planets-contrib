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
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.soxservices.utils.SoXHelper;
import eu.planets_project.services.utils.ServiceUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

public class SoXTests {
	
	static Migrate sox;
	
    /* The location of this service when deployed. */
	static String wsdlLocation = "/pserv-pa-sox/SoX?wsdl";
	
	/**
	 * test output
	 */
	public static String TEST_OUT = null;
	
	public static MigrationPath[] migrationPaths;
	
	private static final String br = System.getProperty("line.separator");
	
	
	private static final String java_version = System.getProperty("java.version");
	
	private static URI mp3_URI = URI.create("planets:fmt/ext/mp3");
	private static URI raw_URI = URI.create("planets:fmt/ext/raw");
	private static URI wav_URI = URI.create("planets:fmt/ext/wav");
	private static URI aiff_URI = URI.create("planets:fmt/ext/aiff");
	private static URI flac_URI = URI.create("planets:fmt/ext/flac");
	private static URI ogg_URI = URI.create("planets:fmt/ext/ogg");
	
	private static List<URI> supportedInputFormats = SoXHelper.getSupportedInputFormats();
	private static List<URI> supportedOutputFormats = SoXHelper.getSupportedOutputFormats();
	
	@BeforeClass
    public static void setup() {
		TEST_OUT = SoXTestsHelper.SoX_LOCAL_OUT;
		
    	printTestTitle("Running SoX tests...on " + java_version);
    	printTestTitle("Path: " + PATH);
    	
    	sox = ServiceCreator.createTestService(Migrate.QNAME, SoX.class, wsdlLocation);
    	
    	List<URI> inputFormats = new ArrayList<URI>();
    	if(supportedInputFormats.contains(mp3_URI)) {
    		inputFormats.add(mp3_URI);
    	}
    	if(supportedInputFormats.contains(raw_URI)) {
    		inputFormats.add(raw_URI);
    	}
    	inputFormats.add(wav_URI);
    	inputFormats.add(aiff_URI);
    	inputFormats.add(flac_URI);
//    	inputFormats.add(ogg_URI);
    	
    	List<URI> outputFormats = new ArrayList<URI>();
    	if(supportedOutputFormats.contains(mp3_URI)) {
    		outputFormats.add(mp3_URI);
    	}
    	outputFormats.add(wav_URI);
    	outputFormats.add(aiff_URI);
    	outputFormats.add(flac_URI);
//    	outputFormats.add(ogg_URI);
//    	outputFormats.add(raw_URI);
    	
    	migrationPaths = ServiceUtils.createMigrationPathways(inputFormats, outputFormats);
    }

	@Test
	public void testDescribe() {
		ServiceDescription desc = sox.describe();
        System.out.println("Recieved service description: " + desc.toXmlFormatted());
        assertTrue("The ServiceDescription should not be NULL.", desc != null );
	}
	
	@Test
	public void testUnsupportedFormats() throws URISyntaxException, InterruptedException {
		printTestTitle("testing unsupported Format behaviour. Please note: Test SHOULD FAIL!");
		
		if(!supportedInputFormats.contains(mp3_URI)) {
			System.out.println("Testing unsupported INPUT format: " + mp3_URI.toASCIIString() + "...service should fail!");
			testMigrate(mp3_URI, wav_URI, null);
//			wait(2500000);
		}
		else {
			System.err.println("MP3 input-format is NOT unsupported: Skipping test for UN-supported Input-format!");
		}
		
		if(!supportedOutputFormats.contains(mp3_URI)) {
			System.out.println("Testing unsupported OUTPUT format: " + mp3_URI.toASCIIString() + "...service should fail!");
			testMigrate(wav_URI, mp3_URI, null);
//			wait(2500000);
		}
		else {
			System.err.println("MP3 output-format is NOT unsupported: Skipping test for UN-supported Output-format!");
		}
		
		if(!supportedInputFormats.contains(raw_URI) && !supportedOutputFormats.contains(mp3_URI)) {
			System.out.println("Testing unsupported migration path: [" 
							+ raw_URI.toASCIIString() 
							+ " --> " + mp3_URI.toASCIIString() + "]...service should fail!");
			testMigrate(raw_URI, mp3_URI, null);
		}
		else {
			System.err.println("Migrationpath: Raw --> MP3 is NOT unsupported: Skipping test for UN-supported MigrationPath!");
		}
		
	}
	
	@Test
	public void testAdvencedCLI() {
		printTestTitle("Testing AdvancedCLI technologie ;-)");
		
		printTestTitle("AdvancedCLI: wav --> aiff, #INFILE# notation");
		List<Parameter> parameters = createParameters("-S -V6 #INFILE# -c2 -r44100 #OUTFILE#", false, false, null); 
		testMigrate(wav_URI, aiff_URI, parameters);
		
		printTestTitle("AdvancedCLI: aiff --> wav, 'infile' notation");
		parameters = createParameters("-S -V6 infile -c2 -r22500 outfile", false, false, null);
		testMigrate(aiff_URI, wav_URI, parameters);
	}
	
	@Test
	public void testAllPossibleMigrationPathways() {
		printTestTitle("testing all supported Formats.");
		
		for(int i=0;i<migrationPaths.length;i++) {
			MigrationPath path = migrationPaths[i];
			URI inputFormat = path.getInputFormat();
			URI outputFormat = path.getOutputFormat();
			
			printTestTitle("Testing migrationPath: [" + inputFormat.toASCIIString() + " --> " + outputFormat.toASCIIString() + "]");
			
			printTestTitle("ShowProgress = TRUE, Verbosity level: 6");
			List<Parameter> parameters = createParameters(null, true, false, "6");
			testMigrate(inputFormat, outputFormat, parameters);
			
//			printTestTitle("ShowProgress = TRUE, Verbosity level: 4");
//			System.out.println("------------------");
//			parameters = createParameters(true, false, "4");
//			testMigrate(inputFormat, outputFormat, parameters);
//			System.out.println();
			
//			printTestTitle("ShowProgress = FALSE, Verbosity level: 4");
//			parameters = createParameters(false, false, "4");
//			testMigrate(inputFormat, outputFormat, parameters);
//			System.out.println();
			
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
			System.err.println("FAILED: " + sr.getStatus());
//			assertTrue("This test SHOULD fail!", sr.getType() == Type.ERROR);
//			System.err.println("Service successfully failed ;-)");
		}
		else {
			System.out.println("Got Report: " + sr.getStatus());
        
			DigitalObject doOut = mr.getDigitalObject();

			assertTrue("Resulting digital object is null.", doOut != null);

			System.out.println("Result: " + doOut);
			System.out.println("Result.content: " + doOut.getContent());
		}
	}
	
	
	
	
	private DigitalObject createDigitalObject(String srcExtension) {
		
		File inputFile = getTestFile(srcExtension);
    	
        DigitalObject input = null;
        
        input = new DigitalObject.Builder(Content.byValue(inputFile)).permanentUri(URI.create("http://soxMigrationsTest.eu")).build();
        
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
	
	
	private List<Parameter> createParameters(String advancedCmdLine, boolean showProgressFlag, boolean noShowProgressFlag, String verbosityLevelValue) {
    	List<Parameter> parameterList = new ArrayList<Parameter>();
    	
    	if(advancedCmdLine!=null && !advancedCmdLine.equalsIgnoreCase("")) {
    		Parameter advancedCLI = new Parameter.Builder("advancedCmd", advancedCmdLine).build();
    		parameterList.add(advancedCLI);
    	}
    	
    	if(showProgressFlag==true) {
    		Parameter showProgress = new Parameter.Builder(
                    "showProgress",
                    "-S").description(
                    "Display input file format/header information, and processing "
                            + "progress as input file(s) percentage complete, elapsed time, and remaining time "
                            + "(if known; shown in brackets), and the number of samples written to the output file. "
                            + "Also shown is a VU meter, and an indication if clipping has occurred.").build();
            parameterList.add(showProgress);
    	}
    	
        if(noShowProgressFlag==true) {
        	Parameter noShowProgress = new Parameter.Builder("noShowProgress",
                    "-q")
                    .description(
                            "Run in quiet mode when SoX wouldn't otherwise do so; this is the opposite of the -S option.")
                    .build();
            parameterList.add(noShowProgress);
        }
        
        if(verbosityLevelValue!=null) {
	        Parameter verbosityLevel = new Parameter.Builder("verbosityLevel",
	        verbosityLevelValue)
	        .description(
	                "Increment or set verbosity level (default 2); levels:" + br + 
	                "1: failure messages" + br + 
	                "2: warnings" + br + 
	                "3: details of processing" + br + 
	                "4-6: increasing levels of debug messages")
	        .build();
	        parameterList.add(verbosityLevel);
        }
        
		return parameterList;
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
	

	
	

}
