/**
 * 
 */
package eu.planets_project.services.jj2000;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.utils.cache.DigitalObjectDiskCache;
import eu.planets_project.services.view.CreateView;
import eu.planets_project.services.view.CreateViewResult;
import eu.planets_project.services.view.ViewStatus;

/**
 * A viewer service based on the JJ2000 code.
 * 
 * Caches the digital object locally and manages viewer sessions for them.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */

@WebService(name = JJ2000ViewerService.NAME, 
        serviceName = CreateView.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.view.CreateView" )
        
public class JJ2000ViewerService implements CreateView {

    /** The name of the service */
    public static final String NAME="JJ2000 Viewer Service";

    /** The default context path */
    private static final String CONTEXT_PATH = "/pserv-pa-jj2000/";
    private static URL defaultBaseUrl;
    static {
       try {
           defaultBaseUrl = new URL("http","localhost",8080,CONTEXT_PATH);
       } catch (MalformedURLException e) {
           e.printStackTrace();
       }
    }

    /** A logger */
    public static Log log = LogFactory.getLog(JJ2000ViewerService.class);
    
    /** A reference to the web service context. */
    @Resource 
    WebServiceContext wsc;

    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#describe()
     */
    public ServiceDescription describe() {
        ServiceDescription.Builder mds = new ServiceDescription.Builder(NAME, CreateView.class.getCanonicalName());
        mds.description("A JPEG 2000 viewer service. Uses the JJ2000 reference implementation. See http://jj2000.epfl.ch/ for copyright information.");
        mds.author("Andrew Jackson <Andrew.Jackson@bl.uk>");
        mds.classname(this.getClass().getCanonicalName());
        // Add a link to the JJ2000 homepage.
        try {
            mds.furtherInfo(new URI("http://jj2000.epfl.ch/"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return mds.build();
    }

    private static CreateViewResult returnWithErrorMessage(String message) {
        ServiceReport rep = new ServiceReport();
        log.error(message);
        rep.setErrorState(ServiceReport.ERROR);
        rep.setError("message");
        return new CreateViewResult(null, null, rep);
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#createView(java.util.List)
     */
    public CreateViewResult createView(List<DigitalObject> digitalObjects) {
        // Lookup server config from message context:
        // @see https://jax-ws.dev.java.net/articles/MessageContext.html
        MessageContext mc = wsc.getMessageContext();
        ServletRequest request = (ServletRequest)mc.get(MessageContext.SERVLET_REQUEST);
        ServletContext sc = (ServletContext) mc.get(MessageContext.SERVLET_CONTEXT);

        // Construct a base URL;
        URL baseUrl = null;
        try {
            baseUrl = new URL( request.getScheme(), request.getServerName(), request.getServerPort(), sc.getContextPath()+"/" );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
        // Instanciate the View:
        return createViewerSession( digitalObjects, baseUrl );
    }

    /**
     * 
     * @param digitalObjects
     * @return
     */
    public static CreateViewResult createViewerSession(List<DigitalObject> digitalObjects, URL baseUrl ) {
        // Store copies of the viewable digital objects:
        for( DigitalObject dob : digitalObjects ) {
            // Can only cope if the object is 'simple':
            if( dob.getContent() == null ) {
                return returnWithErrorMessage("The Content of the DigitalObject should not be NULL.");
            }
        }
        
        String sessionID = DigitalObjectDiskCache.cacheDigitalObjects(digitalObjects);
        URL sessionURL;
        try {
            sessionURL = new URL(baseUrl, "view.jsp?sid="+sessionID);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return returnWithErrorMessage("Failed to construct session URL.");
        }
        
        // Create a service report:
        ServiceReport rep = new ServiceReport();
        rep.setErrorState(ServiceReport.SUCCESS);

        // Return the view id:
        return new CreateViewResult(sessionURL, sessionID, rep);
    }
    
    /**
     * @param jp2url
     * @return
     */
    public static CreateViewResult createViewerSession( URL jp2url ) {
        DigitalObject dob = new DigitalObject.Builder( Content.byReference(jp2url) ).build();
        List<DigitalObject> dobs = new ArrayList<DigitalObject>();
        dobs.add(dob);
        return createViewerSession(dobs, defaultBaseUrl);
    }
    
    public static CreateViewResult createViewerSessionViaService( URL testUrl ) throws MalformedURLException {
        Service service = Service.create( 
                new URL(defaultBaseUrl, "JJ2000ViewerService?wsdl"), JJ2000ViewerService.QNAME );
        CreateView jj2k = service.getPort(CreateView.class);
        
//        URL testUrl = new URL("http","localhost",8080,CONTEXT_PATH+"/resources/world.jp2");
        DigitalObject.Builder dob = new DigitalObject.Builder( Content.byReference( testUrl ));

        List<DigitalObject> digitalObjects = new ArrayList<DigitalObject>();
        digitalObjects.add(dob.build());
        
        CreateViewResult cvr = jj2k.createView(digitalObjects);
        return cvr;
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#getViewStatus(java.lang.String)
     */
    public ViewStatus getViewStatus(String sessionIdentifier) {
        // Lookup this cache:
        File cache = DigitalObjectDiskCache.findCacheDir( sessionIdentifier );
        
        // Default to 'inactive'
        ViewStatus vs = new ViewStatus( ViewStatus.INACTIVE, null );
        
        // If it's active, return info:
        if( cache != null && cache.exists() && cache.isDirectory() ) {
            vs = new ViewStatus( ViewStatus.ACTIVE, null );
        }
        return vs;
    }
    
}
