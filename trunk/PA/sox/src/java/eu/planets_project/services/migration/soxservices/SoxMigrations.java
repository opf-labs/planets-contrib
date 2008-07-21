package eu.planets_project.services.migration.soxservices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import eu.planets_project.ifr.core.common.cli.ProcessRunner;
import eu.planets_project.ifr.core.common.logging.PlanetsLogger;

/**
 * Common sox invocation class
 * 
 *  @author : Thomas Krämer thomas.kraemer@uni-koeln.de
 *  created : 14.07.2008
 *  
 */
public class SoxMigrations {
	
	public final String SOX = "sox";
    PlanetsLogger log = PlanetsLogger.getLogger(SoxMigrations.class);

    public static String SOX_HOME;
    
    
    public SoxMigrations() {
        Properties props = new Properties();
        try {
            props.load( this.getClass().getResourceAsStream("/eu/planets_project/services/migration/soxservices/sox.properties"));
            SOX_HOME = props.getProperty("sox.bin.dir");
            
        } catch( IOException e ) {
            // Use a default for now.
        	SOX_HOME= "/usr/bin/";
        }
        log.info("Pointed to sox in: "+SOX_HOME);
    }

    public byte[] transformAnyToWav(byte [] input) {
        log.info("transformAnyToWav begin ");
        File f = FileUtils.tempFile("tempout", ".wav");
        File inputFile = FileUtils.tempFile( input, "tempin.audio", ".mp3");
        if (!f.canRead()||!inputFile.exists()||!f.exists() || !f.canWrite()||!inputFile.canRead() || !inputFile.canWrite()) {
			throw new IllegalStateException("Can't read from or write to: "
					+ f.getAbsolutePath());
		}
        
        try {
        	List<String> commands = Arrays.asList(SOX, inputFile.getAbsolutePath(),f.getAbsolutePath());  
        	File home = new File(SOX_HOME);
    		
        	ProcessRunner pr = new ProcessRunner(commands);
        	pr.setStartingDir(home);
    		log.info("Executing: " + commands);
    		pr.run();

    		log.info("SOX call output: " + pr.getProcessOutputAsString());
    		log.error("SOX call error: " + pr.getProcessErrorAsString());
    		log.debug("Executing: " + commands+" finished.");
    		
        	
        } catch (Exception ex) {
            log.error("SoX could not create the open document file");
        }
        log.info("transformAnyToWav end");
        log.info(f.length());
    	return getByteArrayFromFile(f);
    }
    
    
    public byte[] transformWavToOgg(byte [] input) {
	    log.info("transformAnyToOgg begin ");
	    File f = FileUtils.tempFile("tempout", ".ogg");
	    File inputFile = FileUtils.tempFile( input, "tempin.audio", ".wav");
	    if (!f.canRead()||!inputFile.exists()||!f.exists() || !f.canWrite()||!inputFile.canRead() || !inputFile.canWrite()) {
			throw new IllegalStateException("Can't read from or write to: "
					+ f.getAbsolutePath());
		}
	    
	    try {
	    	List<String> commands = Arrays.asList(SOX, inputFile.getAbsolutePath(),f.getAbsolutePath());  
	    	File home = new File(SOX_HOME);
			
	    	ProcessRunner pr = new ProcessRunner(commands);
	    	pr.setStartingDir(home);
			log.info("Executing: " + commands);
			pr.run();
	
			log.info("SOX call output: " + pr.getProcessOutputAsString());
			log.error("SOX call error: " + pr.getProcessErrorAsString());
			log.debug("Executing: " + commands+" finished.");
			
	    	
	    } catch (Exception ex) {
	        log.error("SoX could not create the open document file");
	    }
	    log.info("transformAnyToOgg end");
	    log.info(f.length());
		return getByteArrayFromFile(f);
	}

	public byte[] transformWavToFlac(byte [] input) {
        log.info("transformAnyToOgg begin ");
        File f = FileUtils.tempFile("tempout", ".flac");
        File inputFile = FileUtils.tempFile( input, "tempin.audio", ".wav");
        if (!f.canRead()||!inputFile.exists()||!f.exists() || !f.canWrite()||!inputFile.canRead() || !inputFile.canWrite()) {
			throw new IllegalStateException("Can't read from or write to: "
					+ f.getAbsolutePath());
		}
        
        try {
        	List<String> commands = Arrays.asList(SOX, inputFile.getAbsolutePath(),f.getAbsolutePath());  
        	File home = new File(SOX_HOME);
    		
        	ProcessRunner pr = new ProcessRunner(commands);
        	pr.setStartingDir(home);
    		log.info("Executing: " + commands);
    		pr.run();

    		log.info("SOX call output: " + pr.getProcessOutputAsString());
    		log.error("SOX call error: " + pr.getProcessErrorAsString());
    		log.debug("Executing: " + commands+" finished.");
    		
        	
        } catch (Exception ex) {
            log.error("SoX could not create the open document file");
        }
        log.info("transformAnyToOgg end");
        log.info(f.length());
    	return getByteArrayFromFile(f);
    }
    
    
    
    public DataHandler transformAnyToWavDH(DataHandler input) {
	    log.info("transformAnyToWav begin ");
	    File f = FileUtils.tempFile("tempout", ".wav");
	    byte[] raw=null;
	    try {
			
	    FileOutputStream fos = new FileOutputStream(f);
	    log.info(input.getContentType());
	    log.info(raw);
	    log.info(input.getContent().toString());
	    
	    log.info(raw);
	    fos.write(raw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    File inputFile = FileUtils.tempFile( raw , "tempin.audio", ".mp3");
	    if (!f.canRead()||!inputFile.exists()||!f.exists() || !f.canWrite()||!inputFile.canRead() || !inputFile.canWrite()) {
			throw new IllegalStateException("Can't read from or write to: "
					+ f.getAbsolutePath());
		}
	    
	    try {
	    	List<String> commands = Arrays.asList(SOX, inputFile.getAbsolutePath(),f.getAbsolutePath());  
	    	File home = new File(SOX_HOME);
			
	    	ProcessRunner pr = new ProcessRunner(commands);
	    	pr.setStartingDir(home);
			log.info("Executing: " + commands);
			pr.run();
	
			log.info("SOX call output: " + pr.getProcessOutputAsString());
			log.error("SOX call error: " + pr.getProcessErrorAsString());
			log.debug("Executing: " + commands+" finished.");
			
	    	
	    } catch (Exception ex) {
	        log.error("SoX could not create the open document file");
	    }
	    log.info("transformAnyToWav end");
	    log.info(f.length());
	    return new DataHandler(new ByteArrayDataSource(getByteArrayFromFile(f),"application/octet-stream"));
	}

	public DataHandler transformWavToOggDH(DataHandler input) {
	    log.info("transformAnyToWav begin ");
	    File f = FileUtils.tempFile("tempout", ".ogg");
	    byte[] raw=null;
	    try {
			
	    FileOutputStream fos = new FileOutputStream(f);
	    log.info(input.getContentType());
	    log.info(raw);
	    log.info(input.getContent().toString());
	    
	    log.info(raw);
	    fos.write(raw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    File inputFile = FileUtils.tempFile( raw , "tempin.audio", ".wav");
	    if (!f.canRead()||!inputFile.exists()||!f.exists() || !f.canWrite()||!inputFile.canRead() || !inputFile.canWrite()) {
			throw new IllegalStateException("Can't read from or write to: "
					+ f.getAbsolutePath());
		}
	    
	    try {
	    	List<String> commands = Arrays.asList(SOX, inputFile.getAbsolutePath(),f.getAbsolutePath());  
	    	File home = new File(SOX_HOME);
			
	    	ProcessRunner pr = new ProcessRunner(commands);
	    	pr.setStartingDir(home);
			log.info("Executing: " + commands);
			pr.run();
	
			log.info("SOX call output: " + pr.getProcessOutputAsString());
			log.error("SOX call error: " + pr.getProcessErrorAsString());
			log.debug("Executing: " + commands+" finished.");
			
	    	
	    } catch (Exception ex) {
	        log.error("SoX could not create the open document file");
	    }
	    log.info("transformAnyToWav end");
	    log.info(f.length());
	    return new DataHandler(new ByteArrayDataSource(getByteArrayFromFile(f),"application/octet-stream"));
	}

	public DataHandler transformWavToFlacDH(DataHandler input) {
        log.info("transformAnyToWav begin ");
        File f = FileUtils.tempFile("tempout", ".flac");
        byte[] raw=null;
        try {
    		
        FileOutputStream fos = new FileOutputStream(f);
        log.info(input.getContentType());
        log.info(raw);
        log.info(input.getContent().toString());
        
        log.info(raw);
        fos.write(raw);
		} catch (IOException e) {
			e.printStackTrace();
		}
        File inputFile = FileUtils.tempFile( raw , "tempin.audio", ".wav");
        if (!f.canRead()||!inputFile.exists()||!f.exists() || !f.canWrite()||!inputFile.canRead() || !inputFile.canWrite()) {
			throw new IllegalStateException("Can't read from or write to: "
					+ f.getAbsolutePath());
		}
        
        try {
        	List<String> commands = Arrays.asList(SOX, inputFile.getAbsolutePath(),f.getAbsolutePath());  
        	File home = new File(SOX_HOME);
    		
        	ProcessRunner pr = new ProcessRunner(commands);
        	pr.setStartingDir(home);
    		log.info("Executing: " + commands);
    		pr.run();

    		log.info("SOX call output: " + pr.getProcessOutputAsString());
    		log.error("SOX call error: " + pr.getProcessErrorAsString());
    		log.debug("Executing: " + commands+" finished.");
    		
        	
        } catch (Exception ex) {
            log.error("SoX could not create the open document file");
        }
        log.info("transformAnyToWav end");
        log.info(f.length());
        return new DataHandler(new ByteArrayDataSource(getByteArrayFromFile(f),"application/octet-stream"));
    }
    
    
    
    
    
    
    
    // FIXME Refactor this into common.
    private static byte[] getByteArrayFromFile(File file) {
        // Create the byte array to hold the data
		byte[] bytes=null;
		try {
			InputStream is = new FileInputStream(file);

			// Get the size of the file
			long length = file.length();

			// You cannot create an array using a long type.
			// It needs to be an int type.
			// Before converting to an int type, check
			// to ensure that file is not larger than Integer.MAX_VALUE.
			if (length > Integer.MAX_VALUE) {
			    // throw new
			    // IllegalArgumentException("getBytesFromFile@JpgToTiffConverter::
			    // The file is too large (i.e. larger than 2 GB!");
			    System.out.println("Datei ist zu gross (e.g. groesser als 2GB)!");
			}

			bytes = new byte[(int) length];

			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
			        && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			    offset += numRead;
			}

			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
			    throw new IOException("Could not completely read file "
			            + file.getName());
			}

			// Close the input stream and return bytes
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
        return bytes;
    }

}
