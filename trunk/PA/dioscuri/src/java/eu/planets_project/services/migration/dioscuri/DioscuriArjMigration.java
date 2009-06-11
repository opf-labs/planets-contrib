/**
 * 
 */
package eu.planets_project.services.migration.dioscuri;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.dioscuri.utils.DioscuriWrapper;
import eu.planets_project.services.migration.dioscuri.utils.DioscuriWrapperResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ServiceUtils;
import eu.planets_project.services.utils.ZipResult;
import eu.planets_project.services.utils.ZipUtils;

/**
 * @author melmsp
 *
 */

@Stateless()

@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")

@WebService(
        name = DioscuriArjMigration.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class DioscuriArjMigration implements Migrate, Serializable {
	
	private PlanetsLogger log = PlanetsLogger.getLogger(this.getClass()); 
	private static final long serialVersionUID = 7520484154909390134L;

	public static final String NAME = "DioscuriArjMigration";
	private static File WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp("Dioscuri_ArjMigration_TMP".toUpperCase());
//	private static File FLOPPY_INPUT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, "FLOPPY_INPUT");
	private static String FLOPPY_INPUT_FOLDER_NAME = null;
	private static File FLOPPY_INPUT_FOLDER = null;
	private static String DEFAULT_INPUT_NAME = null;
	private String sessionID = FileUtils.randomizeFileName("");
	
	private static String RUN_BAT = "RUN.BAT";
//	private static String INPUT_ZIP_NAME = "input.zip";
	private static String INPUT_ZIP_NAME = null;
	
	private static String EMU_ARJ_PATH= "C:\\ARJ\\ARJ.EXE";
	private static String INDEX_FILE_NAME = "INDEX.TXT";
	
	private static FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
	
	
	public DioscuriArjMigration() {
		FileUtils.deleteTempFiles(WORK_TEMP_FOLDER);
	}
	

	/* (non-Javadoc)
	 * @see eu.planets_project.services.PlanetsService#describe()
	 */
	public ServiceDescription describe() {
		ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME, Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("This is a Prototype wrapper for the DIOSCURI-Emulator developed at KB-NL.\r\n" +
        		"It converts \"ARJ\" archives to selfextracting \"EXE\" files using \"ARJ.EXE\" v2.21, running in a MS-DOS 5.0 environment\r\n" +
        		"emulated by DIOSCURI (16-Bit mode).");
        sd.classname(this.getClass().getCanonicalName());
        sd.version("1.0");
        sd.tool( Tool.create(null, 
        		"ARJ (MS-DOS)", 
        		"2.21", 
        		null, 
        		"http://wiki.oldos.org/Downloads/MSDOS"));
        List<MigrationPath> pathways = new ArrayList<MigrationPath>();
        pathways.add(new MigrationPath(format.createExtensionUri("ARJ"), format.createExtensionUri("EXE"), null));
        
        sd.paths(pathways.toArray(new MigrationPath[] {}));
        return sd.build();
	}


	/* (non-Javadoc)
	 * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameters)
	 */
	public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
			URI outputFormat, List<Parameter> parameters) {
		if(WORK_TEMP_FOLDER.exists()) {
			boolean deletedAllFiles = FileUtils.deleteAllFilesInFolder(WORK_TEMP_FOLDER);
			log.info("Deleted all files in: " + WORK_TEMP_FOLDER.getAbsolutePath() + ": " + String.valueOf(deletedAllFiles).toUpperCase());
		}
		
		INPUT_ZIP_NAME = FileUtils.randomizeFileName("dioscuri-arj-migrate.zip");
		
		DEFAULT_INPUT_NAME = "input" + sessionID;
		
		FLOPPY_INPUT_FOLDER_NAME = "FLOPPY_INPUT" + sessionID;
		
		FLOPPY_INPUT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, FLOPPY_INPUT_FOLDER_NAME);
		
		File inputFile = getInputFileFromDigitalObject(digitalObject, inputFormat, FLOPPY_INPUT_FOLDER);
		String inFileName = inputFile.getName();
		String outFileName = getOutputFileName(inputFile, outputFormat);
//		String outFileName = INDEX_FILE_NAME;
		
		boolean runBatCreated = createArchiveConversionRunBat(FLOPPY_INPUT_FOLDER, inputFile, outFileName);	
//		boolean runBatCreated = createListOfContentRunBat(FLOPPY_INPUT_FOLDER, inputFile, INDEX_FILE_NAME);
		
		DioscuriWrapper dioscuri = new DioscuriWrapper();
		
		if(runBatCreated) {
//			ZipResult zipResult = FileUtils.createZipFileWithChecksum(FLOPPY_INPUT_FOLDER, WORK_TEMP_FOLDER, INPUT_ZIP_NAME);
			ZipResult zipResult = ZipUtils.createZipAndCheck(FLOPPY_INPUT_FOLDER, WORK_TEMP_FOLDER, INPUT_ZIP_NAME);
			DioscuriWrapperResult result = dioscuri.createFloppyImageAndRunDioscuri(zipResult.getZipFile(), inFileName, outFileName, zipResult.getChecksum());
			if(result.getState()==DioscuriWrapperResult.ERROR) {
				return this.returnWithErrorMessage(result.getMessage(), null);
			}
			else {
				return createMigrateResult(result.getResultFile(), inputFormat, outputFormat);
			}
		}
		else {
			return this.returnWithErrorMessage("ERROR when trying to write \"RUN.BAT\". Returning with error, nothing has been done, sorry!", null);
		}
	}
	
	
	/**
	 * This method gets the Content from a DigitalObject and writes it to a File in the specified destFolder. 
	 * 
	 * @param digObj the digitalObject to get the Content from and write it to a File in destFolder.
	 *               <br/>If no name can be retrieved from the digitalObject, an DEFAULT_NAME will be used
	 *               <br/>All file names will be truncated to 8 characters length + extension.
	 *               
	 * @param inputFormat the inputformat to allow proper creation of a file (with name and extension) even if the "format" field in the DigObj is not set!
	 * @param destFolder the folder to write the created file to.
	 * @return the created file
	 */
	private File getInputFileFromDigitalObject(DigitalObject digObj, URI inputFormat, File destFolder) {
		String inName = getFileNameFromDigitalObject(digObj, inputFormat);
		File in = new File(destFolder, inName);
		FileUtils.writeInputStreamToFile(digObj.getContent().read(), in);
		in = FileUtils.truncateNameAndRenameFile(in);
		return in;
	}
	
	
	/**
	 * This methods gets the "title" from a DigitalObject <strong>or</strong> uses a DEFAULT_NAME if this field is not set.
	 *  
	 * @param digObj the digitalObject containing to get the name of the content from
	 * @param inputFormat the inputformat to allow proper creation of a file (with name and extension) even if the "format" field in the DigObj is not set!
	 * @return the name of the file inside the DigObj as a String
	 */
	private String getFileNameFromDigitalObject(DigitalObject digObj, URI inputFormat) {
		String fileName = digObj.getTitle();
		if(fileName==null) {
			String ext = "." + format.getFirstExtension(inputFormat);
			fileName = DEFAULT_INPUT_NAME + ext;
		}
		return fileName;
	}
	
	
	/**
	 * This methods creates the name of the output file based on the input file's name, but with an extension matching the targeted output format.
	 * 
	 * @param inputFile the input file based on which the output file's name will be created (<strong>((inputfile name - extension) + new extension)</strong> ;-)
	 * @param outputFormat the outputFormat to get the proper extension
	 * @return the name of the output file as String
	 */
	private String getOutputFileName(File inputFile, URI outputFormat) {
		String inName = inputFile.getName();
		String outName = null;
		String outExt = "." + format.getFirstExtension(outputFormat);
		if(inName.contains(".")) {
			outName = inName.substring(0, inName.lastIndexOf(".")) + outExt;
		}
		else {
			outName = inName + outExt;
		}
		return outName;
		
	}
	
		/**
		 * This method creates a "RUN.BAT" script needed by Dioscuri to execute a certain application.<br/>
		 * This RUN.BAT will be placed on the floppy image needed for Dioscuri, along with (the/all other) input file(s)
		 * 
		 * @param destFolder the folder where the "RUN.BAT" should be created
		 * @param inputFileName the name of the input file for this migration
		 * @param outputFileName the name of the output file for this migration
		 * @param inputExt 
		 * @return
		 */
		private boolean createArchiveConversionRunBat(File destFolder, File inputFile, String outputFileName) {
			String runScript = null;
			String inputFileName = inputFile.getName();
			String inputExt = FileUtils.getExtensionFromFile(inputFile);
			if(inputExt==null) {
				log.warn("Could not retrieve format extension from input file. Using '.arj' as default.");
			}
			if(inputExt.equalsIgnoreCase("ARJ")) {
				runScript = EMU_ARJ_PATH + " " + "f " + "A:\\" + inputFileName.toUpperCase() + " -je" + 
				"\r\n" + "ECHO Finished archive conversion to selfextracting ARJ archive. Bye Bye..." +
				"\r\n" + "HALT.EXE";
			}
//			else {
//				runScript = EMU_ARJ_PATH + " " + "f " + "A:\\" + inputFileName.toUpperCase() + "-je" + 
//					"\r\n" + "HALT.EXE";
//			}
			
			File runBat = new File(destFolder, RUN_BAT);
			runBat = FileUtils.writeStringToFile(runScript, runBat);
			log.info(RUN_BAT + " created: " + runScript);
			
			return runBat.exists();
		}
		
		/**
		 * This method creates a "RUN.BAT" script needed by Dioscuri to execute a certain application.<br/>
		 * This RUN.BAT will be placed on the floppy image needed for Dioscuri, along with (the/all other) input file(s)
		 * 
		 * @param destFolder the folder where the "RUN.BAT" should be created
		 * @param inputFileName the name of the input file for this migration
		 * @param outputFileName the name of the output file for this migration
		 * @param inputExt 
		 * @return
		 */
		private boolean createListOfContentRunBat(File destFolder, File inputFile, String outputFileName) {
			String runScript = null;
			String inputFileName = inputFile.getName();
			String inputExt = FileUtils.getExtensionFromFile(inputFile);
			if(inputExt.equalsIgnoreCase("ARJ")) {
				runScript = EMU_ARJ_PATH + " " + "a -r -a -ji" + INDEX_FILE_NAME + " "+ "A:\\" + inputFileName.toUpperCase() + 
				"\r\n" + "ECHO Finished archive conversion to selfextracting ARJ archive. Bye Bye..." +
				"\r\n" + "HALT.EXE";
			}
//			else {
//				runScript = EMU_ARJ_PATH + " " + "f " + "A:\\" + inputFileName.toUpperCase() + "-je" + 
//					"\r\n" + "HALT.EXE";
//			}
			
			File runBat = new File(destFolder, RUN_BAT);
			runBat = FileUtils.writeStringToFile(runScript, runBat);
			log.info(RUN_BAT + " created: " + runScript);
			
			return runBat.exists();
		}


	/**
	 * This methods creates the MigrateResult to be returned by this service.
	 * 
	 * @param resultFile the result file that should be wrapped in a DigitalObject
	 * @param inputFormat the input format to create a ServiceReport message
	 * @param outputFormat the output format to create a ServiceReport message
	 * @return a MigrateResult containing everything it should contain...(--> DigObj, ServiceReport)
	 */
	private MigrateResult createMigrateResult(File resultFile, URI inputFormat, URI outputFormat) {
		DigitalObject result = new DigitalObject.Builder(Content.byReference(resultFile))
								.title(resultFile.getName())
								.format(outputFormat)
								.build();

		String message = "Successfully converted input from: \""
                + format.getFirstExtension(inputFormat) + "\" to \""
                + format.getFirstExtension(outputFormat) + "\".";
        ServiceReport sr = new ServiceReport(Type.INFO, Status.SUCCESS, message);
		
		MigrateResult mainMigrateResult = new MigrateResult(result, sr);
		
		return mainMigrateResult;
	}
	
	
	
	/**
	 * @param message an optional message on what happened to the service
	 * @param e the Exception e which causes the problem
	 * @return CharacteriseResult containing a Error-Report
	 */
	private MigrateResult returnWithErrorMessage(final String message,
	        final Exception e) {
	    if (e == null) {
	        return new MigrateResult(null, ServiceUtils
	                .createErrorReport(message));
	    } else {
	        return new MigrateResult(null, ServiceUtils
	                .createExceptionErrorReport(message, e));
	    }
	}
}
