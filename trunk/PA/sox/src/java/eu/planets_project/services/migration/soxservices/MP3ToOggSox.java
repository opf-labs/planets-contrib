/**
 *  @author : Thomas Krämer thomas.kraemer@uni-koeln.de
 *  created : 14.07.2008
 *  
 */
package eu.planets_project.services.migration.soxservices;

import java.io.File;

import javax.activation.DataHandler;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.MTOM;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;

import eu.planets_project.ifr.core.common.api.PlanetsException;
import eu.planets_project.ifr.core.common.logging.PlanetsLogger;
import eu.planets_project.ifr.core.common.services.PlanetsServices;
import eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary;

/**
 * Convert a file from  mp3 to .ogg.
 * 
 */
@Stateless
@Local(BasicMigrateOneBinary.class)
@Remote(BasicMigrateOneBinary.class)
@LocalBinding(jndiBinding = "planets/MP3ToOggSox")
@RemoteBinding(jndiBinding = "planets-project.eu/MP3ToOggSox")
@WebService(name = "MP3ToOggSox", serviceName = BasicMigrateOneBinary.NAME, targetNamespace = PlanetsServices.NS)
@SOAPBinding( style = SOAPBinding.Style.RPC)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@MTOM(enabled = true, threshold = 0)
public class MP3ToOggSox implements BasicMigrateOneBinary {

	PlanetsLogger log = PlanetsLogger.getLogger(MP3ToOggSox.class);


	@WebMethod(operationName = BasicMigrateOneBinary.NAME, action = PlanetsServices.NS
			+ "/" + BasicMigrateOneBinary.NAME)
	@WebResult(name = BasicMigrateOneBinary.NAME + "Result", targetNamespace = PlanetsServices.NS
			+ "/" + BasicMigrateOneBinary.NAME, partName = BasicMigrateOneBinary.NAME
			+ "Result") 
	public @XmlMimeType(value="application/octet-stream") byte[] basicMigrateOneBinary(
			@WebParam(name = "binary", targetNamespace = PlanetsServices.NS
					+ "/" + BasicMigrateOneBinary.NAME, partName = "binary")
			 @XmlMimeType(value="application/octet-stream")
					byte[] inFile) throws PlanetsException {
		SoxMigrations soxm = null;
		File sourceAudioFile = null;
		soxm = new SoxMigrations();
		return soxm.transformMp3ToOgg(inFile);
	}

	
	@WebMethod(operationName = BasicMigrateOneBinary.NAME+"DH", action = PlanetsServices.NS
			+ "/" + BasicMigrateOneBinary.NAME+"DH")
	@WebResult(name = BasicMigrateOneBinary.NAME+"DH" + "Result", targetNamespace = PlanetsServices.NS
			+ "/" + BasicMigrateOneBinary.NAME, partName = BasicMigrateOneBinary.NAME+"DH"
			+ "Result") 
	public DataHandler basicMigrateOneBinaryDH(
			@WebParam(name = "dataHandler", targetNamespace = PlanetsServices.NS
					+ "/" + BasicMigrateOneBinary.NAME, partName = "dataHandler")
					DataHandler inSrc) throws PlanetsException {
		log.info("basicMigrateOneBinaryDH start");
		SoxMigrations soxm = null;
		File sourceAudioFile = null;
		soxm = new SoxMigrations();

		log.info("basicMigrateOneBinary end");
		return soxm.transformMp3ToOggDH(inSrc);
	}
	
	
	
}
