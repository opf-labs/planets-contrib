package eu.planets_project.services.migration.dioscuri;

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
		WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp("DIOSCURI_WRAPPER_TMP");
		FLOPPY_RESULT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, "EXTRACTED_FILES");
	}
	
	private PlanetsLogger log = PlanetsLogger.getLogger(this.getClass());
	
	private static String DIOSCURI_HOME = System.getenv("DIOSCURI_HOME");
	private static File WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp("DIOSCURI_WRAPPER_TMP");
	private static File FLOPPY_RESULT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, "EXTRACTED_FILES");
	
	private static String FLOPPY_NAME = "floppy.ima";
	
//	private static String EMU_PICTVIEW_PATH = "c:\\pictview\\pictview.exe";
	
	private static String DIOSCURI_CONFIG_FILE_PATH = "PA/dioscuri/resources/DioscuriConfig.xml";
	
//	private static String OUTFILE_NAME = "OUTPUT";
	
	private static FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
	
	private static String ERROR_OUT = null;
	private static String PROCESS_OUT = null;

	public DioscuriResult createFloppyImageAndRunDioscuri(File allFilesAsZIP, String inputFileName, String outputFileName, Checksum checksum) {
		
		Migrate floppyHelper = new FloppyImageHelperWin();
		
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
			return null;
		}
		
		log.info("FloppyImageHelperWin report: Successfull created floppy image!");
		
		DigitalObject floppyImageDigObj = floppyHelperResult.getDigitalObject();
		
		File floppyImage = new File(WORK_TEMP_FOLDER, FLOPPY_NAME) ;
			
		FileUtils.writeInputStreamToFile(floppyImageDigObj.getContent().read(), floppyImage);
		
		this.run(floppyImage);
		
		log.info("And...back again ;-)");
		
		File resultFile = this.extractResultFileFromFloppyImage(floppyImage, outputFileName);
		
		DioscuriResult dioscuriResult = new DioscuriResult();
		
		dioscuriResult.setResultFile(resultFile);
		
		if(resultFile!=null) {
			dioscuriResult.setState(DioscuriResult.SUCCESS);
			dioscuriResult.setMessage(PROCESS_OUT);
		}
		else {
			dioscuriResult.setState(DioscuriResult.ERROR);
			dioscuriResult.setMessage(ERROR_OUT);
		}
		
		return dioscuriResult;
	}
	
	private void run(File floppyImage) {
		ProcessRunner dioscuriCmd = new ProcessRunner(); 
		
		dioscuriCmd.setCommand(getDioscuriCommandline(createConfigFile(floppyImage)));
		
		dioscuriCmd.setStartingDir(new File(DIOSCURI_HOME));
		
		dioscuriCmd.run();
	}

	private File extractResultFileFromFloppyImage(File floppyImage, String outputFileName) {
		Migrate extract = new FloppyImageHelperWin();
		
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
			ERROR_OUT = "An unidentified error occured! No result file created! ";
		}
		return main_result;
	}

	public File createConfigFile(File floppyImage) {
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

	public ArrayList<String> getDioscuriCommandline(File configFile) {
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
	
	

}
