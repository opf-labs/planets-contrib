/*
 * ConversionSummary.java
 *
 * Created on 29 June 2007, 10:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eu.planets_project.ifr.core.services.migration.openXML.common;

/**
 *
 * @author CFwilson
 */
import java.math.BigInteger;
import java.util.Date;
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
 *         &lt;element ref="{http://www.planets-project.eu/xml/ns/planets/services/odfInfo}result"/>
 *         &lt;element ref="{http://www.planets-project.eu/xml/ns/planets/services/odfInfo}starttime"/>
 *         &lt;element ref="{http://www.planets-project.eu/xml/ns/planets/services/odfInfo}endtime"/>
 *         &lt;element ref="{http://www.planets-project.eu/xml/ns/planets/services/odfInfo}filenum"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "", propOrder = {
//    "result",
//    "starttime",
//    "endtime",
//    "filenum"
//})
@XmlRootElement(name = "convSummary")
public class ConversionSummary {

    @XmlElement(required = true, name="result")
    protected ConversionResult conversionResult = new ConversionResult();
    @XmlElement(required = true, name="startTime")
    protected Date startTime = new Date();
    @XmlElement(required = true, name="endTime")
    protected Date endTime;
    @XmlElement(required = true, name="fileNum")
    protected int fileNum = 0;

    public ConversionSummary() {
    }
    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link Result }
     *     
     */
    public ConversionResult getresult() {
        return conversionResult;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link Result }
     *     
     */
    public void setresult(ConversionResult value) {
        this.conversionResult = value;
    }

    /**
     * Gets the value of the starttime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Date getstarttime() {
        return startTime;
    }

    /**
     * Sets the value of the starttime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setstarttime(Date value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the endtime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Date getendtime() {
        return endTime;
    }

    /**
     * Sets the value of the endtime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setendtime(Date value) {
        this.endTime = value;
    }

    /**
     * Gets the value of the filenum property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public int getFileNum() {
        return fileNum;
    }
    
    public int incrementFileNum() {
        return ++fileNum;
    }
}
