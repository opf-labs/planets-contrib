/**
 * GrateViewService 
 */
package eu.planets_project.services.grateview;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.io.FileUtils;

import eu.planets_project.ifr.core.storage.utils.DigitalObjectDiskCache;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.floppyImageHelper.api.FloppyImageHelper;
import eu.planets_project.services.migration.floppyImageHelper.api.FloppyImageHelperFactory;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.ZipResult;
import eu.planets_project.services.utils.ZipUtils;
import eu.planets_project.services.view.CreateView;
import eu.planets_project.services.view.CreateViewResult;
import eu.planets_project.services.view.ViewActionResult;
import eu.planets_project.services.view.ViewStatus;

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
	private static final URI DEFAULT_BASE_URL = URI.create("http://132.230.4.14:8080"+CONTEXT_PATH);

	/** A logger */
	public static final Logger log = Logger.getLogger(GrateViewService.class.getName());

	private static final String FLOPPY_PATH =  "grate-floppy-content";
	private static final String TMP_PATH = "grate-tmp";
	
	/** The location of the GRATE server */
    private static String grateBaseUrl = "http://planets.ruf.uni-freiburg.de/~randy/GRATE_IF.php";

	/** A reference to the web service context. */
	@Resource 
	WebServiceContext wsc;

	public ServiceDescription describe() 
	{
		ServiceDescription.Builder mds;
		mds = new ServiceDescription.Builder(NAME, CreateView.class.getCanonicalName());
		mds.description("A GRATE (emulation) viewer service. This service uses the GRATE endpoint hosted at "+grateBaseUrl+".");
		mds.author("Klaus Rechert <klaus.rechert@rz.uni-freiburg.de>");
		mds.classname(this.getClass().getCanonicalName());
		return mds.build();
	}

	private static CreateViewResult returnWithErrorMessage(String message) 
	{
		ServiceReport rep = new ServiceReport(Type.ERROR, Status.TOOL_ERROR, message);
		log.severe(message);
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

	private static void loadProperties()
	{
		Properties props = new Properties();
		try {
			props.load(GrateViewService.class.getResourceAsStream("grateview.properties"));
			grateBaseUrl = props.getProperty("grate.base.url");
		}
		catch (Exception e)
		{	
			e.printStackTrace();
		}
	}

	public CreateViewResult createView(List<DigitalObject> digitalObjects,  List<Parameter> parameters) {
		// Instanciate the View:
		try {
            return createViewerSession(digitalObjects, getBaseURIFromWSContext(wsc));
        } catch (IOException e) {
            e.printStackTrace();
            return returnWithErrorMessage("Could not perform required IO: " + e.getMessage());
        }
	}

	/**
	* 
	* @param digitalObjects
	* @return
	 * @throws IOException 
	*/
	public static CreateViewResult createViewerSession(List<DigitalObject> digitalObjects, URI baseUrl) throws IOException
	{	
		FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
		
		for( DigitalObject dob : digitalObjects ) {
		    if( dob.getContent() == null ) {
			return returnWithErrorMessage("The Content of the DigitalObject should not be NULL.");
		    }
		}

		loadProperties();
	
		File systemTempFolder = new File(System.getProperty("java.io.tmpdir"));
		FileUtils.deleteDirectory(new File(systemTempFolder, TMP_PATH)); 
		File temp_dir = new File(systemTempFolder, TMP_PATH);
		FileUtils.forceMkdir(temp_dir);
		if(temp_dir == null)
			return returnWithErrorMessage("Failed to create temp folder.");

		FileUtils.deleteDirectory(new File(systemTempFolder, FLOPPY_PATH));
		File content_dir = new File(systemTempFolder, FLOPPY_PATH);
		FileUtils.forceMkdir(content_dir);
		if(content_dir == null)
			return returnWithErrorMessage("Failed to create temp folder.");

		int i = 0;
		for( DigitalObject dob : digitalObjects ) 
		{
			String filename = dob.getTitle();
			if(filename == null)
				filename = "no_file_name" + i++;
			DigitalObjectUtils.toFile(dob, new File(content_dir, filename));
		}
		File temp = File.createTempFile("floppy", ".zip");
		ZipResult zip_result = ZipUtils.createZipAndCheck(content_dir, temp_dir, temp.getName(), false);
		FileUtils.deleteQuietly(temp);
		if(zip_result == null)
			return returnWithErrorMessage("Failed to create zip file.");

//		DigitalObjectContent doc = Content.byReference(zip_result.getZipFile());
		DigitalObject doz =  DigitalObjectUtils.createZipTypeDigitalObject(zip_result.getZipFile(), zip_result.getZipFile().getName(), true, true, false);

		FloppyImageHelper helper = FloppyImageHelperFactory.getFloppyImageHelperInstance();

		List<Parameter> __unused__ = new ArrayList<Parameter>();
		MigrateResult mr = helper.migrate(doz, 
				format.createExtensionUri("zip"), 
				format.createExtensionUri("IMA"), 
				__unused__);
	
		if(mr.getReport().getStatus() != Status.SUCCESS)
			return returnWithErrorMessage(mr.getReport().toString());

		String sessionID = DigitalObjectDiskCache.cacheDigitalObject(mr.getDigitalObject());
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
			sessionURL = new URL(grateBaseUrl + "?puid=2&uid=1&object_url=" + imgPath);
		}
		catch (Exception e)
		{
			return returnWithErrorMessage("Failed creating sessionURL: " + grateBaseUrl + "?puid=2&uid=1&object_url=" + imgPath);
		}
		ServiceReport rep = new ServiceReport(Type.INFO, Status.SUCCESS, "OK");

		// Return the view id:
		return new CreateViewResult(sessionURL, sessionID, rep);
	}

	public static CreateViewResult createViewerSessionViaService(URI _url) throws MalformedURLException 
	{
		Service service = Service.create(DEFAULT_BASE_URL.resolve("GrateViewService?wsdl").toURL(), GrateViewService.QNAME );
		CreateView grate = service.getPort(CreateView.class);

		// Construct a list of DOBs covering the given URL:
		DigitalObject dob = new DigitalObject.Builder(Content.byReference(_url.toURL())).build();
		List<DigitalObject> digitalObjects = new ArrayList<DigitalObject>();
		digitalObjects.add(dob);

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
