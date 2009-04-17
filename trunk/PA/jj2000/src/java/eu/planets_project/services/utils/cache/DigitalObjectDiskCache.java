/**
 * 
 */
package eu.planets_project.services.utils.cache;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.planets_project.services.datatypes.ImmutableContent;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.jj2000.JJ2000ViewerService;

/**
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class DigitalObjectDiskCache {

    /* FIXME Implement a Cache object, that serialised to XML, and that 
     * re-maps any remote binaries into local ones on construction.
     */
    
    public class CacheableDigitalObjects {
        List<DigitalObject> digitalObjects;
        
        public CacheableDigitalObjects( List<DigitalObject> digitalObjects ) {
            // Re-map into new DOs that embed/localise the binaries.
            // Add getter and serialisation code.
        }
        
    }
    
    /**
     * @param sessionId
     * @return
     */
    public static File findCacheDir( String sessionId ) {
        // For security reasons, do not allow directory separators:
        if( sessionId.contains("/") ) return null;
        if( sessionId.contains("\\") ) return null;
        return new File(System.getProperty("java.io.tmpdir"), "pserv-pa-jj2000/"+sessionId);
    }

    /**
     * @param digitalObjects
     * @return
     */
    public static String cacheDigitalObjects( List<DigitalObject> digitalObjects ) {
        // Generate a UUID to act as the session ID:
        String sessionId = UUID.randomUUID().toString();
        
        // Create a directory in the temp space, and store the DOs in there.
        File cachedir = findCacheDir( sessionId );
        cachedir.mkdir();
        
        JJ2000ViewerService.log.info("Created cache dir: " + cachedir.getAbsolutePath() );
        
        // Store Digital Objects:
        for( DigitalObject dob : digitalObjects ) {
            dob.toXml();
        }
        
        return sessionId;
    }

    /**
     * @param sessionId
     * @return
     */
    public static List<DigitalObject> recoverDigitalObjects( String sessionId ) {
        List<DigitalObject> dobs = new ArrayList<DigitalObject>();
        // Lookup stored items:
        
        // Parse back into DigObjects:
        try {
            URL testurl1 = new URL("http://127.0.0.1:8080/pserv-pa-jj2000/resources/world.jp2");
            dobs.add( new DigitalObject.Builder( ImmutableContent.byReference(testurl1)).build() );
            URL testurl2 = new URL("http://127.0.0.1:8080/pserv-pa-jj2000/resources/Cevennes2.jp2");
            dobs.add( new DigitalObject.Builder( ImmutableContent.byReference(testurl2)).build() );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
        return dobs;
    }

    /**
     * @param sessionId
     * @param i
     * @return
     */
    public static DigitalObject findCachedDigitalObject( String sessionId, int i ) {
        List<DigitalObject> digitalObjects = recoverDigitalObjects(sessionId);
        if( digitalObjects == null ) return null;
        if( digitalObjects.size() == 0 ) return null;
        // Range check:
        if( i < 0 ) i = 0;
        if( i >= digitalObjects.size() ) i = 0;
        // Return:
        return digitalObjects.get(i);
    }

}
