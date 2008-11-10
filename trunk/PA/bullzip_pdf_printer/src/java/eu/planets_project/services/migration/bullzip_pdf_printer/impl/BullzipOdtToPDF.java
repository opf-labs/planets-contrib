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
@LocalBinding(jndiBinding = "planets/BullzipOdtToPDF")
@RemoteBinding(jndiBinding = "planets-project.eu/BullzipOdtToPDF")
@WebService(
        name = "BullzipOdtToPDF", 
        serviceName = BasicMigrateOneBinary.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.BasicMigrateOneBinary")
@SOAPBinding(
        parameterStyle = SOAPBinding.ParameterStyle.BARE,
        style = SOAPBinding.Style.RPC)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@MTOM
public class BullzipOdtToPDF implements BasicMigrateOneBinary, Serializable {
		
	private static final long serialVersionUID = 4418470348143999391L;
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
        byte[] binary ) {
		
		BullzipPDFPrinter pdfPrinter = new BullzipPDFPrinter(plogger);
		
		plogger.info("Starting BullzipOdtToPDF Service...");
		return pdfPrinter.printOdtToPDF(binary);
	}
}

