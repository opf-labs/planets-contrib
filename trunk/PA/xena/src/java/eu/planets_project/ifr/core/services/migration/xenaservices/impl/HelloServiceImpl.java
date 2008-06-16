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
//@Stateless
//@Local(HelloService.class)
//@LocalBinding(jndiBinding = "planets-project.eu/HelloService")
//@Remote(HelloService.class)
//@RemoteBinding(jndiBinding = "planets-project.eu/HelloService")

// Web Service Annotations, copied from the inherited interface.
//@WebService(name = HelloService.NAME, serviceName= HelloService.NAME, targetNamespace = PlanetsServices.NS )
//@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class HelloServiceImpl implements HelloServiceExample {
    private static PlanetsLogger log = PlanetsLogger.getLogger(HelloServiceImpl.class);

    /* (non-Javadoc)
     * @see eu.planets_project.ifr.core.security.api.services.HelloService#helloWorld(java.lang.String)
     */
//    @WebMethod(operationName = "HelloWorld", action = PlanetsServices.NS + "/HelloService/HelloWorld")
//    @WebResult(name = "HelloWorldResult", targetNamespace = PlanetsServices.NS + "/HelloService", partName = "HelloWorldResult")
    public String helloWorld(
//           @WebParam(name = "who", targetNamespace = PlanetsServices.NS + "/HelloService", partName = "who")
        String who) {
        return "Hello from Java, " + who + "!";
    }
    /**
     * Simple test.
     * @param args
     */
    static public void main( String[] args ) {
        log.info("Attempting to use a web service.");
        System.out.println("Attempting to use a web service.");
        URL wsdl = null;
        try {
//            wsdl = new URL("http://xplt200574:8080/ifr-xenaservices-ejb/HelloServiceImpl2?wsdl");
            wsdl = new URL("http://xplt200574:8080/ifr-xenaservices-ejb/HelloServiceImpl?wsdl");
//          wsdl = new URL("http://xplt200574:8080/sample-ifr-sample-ejb/HelloServiceImpl?wsdl");
//            wsdl = new URL("http://localhost:3800/WCFService1/HelloService.asmx?WSDL");
//            wsdl = new URL("http://localhost:3800/WCFService1/Service.svc?wsdl");
        } catch( MalformedURLException e ) {
//          log.warn("Badly formed URL!");
          System.out.println("Badly formed URL! "+ e);
        }
        
        Service service = Service.create( wsdl, 
                new QName(PlanetsServices.NS, HelloServiceExample.NAME ));
        HelloServiceExample hs = (HelloServiceExample)service.getPort(HelloServiceExample.class); 
        
        System.out.println("Sending request... ");
        String message = hs.helloWorld("Fred");
//        log.info("Recieved: " + message );
        System.out.println("Recieved: " + message );
    }

}
