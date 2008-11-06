package eu.planets_project.services.migration.bullzip_pdf_printer.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.MTOM;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;

import eu.planets_project.services.PlanetsException;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;

@Stateless()
@Local(BasicMigrateOneBinary.class)
@Remote(BasicMigrateOneBinary.class)
@LocalBinding(jndiBinding = "planets/BullzipPDFPrinter")
@RemoteBinding(jndiBinding = "planets-project.eu/BullzipPDFPrinter")
@WebService(
        name = "BullzipPDFPrinter", 
//	      This is not appropriate when using the endpointInterface approach.
        serviceName = BasicMigrateOneBinary.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.BasicMigrateOneBinary")
@SOAPBinding(
        parameterStyle = SOAPBinding.ParameterStyle.BARE,
        style = SOAPBinding.Style.RPC)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@MTOM
public class BullzipPDFPrinter implements BasicMigrateOneBinary, Serializable {
		
	private static final long serialVersionUID = 224209425300118923L;
	private static String BULLZIP_PDF_HOME = System.getenv("BULLZIP_PDF_HOME") + File.separator;
	private static String SYSTEM_TEMP = System.getProperty("java.io.tmpdir") + File.separator;
	private static String BULLZIP_PDF_WORK = "BULLZIP_PDF";
	private static String BULLZIP_PDF_OUTPUT_DIR = "output";
	private static String BULLZIP_PDF_IN = "INPUT";
	private static String BULLZIP_PDF_SRC_FILE_NAME = "bullzip_pdf_src.bin";
	private static String BULLZIP_PDF_OUTPUT_FILE_NAME = "bullzip_pdf_result.pdf";
	private static String BULLZIP_CMD = "printto";
	private static String BULLZIP_PRINTER_NAME = "Bullzip PDF Printer";
	
	
	// Creating a PlanetsLogger...
	private PlanetsLogger plogger = PlanetsLogger.getLogger(this.getClass());
	
	

	/** 
	 *  @param binary a byte[] with the input file
	 *  @return a byte[] which contains the created .pdf file
	 */
	
	@WebMethod(
            operationName = BasicMigrateOneBinary.NAME, 
            action = PlanetsServices.NS + "/" + BasicMigrateOneBinary.NAME)
    @WebResult(
            name = BasicMigrateOneBinary.NAME+"Result", 
            targetNamespace = PlanetsServices.NS + "/" + BasicMigrateOneBinary.NAME, 
            partName = BasicMigrateOneBinary.NAME + "Result")
    public byte[] basicMigrateOneBinary ( 
            @WebParam(name = "binary", targetNamespace = PlanetsServices.NS + "/" + BasicMigrateOneBinary.NAME, partName = "binary")
            byte[] binary ) throws PlanetsException {
		
		if(BULLZIP_PDF_HOME==null){
    		System.err.println("BULLZIP_PDF_HOME is not set! Please create an system variable\n" +
    				"and point it to the BullzipPDFPrinter installation folder!");
    		plogger.error("BULLZIP_PDF_HOME is not set! Please create an system variable\n" +
    				"and point it to the BullzipPDFPrinter installation folder!");
    	}
		
		if(BULLZIP_PDF_HOME.endsWith(File.separator + File.separator)){
			BULLZIP_PDF_HOME = BULLZIP_PDF_HOME.replace(File.separator + File.separator, File.separator);
		}
		
		
		plogger.info("Starting BullzipPDFPrinter Service...");
		ProcessRunner shell = new ProcessRunner();
		List <String> bullzipPDF_arguments = null;
		File srcFile = null;
		File workFolder = null;
		File inFolder = null;
		File bullzip_pdf_result_file = null;
		byte[] output = null;
		
		try {
			plogger.info("SYSTEM_TEMP is: " + SYSTEM_TEMP);
			
			// Creating the Work folder...
			workFolder = FileUtils.createWorkFolderInSysTemp(BULLZIP_PDF_WORK);
			plogger.info("Work folder created: " + workFolder.getAbsolutePath());
			
			
			// Creating the IN folder
			inFolder = FileUtils.createFolderInWorkFolder(workFolder, BULLZIP_PDF_IN);
			plogger.info("Input folder created: " + inFolder.getAbsolutePath());
			
			// Creating the  source file...
			srcFile = new File(inFolder, BULLZIP_PDF_SRC_FILE_NAME);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(srcFile));
			out.write(binary);
			out.flush();
			out.close();
			plogger.info("Temp source file created: " + srcFile.getAbsolutePath());
			
			// Configuring the commandline
			plogger.info("Configuring command line arguments...");
			bullzipPDF_arguments = new ArrayList <String>();
			// adding the tool-path
			bullzipPDF_arguments.add(BULLZIP_PDF_HOME + BULLZIP_CMD);
			// adding the source file name and path
			bullzipPDF_arguments.add(srcFile.getAbsolutePath());
			// adding the name of the Bullzip-PDF Printer device 
			bullzipPDF_arguments.add("\"" + BULLZIP_PRINTER_NAME + "\"");
			
			// creating logger output for commandline:
			String line = "";
			for (String argument : bullzipPDF_arguments) {
				line = line + argument + " "; 
			}
			
			// Setting the command for ProcessRunner
			plogger.info("Setting command to: \n" + line);
			shell.setCommand(bullzipPDF_arguments);
			
			// setting the start directory
			shell.setStartingDir(new File(BULLZIP_PDF_HOME));
			plogger.info("Setting starting Dir to: " + BULLZIP_PDF_HOME);
			plogger.info("Running Bullzip PDF Printer Tool...");
			shell.run();
			
			// Getting the process outputs and errors and logging them out 
			String processOutput = shell.getProcessOutputAsString();
			String processError = shell.getProcessErrorAsString();
			plogger.info("Process Output: " + processOutput);
			plogger.info("Process Error: " + processError);
			
			// Pointing to the RESULT file
			bullzip_pdf_result_file = new File(BULLZIP_PDF_HOME + BULLZIP_PDF_OUTPUT_DIR, BULLZIP_PDF_OUTPUT_FILE_NAME);
			
			output = ByteArrayHelper.read(bullzip_pdf_result_file);
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Cleaning up all created Temp-Files:
		FileUtils.deleteTempFiles(workFolder, plogger);
		
		return output;
	}
}

