package eu.planets_project.services.migration.imagemagick;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.identification.imagemagick.utils.ImageMagickHelper;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.ServiceUtils;


/**
 * @author Peter Melms
 *
 */
@Stateless()
@Local(Migrate.class)
@Remote(Migrate.class)

@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@WebService(
        name = Im4JavaImageMagickMigrate.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class Im4JavaImageMagickMigrate implements Migrate, Serializable {

	private static final long serialVersionUID = 5719023841822556481L;

	private static Log plogger = LogFactory.getLog(Im4JavaImageMagickMigrate.class);
    
    /**
     * the service name
     */
    public static final String NAME = "Im4JavaImageMagickMigrate";

    /** Array of compression type strings */
    public static List<String> compressionTypes = new ArrayList<String>();
    private static final String IMAGE_MAGICK_URI = "http://www.imagemagick.org";
    
    private static List<URI> inFormats = ImageMagickHelper.getSupportedInputFormats();
    private static List<URI> outFormats = ImageMagickHelper.getSupportedOutputFormats();

    /**
     * default no arg constructor
     */
    public Im4JavaImageMagickMigrate() {
        
        compressionTypes.add("BZip");
        compressionTypes.add("Fax");
        compressionTypes.add("Group4");
        compressionTypes.add("JPEG");
        compressionTypes.add("JPEG2000");
        compressionTypes.add("Lossless");
        compressionTypes.add("LosslessJPEG");
        compressionTypes.add("LZW");
        compressionTypes.add("None");
        compressionTypes.add("RLE");
        compressionTypes.add("Zip");
        compressionTypes.add("RunlegthEncoded");

        plogger.info("Hello! Initializing Im4JavaImageMagickMigrate service...");
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
    	StringBuffer compressionTypesList = new StringBuffer();
    	for (String compressionType : compressionTypes) {
			compressionTypesList.append(compressionType + ", ");
		}
    	String supportedCompressionTypes = compressionTypesList.toString();
    	if(supportedCompressionTypes.endsWith(", ")) {
    		supportedCompressionTypes = supportedCompressionTypes.substring(0, supportedCompressionTypes.lastIndexOf(", "));
    	}
        ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME,Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("A wrapper for ImageMagick file format conversions. Using ImageMagick v6.5.3.3-Q8 and JMagick v6.3.9-Q8.\n" +
                "This service accepts input and target formats of this shape: 'planets:fmt/ext/[extension]'\n" +
        "e.g. 'planets:fmt/ext/tiff' or 'planets:fmt/ext/tif'");

        sd.classname(this.getClass().getCanonicalName());
        sd.version("0.2");

        List<Parameter> parameterList = new ArrayList<Parameter>();
        Parameter compressionTypeParam = new Parameter.Builder("compressionType", supportedCompressionTypes).description( 
                "Allowed values: " + supportedCompressionTypes).build();
        parameterList.add(compressionTypeParam);

        Parameter imageQualityLevelParam = new Parameter.Builder("imageQuality", "0.00-100.00").description(
                "This should be an double value between: 0.00 - 100.00, " +
                "representing the image quality of the compressed image in percent (Best=100.00).").build();
        parameterList.add(imageQualityLevelParam);

        sd.parameters(parameterList);
        
        sd.tool( Tool.create(null, "ImageMagick", "6.3.9-Q8", null, IMAGE_MAGICK_URI) );
        
        // Checks the installed extensions and supported formats on the fly and creates Migration paths matching the systems capabilities.
        sd.paths(ServiceUtils.createMigrationPathways(inFormats, outFormats));

        return sd.build();
    }
    

    /**
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, List<Parameter> parameters) {
    	CoreImageMagick coreIM = new CoreImageMagick();
        return coreIM.doIm4JavaMigration(digitalObject, inputFormat, outputFormat, parameters);
    }
}
