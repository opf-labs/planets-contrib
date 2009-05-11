package eu.planets_project.services.grateview;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ImmutableContent;
import eu.planets_project.services.utils.FileUtils;

/**
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 * @author <a href="mailto:klaus.rechert@rz.uni-freiburg.de">Klaus Rechert</a>
 *
 */
public class DigitalObjectDiskCache {

	public static Log log = LogFactory.getLog(DigitalObjectDiskCache.class);

	/**
	* @param sessionId
	* @return
	*/
	public static File findCacheDir( String sessionId ) {
		// For security reasons, do not allow directory separators:
		if( sessionId.contains("/") ) 
			return null;
		if( sessionId.contains("\\") ) 
			return null;
		return new File(System.getProperty("java.io.tmpdir"), "pserv-pa-grateview/"+sessionId);
	}

	/**
	* @param digitalObjects
	* @return
	*/
	public static String cacheDigitalObjects( List<DigitalObject> digitalObjects ) 
	{       
		String sessionId = UUID.randomUUID().toString();
		File cachedir = findCacheDir( sessionId );
		if(!cachedir.mkdirs())
		{
			log.error("failed to create caching dir: " + cachedir);
			return null;
		}

		// Store Digital Objects:
		for( DigitalObject dob : digitalObjects ) 
		{
			String filename = UUID.randomUUID().toString();
			FileUtils.writeInputStreamToFile(dob.getContent().read(), cachedir, filename);
		}
		return sessionId;
	}

	/**
	* @param sessionId
	* @return
	*/
	public static List<DigitalObject> recoverDigitalObjects( String sessionId ) {
		List<DigitalObject> dobs = new ArrayList<DigitalObject>();

		File cachedir = findCacheDir(sessionId);
		if(!cachedir.isDirectory())
		{
			log.error("recovering failed: " + cachedir);
			log.error("no such directory");
			return null;
		}

		File[] filelist = cachedir.listFiles();
		try {
			for (File f : filelist) {
				Content c = ImmutableContent.byReference(f.toURL());
				dobs.add(new DigitalObject.Builder(c).build());
			}
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
		return dobs;
	}

	/**
	* @param sessionId
	* @param i index 
	* @return
	*/
	public static DigitalObject findCachedDigitalObject( String sessionId, int i ) 
	{
		List<DigitalObject> digitalObjects = recoverDigitalObjects(sessionId);
		if( digitalObjects == null || digitalObjects.size() == 0) 
			return null;

		if( i < 0 || i >= digitalObjects.size())
			i = 0;

		return digitalObjects.get(i);
	}
}

