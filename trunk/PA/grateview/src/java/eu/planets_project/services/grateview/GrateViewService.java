/**
 * GrateViewService 
 */
package eu.planets_project.services.grateview;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.services.*;
import eu.planets_project.services.datatypes.*;
import eu.planets_project.services.view.*;

/**
 * A GRATE Viewer service. 
 *
 * Provides a URI to a VNC-qemu session.
 * Caches the digital object locally and manages viewer sessions for them.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 * @author <a href="mailto:klaus.rechert@rz.uni-freiburg.de">Klaus Rechert</a>
 *
 */
@WebService(name = GrateViewService.NAME, 
	serviceName = CreateView.NAME, 
	targetNamespace = PlanetsServices.NS,
	endpointInterface = "eu.planets_project.services.view.CreateView" )
        
public class GrateViewService implements CreateView {

	/** The name of the service */
	public static final String NAME="GRATE View Service";

	/** context */
	private static final String CONTEXT_PATH = "/pserv-pa-grateview/";
	private static final URI defaultBaseUrl = URI.create("http://132.230.4.14:8080"+CONTEXT_PATH);

	/** A logger */
	public static Log log = LogFactory.getLog(GrateViewService.class);

	/** A reference to the web service context. */
	@Resource 
	WebServiceContext wsc;

	public ServiceDescription describe() 
	{
		ServiceDescription.Builder mds;
		mds = new ServiceDescription.Builder(NAME, CreateView.class.getCanonicalName());
		mds.description("A GRATE (emulation) viewer service.");
		mds.author("Klaus Rechert <klaus.rechert@rz.uni-freiburg.de>");
		mds.classname(this.getClass().getCanonicalName());
		return mds.build();
	}

	private static CreateViewResult returnWithErrorMessage(String message) 
	{
		ServiceReport rep = new ServiceReport();
		log.error(message);
		rep.setErrorState(ServiceReport.TOOL_ERROR);
		rep.setError(message);
		return new CreateViewResult(null, null, rep);
	}

	public static URI getBaseURIFromWSContext( WebServiceContext wsc ) 
	{
		if( wsc == null ) 
			return null;

		// Lookup server config from message context:
		// @see https://jax-ws.dev.java.net/articles/MessageContext.html
		MessageContext mc = wsc.getMessageContext();
		if( mc == null ) 
			return null;

		ServletRequest request = (ServletRequest)mc.get(MessageContext.SERVLET_REQUEST);
		ServletContext sc = (ServletContext) mc.get(MessageContext.SERVLET_CONTEXT);
		if( request == null || sc == null ) 
			return null;

		// Construct a base URL;
		URI baseUrl = null;
		try {
			baseUrl = new URL(request.getScheme(), request.getServerName(), 
			request.getServerPort(), sc.getContextPath()+"/" ).toURI();
		} catch (MalformedURLException e) {
		    e.printStackTrace();
		} catch (URISyntaxException e) {
		    e.printStackTrace();
		}

		return baseUrl;
	}

	public CreateViewResult createView(List<DigitalObject> digitalObjects,  List<Parameter> parameters) {
		// Instanciate the View:
		return createViewerSession(digitalObjects, getBaseURIFromWSContext(wsc));
	}

	/**
	* 
	* @param digitalObjects
	* @return
	*/
	public static CreateViewResult createViewerSession(List<DigitalObject> digitalObjects, 
		URI baseUrl){
		
		for( DigitalObject dob : digitalObjects ) {
		    if( dob.getContent() == null ) {
			return returnWithErrorMessage("The Content of the DigitalObject should not be NULL.");
		    }
		}

		String sessionID = DigitalObjectDiskCache.cacheDigitalObjects(digitalObjects);
		String imgPath = null;
		URL sessionURL = null;
		try {
			imgPath = baseUrl.resolve("cache.jsp?sid="+sessionID).toURL().toString();
			imgPath = URLEncoder.encode(imgPath, "UTF-8");	
		} catch (Exception e) {
			e.printStackTrace();
			return returnWithErrorMessage("Failed to resolve image path.");
		}

		try {
			sessionURL = new URL("http://planets.ruf.uni-freiburg.de/~randy/GRATE_IF.php?puid=2&uid=1&object_url=" + imgPath);
		}
		catch (Exception e)
		{
			return returnWithErrorMessage("Failed creating sessionURL");
		}
		ServiceReport rep = new ServiceReport();
		rep.setErrorState(ServiceReport.SUCCESS);

		// Return the view id:
		return new CreateViewResult(sessionURL, sessionID, rep);
	}

	public static CreateViewResult createViewerSessionViaService(URI _url) throws MalformedURLException 
	{
		Service service = Service.create(defaultBaseUrl.resolve("GrateViewService?wsdl").toURL(), GrateViewService.QNAME );
		CreateView grate = service.getPort(CreateView.class);

		// Construct a list of DOBs covering the given URL:
		DigitalObject.Builder dob = new DigitalObject.Builder(ImmutableContent.byReference( _url.toURL() ));
		List<DigitalObject> digitalObjects = new ArrayList<DigitalObject>();
		digitalObjects.add(dob.build());

		// Invoke the service and create the view:
		CreateViewResult cvr = grate.createView(digitalObjects, null);
		return cvr;
	}

	/* 
	* @see eu.planets_project.services.view.CreateView#getViewStatus(java.lang.String)
	*
	* TODO: determin real value (if possible)
	*/
	public ViewStatus getViewStatus(String sessionIdentifier) {
		ViewStatus vs = new ViewStatus(ViewStatus.Status.INACTIVE, null );
		return vs;
	}

	public ViewActionResult doAction(String session, String action)
	{
		return null;
	}
}
