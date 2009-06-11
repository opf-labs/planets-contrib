/**
 * 
 */
package eu.planets_project.services.identification.imagemagick;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.identification.imagemagick.utils.ImageMagickHelper;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ServiceUtils;

/**
 * @author melmsp
 *
 */

@Local(Identify.class)
@Remote(Identify.class)
@Stateless

@WebService(name = ImageMagickIdentify.NAME, 
        serviceName = Identify.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.identify.Identify" )
        
public class ImageMagickIdentify implements Identify {
	
	public static final String NAME = "ImageMagickIdentify";
	
	public static final long serialVersionUID = -772290809743383420L;
	
	private PlanetsLogger PLOGGER = PlanetsLogger.getLogger(this.getClass()) ;
	
	private static final String WORKFOLDER_NAME = "ImageMagickIdentify";
	private String sessionID = FileUtils.randomizeFileName("");
	private final String DEFAULT_INPUT_NAME = "ImageMagickIdentify_input" + sessionID;
	private static final String DEFAULT_EXTENSION = "bin";
	private static final FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
	
	
	
	/**
	 * Default Constructor, setting the System.property to tell Jboss to use its own Classloader...
	 */
	public ImageMagickIdentify(){
	    System.setProperty("jmagick.systemclassloader","no"); // Use the JBoss-Classloader, instead of the Systemclassloader.
	    PLOGGER.info("Hello! Initializing and starting ImageMagickIdentify service!");
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.services.identify.Identify#describe()
	 */
	public ServiceDescription describe() {
		ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME, Identify.class.getCanonicalName());
        sd.description("A DigitalObject Identification Service based on ImageMagick. \n" +
        		"It returns a list of PRONOM IDs, matching for the identified file format!\n" +
        		"Please note: the first URI in the result list is a PLANETS format URI (e.g. \"planets:fmt/ext/tiff\")\n" +
        		"denoting the file format returned by ImageMagick for the file under consideration.\n" +
        		"The following URIs are the matching PRONOM IDs.");
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.classname(this.getClass().getCanonicalName());
        List<URI> formats = ImageMagickHelper.getSupportedInputFormats();
        if( formats != null )
            sd.inputFormats(formats.toArray(new URI[]{}));
        return sd.build();
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.services.identify.Identify#identify(eu.planets_project.services.datatypes.DigitalObject)
	 */
	public IdentifyResult identify(DigitalObject digitalObject, List<Parameter> parameters ) {
		
		if(digitalObject.getContent()==null) {
			PLOGGER.error("The Content of the DigitalObject should NOT be NULL! Returning with ErrorReport");
			return this.returnWithErrorMessage("The Content of the DigitalObject should NOT be NULL! Returning with ErrorReport", null);
		}
		
		String fileName = digitalObject.getTitle();
		PLOGGER.info("Input file to identify: " +fileName);
		URI inputFormat = digitalObject.getFormat();
        if(inputFormat!=null) {
            PLOGGER.info("Assumed file format: " + inputFormat.toASCIIString());
        }
		String extension = null;
		
		if(inputFormat!=null) {
			extension = format.getExtensions(inputFormat).iterator().next();
			PLOGGER.info("Found extension for input file: " + extension);
		}
		else {
			PLOGGER.info("I am not able to find the file extension, using DEFAULT_EXTENSION instead: " + DEFAULT_EXTENSION);
			extension = DEFAULT_EXTENSION;
		}
		
		if(fileName==null || fileName.equalsIgnoreCase("")) {
			PLOGGER.info("Could not retrieve file name\n(digitalObject.getTitle() returns NULL), using DEFAULT_INPUT_NAME instead: " + DEFAULT_INPUT_NAME + "." + extension);
			fileName = DEFAULT_INPUT_NAME + "." + extension;
		}
		
		File workFolder = FileUtils.createWorkFolderInSysTemp(WORKFOLDER_NAME);
		PLOGGER.info("Created workfolder for temp files: " + workFolder.getAbsolutePath());
		if(workFolder.exists()) {
			FileUtils.deleteAllFilesInFolder(workFolder);
		}
		
		File inputFile = FileUtils.writeInputStreamToFile(digitalObject.getContent().read(), workFolder, fileName);
		PLOGGER.info("Created temporary input file: " + inputFile.getAbsolutePath());
		
		ArrayList<URI> uriList = null;
		
		String srcImageFormat = null;
		
		try {
			PLOGGER.info("Initialising ImageInfo Object");
			ImageInfo imageInfo = new ImageInfo(inputFile.getAbsolutePath());
			MagickImage image = new MagickImage(imageInfo);
			
		    // Checking input image format
		    srcImageFormat = image.getMagick();
		    PLOGGER.info("The image format is: '" + srcImageFormat + "'");
		    image.destroyImages();
		    
		} catch (MagickException e) {
			PLOGGER.error("The file seems to have an unsupported file format!");
			e.getLocalizedMessage();
			e.printStackTrace();
		}
		
		Set<URI> uris = format.getUrisForExtension(srcImageFormat);
//		List<URI> uris = fr.search(srcImageFormat);
	    
	    if(uris.size() <= 0) {
	    	PLOGGER.error("No URI returned for this extension: " + srcImageFormat + ".\n" 
	    			+ "Input file: " + inputFile.getName() + " could not be identified!!!");
	    	return this.returnWithErrorMessage("No URI returned for this extension: " + srcImageFormat + ".\n" 
	    			+ "Input file: " + inputFile.getName() + " could not be identified!!!", null);
	    }
	    
	    uriList = new ArrayList <URI> (uris);
	    
	    URI formatURI = format.createExtensionUri(srcImageFormat);
	    uriList.add(0, formatURI);
	    String infoString = createFormatInfoString(uris);
	    PLOGGER.info("Successfully identified Input file as: " + formatURI.toASCIIString() + "\n" + infoString);
	    ServiceReport sr = new ServiceReport(Type.INFO, Status.SUCCESS,
                "Successfully identified Input file as: "
                        + formatURI.toASCIIString() + "\n" + infoString);
		IdentifyResult identRes = new IdentifyResult(uriList, IdentifyResult.Method.PARTIAL_PARSE, sr);
		
		PLOGGER.info("SUCCESS! Returning IdentifyResult. Goodbye!");
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
	

}
