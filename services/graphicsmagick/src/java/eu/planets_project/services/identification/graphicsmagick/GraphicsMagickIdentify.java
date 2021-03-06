/**
 * 
 */
package eu.planets_project.services.identification.graphicsmagick;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.migration.graphicsmagick.utils.CoreGraphicsMagick;
import eu.planets_project.services.migration.graphicsmagick.utils.GraphicsMagickResult;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.ServiceUtils;

@Local(Identify.class)
@Remote(Identify.class)
@Stateless

@WebService(name = GraphicsMagickIdentify.NAME, 
        serviceName = Identify.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.identify.Identify" )
@MTOM
@StreamingAttachment( parseEagerly=true, memoryThreshold=ServiceUtils.JAXWS_SIZE_THRESHOLD )
public class GraphicsMagickIdentify implements Identify {
	
	public static final long serialVersionUID = 6268629849696320639L;

	public static final String NAME = "GraphicsMagickIdentify";
	
	private static String version = CoreGraphicsMagick.getVersion();
	private static Logger log = Logger.getLogger(GraphicsMagickIdentify.class.getName()) ;
	
	private FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
	
	/* (non-Javadoc)
	 * @see eu.planets_project.services.identify.Identify#identify(eu.planets_project.services.datatypes.DigitalObject, java.util.List)
	 */
public IdentifyResult identify(DigitalObject digitalObject, List<Parameter> parameters ) {
		
		if(digitalObject.getContent()==null) {
			log.severe("The Content of the DigitalObject should NOT be NULL! Returning with ErrorReport");
			return this.returnWithErrorMessage("The Content of the DigitalObject should NOT be NULL! Returning with ErrorReport", null);
		}
		
		File inputFile = DigitalObjectUtils.toFile(digitalObject);
		
		ArrayList<URI> uriList = null;
		
		GraphicsMagickResult result = CoreGraphicsMagick.identify(inputFile);
		
		String srcImageFormat = "unkown";
		
		if(result.SUCCESS) {
			srcImageFormat = result.getFormatExtension();
		}
		else {
			log.severe("NOT IDENTIFIED! Got: " + result.getErrorMsg());
	    	return this.returnWithErrorMessage("NOT IDENTIFIED! Got: " + result.getErrorMsg(), null);
		}
		
		Set<URI> uris = format.getUrisForExtension(srcImageFormat);
	    
	    uriList = new ArrayList <URI> (uris);
	    
	    URI formatURI = format.createExtensionUri(srcImageFormat);
	    uriList.add(0, formatURI);
	    String infoString = createFormatInfoString(uris);
	    log.info("Successfully identified Input file as: " + formatURI.toASCIIString() + "\n" + infoString);
	    ServiceReport sr = new ServiceReport(Type.INFO, Status.SUCCESS,
                "Successfully identified Input file as: "
                        + formatURI.toASCIIString() + "\n" + infoString);
		IdentifyResult identRes = new IdentifyResult(uriList, IdentifyResult.Method.EXTENSION, sr);
		
		log.info("SUCCESS! Returning IdentifyResult. Goodbye!");
		return identRes;
	}

	private String createFormatInfoString(Set<URI> uris) {
		StringBuffer buf = new StringBuffer();
	    buf.append("Matching PRONOM IDs for this extension type: \n");
	    for (URI uri : uris) {
	    	buf.append(uri.toASCIIString() + " (\"" + format.getExtensions(uri) + "\")\n");
		}
		return buf.toString();
	}



	private IdentifyResult returnWithErrorMessage(String message, Exception e ) {
	    if( e == null ) {
	        return new IdentifyResult(null, null, ServiceUtils.createErrorReport(message));
	    } else {
	        return new IdentifyResult(null, null, ServiceUtils.createExceptionErrorReport(message, e));
	    }
	}

	public ServiceDescription describe() {
		ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME, Identify.class.getCanonicalName());
        sd.description("A DigitalObject Identification Service based on GraphicsMagick v" + version + ". \n" +
        		"It returns a list of PRONOM IDs, matching the identified file format!\n" +
        		"Please note: the first URI in the result list is a PLANETS format URI (e.g. \"planets:fmt/ext/tiff\")\n" +
        		"denoting the file format returned by GraphicsMagick for the file under consideration.\n" +
        		"The following URIs are the matching PRONOM IDs.");
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.classname(this.getClass().getCanonicalName());
        sd.tool( Tool.create(null, "GraphicsMagick", version, null, "http://www.graphicsmagick.org/") );
        sd.logo(URI.create("http://www.graphicsmagick.org/images/gm-107x76.png"));
        
        List<URI> formats = CoreGraphicsMagick.getSupportedInputFormats();
        if( formats != null )
            sd.inputFormats(formats.toArray(new URI[]{}));
        return sd.build();
	}

}
