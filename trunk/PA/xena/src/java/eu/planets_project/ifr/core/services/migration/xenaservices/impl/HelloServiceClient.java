/**
 * 
 */
package eu.planets_project.ifr.core.services.migration.xenaservices.impl;

import eu.planets_project.ifr.core.common.services.PlanetsServices;
import eu.planets_project.ifr.core.services.migration.xenaservices.api.HelloService;

import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 * @author AnJackson
 *
 */

public class HelloServiceClient {

    /**
     * Simple test.
     * @param args
     */
    static public void main( String[] args ) {
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
                new QName(PlanetsServices.NS, HelloService.NAME ));
        HelloService hs = (HelloService)service.getPort(HelloService.class); 
        
        System.out.println("Sending request... ");
        String message = hs.helloWorld("Fred");
//        log.info("Recieved: " + message );
        System.out.println("Recieved: " + message );
    }

}
