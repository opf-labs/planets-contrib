package eu.planets_project.services.migration.graphicsmagick.utils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.ProcessRunner;

/**
 * @author melmsp
 *
 */
public class CoreGraphicsMagick {
	
	private static Log log = LogFactory.getLog(CoreGraphicsMagick.class);
	
	private static final FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
	
	private static List<URI> inFormats = null;
	private static List<URI> outFormats = null;
	
	public GraphicsMagickResult convert(File inputFile, File outputFolder, String outFormatExtension, String compressionType, int imageQuality) {
		String outFileName = FileUtils.getOutputFileNameFor(inputFile.getName(), outFormatExtension);
		File outputFile = new File(outputFolder, outFileName);
		ProcessRunner convert = new ProcessRunner(getConvertCmd(inputFile, outputFile, compressionType, imageQuality));
		convert.run();
		String error = convert.getProcessErrorAsString();
		String out = convert.getProcessOutputAsString();
		GraphicsMagickResult result = new GraphicsMagickResult();
		if(migrationSuccessful(outputFile)) {
			result.setResultFile(outputFile);
			result.setErrorMsg(error);
			result.setOutputMsg(out);
			result.setFormatExtension(identify(outputFile).getFormatExtension());
			return result;
		}
		else {
			result.setErrorMsg(error);
			result.setOutputMsg(out);
			return result;
		}
	}
	
	public boolean inputFormatIsValid(File inputFile, URI guessedInputURI) {
		String inputExt = identify(inputFile).getFormatExtension(); 
		String guessedInputExt = format.getFirstExtension(guessedInputURI); 
		return extensionsAreEqual(inputExt, guessedInputExt);
	}
	
	
	public GraphicsMagickResult identify(File inputFile) {
		ProcessRunner identify = new ProcessRunner(getIdentifyCmd(inputFile));
		identify.run();
		String out = identify.getProcessOutputAsString();
		String error = identify.getProcessErrorAsString();
		
		GraphicsMagickResult result = new GraphicsMagickResult();
		
		if(out!=null && out.length()>0) {
			String[] parts = out.split(" ", 3);
			String ext = parts[1];
			result.setFormatExtension(ext);
			result.setOutputMsg(out);
			return result;
		}
		else {
			result.setErrorMsg(error);
			return result;
		}
	}
	
	public static List<URI> getSupportedInputFormats() {
		if(inFormats == null || outFormats == null) {
			init();
		}
		return inFormats;
	}
	
	public static List<URI> getSupportedOutputFormats() {
		if(inFormats == null || outFormats == null) {
			init();
		}
		return outFormats;
	}
	
	private List<String> getConvertCmd(File inputFile, File outputFile, String compressionType, int imageQuality) {
		List<String> command = new ArrayList<String>();
		command.add("gm");
		command.add("convert");
		command.add(inputFile.getAbsolutePath());
		command.add("-compress");
		command.add(compressionType);
		command.add("-quality");
		command.add(Integer.toString(imageQuality));
		command.add(outputFile.getAbsolutePath());
		return command;
	}

	private List<String> getIdentifyCmd(File inputFile) {
		List<String> command = new ArrayList<String>();
		command.add("gm");
		command.add("identify");
		command.add(inputFile.getAbsolutePath());
		return command;
	}

	private static void init() {
		log.info("[" + CoreGraphicsMagick.class.getName() + "] " + "Initializing GraphicsMagick format tables." + System.getProperty("line.separator") +  "Checking supported formats and installed libraries...will be back soon, please hang on!");
		ProcessRunner graphicsMagick = new ProcessRunner();
		
		graphicsMagick.setCommand(getListCommand());
		graphicsMagick.run();
		String output = graphicsMagick.getProcessOutputAsString();
		output = output.replace("*", "").trim().replaceAll("[ ]+", " ").trim();
		StringTokenizer tokenizer = new StringTokenizer(output, System.getProperty("line.separator"));
		List<String> lines = new ArrayList<String>();
		
		while(tokenizer.hasMoreTokens()) {
			String currentLine = tokenizer.nextToken();
			if(currentLine.matches("[ ]?[A-Za-z0-9]+ [A-Za-z0-9]+ [rw\\-+]{3}.+?")) {
				lines.add(currentLine.trim());
			}
		}
		
		String[] splitted = null;
		TreeSet<URI> inputFormats = new TreeSet<URI>();
		TreeSet<URI> outputFormats = new TreeSet<URI>();
		for (String currentString : lines) {
			splitted = currentString.split(" ", 4);
			if(splitted[2].contains("r")) {
				if(splitted[0].equalsIgnoreCase("TIFF")) {
					inputFormats.add(format.createExtensionUri("TIF"));
				}
				if(splitted[0].equalsIgnoreCase("JPEG")) {
					inputFormats.add(format.createExtensionUri("JPG"));
				}
				inputFormats.add(format.createExtensionUri(splitted[0]));
			}
			if(splitted[2].contains("w")) {
				if(splitted[0].equalsIgnoreCase("TIFF")) {
					outputFormats.add(format.createExtensionUri("TIF"));
				}
				if(splitted[0].equalsIgnoreCase("JPEG")) {
					outputFormats.add(format.createExtensionUri("JPG"));
				}
				outputFormats.add(format.createExtensionUri(splitted[0]));
			}
		}
		inFormats = new ArrayList<URI>();
		outFormats = new ArrayList<URI>();
		inFormats.addAll(inputFormats);
		outFormats.addAll(outputFormats);
	}

	private static List<String> getListCommand() {
		List<String> commands = new ArrayList<String>();
		commands.add("gm");
		commands.add("convert");
		commands.add("-list");
		commands.add("format");
		return commands;
	}

	private boolean extensionsAreEqual(String extension1, String extension2) {
	    	if(extension1.contains(".")) {
	    		extension1 = extension1.replace(".", "");
	    	}
	    	if(extension2.contains(".")) {
	    		extension2 = extension2.replace(".", "");
	    	}
	//        plogger.info("Starting to compare these two extensions: " + extension1 + " and " + extension2);
	
	        Set <URI> ext1FormatURIs = format.getUrisForExtension(extension1.toLowerCase());
	//        plogger.info("Got list of URIs for " + extension1);
	
	        Set <URI> ext2FormatURIs = format.getUrisForExtension(extension2.toLowerCase());
	//        plogger.info("Got list of URIs for " + extension2);
	
	        boolean success = false;
	        
	        if(ext1FormatURIs==null || ext2FormatURIs==null) {
	        	if(extension1.equalsIgnoreCase(extension2)) {
	        		return true;
	        	}
	        	else {
	        		return false;
	        	}
	        }
	
	//        plogger.info("Trying to match URIs...");
	        for(URI currentUri: ext1FormatURIs) {
	//            plogger.info("current URI: " + currentUri.toASCIIString());
	            if(ext2FormatURIs.contains(currentUri)) {
	                success = true;
	                break;
	            }
	            else {
	                success = false;
	            }
	        }
	        if(success) {
	            log.info("Success! Actual format of input file verified!");
	        }
	        else {
	        	log.error("Error! File has a different format than it claims it has!");
	        }
	
	        return success;
	    }

	private boolean migrationSuccessful(File resultFile) {
		if(resultFile.exists() && resultFile.length() > 0) {
			return true;
		}
		else {
			return false;
		}
	}
}
