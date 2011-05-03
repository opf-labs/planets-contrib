package eu.planets_project.services.migration.dioscuri.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.annotation.ejb.TransactionTimeout;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Checksum;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.DigitalObjectContent;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.floppyImageHelper.api.FloppyImageHelper;
import eu.planets_project.services.migration.floppyImageHelper.api.FloppyImageHelperFactory;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.ProcessRunner;
import eu.planets_project.services.utils.ZipUtils;

@TransactionTimeout(6000)
public class DioscuriWrapper {
	private static Logger log = Logger.getLogger(DioscuriWrapper.class.getName());
	public DioscuriWrapper() {
		try {
            FileUtils.deleteDirectory(WORK_TEMP_FOLDER);
            File systemTemp = new File(System.getProperty("java.io.tmpdir"));
            WORK_TEMP_FOLDER = new File(systemTemp, WORK_TEMP_NAME);
            FileUtils.forceMkdir(WORK_TEMP_FOLDER);
            String extractedFilesFolder = randomize("EXTRACTED_FILES");
            FLOPPY_RESULT_FOLDER = new File(WORK_TEMP_FOLDER, extractedFilesFolder);
            FileUtils.forceMkdir(FLOPPY_RESULT_FOLDER);
            log.info("Installed OS: " + OS_NAME + ", Version: " + OS_VERSION + ", Architecture: " + OS_ARCHITECTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    public static String randomize(String name) {
        File f = null;
        try {
            f = File.createTempFile(name, "." + FilenameUtils.getExtension(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = f != null ? f.getName() : null;
        FileUtils.deleteQuietly(f);
        return result;
    }
    
    public static File truncateNameAndRenameFile(final File file) {
        String newName = file.getName();
        String parent = file.getParent();
        String ext = "";
        if (newName.contains(".")) {
            ext = newName.substring(newName.lastIndexOf("."));
            newName = newName.substring(0, newName.lastIndexOf("."));
        }
        if (newName.length() > 8) {
            newName = newName.substring(0, 8);
            log.info("File name longer than 8 chars. Truncated file name to: "
                    + newName
                    + " to avoid problems with long file names in DOS!");
            
            newName = newName + ext;
            File renamedFile = new File(new File(parent), newName);
            boolean renamed = file.renameTo(renamedFile);
            if (!renamed) {
//                throw new IllegalArgumentException("Could not rename: " + file);
            }
            return renamedFile;
        }
        else {
            return file;
        }
        
    }
    
    private String DIOSCURI_HOME = System.getenv("DIOSCURI_HOME");
//	private String DIOSCURI_HOME = "D:/PLANETS/DIOSCURI_HOME"; // TESTING
	
	private String OS_NAME = System.getProperty("os.name");
	private String OS_VERSION = System.getProperty("os.version");
	private String OS_ARCHITECTURE = System.getProperty("os.arch");
	private String WORK_TEMP_NAME = "DIOSCURI_WRAPPER_TMP";
	private File WORK_TEMP_FOLDER = null;
	private String sessionID = randomize("dioscuri");
//	private String FLOPPY_RESULT_NAME = "EXTRACTED_FILES" + sessionID;
	private File FLOPPY_RESULT_FOLDER = null;
	
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
			log.severe(ERROR_OUT);
			return result;
		}
		else {
			log.info("DIOSCURI_HOME is set: " + DIOSCURI_HOME);
		}
		
		FloppyImageHelper floppyHelper = FloppyImageHelperFactory.getFloppyImageHelperInstance();
		
		if(floppyHelper==null) {
			return this.createErrorResult(ERROR_OUT);
		}
		
		DigitalObject floppyInput = DigitalObjectUtils.createZipTypeDigitalObject(allFilesAsZIP, allFilesAsZIP.getName(), true, true, false);
		
		MigrateResult floppyHelperResult = floppyHelper.migrate(floppyInput, format.createExtensionUri("ZIP"), format.createExtensionUri("IMA"), null);
		
		if(floppyHelperResult.getReport().getStatus() != Status.SUCCESS ) {
			log.severe(floppyHelperResult.getReport().getMessage());
			return createErrorResult(floppyHelperResult.getReport().getMessage());
		}
		
		log.info("FloppyHelperService report: Successfull created floppy image!");
		
		DigitalObject floppyImageDigObj = floppyHelperResult.getDigitalObject();
		
		File floppyImage = new File(WORK_TEMP_FOLDER, randomize(FLOPPY_NAME)) ;
			
		DigitalObjectUtils.toFile(floppyImageDigObj, floppyImage);
		
		try {
            this.run(floppyImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
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
		
		DigitalObject floppy = new DigitalObject.Builder(Content.byReference(floppyImage))
									.title(floppyImage.getName())
									.format(format.createExtensionUri(FilenameUtils.getExtension(floppyImage.getName())))
									.build();
		
		MigrateResult mr = extract.migrate(floppy, format.createExtensionUri(FilenameUtils.getExtension(floppyImage.getName())), format.createExtensionUri("ZIP"), null);
		
		if(mr.getReport().getType() == Type.ERROR) {
			log.severe("No Result received from FloppyImageHelperWin. Returning with ERROR: " + mr.getReport().getMessage());
			return this.createErrorResult("No Result received from FloppyImageHelperWin. Returning with ERROR: " + mr.getReport().getMessage());
		}
		
		String resultName = mr.getDigitalObject().getTitle();
		
		if(resultName==null) {
			resultName = "FIH_result" + sessionID + ".zip";
		}
		
		File resultZIP = new File(WORK_TEMP_FOLDER, resultName);
		
		DigitalObjectContent resultContent = mr.getDigitalObject().getContent();
		
		if(resultContent==null) {
			log.severe("There is no result file! Returning with ERROR! ");
			return this.createErrorResult("There is no result file! Returning with ERROR! ");
		}
		
		DigitalObjectUtils.toFile(mr.getDigitalObject(), resultZIP);
		
		Checksum check = resultContent.getChecksum();
		
		List<File> extractedFiles = null;
		
		if(check==null) {
			extractedFiles = ZipUtils.unzipTo(resultZIP, FLOPPY_RESULT_FOLDER);
		}
		else {
			extractedFiles = ZipUtils.checkAndUnzipTo(resultZIP, FLOPPY_RESULT_FOLDER, check);
		}
		
		File outFile = new File(FLOPPY_RESULT_FOLDER, outputFileName);
        log.fine("Looking for file: "+outFile.getAbsolutePath());
        
		for( File exfile: extractedFiles) {
		    log.fine("Found file: "+exfile.getAbsolutePath());
		}
		
		int index = extractedFiles.indexOf(outFile);
		
		File main_result = null;
		
		if(index!=-1) {
			main_result = extractedFiles.get(index);
		}
		else {
			return this.createErrorResult("Result file could not be found. An unidentified error occured! No result file created! "+PROCESS_OUT+" : "+ERROR_OUT);
		}
		
		DioscuriWrapperResult dr = new DioscuriWrapperResult();
		dr.setMessage(PROCESS_OUT);
		dr.setResultFile(main_result);
		dr.setState(DioscuriWrapperResult.SUCCESS);
		log.info("Dioscuri executed just fine. Returning SUCCESS");
		return dr;
	}


	/**
	 * This method runs Dioscuri while using the passed floppyImage.
	 * 
	 * @param floppyImage the floppyImage with all files on it needed by Dioscuri
	 * @throws IOException 
	 */
	private void run(File floppyImage) throws IOException {
		ProcessRunner dioscuriCmd = new ProcessRunner(); 
		
		File config = createConfigFile(floppyImage);
		
		dioscuriCmd.setCommand(getDioscuriCommandline(config));
		
		dioscuriCmd.setStartingDir(new File(DIOSCURI_HOME));
		
		log.info("ATTENTION: Running DIOSCURI Emulator...this might take a while, so please be patient and hang on ;-)");
		
		dioscuriCmd.run();
		
		PROCESS_OUT = dioscuriCmd.getProcessOutputAsString();
		ERROR_OUT = dioscuriCmd.getProcessErrorAsString();
        log.info("Got Process output: "+PROCESS_OUT);
        if( ERROR_OUT != null && !"".equals(ERROR_OUT) )
            log.severe("Got Process error output: "+ERROR_OUT);
	}


	/**
	 * Generates the configuration file needed by Dioscuri by adding the path to the floppy image that should be used by Dioscuri.
	 * Further modifications to the DioscuriConfig.xml can be made in "PA/dioscuri/resources/DioscuriConfig.xml", 
	 * <br/><strong>BUT</strong> be careful if you are not knowing exactly what you are doing!
	 * 
	 * @param floppyImage the floppyImage to be used, containing all files that should be passed to the emulator
	 * @return the configuration file
	 * @throws IOException 
	 */
	private File createConfigFile(File floppyImage) throws IOException {
		log.info("Looking up DioscuriConfig.xml template to create config file...");
		StringWriter writer = new StringWriter();
		IOUtils.copy(this.getClass().getResourceAsStream(DIOSCURI_CONFIG_FILE_PATH), writer);
		String configString = writer.toString();
		if(configString!=null) {
			log.info("Success! DioscuriConfig.xml Template found and adjusted.");
		}
		else {
			log.severe("ERROR: Config file-template not found, unable to run Dioscuri without that!");
			ERROR_OUT = "ERROR: Config file-template not found, unable to run Dioscuri without that!";
			return null;
		}
		String floppyPath = floppyImage.getAbsolutePath();
		if(floppyPath.contains("\\")) {
			floppyPath = floppyPath.replace("\\", "/");
		}
		configString = configString.replace("INSERT_FLOPPY_PATH_HERE", floppyPath);
		File tmpConfigFile = new File(WORK_TEMP_FOLDER, randomize("dioscuri_config.xml")); 
		FileUtils.writeStringToFile(tmpConfigFile, configString);
		log.info("Created config file for Dioscuri: " + tmpConfigFile.getAbsolutePath());
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
		commands.add(configFile.getAbsolutePath());
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
