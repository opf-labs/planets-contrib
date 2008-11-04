package eu.planets_project.services.migration.soxservices;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;

/**
 * Common sox invocation class
 * 
 * @author : Thomas Krämer thomas.kraemer@uni-koeln.de created : 14.07.2008
 * 
 */
public class SoxMigrations {

    public final String SOX = "sox";
    PlanetsLogger log = PlanetsLogger.getLogger(SoxMigrations.class);

    public static final String SYSTEM_TEMP = System.getProperty("java.io.tmpdir") + File.separator;
    public static  String SoX_WORK_DIR = null;
    public static  String SoX_IN = null;
    public static  String SoX_OUTPUT_DIR = null;
    public static String SOX_HOME = null;
    
    public SoxMigrations() {
    	
    	SOX_HOME = System.getenv("SOX_HOME") + File.separator;
    	
    	if(SOX_HOME==null){
    		System.err.println("SOX_HOME is not set! Please create an system variable\n" +
    				"and point it to the SoX installation folder!");
    		log.error("SOX_HOME is not set! Please create an system variable\n" +
    				"and point it to the SoX installation folder!");
    	}
		
		if(SYSTEM_TEMP.endsWith(File.separator)) {
		    if (SYSTEM_TEMP.endsWith(File.separator + File.separator)) {
		        SoX_WORK_DIR = SYSTEM_TEMP.replace(File.separatorChar + File.separator, File.separator) + "SoX" + File.separator;
		        SoX_IN = SoX_WORK_DIR + "IN" + File.separator;
		        SoX_OUTPUT_DIR = SoX_WORK_DIR + "OUT" + File.separator;
		    }
		    else {
		    	SoX_WORK_DIR = SYSTEM_TEMP + File.separator + "SoX" + File.separator;
		    	SoX_IN = SoX_WORK_DIR + "IN" + File.separator;
		        SoX_OUTPUT_DIR = SoX_WORK_DIR + "OUT" + File.separator;
		    }
		}
		else {
			SoX_WORK_DIR = SYSTEM_TEMP + File.separator + "SoX" + File.separator;
			SoX_IN = SoX_WORK_DIR + "IN" + File.separator;
		    SoX_OUTPUT_DIR = SoX_WORK_DIR + "OUT" + File.separator;
		}
        log.info("Pointed to sox in: " + SOX_HOME);
        System.out.println("Pointed SoX_HOME to: " + SOX_HOME);
    }

	public byte[] transformMp3ToOgg(byte[] input) {
        log.info("transformMp3ToOgg begin ");
        return genericTransformAudioSrcToAudioDest(input, ".mp3", ".ogg", null);
    }
    
    public byte[] transformWavToAiff(byte[] input) {
    	log.info("transformWavToAiff begin ");
    	return genericTransformAudioSrcToAudioDest(input, ".wav", ".aiff", null);
    }

    public byte[] transformMp3ToWav(byte[] input) {
        log.info("transformMp3ToWav begin ");
        return genericTransformAudioSrcToAudioDest(input, ".mp3", ".wav", null);
    }

    public byte[] transformWavToOgg(byte[] input) {
        log.info("transformWavToOgg begin ");
        return genericTransformAudioSrcToAudioDest(input, ".wav", ".ogg", null);
    }

    public byte[] transformWavToFlac(byte[] input) {
        log.info("transformWavToFlac begin ");
        return genericTransformAudioSrcToAudioDest(input, ".wav", ".flac", null);
    }

    public byte[] transformMp3ToFlac(byte[] input) {
        log.info("transformMp3ToFlac begin ");
        return genericTransformAudioSrcToAudioDest(input, ".mp3", ".flac", null);
    }

//    public DataHandler transformMp3ToOggDH(DataHandler input) {
//        log.info("transformMp3ToOggDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".mp3", ".ogg",
//                null);
//    }
//    
//    public DataHandler transformWavToAiffDH(DataHandler input) {
//    	log.info("transformWavToRAW begin ");
//    	return genericTransformAudioSrcToAudioDestDH(input, ".wav", ".aiff", null);
//    }
//
//    public DataHandler transformMp3ToWavDH(DataHandler input) {
//        log.info("transformMp3ToWavDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".mp3", ".wav",
//                null);
//    }
//
//    public DataHandler transformWavToOggDH(DataHandler input) {
//        log.info("transformWavToOggDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".wav", ".ogg",
//                null);
//    }
//
//    public DataHandler transformWavToFlacDH(DataHandler input) {
//        log.info("transformWavToFlacDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".wav", ".flac",
//                null);
//    }
//
//    public DataHandler transformMp3ToFlacDH(DataHandler input) {
//        log.info("transformMp3ToFlacDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".mp3", ".flac",
//                null);
//    }

    public byte[] genericTransformAudioSrcToAudioDest(byte[] input,
            String srcSuffix, String destSuffix, ArrayList<String> soxCliParams) {
    	
        if (!srcSuffix.startsWith("."))
            srcSuffix = "." + srcSuffix;
        
        if (!destSuffix.startsWith("."))
            destSuffix = "." + destSuffix;
        
        log.info("genericTransformAudioSrcToAudioDest begin: Converting from "
                + srcSuffix + " to " + destSuffix);
        File workFolder = new File(SoX_WORK_DIR);
        if(!workFolder.exists()) {
        	workFolder.mkdir();
        }
        
        File outputFolder = new File(SoX_OUTPUT_DIR);
        
        if(!outputFolder.exists()) {
        	boolean madeOutputFolder = outputFolder.mkdir();
        }
        
        String outputFilePath = outputFolder.getAbsolutePath() + File.separator + "SoX_OUTPUT_FILE" + destSuffix; 
        
        File inputFolder = new File(SoX_IN);
        
        if(!inputFolder.exists()) {
        	boolean madeInputFolder = inputFolder.mkdir();
        }
        
        String inputFilePath = inputFolder.getAbsolutePath() + File.separator + "SoX_INPUT_FILE" + srcSuffix;
        File inputFile = ByteArrayHelper.writeToDestFile(input, inputFilePath);
        
        
        try {
            List<String> commands = Arrays.asList(SOX_HOME + SOX, inputFile
                    .getAbsolutePath(), outputFilePath);
            if(soxCliParams!=null) {
            	commands.addAll(soxCliParams);
            }
            
            ProcessRunner pr = new ProcessRunner(commands);
            
            pr.setStartingDir(new File(SOX_HOME));
            
            log.info("Executing: " + commands);
            
            pr.run();

            log.info("SOX call output: " + pr.getProcessOutputAsString());
            log.error("SOX call error: " + pr.getProcessErrorAsString());
            
            log.debug("Executing: " + commands + " finished.");

        } catch (Exception ex) {
            log.error("SoX could not create the output file");
        }
        log.info("genericTransformAudioSrcToAudioDest end");
        File processOutputFile = new File(outputFilePath);
        byte[] outputFileData = null;
        
        if(processOutputFile.canRead()) {
        	outputFileData = ByteArrayHelper.read(new File(outputFilePath));
            log.info(outputFileData.length);
        }
        else {
        	outputFileData = null;
        	log.error("SoX didn't create an output file!");
        }
        
        boolean deletedFolders = deleteTempFiles(workFolder);
        
        return outputFileData;
    }

    
    
//    public DataHandler genericTransformAudioSrcToAudioDestDH(DataHandler input,
//            String srcSuffix, String destSuffix, ArrayList<String> soxCliParams) {
//        log
//                .info("genericTransformAudioSrcToAudioDestDH begin : Converting from "
//                        + srcSuffix + " to " + destSuffix);
//        File f = FileUtils.tempFile("tempout.audio", destSuffix);
//        byte[] raw = null;
//        try {
//            FileOutputStream fos = new FileOutputStream(f);
//            log.info(input.getContentType());
//            log.info(raw);
//            log.info(input.getContent().toString());
//            log.info(raw);
//            fos.write(raw);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        File inputFile = FileUtils.tempFile(raw, "tempin.audio", srcSuffix);
////        if (!f.canRead() || !inputFile.exists() || !f.exists() || !f.canWrite()
////                || !inputFile.canRead() || !inputFile.canWrite()) {
////            throw new IllegalStateException("Can't read from or write to: "
////                    + f.getAbsolutePath());
////        }
//        try {
//            List<String> commands = Arrays.asList(SOX_HOME + File.separator + SOX, inputFile
//                    .getAbsolutePath(), f.getAbsolutePath());
//            File home = new File(SOX_HOME);
//            if (soxCliParams != null) {
//                commands.addAll(soxCliParams);
//            }
//            ProcessRunner pr = new ProcessRunner(commands);
//            pr.setStartingDir(home);
//            log.info("Executing: " + commands);
//            pr.run();
//            log.info("SOX call output: " + pr.getProcessOutputAsString());
//            log.error("SOX call error: " + pr.getProcessErrorAsString());
//            log.debug("Executing: " + commands + " finished.");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            log.error("SoX could not create the open document file");
//        }
//        log.info("genericTransformAudioSrcToAudioDestDH end");
//        log.info(f.length());
//        return new DataHandler(new ByteArrayDataSource(ByteArrayHelper.read(f),
//                "application/octet-stream"));
//    }
    
    private boolean deleteTempFiles(File workFolder) {
		String workFolderName = workFolder.getPath();
		if (workFolder.isDirectory()){
			File[] entries = workFolder.listFiles();
				for (int i=0;i<entries.length;i++){
					File current = entries[i];
					deleteTempFiles(current);
				}
			if (workFolder.delete()) {
				log.info("Deleted: " + workFolderName);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (workFolder.delete()) {
				log.info("Deleted: " + workFolderName);
				return true;
			}
			else {
				return false;
			}
		}
	}

}
