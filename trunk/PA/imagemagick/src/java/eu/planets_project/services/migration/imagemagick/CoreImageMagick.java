package eu.planets_project.services.migration.imagemagick;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.ProcessRunner;
import eu.planets_project.services.utils.ServiceUtils;

public class CoreImageMagick {
	
	private static Log plogger = LogFactory.getLog(CoreImageMagick.class);
	
	/** Array of compression type strings */
    public static List<String> compressionTypes = new ArrayList<String>();
    public static String[] jmagick_compressionTypes = new String[11];
    
    private static final String COMPRESSION_TYPE = "compressionType";
    private static final String COMPRESSION_TYPE_PARAM_DEFAULT = "None"; 
     
    private static final String IMAGE_QUALITY_LEVEL = "imageQuality";
    private static final String COMPRESSION_QUALITY_LEVEL = "compressionQuality";
    private static final double COMPRESSION_QUALITY_LEVEL_DEFAULT = 100.00;
    
    private static final int JMAGICK_COMPRESSION_TYPE_PARAM_DEFAULT = 1;
    private static final int JMAGICK_COMPRESSION_QUALITY_LEVEL_DEFAULT = 100;
    
    private static final String IM4JAVA_TEMP = "Im4Java_ImageMagickMigrate";
    private static final String JMAGICK_TEMP = "JMagick_ImageMagickMigrate";
    private static final String sessionID = FileUtils.randomizeFileName("");
    private static final String DEFAULT_INPUT_FILE_NAME = "imageMagickInput" + sessionID;
    private static final FormatRegistry formatRegistry = FormatRegistryFactory.getFormatRegistry();
    
    private static List<URI> inFormats = ImageMagickHelper.getSupportedInputFormats();
    private static List<URI> outFormats = ImageMagickHelper.getSupportedOutputFormats();
    
    private static String compressionType = COMPRESSION_TYPE_PARAM_DEFAULT;
    private static double imageQuality = COMPRESSION_QUALITY_LEVEL_DEFAULT;
    
    private File im_home = null;
    private String im_home_path = null;
    
    private static String version = null;
    
    private static final String IMAGE_MAGICK_URI = "http://www.imagemagick.org";
    
    public CoreImageMagick() {
        plogger.info("Hello! Initializing ImageMagick services...");
    }
    public static ServiceDescription describeJMagickMigrate(String serviceName, String className) {
    	if(version==null) {
    		version = checkImageMagickVersion();
    	}
    	
    	jmagick_compressionTypes[0] = "Undefined Compression";
        jmagick_compressionTypes[1] = "No Compression";
        jmagick_compressionTypes[2] = "BZip Compression";
        jmagick_compressionTypes[3] = "Fax Compression";
        jmagick_compressionTypes[4] = "Group4 Compression";
        jmagick_compressionTypes[5] = "JPEG Compression";
        jmagick_compressionTypes[6] = "JPEG2000 Compression";
        jmagick_compressionTypes[7] = "LosslessJPEG Compression";
        jmagick_compressionTypes[8] = "LZW Compression";
        jmagick_compressionTypes[9] = "RLE Compression";
        jmagick_compressionTypes[10] = "Zip Compression";
        
        ServiceDescription.Builder sd = new ServiceDescription.Builder(serviceName,Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("A wrapper for ImageMagick file format conversions. Using ImageMagick " + version + " and JMagick.\n" +
                "This service accepts input and target formats of this shape: 'planets:fmt/ext/[extension]'\n" +
        "e.g. 'planets:fmt/ext/tiff' or 'planets:fmt/ext/tif'");

        sd.classname(className);
        sd.version("1.0");

        List<Parameter> parameterList = new ArrayList<Parameter>();
        StringBuffer buf = new StringBuffer();
        for(int i=0;i<jmagick_compressionTypes.length;i++) {
        	buf.append(i + " = " + jmagick_compressionTypes[i] + System.getProperty("line.separator"));
        }
        Parameter compressionTypeParam = new Parameter.Builder("compressionType", "0-10")
        	.description("Allowed int values: " + System.getProperty("line.separator")
        			+ buf.toString())
        			.build();
        parameterList.add(compressionTypeParam);

        Parameter compressionLevelParam = new Parameter.Builder("compressionQuality", "0-100").description(
                "This should be an int value between: 0 - 100, representing the compression quality in percent.").build();
        parameterList.add(compressionLevelParam);

        sd.parameters(parameterList);
        
        sd.tool( Tool.create(null, "ImageMagick", version, null, IMAGE_MAGICK_URI) );
        sd.logo(URI.create("http://www.imagemagick.org/image/logo.jpg"));
        
        // Checks the installed extensions and supported formats on the fly and creates Migration paths matching the systems capabilities.
        if(inFormats!=null && outFormats!=null) {
        	 sd.paths(ServiceUtils.createMigrationPathways(inFormats, outFormats));
        }
        return sd.build();
    }
    
    public static ServiceDescription describeIm4JavaMigrate(String serviceName , String className) {
    	if(version==null) {
    		version = checkImageMagickVersion();
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
    	
    	StringBuffer compressionTypesList = new StringBuffer();
    	for (String currentCompressionType : compressionTypes) {
			compressionTypesList.append(currentCompressionType + ", ");
		}
    	String supportedCompressionTypes = compressionTypesList.toString();
    	if(supportedCompressionTypes.endsWith(", ")) {
    		supportedCompressionTypes = supportedCompressionTypes.substring(0, supportedCompressionTypes.lastIndexOf(", "));
    	}
        ServiceDescription.Builder sd = new ServiceDescription.Builder(serviceName, Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("A wrapper for ImageMagick file format conversions. Using ImageMagick " + version + " and im4Java.\n" +
                "This service accepts input and target formats of this shape: 'planets:fmt/ext/[extension]'\n" +
        		"e.g. 'planets:fmt/ext/tiff' or 'planets:fmt/ext/tif'");

        sd.classname(className);
        sd.version("1.1");

        List<Parameter> parameterList = new ArrayList<Parameter>();
        Parameter compressionTypeParam = new Parameter.Builder("compressionType", supportedCompressionTypes).description( 
                "Allowed values: " + supportedCompressionTypes).build();
        parameterList.add(compressionTypeParam);

        Parameter imageQualityLevelParam = new Parameter.Builder("imageQuality", "0.00-100.00").description(
                "This should be an double value between: 0.00 - 100.00, " +
                "representing the image quality of the compressed image in percent (Best=100.00).").build();
        parameterList.add(imageQualityLevelParam);

        sd.parameters(parameterList);
        
        sd.tool( Tool.create(null, "ImageMagick", version, null, IMAGE_MAGICK_URI) );
        sd.logo(URI.create("http://www.imagemagick.org/image/logo.jpg"));
        
        // Checks the installed extensions and supported formats on the fly and creates Migration paths matching the systems capabilities.
        if(inFormats!=null && outFormats!=null) {
        	sd.paths(ServiceUtils.createMigrationPathways(inFormats, outFormats));
        }
        return sd.build();
    }
	
	public MigrateResult doJMagickMigration(DigitalObject digOb, URI inputFormat, URI outputFormat, List<Parameter> parameters) {
		System.setProperty("jmagick.systemclassloader","no"); // Use the JBoss-Classloader, instead of the Systemclassloader.
		plogger.info("...and ready! Checking input...");
		
		String inputExt = formatRegistry.getFirstExtension(inputFormat);
	    String outputExt = formatRegistry.getFirstExtension(outputFormat);
		
		URI testIn = formatRegistry.createExtensionUri(inputExt);
		URI testOut = formatRegistry.createExtensionUri(outputExt);
        
		if(inFormats!=null && outFormats!=null) {
			if(testIn!=null) {
			    if(!inFormats.contains(testIn)) {
			    	return returnWithErrorMessage("The input format: " + inputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!", null);
			    }
			    
			    if(!outFormats.contains(testOut)) {
			    	return returnWithErrorMessage("The output format: " + outputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!", null);
			    }
			}
			else {
				return returnWithErrorMessage("The format you have passed can not be resolved: " + inputFormat.toASCIIString() + " Nothing has been done, sorry!", null);
			}
		}
        
        
        File imageMagickTmpFolder = FileUtils.createWorkFolderInSysTemp(JMAGICK_TEMP);
        plogger.info("Created tmp folder: " + imageMagickTmpFolder.getAbsolutePath());

        plogger.info("Getting content from DigitalObject as InputStream...");
        File inputFile;
        File outputFile;

        plogger.info("Writing content to temp file.");
		inputFile = new File(imageMagickTmpFolder, FileUtils.randomizeFileName(DigitalObjectUtils.getFileNameFromDigitalObject(digOb, inputFormat)));
		
		if(inputFile.exists()) {
			plogger.info("PLEASE NOTE: Input file with same name already exists. Deleting old file: " + inputFile.getName());
			inputFile.delete();
		}
		
		FileUtils.writeInputStreamToFile( digOb.getContent().read(), inputFile );
		plogger.info("Temp file created for input: " + inputFile.getAbsolutePath());

		// Also define the output file:
		outputFile = new File(imageMagickTmpFolder, getOutputFileName(inputFile.getName(), outputFormat));

        plogger.info("Starting ImageMagick Migration from " + inputExt + " to " + outputExt + "...");
        
        try {
//            plogger.debug("Initialising ImageInfo Object");
//            ImageInfo imageInfo = new ImageInfo(inputFile.getAbsolutePath());
//            MagickImage image = new MagickImage(imageInfo);
//            plogger.info("ImageInfo object created for file: " + inputFile.getAbsolutePath());
//            String actualSrcFormat = image.getMagick();
//            plogger.info("ImageMagickMigrate: Given src file format extension is: " + inputExt);
//            plogger.info("ImageMagickMigrate: Actual src format is: " + actualSrcFormat);
        	
        	String actualSrcFormat = verifyInputFormat(inputFile);
        	
        	if(actualSrcFormat.equalsIgnoreCase("unknown")) {
    	    	plogger.warn("The input format is unknown. Trying to use the specified input format...");
    	    	actualSrcFormat = inputExt;
    	    }
            
            // Has the input file the format it claims it has?
            if(extensionsAreEqual(inputExt, actualSrcFormat) == false) {
                // if NOT just return without doing anything...
                return returnWithErrorMessage("The passed input file format (" + inputExt.toUpperCase() + ") does not match the actual format (" + actualSrcFormat + ") of the file!\n" +
                        "This could cause unpredictable behaviour. Nothing has been done!", null);
            }
            
            ImageInfo imageInfo = new ImageInfo(inputFile.getAbsolutePath());
            MagickImage image = new MagickImage(imageInfo);
            
            parseJMagickParameters(parameters, imageInfo, image);

            image.setImageFormat(outputExt);
            plogger.info("Setting new file format for output file to: " + outputExt);

            String outputFilePath = outputFile.getAbsolutePath();
            plogger.info("Starting to write result file to: " + outputFilePath);

            image.setFileName(outputFilePath);
            image.writeImage(imageInfo);
            
            if(migrationSuccessful(outputFile))
                plogger.info("Successfully created result file at: " + outputFilePath);
            else 
                return returnWithErrorMessage("Something went terribly wrong with ImageMagick: No output file created!!!", null);

        } catch (MagickException e) {
            e.printStackTrace();
            return returnWithErrorMessage("Something went terribly wrong with ImageMagick: ", e);
        }

       DigitalObject newDigObj = createDigitalObject(outputFile, outputFormat);
       
       ServiceReport report = null;

       newDigObj = new DigitalObject.Builder(Content.byReference(outputFile))
       		.format(outputFormat)
       		.title(outputFile.getName())
       		.build();

       plogger.info("Created new DigitalObject for result file...");

       report = new ServiceReport(Type.INFO, Status.SUCCESS, "OK! Migration successful.");
       plogger.info("Created Service report...");

       plogger.info("Success!! Returning results! Goodbye!");
       return new MigrateResult(newDigObj, report);
	}
	
	
	public MigrateResult doIm4JavaMigration(DigitalObject digOb, URI inputFormat, URI outputFormat, List<Parameter> parameters) {
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("windows")) {
			im_home_path = System.getenv("IMAGEMAGICK_HOME");
			if(im_home_path!=null) {
				im_home = new File(im_home_path);
				IMGlobalSettings.setImageMagickHomeDir(im_home);
			}
		}
		
		plogger.info("...and ready! Checking input...");
	    
		String inputExt = formatRegistry.getFirstExtension(inputFormat);
	    String outputExt = formatRegistry.getFirstExtension(outputFormat);
		
		URI testIn = formatRegistry.createExtensionUri(inputExt);
		URI testOut = formatRegistry.createExtensionUri(outputExt);
        
		if(inFormats!=null && outFormats!=null) {
			if(testIn!=null) {
			    if(!inFormats.contains(testIn)) {
			    	return returnWithErrorMessage("The input format: " + inputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!", null);
			    }
			    
			    if(!outFormats.contains(testOut)) {
			    	return returnWithErrorMessage("The output format: " + outputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!", null);
			    }
			}
			else {
				return returnWithErrorMessage("The format you have passed can not be resolved: " + inputFormat.toASCIIString() + " Nothing has been done, sorry!", null);
			}
		}
	    
	    File imageMagickTmpFolder = FileUtils.createWorkFolderInSysTemp(IM4JAVA_TEMP);
	    plogger.info("Created tmp folder: " + imageMagickTmpFolder.getAbsolutePath());
	
	    plogger.info("Getting content from DigitalObject as InputStream...");
	    File inputFile;
	    File outputFile;
	
	    plogger.info("Writing content to temp file.");
		inputFile = new File(imageMagickTmpFolder, FileUtils.randomizeFileName(DigitalObjectUtils.getFileNameFromDigitalObject(digOb, inputFormat)));
		if(inputFile.exists()) {
			plogger.info("PLEASE NOTE: Input file with same name already exists. Deleting old file: " + inputFile.getName());
			inputFile.delete();
		}
		FileUtils.writeInputStreamToFile( digOb.getContent().read(), inputFile );
		plogger.info("Temp file created for input: " + inputFile.getAbsolutePath());
	
		// Also define the output file:
		outputFile = new File(imageMagickTmpFolder, getOutputFileName(inputFile.getName(), outputFormat));
	
	    plogger.info("Starting ImageMagick Migration from " + inputExt + " to " + outputExt + "...");
	    
	    
	    String actualSrcFormat = verifyInputFormat(inputFile);
	    
	    if(actualSrcFormat.equalsIgnoreCase("unknown")) {
	    	plogger.warn("The input format is unknown. Trying to use the specified input format...");
	    	actualSrcFormat = inputExt;
	    }
	    
	    if(actualSrcFormat!=null) {
	    	if(!extensionsAreEqual(inputExt, actualSrcFormat)) {
	    		return returnWithErrorMessage("The inputImage does NOT have the file format you claim it has: " + inputExt.toUpperCase() + 
	    				" The actual format is instead: " + actualSrcFormat + ". Please check! " +
	    						"Returning with Error-Report, nothing has been done, sorry!", null);
	    	}
	    }
	    else {
	    	return returnWithErrorMessage("Unable to figure out input file format. It does NOT have the format you think it has: " + inputExt + 
	    			". Please check! " +
	    			"Returning with Error-Report, nothing has been done, sorry.", null);
	    }
	    
	    parseIm4JavaParameters(parameters);
	    
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
	        return returnWithErrorMessage("Something went terribly wrong with ImageMagick: No output file created!!!", null);
	
	    DigitalObject newDigObj = createDigitalObject(outputFile, outputFormat);
	    
	    ServiceReport report = null;
	
	    plogger.info("Created new DigitalObject for result file...");
	
	    report = new ServiceReport(Type.INFO, Status.SUCCESS, "OK! Migration successful.");
	    plogger.info("Created Service report...");
	
	    plogger.info("Success!! Returning results! Goodbye!");
	    return new MigrateResult(newDigObj, report);
	}
	private void parseJMagickParameters(List<Parameter> parameters, ImageInfo imageInfo, MagickImage image) {
		try {
			if(parameters != null) {
	            plogger.info("Got additional parameters:");
	            int compressionType;
	            int compressionQuality; 
	            for (Iterator<Parameter> iterator = parameters.iterator(); iterator.hasNext();) {
	                Parameter parameter = (Parameter) iterator.next();
	                String name = parameter.getName();
	                plogger.info("Got parameter: " + name + " with value: " + parameter.getValue());
	                if(!name.equalsIgnoreCase(COMPRESSION_QUALITY_LEVEL) && !name.equalsIgnoreCase(COMPRESSION_TYPE)) {
	                    plogger.info("Invalid parameter with name: " + parameter.getName());
	
	                    plogger.info("Setting compressionQualilty to Default value: " + JMAGICK_COMPRESSION_QUALITY_LEVEL_DEFAULT);
	                    imageInfo.setQuality(JMAGICK_COMPRESSION_QUALITY_LEVEL_DEFAULT);
	
	                    plogger.info("Setting Compression Type to Default value: " + COMPRESSION_TYPE_PARAM_DEFAULT + jmagick_compressionTypes[JMAGICK_COMPRESSION_TYPE_PARAM_DEFAULT]);
	                    image.setCompression(JMAGICK_COMPRESSION_TYPE_PARAM_DEFAULT);
	                }
	
	                if(name.equalsIgnoreCase(COMPRESSION_QUALITY_LEVEL)) {
	                    compressionQuality = Integer.parseInt(parameter.getValue());
	                    if(compressionQuality >=0 && compressionQuality <=100) {
	                        plogger.info("Setting compressionQualilty to: " + compressionQuality);
	                        imageInfo.setQuality(compressionQuality);
	                    }
	                    else {
	                        plogger.info("Invalid value for compressionQualilty: " + parameter.getValue());
	                        plogger.info("Setting compressionQualilty to Default value: " + COMPRESSION_QUALITY_LEVEL_DEFAULT);
	                        imageInfo.setQuality(JMAGICK_COMPRESSION_QUALITY_LEVEL_DEFAULT);
	                    }
	
	                }
	
	                if(name.equalsIgnoreCase(COMPRESSION_TYPE)) {
	                    compressionType = Integer.parseInt(parameter.getValue());
	                    if(compressionType >= 0 && compressionType <= 10) {
	                        plogger.info("Setting Compression type to: " + jmagick_compressionTypes[compressionType]);
	                        image.setCompression(compressionType);
	                    }
	                    else {
	                        plogger.info("Invalid value for Compression type: " + parameter.getValue());
	                        plogger.info("Setting Compression Type to Default value: " + COMPRESSION_TYPE_PARAM_DEFAULT + jmagick_compressionTypes[JMAGICK_COMPRESSION_TYPE_PARAM_DEFAULT]);
	                        image.setCompression(JMAGICK_COMPRESSION_TYPE_PARAM_DEFAULT);
	                    }
	                }
	            }
	        }
	        else {
	            plogger.info("No parameters passed! Setting default values for compressionType and compressionQuality");
	            image.setCompression(JMAGICK_COMPRESSION_TYPE_PARAM_DEFAULT);
	            imageInfo.setQuality(JMAGICK_COMPRESSION_QUALITY_LEVEL_DEFAULT);
	        }
		} catch (MagickException e) {
	        e.printStackTrace();
		}
	}
	
	private void parseIm4JavaParameters(List<Parameter> parameters) {
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
	                    plogger.info("Setting Compression type to: " + compressionType);
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
	        plogger.info("No parameters passed! Using default values for 1) compressionType: " + compressionType +" and 2) compressionQuality: " + imageQuality);
	    }
	}
	private DigitalObject createDigitalObject(File outputFile, URI outputFormat) {
		DigitalObject newDigObj = new DigitalObject.Builder(Content.byReference(outputFile))
   			.format(outputFormat)
   			.title(outputFile.getName())
   			.build();
		return newDigObj;
	}

	public static String verifyInputFormat(File inputImage) {
		plogger.info("Checking image file format...");
		
		ProcessRunner identify = new ProcessRunner();
		
		ArrayList<String> cmd = new ArrayList<String>();
		
		cmd.add("identify");
		cmd.add(inputImage.getAbsolutePath());
		
		identify.setCommand(cmd);
		identify.run();
		
		String output = identify.getProcessOutputAsString();
		String error = identify.getProcessErrorAsString();
		
		if(output.equalsIgnoreCase("")) {
			plogger.warn(error);
			return "unknown";
		}
		else {
			String[] parts = output.split(" ");
			String ext = parts[1];
			return ext;
		}
	}
	
	public static String checkImageMagickVersion() {
		ProcessRunner checker = new ProcessRunner();
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("identify");
		cmd.add("-version");
		checker.setCommand(cmd);
		checker.run();
		String error = checker.getProcessErrorAsString();
		String output = checker.getProcessOutputAsString();
		String version = "unknown";
		
		if(!error.equalsIgnoreCase("")) {
			plogger.error(error);
		}
		if(output!= null && !output.equalsIgnoreCase("")) {
			if(output.contains("ImageMagick")) {
				String[] parts = output.split(" ", 6);
				version = parts[2] + " " + parts[3] + " " + parts[4];
			}
		}
		
		return version;
	}

	private String getInputFileName(DigitalObject digOb, URI inputFormat) {
		String title = DigitalObjectUtils.getFileNameFromDigitalObject(digOb, inputFormat);
		
//		if(title==null || title.equals("")) {
//			String inputExt = formatRegistry.getFirstExtension(inputFormat);
//			fileName = DEFAULT_INPUT_FILE_NAME + "." + inputExt;
//		}
//		else {
//			fileName = title;
//		}
		
		return title;
	}

	private String getOutputFileName(String inputFileName, URI outputFormat) {
		String fileName = null;
		String outputExt = formatRegistry.getFirstExtension(outputFormat);
		if(inputFileName.contains(" ")) {
			inputFileName = inputFileName.replaceAll(" ", "_");
		}
		
		if(inputFileName.contains(".")) {
			fileName = inputFileName.substring(0, inputFileName.lastIndexOf(".")) + "." + outputExt;
		}
		else {
			fileName = inputFileName + "." + outputExt;
		}
		return fileName;
	}

	private boolean migrationSuccessful(File resultFile) {
		if(resultFile.exists() && resultFile.length() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	private boolean extensionsAreEqual(String extension1, String extension2) {
	    	if(extension1.contains(".")) {
	    		extension1 = extension1.replace(".", "");
	    	}
	    	if(extension2.contains(".")) {
	    		extension2 = extension2.replace(".", "");
	    	}
	//        plogger.info("Starting to compare these two extensions: " + extension1 + " and " + extension2);
	
	        Set <URI> ext1FormatURIs = formatRegistry.getUrisForExtension(extension1.toLowerCase());
	//        plogger.info("Got list of URIs for " + extension1);
	
	        Set <URI> ext2FormatURIs = formatRegistry.getUrisForExtension(extension2.toLowerCase());
	//        plogger.info("Got list of URIs for " + extension2);
	
	        boolean success = false;
	        
	        if(ext1FormatURIs==null || ext2FormatURIs==null) {
	        	if(extension1.equalsIgnoreCase(extension2)) {
	        		return true;
	        	}
	        	else {
	        		return false;
	        	}
	        }
	
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
	            plogger.info("Success! Actual format of input file verified!");
	        }
	        else {
	            plogger.info("Error! File has a different format than it should have!");
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
