/*
 * ConversionReport.java
 *
 * Created on 28 June 2007, 16:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eu.planets_project.ifr.core.services.migration.openXML.common;

/**
 *
 * @author CFwilson
 */
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
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
 *         &lt;element ref="{http://www.planets-project.eu/xml/ns/planets/services/OpenXMLMigrationServiceRemoteInterfaceInfo}convsummary"/>
 *         &lt;element ref="{http://www.planets-project.eu/xml/ns/planets/services/OpenXMLMigrationServiceRemoteInterfaceInfo}files"/>
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
    "conversionSummary",
    "convertedFileList"
})
@XmlRootElement(name = "convReport")
public class ConversionReport {

    @XmlElement(required = true, name="convSummary")
    protected ConversionSummary conversionSummary;
//    @XmlElementWrapper(required = true, name="files") - The required flag is non-standard.
    @XmlElementWrapper(name="files")
    @XmlElement(required = true, name="file")
    protected List<ConvertedFile> convertedFileList;
    /**
     * name of the report file
     */
    @XmlTransient
    public String reportFileName;
    
    /**
     * no arg constructor
     */
    public ConversionReport () {
        conversionSummary = new ConversionSummary();
        reportFileName = "";
    }

    /**
     * @param reportFileNameVal a name for the report file
     */
    public ConversionReport (String reportFileNameVal) {
        conversionSummary = new ConversionSummary();
        reportFileName = reportFileNameVal;
    }
    /**
     * Gets the value of the convsummary property.
     * 
     * @return
     *     possible object is
     *     {@link ConversionSummary }
     *     
     */
    public ConversionSummary getConversionSummary() {
        return conversionSummary;
    }

    /**
     * Sets the value of the convsummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConversionSummary }
     *     
     */
    public void setConversionSummary(ConversionSummary value) {
        this.conversionSummary = value;
    }

    /**
     * @return list of converted files
     */
    public List<ConvertedFile> getConvertedFileList() {
        if (convertedFileList == null) {
            convertedFileList = new ArrayList<ConvertedFile>();
        }
        return this.convertedFileList;
    }
    
    /**
     * @param theFile
     * @return the size of the list
     */
    public int addConvertedFile(ConvertedFile theFile) {
        if (convertedFileList == null) {
            convertedFileList = new ArrayList<ConvertedFile>();
        }
        
        convertedFileList.add(theFile);
        
        return convertedFileList.size();
    }
}
