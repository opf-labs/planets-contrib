package eu.planets_project.services.migration.dioscuri.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.annotation.ejb.TransactionTimeout;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Checksum;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.DigitalObjectContent;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.floppyImageHelper.api.FloppyImageHelper;
import eu.planets_project.services.migration.floppyImageHelper.api.FloppyImageHelperFactory;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;
import eu.planets_project.services.utils.ZipUtils;


@TransactionTimeout(6000)
public class DioscuriWrapper {
	
	public DioscuriWrapper() {
		FileUtils.deleteTempFiles(WORK_TEMP_FOLDER);
		WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp(WORK_TEMP_NAME);
		FLOPPY_RESULT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, FileUtils.randomizeFileName("EXTRACTED_FILES"));
		log.info("Installed OS: " + OS_NAME + ", Version: " + OS_VERSION + ", Architecture: " + OS_ARCHITECTURE);
	}
	
	private PlanetsLogger log = PlanetsLogger.getLogger(this.getClass());
	
	private String DIOSCURI_HOME = System.getenv("DIOSCURI_HOME");
//	private String DIOSCURI_HOME = "D:/PLANETS/DIOSCURI_HOME"; // TESTING
	
	private String OS_NAME = System.getProperty("os.name");
	private String OS_VERSION = System.getProperty("os.version");
	private String OS_ARCHITECTURE = System.getProperty("os.arch");
	private String WORK_TEMP_NAME = "DIOSCURI_WRAPPER_TMP";
	private File WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp(WORK_TEMP_NAME);
	private String sessionID = FileUtils.randomizeFileName("");
	private String FLOPPY_RESULT_NAME = "EXTRACTED_FILES" + sessionID;
	private File FLOPPY_RESULT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, FLOPPY_RESULT_NAME);
	
	private String FLOPPY_NAME = "floppy" + sessionID + ".ima";
	
	private String DIOSCURI_CONFIG_FILE_PATH = "DioscuriConfig.xml";
	
	private static FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
	
	private String ERROR_OUT = null;
	private String PROCESS_OUT = null;

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
					"Please install it and point a System variable called DIOSCURI_HOME to the installation folder!\n" +
					"Otherwise this service will continously refuse to work ;-)";
			DioscuriWrapperResult result = new DioscuriWrapperResult();
			result.setMessage(ERROR_OUT);
			result.setState(DioscuriWrapperResult.ERROR);
			log.error(ERROR_OUT);
			return result;
		}
		else {
			log.info("DIOSCURI_HOME is set: " + DIOSCURI_HOME);
		}
		
		FloppyImageHelper floppyHelper = FloppyImageHelperFactory.getFloppyImageHelperInstance();
		
		if(floppyHelper==null) {
			return this.createErrorResult(ERROR_OUT);
		}
		
//		DigitalObjectContent content = null;
//		
//		if(checksum==null) {
//			content = Content.byReference(allFilesAsZIP);
//		}
//		else {
//			content = Content.byReference(allFilesAsZIP).withChecksum(checksum);
//		}
		
//		DigitalObject floppyInput = new DigitalObject.Builder(content)
//										.title(allFilesAsZIP.getName())
//										.format(format.createExtensionUri("ZIP"))
//										.build();
		
		DigitalObject floppyInput = DigitalObjectUtils.createZipTypeDigOb(allFilesAsZIP, allFilesAsZIP.getName(), true, true);
		
		MigrateResult floppyHelperResult = floppyHelper.migrate(floppyInput, format.createExtensionUri("ZIP"), format.createExtensionUri("IMA"), null);
		
		if(floppyHelperResult.getReport().getType() == Type.ERROR) {
			log.error(floppyHelperResult.getReport().getMessage());
			return createErrorResult(floppyHelperResult.getReport().getMessage());
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
		FloppyImageHelper extract = FloppyImageHelperFactory.getFloppyImageHelperInstance();
		
		if(extract==null) {
			return this.createErrorResult(ERROR_OUT);
		}
		
		log.info("FloppyImageHelper instance created: " + extract.getClass().getName());
		DigitalObject floppy = new DigitalObject.Builder(Content.byReference(floppyImage))
									.title(floppyImage.getName())
									.format(format.createExtensionUri(FileUtils.getExtensionFromFile(floppyImage)))
									.build();
		
		MigrateResult mr = extract.migrate(floppy, format.createExtensionUri(FileUtils.getExtensionFromFile(floppyImage)), format.createExtensionUri("ZIP"), null);
		
		if(mr.getReport().getType() == Type.ERROR) {
			log.error("No Result received from FloppyImageHelperWin. Returning with ERROR: " + mr.getReport().getMessage());
			return this.createErrorResult("No Result received from FloppyImageHelperWin. Returning with ERROR: " + mr.getReport().getMessage());
		}
		
		String resultName = mr.getDigitalObject().getTitle();
		
		if(resultName==null) {
			resultName = "FIH_result" + sessionID + ".zip";
		}
		
		File resultZIP = new File(WORK_TEMP_FOLDER, resultName);
		
		DigitalObjectContent resultContent = mr.getDigitalObject().getContent();
		
		if(resultContent==null) {
			log.error("There is no result file! Returning with ERROR! ");
			return this.createErrorResult("There is no result file! Returning with ERROR! ");
		}
		
		FileUtils.writeInputStreamToFile(resultContent.read(), resultZIP);
		
		Checksum check = resultContent.getChecksum();
		
		List<File> extractedFiles = null;
		
		if(check==null) {
			extractedFiles = ZipUtils.unzipTo(resultZIP, FLOPPY_RESULT_FOLDER);
		}
		else {
			extractedFiles = ZipUtils.checkAndUnzipTo(resultZIP, FLOPPY_RESULT_FOLDER, check);
		}
		
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


	/**
	 * This method runs Dioscuri while using the passed floppyImage.
	 * 
	 * @param floppyImage the floppyImage with all files on it needed by Dioscuri
	 */
	private void run(File floppyImage) {
		ProcessRunner dioscuriCmd = new ProcessRunner(); 
		
		File config = createConfigFile(floppyImage);
		
		dioscuriCmd.setCommand(getDioscuriCommandline(config));
		
		dioscuriCmd.setStartingDir(new File(DIOSCURI_HOME));
		
		log.info("ATTENTION: Running DIOSCURI Emulator...this might take a while, so please be patient and hang on ;-)");
		
		dioscuriCmd.run();
		
		PROCESS_OUT = dioscuriCmd.getProcessOutputAsString();
		ERROR_OUT = dioscuriCmd.getProcessErrorAsString();
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
		log.info("Looking up DioscuriConfig.xml template to create config file...");
		String configString = new String(FileUtils.writeInputStreamToBinary(this.getClass().getResourceAsStream(DIOSCURI_CONFIG_FILE_PATH)));
		if(configString!=null) {
			log.info("Success! Template found.");
		}
		else {
			log.error("ERROR: Config file-template not found, unable to run Dioscuri without that!");
			ERROR_OUT = "ERROR: Config file-template not found, unable to run Dioscuri without that!";
			return null;
		}
		String floppyPath = floppyImage.getAbsolutePath();
		if(floppyPath.contains("\\")) {
			floppyPath = floppyPath.replace("\\", "/");
		}
		configString = configString.replace("INSERT_FLOPPY_PATH_HERE", floppyPath);
		File tmpConfigFile = new File(WORK_TEMP_FOLDER, "dioscuri_config.xml"); 
		FileUtils.writeStringToFile(configString, tmpConfigFile);
		log.info("Created config file for Dioscuri");
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
