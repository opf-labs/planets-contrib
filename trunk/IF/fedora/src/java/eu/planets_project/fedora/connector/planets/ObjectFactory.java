//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-793 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.11.26 at 06:02:40 PM GMT+01:00 
//


package eu.planets_project.fedora.connector.planets;

import eu.planets_project.fedora.connector.planets.Datastream;
import eu.planets_project.fedora.connector.planets.PlanetsDatastream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.planets_project.fedora.planetsdatastream.test package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _PlanetsDatastream_QNAME = new QName("", "planetsDatastream");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.planets_project.fedora.planetsdatastream.test
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Datastream }
     * 
     */
    public Datastream createDatastream() {
        return new Datastream();
    }

    /**
     * Create an instance of {@link PlanetsDatastream }
     * 
     */
    public PlanetsDatastream createPlanetsDatastream() {
        return new PlanetsDatastream();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PlanetsDatastream }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "planetsDatastream")
    public JAXBElement<PlanetsDatastream> createPlanetsDatastream(PlanetsDatastream value) {
        return new JAXBElement<PlanetsDatastream>(_PlanetsDatastream_QNAME, PlanetsDatastream.class, null, value);
    }

}
