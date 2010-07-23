/**
 * 
 */
package eu.planets_project.services.qemu;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.io.FileUtils;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.floppyImageHelper.api.FloppyImageHelper;
import eu.planets_project.services.migration.floppyImageHelper.api.FloppyImageHelperFactory;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.ZipResult;
import eu.planets_project.services.utils.ZipUtils;
import eu.planets_project.services.view.CreateView;
import eu.planets_project.services.view.CreateViewResult;
import eu.planets_project.services.view.ViewAction;
import eu.planets_project.services.view.ViewActionResult;
import eu.planets_project.services.view.ViewStatus;

/**
 * A qemu viewer service based on the vncplay interactive session recorder.
 * 
 * 
 * @author <a href="mailto:i.valizada@googlemail.com">Isgandar Valizada</a>
 * @author <a href="mailto:klaus.rechert@rz.uni-freiburg.de">Klaus Rechert</a>
 *
 */

@WebService(name = QemuViewService.NAME, serviceName = CreateView.NAME, targetNamespace = PlanetsServices.NS, endpointInterface = "eu.planets_project.services.view.CreateView")
public class QemuViewService implements CreateView
{
    /** The name of the service */
    public static final String NAME = "Qemu Viewer Service";
    
    /** The default context path */
    private static final String CONTEXT_PATH = "/pserv-pa-qemu/";
    private static final URI defaultBaseUrl = URI.create("http://localhost:8080" + CONTEXT_PATH);
    
    /** A logger */
    public static final Logger log = Logger.getLogger(QemuViewService.class.getName());
	
    private static Properties properties = null;
    private static File cachedir = new File(System
            .getProperty("java.io.tmpdir"), "planets-tmp-dob-cache/");
    /** The id of qemu proccess */
    private Process procQemu;

    private static final String FLOPPY_PATH = "qemu-floppy";
    private static final String TMP_PATH = "qemu-tmp";

    /** A reference to the web service context. */
    @Resource 
    WebServiceContext wsc;
    

    public boolean startQemu(String qemuImage, String floppy)
    {
    	loadProperties();
    	log.info("here");
        try
        { 
        	Runtime rt = Runtime.getRuntime();
        	procQemu = rt.exec("qemu -vnc :0 /home/ubuntu/planets/migrate_test/Windows311.qcow");
        }
        catch(IOException e) 
        {
        	return false;  
        }
        return true;
    }

	private URI getBaseURIFromWSContext() 
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
 
    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#describe()
     */
    public ServiceDescription describe() 
    {
        ServiceDescription.Builder mds = new ServiceDescription.Builder(NAME, CreateView.class.getCanonicalName());
        mds.description("Qemu View Service");
        mds.author("author <author_mailbox>");
        mds.classname(this.getClass().getCanonicalName());
        
        // Add a link to the vncplay homepage as the 'tool' id.
        mds.tool(Tool.create(null, "qemu", null, null, ""));
        
        return mds.build();
    } 
   
    private CreateViewResult returnWithErrorMessage(String message) 
    {
    	ServiceReport rep = new ServiceReport(Type.ERROR, Status.TOOL_ERROR, message);
    	log.severe(message);
    	return new CreateViewResult(null, null, rep);
    }
 

    private String createFloppy(List<DigitalObject> digitalObjects) throws IOException
    {
    	if(digitalObjects == null)
    		return null;
    	
    	FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
    	File systemTempFolder = new File(System.getProperty("java.io.tmpdir"));
    	FileUtils.deleteDirectory(new File(systemTempFolder, TMP_PATH)); 
    	File temp_dir = new File(systemTempFolder, TMP_PATH);
    	FileUtils.forceMkdir(temp_dir);
    	if(temp_dir == null)
    		return null;

    	FileUtils.deleteDirectory(new File(systemTempFolder, FLOPPY_PATH));
    	File content_dir = new File(systemTempFolder, FLOPPY_PATH);
    	FileUtils.forceMkdir(content_dir);
    	if(content_dir == null)
    		return null;

    	int i = 0;
    	for( DigitalObject dob : digitalObjects ) 
    	{
    		String filename = dob.getTitle();
    		if(filename == null)
    			filename = "no_file_name" + i++;
    		File file = new File(content_dir, filename);
    		DigitalObjectUtils.toFile(dob, file);
    	}
    	File temp = File.createTempFile("floppy", ".zip");
    	ZipResult zip_result = ZipUtils.createZipAndCheck(content_dir, temp_dir, 
    				temp.getName(), false);
    	FileUtils.deleteQuietly(temp);
    	if(zip_result == null)
    		return null;
	
    	//DigitalObjectContent doc = Content.byReference(zip_result.getZipFile());
    	DigitalObject doz =  DigitalObjectUtils.createZipTypeDigitalObject(zip_result.getZipFile(), 
    			zip_result.getZipFile().getName(), true, true, false);

    	FloppyImageHelper helper = FloppyImageHelperFactory.getFloppyImageHelperInstance();
    	List<Parameter> __unused__ = new ArrayList<Parameter>();
    	MigrateResult mr = helper.migrate(doz, 
    			format.createExtensionUri("zip"), 
    			format.createExtensionUri("IMA"), 
    			__unused__);
	
    	if(mr.getReport().getStatus() != Status.SUCCESS)
    		return null;
    		
    	
    	String sessionId = UUID.randomUUID().toString();
    	if (!cachedir.exists()) {
    		if (!cachedir.mkdirs()) {
    			log.severe("failed to create caching dir: " + cachedir);
    			return null;
    		}
    	}
    	File file = new File(cachedir, sessionId);
    	DigitalObjectUtils.toFile(mr.getDigitalObject(), file);
    	
    	log.info("floppy created @ " + cachedir.getAbsolutePath() + "sessionID");
    	return cachedir.getAbsolutePath() + "sessionID";

    }
    
    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#createView(java.util.List, java.util.List)
     */
    public CreateViewResult createView(List<DigitalObject> digitalObjects, List<Parameter> parameters) 
    {
    	String floppy = null;
        try {
            floppy = createFloppy(digitalObjects);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
	
    	URI tmp = getBaseURIFromWSContext();
    	if(tmp != null)
    		log.info(tmp.toString());
    	else
    		log.info("base url failed");
    	
        // start os emulation with injected files(if any)  
        startQemu("", floppy); 
        ServiceReport rep = new ServiceReport(Type.INFO, Status.SUCCESS, "port:5900");
	
        URL sessionURL = null;
        try
        {
            sessionURL = new URL("http://localhost:8080/pserv-pa-qemu/recording.html");
        }
        catch (MalformedURLException e)
        {
        	returnWithErrorMessage("MalformedURLException");
        }

        return new CreateViewResult(sessionURL, "", rep);
    }
   
 
    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#getViewStatus(java.lang.String)
     */
    public ViewStatus getViewStatus(String sessionIdentifier) 
    {
        // TODO Override with UNKNOWN for now.
        ViewStatus vs = new ViewStatus(ViewStatus.Status.UNKNOWN, null);
        
        return vs;
    }


    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#doAction(java.lang.String, java.lang.String)
     */
    public ViewActionResult doAction(String sessionIdentifier, String action) 
    {
        if(ViewAction.SHUTDOWN.equals(action)) 
        {
           // TODO: kill QEMU 
        }
        
        return null;
    }
	
    private static Properties loadProperties()
    {
	if(properties != null)
		return properties;


        Properties props = new Properties();
        try {  
            props.load(QemuViewService.class.getResourceAsStream("qemu.properties"));
                        //grateBaseUrl = props.getProperty("grate.base.url");
        }   
        catch (Exception e)
        {
             e.printStackTrace();
        }
        return properties;
    }
}
