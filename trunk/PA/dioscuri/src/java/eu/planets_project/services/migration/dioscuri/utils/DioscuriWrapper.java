package eu.planets_project.services.migration.dioscuri.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Checksum;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ImmutableContent;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.floppyImageHelper.FloppyImageHelperWin;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;


public class DioscuriWrapper {
	
	public DioscuriWrapper() {
		FileUtils.deleteTempFiles(WORK_TEMP_FOLDER);
		WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp(WORK_TEMP_NAME);
		FLOPPY_RESULT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, "EXTRACTED_FILES");
		log.info("Installed OS: " + OS_NAME + "\nVersion: " + OS_VERSION + "\nArchitecture: " + OS_ARCHITECTURE);
	}
	
	private PlanetsLogger log = PlanetsLogger.getLogger(this.getClass());
	
	private static String DIOSCURI_HOME = System.getenv("DIOSCURI_HOME");
	private static String OS_NAME = System.getProperty("os.name");
	private static String OS_VERSION = System.getProperty("os.version");
	private static String OS_ARCHITECTURE = System.getProperty("os.arch");
	private static String WORK_TEMP_NAME = "DIOSCURI_WRAPPER_TMP";
	private static File WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp(WORK_TEMP_NAME);
	private static String FLOPPY_RESULT_NAME = "EXTRACTED_FILES";
	private static File FLOPPY_RESULT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, FLOPPY_RESULT_NAME);
	
	private static String FLOPPY_NAME = "floppy.ima";
	
	private static String DIOSCURI_CONFIG_FILE_PATH = "PA/dioscuri/resources/DioscuriConfig.xml";
	
	private static FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
	
	private static String ERROR_OUT = null;
	private static String PROCESS_OUT = null;

	/**
	 * This method takes all files in the passed zip file, extracts them and creates a floppy image containing all these files.
	 * TO create the floppy image, the FloppyImageHelperWin service is used (at the moment). 
	 * <br/>
	 * <br/>ATTENTION: If you are running a Non-Windows machine, this service will not work.
	 * 
	 * @param allFilesAsZIP all files that should be written to a floppy image and passed to Dioscuri
	 * @param inputFileName the name of the file (which should be inside the passed ZIP) to specify which file to use as input file for the migration.
	 * @param outputFileName the name of the output file to be returned as result.
	 * @param checksum the checksum of the ZIP file, if there is one, if not, please pass "null".
	 * @return
	 */
	public DioscuriWrapperResult createFloppyImageAndRunDioscuri(File allFilesAsZIP, String inputFileName, String outputFileName, Checksum checksum) {
		if(DIOSCURI_HOME==null) {
			ERROR_OUT = 
					"Dioscuri is NOT properly installed on your system!\n" +
					"Please install it and point a Systemvariable called DIOSCURI_HOME to the installation folder!\n" +
					"Otherwise this service will continously refuse to work ;-)";
			DioscuriWrapperResult result = new DioscuriWrapperResult();
			result.setMessage(ERROR_OUT);
			result.setState(DioscuriWrapperResult.ERROR);
			return result;
		}
		else {
			log.info("DIOSCURI_HOME is set: " + DIOSCURI_HOME);
		}
		
		Migrate floppyHelper = checkOperatingSystemAndCreateService();
		
		if(floppyHelper==null) {
			return this.createErrorResult(ERROR_OUT);
		}
		
		Content content = null;
		
		if(checksum==null) {
			content = ImmutableContent.asStream(allFilesAsZIP);
		}
		else {
			content = ImmutableContent.asStream(allFilesAsZIP).withChecksum(checksum);
		}
		
		DigitalObject floppyInput = new DigitalObject.Builder(content)
										.title(allFilesAsZIP.getName())
										.format(format.createExtensionUri("ZIP"))
										.build();
		
		MigrateResult floppyHelperResult = floppyHelper.migrate(floppyInput, format.createExtensionUri("ZIP"), format.createExtensionUri("IMA"), null);
		
		if(floppyHelperResult.getReport().getErrorState()!= 0) {
			log.error(floppyHelperResult.getReport().getError());
			return createErrorResult(floppyHelperResult.getReport().getError());
		}
		
		log.info("FloppyImageHelperWin report: Successfull created floppy image!");
		
		DigitalObject floppyImageDigObj = floppyHelperResult.getDigitalObject();
		
		File floppyImage = new File(WORK_TEMP_FOLDER, FLOPPY_NAME) ;
			
		FileUtils.writeInputStreamToFile(floppyImageDigObj.getContent().read(), floppyImage);
		
		this.run(floppyImage);
		
		log.info("And...back again ;-)");
		
		return this.extractResultFileFromFloppyImage(floppyImage, outputFileName);
	}
	
	
	/**
	 * Extracts all files from a floppy image and gets the result file of the migration process carried out inside the emulator.
	 * <br/>This methods uses the FloppyImageHelperWin service again to extract files from a floppy image.
	 * <br/>ATTENTION: As this service requires Windows, this method will (for now) fail, if you are running a Non-Windows platform. 
	 * 
	 * @param floppyImage the floppy image to extract the result file from.
	 * @param outputFileName the name of the result file to pick it up between all other files which might be on this image.
	 * @return the DioscuriWrapperResult object containing the file specified by outputFileName
	 */
	private DioscuriWrapperResult extractResultFileFromFloppyImage(File floppyImage, String outputFileName) {
		Migrate extract = checkOperatingSystemAndCreateService();
		
		if(extract==null) {
			this.createErrorResult(ERROR_OUT);
		}
		
		DigitalObject floppy = new DigitalObject.Builder(ImmutableContent.asStream(floppyImage))
									.title(floppyImage.getName())
									.format(format.createExtensionUri(FileUtils.getExtensionFromFile(floppyImage)))
									.build();
		
		MigrateResult mr = extract.migrate(floppy, format.createExtensionUri(FileUtils.getExtensionFromFile(floppyImage)), format.createExtensionUri("ZIP"), null);
		
		File resultZIP = new File(WORK_TEMP_FOLDER, mr.getDigitalObject().getTitle());
		
		FileUtils.writeInputStreamToFile(mr.getDigitalObject().getContent().read(), resultZIP);
		
		Checksum check = mr.getDigitalObject().getContent().getChecksum();
		
		List<File> extractedFiles = FileUtils.extractFilesFromZipAndCheck(resultZIP, FLOPPY_RESULT_FOLDER, check);
		
		int index = extractedFiles.indexOf(new File(FLOPPY_RESULT_FOLDER, outputFileName));
		
		File main_result = null;
		
		if(index!=-1) {
			main_result = extractedFiles.get(index);
		}
		else {
			return this.createErrorResult("An unidentified error occured! No result file created!");
		}
		
		DioscuriWrapperResult dr = new DioscuriWrapperResult();
		dr.setMessage(PROCESS_OUT);
		dr.setResultFile(main_result);
		dr.setState(DioscuriWrapperResult.SUCCESS);
		return dr;
	}


	private Migrate checkOperatingSystemAndCreateService() {
		Migrate floppyHelper = null;
		if(OS_NAME.toLowerCase().contains("windows")) {
			floppyHelper = new FloppyImageHelperWin();
		}
		else {
			ERROR_OUT = "You are running this service on a Non-Windows machine. The used tool is not available for different platforms (at the moment).";
		}
		return floppyHelper;
	}
	
	/**
	 * This method runs Dioscuri while using the passed floppyImage.
	 * 
	 * @param floppyImage the floppyImage with all files on it needed by Dioscuri
	 */
	private void run(File floppyImage) {
		ProcessRunner dioscuriCmd = new ProcessRunner(); 
		
		dioscuriCmd.setCommand(getDioscuriCommandline(createConfigFile(floppyImage)));
		
		dioscuriCmd.setStartingDir(new File(DIOSCURI_HOME));
		
		dioscuriCmd.run();
	}


	/**
	 * Generates the configuration file needed by Dioscuri by adding the path to the floppy image that should be used by Dioscuri.
	 * Further modifications to the DioscuriConfig.xml can be made in "PA/dioscuri/resources/DioscuriConfig.xml", 
	 * <br/><strong>BUT</strong> be careful if you are not knowing exactly what you are doing!
	 * 
	 * @param floppyImage the floppyImage to be used, containing all files that should be passed to the emulator
	 * @return the configuration file
	 */
	private File createConfigFile(File floppyImage) {
		String configString = FileUtils.readTxtFileIntoString(new File(DIOSCURI_CONFIG_FILE_PATH));
		String floppyPath = floppyImage.getAbsolutePath();
		if(floppyPath.contains("\\")) {
			floppyPath = floppyPath.replace("\\", "/");
		}
		configString = configString.replace("INSERT_FLOPPY_PATH_HERE", floppyPath);
		File tmpConfigFile = new File(WORK_TEMP_FOLDER, "dioscuri_config.xml"); 
		FileUtils.writeStringToFile(configString, tmpConfigFile);
		return tmpConfigFile;
	}


	/**
	 * Creates the command line used by the ProcessRunner to run Dioscuri. 
	 * 
	 * @param configFile the configFile that should be used to configure Dioscuri.
	 * @return
	 */
	private ArrayList<String> getDioscuriCommandline(File configFile) {
		ArrayList<String> commands = new ArrayList<String> ();
		commands.add("java");
		commands.add("-jar");
		commands.add("dioscuri.jar");
		commands.add("-c");
		commands.add("\"" + configFile.getAbsolutePath()+ "\"");
		commands.add("-h");
		commands.add("autorun");
		commands.add("autoshutdown");
		
		return commands;
	}


	private DioscuriWrapperResult createErrorResult(String errorMessage) {
		DioscuriWrapperResult result = new DioscuriWrapperResult();
		result.setMessage(errorMessage);
		result.setState(DioscuriWrapperResult.ERROR);
		return result;
	}
	
	

}
