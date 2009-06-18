package eu.planets_project.services.migration.imagemagick;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;


/**
 * local tests for image magick migrations
 *
 */
public class Im4JavaImageMagickMigrateTests {
	  
	static Migrate imageMagick;
    /* The location of this service when deployed. */
	
	static String wsdlLocation = "/pserv-pa-imagemagick/Im4JavaImageMagickMigrate?wsdl";
	
	/**
	 * test output
	 */
	public static String TEST_OUT = "Im4JavaImageMagickTestOutput";
	
	 public static List<String> compressionTypes = new ArrayList<String>();
	
    /**
     * test setup
     */
    @BeforeClass
    public static void setup() {
        // Config the logger:
        Logger.getLogger("").setLevel( Level.FINE );
        
//        System.setProperty("pserv.test.context", "server");
//        System.setProperty("pserv.test.host", "localhost");
//        System.setProperty("pserv.test.port", "8080");
        
        
        
        // This method handles the local/standalone/server test context setup:
        imageMagick = ServiceCreator.createTestService(Migrate.QNAME, Im4JavaImageMagickMigrate.class, wsdlLocation);
        
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
    }
    
    /**
     * Test the Description method.
     */
    @Test
    public void testDescribe() {
    	System.out.println("running Service at: " + imageMagick.QNAME);
        ServiceDescription desc = imageMagick.describe();
        System.out.println("Recieved service description: " + desc.toXmlFormatted());
        assertTrue("The ServiceDescription should not be NULL.", desc != null );
    }
    
    /**
     * test jpg compression
     * @throws IOException
     */
    @Test
    public void testJpgCompression () throws IOException {
    	String inputFormatExt = "TIFF";
        String outputFormatExt = "JPEG";
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_JPEG, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_NO, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_100));
    }
    
    /**
     * test png compression
     * @throws IOException
     */
    @Test
    public void testPngCompression () throws IOException {
    	
    	String inputFormatExt = "TIFF";
        String outputFormatExt = "PNG";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_100));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_NO, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_100));
    }
    
    
    /**
     * test jpg to tiff migration
     * @throws IOException
     */
    @Test
    public void testTiffToJP2 () throws IOException {
    	String inputFormatExt = "tiff";
        String outputFormatExt = "jp2";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_JPEG2000, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_JPEG2000, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_50));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * Test JPEG2 to PNG migration
     * @throws IOException
     */
    @Test
    public void testJP2ToPng () throws IOException {
    	String inputFormatExt = "JPC";
        String outputFormatExt = "png";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_50));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * Test for JPEG to TIFF migration
     * @throws IOException
     */
    @Test
    public void testJpgToTiff () throws IOException {
    	String inputFormatExt = "jpeg";
        String outputFormatExt = "tif";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));

        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_50));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * test jpg to png migration
     * @throws IOException
     */
   @Test
    public void testJpgToPng () throws IOException {
    	String inputFormatExt = "jpeg";
        String outputFormatExt = "png";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_75));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
   /**
    * test jpg to gif migration
    * @throws IOException
    */
    @Test
    public void testJpgToGif () throws IOException {
    	String inputFormatExt = "jpeg";
        String outputFormatExt = "gif";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_75));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * test png to tiff migration
     * @throws IOException
     */
    @Test
    public void testPngToTiff () throws IOException {
    	String inputFormatExt = "png";
        String outputFormatExt = "tiff";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_75));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * test pnf to jpg migration
     * @throws IOException
     */
    @Test
    public void testPngToJpg () throws IOException {
    	String inputFormatExt = "png";
        String outputFormatExt = "jpeg";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_JPEG, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_JPEG, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_50));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * test png to gif migration
     * @throws IOException
     */
    @Test
    public void testPngToGif () throws IOException {
    	String inputFormatExt = "png";
        String outputFormatExt = "gif";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_75));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * test tiff to jpg migration
     * @throws IOException
     */
    @Test
    public void testTiffToJpg () throws IOException {
    	String inputFormatExt = "tiff";
        String outputFormatExt = "jpeg";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_JPEG, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_JPEG, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_75));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }

    /**
     * test tiff to png migration
     * @throws IOException
     */
    @Test
    public void testTiffToPng () throws IOException {
    	String inputFormatExt = "tiff";
        String outputFormatExt = "png";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_75));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * test tiff to gif migration
     * @throws IOException
     */
    @Test
    public void testTiffToGif () throws IOException {
    	String inputFormatExt = "tiff";
        String outputFormatExt = "gif";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_75));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * test tiff to tga migration
     * @throws IOException
     */
    @Test
    public void testTiffToTga () throws IOException {
    	String inputFormatExt = "tiff";
        String outputFormatExt = "tga";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_RLE, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_RLE, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_75));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * test tga to tiff migration
     * @throws IOException
     */
    @Test
    public void testTgaToTiff () throws IOException {
    	String inputFormatExt = "tga";
        String outputFormatExt = "tiff";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_RLE, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_RLE, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_75));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    
    /**
     * test tiff to pcx migration
     * @throws IOException
     */
    @Test
    public void testTiffToPcx () throws IOException {
    	String inputFormatExt = "tiff";
        String outputFormatExt = "pcx";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_RLE, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_RLE, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_50));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    /**
     * test pcx to tiff migration
     * @throws IOException
     */
    @Test
    public void testPcxToTiff () throws IOException {
    	String inputFormatExt = "pcx";
        String outputFormatExt = "tiff";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_LZW, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_50));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    

    
    /**
     * test tiff to pdf migration
     * @throws IOException
     */
    @Test
    public void testTiffToPdf () throws IOException {
    	String inputFormatExt = "tiff";
        String outputFormatExt = "pdf";
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_JPEG, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_25));
        
        testMigrate(inputFormatExt, outputFormatExt, createParameters(Im4JavaImageMagickMigrateTestHelper.COMP_TYPE_JPEG, Im4JavaImageMagickMigrateTestHelper.COMP_QUAL_50));
        
        testMigrate(inputFormatExt, outputFormatExt, null);
    }
    
    private List<Parameter> createParameters(String compressionType, double imageQuality) {
    	if(compressionType==null || imageQuality==-1) {
    		return null;
    	}
    	
    	List<Parameter> parameterList = new ArrayList<Parameter>();
    	
    	if((compressionType!=null) && (imageQuality!=-1)) {
            parameterList.add(new Parameter("compressionType", compressionType));
            parameterList.add(new Parameter("imageQuality", String.valueOf(imageQuality)));
          
    	}
    	
		return parameterList;
    }

    private File getTestFile(String srcExtension) {
    	File testFile = null;
    	
    	if (srcExtension.equalsIgnoreCase("BMP")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.BMP_TEST_FILE);
    		return testFile;
		}
    	
    	if (srcExtension.equalsIgnoreCase("GIF")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.GIF_TEST_FILE);
    		return testFile;
		}
    	
    	if(srcExtension.equalsIgnoreCase("JPG") || srcExtension.equalsIgnoreCase("JPEG")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.JPG_TEST_FILE);
    		return testFile;
    	}
    	
    	if(srcExtension.equalsIgnoreCase("JP2") || srcExtension.equalsIgnoreCase("J2K")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.JP2_TEST_FILE);
    		return testFile;
    	}
    	
    	if(srcExtension.equalsIgnoreCase("JPC")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.JPC_TEST_FILE);
    		return testFile;
    	}
    	
    	if (srcExtension.equalsIgnoreCase("PCX")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.PCX_TEST_FILE);
    		return testFile;
		}
    	
    	if (srcExtension.equalsIgnoreCase("PDF")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.PDF_TEST_FILE);
    		return testFile;
		}
    	
    	if (srcExtension.equalsIgnoreCase("PNG")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.PNG_TEST_FILE);
    		return testFile;
		}
    	
    	if (srcExtension.equalsIgnoreCase("RAW")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.RAW_TEST_FILE);
    		return testFile;
		}
    	
    	if (srcExtension.equalsIgnoreCase("TGA")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.TGA_TEST_FILE);
    		return testFile;
		}
    	
    	if (srcExtension.equalsIgnoreCase("TIF") || srcExtension.equalsIgnoreCase("TIFF")) {
    		testFile = new File(Im4JavaImageMagickMigrateTestHelper.TIFF_TEST_FILE);
    		return testFile;
		}
    	return null;
    }

    /**
     * Test the pass-thru migration.
     * @param srcExtension 
     * @param targetExtension 
     * @param parameters 
     * @throws IOException 
     */
    public void testMigrate(String srcExtension, String targetExtension, List<Parameter> parameters) throws IOException {

        File inputFile = getTestFile(srcExtension);

        FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
        DigitalObject input = new  DigitalObject.Builder(Content.byReference(inputFile))
        	.format(format.createExtensionUri(srcExtension))
        	.title(inputFile.getName())
        	.build();
        System.out.println("Input: " + input);

        MigrateResult mr = imageMagick.migrate(input, format
                .createExtensionUri(srcExtension), format
                .createExtensionUri(targetExtension), parameters);

        ServiceReport sr = mr.getReport();
        System.out.println("Got Report: "+sr);

        DigitalObject doOut = mr.getDigitalObject();

        assertTrue("Resulting digital object is null.", doOut != null);

        System.out.println("DigitalObject.getTitle(): " + doOut.getTitle());
        System.out.println("DigitalObject.getFormat(): " + doOut.getFormat().toASCIIString());

        String compressionType = "None";
        double imageQuality= 100.00;
        String compressionTypeStr = "";

        if(parameters!=null) {

            for (Iterator<Parameter> iterator = parameters.iterator(); iterator.hasNext();) {
                Parameter parameter = (Parameter) iterator.next();
                String name = parameter.getName();
                if(name.equalsIgnoreCase("compressionType")) {
                    compressionType = parameter.getValue();
                }
                if(name.equalsIgnoreCase("imageQuality")) {
                    imageQuality = Double.parseDouble(parameter.getValue());
                }
            }
            compressionTypeStr = "-" + compressionType;
        }
        else {
            compressionType = "None";		// Setting compressionType to default value = No compression
            imageQuality = 100.00;	// Setting compressionQuality to default value = 100%
            compressionTypeStr = "-" + "DEFAULT_NO_COMP";
        }      

        File outFolder = FileUtils.createWorkFolderInSysTemp(TEST_OUT + File.separator + srcExtension.toUpperCase() + "-" + targetExtension.toUpperCase());
        String outFileName = 

            srcExtension 
            + "_To_" 
            + targetExtension 
            + compressionTypeStr
            + "_"
            + imageQuality
            + "."
            + targetExtension;

        //            ByteArrayHelper.writeToDestFile(doOut.getContent().getValue(), outFile.getAbsolutePath());
        File outFile = new File(outFolder, outFileName);
        if(outFile.exists()) {
            outFile.delete();
        }
        outFile = FileUtils.writeInputStreamToFile(doOut.getContent().read(), outFolder, outFileName);

        System.out.println("Please find the result file here: " + outFile.getAbsolutePath() + "\n\n");
        assertTrue("Result file created?", outFile.canRead());
            
    }

}

