package eu.planets_project.services.migration.imagemagick;

/**
 * Test helper strings for Image Magick migration tests
 *
 */
public class Im4JavaImageMagickMigrateTestHelper {

	/** output for stand alone tests */
	public static final String STANDALONE_TEST_OUT = "IMAGE_MAGICK_MIGRATIONS_STANDALONE_TEST";
	/** output for server tests */
	public static final String SERVER_TEST_OUT = "IMAGE_MAGICK_MIGRATIONS_SERVER_TEST";
	/** output for local tests */
	public static final String LOCAL_TEST_OUT = "IMAGE_MAGICK_MIGRATIONS_LOCAL_TEST";
	/** the location of the jped test file */
	public static final String JPG_TEST_FILE = "tests/test-files/images/bitmap/test_jpg/2274192346_4a0a03c5d6.jpg";
	/** location of the PNG test file */
	public static final String PNG_TEST_FILE = "tests/test-files/images/bitmap/test_png/2274192346_4a0a03c5d6.png";
	/** location of the TIFF test file */
	public static final String TIFF_TEST_FILE = "tests/test-files/images/bitmap/test_tiff/2274192346_4a0a03c5d6.tif";
	/** location of the GIF test file */
	public static final String GIF_TEST_FILE = "tests/test-files/images/bitmap/test_gif/2274192346_4a0a03c5d6.gif";
	/** location of the BMP test file */
	public static final String BMP_TEST_FILE = "tests/test-files/images/bitmap/test_bmp/2274192346_4a0a03c5d6.bmp";
	/** location of the RAW test file */
	public static final String RAW_TEST_FILE = "tests/test-files/images/bitmap/test_raw/2274192346_4a0a03c5d6.raw";
	/** location of the TGA test file */
	public static final String TGA_TEST_FILE = "tests/test-files/images/bitmap/test_tga/2274192346_4a0a03c5d6.tga";
	/** location of the PCX test file */
	public static final String PCX_TEST_FILE = "tests/test-files/images/bitmap/test_pcx/2274192346_4a0a03c5d6.pcx";
	/** location of the PDF test file */
	public static final String PDF_TEST_FILE = "tests/test-files/images/bitmap/test_pdf/2274192346_4a0a03c5d6.pdf";
	/** location of the JPEG 200 test file */
//	public static final String JP2_TEST_FILE = "tests/test-files/images/bitmap/test_jp2/2274192346_4a0a03c5d6.jp2";
	public static final String JP2_TEST_FILE = "tests/test-files/images/bitmap/test_jp2/Descartes.jp2";
	
	public static final String JPC_TEST_FILE = "tests/test-files/images/bitmap/test_jp2/Descartes.jpc";
	/** compression quality constant */
	public static double COMP_QUAL_25 = 25.00;
	/** compression quality constant */
	public static double COMP_QUAL_50 = 50.00;
	/** compression quality constant */
	public static double COMP_QUAL_75 = 75.00;
	/** compression quality constant */
	public static double COMP_QUAL_100 = 100;
	
	/** compression type None */
	public static String COMP_TYPE_NO = "None";
	/** compression type BZIP */
	public static String COMP_TYPE_BZIP = "BZip";
	/** compression type fax */
	public static String COMP_TYPE_FAX = "Fax";
	/** compression type group 4 fax */
	public static String COMP_TYPE_GROUP4 = "Group4";
	/** compression type JPEG */
	public static String COMP_TYPE_JPEG = "JPEG";
	/** compression type JPEG 2000 */
	public static String COMP_TYPE_JPEG2000 = "JPEG2000";
	/** compression type JPEG lossless */
	public static String COMP_TYPE_JPEG_LOSSLESS = "LosslessJPEG";
	/** compression type LZW */
	public static String COMP_TYPE_LZW = "LZW";
	/** compression type RLE */
	public static String COMP_TYPE_RLE = "RLE";
	/** compression type zip */
	public static String COMP_TYPE_ZIP = "Zip";
}
