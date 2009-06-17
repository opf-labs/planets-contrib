/**
 * 
 */
package eu.planets_project.services.modification.imagemagick;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

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
import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.identification.imagemagick.utils.ImageMagickHelper;
import eu.planets_project.services.modify.Modify;
import eu.planets_project.services.modify.ModifyResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ServiceUtils;

/**
 * @author melmsp
 *
 */

@Stateless
@Local(Modify.class)
@Remote(Modify.class)

@WebService(name = ImageMagickRotate.NAME, 
        serviceName = Modify.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.modify.Modify" )
public class ImageMagickRotate implements Modify {
	
	public static final String NAME = "ImageMagickRotate";
	
	private PlanetsLogger PLOGGER = PlanetsLogger.getLogger(this.getClass()) ;
	
	private String workFolderName = "IMAGEMAGICK_ROTATE_TMP";
	private File work_folder = FileUtils.createFolderInWorkFolder(FileUtils.getPlanetsTmpStoreFolder(), workFolderName);
//	private String sessionID = FileUtils.randomizeFileName("");
//	private String inputImageName = "imageMagickRotateInput" + sessionID;
//	private String resultImageName = "imageMagickRotateResult" + sessionID;
	private String inputImageName = "imageMagickRotateInput";
	private String resultImageName = "imageMagickRotateResult";
	private String extension = null;
	
	private String rotateClockwise = "rotateClockwise";
	private String rotateCounterClockwise = "rotateCounterClockwise";
	private double degrees = -1;
	private String backgroundColor = "backgroundColor";
	private String color = null;
	private boolean color_set = false;
	
	
	private String br = System.getProperty("line.separator");
	
	private static FormatRegistry formatReg = FormatRegistryFactory.getFormatRegistry();
	
	private List<URI> inFormats = null;
	
	public ImageMagickRotate () {
		String im_home_path = System.getenv("IMAGEMAGICK_HOME");
		if(im_home_path!=null) {
			// Setting the installation dir for ImageMagick to make im4java work on windows platforms
			File im_home = new File(im_home_path);
			IMGlobalSettings.setImageMagickHomeDir(im_home);
		}
		else {
			PLOGGER.error("The System variable IMAGEMAGICK_HOME is not set properly. " +
					"Please install ImageMagick and set up a system variable pointing to the ImageMagick " +
					"installation folder! Otherwise this service won't work on Windows OS!");
		}
		
		// Use the JBoss-Classloader, instead of the Systemclassloader.
		System.setProperty("jmagick.systemclassloader","no"); 
	    PLOGGER.info("Hello! Initializing and starting ImageMagickRotate service!");
	    // getting formats supported by ImageMagick on THIS system (the system where this service is running)
	    // This may vary from system to system, depending on which external libraries are installed for image handling (e.g. jasper for JP2000 etc.)
	    inFormats = ImageMagickHelper.getSupportedInputFormats();
	    PLOGGER.info("Ready!");
	}
	
	/* (non-Javadoc)
	 * @see eu.planets_project.services.PlanetsService#describe()
	 */
	public ServiceDescription describe() {
		ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME, Modify.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("This service uses ImageMagick (via 'im4java') to rotate images. That means, that you will have to install ImageMagick on the machine" +
        		" where this service will be deployed to get this to work. ");
        sd.instructions("You can specify the amount of degrees, about which the image will be rotated. This rotation can be performed clockwise" + 
        		br + "(use \"rotateClockwise\"-param or counter-clockwise (use \"rotateCounterClockwise\" param." + 
        				br + "You can also give a background color used to fill the image areas added during rotation." + 
        				br + "Please see http://www.imagemagick.org/script/color.php for allowed values/color-names.");
        List<Parameter> parameters = new ArrayList<Parameter>();
        Parameter rotateClockwise = new Parameter.Builder(this.rotateClockwise, "0.00")
        								.description("The amount of degrees to rotate the image. This parameter is used to rotate clockwise.")
        								.type("double")
        								.build();
        Parameter rotateCounterClockwise = new Parameter.Builder(this.rotateCounterClockwise, "0.00")
        								.description("The amount of degrees to rotate the image. This parameter is used to rotate counter-clockwise.")
        								.type("double")
        								.build();
        Parameter backgroundColor = new Parameter.Builder(this.backgroundColor, "Please see http://www.imagemagick.org/script/color.php for allowed values/color-names.")
        								.description("When you rotate an image, the resulting image " +
        										"will be larger than the original to make sure no part " +
        										"of the image is cropped. You can specify here the color " +
        										"that will be used to fill empty areas of the image. " +
        										"Please see http://www.imagemagick.org/script/color.php " +
        										"for allowed values/color-names. The DEFAULT if no background color is specified is WHITE")
        								.type("String")
        								.build();
        parameters.add(rotateClockwise);
        parameters.add(rotateCounterClockwise);
        parameters.add(backgroundColor);
        sd.classname(this.getClass().getCanonicalName());
        sd.version("1.0");
        sd.tool(Tool.create(null, "ImageMagick", "v6.3.9-Q8", null, "http://www.imagemagick.org"));
        // InputFormats are created on the fly, depending on the system where the service is deployed. 
        sd.inputFormats(inFormats.toArray(new URI[]{}));
        URI rotateActionUri = formatReg.createActionUri("rotate");
        sd.properties(new Property.Builder(rotateActionUri).name("Supported modification ation").value(rotateActionUri.toASCIIString()).description("this service rotate images.").unit("Degree").build());
        return sd.build();
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.services.modify.Modify#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, java.util.List)
	 */
	public ModifyResult modify(DigitalObject digitalObject, URI inputFormat,
			List<Parameter> parameters) {

		extension = "." + formatReg.getFirstExtension(inputFormat);
		
		URI testIn = formatReg.createExtensionUri(formatReg.getFirstExtension(inputFormat));
		
		if(inFormats!=null) {
			if(testIn!=null) {
			    if(!inFormats.contains(testIn)) {
			    	return returnWithErrorMessage("The input format: " + inputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!", null);
			    }
			}
			else  {
				return returnWithErrorMessage("The input format: " + inputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!", null);
			}
		}
		
		File inputFile = new File(work_folder, FileUtils.randomizeFileName(inputImageName + extension)); 
		FileUtils.writeInputStreamToFile(digitalObject.getContent().read(), inputFile);
		
		boolean parameters_correct = parseParameters(parameters);
		
		if(!parameters_correct) {
			return this.returnWithErrorMessage("Don't understand the parameters you've passed to this service! Sorry.", null);
		}
		
		File resultFile = new File(work_folder.getAbsolutePath() + File.separator + FileUtils.randomizeFileName(resultImageName + extension));
		
		IMOperation op = new IMOperation();
	    op.addImage(inputFile.getAbsolutePath());
	    
	    if(color_set) {
	    	op.background(color);
	    }
	    
	    op.rotate(degrees);
	    
	    op.addImage(resultFile.getAbsolutePath());
	    
	    try {
	    	ConvertCmd convert = new ConvertCmd();
		    List<String> commands = op.getCmdArgs();
		    for (String string : commands) {
				System.out.print(string + " ");
			}
			convert.run(op);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IM4JavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		File resultFile = new File(work_folder.getAbsolutePath() + File.separator + resultImageName + extension);
		
		if(!resultFile.exists()) {
			return this.returnWithErrorMessage("No result file found. Something has gone terribly wrong during this operation. Sorry.", null);
		}
		
		DigitalObject result = new DigitalObject.Builder(Content.byReference(resultFile))
									.format(inputFormat)
									.build();
		
		ServiceReport report = new ServiceReport(Type.INFO, Status.SUCCESS, "Rotate operation executed successfully (as far as I can tell from here ;-) ).");
		
		ModifyResult mr = new ModifyResult(result, report);
		
		return mr;
	}
	
	
	private boolean parseParameters(List<Parameter> parameters) {
		if(parameters!=null) {
			if(parameters.size()>0) {
				for (Parameter parameter : parameters) {
					String currentName = parameter.getName();
					String currentValue = parameter.getValue();
					if(currentName.equalsIgnoreCase(rotateClockwise)) {
						degrees = Double.parseDouble(currentValue);
						continue;
					}
					if(currentName.equalsIgnoreCase(rotateCounterClockwise)) {
						degrees = Double.parseDouble(currentValue) * (-1);
						continue;
					}
					if(currentName.equalsIgnoreCase(backgroundColor)) {
						color = currentValue;
						color_set = true;
					}
				}
				return true;
			}
			// size of parameters list == 0 --> return false;
			else {
				return false; 
			}
		}
		// no parameters passed (== null) --> return false; 
		else {
			return false;
		}
	}
	
	
	/**
	 * @param message an optional message on what happened to the service
	 * @param e the Exception e which causes the problem
	 * @return CharacteriseResult containing a Error-Report
	 */
	private ModifyResult returnWithErrorMessage(final String message,
	        final Exception e) {
	    if (e == null) {
	        return new ModifyResult(null, ServiceUtils
	                .createErrorReport(message));
	    } else {
	        return new ModifyResult(null, ServiceUtils
	                .createExceptionErrorReport(message, e));
	    }
	}

}
