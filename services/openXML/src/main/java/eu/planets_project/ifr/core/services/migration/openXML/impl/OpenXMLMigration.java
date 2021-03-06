/*
 * migrationService.java
 *
 * Created on 27 June 2007, 15:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eu.planets_project.ifr.core.services.migration.openXML.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.io.FileUtils;
import org.jboss.annotation.ejb.RemoteBinding;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.ifr.core.services.migration.openXML.api.OpenXMLMigrationServiceRemoteInterface;
import eu.planets_project.ifr.core.services.migration.openXML.api.PlanetsServiceException;
import eu.planets_project.ifr.core.services.migration.openXML.common.ConversionReport;
import eu.planets_project.ifr.core.services.migration.openXML.common.ConversionResult;
import eu.planets_project.ifr.core.services.migration.openXML.common.ConvertedFile;
import eu.planets_project.ifr.core.services.migration.openXML.common.ConvertedFileNames;
import eu.planets_project.services.utils.ServiceUtils;

/**
 *
 * @author CFWilson
 */
@javax.jws.WebService(name="OpenXMLMigration", targetNamespace="http://planets-project.eu/ifr/core/services/migration", serviceName="OpenXMLMigration")
@Stateless()
@Remote(OpenXMLMigrationServiceRemoteInterface.class)
@RemoteBinding(jndiBinding="planets-project.eu/OpenXMLMigrationServiceRemoteInterface")
@MTOM
@StreamingAttachment( parseEagerly=true, memoryThreshold=ServiceUtils.JAXWS_SIZE_THRESHOLD )
public class OpenXMLMigration implements OpenXMLMigrationServiceRemoteInterface {
    
	private static Logger log = Logger.getLogger(OpenXMLMigration.class.getName());
	static final String TEMP_EXTENSION = ".$TMP$";
    static final String XML_EXTENSION = ".xml";

    // Configuration variable and public method to test that the configuration is OK
    private OpenXMLMigrationConfig config = null;

    /**
     * @return true if the configuration is valid
     */
    @javax.jws.WebMethod()
    public boolean isConfigValid() {
        boolean configValid = false;
        if (config != null) {
            configValid = config.isConfigValid();
        }
        return configValid;
    }

    // Conversion Report variable to hold the results of the conversion process and return them to the user
    private ConversionReport convReport;
    
    /** 
     * Creates a new instance of OpenXMLServiceTest
     * @throws PlanetsServiceException 
     */
    public OpenXMLMigration() throws PlanetsServiceException {
        
        config = new OpenXMLMigrationConfig("eu/planets_project/ifr/core/services/migration/openXML/openXMLMigration-config.xml");
    }

    /**
     * @see eu.planets_project.ifr.core.services.migration.openXML.api.OpenXMLMigrationServiceRemoteInterface#convertFileRef(java.lang.String)
     */
    @javax.jws.WebMethod()
    public String convertFileRef(@javax.jws.WebParam(name="fileRef") String toConvert_) throws PlanetsServiceException {
    //public ConversionReport convertFileRef(String toConvert_, String convertPath_) throws PlanetsServiceException {
    	log.fine("convertFileRef:: fileRef = " + toConvert_);
        File toConvert = new File(toConvert_);
        // Create the output dir if it doesn't already exist
        File convertPath = new File(config.getOutputDir());
        try {
        	log.fine("Checking output directory " + convertPath.getCanonicalPath());
            if (!convertPath.exists()) {
            	log.fine("Creating output directory " + convertPath.getCanonicalPath());
                if (!convertPath.mkdirs()) {
                    throw new PlanetsServiceException("OpenXMLMigration::convertFile: Couldn't create output directory");
                }
            }
            
        } catch (IOException e) {
            throw new PlanetsServiceException(e);
        }
        return this.convertFile(toConvert, convertPath).get(0);
    }
    
    
    /**
     * @see eu.planets_project.ifr.core.services.migration.openXML.api.OpenXMLMigrationServiceRemoteInterface#convertFileRefs(java.lang.String[])
     */
    @javax.jws.WebMethod()
    public String[] convertFileRefs(@javax.jws.WebParam(name="fileRefs") String[] toConvert_ ) throws PlanetsServiceException {

        String[] ret = new String[toConvert_.length];
        for(int i=0; i<toConvert_.length; i++){
          ret[i] = convertFileRef(toConvert_[i]);
        }
        return ret;
    }

    /**
     * Method to take a single file object and request conversion to a path
     * @param toConvert_
     * @param convertPath_
     * @return the list of converted names
     * @throws PlanetsServiceException
     */
    public ArrayList<String> convertFile(File toConvert_, File convertPath_) throws PlanetsServiceException {

        // Let's test that the configuration is OK
        if (!this.isConfigValid()) {
            // Throw an exception if it doesn't
            throw new PlanetsServiceException("OpenXMLService::convertFile: Service configuration is not valid");
        }

        // First test that the file is a file and exists
        if (!toConvert_.isFile()) {
            // Throw an exception if it doesn't
            throw new PlanetsServiceException("OpenXMLService::convertFile: " +
                                              toConvert_.getAbsolutePath() +
                                               " is not a file.");
        }

        // Test that the conversion path is a valid directory
        if (!convertPath_.isDirectory()) {
            // Throw an exception if it doesn't
            throw new PlanetsServiceException("OpenXMLService::convertFile: " +
                                              convertPath_.getAbsolutePath() +
                                               " is not a directory");
            
        }

        // We have a real file, let's copy it to the OpenXML conversion directory.
        // At first we'll add the $TMP$ extension to stop clashes, so lets sort the name out
        String toConvertTempName = config.getConversionDir() + "/" + toConvert_.getName() + TEMP_EXTENSION;
        // Now to create the file object and check if it already exists
        File toConvertTempCopy = new File(toConvertTempName);
        if (toConvertTempCopy.exists()) {
            // Already in existence, for now throw an exception
            // TODO : we can sort this by adding some sort of "friendly" indexer to prevent the name clash
            throw new PlanetsServiceException("OpenXMLService::convertFile: File " +
                                              toConvert_.getName() + " can't be converted due to a temp file name clash " +
                                               toConvertTempCopy.getAbsolutePath());
        }

        // Now call the internal copy method in a catch to wrap the IOException.
        try {
            this.copyFile(toConvert_, toConvertTempCopy);
        }
        catch (IOException ie) {
            throw new PlanetsServiceException("OpenXMLService::convertFile: Problem copying file " +
                                              toConvert_.getAbsolutePath() + " to " +
                                               toConvertTempCopy.getAbsolutePath(), ie);
        }

        // We've now copied the file so we can rename it and get rid of the temp extension & make a file
        String toConvertName = 
                toConvertTempCopy.getAbsolutePath().substring(0,
                                                                         toConvertTempCopy.getAbsolutePath().lastIndexOf(TEMP_EXTENSION));
        File toConvertCopy = new File(toConvertName);

        // Do the rename, under control, have to throw a stupid exception since reason for a rename
        // fail is no more specific than a returned false
        if (!toConvertTempCopy.renameTo(toConvertCopy)) {
            throw new PlanetsServiceException("OpenXMLService::convertFile: Couldn't rename file " +
                                              toConvertTempCopy.getAbsolutePath() + 
                                              "  to " +
                                               toConvertCopy.getAbsolutePath() + " due to an unknown problem");
        }
        // We need to collect the returned file, the trigger is the XML report file
        // that corresponds to the file name so let's make it
        String reportFileName = config.getConvertedDir() + "/" + 
                                  toConvertCopy.getName().substring(0, toConvertCopy.getName().lastIndexOf(".")) +
                                  XML_EXTENSION;
        
        // Get the returned conversion report object
        getConversionReport(reportFileName);

        // Now let's copy the converted file to the requested location
        return this.processConversionReport(new File(config.getConvertedDir()), convertPath_);
    }
    
    /*
     * protected method to copy a file given a source and destination
     */
    protected void copyFile(File source_, File destination_) throws IOException, PlanetsServiceException {

        // First check that the file to doesn't already exist
        if (destination_.exists()) {
            // Already in existence, for now throw an exception
            // TODO : we can sort this by adding some sort of "friendly" indexer to prevent the name clash
            throw new PlanetsServiceException("OpenXMLService::copyFile: File " +
                                              source_.getAbsolutePath() + 
                                              " can't be copied to " +
                                               destination_.getAbsolutePath() + " due to a name clash");
        }

        // Let's do the file copy, first we'll need an input and output stream
        InputStream in = null;
        OutputStream out = null;
        // Put this in a try block so we can catch the IOException and wrap it in a planets one
        try {
            // Set up the new streams
            in = new FileInputStream(source_);
            out = new FileOutputStream(destination_);

            // Now for the actual copy, we'll need a byte buffer and an integer for the length
            byte[] buffer = new byte[1024];
            int len;
            // Loop through and copy
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
        finally {
                // Always try to close the streams
                in.close();
                out.close();
        }
    }
    
    protected void getConversionReport(String reportFileName_) throws PlanetsServiceException {
        boolean isFinished = false;
        File[] foundReportFiles = null;

        // Need this for the file filter
        final File reportFile = new File(reportFileName_);
        /*
        * Put together a file filter that only returns the report file
        */
        FileFilter triggerFilt = new FileFilter() {
           public boolean accept(File file) {
               return(file.isFile() && (file.compareTo(reportFile) == 0));
           }
        };

        // Need a directory to watch, this is the converted directory from the config
        File convertedDir = new File(config.getConvertedDir());

        // Spare object for unmarshaller return
        Object obj = null;
        
        // Set the timeout loop
        int retries = config.getTimeout() / config.getPollfrequency();
        int timesTried = 0;
        log.fine("getConversionReport:: retries = " + retries);
        try {
            // Loop looking for files, only finishes if it finds a .$FILESET$
            // or a .$END$ file, sleeps for 5 seconds between iterations
            while (!isFinished)
            {
               foundReportFiles = convertedDir.listFiles(triggerFilt);
               isFinished = (foundReportFiles.length > 0);
               /*
                * TODO: Thread the config timeout variable  through this loop to prevent deadlock
                */
               Thread.sleep(config.getPollfrequency());
               if (timesTried++ >= retries)
               {
                   throw new PlanetsServiceException("openXMLMigration:: Timed out waiting for returned file: " + reportFileName_, new TimeoutException());
               }
            }

            // Extra sleep to avoid access clash with Wolfgang's tool
            Thread.sleep(2000);
            // We've got the report file now so we need to deserialize to an object
            JAXBContext context = JAXBContext.newInstance(ConversionReport.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            obj = unmarshaller.unmarshal(foundReportFiles[0]);
            
            //Delete the report
            FileUtils.deleteQuietly(foundReportFiles[0]);
        }
        catch (JAXBException jxbe) {
            throw new PlanetsServiceException("OpenXMLService::getReportFile: JAXB Problem deserializing from file " +
                                              foundReportFiles[0].getAbsolutePath(), jxbe);
        }
        catch (InterruptedException ie) {
            throw new PlanetsServiceException("OpenXMLService::getReportFile: Caught InterruptedException", ie);
        }

        // Return found file
        this.convReport = (ConversionReport)obj;
    }

    /*
     * protected method to process a conversion report file and move the files
     */
    protected ArrayList<String> processConversionReport(File currentPath_, File convPath_) throws PlanetsServiceException {
        int successfulCount = 0;
        
        log.fine("processConversionReport:: currentPath_ = " + currentPath_.getAbsolutePath());
        log.fine("processConversionReport:: convPath_ = " + convPath_.getAbsolutePath());
        List<ConvertedFile> convFileList = convReport.getConvertedFileList();
        ArrayList<String> outputFileList = new ArrayList<String>();
        
        Iterator<ConvertedFile> iterator = convFileList.iterator();
        log.fine("Iterator has " + convFileList.size() + " elements");
        while (iterator.hasNext()) {
            ConvertedFile convFile = iterator.next();

            // Get the conversion result and check it
            ConversionResult convResult = convFile.getResult();
            if (convResult.getCode() != 0) {
                // If the process failed add a null and check the other files
                outputFileList.add(null);
                continue;
            }
            
            ConvertedFileNames convFileNames = convFile.getConvertedFileNames();
            File originalCopy = new File(currentPath_.getAbsolutePath() + "/" + convFileNames.getInput());
            File currentFile = new File(currentPath_.getAbsolutePath() + "/" + convFileNames.getActual());
            File outputFile = new File(convPath_.getAbsolutePath() + "/" + convFileNames.getOutput());
            
            if (originalCopy.isFile() && originalCopy.exists()) {
                FileUtils.deleteQuietly(originalCopy);
            }

            if (!currentFile.isFile()) {
                throw new PlanetsServiceException("OpenXMLService::processConversionReport: " + currentFile.getAbsolutePath() + "is not a file");
            }

            // Check if the output file already exists
            if (outputFile.exists()) {
                // Output file exists so we'll need a new file name, call the indexer
                outputFile = new File(createIndexedName(outputFile.getAbsolutePath()));
            }

            log.fine("processConversionReport:: renaming " + 
                                                                          currentFile.getAbsolutePath() + " to " +
                                                                          outputFile.getAbsolutePath());
            if (!currentFile.renameTo(outputFile)) {
                throw new PlanetsServiceException("OpenXMLService::processConversionReport: Unknown error renaming " + currentFile.getAbsolutePath() + "to" + outputFile.getAbsolutePath());
            }
            try {
                outputFileList.add(outputFile.getCanonicalPath().replaceAll("\\\\", "/"));
                
            } catch (IOException e) {
                throw new PlanetsServiceException(e);
            }
            successfulCount++;
        }
        
        
        return outputFileList;
    }
    
    // Method to add an index to a file name before the extension where the file already exists, i.e. if test.txt exists will return test[1].txt
    protected String createIndexedName(String nameToIndex_) {
        // Strip the extension from the name
        String pathPart = nameToIndex_.substring(0, nameToIndex_.lastIndexOf("."));
        String extPart = nameToIndex_.substring(nameToIndex_.lastIndexOf("."));
        String indexedName = null;

        log.fine("createIndexName:: nameToIndex_: " + nameToIndex_);
        log.fine("createIndexName:: pathPart: " + pathPart);
        log.fine("createIndexName:: extPart: " + extPart);

        for (int indexer = 0; indexer < 1000; indexer++) {
            // Create a new file name
            String newName = pathPart + "[" + indexer + "]" + extPart;
            // See if the file exists
            File checkName = new File(newName);
            if (!checkName.exists()) {
                indexedName = checkName.getAbsolutePath();
                break;
            }
        }
        log.fine("createIndexName:: indexedName: " + indexedName);

        return indexedName;
    }
}
