package eu.planets_project.ifr.contrib.ait.services.validation.mimeutil.impl;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.ifr.contrib.ait.services.MimeUtilShared;
import eu.planets_project.ifr.contrib.ait.services.identification.mimeutil.impl.MimeUtilIdentification;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.utils.ServiceUtils;
import eu.planets_project.services.validate.Validate;
import eu.planets_project.services.validate.ValidateResult;

/**
 * Validation service using Mime-Util (2.1.3).
 * @author <a href="mailto:andrew.lindley@ait.ac.at">Andrew Lindley</a>
 * @since 14.06.2010
 */
@WebService(
		name = MimeUtilValidation.NAME, 
		serviceName = Validate.NAME, 
		targetNamespace = PlanetsServices.NS, 
		endpointInterface = "eu.planets_project.services.validate.Validate")
@Stateless
@MTOM
@StreamingAttachment( parseEagerly=true, memoryThreshold=ServiceUtils.JAXWS_SIZE_THRESHOLD )
public final class MimeUtilValidation implements Validate, Serializable {
    /***/
    private static final long serialVersionUID = 2127494848765937613L;
    /***/
    static final String NAME = "MimeUtilValidation";

    /**
     * {@inheritDoc}
     * @see Validate#validate(eu.planets_project.services.datatypes.DigitalObject,
     *      java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public ValidateResult validate(final DigitalObject digitalObject,
            final URI format, final List<Parameter> parameters) {
        ServiceReport report;
        boolean valid = false;
        
        //1. check if the identify did actually succeed
        IdentifyResult identResult = identify(digitalObject, parameters);
        if(identResult.getReport().getType().equals(Type.ERROR)){
        	report = identResult.getReport();
        }
        else{
	        //2. run the validate operation to check if the fmt is in the known range
	        valid = validateDigitalObject(identResult, format);
	        report = new ServiceReport(Type.INFO, Status.SUCCESS, "validation OK: "+identResult.getReport().getMessage()); 
        }
        
        ValidateResult result = new ValidateResult.Builder(format, report)
        .ofThisFormat(valid).build();
        return result;
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.validate.Validate#describe()
     */
    public ServiceDescription describe() {
    	String sServiceDescription = "Validation service using Mime-Util (2.1.3)." +
    			"The Mime-Type Extractor Service uses the Mime Type Detection Utility (Mime-Util) project from sourceforge https://sourceforge.net/projects/mime-util/" +
    			"It runs under Apache License V2.0 and enables Java programs to detect MIME types based on file extensions, magic data and content sniffing." +
    			"It supports detection from java.io.File, java.io.InputStream, java.net.URL and byte arrays. " +
    			"See the project website www.eoss.org.nz/mime-util for detail" +
    			"The given mime-util service wrapper at hand provides a wrapper following the Planets preservation interfaces" +
    			"'Identify' and 'Validate'.";
    	
        ServiceDescription.Builder sd = new ServiceDescription.Builder(
                "Mime-Util Validation Service", Validate.class
                        .getCanonicalName());
        sd.classname(this.getClass().getCanonicalName());
        sd.description(sServiceDescription);
        sd.author("Andrew Lindley");
        sd.tool(MimeUtilShared.tool);
        sd.furtherInfo(URI.create("https://sourceforge.net/projects/mime-util/"));
        //sd.inputFormats(inputFormats());
        sd.serviceProvider("AIT - Austrian Institute of Technology GmbH - www.ait.ac.at");
        sd.parameters(MimeUtilIdentification.getAllSupportedParameters());
  
        return sd.build();
    }

    /**
     * @param identResult The IdentifyResult object from calling the service's identify operation
     * @param fmt The pronom URI the binary should be validated against
     * @return Returns true if the given pronom URI describes the given binary
     *         file, else false
     */
    private boolean validateDigitalObject(IdentifyResult identResult, URI fmt){
    	/* check if it is what we expected: */
        for (URI uri : identResult.getTypes()) {
            if (uri != null && uri.equals(fmt)) {
                /* One of the identified types is the one we expected: */
                return true;
            }
        }
        return false;
    }

    /**
     * Calls the MimeUtilIdentification service and delivers a IdentifyResult
     * @param digitalObject
     * @param params
     * @return
     */
    private IdentifyResult identify(final DigitalObject digitalObject, List<Parameter> params) {
        /* Identify the binary: */
        MimeUtilIdentification identification = new MimeUtilIdentification();
        IdentifyResult identifyResult = identification.identify(digitalObject, params);
        return identifyResult;
    }

}
