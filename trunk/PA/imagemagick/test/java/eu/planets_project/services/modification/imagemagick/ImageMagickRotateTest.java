/**
 * 
 */
package eu.planets_project.services.modification.imagemagick;

import static org.junit.Assert.assertTrue;

import java.io.File;
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
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * @author melmsp
 *
 */
public class ImageMagickRotateTest {
	
	String WSDL = "/pserv-pa-imagemagick/ImageMagickRotate?wsdl";
	
	File testFile = new File("tests/test-files/images/bitmap/test_tiff/2326378356_65c3b81dfd.tif");
	
	Modify im_rotate = ServiceCreator.createTestService(Modify.QNAME, ImageMagickRotate.class, WSDL);
	
	static FormatRegistry formatReg = FormatRegistryFactory.getFormatRegistry();
	
	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#describe()}.
	 */
	@Test
	public void testDescribe() {
		System.out.println("running Service at: " + Modify.QNAME);
	    ServiceDescription desc = im_rotate.describe();
	    System.out.println("Recieved service description: " + desc.toXmlFormatted());
	    assertTrue("The ServiceDescription should not be NULL.", desc != null );
	}
	
	@BeforeClass
	public static void setup() {
//		System.setProperty("pserv.test.context", "server");
//        System.setProperty("pserv.test.host", "localhost");
//        System.setProperty("pserv.test.port", "8080");
	}

	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.util.List)}.
	 */
	@Test
	public void testModifyRotateClockwise() {
		DigitalObject input = new DigitalObject.Builder(Content.byReference(testFile)).build();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("rotateClockwise", "17.63"));
		ModifyResult mr = im_rotate.modify(input, formatReg.createExtensionUri("tiff"), parameters);
		assertTrue("ModifyResult should not be NULL!", mr!=null);
		File result = DigitalObjectUtils.toFile(mr.getDigitalObject());
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
		File result = DigitalObjectUtils.toFile(mr.getDigitalObject());
		assertTrue("File has been written correctly!", result.exists());
	}
	
	/**
	 * Test method for {@link eu.planets_project.services.modification.imagemagick.ImageMagickCrop#modify(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.util.List)}.
	 */
	@Test
	public void testModifyRotateClockwiseWithBackground() {
		DigitalObject input = new DigitalObject.Builder(Content.byReference(testFile)).build();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("rotateClockwise", "7.75"));
		parameters.add(new Parameter("backgroundColor", "CornflowerBlue"));
		ModifyResult mr = im_rotate.modify(input, formatReg.createExtensionUri("tiff"), parameters);
		assertTrue("ModifyResult should not be NULL!", mr!=null);
		File result = DigitalObjectUtils.toFile(mr.getDigitalObject());
		assertTrue("File has been written correctly!", result.exists());
	}
}
