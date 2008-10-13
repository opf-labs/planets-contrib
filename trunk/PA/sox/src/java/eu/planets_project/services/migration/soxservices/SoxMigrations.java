package eu.planets_project.services.migration.soxservices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

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

	public static String SOX_HOME;

	public SoxMigrations() {
		Properties props = new Properties();
		try {
			props
					.load(this
							.getClass()
							.getResourceAsStream(
									"/eu/planets_project/services/migration/soxservices/sox.properties"));
			SOX_HOME = props.getProperty("sox.bin.dir");

		} catch (IOException e) {
			// Use a default for now.
			SOX_HOME = "/usr/bin/";
		}
		log.info("Pointed to sox in: " + SOX_HOME);
	}

	public byte[] transformMp3ToOgg(byte[] input) {
		log.info("transformMp3ToOgg begin ");
		return genericTransformAudioSrcToAudioDest(input, ".mp3", ".ogg", null);
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
	
	
	
	public DataHandler transformMp3ToOggDH(DataHandler input) {
		log.info("transformMp3ToOggDH begin ");
		return genericTransformAudioSrcToAudioDestDH(input, ".mp3", ".ogg", null);
	}

	public  DataHandler transformMp3ToWavDH( DataHandler input) {
		log.info("transformMp3ToWavDH begin ");
		return genericTransformAudioSrcToAudioDestDH(input, ".mp3", ".wav", null);
	}

	public  DataHandler transformWavToOggDH( DataHandler input) {
		log.info("transformWavToOggDH begin ");
		return genericTransformAudioSrcToAudioDestDH(input, ".wav", ".ogg", null);
	}

	public  DataHandler transformWavToFlacDH( DataHandler input) {
		log.info("transformWavToFlacDH begin ");
		return genericTransformAudioSrcToAudioDestDH(input, ".wav", ".flac", null);
	}

	public  DataHandler transformMp3ToFlacDH( DataHandler input) {
		log.info("transformMp3ToFlacDH begin ");
		return genericTransformAudioSrcToAudioDestDH(input, ".mp3", ".flac", null);
	}
	

	public byte[] genericTransformAudioSrcToAudioDest(byte[] input,
			String srcSuffix, String destSuffix, ArrayList<String> soxCliParams) {
	
		if (!srcSuffix.startsWith("."))
			srcSuffix = "." + srcSuffix;
		if (!destSuffix.startsWith("."))
			destSuffix = "." + destSuffix;
		log.info("genericTransformAudioSrcToAudioDest begin: Converting from "
				+ srcSuffix + " to " + destSuffix);
		File f = FileUtils.tempFile("tempout", destSuffix);
	
		File inputFile = FileUtils.tempFile(input, "tempin.audio", srcSuffix);
		if (!f.canRead() || !inputFile.exists() || !f.exists() || !f.canWrite()
				|| !inputFile.canRead() || !inputFile.canWrite()) {
			throw new IllegalStateException("Can't read from or write to: "
					+ f.getAbsolutePath());
		}
	
		try {
			List<String> commands = Arrays.asList(SOX, inputFile
					.getAbsolutePath(), f.getAbsolutePath());
			commands.addAll(soxCliParams);
			File home = new File(SOX_HOME);
	
			ProcessRunner pr = new ProcessRunner(commands);
			pr.setStartingDir(home);
			log.info("Executing: " + commands);
			pr.run();
	
			log.info("SOX call output: " + pr.getProcessOutputAsString());
			log.error("SOX call error: " + pr.getProcessErrorAsString());
			log.debug("Executing: " + commands + " finished.");
	
		} catch (Exception ex) {
			log.error("SoX could not create the open document file");
		}
		log.info("genericTransformAudioSrcToAudioDest end");
		log.info(f.length());
	
		return ByteArrayHelper.read(f);
	}

	public DataHandler genericTransformAudioSrcToAudioDestDH(DataHandler input,
			String srcSuffix, String destSuffix, ArrayList<String> soxCliParams) {
		log
				.info("genericTransformAudioSrcToAudioDestDH begin : Converting from "
						+ srcSuffix + " to " + destSuffix);
		File f = FileUtils.tempFile("tempout.audio", destSuffix);
		byte[] raw = null;
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
		File inputFile = FileUtils.tempFile(raw, "tempin.audio", srcSuffix);
		if (!f.canRead() || !inputFile.exists() || !f.exists() || !f.canWrite()
				|| !inputFile.canRead() || !inputFile.canWrite()) {
			throw new IllegalStateException("Can't read from or write to: "
					+ f.getAbsolutePath());
		}
		try {
			List<String> commands = Arrays.asList(SOX, inputFile
					.getAbsolutePath(), f.getAbsolutePath());
			File home = new File(SOX_HOME);
			commands.addAll(soxCliParams);
			ProcessRunner pr = new ProcessRunner(commands);
			pr.setStartingDir(home);
			log.info("Executing: " + commands);
			pr.run();
			log.info("SOX call output: " + pr.getProcessOutputAsString());
			log.error("SOX call error: " + pr.getProcessErrorAsString());
			log.debug("Executing: " + commands + " finished.");
		} catch (Exception ex) {
			log.error("SoX could not create the open document file");
		}
		log.info("genericTransformAudioSrcToAudioDestDH end");
		log.info(f.length());
		return new DataHandler(new ByteArrayDataSource(ByteArrayHelper.read(f),
				"application/octet-stream"));
	}

}
