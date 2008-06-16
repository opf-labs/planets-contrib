/**
 * 
 */
package eu.planets_project.ifr.core.services.migration.xenaservices.impl;

import javax.ejb.Remote;
import javax.ejb.Local;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.LocalBinding;
import javax.ejb.Stateless;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;

import eu.planets_project.ifr.core.common.logging.PlanetsLogger;
import eu.planets_project.ifr.core.common.services.PlanetsServices;
import eu.planets_project.ifr.core.services.migration.xenaservices.api.HelloServiceExample;

import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 * @author AnJackson
 *
 */

// EJB Annotations:
@Stateless
@Local(HelloServiceExample.class)
@LocalBinding(jndiBinding = "planets-project.eu/HelloService")
@Remote(HelloServiceExample.class)
@RemoteBinding(jndiBinding = "planets-project.eu/HelloService")

public class HelloServiceImpl2 implements HelloServiceExample {
    private static PlanetsLogger log = PlanetsLogger.getLogger(HelloServiceImpl2.class);

    /* (non-Javadoc)
     * @see eu.planets_project.ifr.core.security.api.services.HelloService#helloWorld(java.lang.String)
     */
    public String helloWorld(
        String who) {
        return "Hello 2, via Java, " + who + "!";
    }
    
}
