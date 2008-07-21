/**
 *  @author : Thomas Krämer thomas.kraemer@uni-koeln.de
 *  created : 14.07.2008
 *  
 */
package eu.planets_project.services.migration.soxservices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *  @author: Thomas Kraemer thomas.kraemer@uni-koeln.de
 *  created: 14.07.2008
 */
public class FileUtils {

    /**
	 * @param name
	 *            The name to use when generattraning the temp file
	 * @return Returns a temp file created using File.createTempFile
	 */
	public static File tempFile(String name, String suffix) {
		File input;
		try {
			input = File.createTempFile  ( name, suffix);
			return input;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * @param name
	 *            The name to use when generattraning the temp file
	 * @return Returns a temp file created using File.createTempFile
	 */
	public static File tempFile(byte[] data ,String  name,String suffix) {
		File input;
		try {
			input = File.createTempFile  ( name, suffix);
			FileOutputStream fos = new FileOutputStream(input);
			fos.write(data);
			fos.flush();
			fos.close();
			return input;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
