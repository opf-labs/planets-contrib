/**
 * 
 */
package eu.planets_project.services.modification.imagemagick;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.modify.Modify;
import eu.planets_project.services.modify.ModifyResult;
import eu.planets_project.services.utils.FileUtils;

/**
 * @author melmsp
 *
 */
public class ImageMagickRotateTest {
	
	File test_tmp_folder = FileUtils.createWorkFolderInSysTemp("ImageMagickRotate_test_tmp".toUpperCase()); 
	File testFile = new File("tests/test-files/images/bitmap/test_tiff/2326378356_65c3b81dfd.tif");
	
	Modify im_rotate = new ImageMagickRotate();
	
	static FormatRegistry formatReg = FormatRegistryFactory.getFormatRegistry();
	
	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#describe()}.
	 */
	@Test
	public void testDescribe() {
		System.out.println("running Service at: " + im_rotate.QNAME);
	    ServiceDescription desc = im_rotate.describe();
	    System.out.println("Recieved service description: " + desc.toXmlFormatted());
	    assertTrue("The ServiceDescription should not be NULL.", desc != null );
	}

	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.util.List)}.
	 */
	@Test
	public void testModifyRotateClockwise() {
		DigitalObject input = new DigitalObject.Builder(Content.byReference(testFile)).build();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("rotateClockwise", "0.5"));
		ModifyResult mr = im_rotate.modify(input, formatReg.createExtensionUri("tiff"), parameters);
		assertTrue("ModifyResult should not be NULL!", mr!=null);
		File result = new File(test_tmp_folder, "rotated_CW.tiff");
		FileUtils.writeInputStreamToFile(mr.getDigitalObject().getContent().read(), result);
		assertTrue("File has been written correctly!", result.exists());
	}
	
	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.util.List)}.
	 */
	@Test
	public void testModifyRotateCounterClockwise() {
		DigitalObject input = new DigitalObject.Builder(Content.byReference(testFile)).build();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("rotateCounterClockwise", "45.00"));
		ModifyResult mr = im_rotate.modify(input, formatReg.createExtensionUri("tiff"), parameters);
		assertTrue("ModifyResult should not be NULL!", mr!=null);
		File result = new File(test_tmp_folder, "rotated_CCW.tiff");
		FileUtils.writeInputStreamToFile(mr.getDigitalObject().getContent().read(), result);
		assertTrue("File has been written correctly!", result.exists());
	}
	
	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.util.List)}.
	 */
	@Test
	public void testModifyRotateClockwiseWithBackground() {
		DigitalObject input = new DigitalObject.Builder(Content.byReference(testFile)).build();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("rotateClockwise", "0.5"));
		parameters.add(new Parameter("backgroundColor", "CornflowerBlue"));
		ModifyResult mr = im_rotate.modify(input, formatReg.createExtensionUri("tiff"), parameters);
		assertTrue("ModifyResult should not be NULL!", mr!=null);
		File result = new File(test_tmp_folder, "rotated_CW_background.tiff");
		FileUtils.writeInputStreamToFile(mr.getDigitalObject().getContent().read(), result);
		assertTrue("File has been written correctly!", result.exists());
	}
}
