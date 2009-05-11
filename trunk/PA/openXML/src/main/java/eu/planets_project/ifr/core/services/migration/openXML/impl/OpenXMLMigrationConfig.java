/*
 * OpenXMLServiceConfig.java
 *
 * Created on 30 June 2007, 09:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eu.planets_project.ifr.core.services.migration.openXML.impl;

import eu.planets_project.ifr.core.services.migration.openXML.api.PlanetsServiceException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author CFwilson
 * Class to open and load the config file into some simple variables
 **/
public class OpenXMLMigrationConfig {
    // Holds the path to the config file and the XML doc representation of the file
    private String confFilePath = null;
    /**
     * @return the config file path
     */
    public String getConfFilePath() {return confFilePath;}
    private Document confDoc;

    // Holds the paths to the conversion and the converted directories
    private String conversionDir = null;
    /**
     * @return the conversion directory
     */
    public String getConversionDir() {return conversionDir;}
    private String convertedDir = null;
    /**
     * @return the converted result directory
     */
    public String getConvertedDir() {return convertedDir;}
    private String outputDir = null;
    /**
     * @return the output dir
     */
    public String getOutputDir() {return outputDir;}
    // Poll frequency default to 5 seconds
    private int pollFrequency = 5000;
    /**
     * @return the polling frequency
     */
    public int  getPollfrequency() {return pollFrequency;}
    private int timeout = 60000;
    /**
     * @return a timeout val
     */
    public int getTimeout() {return timeout;}
    private boolean configValid = false;
    /**
     * @return true if config is valid
     */
    public boolean isConfigValid() {return configValid;}

    /**
     * Constructor, opens and parses the config file
     * just takes a location for the xml config file
     * @param path 
     * @throws PlanetsServiceException 
     */
    public OpenXMLMigrationConfig(String path) throws PlanetsServiceException {
        // Set the path member and parse the config file
        confFilePath = path;
        parseFile(path);
    }

    /**
     * Public method to parse the  passed config file
     * @param path 
     * @throws PlanetsServiceException 
     */
    public void parseFile(String path) throws PlanetsServiceException {
        // Document builder factory to create the document builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            // Create a new builder and parse the XML config file
            DocumentBuilder builder = factory.newDocumentBuilder();
            confDoc = builder.parse(this.getClass().getClassLoader().getResourceAsStream(path));

            // Get the folder in which to place items for conversion, there can be ONLY ONE
            NodeList conversionDirNodes = confDoc.getElementsByTagName("conversionDir");
            if (conversionDirNodes.getLength() != 1) {
                throw new PlanetsServiceException("There should be one conversionDir element in the config file: " + path);
            }
            conversionDir = ((Element)conversionDirNodes.item(0)).getAttribute("path");

            // Now get the folder in which the OpenXML Tool will place converted files, there can be ONLY ONE
            NodeList convertedDirNodes = confDoc.getElementsByTagName("convertedDir");
            if (convertedDirNodes.getLength() != 1) {
                throw new PlanetsServiceException("There should be one convertedDir element in the config file: " + path);
            }
            convertedDir = ((Element)convertedDirNodes.item(0)).getAttribute("path");

            // Now get the folder in which to write the output files, there can be ONLY ONE
            NodeList outputDirNodes = confDoc.getElementsByTagName("outputDir");
            if (outputDirNodes.getLength() != 1) {
                throw new PlanetsServiceException("There should be one outputDir element in the config file: " + path);
            }
            outputDir = ((Element)outputDirNodes.item(0)).getAttribute("path");

            // Now get the poll frequency
            NodeList pollFreqNodes = confDoc.getElementsByTagName("pollFrequency");
            if (pollFreqNodes.getLength() != 1) {
                throw new PlanetsServiceException("There should be one pollFrequency element in the config file: " + path);
            }
            pollFrequency = Integer.parseInt(((Element)pollFreqNodes.item(0)).getAttribute("milliseconds"));

            // Now get the poll frequency
            NodeList timeoutFreqNodes = confDoc.getElementsByTagName("timeout");
            if (timeoutFreqNodes.getLength() != 1) {
                throw new PlanetsServiceException("There should be one timeout element in the config file: " + path);
            }
            timeout = Integer.parseInt(((Element)timeoutFreqNodes.item(0)).getAttribute("milliseconds"));

        } catch (SAXParseException spe) {
            // Error generated by the parser
            System.out.println("\n** Parsing error"
              + ", line " + spe.getLineNumber()
              + ", uri " + spe.getSystemId());
            System.out.println("   " + spe.getMessage() );

            // Use the contained exception, if any
            Exception  x = spe;
            if (spe.getException() != null)
              x = spe.getException();
            x.printStackTrace();

        } catch (SAXException sxe) {
            // Error generated during parsing
            Exception  x = sxe;
            if (sxe.getException() != null)
              x = sxe.getException();
            x.printStackTrace();

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();

        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }
        // Only set the path member with the valid flag once all of this is done
        confFilePath = path;
        configValid = true;
    }
}
