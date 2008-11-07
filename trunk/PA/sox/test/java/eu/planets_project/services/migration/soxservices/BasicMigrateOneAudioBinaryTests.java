package eu.planets_project.services.migration.soxservices;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

import org.junit.Test;

import eu.planets_project.services.PlanetsException;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.services.utils.test.ServiceCreator;

public class BasicMigrateOneAudioBinaryTests {

    public void test(BasicMigrateOneBinary converter, String srcFormat,
            String destformat) {

        try {
            System.out.println("Sourceformat: " + srcFormat);

            System.out.println("Targetformat: " + destformat);

            String fileName = "PA/sox/test/resources/input" + srcFormat;

            DataSource ds = new FileDataSource(fileName);
            DataHandler dataHandler = new DataHandler(ds);

            File srcFile = new File(fileName);

            if (srcFile.exists() && srcFile.canRead()) {
                System.out.println("OK.");
                System.out.println(srcFile.getAbsolutePath());
            }

            System.out.println("creating Byte[]");

            byte[] imageData = getByteArrayFromFile(srcFile);

            System.out.println("data byta array has length: "
                    + imageData.length);
            System.out.println("Sending audio data...");
            System.out.println(imageData.length + " Byte");
            System.out.println(converter.QNAME + " Class: "
                    + converter.getClass().getName());

            byte[] resdh_orig = converter.basicMigrateOneBinary(imageData);

            // assertTrue("Sox returned null data;", resdh_orig != null);

            File resultFile = null;

            if (resdh_orig != null) {
                System.out.println("SUCCESS!!!");
                System.out
                        .println("Service executed, resulting byte array has length: "
                                + resdh_orig.length);
                System.out.println("Byte [] decoded...");

                File testdir = new File(System.getProperty("user.home")
                        + System.getProperty("file.separator")
                        + "soundconversion");

                if (!testdir.exists())
                    testdir.mkdir();

                resultFile = new File(testdir, "converted" + destformat);

                if (!resultFile.exists()) {
                    resultFile.createNewFile();
                }

                FileOutputStream fos = new FileOutputStream(resultFile);
                fos.write(resdh_orig);
                fos.flush();
                fos.close();

                System.out.println(resultFile.getAbsolutePath() + " has "
                        + resultFile.length() + "  bytes.");
            }

            if (srcFormat.equalsIgnoreCase(".mp3")
                    | destformat.equalsIgnoreCase(".mp3")) {
                if (resdh_orig == null) {
                    System.err.println("WARNING: SoX returned NULL as result!");
                    System.err
                            .println("To use SoX for mp3 conversion, you need to have an additional external"
                                    + "\n mp3-Converter/library installed (e.g. LAME)");
                    assertTrue("mp3-Conversion-Test skipped", (srcFormat
                            .equalsIgnoreCase(".mp3") || destformat
                            .equalsIgnoreCase(".mp3")));
                }
            }

            if (srcFormat.equalsIgnoreCase(".ogg")
                    | destformat.equalsIgnoreCase(".ogg")) {
                if (resdh_orig == null) {
                    System.err.println("WARNING: SoX returned NULL as result!");
                    System.err
                            .println("To use SoX for \"ogg\" conversion, you need to have an additional external"
                                    + "\n OggVorbis-Converter/library installed!");
                    assertTrue("ogg-Conversion-Test skipped", (srcFormat
                            .equalsIgnoreCase(".ogg") || destformat
                            .equalsIgnoreCase(".ogg")));
                }
            }
            if (srcFormat.equalsIgnoreCase(".flac")
                    | destformat.equalsIgnoreCase(".flac")) {
                if (resdh_orig == null) {
                    System.err.println("WARNING: SoX returned NULL as result!");
                    System.err
                            .println("To use SoX for \"flac\" conversion, you need to have an additional external"
                                    + "\n Flac-Converter/library installed!");
                    assertTrue("Flac-Conversion-Test skipped", (srcFormat
                            .equalsIgnoreCase(".flac") || destformat
                            .equalsIgnoreCase(".flac")));
                }
            }

        } catch (MalformedURLException e) {
            fail(e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void localTestsMp3ToWav() {
        System.out.println("**********************************************");
        test(new MP3ToWavSox(), ".mp3", ".wav");
    }

    @Test
    public void localTestsMp3ToOgg() {
        System.out.println("**********************************************");
        test(new MP3ToOggSox(), ".mp3", ".ogg");
    }

    @Test
    public void localTestsMp3ToFlac() {
        System.out.println("**********************************************");
        test(new MP3ToFlacSox(), ".mp3", ".flac");
    }

    @Test
    public void localTestsWavToAiff() {
        System.out.println("**********************************************");
        test(new WavToAiffSox(), ".wav", ".aiff");
    }

    @Test
    public void localTestsWavToFlac() {
        System.out.println("**********************************************");
        test(new WavToFlacSox(), ".wav", ".flac");
    }

    @Test
    public void localTestsWavToOgg() {
        System.out.println("**********************************************");
        test(new WavToOggSox(), ".wav", ".ogg");
    }

    @Test
    public void clientTestMp3ToOgg() {
        System.out.println("**********************************************");
        BasicMigrateOneBinary migrate = ServiceCreator.createTestService(
                BasicMigrateOneBinary.QNAME, MP3ToOggSox.class,
                "/pserv-pa-sox/MP3ToOggSox?wsdl");
        test(migrate, ".mp3", ".ogg");
    }

    @Test
    public void clientTestMp3ToWav() {
        System.out.println("**********************************************");
        BasicMigrateOneBinary migrate = ServiceCreator.createTestService(
                BasicMigrateOneBinary.QNAME, MP3ToWavSox.class,
                "/pserv-pa-sox/MP3ToWavSox?wsdl");
        test(migrate, ".mp3", ".wav");
    }

    @Test
    public void clientTestMp3ToFlac() {
        System.out.println("**********************************************");
        BasicMigrateOneBinary migrate = ServiceCreator.createTestService(
                BasicMigrateOneBinary.QNAME, MP3ToFlacSox.class,
                "/pserv-pa-sox/MP3ToFlacSox?wsdl");
        test(migrate, ".mp3", ".flac");
    }

    @Test
    public void clientTestWavToOgg() {
        System.out.println("**********************************************");
        BasicMigrateOneBinary migrate = ServiceCreator.createTestService(
                BasicMigrateOneBinary.QNAME, WavToOggSox.class,
                "/pserv-pa-sox/WavToOggSox?wsdl");
        test(migrate, ".wav", ".ogg");
    }

    @Test
    public void clientTestWavToFlac() {
        System.out.println("**********************************************");
        BasicMigrateOneBinary migrate = ServiceCreator.createTestService(
                BasicMigrateOneBinary.QNAME, WavToFlacSox.class,
                "/pserv-pa-sox/WavToFlacSox?wsdl");
        test(migrate, ".wav", ".flac");
    }

    @Test
    public void clientTestWavToAiff() {
        System.out.println("**********************************************");
        BasicMigrateOneBinary migrate = ServiceCreator.createTestService(
                BasicMigrateOneBinary.QNAME, WavToAiffSox.class,
                "/pserv-pa-sox/WavToAiffSox?wsdl");
        test(migrate, ".wav", ".aiff");
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
