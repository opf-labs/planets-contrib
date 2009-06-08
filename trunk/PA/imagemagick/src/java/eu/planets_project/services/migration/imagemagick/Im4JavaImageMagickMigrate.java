package eu.planets_project.services.migration.imagemagick;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.settings.IMGlobalSettings;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.identification.imagemagick.utils.ImageMagickHelper;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
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
    private static final String COMPRESSION_TYPE = "compressionType";
    private static final String COMPRESSION_TYPE_PARAM_DEFAULT = "None"; 
    private static final String IMAGE_QUALITY_LEVEL = "imageQuality";
    private static final double COMPRESSION_QUALITY_LEVEL_DEFAULT = 100.00;
    private static final String IMAGEMAGICK_TEMP = "Im4JavaImageMagickMigrate";
    private static final String DEFAULT_INPUT_FILE_NAME = "Im4JavaImageMagickInput";
    private static final String DEFAULT_OUTPUT_FILE_NAME = "Im4JavaImageMagickOutput";
    private static final String IMAGE_MAGICK_URI = "http://www.imagemagick.org";
    private static final FormatRegistry formatRegistry = FormatRegistryFactory.getFormatRegistry();
    
    private static List<URI> inFormats = ImageMagickHelper.getSupportedInputFormats();
    private static List<URI> outFormats = ImageMagickHelper.getSupportedOutputFormats();
    
    private String compressionType = COMPRESSION_TYPE_PARAM_DEFAULT;
    private double imageQuality = COMPRESSION_QUALITY_LEVEL_DEFAULT;
    
//    private long startTime = 0;
//    private long endTime = 0;

    /**
     * default no arg constructor
     */
    public Im4JavaImageMagickMigrate() {
        System.setProperty("jmagick.systemclassloader","no"); // Use the JBoss-Classloader, instead of the Systemclassloader.
        String im_home_path = System.getenv("IMAGEMAGICK_HOME");
        
        if(im_home_path!=null) {
        	File im_home = new File(im_home_path);
        	IMGlobalSettings.setImageMagickHomeDir(im_home);
        }
        
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

        plogger.info("...and ready! Checking input...");
        
        if(!inFormats.contains(inputFormat)) {
        	return this.returnWithErrorMessage("The Format: " + inputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!", null);
        }
        
        if(!outFormats.contains(outputFormat)) {
        	return this.returnWithErrorMessage("The Format: " + outputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!", null);
        }
        
        
        String inputExt = formatRegistry.getFirstExtension(inputFormat);
        String outputExt = formatRegistry.getFirstExtension(outputFormat);
        
        String fileTitle = digitalObject.getTitle();
        if(fileTitle==null) {
        	fileTitle = DEFAULT_INPUT_FILE_NAME + "." + inputExt;
        }

        File imageMagickTmpFolder = FileUtils.createWorkFolderInSysTemp(IMAGEMAGICK_TEMP);
        plogger.info("Created tmp folder: " + imageMagickTmpFolder.getAbsolutePath());

        plogger.info("Getting content from DigitalObject as InputStream...");
        File inputFile;
        File outputFile;

        plogger.info("Writing content to temp file.");
//            inputFile = File.createTempFile(DEFAULT_INPUT_FILE_NAME, "." + inputExt, imageMagickTmpFolder);
		inputFile = new File(imageMagickTmpFolder, getInputFileName(digitalObject, inputFormat));
		FileUtils.writeInputStreamToFile( digitalObject.getContent().read(), inputFile );
		plogger.info("Temp file created for input: " + inputFile.getAbsolutePath());

		// Also define the output file:
		outputFile = new File(imageMagickTmpFolder, getOutputFileName(inputFile.getName(), outputFormat));

        plogger.info("Starting ImageMagick Migration from " + inputExt + " to " + outputExt + "...");
        
        String actualSrcFormat = verifyInputFormat(inputFile);
        
        if(actualSrcFormat!=null) {
        	if(!extensionsAreEqual(inputExt, actualSrcFormat)) {
        		return this.returnWithErrorMessage("The inputImage does NOT have the file format you claim it has: " + inputExt + "" +
        				"The actual format is instead: " + actualSrcFormat + ". Please check! " +
        						"Returning with Error-Report, nothing has been done, sorry!", null);
        	}
        }
        else {
        	return this.returnWithErrorMessage("Unable to figure out input file format. It does NOT have the format you think it has: " + inputExt + 
        			". Please check! " +
        			"Returning with Error-Report, nothing has been done, sorry.", null);
        }
        
        parseParameters(parameters);
        
        try {
        	IMOperation imOp = new IMOperation();
            imOp.addImage(inputFile.getAbsolutePath());
            imOp.quality(imageQuality);
            imOp.compress(compressionType);
            imOp.addImage(outputFile.getAbsolutePath());
            
            ConvertCmd convert = new ConvertCmd();
        	
			convert.run(imOp);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (IM4JavaException e1) {
			e1.printStackTrace();
		}

        if(migrationSuccessful(outputFile))
            plogger.info("Successfully created result file at: " + outputFile.getAbsolutePath());
        else 
            return this.returnWithErrorMessage("Something went terribly wrong with ImageMagick: No output file created!!!", null);

        DigitalObject newDigObj = null;
        ServiceReport report = null;

        newDigObj = new DigitalObject.Builder(Content.byReference(outputFile))
        	.format(outputFormat)
        	.title(outputFile.getName())
        	.build();

        plogger.info("Created new DigitalObject for result file...");

        report = new ServiceReport(Type.INFO, Status.SUCCESS, "OK");
        plogger.info("Created Service report...");

        plogger.info("Success!! Returning results! Goodbye!");
        return new MigrateResult(newDigObj, report);
    }
    
    private String getInputFileName(DigitalObject digOb, URI inputFormat) {
    	String title = digOb.getTitle();
    	String fileName = null;
    	String inputExt = formatRegistry.getFirstExtension(inputFormat);
    	if(title!=null && !title.equals("")) {
    		if(title.contains(".")) {
    			fileName = title.substring(0, title.lastIndexOf(".")) + "." + inputExt;
    		}
    		else {
    			fileName = title + "." + inputExt;
    		}
    	}
    	else {
    		fileName = DEFAULT_INPUT_FILE_NAME + "." + inputExt;
    	}
    	return fileName;
    }
    
    private String getOutputFileName(String inputFileName, URI outputFormat) {
		String fileName = null;
		String outputExt = formatRegistry.getFirstExtension(outputFormat);
    	if(inputFileName.contains(".")) {
			fileName = inputFileName.substring(0, inputFileName.lastIndexOf(".")) + "." + outputExt;
		}
		else {
			fileName = inputFileName + "." + outputExt;
		}
    	return fileName;
    }
    
    private String verifyInputFormat(File inputImage) {
    	plogger.debug("Initialising ImageInfo Object");
        ImageInfo imageInfo;
        String actualSrcFormat = null;
		try {
			imageInfo = new ImageInfo(inputImage.getAbsolutePath());
			MagickImage image = new MagickImage(imageInfo);
	        plogger.info("ImageInfo object created for file: " + inputImage.getAbsolutePath());
	        actualSrcFormat = image.getMagick();
	        plogger.info("Im4JavaImageMagickMigrate: Actual src format is: " + actualSrcFormat);
	                
		} catch (MagickException e) {
			e.printStackTrace();
		}
		return actualSrcFormat;
        
    }
    
    
    private boolean migrationSuccessful(File resultFile) {
    	if(resultFile.exists() && resultFile.length() > 0) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    
    
    private void parseParameters(List<Parameter> parameters) {
    	
    	if(parameters != null) {
            plogger.info("Got additional parameters:");

            for (Parameter parameter : parameters) {
                String name = parameter.getName();
                plogger.info("Got parameter: " + name + " with value: " + parameter.getValue());
                if(!name.equalsIgnoreCase(IMAGE_QUALITY_LEVEL) && !name.equalsIgnoreCase(COMPRESSION_TYPE)) {
                    plogger.info("Invalid parameter with name: " + name);

                    plogger.info("Setting compressionQualilty to Default value: " + imageQuality);
                    imageQuality = COMPRESSION_QUALITY_LEVEL_DEFAULT;

                    plogger.info("Setting Compression Type to Default value: " + compressionType);
                    compressionType = COMPRESSION_TYPE_PARAM_DEFAULT;
                }


                if(name.equalsIgnoreCase(IMAGE_QUALITY_LEVEL)) {
                    imageQuality = Double.parseDouble(parameter.getValue());
                    if(imageQuality >=0.00 && imageQuality <=100.00) {
                        plogger.info("Setting compressionQualilty to: " + imageQuality);
                    }
                    else {
                        plogger.info("Invalid value for compressionQualilty: " + parameter.getValue());
                        plogger.info("Setting compressionQualilty to Default value: " + COMPRESSION_QUALITY_LEVEL_DEFAULT);
                    }

                }

                if(name.equalsIgnoreCase(COMPRESSION_TYPE)) {
                    compressionType = parameter.getValue();
                    
                    if(compressionTypes.contains(compressionType)) {
                        plogger.info("Trying to set Compression type to: " + compressionType);
                    }
                    else {
                        plogger.info("Invalid value for Compression type: " + compressionType);
                        plogger.info("Setting Compression Type to Default value: " + COMPRESSION_TYPE_PARAM_DEFAULT);
                        compressionType = COMPRESSION_TYPE_PARAM_DEFAULT;
                    }
                }
            }
        }
        else {
        	imageQuality = COMPRESSION_QUALITY_LEVEL_DEFAULT;
            compressionType = COMPRESSION_TYPE_PARAM_DEFAULT;
            plogger.info("No parameters passed! Using default values for 1) compressionType and 2) compressionQuality: 1) " + compressionType + ", 2) " + imageQuality);
        }
    }

    
    private boolean extensionsAreEqual(String extension1, String extension2) {

//        plogger.info("Starting to compare these two extensions: " + extension1 + " and " + extension2);

        Set <URI> ext1FormatURIs = formatRegistry.getUrisForExtension(extension1.toLowerCase());
//        plogger.info("Got list of URIs for " + extension1);

        Set <URI> ext2FormatURIs = formatRegistry.getUrisForExtension(extension2.toLowerCase());
//        plogger.info("Got list of URIs for " + extension2);

        boolean success = false;

//        plogger.info("Trying to match URIs...");
        for(URI currentUri: ext1FormatURIs) {
//            plogger.info("current URI: " + currentUri.toASCIIString());
            if(ext2FormatURIs.contains(currentUri)) {
                success = true;
                break;
            }
            else {
                success = false;
            }
        }
        if(success) {
            plogger.info("Success!");
        }
        else {
            plogger.info("No success.");
        }

        return success;
    }

    private MigrateResult returnWithErrorMessage(String message, Exception e ) {
        if( e == null ) {
            return new MigrateResult(null, ServiceUtils.createErrorReport(message));
        } else {
            return new MigrateResult(null, ServiceUtils.createExceptionErrorReport(message, e));
        }
    }


}
