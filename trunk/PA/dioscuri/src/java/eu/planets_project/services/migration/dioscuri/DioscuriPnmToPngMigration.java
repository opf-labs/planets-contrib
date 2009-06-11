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
        name = DioscuriPnmToPngMigration.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class DioscuriPnmToPngMigration implements Migrate, Serializable {
	
	private PlanetsLogger log = PlanetsLogger.getLogger(this.getClass()); 
	private static final long serialVersionUID = 7520484154909390134L;

	public static final String NAME = "DioscuriPnmToPngMigration";
	private String WORK_TEMP_NAME = "DIOSCURI_PNM2PNG_TMP";
	private String sessionID = FileUtils.randomizeFileName("");
	private String FLOPPY_INPUT_NAME = "FLOPPY_INPUT" + sessionID;
	private File WORK_TEMP_FOLDER = FileUtils.createWorkFolderInSysTemp(WORK_TEMP_NAME);
	private File FLOPPY_INPUT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, FLOPPY_INPUT_NAME);
	
	private String DEFAULT_INPUT_NAME = "input" + sessionID;
	private static String RUN_BAT = "RUN.BAT";
//	private static String INPUT_ZIP_NAME = "input.zip";
	private static String INPUT_ZIP_NAME = null;
	
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
	}
	

	/* (non-Javadoc)
	 * @see eu.planets_project.services.PlanetsService#describe()
	 */
	public ServiceDescription describe() {
		ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME, Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("This is a Prototype wrapper for the DIOSCURI-Emulator developed at KB-NL.\r\n" +
        		"This service converts images from \".PNM\"-format to \".PNG\" and vice versa, using \"pnm2png.exe\" and \" png2pnm.exe\" running on MS-DOS 5.0 \r\n" +
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
		if(WORK_TEMP_FOLDER.exists()) {
			FileUtils.deleteAllFilesInFolder(WORK_TEMP_FOLDER);
		}
		INPUT_ZIP_NAME = FileUtils.randomizeFileName("dioscuri-pnm-png-input.zip");
		
		FLOPPY_INPUT_FOLDER = FileUtils.createFolderInWorkFolder(WORK_TEMP_FOLDER, FLOPPY_INPUT_NAME);
		
		File inputFile = getInputFileFromDigitalObject(digitalObject, inputFormat, FLOPPY_INPUT_FOLDER);
		String inFileName = inputFile.getName();
		String outFileName = getOutputFileName(inputFile, outputFormat);
		
		boolean runBatCreated = createRunBat(FLOPPY_INPUT_FOLDER, inputFile, outFileName);	
		
		DioscuriWrapper dioscuri = new DioscuriWrapper();
		
		if(runBatCreated) {
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
		private boolean createRunBat(File destFolder, File inputFile, String outputFileName) {
			String runScript = null;
			String inputFileName = inputFile.getName();
			String inputExt = FileUtils.getExtensionFromFile(inputFile);
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
