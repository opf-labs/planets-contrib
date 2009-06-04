/**
 * 
 */
package eu.planets_project.services.migration.soxservices;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;
import eu.planets_project.services.utils.ServiceUtils;

/**
 * @author melmsp
 *
 */


@Stateless()
@Local(Migrate.class)
@Remote(Migrate.class)

@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@WebService(
        name = SoX.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class SoX implements Migrate, Serializable {
	
	public static final String NAME = "SoX";

	private static final long serialVersionUID = -402768544068369154L;
	
	private static final String SoX_HOMEPAGE_URI = "http://sox.sourceforge.net/";
	
	private static final String SHOW_PROGRESS_PARAM = "showProgress";
	private static final String NO_SHOW_PROGRESS_PARAM = "noShowProgress";
	private static final String VERBOSITY_LEVEL_PARAM = "verbosityLevel";
	
	private PlanetsLogger plogger = PlanetsLogger.getLogger(this.getClass());
	
	
	/**
    * SOX tool name as a string
    */
	public final String SOX = "sox";
	
	/**
     * the SOX working directory
     */
    public static  String SoX_WORK_DIR = "SOX";
    
    /**
     * the SOX input dir
     */
    public static  String SoX_IN = "INPUT";
    
    /**
     * the SOX output dir
     */
    public static  String SoX_OUTPUT_DIR = "OUT";
    
    /**
     * SOX home dir
     */
    public static String SOX_HOME = null;
    
    public static String processOutput = "";
    public static String processError = "";
	
	public SoX() {
		// Getting SoX installation location 
		SOX_HOME = System.getenv("SOX_HOME");
    	
    	if(SOX_HOME==null){
    		System.err.println("SOX_HOME is not set! Please create an system variable\n" +
    				"and point it to the SoX installation folder!");
    		plogger.error("SOX_HOME is not set! Please create an system variable\n" +
    				"and point it to the SoX installation folder!");
    		SOX_HOME = "";
    	} else {
    	    SOX_HOME += File.separator;
    	}
		
        plogger.info("Found SoX installation in: " + SOX_HOME);
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.services.migrate.Migrate#describe()
	 */
	public ServiceDescription describe() {
		ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME,Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("A wrapper for the SoX Audio Converter. Using SoX version 14.1.0\n" +
        		"This service accepts input and target formats of this shape: 'planets:fmt/ext/[extension]'\n" +
        		"e.g. 'planets:fmt/ext/wav' or 'planets:fmt/ext/au'\n" +
        		"\n" +
        		"IMPORTANT NOTE:\n" +
        		"---------------\n" +
        		"Decoding and/or Encoding from or to 'mp3', 'ogg' or 'flac' requires additional libraries to be installed\n" +
        		"(on the machine where the the SoX tool is installed, i.e. the machine this service runs on)!!!\n" +
        		"So it might be that some migrations from or to these formats will not deliver the expected results!");
        
        sd.classname(this.getClass().getCanonicalName());
        sd.version("0.1");
        sd.name("Sox Audio Converter Service");
        sd.type("Migrate");
        List<Parameter> parameterList = new ArrayList<Parameter>();
        
        Parameter showProgress = new Parameter.Builder("showProgress", "-S")
                .description(
                        "Display input file format/header information, and processing "
                                + "progress as input file(s) percentage complete, elapsed time, and remaining time "
                                + "(if known; shown in brackets), and the number of samples written to the output file. "
                                + "Also shown is a VU meter, and an indication if clipping has occurred.")
                .build();
        parameterList.add(showProgress);
        
//        Parameter noShowProgress = new Parameter("noShowProgress", "-q");
//        noShowProgress.setDescription("Run in quiet mode when SoX wouldn't otherwise do so; this is the opposite of the -S option.");
//        parameterList.add(noShowProgress);
        
        Parameter verbosityLevel = new Parameter.Builder("verbosityLevel",
                "0-4")
                .description(
                        "This should be an int value between: 0 - 4.\n"
                                + "0: No messages are shown at all; use the exit status to determine if an error has occurred.\n"
                                + "1: Only error messages are shown. These are generated if SoX cannot complete the requested commands.\n"
                                + "2: Warning messages are also shown. These are generated if SoX can complete the requested commands, but not exactly according to the requested command parameters, or if clipping occurs.\n"
                                + "3: Descriptions of SoX's processing phases are also shown. Useful for seeing exactly how SoX is processing your audio.\n"
                                + "4: and above: Messages to help with debugging SoX are also shown.\n"
                                + "By default, the verbosity level is set to 2; "
                                + "each occurrence of the -V option increases the verbosity level by 1. "
                                + "Alternatively, the verbosity level can be set to an absolute number by "
                                + "specifying it immediately after the -V; e.g. -V0 sets it to 0. ")
                .build();
        parameterList.add(verbosityLevel);
        
        sd.parameters(parameterList);

        sd.tool(Tool.create(null, "SoX", "14.1.0", null, SoX_HOMEPAGE_URI));
		
		List<String> inputFormats = new ArrayList<String> ();
//		inputFormats.add("MP3");	// mp3 needs additional library, e.g. "lame"
        inputFormats.add("WAV");
		inputFormats.add("AIFF");
		inputFormats.add("FLAC");
		inputFormats.add("OGG");
//		inputFormats.add("RAW");	// raw does not work as input, but works as output
		
		List<String> outputFormats = new ArrayList<String> ();
//		outputFormats.add("MP3");	// mp3 needs additional library, e.g. "lame"
		outputFormats.add("WAV");
		outputFormats.add("AIFF");
		outputFormats.add("FLAC");
		outputFormats.add("OGG");
		outputFormats.add("RAW");
		
		// creating all possible/sensible combinations
		sd.paths(createMigrationPathwayMatrix(inputFormats, outputFormats));
		return sd.build();
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
	 */
	public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
			URI outputFormat, List<Parameter> parameters) {
		
		String inputError = null;
		boolean inputErrDetected = false;
		String outputError = null;
		boolean outputErrDetected = false;
		
		/* Is the input format supported by this SoX service?
		 * if not, an error message is set up
		 */
		if(!isInputFormatSupported(inputFormat)) {
			inputError = "Unsupported InputFormat " + inputFormat.toASCIIString() + 
			". No result file has been created!";
			inputErrDetected = true;
		}
		
		/* Is the output format supported by this SoX service?
		 * if not, an error message is set up
		 */
		if(!isOutputFormatSupported(outputFormat)) {
			outputError = "Unsupported OutputFormat " + outputFormat.toASCIIString() + 
			". No result file has been created!";
			outputErrDetected = true;
		}

		/* 
		 * Is this an unsupported migration path way?
		 */
		if(inputErrDetected & outputErrDetected) {
			String combinedErrorMessage = "Unsupported migration path: [" + inputFormat.toASCIIString()
											+ " --> " 
											+ outputFormat.toASCIIString()
											+ "] is not supported! No result file has been created!";
			return this.returnWithErrorMessage(combinedErrorMessage, null);
		}
		
		// if we found an invalid input format, cancel execution and return with 
		// an error report
		if(inputErrDetected) {
			return this.returnWithErrorMessage(inputError, null);
		}
		
		// if we found an invalid output format, cancel execution and return with 
		// an error report
		if(outputErrDetected) {
			return this.returnWithErrorMessage(outputError, null);
		}
		
		// executing the migration by calling the convertAudio(...) method
		// and returning the MigrateResult
		return convertAudio(digitalObject, inputFormat, outputFormat, parameters);
		
	}
	
	/* 
	 * Method to test, whether the inputFormat is supported by this service, i.e. by testing 
	 * whether the input format is found in the possible migration paths 
	 */
	private boolean isInputFormatSupported(URI inputFormat) {
		MigrationPath[] migrationPaths = this.describe().getPaths().toArray(new MigrationPath[]{});
		boolean isSupported = false;
		
		for (int i = 0; i < migrationPaths.length; i++) {
			URI currentInputFormat = migrationPaths[i].getInputFormat();
			if(currentInputFormat.toASCIIString().equalsIgnoreCase(inputFormat.toASCIIString())) {
				isSupported = true;
				return isSupported;
			}
		}
		return isSupported;
	}
	
	/* 
	 * Method to test, whether the output Format is supported by this service, i.e. by testing 
	 * whether the output format is found in the possible migration paths 
	 */
	private boolean isOutputFormatSupported(URI outputFormat) {
		MigrationPath[] migrationPaths = this.describe().getPaths().toArray(new MigrationPath[]{});
		boolean isSupported = false;
		
		for (int i = 0; i < migrationPaths.length; i++) {
			URI currentOutputFormat = migrationPaths[i].getOutputFormat();
			if(currentOutputFormat.toASCIIString().equalsIgnoreCase(outputFormat.toASCIIString())) {
				isSupported = true;
				return isSupported;
			}
		}
		return isSupported;
	}
	
	
	
	/**
	 * 
     * @param input input data as byte[]
     * @param inputFormat a format URI specifying the input format (e.g. planets:format/ext/wav) 
     * @param outputFormat a format URI specifying the destination format 
     * @param parameters some additional parameters you wish to pass to the underlying tool?
     * @return the migrated byte[]
     */
    public MigrateResult convertAudio(DigitalObject input,
            URI inputFormat, URI outputFormat, List<Parameter> parameters) {
    	
    	FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
        String srcExt = format.getFirstExtension(inputFormat);
		String destExt = format.getFirstExtension(outputFormat);
    	
        if (!srcExt.startsWith("."))
        	srcExt = "." + srcExt;
        
        if (!destExt.startsWith("."))
        	destExt = "." + destExt;
        
        plogger.info("Starting migration. Converting from "
                + inputFormat.toASCIIString() + " to " + outputFormat.toASCIIString());
        
        // Creating the work folder in Sys temp
        File workFolder = FileUtils.createWorkFolderInSysTemp(SoX_WORK_DIR);
        
        // creating the output folder for temp files inside the work folder
        File outputFolder = FileUtils.createFolderInWorkFolder(workFolder, SoX_OUTPUT_DIR);
        
        // the path for the migrated file to be created
        String outputFilePath = outputFolder.getAbsolutePath() + File.separator + "SoX_OUTPUT_FILE" + destExt; 
        
        // the file the input (temp) file is placed in
        File inputFolder = FileUtils.createFolderInWorkFolder(workFolder, SoX_IN);
        
        // the path to the input file
        String inputFilePath = "SoX_INPUT_FILE" + srcExt;
        
     // getting the input data from the DigitalObject and writing it to a File...
        File inputFile = FileUtils.writeInputStreamToFile(input.getContent().read(), inputFolder, inputFilePath);
        
        // setting up the command line 
        List<String> soxCommands = new ArrayList<String>();
        soxCommands.add(SOX_HOME + SOX);	// the path and name of the tool itself
        soxCommands.add(inputFile.getAbsolutePath());	// the input file
        soxCommands.add(outputFilePath);	// the output file path
        
        
     // Are there any additional parameters for us?
		if(parameters != null) {
			plogger.info("Got additional parameters:");
			String showProgress;
			String noShowProgress;
			String verbosityLevel;
			
			for (Iterator<Parameter> iterator = parameters.iterator(); iterator.hasNext();) {
				Parameter parameter = (Parameter) iterator.next();
				String name = parameter.getName();
				String value = parameter.getValue();
				
				plogger.info("Got parameter: " + name + " with value: " + value);
				if(!name.equalsIgnoreCase(SHOW_PROGRESS_PARAM) && !name.equalsIgnoreCase(NO_SHOW_PROGRESS_PARAM) && !name.equalsIgnoreCase(VERBOSITY_LEVEL_PARAM)) {
					plogger.info("Invalid parameter with name: " + parameter.getName()+"\n using DEFAULT values.");
				}
				
				if(name.equalsIgnoreCase(SHOW_PROGRESS_PARAM)) {
					showProgress = value;
					soxCommands.add(showProgress);
					plogger.info("Enabling 'showProgress' feature '-S'.");
				}
				
				if(name.equalsIgnoreCase(NO_SHOW_PROGRESS_PARAM)) {
					noShowProgress = value;
					soxCommands.add(noShowProgress);
					plogger.info("Enabling 'noShowProgress' feature '-q' or 'quiet mode'.");
				}
				
				if(name.equalsIgnoreCase(VERBOSITY_LEVEL_PARAM)){
					verbosityLevel = value;
					String parameterString = "-V" + verbosityLevel;
					soxCommands.add(parameterString);
					plogger.info("Setting verbosity level to: " + verbosityLevel + "\n" +
							"(Please see ServiceDescription for further information)");
				}
			}
		}
        
		//**********************************************************************
        // Configuring and running the ProcessRunner
		ProcessRunner pr = new ProcessRunner(soxCommands);
		if( SOX_HOME != null && ! "".equals(SOX_HOME))
		    pr.setStartingDir(new File(SOX_HOME));
        
        plogger.info("Executing: " + soxCommands);
        
        pr.run();

        processOutput = pr.getProcessOutputAsString();
        processError = pr.getProcessErrorAsString();
        
        plogger.info("SOX call output: " + processOutput);
        plogger.error("SOX call error: " + processError);
        
        plogger.debug("Executing: " + soxCommands + " finished.");

        plogger.info("Migration finished.");
		//**********************************************************************
        
        
        
        // Getting the output file...
        File processOutputFile = new File(outputFilePath);
        
        // if an output file has been created by the service,
        // read it into a byte[]
        if(processOutputFile.canRead()) {
        	DigitalObject resultDigObj = null;
			try {
				resultDigObj = createDigitalObjectByValue(new URL("http://planetsSoxMigrationService.org"), processOutputFile);
				plogger.info("Created new DigitalObject for result file...");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            plogger.info("Output file lenght: " + processOutputFile.length());
         // create a ServiceReport...
            ServiceReport report = new ServiceReport(Type.INFO, Status.SUCCESS,
                    "Output and error logs:\n" + "--------------------------\n"
                            + processOutput + "\n" + processError);
			
			plogger.info("Created Service report...");
			
			plogger.info("Success!! Returning results!");
			// and create a MigrateResult
			MigrateResult migrateResult = new MigrateResult(resultDigObj, report);

			// and return the result
			return migrateResult;
        }
        
        // if no output file has been created, log out an error and return with an Error report!
        else {
        	plogger.error("No result file created. Maybe SoX is missing a required library for this migration?");
        	return this.returnWithErrorMessage("No result file created. Maybe SoX is missing a required library for this migration?", null);
        }
    }
    
    // Creates a MigrateResult, containing a ServiceReport with the passed
    // error message, and optional a thrown exception e
    private MigrateResult returnWithErrorMessage(String message, Exception e ) {
        if( e == null ) {
        	ServiceReport errorReport = ServiceUtils.createErrorReport(message
        			+ "\n" 
					+ "Process error log:\n" 
					+ "------------------\n"
					+ processError);
            return new MigrateResult(null, errorReport);
        } else {
        	ServiceReport exceptionErrorReport = ServiceUtils.createExceptionErrorReport(message
        			+ "\n" 
					+ "Process error log:\n" 
					+ "------------------\n"
					+ processError + "\n", e);
            return new MigrateResult(null, exceptionErrorReport);
        }
    }
	
	// Convenience method to create a DigitalObject byValue (byte[])
	private DigitalObject createDigitalObjectByValue(URL permanentURL, byte[] resultFileBlob) {
		DigitalObject digObj =  new DigitalObject.Builder(Content.byValue(resultFileBlob)).build();
		return digObj;
	}
	
	// Convenience method to create a DigitalObject byValue (File)
	private DigitalObject createDigitalObjectByValue(URL permanentURL, File resultFile) {
		DigitalObject digObj =  new DigitalObject.Builder(Content.byValue(resultFile)).build();
		return digObj;
	}
	
	// Convenience method to create a DigitalObject byReference
	private DigitalObject createDigitalObjectByReference(URL permanentURL, URL fileLocation) {
		DigitalObject digObj =  new DigitalObject.Builder(Content.byReference(fileLocation)).build();
		return digObj;
	}
	
	private MigrationPath[] createMigrationPathwayMatrix (List<String> inputFormats, List<String> outputFormats) {
		List<MigrationPath> paths = new ArrayList<MigrationPath>();

		for (Iterator<String> iterator = inputFormats.iterator(); iterator.hasNext();) {
			String input = iterator.next();
			
			for (Iterator<String> iterator2 = outputFormats.iterator(); iterator2.hasNext();) {
				String output = iterator2.next();
				if(!input.equalsIgnoreCase(output)) {
					FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
                    MigrationPath path = new MigrationPath(format.createExtensionUri(input), format.createExtensionUri(output), null);
					paths.add(path);
				}
				// Debug...
//				System.out.println(path.getInputFormat() + " --> " + path.getOutputFormat());
			}
		}
		
		return paths.toArray(new MigrationPath[]{});
	}
	

}
