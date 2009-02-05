package eu.planets_project.services.migration.soxservices;

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

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.services.utils.PlanetsLogger;

/**
 * @deprecated Use {@link SoX} instead.
 */

/**
 * Convert a file from  mp3 to .wav.
 *  @author : Thomas Kraemer thomas.kraemer@uni-koeln.de
 *  created : 14.07.2008
 */
@Stateless
@Local(BasicMigrateOneBinary.class)
@Remote(BasicMigrateOneBinary.class)
@LocalBinding(jndiBinding = "planets/MP3ToWavSox")
@RemoteBinding(jndiBinding = "planets-project.eu/MP3ToWavSox")
@WebService(name = "MP3ToWavSox", 
		 	serviceName = BasicMigrateOneBinary.NAME, 
		 	targetNamespace = PlanetsServices.NS,
		 	endpointInterface = "eu.planets_project.services.migrate.BasicMigrateOneBinary")
@SOAPBinding( style = SOAPBinding.Style.RPC)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@MTOM(enabled = true, threshold = 0)
@Deprecated
public class MP3ToWavSox implements BasicMigrateOneBinary {

	PlanetsLogger log = PlanetsLogger.getLogger(MP3ToWavSox.class);


	/**
	 * @see eu.planets_project.services.migrate.BasicMigrateOneBinary#basicMigrateOneBinary(byte[])
	 */
	@WebMethod(operationName = BasicMigrateOneBinary.NAME, action = PlanetsServices.NS
			+ "/" + BasicMigrateOneBinary.NAME)
	@WebResult(name = BasicMigrateOneBinary.NAME + "Result", targetNamespace = PlanetsServices.NS
			+ "/" + BasicMigrateOneBinary.NAME, partName = BasicMigrateOneBinary.NAME
			+ "Result") 
	public  byte[] basicMigrateOneBinary(
			@WebParam(name = "binary", targetNamespace = PlanetsServices.NS
					+ "/" + BasicMigrateOneBinary.NAME, partName = "binary")
			 
					byte[] binary) {
		log.info("basicMigrateOneBinary start");
		SoxMigrations soxm = null;
		soxm = new SoxMigrations();

		log.info("basicMigrateOneBinary end");
		return soxm.transformMp3ToWav(binary);
	}

	
//	@WebMethod(operationName = BasicMigrateOneBinary.NAME+"DH", action = PlanetsServices.NS
//			+ "/" + BasicMigrateOneBinary.NAME+"DH")
//	@WebResult(name = BasicMigrateOneBinary.NAME+"DH" + "Result", targetNamespace = PlanetsServices.NS
//			+ "/" + BasicMigrateOneBinary.NAME, partName = BasicMigrateOneBinary.NAME+"DH"
//			+ "Result") 
//	public DataHandler basicMigrateOneBinaryDH(
//			@WebParam(name = "dataHandler", targetNamespace = PlanetsServices.NS
//					+ "/" + BasicMigrateOneBinary.NAME, partName = "dataHandler")
//					DataHandler inSrc) throws PlanetsException {
//		log.info("basicMigrateOneBinaryDH start");
//		SoxMigrations soxm = null;
//		soxm = new SoxMigrations();
//
//		log.info("basicMigrateOneBinary end");
//		return soxm.transformMp3ToWavDH(inSrc);
//	}
	
	
	
}
