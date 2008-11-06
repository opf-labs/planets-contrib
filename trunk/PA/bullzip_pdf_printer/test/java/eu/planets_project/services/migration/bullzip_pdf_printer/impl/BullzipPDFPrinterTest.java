/**
 * 
 */
package eu.planets_project.services.migration.bullzip_pdf_printer.impl;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.Test;

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
	public static String TEST_OUTPUT_FILE_NAME = "bullzip_pdf_test_result.pdf";
	public static String WSDL_LOC = "/pserv-pa-bullzip_pdf_printer/BullzipPDFPrinter?wsdl";
	
	

	/**
	 * Test method for {@link eu.planets_project.services.migration.bullzip_pdf_printer.impl.BullzipPDFPrinter#basicMigrateOneBinary(byte[])}.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws MalformedURLException 
	 * @throws PlanetsException 
	 */
	@Test
	public void testBasicMigrateOneBinary() throws MalformedURLException, InstantiationException, IllegalAccessException, PlanetsException {
		if (!System.getProperty("os.name").contains("Windows")){
            System.out.println("Can't test the message text service on non-Windows platforms;");
            return;
        }
		
		BasicMigrateOneBinary bullzipService = ServiceCreator.createTestService(BasicMigrateOneBinary.QNAME, BullzipPDFPrinter.class, WSDL_LOC);
		
		File testFile = new File("PA/bullzip_pdf_printer/test/resources/eu/planets_project/services/migration/bullzip_pdf_printer/TEST_README.txt");
		
		byte[] testFileBlob = ByteArrayHelper.read(testFile);
		
		byte[] result = bullzipService.basicMigrateOneBinary(testFileBlob);
		
		File outputFolder = FileUtils.createWorkFolderInSysTemp(TEST_OUTPUT_FOLDER);
		
		File resultFile = ByteArrayHelper.writeToDestFile(result, outputFolder.getAbsolutePath() + File.separator + TEST_OUTPUT_FILE_NAME);
		
		System.out.println("Please find the result file here: " + resultFile.getAbsolutePath());
		
	}

}
