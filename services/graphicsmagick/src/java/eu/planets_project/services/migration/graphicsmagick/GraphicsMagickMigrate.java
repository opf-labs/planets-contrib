package eu.planets_project.services.migration.graphicsmagick;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.graphicsmagick.utils.CoreGraphicsMagick;
import eu.planets_project.services.migration.graphicsmagick.utils.GraphicsMagickResult;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.ServiceUtils;

/**
 * @author Peter Melms
 *
 */
@Stateless
@MTOM
@StreamingAttachment( parseEagerly=true, memoryThreshold=ServiceUtils.JAXWS_SIZE_THRESHOLD )
@WebService(
        name = GraphicsMagickMigrate.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class GraphicsMagickMigrate implements Migrate, Serializable {
	
	public static final String NAME = "GraphicsMagickMigrate";
	
	private static final long serialVersionUID = 2542354060692928942L;

	private static Logger log = Logger.getLogger(GraphicsMagickMigrate.class.getName());
	
	private static final FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
	
	/** Array of compression type strings */
    public static List<String> compressionTypes = new ArrayList<String>();
    
    private static final String COMPRESSION_TYPE = "compressionType";
    private static final String COMPRESSION_TYPE_PARAM_DEFAULT = "None"; 
     
    private static final String IMAGE_QUALITY_LEVEL = "imageQuality";
    private static final int IMAGE_QUALITY_LEVEL_DEFAULT = 100;
    
    private static List<URI> inFormats = CoreGraphicsMagick.getSupportedInputFormats();
    private static List<URI> outFormats = CoreGraphicsMagick.getSupportedOutputFormats();
    
    private static String compressionType = COMPRESSION_TYPE_PARAM_DEFAULT;
    private static int imageQuality = IMAGE_QUALITY_LEVEL_DEFAULT;
	
	public GraphicsMagickMigrate() {
		compressionTypes.add("None");
    	compressionTypes.add("BZip");
        compressionTypes.add("Fax");
        compressionTypes.add("Group4");
        compressionTypes.add("JPEG");
        compressionTypes.add("Lossless");
        compressionTypes.add("LZW");
        compressionTypes.add("RLE");
        compressionTypes.add("Zip");
	}
	
	/* (non-Javadoc)
	 * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, java.util.List)
	 */
	public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
			URI outputFormat, List<Parameter> parameters) {
		
		String inputExt = format.getFirstExtension(inputFormat);
		String outputExt = format.getFirstExtension(outputFormat);
		
		URI testIn = format.createExtensionUri(inputExt);
		URI testOut = format.createExtensionUri(outputExt);
        
		if(inFormats!=null && outFormats!=null) {
			if(testIn!=null) {
			    if(!inFormats.contains(testIn)) {
			    	return returnWithErrorMessage("The input format: " + inputFormat.toASCIIString() + " is NOT supported by this GraphicsMagick-Service!", null);
			    }
			    
			    if(!outFormats.contains(testOut)) {
			    	return returnWithErrorMessage("The output format: " + outputFormat.toASCIIString() + " is NOT supported by this GraphicsMagick-Service!", null);
			    }
			}
			else {
				return returnWithErrorMessage("The format you have passed can not be resolved: " + inputFormat.toASCIIString() + " Nothing has been done, sorry!", null);
			}
		}
		
		File inputFile = null;
        try {
            inputFile = File.createTempFile("planets", "." + inputExt);
            DigitalObjectUtils.toFile(digitalObject, inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		parseParameters(parameters);
		
		CoreGraphicsMagick gm = new CoreGraphicsMagick();
		
		if(!gm.inputFormatIsValid(inputFile, inputFormat)) {
			return this.returnWithErrorMessage("The input file has a different Format than it claims it has! Nothing has been done, sorry!", null);
		}
		
		GraphicsMagickResult result = gm.convert(inputFile, outputExt, compressionType, imageQuality);
		
		if(result.SUCCESS) {
			DigitalObject resultDO = createDigitalObject(result.getResultFile(), outputFormat);
			ServiceReport report = new ServiceReport(Type.INFO, Status.SUCCESS, result.getOutputMsg());
			MigrateResult mr = new MigrateResult(resultDO, report);
			return mr;
		}
		else {
			return this.returnWithErrorMessage(result.getErrorMsg(), null);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see eu.planets_project.services.PlanetsService#describe()
	 */
	public ServiceDescription describe() {
		StringBuffer compressionTypesList = new StringBuffer();
    	for (String currentCompressionType : compressionTypes) {
			compressionTypesList.append(currentCompressionType + ", ");
		}
    	String supportedCompressionTypes = compressionTypesList.toString();
    	if(supportedCompressionTypes.endsWith(", ")) {
    		supportedCompressionTypes = supportedCompressionTypes.substring(0, supportedCompressionTypes.lastIndexOf(", "));
    	}
    	
    	String version = CoreGraphicsMagick.getVersion();
    	
        ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME, Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("A wrapper for GraphicsMagick file format conversions. Using GraphicsMagick v" + version + ".\n" +
                "This service accepts input and target formats of this shape: 'planets:fmt/ext/[extension]'\n" +
        		"e.g. 'planets:fmt/ext/tiff' or 'planets:fmt/ext/tif'");

        sd.classname(this.getClass().getCanonicalName());
        sd.version("1.1");

        List<Parameter> parameterList = new ArrayList<Parameter>();
        Parameter compressionTypeParam = new Parameter.Builder("compressionType", supportedCompressionTypes).description( 
                "Allowed values: " + supportedCompressionTypes).build();
        parameterList.add(compressionTypeParam);

        Parameter imageQualityLevelParam = new Parameter.Builder("imageQuality", "0-100").description(
                "This should be an int value between: 0 - 100, " +
                "representing the image quality of the compressed image in percent (Best=100).").build();
        parameterList.add(imageQualityLevelParam);

        sd.parameters(parameterList);
        
        sd.tool( Tool.create(null, "GraphicsMagick", version, null, "http://www.graphicsmagick.org/") );
        sd.logo(URI.create("http://www.graphicsmagick.org/images/gm-107x76.png"));
        
        // Checks the installed extensions and supported formats on the fly and creates Migration paths matching the systems capabilities.
        if(inFormats!=null && outFormats!=null) {
        	sd.paths(ServiceUtils.createMigrationPathways(inFormats, outFormats));
        }
        return sd.build();
	}

	private void parseParameters(List<Parameter> parameters) {
		if(parameters != null) {
	        log.info("Got additional parameters:");
	
	        for (Parameter parameter : parameters) {
	            String name = parameter.getName();
	            log.info("Got parameter: " + name + " with value: " + parameter.getValue());
	            if(!name.equalsIgnoreCase(IMAGE_QUALITY_LEVEL) && !name.equalsIgnoreCase(COMPRESSION_TYPE)) {
	            	log.info("Invalid parameter with name: " + name);
	
	            	log.info("Setting compressionQualilty to Default value: " + imageQuality);
	                imageQuality = IMAGE_QUALITY_LEVEL_DEFAULT;
	
	                log.info("Setting Compression Type to Default value: " + compressionType);
	                compressionType = COMPRESSION_TYPE_PARAM_DEFAULT;
	            }
	
	            if(name.equalsIgnoreCase(IMAGE_QUALITY_LEVEL)) {
	                imageQuality = Integer.parseInt(parameter.getValue());
	                if(imageQuality >=0.00 && imageQuality <=100.00) {
	                	log.info("Setting compressionQualilty to: " + imageQuality);
	                }
	                else {
	                	log.info("Invalid value for compressionQualilty: " + parameter.getValue());
	                	log.info("Setting compressionQualilty to Default value: " + IMAGE_QUALITY_LEVEL_DEFAULT);
	                }
	
	            }
	
	            if(name.equalsIgnoreCase(COMPRESSION_TYPE)) {
	                compressionType = parameter.getValue();
	                
	                if(compressionTypes.contains(compressionType)) {
	                	log.info("Setting Compression type to: " + compressionType);
	                }
	                else {
	                	log.info("Invalid value for Compression type: " + compressionType);
	                	log.info("Setting Compression Type to Default value: " + COMPRESSION_TYPE_PARAM_DEFAULT);
	                    compressionType = COMPRESSION_TYPE_PARAM_DEFAULT;
	                }
	            }
	        }
	    }
	    else {
	    	imageQuality = IMAGE_QUALITY_LEVEL_DEFAULT;
	        compressionType = COMPRESSION_TYPE_PARAM_DEFAULT;
	        log.info("No parameters passed! Using default values for 1) compressionType: " + compressionType +" and 2) compressionQuality: " + imageQuality);
	    }
	}

	private DigitalObject createDigitalObject(File outputFile, URI outputFormat) {
		DigitalObject newDigObj = new DigitalObject.Builder(Content.byReference(outputFile))
   			.format(outputFormat)
   			.title(outputFile.getName())
   			.build();
		return newDigObj;
	}
	
	
	private MigrateResult returnWithErrorMessage(String message, Exception e ) {
	    if( e == null ) {
	        return new MigrateResult(null, ServiceUtils.createErrorReport(message));
	    } else {
	        return new MigrateResult(null, ServiceUtils.createExceptionErrorReport(message, e));
	    }
	}

}
