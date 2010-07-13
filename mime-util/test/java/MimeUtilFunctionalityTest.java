import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;

public class MimeUtilFunctionalityTest {

	public static File testFile1, testFile2, testFile3, testFile4, testFile5;
	public File[] files;

	@Before
	public void setUp() throws Exception {
		testFile1 = new File(
				"D:/Implementation/Planets/_non-svn/SampleImages/forum_europe_png.png");
		testFile2 = new File(
				"D:/Implementation/Planets/_non-svn/SampleImages/Sunflower_as_GIF.gif");
		testFile3 = new File(
				"D:/Implementation/Planets/_non-svn/SampleImages/TIFF/00000001.tif");
		testFile4 = new File(
				"D:/Implementation/Planets/_non-svn/SampleImages/usa_bundesstaaten_png.png");
		testFile5 = new File(
				"D:/Implementation/Planets/_non-svn/SampleImages/CB_TM432_jp2.jp2");
		files = new File[] {testFile1, testFile2, testFile3, testFile4, testFile5};
	}

	@After
	public void tearDown() throws Exception {
	}
	
	private boolean compareMediaType(int fileNr, String extractedType){
		if(fileNr>0||fileNr<6){
			return extractedType.equals("image") ?  true : false;
		}
		
		return false;
	}
	
	private boolean compareSubType(int fileNr, String extractedType){
		if(fileNr==1||fileNr==4){
			return extractedType.equals("png") ?  true : false;
		}
		if(fileNr==2){
			return extractedType.equals("gif") ?  true : false;
		}
		if(fileNr==3){
			return extractedType.equals("tiff") ?  true : false;
		}
		if(fileNr==5){
			return extractedType.equals("jp2") ?  true : false;
		}
		return false;
	}

	@Test
	public void testOpendesktopMimeDetector() {
		MimeUtil
				.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
		
		int i = 1;
		for(File f : files){
			System.out.println("OpendesktopMimeDetector "+f.getAbsolutePath());
			try{
				MimeType m = MimeUtil.getMostSpecificMimeType(MimeUtil
						.getMimeTypes(f));
				System.out.println(m.getMediaType()+" ;"+m.getSpecificity()+" ;"+m.getSubType());
				System.out.println("==================");
				
				assertTrue(compareMediaType(i, m.getMediaType()));
				assertTrue(compareSubType(i, m.getSubType()));
			}
			catch(Exception e){
				System.out.println(e.toString());
			}
			i++;
		}
		
		MimeUtil
		.unregisterMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");

	}

	@Test
	public void testExtensionMimeDetector() {
		MimeUtil
				.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
		int i = 1;
		for(File f : files){
			System.out.println("ExtensionMimeDetector "+f.getAbsolutePath());
			MimeType m = MimeUtil.getMostSpecificMimeType(MimeUtil
					.getMimeTypes(f));
			System.out.println(m.getMediaType()+" ;"+m.getSpecificity()+" ;"+m.getSubType());
			System.out.println("==================");
			
			assertTrue(compareMediaType(i, m.getMediaType()));
			assertTrue(compareSubType(i, m.getSubType()));
			i++;
			
		}
		MimeUtil
				.unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
	}

	
	@Test
	public void testMagicMimeMimeDetector() {
		MimeUtil
				.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		
		int i = 1;
		for(File f : files){
			System.out.println("MagicMimeMimeDetector "+f.getAbsolutePath());
			MimeType m = MimeUtil.getMostSpecificMimeType(MimeUtil
					.getMimeTypes(f));
			System.out.println(m.getMediaType()+" ;"+m.getSpecificity()+" ;"+m.getSubType());
			System.out.println("==================");
			
			assertTrue(compareMediaType(i, m.getMediaType()));
			assertTrue(compareSubType(i, m.getSubType()));
			i++;
		}
		
		MimeUtil
				.unregisterMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
	}

}
