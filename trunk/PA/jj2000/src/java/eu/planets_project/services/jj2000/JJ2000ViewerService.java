/**
 * 
 */
package eu.planets_project.services.jj2000;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
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
    
    private static Log log = LogFactory.getLog(JJ2000ViewerService.class);

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

    private CreateViewResult returnWithErrorMessage(String message) {
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
        
        // Store copies of the viewable digital objects:
        for( DigitalObject dob : digitalObjects ) {
            // Can only cope if the object is 'simple':
            if( dob.getContent() == null ) {
                return this.returnWithErrorMessage("The Content of the DigitalObject should not be NULL.");
            }
        }
        
        // Create a service report:
        ServiceReport rep = new ServiceReport();
        rep.setErrorState(ServiceReport.SUCCESS);

        // Return the view id:
        return new CreateViewResult(null, null, rep);
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#getViewStatus(java.lang.String)
     */
    public ViewStatus getViewStatus(String sessionIdentifier) {
        ViewStatus vs = new ViewStatus( -1, null );
        return vs;
    }

    
    
}
