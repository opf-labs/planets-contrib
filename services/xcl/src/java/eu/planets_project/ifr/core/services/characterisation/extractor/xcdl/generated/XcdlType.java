//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, vhudson-jaxb-ri-2.1-661
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2009.01.16 at 04:26:00 PM CET
//

package eu.planets_project.ifr.core.services.characterisation.extractor.xcdl.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * A XCDL document describes digital objects. Every xcdl description shall have
 * an identification number.
 * <p>
 * Java class for xcdlType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;xcdlType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref=&quot;{http://www.planets-project.eu/xcl/schemas/xcl}object&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;id&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xcdlType", propOrder = { "objects" })
@XmlSeeAlso( { Xcdl.class })
public class XcdlType {

    @XmlElement(name = "object", required = true)
    protected List<Object> objects;
    @XmlAttribute(required = true)
    protected String id;

    /**
     * An object is a string of content carrying tokens (called normData) each
     * token can be associated with different meanings (called properties).
     * Properties can either add an atomic meaning to the referenced token or
     * they can reference an other object with the token. Gets the value of the
     * objects property.
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the objects property.
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getObjects().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Object }
     */
    public List<Object> getObjects() {
        if (objects == null) {
            objects = new ArrayList<Object>();
        }
        return this.objects;
    }

    /**
     * Gets the value of the id property.
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

}
