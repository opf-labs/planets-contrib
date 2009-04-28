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
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ImmutableContent;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ServiceUtils;
import eu.planets_project.services.utils.ZipResult;

/**
 * @author melmsp
 *
 */

@Stateless()

@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")

@WebService(
        name = DioscuriPnmToPngMigration.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class DioscuriPnmToPngMigration implements Migrate, Serializable {
	
	private PlanetsLogger log = PlanetsLogger.getLogger(this.getClass()); 
	private static final long serialVersionUID = 7520484154909390134L;

	public static final String NAME = "DioscuriPnmToPngMigration";
	private static String DIOSCURI_HOME = System.getenv("DIOSCURI_HOME");
	private static File WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp("DIOSCURI_TMP");
	private static File FLOPPY_INPUT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, "FLOPPY_INPUT");
	
	private static String DEFAULT_INPUT_NAME = "input";
	private static String RUN_BAT = "RUN.BAT";
	private static String INPUT_ZIP_NAME = "input.zip";
	
//	private static String EMU_PICTVIEW_PATH = "c:\\pictview\\pictview.exe";
	
	private static String EMU_PNM_TO_PNG_PATH = "C:\\pngminus\\pnm2png.exe";
	private static String EMU_PNG_TO_PNM_PATH = "C:\\pngminus\\png2pnm.exe";
	
	private static FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
	
//	private HashMap<URI, String> supportedFormats = new HashMap<URI, String>();
	
	public DioscuriPnmToPngMigration() {
//		supportedFormats.put(Format.extensionToURI("TGA"), "-a");
//		supportedFormats.put(Format.extensionToURI("JPEG"), "-j");
//		supportedFormats.put(Format.extensionToURI("JPG"), "-j");
//		supportedFormats.put(Format.extensionToURI("RAS"), "-r");
//		supportedFormats.put(Format.extensionToURI("PIC"), "-A");
//		supportedFormats.put(Format.extensionToURI("PPM"), "-k");
//		supportedFormats.put(Format.extensionToURI("PNM"), "-k");
//		supportedFormats.put(Format.extensionToURI("PGM"), "-k");
//		supportedFormats.put(Format.extensionToURI("PBM"), "-k");
//		supportedFormats.put(Format.extensionToURI("SCX"), "-s");
//		supportedFormats.put(Format.extensionToURI("CUT"), "-c");
//		supportedFormats.put(Format.extensionToURI("IFF"), "-l");
//		supportedFormats.put(Format.extensionToURI("ILBM"), "-l");
//		supportedFormats.put(Format.extensionToURI("TIFF"), "-t");
//		supportedFormats.put(Format.extensionToURI("TIF"), "-t");
//		supportedFormats.put(Format.extensionToURI("CEL"), "-C");
//		supportedFormats.put(Format.extensionToURI("IMG"), "-n");
//		supportedFormats.put(Format.extensionToURI("RLE"), "-u");
//		supportedFormats.put(Format.extensionToURI("INGR"), "-e");
//		supportedFormats.put(Format.extensionToURI("OS2BMP"), "-o");
//		supportedFormats.put(Format.extensionToURI("GIF"), "-g");
//		supportedFormats.put(Format.extensionToURI("PCX"), "-p");
//		supportedFormats.put(Format.extensionToURI("BMP"), "-w");
//		supportedFormats.put(Format.extensionToURI("ICO"), "-i");
		FileUtils.deleteTempFiles(WORK_TEMP_FOLDER);
		WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp("DIOSCURI_TMP");
		FLOPPY_INPUT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, "FLOPPY_INPUT");
	}
	

	/* (non-Javadoc)
	 * @see eu.planets_project.services.PlanetsService#describe()
	 */
	public ServiceDescription describe() {
		ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME, Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("This is a Prototype wrapper for the DIOSCURI-Emulator developed at KB-NL.\r\n" +
        		"This service converts images from \".PNM\"-format to \".PNG\", using \"pnm2png.exe\" running in a MS-DOS 5.0 environment\r\n" +
        		"emulated by DIOSCURI (16-Bit mode).");
        sd.classname(this.getClass().getCanonicalName());
        sd.version("1.0");
        sd.tool( Tool.create(null, 
        		"PngMinus (MS-DOS)", 
        		"unknown", 
        		"Pnm2png / Png2pnm\n" +
        		"As said, pnmtopng requires PbmPlus / netpbm to be built. And not everybody wants to or is able " +
        		"to get that library in shape. Also, libpng and zlib are available on more platforms than netpbm. " +
        		"Therefore I wrote PngMinus, with the pnm2png and png2pnm converter utilities, that has built-in " +
        		"functions to read ppm/pgm/pbm files. But it is also more limited related to the size of the files " +
        		"it can handle. \n" +
        		"The package comes with build scripts to compile PngMinus on Linux using gcc or on MS-DOS using " +
        		"Borland's Turbo-C 3.0. Which doesn't mean that it is limited to that, for example I also " +
        		"compiled it successfully using Visual-C. You can download the sources from this site, " +
        		"but the package is also included with libpng, where you will find it in the \"contrib\" folder." +
        		"For those who just want some MS-DOS binaries, this zip-file contains two executables that also " +
        		"run fine in a Windows DOS-box. I have also included two libpng and zlib library files, precompiled " +
        		"for the MS-DOS platform. With the two zip-files together, you are all set to go without the need " +
        		"for any other packages. ", 
        		"http://www.schaik.com/png/pnmtopng.html#pngminus"));
        List<MigrationPath> pathways = new ArrayList<MigrationPath>();
        pathways.add(new MigrationPath(format.createExtensionUri("PNM"), format.createExtensionUri("PNG"), null));
        pathways.add(new MigrationPath(format.createExtensionUri("PNG"), format.createExtensionUri("PNM"), null));
        
        sd.paths(pathways.toArray(new MigrationPath[] {}));
        return sd.build();
	}


	/* (non-Javadoc)
	 * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameters)
	 */
	public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
			URI outputFormat, List<Parameter> parameters) {
		
		if(DIOSCURI_HOME==null) {
			return this.returnWithErrorMessage("Dioscuri is NOT properly installed on your system!\n" +
					"Please install it and point a Systemvariable called DIOSCURI_HOME to the installation folder!\n" +
					"Otherwise this service will refuse to work ;-)", null);
		}
		else {
			log.info("DIOSCURI_HOME is set: " + DIOSCURI_HOME);
		}
		
		FileUtils.deleteTempFiles(WORK_TEMP_FOLDER);
		WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp("DIOSCURI_TMP");
		FLOPPY_INPUT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, "FLOPPY_INPUT");
		
		File inputFile = getInputFileFromDigitalObject(digitalObject, inputFormat, FLOPPY_INPUT_FOLDER);
		String inFileName = inputFile.getName();
		String outFileName = getOutputFileName(inputFile, outputFormat);
		
		boolean runBatCreated = createRunBat(FLOPPY_INPUT_FOLDER, inFileName, outFileName, FileUtils.getExtensionFromFile(inputFile));	
		
		DioscuriWrapper dioscuri = new DioscuriWrapper();
		
		if(runBatCreated) {
			ZipResult zipResult = FileUtils.createZipFileWithChecksum(FLOPPY_INPUT_FOLDER, WORK_TEMP_FOLDER, INPUT_ZIP_NAME);
			DioscuriResult result = dioscuri.createFloppyImageAndRunDioscuri(zipResult.getZipFile(), inFileName, outFileName, zipResult.getChecksum());
			if(result.getState()==DioscuriResult.ERROR) {
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
	
	private File getInputFileFromDigitalObject(DigitalObject digObj, URI inputFormat, File destFolder) {
		String inName = getFileNameFromDigitalObject(digObj, inputFormat);
		File in = new File(destFolder, inName);
		FileUtils.writeInputStreamToFile(digObj.getContent().read(), in);
		in = FileUtils.truncateNameAndRenameFile(in);
		return in;
	}
	
	private String getFileNameFromDigitalObject(DigitalObject digObj, URI inputFormat) {
		String fileName = digObj.getTitle();
		if(fileName==null) {
			String ext = "." + format.getFirstExtension(inputFormat);
			fileName = DEFAULT_INPUT_NAME + ext;
		}
		return fileName;
	}
	
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
	
	private MigrateResult createMigrateResult(File resultFile, URI inputFormat, URI outputFormat) {
		DigitalObject result = new DigitalObject.Builder(ImmutableContent.asStream(resultFile))
								.title(resultFile.getName())
								.format(format.createExtensionUri(FileUtils.getExtensionFromFile(resultFile)))
								.build();

		ServiceReport sr = new ServiceReport();

		sr.setInfo("Successfully converted input from: \"" + format.getFirstExtension(inputFormat) + "\" to \"" + format.getFirstExtension(outputFormat) + "\".");
		sr.setErrorState(ServiceReport.SUCCESS);
		
		MigrateResult mainMigrateResult = new MigrateResult(result, sr);
		
		return mainMigrateResult;
	}
	
	
//	private File createFloppyImage(DigitalObject digObj, URI inputFormat, URI outputFormat) {
//		String inputExt = "." + format.getFirstExtension(inputFormat);
//		String outputExt = "." + format.getFirstExtension(outputFormat);
//		
//		String inputFileName = digObj.getTitle();
//		
//		if(inputFileName==null) {
//			inputFileName = DEFAULT_INPUT_NAME + inputExt;
//		}
//		
//		File inputFile = new File(WORK_TEMP_FOLDER, inputFileName);
//		FileUtils.writeInputStreamToFile(digObj.getContent().read(), inputFile);
//		
//		inputFile = FileUtils.truncateNameAndRenameFile(inputFile);
//		
//		String outputFileName = this.stripExtension(inputFile.getName()) + outputExt;
//		
//		boolean run_bat_created = createRunBat(FLOPPY_INPUT_FOLDER, inputFile.getName(), outputFileName, inputExt);
//		
//		if(run_bat_created) {
//			ZipResult zip = FileUtils.createZipFileWithChecksum(FLOPPY_INPUT_FOLDER, WORK_TEMP_FOLDER, INPUT_ZIP_NAME);
//			
//			Migrate floppyHelper = new FloppyImageHelperWin();
//			
//			Content content = ImmutableContent.asStream(zip.getZipFile()).withChecksum(zip.getChecksum());
//			
//			DigitalObject floppyInput = new DigitalObject.Builder(content)
//											.title(zip.getZipFile().getName())
//											.format(format.createExtensionUri("ZIP"))
//											.build();
//			
//			MigrateResult floppyHelperResult = floppyHelper.migrate(floppyInput, format.createExtensionUri("ZIP"), format.createExtensionUri("IMA"), null);
//			
//			if(floppyHelperResult.getReport().getErrorState()!= 0) {
//				ERROR_OUT = floppyHelperResult.getReport().getError();
//				return null;
//			}
//			
//			log.info("FloppyImageHelperWin report: Successfull created floppy image!");
//			
//			DigitalObject floppyImageDigObj = floppyHelperResult.getDigitalObject();
//			
//			File floppyImage = new File(WORK_TEMP_FOLDER, FLOPPY_NAME) ;
//				
//			FileUtils.writeInputStreamToFile(floppyImageDigObj.getContent().read(), floppyImage);
//			
//			return floppyImage;
//		}
//		else {
//			ERROR_OUT = "Error writing run.bat, therefore no floppyImage will be created!";
//			return null;
//		}
//	}
	
	private boolean createRunBat(File destFolder, String inputFileName, String outputFileName, String inputExt) {
		String runScript = null;
		if(inputExt.equalsIgnoreCase("PNM")) {
			runScript = EMU_PNM_TO_PNG_PATH + " " + "A:\\" + inputFileName.toUpperCase() + " " + "A:\\" + outputFileName.toUpperCase() +
				"\r\n" + "HALT.EXE";
		}
		else {
			runScript = EMU_PNG_TO_PNM_PATH + " " + "A:\\" + inputFileName.toUpperCase() + " " + "A:\\" + outputFileName.toUpperCase() +
				"\r\n" + "HALT.EXE";
		}
		
		File runBat = new File(destFolder, RUN_BAT);
		runBat = FileUtils.writeStringToFile(runScript, runBat);
		log.info("run.bat created: " + runScript);
		
		return runBat.exists();
	}
	
//	private File extractResultFileFromFloppyImage(File floppyImage) {
//		Migrate extract = new FloppyImageHelperWin();
//		
//		DigitalObject floppy = new DigitalObject.Builder(ImmutableContent.asStream(floppyImage))
//									.title(floppyImage.getName())
//									.format(format.createExtensionUri(FileUtils.getExtensionFromFile(floppyImage)))
//									.build();
//		
//		MigrateResult mr = extract.migrate(floppy, format.createExtensionUri(FileUtils.getExtensionFromFile(floppyImage)), format.createExtensionUri("ZIP"), null);
//		
//		File resultZIP = new File(WORK_TEMP_FOLDER, mr.getDigitalObject().getTitle());
//		
//		FileUtils.writeInputStreamToFile(mr.getDigitalObject().getContent().read(), resultZIP);
//		
//		Checksum check = mr.getDigitalObject().getContent().getChecksum();
//		
//		List<File> extractedFiles = FileUtils.extractFilesFromZipAndCheck(resultZIP, FLOPPY_RESULT_FOLDER, check);
//		
//		int index = extractedFiles.indexOf(new File(FLOPPY_RESULT_FOLDER, OUTFILE_NAME + OUTFILE_EXT));
//		
//		File main_result = null;
//		
//		if(index!=-1) {
//			main_result = extractedFiles.get(index);
//		}
//		else {
//			ERROR_OUT = "An unidentified error occured! No result file created! ";
//		}
//		return main_result;
//	}

//	private File createConfigFile(File floppyImage) {
//		String configString = FileUtils.readTxtFileIntoString(new File(DIOSCURI_CONFIG_FILE_PATH));
//		String floppyPath = floppyImage.getAbsolutePath();
//		if(floppyPath.contains("\\")) {
//			floppyPath = floppyPath.replace("\\", "/");
//		}
//		configString = configString.replace("INSERT_FLOPPY_PATH_HERE", floppyPath);
//		File tmpConfigFile = new File(WORK_TEMP_FOLDER, "dioscuri_config.xml"); 
//		FileUtils.writeStringToFile(configString, tmpConfigFile);
//		return tmpConfigFile;
//	}
	
	
	
//	private ArrayList<String> getDioscuriCommandline(File configFile) {
//		ArrayList<String> commands = new ArrayList<String> ();
//		commands.add("java");
//		commands.add("-jar");
//		commands.add("dioscuri.jar");
//		commands.add("-c");
//		commands.add("\"" + configFile.getAbsolutePath()+ "\"");
//		commands.add("-h");
//		commands.add("autorun");
//		commands.add("autoshutdown");
//		
//		return commands;
//	}
	
//	private String stripExtension(String fileName) {
//		String result = null;
//		if(fileName.contains(".")) {
//			result = fileName.substring(0, fileName.lastIndexOf("."));
//			return result;
//		}
//		else {
//			return fileName;
//		}
//	}
	
	
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
