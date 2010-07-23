
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
 *         &lt;element name="migrateOneBinaryResult" type="{http://www.dialogika.de/Planets/planets.webservice/GenericMigration}MigrateOneBinaryResult" minOccurs="0"/>
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
    "migrateOneBinaryResult"
})
@XmlRootElement(name = "migrateOneBinaryResponse")
public class MigrateOneBinaryResponse {

    protected MigrateOneBinaryResult migrateOneBinaryResult;

    /**
     * Gets the value of the migrateOneBinaryResult property.
     * 
     * @return
     *     possible object is
     *     {@link MigrateOneBinaryResult }
     *     
     */
    public MigrateOneBinaryResult getMigrateOneBinaryResult() {
        return migrateOneBinaryResult;
    }

    /**
     * Sets the value of the migrateOneBinaryResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link MigrateOneBinaryResult }
     *     
     */
    public void setMigrateOneBinaryResult(MigrateOneBinaryResult value) {
        this.migrateOneBinaryResult = value;
    }

}
