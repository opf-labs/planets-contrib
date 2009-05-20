/**
 * 
 */
package eu.planets_project.services.modification.imagemagick;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;

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

@WebService(name = ImageMagickCrop.NAME, 
        serviceName = Modify.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.modify.Modify" )
public class ImageMagickCrop implements Modify {
	
	public static final String NAME = "ImageMagickCrop";
	
	private PlanetsLogger PLOGGER = PlanetsLogger.getLogger(this.getClass()) ;
	
	private String workFolderName = "IMAGEMAGICK_CROP_TMP";
	private File work_folder = FileUtils.createWorkFolderInSysTemp(workFolderName);
	private String inputImageName = "imageMagickCropInput";
	private String resultImageName = "imageMagickCropResult";
	private String extension = null;
	
	private Point top_left = new Point();
	private Point bottom_right = new Point();
	
	private Dimension crop_dimensions = new Dimension();
	
	private String top_left_point = "top_left_point";
	private String bottom_right_point = "bottom_right_point";
	private String crop_area_size = "crop_area_size"; 
	
	private boolean top_left_set = false;
	private boolean bottom_right_set = false;
	private boolean crop_dimensions_set = false;
	private boolean useTwoPointNotation = false;
	private boolean useWidthAndHeightNotation = false;
	
	private String br = System.getProperty("line.separator");
	
	private static FormatRegistry formatReg = FormatRegistryFactory.getFormatRegistry();
	
	private List<URI> inFormats = null;
	
	public ImageMagickCrop () {
		File im_home = new File(System.getenv("IMAGEMAGICK_HOME"));
		// cleaning the TMP folder first
		FileUtils.deleteTempFiles(work_folder);
		work_folder = FileUtils.createWorkFolderInSysTemp(workFolderName);
		// Setting the installation dir for ImageMagick to make im4java work on windows platforms 
		IMGlobalSettings.setImageMagickHomeDir(im_home);
		// Use the JBoss-Classloader, instead of the Systemclassloader.
		System.setProperty("jmagick.systemclassloader","no"); 
	    PLOGGER.info("Hello! Initializing and starting ImageMagickCrop service!");
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
		sd.properties(new Property.Builder(URI.create("planets:mod/crop")).name("Supported modification action").value("crop").build());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("This service uses ImageMagick to crop images. That means, that you will have to install ImageMagick on the machine" +
        		"where this service will be deployed to get this to work. ");
        sd.classname(this.getClass().getCanonicalName());
        sd.version("1.0");
        sd.tool(Tool.create(null, "ImageMagick", "v6.3.9-Q8", null, "http://www.imagemagick.org"));
        // InputFormats are created on the fly, depending on the system where the service is deployed. 
        sd.inputFormats(inFormats.toArray(new URI[]{}));
        URI cropActionUri = formatReg.createActionUri("crop");
        sd.properties(new Property.Builder(cropActionUri).name("Supported modification ation").value(cropActionUri.toASCIIString()).description("this service crops images. You can specify the area that should be kept, all surrounding parts of the image will be deleted.").build());
        return sd.build();
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.services.modify.Modify#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, java.util.List)
	 */
	public ModifyResult modify(DigitalObject digitalObject, URI inputFormat,
			List<Parameter> parameters) {

		extension = "." + formatReg.getFirstExtension(inputFormat);
		File inputFile = new File(work_folder, inputImageName + extension); 
		FileUtils.writeInputStreamToFile(digitalObject.getContent().read(), inputFile);
		
		Rectangle croppingArea = null;
		
		boolean parameters_correct = parseParameters(parameters);
		
		if(!parameters_correct) {
			return this.returnWithErrorMessage("Don't understand the parameters you've passed to this service! Sorry." + br +
					"One of the following issues could be the cause: 1) You don't have passed params, so I don't know what to do now ;-)" + br + 
					"You have passed the parameters with bad syntax. Please check and see the hints below for further information." + br + 
					"You have to specify the cropping parameters the following way:" + br + 
					"1) top_left_point = X,Y (with X and Y beeing the coordinates this point should have!)" + br + 
					"   bottom_right_point = X,Y (with X and Y beeing the coordinates this point should have!)" + br +
					"2) top_left_point = X,Y (with X and Y beeing the coordinates this point should have!)" + br + 
					"   crop_area_size = WIDTH,HEIGHT (with WIDTH and HEIGHT beeing the width and height (in pixel) of the area to keep!)" + br + 
					"If no top_left_point is specified, it is assumed to be located at (0,0) which is the upper left corner of the image.", null);
		}
		
		croppingArea = createCroppingArea(inputFile);
		
		IMOperation op = new IMOperation();
	    op.addImage(inputFile.getAbsolutePath());
	    
	    op.crop(croppingArea.width, croppingArea.height, croppingArea.x, croppingArea.y);
	    
	    op.p_repage();
	    op.addImage(work_folder.getAbsolutePath() + File.separator + resultImageName + extension);
	    
	    try {
	    	ConvertCmd convert = new ConvertCmd();
		    List<String> commands = op.getCmdArgs();
//		    for (String string : commands) {
//				System.out.print(string + " ");
//			}
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
		
		File resultFile = new File(work_folder.getAbsolutePath() + File.separator + resultImageName + extension);
		
		if(!resultFile.exists()) {
			return this.returnWithErrorMessage("No result file found. Something has gone terribly wrong during this operation. Sorry.", null);
		}
		
		DigitalObject result = new DigitalObject.Builder(Content.byReference(resultFile))
									.format(inputFormat)
									.build();
		
		ServiceReport report = new ServiceReport(Type.INFO, Status.SUCCESS, "Crop operation executed successfully (as far as I can tell from here ;-) ).");
		
		ModifyResult mr = new ModifyResult(result, report);
		
		return mr;
	}
	
	
	private boolean parseParameters(List<Parameter> parameters) {
		if(parameters!=null) {
			if(parameters.size()>0) {
				for (Parameter parameter : parameters) {
					String currentName = parameter.getName();
					String currentValue = parameter.getValue();
					if(currentName.equalsIgnoreCase(top_left_point)) {
						top_left = parsePointParam(currentValue);
						top_left_set = true;
						continue;
					}
					if(currentName.equalsIgnoreCase(bottom_right_point)) {
						bottom_right = parsePointParam(currentValue);
						bottom_right_set = true;
						continue;
					}
					if(currentName.equalsIgnoreCase(crop_area_size)) {
						crop_dimensions = parseDimensionParam(currentValue);
						crop_dimensions_set = true;
						continue;
					}
				}
				// if no top left point is specified it's set to (0, 0)
				if(!top_left_set) {
					top_left.setLocation(0, 0);
					top_left_set = true;
				}
				if(top_left_set && crop_dimensions_set) {
					useWidthAndHeightNotation = true;
					useTwoPointNotation = false;
				}
				else if(top_left_set && bottom_right_set) {
					useTwoPointNotation = true;
				}
				
				else {
					// wrong parameters passed
					return false;
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
	
	
	private Point parsePointParam(String paramValue) {
		Point point = new Point();
		StringTokenizer cutter = new StringTokenizer(paramValue, ",");
		if(cutter.countTokens()!=2) {
			return null;
		}
		if(cutter.hasMoreTokens()) {
			point.x = Integer.parseInt(cutter.nextToken());
		}
		if(cutter.hasMoreTokens()) {
			point.y = Integer.parseInt(cutter.nextToken());
		}
		return point;
	}
	
	
	private Dimension parseDimensionParam(String paramValue) {
		Dimension dimension = new Dimension();
		StringTokenizer cutter = new StringTokenizer(paramValue, ",");
		if(cutter.countTokens()!=2) {
			return null;
		}
		if(cutter.hasMoreTokens()) {
			dimension.width = Integer.parseInt(cutter.nextToken());
		}
		if(cutter.hasMoreTokens()) {
			dimension.height = Integer.parseInt(cutter.nextToken());
		}
		return dimension;
		
	}
	
	
	private Rectangle createCroppingArea(File inputFile) {
		
		if(useTwoPointNotation) {
			ImageInfo imageInfo = null;
		    MagickImage image = null;
		    Dimension input_file_dimension = null;
		    
			try {
				
				imageInfo = new ImageInfo(inputFile.getAbsolutePath());
				image = new MagickImage(imageInfo);
			    input_file_dimension = image.getDimension();
			    
			} catch (MagickException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return getCroppingAreaFromTwoPointsNotation(input_file_dimension, top_left, bottom_right);
		}
		
		if(useWidthAndHeightNotation){
			return getCroppingAreaFromPointWidthHeightNotation(top_left, crop_dimensions);
		}
		return null;
	}
	
	
	private Rectangle getCroppingAreaFromPointWidthHeightNotation(Point top_left, Dimension crop_area_size) {
		Rectangle croppingArea = new Rectangle(top_left, crop_area_size);
		return croppingArea;
	}
	
	
	private Rectangle getCroppingAreaFromTwoPointsNotation(Dimension imageSizeInPixel, Point top_left, Point bottom_right) {
	    
	    int height = imageSizeInPixel.height;
	    int width = imageSizeInPixel.width;
	    
	    int right_border_width		= top_left.x;
	    int left_border_width		= width - bottom_right.x;
	    
	    int top_border_height		= top_left.y;
	    int bottom_border_height	= height - bottom_right.y;
	    
	    int selection_height 		= height - top_border_height - bottom_border_height;
	    int selection_width 		= width - right_border_width - left_border_width;
	    
	    Dimension selectionSize = new Dimension(selection_width, selection_height);
	    
	    Rectangle selection = new Rectangle(top_left, selectionSize);
	    return selection;
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
