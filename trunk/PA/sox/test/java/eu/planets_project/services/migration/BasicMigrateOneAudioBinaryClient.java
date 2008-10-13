package eu.planets_project.services.migration;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.hibernate.validator.AssertFalse;
import static org.junit.Assert.*;
import org.junit.Test;

import eu.planets_project.services.PlanetsException;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.services.migration.soxservices.MP3ToFlacSox;
import eu.planets_project.services.migration.soxservices.MP3ToOggSox;
import eu.planets_project.services.migration.soxservices.MP3ToWavSox;
import eu.planets_project.services.migration.soxservices.WavToFlacSox;
import eu.planets_project.services.migration.soxservices.WavToOggSox;

public class BasicMigrateOneAudioBinaryClient {

	public void test(BasicMigrateOneBinary converter, String srcFormat,
			String destformat) {
		try {
			System.out.println("Sourceformat: " + srcFormat);
			System.out.println("Targetformat: " + destformat);
			String fileName = "/var/www/ab" + srcFormat;
			DataSource ds = new FileDataSource(fileName);
			DataHandler dataHandler = new DataHandler(ds);

			File srcFile = new File(fileName);
			if (srcFile.exists() && srcFile.canRead()) {
				System.out.println("OK.");
				System.out.println(srcFile.getAbsolutePath());
			}
			System.out.println("creating Byte[]");
			byte[] imageData = getByteArrayFromFile(srcFile);
			System.out.println("Sending audio data...");
			System.out.println(imageData.length + " Byte");
			System.out.println(converter.QNAME + " Class: "
					+ converter.getClass().getName());
			byte[] resdh_orig = converter.basicMigrateOneBinary(imageData);

			System.out.println("Service executed...");
			System.out.println("Byte [] decoded...");
			File testdir = new File(System.getProperty("user.home")
					+ System.getProperty("file.separator") + "soundconversion");
			if (!testdir.exists())
				testdir.mkdir();
			File resultFile = new File(testdir, "converted" + destformat);
			if (!resultFile.exists()) {
				resultFile.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(resultFile);
			fos.write(resdh_orig);
			fos.flush();
			fos.close();
			System.out.println(resultFile.getAbsolutePath() + " has "
					+ resultFile.length() + "  bytes.");
			assertFalse("Result file is empty",resultFile.length()==0);
		} catch (MalformedURLException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {

			fail(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {

			fail(e.getMessage());
			e.printStackTrace();
		} catch (PlanetsException e) {

			fail(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * 23.07.2008 13:31:10
	 */
	@Test
	public void localTests() {
		System.out.println("**********************************************");

		test(new MP3ToWavSox(), ".mp3", "local.wav");
		System.out.println("**********************************************");

		test(new MP3ToOggSox(), ".mp3", "local.ogg");
		System.out.println("**********************************************");

		test(new MP3ToFlacSox(), ".mp3", "local.flac");
		System.out.println("**********************************************");

		test(new WavToFlacSox(), ".wav", "local2.flac");
		System.out.println("**********************************************");

		test(new WavToOggSox(), ".wav", "local2.ogg");
		System.out.println("**********************************************");

	}

	@Test
	public void clientTest() {
		try {
			Service service = null;
			BasicMigrateOneBinary servicePort = null;

			System.out
					.println("**********************************************");

			service = Service.create(new URL(
					"http://localhost:8080/pserv-pa-sox/MP3ToOggSox?wsdl"),
					new QName(PlanetsServices.NS, BasicMigrateOneBinary.NAME));
			servicePort = service.getPort(BasicMigrateOneBinary.class);
			test(servicePort, ".mp3", ".ogg");

			System.out
					.println("**********************************************");

			service = Service.create(new URL(
					"http://localhost:8080/pserv-pa-sox/MP3ToWavSox?wsdl"),
					new QName(PlanetsServices.NS, BasicMigrateOneBinary.NAME));
			servicePort = service.getPort(BasicMigrateOneBinary.class);
			test(servicePort, ".mp3", ".wav");

			System.out
					.println("**********************************************");

			service = Service.create(new URL(
					"http://localhost:8080/pserv-pa-sox/MP3ToFlacSox?wsdl"),
					new QName(PlanetsServices.NS, BasicMigrateOneBinary.NAME));
			servicePort = service.getPort(BasicMigrateOneBinary.class);
			test(servicePort, ".mp3", ".flac");

			System.out
					.println("**********************************************");

			service = Service.create(new URL(
					"http://localhost:8080/pserv-pa-sox/WavToOggSox?wsdl"),
					new QName(PlanetsServices.NS, BasicMigrateOneBinary.NAME));
			servicePort = service.getPort(BasicMigrateOneBinary.class);
			test(servicePort, ".wav", "2.ogg");

			System.out
					.println("**********************************************");

			service = Service.create(new URL(
					"http://localhost:8080/pserv-pa-sox/WavToFlacSox?wsdl"),
					new QName(PlanetsServices.NS, BasicMigrateOneBinary.NAME));
			servicePort = service.getPort(BasicMigrateOneBinary.class);
			test(servicePort, ".wav", "2.flac");

		} catch (IOException e) {
			fail(e.getMessage());
		}

	}

	private static byte[] getByteArrayFromFile(File file) throws IOException {
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

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

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
		return bytes;
	}

}
