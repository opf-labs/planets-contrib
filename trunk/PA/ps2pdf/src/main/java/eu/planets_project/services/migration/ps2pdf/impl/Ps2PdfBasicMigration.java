package eu.planets_project.services.migration.ps2pdf.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.BindingType;

import org.jboss.annotation.ejb.RemoteBinding;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.services.utils.ProcessRunner;



/**
 * Postscript to PDF basic migration service
 *
 */
@WebService(name = Ps2PdfBasicMigration.NAME, serviceName = BasicMigrateOneBinary.NAME, targetNamespace = PlanetsServices.NS)
@SOAPBinding(
        parameterStyle = SOAPBinding.ParameterStyle.BARE,
        style = SOAPBinding.Style.RPC)
@Stateless
@Remote(BasicMigrateOneBinary.class)
@RemoteBinding(jndiBinding="planets_project.eu/Ps2PdfBasicMigrationServiceRemote")
@BindingType(value="http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public class Ps2PdfBasicMigration implements BasicMigrateOneBinary, Serializable
{
    
	private static final long serialVersionUID = 1878137433497934155L;
	/** The service name */
	public static final String NAME = "Ps2PdfBasicMigration";

	/**
	 * @see eu.planets_project.services.migrate.BasicMigrateOneBinary#basicMigrateOneBinary(byte[])
	 */
	@WebMethod(operationName = BasicMigrateOneBinary.NAME,
	           action = PlanetsServices.NS + "/" + BasicMigrateOneBinary.NAME)
	@WebResult(name = BasicMigrateOneBinary.NAME + "Result",
	           targetNamespace = PlanetsServices.NS + "/" + BasicMigrateOneBinary.NAME,
	           partName = BasicMigrateOneBinary.NAME + "Result")
	public byte[] basicMigrateOneBinary(
	        @WebParam(name = "binary", targetNamespace = PlanetsServices.NS  + "/" + BasicMigrateOneBinary.NAME, partName = "binary")
	        byte[] binary)
	{
		InputStream psfile = new ByteArrayInputStream(binary);
		
		ProcessRunner runner = new ProcessRunner();
		List<String> command = new ArrayList<String>();
		command.add("ps2pdf");
		command.add("-");
		command.add("-");
		
		runner.setCommand(command);
		runner.setInputStream(psfile);
		runner.setCollection(true);
		runner.setOutputCollectionByteSize(-1);
		
		runner.run();
		int return_code = runner.getReturnCode();
		
		if (return_code != 0){
			throw new RuntimeException("Execution failed:" + runner.getProcessErrorAsString());
		}
		InputStream pdfFileStream = runner.getProcessOutput();
		
		byte[] pdffile;
		try {
			pdffile = new byte[pdfFileStream.available()];
			pdfFileStream.read(pdffile);
		} catch (IOException e) {
			throw new RuntimeException("Execution failed:" + runner.getProcessErrorAsString());
		}
		
		return pdffile;
		
	}
}
