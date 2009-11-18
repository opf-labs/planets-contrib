package eu.planets_project.services.migration.soxservices.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.ifr.core.techreg.formats.api.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.utils.ProcessRunner;

public class SoXHelper {
	
	private static String SOX_HOME = System.getenv("SOX_HOME");
	
	private static Log log = LogFactory.getLog(SoXHelper.class);
	
	private static List<URI> inputFormats = null;
	private static List<URI> outputFormats = null;
	
	private static String version = null;
	
	private static FormatRegistry fReg = FormatRegistryFactory.getFormatRegistry();
	
	private static final String br = System.getProperty("line.separator");
	
	
	public static List<URI> getSupportedInputFormats() {
		if(inputFormats == null) {
			init();
			return inputFormats;
		}
		else {
			return inputFormats;
		}
		
	}
	
	public static List<URI> getSupportedOutputFormats() {
		if(outputFormats == null) {
			init();
			return outputFormats;
		}
		else {
			return outputFormats;
		}
	}
	
	public static String getVersion () {
		readVersion(); 
		return version;
	}
	
	
	
	private static void init() {
		if(SOX_HOME==null) {
			log.warn("SOX_HOME is not set! Trying to look up SoX on the PATH...");
			SOX_HOME = "";
		}
		if(inputFormats == null && outputFormats == null) { 
			checkFullFormats();
		}
	}
	
	private static void readVersion() {
		ProcessRunner versionCmd = new ProcessRunner();
		versionCmd.setCommand(getVersionCmd());
		if(SOX_HOME!=null && !SOX_HOME.equalsIgnoreCase("")) {
			versionCmd.setStartingDir(new File(SOX_HOME));
		}
		versionCmd.run();
		String output = versionCmd.getProcessOutputAsString();
		String soxPattern = "sox: ";
		String[] parts = output.split(" ", 3);
		
		if(output.contains(soxPattern))
			version = parts[2].trim();
	}
	
	private static List<String> getVersionCmd() {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("sox");
		cmd.add("--version");
		return cmd;
	}
	
	private static void checkFullFormats() {
		inputFormats = new ArrayList<URI>();
		outputFormats = new ArrayList<URI>();
		ProcessRunner checkFormats = new ProcessRunner();
		checkFormats.setCommand(getFullFormatListCmd());
		if(SOX_HOME!=null && !SOX_HOME.equalsIgnoreCase("")) {
			checkFormats.setStartingDir(new File(SOX_HOME));
		}
		checkFormats.run();
		String output = checkFormats.getProcessOutputAsString();
		String[] lines = null;
		
		if(!output.contains(br)) {
			lines = output.split("\n");
		}
		else {
			lines = output.split(br);
		}
		String[] formatParts = null;
		String[] readsParts = null;
		String[] writesParts = null;
		for (String currentLine : lines) {
			
			if(currentLine.contains("Format:")) {
				formatParts = currentLine.split(": ");
				continue;
			}
			if(currentLine.contains("Reads")) {
				readsParts = currentLine.split(": ");
				continue;
			}
			if(currentLine.contains("Writes:")) {
				writesParts = currentLine.split(":", 2);
			}
			if(formatParts!=null && readsParts!=null && writesParts!=null) {
				if(readsParts[1].equalsIgnoreCase("yes")) {
					inputFormats.add(fReg.createExtensionUri(formatParts[1].trim()));
				}
				if(!writesParts[1].equalsIgnoreCase("no")) {
					outputFormats.add(fReg.createExtensionUri(formatParts[1].trim()));
				}
				formatParts = null;
				readsParts = null;
				writesParts = null;
			}
			
		}
		
	}
	
	
//	private static void checkFormats() {
//		ProcessRunner checkFormats = new ProcessRunner();
//		checkFormats.setCommand(getHelpCmd());
//		checkFormats.setStartingDir(new File(SOX_HOME));
//		checkFormats.run();
//		String output = checkFormats.getProcessOutputAsString();
//		String file_formats = "FILE FORMATS: ";
//		String effects = "EFFECTS: ";
//		if(output.contains(file_formats)) {
//			int startIndex = output.indexOf(file_formats) + file_formats.length();
//			String lineSep = "\n";
//			int endIndex = output.indexOf(lineSep, startIndex);
//			output = output.substring(startIndex, endIndex).trim();
//			String[] parts = output.split(" ");
//			inputFormats = new ArrayList<URI>();
//			outputFormats = new ArrayList<URI>();
//			for (String ext : parts) {
//				if(!ext.equalsIgnoreCase("PLAYLIST") 
//						|| !ext.equalsIgnoreCase("FORMATS:")
//						|| !ext.equalsIgnoreCase("AUDIO")
//						|| !ext.equalsIgnoreCase("DEVICE")
//						|| !ext.equalsIgnoreCase("DRIVERS:")) {
//					
//					System.err.println("Adding: " + ext);
//					inputFormats.add(fReg.createExtensionUri(ext));
//					outputFormats.add(fReg.createExtensionUri(ext));
//				}
//			}
//		}
//		else {
//			inputFormats = new ArrayList<URI>();
//			outputFormats = new ArrayList<URI>();
//		}
//	}
	
	public static String getHelpText() {
		ProcessRunner soxHelp = new ProcessRunner(getHelpCmd());
		if(SOX_HOME!=null && !SOX_HOME.equalsIgnoreCase("")) {
			soxHelp.setStartingDir(new File(SOX_HOME));
		}
		soxHelp.run();
		String help = soxHelp.getProcessOutputAsString();
		return help;
	}
	
	
	
	private static List<String> getFullFormatListCmd() {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("sox");
		cmd.add("--help-format");
		cmd.add("all");
		return cmd;
	}
	
	private static List<String> getHelpCmd() {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("sox");
		cmd.add("-h");
		return cmd;
	}

}
