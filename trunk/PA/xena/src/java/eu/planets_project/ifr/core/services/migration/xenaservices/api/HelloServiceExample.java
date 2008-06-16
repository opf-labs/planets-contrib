/**
 * 
 */
package eu.planets_project.ifr.core.services.migration.xenaservices.api;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;

import eu.planets_project.ifr.core.common.services.PlanetsServices;

/**
 * @author AnJackson
 *
 */
@WebService(name = HelloServiceExample.NAME, serviceName= HelloServiceExample.NAME, targetNamespace = PlanetsServices.NS )
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface HelloServiceExample {
    
    public static final String NAME = "HelloService";
    public static final QName QNAME = new QName(PlanetsServices.NS, HelloServiceExample.NAME );

    /**
     *
     * @param parameters
     * @return
     *     returns eu.planets_project.ifr.core.sample.helloservice.HelloWorldResponse
     */
    @WebMethod(operationName = "HelloWorld", action = PlanetsServices.NS + "/HelloService/HelloWorld")
    @WebResult(name = "HelloWorldResult", targetNamespace = PlanetsServices.NS + "/HelloService", partName = "HelloWorldResult")
    public abstract String helloWorld(
        @WebParam(name = "who", targetNamespace = PlanetsServices.NS + "/HelloService", partName = "who")
        String who);

}
                                  