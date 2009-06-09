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
        name = ImageMagickMigrate.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class ImageMagickMigrate implements Migrate, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1999759257332654952L;

    private static Log plogger = LogFactory.getLog(ImageMagickMigrate.class);
    
    /**
     * the service name
     */
    public static final String NAME = "ImageMagickMigrate";

    /** Array of compression type strings */
    public static String[] jmagick_compressionTypes = new String[11];
    private static final String IMAGE_MAGICK_URI = "http://www.imagemagick.org";

    /**
     * default no arg constructor
     */
    public ImageMagickMigrate() {
        plogger.info("Hello! Initializing ImageMagickMigrate service...");
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
        ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME,Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("A wrapper for ImageMagick file format conversions. Using ImageMagick v6.3.9-Q8 and JMagick v6.3.9-Q8.\n" +
                "This service accepts input and target formats of this shape: 'planets:fmt/ext/[extension]'\n" +
        "e.g. 'planets:fmt/ext/tiff' or 'planets:fmt/ext/tif'");

        sd.classname(this.getClass().getCanonicalName());
        sd.version("0.1");

        List<Parameter> parameterList = new ArrayList<Parameter>();
        Parameter compressionTypeParam = new Parameter.Builder("compressionType", "0-10").description( 
                "Allowed int values: 0 - 10").build();
        parameterList.add(compressionTypeParam);

        Parameter compressionLevelParam = new Parameter.Builder("compressionQuality", "0-100").description(
                "This should be an int value between: 0 - 100, representing the compression quality in percent.").build();
        parameterList.add(compressionLevelParam);

        sd.parameters(parameterList);
        
        sd.tool( Tool.create(null, "ImageMagick", "6.3.9-Q8", null, IMAGE_MAGICK_URI) );
        
        // Checks the installed extensions and supported formats on the fly and creates Migration paths matching the systems capabilities.
        if( ImageMagickHelper.getSupportedInputFormats() != null ) {
          sd.paths(ServiceUtils.createMigrationPathways(ImageMagickHelper.getSupportedInputFormats(), ImageMagickHelper.getSupportedOutputFormats()));
        }
        return sd.build();
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, List<Parameter> parameters) {
    	CoreImageMagick coreIM = new CoreImageMagick();
        return coreIM.doJMagickMigration(digitalObject, inputFormat, outputFormat, parameters);
    }
}
