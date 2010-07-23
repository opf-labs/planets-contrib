package eu.planets_project.ifr.contrib.ait.services.identification.mimeutil.impl;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.planets_project.ifr.contrib.ait.services.MimeUtilShared;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.ServiceUtils;

/**
 * Identification service using Mime-Util (2.1.3).
 * @author <a href="mailto:andrew.lindley@ait.ac.at">Andrew Lindley</a>
 * @since 14.06.2010
 */
@WebService(
		name = MimeUtilIdentification.NAME, 
		serviceName = Identify.NAME, 
		targetNamespace = PlanetsServices.NS, 
		endpointInterface = "eu.planets_project.services.identify.Identify")
@Stateless
@MTOM
@StreamingAttachment( parseEagerly=true, memoryThreshold=ServiceUtils.JAXWS_SIZE_THRESHOLD )
public final class MimeUtilIdentification implements Identify, Serializable {
    private static Logger log = Logger.getLogger(MimeUtilIdentification.class.getName());
    static final String NAME = "MimeUtilIdentification";
    private static final long serialVersionUID = 1127650680714441971L;
    private String status;
    private Detectors registeredMimeDetector = null;
    
    /**
     * A list of supported detectors, which can be selected as identification type
     * through the appropriate service parameter.
     */
    private static enum Detectors{
    	MagicMimeMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector",
    			 "The magic mime rules files are loaded in the following way."
    			 +"<li>From a JVM system property <code>magic-mime</code> i.e"
    			 +"<code>-Dmagic-mime=../my/magic/mime/rules</code></li>"
    			 +"<li>From any file named <code>magic.mime</code> that can be found on the classpath</li>"
    			 +"<li>From a file named <code>.magic.mime</code> in the users home directory</li>"
    			 +"<li>From the normal Unix locations <code>/usr/share/file/magic.mime</code>"
    			 +"and <code>/etc/magic.mime</code> (in that order)</li>"
    			 +"<li>From the internal <code>magic.mime</code> file"
    			 +"<code>eu.medsea.mimeutil.magic.mime</code> if, and only if, no files are"
    			 +"located in step 4 above.</li>"
    			 +"Each rule file is appended to the end of the existing rules so the earlier in"
    			 +"the sequence you define a rule means this will take precedence over rules"
    			 +"loaded later. </p>"
    			 +"You can add new mime mapping rules using the syntax defined for the Unix"
    			 +"magic.mime file by placing these rules in any of the files or locations"
    			 +"listed above. You can also change an existing mapping rule by redefining the"
    			 +"existing rule in one of the files listed above. This is handy for some of the"
    			 +"more sketchy rules defined in the existing Unix magic.mime files."),
    			 
    	ExtensionMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector",
    			  "Each property file loaded will add to the list of extensions understood by MimeUtil."
    			 +"If there is a clash of extension names then the last one loaded wins, i.e they are not adative, this makes it"
    			 +"possible to completely change the mime types associated to a file extension declared in previously loaded property files."
    			 +"The extensions are also case sensitive meaning that bat, bAt, BAT and Bat can all be recognised individually. If however,"
    			 +"no match is found using case sensitive matching then it will perform an insensitive match by lower casing the extension"
    			 +"of the file to be matched first."),
    	
    	OpendesktopMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector",
    			 "he Opendesktop shared mime database contains glob rules and magic number"
    			+"lookup information to enable applications to detect the mime types of files."
    			+"This class uses the mime.cache file which is one of the files created by the"
    			+"update-mime-database application. This file is a memory mapped file that"
    			+"enables the database to be updated and copied without interrupting applications."
    			+"This implementation follows the memory mapped spec so it is not required to"
    			+"restart an application using this mime detector should the underlying mime.cache database change."
    			+"For a complete description of the information contained in this file please"
    			+"see: http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-latest.html"),
    	
    	WindowsRegistryMimeDetecor("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector",
    			"Get the content type for a file extension as stored in the Windows Registry"
  				+"The extensions are stored at HKEY_CLASSES_ROOT"
    			+"This MimeDetector will only operate on Windows machines. On any other platform"
    			+"the methods throw a UnsupportedOperationException (These are swallowed by the MimeUtil class)"
    			+"Therefore, it is perfectly acceptable to register this MimeDetector with MimeUtil and it"
    			+"will only be used on a Windows Platform. On all other platforms it will just be ignored.");
    	
    	//full qualified name
    	private String QN;
    	//human readable name which is used in the parameters
    	private String paramName;
    	//the mime detector's description
    	private String description;

    	private Detectors(String qn, String description) {
    		QN = qn;
    	}

    	public String getQN() {
    		return QN;
    	}
    	
    	public String getParamName(){
    		return paramName;
    	}
    	
    	public String getDescription(){
    		return description;
    	}
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.identify.Identify#identify(eu.planets_project.services.datatypes.DigitalObject,
     *      java.util.List)
     */
    public IdentifyResult identify(final DigitalObject digitalObject, List<Parameter> parameters) {
        File file = DigitalObjectUtils.toFile(digitalObject);
        ServiceReport report;
        MimeType type = null;
        List<URI> types = new ArrayList<URI>();
        
        //1) register a mime detector
        this.registerMimeDetector(parameters);
        
        try{
        	//2) identify the binary
        	type = identifyBinary(file);
        	log.info("MimeUtil Identification, got types: " + types);
        	
        	//3) create the service reports
        	if (type == null) {
                report = new ServiceReport(Type.ERROR, Status.TOOL_ERROR,
                        "Could not identify " + file.getAbsolutePath()+" using the applied identification method: "+this.registeredMimeDetector);
            } else {
            	//in case of success
                report = new ServiceReport(Type.INFO, Status.SUCCESS, "applied identification method:"+this.registeredMimeDetector+", mediatype:"+type.getMediaType()+", specificity:"+type.getSpecificity());
                URI uri = new URI("planets:fmt/ext/"+type.getSubType());
                types.add(uri);
            }
        
        }catch(Exception e){
        	log.info("MimeUtil Identification error "+e.toString());
        	//generate an error report
        	 report = new ServiceReport(Type.ERROR, Status.TOOL_ERROR,
                     "Could not identify " + file.getAbsolutePath()+ "error: "+e.toString()+" for identification method: "+this.registeredMimeDetector);
        }
        
        //3) unregister MimeDetector again
        this.unregisterMimeDetector();
        
        //return the identify result
        return new IdentifyResult(types, null, report);
    }

    
    /**
     * Register the proper MimeDetector according to a given parameter definition.
     * Parameters that are not relevant for this task are being ignored
     * If no parameters are provided it will default to the default settings
     * @param params
     */
    private void registerMimeDetector(List<Parameter> params){ 
    	//check if relevant parameters were passed along
    	Detectors mimeDetector = null;
    	if(params!=null){
    		for(Parameter p : params){
    			//look for the parameters called mime-detector
    			if(p.getName().equals("mime-detector")){
    				//if there are multiple parameters with mime-detectors we'll just use the last one found
	    			try{
	    				Detectors d = Detectors.valueOf(p.getValue());
	    				if(d!=null){
		    				mimeDetector = d;
		    			}
	    			}catch(Exception e){
	    				//e.g. no valid param specified
	    			}
	    			
	    		}
    		}
    	}

    	//so let's check if we have found a mime detector configuration in the given parameters
    	if(mimeDetector==null){
    		mimeDetector = getDefaultMimeDetector();
    	}
    	
    	//finally remember the mime detector we're using
    	this.registeredMimeDetector = mimeDetector;
    }
    
    /**
     * unregisters the mime detector which has previously been registered
     */
    private void unregisterMimeDetector(){
    	if(this.registeredMimeDetector!=null){
    		MimeUtil.unregisterMimeDetector(this.registeredMimeDetector.getQN());
    	}
    }
    
    /**
     * Returns the MimeDetector default configuration
     * @return
     */
    private Detectors getDefaultMimeDetector(){
    	return Detectors.MagicMimeMimeDetector;
    }
    
    /**
     * populate and returns a list of default parameters
     * @return
     */
    private List<Parameter> getDefaultParameters(){
    	 List<Parameter> paramList = new ArrayList<Parameter>();
         Parameter mimeDetectorParam = new Parameter.Builder("mime-detector", this.getDefaultMimeDetector().name()).description(
                this.getDefaultMimeDetector().description).build();
         paramList.add(mimeDetectorParam);
         return paramList;
    }
    
    /**
     * populate and returns a list of default parameters
     * @return
     */
    public static List<Parameter> getAllSupportedParameters(){
    	 List<Parameter> paramList = new ArrayList<Parameter>();
    	 for(Detectors d : Detectors.values()){
    		 Parameter mimeDetectorParam = new Parameter.Builder("mime-detector", d.name()).description(d.description).build();
    	     paramList.add(mimeDetectorParam);
    	 }
         return paramList;
    }
    
    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.identify.Identify#describe()
     */
    public ServiceDescription describe() {
    	String sServiceDescription = "Identification service using Mime-Util (2.1.3)." +
    			"The Mime-Type Extractor Service uses the Mime Type Detection Utility (Mime-Util) project from sourceforge https://sourceforge.net/projects/mime-util/" +
    			"It runs under Apache License V2.0 and enables Java programs to detect MIME types based on file extensions, magic data and content sniffing." +
    			"It supports detection from java.io.File, java.io.InputStream, java.net.URL and byte arrays. " +
    			"See the project website www.eoss.org.nz/mime-util for detail" +
    			"The given mime-util service wrapper at hand provides a wrapper following the Planets preservation interfaces" +
    			"'Identify' and 'Validate'.";
    	
        ServiceDescription.Builder sd = new ServiceDescription.Builder(
                "Mime-Util Identification Service", Identify.class
                        .getCanonicalName());
        sd.classname(this.getClass().getCanonicalName());
        sd.description(sServiceDescription);
        sd.author("Andrew Lindley");
        sd.tool(MimeUtilShared.tool);
        sd.furtherInfo(URI.create("https://sourceforge.net/projects/mime-util/"));
        //sd.inputFormats(inputFormats());
        sd.serviceProvider("AIT - Austrian Institute of Technology GmbH - www.ait.ac.at");
        sd.parameters(this.getAllSupportedParameters());
  
        return sd.build();
    }
  
    
    /**
     * Identifies the binary by the preselected (registeredMimeDetector) and extracts the
     * most specific mime type.
     * @param f
     * @return
     * @throws Exception
     */
    private MimeType identifyBinary(File f) throws Exception{
    	List<MimeType> ret = new ArrayList<MimeType>();
    	MimeUtil.registerMimeDetector(this.registeredMimeDetector.QN);
    	
    	System.out.println("ExtensionMimeDetector "+f.getAbsolutePath());
    	MimeType m = MimeUtil.getMostSpecificMimeType(MimeUtil.getMimeTypes(f));
    	System.out.println(m.getMediaType()+" ;"+m.getSpecificity()+" ;"+m.getSubType());
    	System.out.println("==================");
    	
    	MimeUtil.unregisterMimeDetector(this.registeredMimeDetector.QN);
    	return m;
    }
}
