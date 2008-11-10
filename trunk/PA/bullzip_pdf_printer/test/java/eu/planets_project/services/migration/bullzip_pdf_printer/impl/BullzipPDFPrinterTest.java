/**
 * 
 */
package eu.planets_project.services.migration.bullzip_pdf_printer.impl;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import eu.planets_project.services.PlanetsException;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * @author melmsp
 *
 */
public class BullzipPDFPrinterTest {
	
	public static String TEST_OUTPUT_FOLDER = "BULLZIP_PDF_TEST_OUT";
	public static String TEST_OUTPUT_FILE_NAME = "bullzip_pdf_test_result";
	public static String WSDL_LOC = "/pserv-pa-bullzip_pdf_printer/BullzipPDFPrinter?wsdl";
	

	/**
	 * Test method for {@link eu.planets_project.services.migration.bullzip_pdf_printer.impl.BullzipPDFPrinter#basicMigrateOneBinary(byte[])}.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws MalformedURLException 
	 * @throws PlanetsException 
	 */
	@Test
	public void testBullzipTxtToPDF() throws MalformedURLException, InstantiationException, IllegalAccessException, PlanetsException {
		if (!System.getProperty("os.name").contains("Windows")){
            System.err.println("Can't test the message text service on non-Windows platforms;");
        }
		
		BasicMigrateOneBinary txtToPDF = ServiceCreator.createTestService(BasicMigrateOneBinary.QNAME, BullzipTxtToPDF.class, WSDL_LOC);
		
		File testFile = new File("PA/bullzip_pdf_printer/test/resources/eu/planets_project/services/migration/bullzip_pdf_printer/TEST_README.txt");
		
		byte[] testFileBlob = ByteArrayHelper.read(testFile);
		
		byte[] result = txtToPDF.basicMigrateOneBinary(testFileBlob);
		
		assertTrue("Conversion successfully finished!!!", result!=null);
		
		File outputFolder = FileUtils.createWorkFolderInSysTemp(TEST_OUTPUT_FOLDER);
		
		File resultFile = ByteArrayHelper.writeToDestFile(result, outputFolder.getAbsolutePath() + File.separator + TEST_OUTPUT_FILE_NAME + "_txt" + ".pdf");
		
		System.out.println("Please find the result file here: " + resultFile.getAbsolutePath());
		
	}
	
	@Test
	public void testBullzipDocToPDF() throws MalformedURLException, InstantiationException, IllegalAccessException, PlanetsException {
		if (!System.getProperty("os.name").contains("Windows")){
            System.out.println("Can't test the message text service on non-Windows platforms;");
        }
		
		BasicMigrateOneBinary docToPDF = ServiceCreator.createTestService(BasicMigrateOneBinary.QNAME, BullzipDocToPDF.class, WSDL_LOC);
		
		File testFile = new File("PA/bullzip_pdf_printer/test/resources/eu/planets_project/services/migration/bullzip_pdf_printer/TEST_README.doc");
		
		byte[] testFileBlob = ByteArrayHelper.read(testFile);
		
		byte[] result = docToPDF.basicMigrateOneBinary(testFileBlob);
		
		assertTrue("Conversion successfully finished!!!", result!=null);
		
		File outputFolder = FileUtils.createWorkFolderInSysTemp(TEST_OUTPUT_FOLDER);
		
		File resultFile = ByteArrayHelper.writeToDestFile(result, outputFolder.getAbsolutePath() + File.separator + TEST_OUTPUT_FILE_NAME + "_doc" + ".pdf");
		
		System.out.println("Please find the result file here: " + resultFile.getAbsolutePath());
		
	}
	
	@Test
	public void testBullzipRtfToPDF() throws MalformedURLException, InstantiationException, IllegalAccessException, PlanetsException {
		if (!System.getProperty("os.name").contains("Windows")){
            System.out.println("Can't test the message text service on non-Windows platforms;");
        }
		
		BasicMigrateOneBinary rtfToPDF = ServiceCreator.createTestService(BasicMigrateOneBinary.QNAME, BullzipRtfToPDF.class, WSDL_LOC);
		
		File testFile = new File("PA/bullzip_pdf_printer/test/resources/eu/planets_project/services/migration/bullzip_pdf_printer/TEST_README.rtf");
		
		byte[] testFileBlob = ByteArrayHelper.read(testFile);
		
		byte[] result = rtfToPDF.basicMigrateOneBinary(testFileBlob);
		
		assertTrue("Conversion successfully finished!!!", result!=null);
		
		File outputFolder = FileUtils.createWorkFolderInSysTemp(TEST_OUTPUT_FOLDER);
		
		File resultFile = ByteArrayHelper.writeToDestFile(result, outputFolder.getAbsolutePath() + File.separator + TEST_OUTPUT_FILE_NAME + "_rtf" + ".pdf");
		
		System.out.println("Please find the result file here: " + resultFile.getAbsolutePath());
		
	}
	
	@Test
	public void testBullzipOdtToPDF() throws MalformedURLException, InstantiationException, IllegalAccessException, PlanetsException {
		if (!System.getProperty("os.name").contains("Windows")){
            System.out.println("Can't test the message text service on non-Windows platforms;");
        }
		
		BasicMigrateOneBinary odtToPDF = ServiceCreator.createTestService(BasicMigrateOneBinary.QNAME, BullzipOdtToPDF.class, WSDL_LOC);
		
		File testFile = new File("PA/bullzip_pdf_printer/test/resources/eu/planets_project/services/migration/bullzip_pdf_printer/TEST_README.odt");
		
		byte[] testFileBlob = ByteArrayHelper.read(testFile);
		
		byte[] result = odtToPDF.basicMigrateOneBinary(testFileBlob);
		
		assertTrue("Conversion successfully finished!!!", result!=null);
		
		File outputFolder = FileUtils.createWorkFolderInSysTemp(TEST_OUTPUT_FOLDER);
		
		File resultFile = ByteArrayHelper.writeToDestFile(result, outputFolder.getAbsolutePath() + File.separator + TEST_OUTPUT_FILE_NAME + "_odt" + ".pdf");
		
		System.out.println("Please find the result file here: " + resultFile.getAbsolutePath());
		
	}

}
