
package de.dialogika.planets.planets_webservice.genericmigration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetMatchingOutTypeResult" type="{http://www.dialogika.de/Planets/planets.webservice/GenericMigration}ArrayOfString" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getMatchingOutTypeResult"
})
@XmlRootElement(name = "GetMatchingOutTypeResponse")
public class GetMatchingOutTypeResponse {

    @XmlElement(name = "GetMatchingOutTypeResult")
    protected ArrayOfString getMatchingOutTypeResult;

    /**
     * Gets the value of the getMatchingOutTypeResult property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getGetMatchingOutTypeResult() {
        return getMatchingOutTypeResult;
    }

    /**
     * Sets the value of the getMatchingOutTypeResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setGetMatchingOutTypeResult(ArrayOfString value) {
        this.getMatchingOutTypeResult = value;
    }

}
