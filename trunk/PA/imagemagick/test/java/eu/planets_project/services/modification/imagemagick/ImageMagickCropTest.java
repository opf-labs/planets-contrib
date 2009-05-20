/**
 * 
 */
package eu.planets_project.services.modification.imagemagick;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
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
public class ImageMagickCropTest {
	
	File test_tmp_folder = FileUtils.createWorkFolderInSysTemp("ImageMagickCrop_test_tmp".toUpperCase()); 
	File testFile = new File("tests/test-files/images/bitmap/test_tiff/2326378356_65c3b81dfd.tif");
	
	Modify im_crop = new ImageMagickCrop();
	
	static FormatRegistry formatReg = FormatRegistryFactory.getFormatRegistry();
	
	static URI actionURI = null;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		actionURI = new URI("planets:mod/crop");
	}

	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#describe()}.
	 */
	@Test
	public void testDescribe() {
		System.out.println("running Service at: " + im_crop.QNAME);
	    ServiceDescription desc = im_crop.describe();
	    System.out.println("Recieved service description: " + desc.toXmlFormatted());
	    assertTrue("The ServiceDescription should not be NULL.", desc != null );
	}

	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.util.List)}.
	 */
	@Test
	public void testModifyTwoPointNotation() {
		DigitalObject input = new DigitalObject.Builder(Content.byReference(testFile)).build();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("top_left_point", "70,70"));
		parameters.add(new Parameter("bottom_right_point", "400,250"));
		
		ModifyResult mr = im_crop.modify(input, formatReg.createExtensionUri("tiff"), parameters);
		assertTrue("ModifyResult should not be NULL!", mr!=null);
		File result = new File(test_tmp_folder, "cropped.tiff");
		FileUtils.writeInputStreamToFile(mr.getDigitalObject().getContent().read(), result);
		assertTrue("File has been written correctly!", result.exists());
	}
	
	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.util.List)}.
	 */
	@Test
	public void testModifyPointWidthHeightNotation() {
		DigitalObject input = new DigitalObject.Builder(Content.byReference(testFile)).build();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("top_left_point", "70,70"));
		parameters.add(new Parameter("crop_area_size", "330,180"));
		ModifyResult mr = im_crop.modify(input, formatReg.createExtensionUri("tiff"), parameters);
		assertTrue("ModifyResult should not be NULL!", mr!=null);
		File result = new File(test_tmp_folder, "cropped1.tiff");
		FileUtils.writeInputStreamToFile(mr.getDigitalObject().getContent().read(), result);
		assertTrue("File has been written correctly!", result.exists());
	}
	
	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.util.List)}.
	 */
	@Test
	public void testModifyBothNotation() {
		DigitalObject input = new DigitalObject.Builder(Content.byReference(testFile)).build();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("top_left_point", "70,70"));
		parameters.add(new Parameter("bottom_right_point", "400,250"));
		parameters.add(new Parameter("crop_area_size", "330,180"));
		ModifyResult mr = im_crop.modify(input, formatReg.createExtensionUri("tiff"), parameters);
		assertTrue("ModifyResult should not be NULL!", mr!=null);
		File result = new File(test_tmp_folder, "cropped2.tiff");
		FileUtils.writeInputStreamToFile(mr.getDigitalObject().getContent().read(), result);
		assertTrue("File has been written correctly!", result.exists());
	}
	
	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.util.List)}.
	 */
	@Test
	public void testModifyBothWithoutTopLeft() {
		DigitalObject input = new DigitalObject.Builder(Content.byReference(testFile)).build();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("bottom_right_point", "400,250"));
		parameters.add(new Parameter("crop_area_size", "330,180"));
		ModifyResult mr = im_crop.modify(input, formatReg.createExtensionUri("tiff"), parameters);
		assertTrue("ModifyResult should not be NULL!", mr!=null);
		File result = new File(test_tmp_folder, "cropped3.tiff");
		FileUtils.writeInputStreamToFile(mr.getDigitalObject().getContent().read(), result);
		assertTrue("File has been written correctly!", result.exists());
	}
}
