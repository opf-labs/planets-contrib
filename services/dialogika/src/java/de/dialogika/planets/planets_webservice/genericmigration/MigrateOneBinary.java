
package de.dialogika.planets.planets_webservice.genericmigration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="binary" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="parameters" type="{http://www.dialogika.de/Planets/planets.webservice/GenericMigration}ArrayOfParameter" minOccurs="0"/>
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
    "binary",
    "parameters"
})
@XmlRootElement(name = "migrateOneBinary")
public class MigrateOneBinary {

    protected byte[] binary;
    protected ArrayOfParameter parameters;

    /**
     * Gets the value of the binary property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getBinary() {
        return binary;
    }

    /**
     * Sets the value of the binary property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setBinary(byte[] value) {
        this.binary = ((byte[]) value);
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfParameter }
     *     
     */
    public ArrayOfParameter getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfParameter }
     *     
     */
    public void setParameters(ArrayOfParameter value) {
        this.parameters = value;
    }

}
