package eu.planets_project.services.migration.soxservices;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;

/**
 * @deprecated Use {@link SoX} instead.
 */

/**
 * Common sox invocation class
 * 
 * @author : Thomas Kraemer thomas.kraemer@uni-koeln.de created : 14.07.2008
 * 
 */
@Deprecated
public class SoxMigrations {

    /**
     * SOX as a string
     */
    public final String SOX = "sox";
    PlanetsLogger plogger = PlanetsLogger.getLogger(SoxMigrations.class);

    /**
     * the system temp directory
     */
    public static final String SYSTEM_TEMP = System.getProperty("java.io.tmpdir") + File.separator;
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
    
    /**
     * no arg default constructor, sets up the directories
     */
    public SoxMigrations() {
    	
    	SOX_HOME = System.getenv("SOX_HOME") + File.separator;
    	
    	if(SOX_HOME==null){
    		System.err.println("SOX_HOME is not set! Please create an system variable\n" +
    				"and point it to the SoX installation folder!");
    		plogger.error("SOX_HOME is not set! Please create an system variable\n" +
    				"and point it to the SoX installation folder!");
    	}
		
        plogger.info("Found SoX installation in: " + SOX_HOME);
        System.out.println("Found SoX installation in: " + SOX_HOME);
    }

	/**
	 * @param input
	 * @return the migrated OGG file
	 */
    @Deprecated
	public byte[] transformMp3ToOgg(byte[] input) {
        plogger.info("transformMp3ToOgg begin ");
        return genericTransformAudioSrcToAudioDest(input, ".mp3", ".ogg", null);
    }
    
	/**
	 * @param input
	 * @return the migrated AIFF file
	 */
    @Deprecated
    public byte[] transformWavToAiff(byte[] input) {
    	plogger.info("transformWavToAiff begin ");
    	return genericTransformAudioSrcToAudioDest(input, ".wav", ".aiff", null);
    }

	/**
	 * @param input
	 * @return the migrated WAV file
	 */
    @Deprecated
    public byte[] transformMp3ToWav(byte[] input) {
        plogger.info("transformMp3ToWav begin ");
        return genericTransformAudioSrcToAudioDest(input, ".mp3", ".wav", null);
    }

	/**
	 * @param input
	 * @return the migrated OGG file
	 */
    @Deprecated
    public byte[] transformWavToOgg(byte[] input) {
        plogger.info("transformWavToOgg begin ");
        return genericTransformAudioSrcToAudioDest(input, ".wav", ".ogg", null);
    }

	/**
	 * @param input
	 * @return the migrated FLAC file
	 */
    @Deprecated
    public byte[] transformWavToFlac(byte[] input) {
        plogger.info("transformWavToFlac begin ");
        return genericTransformAudioSrcToAudioDest(input, ".wav", ".flac", null);
    }

	/**
	 * @param input
	 * @return the migrated FLAC file
	 */
    @Deprecated
    public byte[] transformMp3ToFlac(byte[] input) {
        plogger.info("transformMp3ToFlac begin ");
        return genericTransformAudioSrcToAudioDest(input, ".mp3", ".flac", null);
    }


    /**
     * @param input
     * @param srcSuffix
     * @param destSuffix
     * @param parameters
     * @return the migrated byte[]
     */
    public byte[] genericTransformAudioSrcToAudioDest(byte[] input,
            String srcSuffix, String destSuffix, List<Parameter> parameters) {
    	
        if (!srcSuffix.startsWith("."))
            srcSuffix = "." + srcSuffix;
        
        if (!destSuffix.startsWith("."))
            destSuffix = "." + destSuffix;
        
        plogger.info("genericTransformAudioSrcToAudioDest begin: Converting from "
                + srcSuffix + " to " + destSuffix);
        File workFolder = FileUtils.createWorkFolderInSysTemp(SoX_WORK_DIR);
        
        File outputFolder = FileUtils.createFolderInWorkFolder(workFolder, SoX_OUTPUT_DIR);
        
        String outputFilePath = outputFolder.getAbsolutePath() + File.separator + "SoX_OUTPUT_FILE" + destSuffix; 
        
        File inputFolder = FileUtils.createFolderInWorkFolder(workFolder, SoX_IN);
        
        String inputFilePath = inputFolder.getAbsolutePath() + File.separator + "SoX_INPUT_FILE" + srcSuffix;
        File inputFile = FileUtils.writeByteArrayToFile(input, inputFilePath);
        
        
        try {
            List<String> commands = Arrays.asList(SOX_HOME + SOX, inputFile
                    .getAbsolutePath(), outputFilePath);
            if(parameters!=null) {
            	// TO DO: fill in parameter handling!
            }
            
            ProcessRunner pr = new ProcessRunner(commands);
            
            pr.setStartingDir(new File(SOX_HOME));
            
            plogger.info("Executing: " + commands);
            
            pr.run();

            plogger.info("SOX call output: " + pr.getProcessOutputAsString());
            plogger.error("SOX call error: " + pr.getProcessErrorAsString());
            
            plogger.debug("Executing: " + commands + " finished.");

        } catch (Exception ex) {
            plogger.error("SoX could not create the output file");
        }
        plogger.info("genericTransformAudioSrcToAudioDest end");
        File processOutputFile = new File(outputFilePath);
        byte[] outputFileData = null;
        
        if(processOutputFile.canRead()) {
        	outputFileData = FileUtils.readFileIntoByteArray(new File(outputFilePath));
            plogger.info(outputFileData.length);
        }
        else {
        	outputFileData = null;
        	plogger.error("SoX didn't create an output file!");
        }
        
        
        return outputFileData;
    }
}
