/*
 * odfToolSimulator.java
 *
 * Created on 19 June 2007, 15:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eu.planets_project.ifr.core.services.migration.odf.impl;

import eu.planets_project.ifr.core.services.migration.odf.impl.DirWatchException;
import eu.planets_project.ifr.core.services.migration.odf.impl.configException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.planets_project.ifr.core.services.migration.odf.common.ConversionReport;
import eu.planets_project.ifr.core.services.migration.odf.common.ConversionSummary;
import eu.planets_project.ifr.core.services.migration.odf.common.ConvertedFile;

/**
 *
 * @author CFwilson
 *
 * Description : This program is intended to be run to provide a facade for the ODF conversion
 *               too that's been created as a service for Planets by Wolfgang Keber.
 *
 *               Wolfgang's tool has taken a "watched folder" approach to file conversion.
 *               This tool will emulate the behaviour of Wolfgang's service without
 *               performing any conversion tasks.
 */
public class odfToolSimulator {
    public final String triggerExt = new String(".$FILESET$");
    public final String endExt = new String(".$END$");
    public final String tempExt = new String(".$TMP$");
    private Config config;
    
    /** Creates a new instance of odfToolSimulator */
    public odfToolSimulator() {
        try {
            File foundFile = null;
            
            // Set up the Config object to hold config file settings
            config = new Config("C:/planets/IF/dev/projects/testUtils/odfToolSimulator/config.xml");
            DirectoryWatcher dirWatch = new DirectoryWatcher(config.getWatchedDir());

            // Go into a loop looking for files, need a trigger file or end file
            boolean isEndFile = false;
            while (!isEndFile)
            {
                // Start the directory watcher and get the returned file
                foundFile = dirWatch.watchDirectory();
                
                // This tests if the file is an end file (ext = .$END$)
                // The test also sets the loop control boolean
                if (!(isEndFile = foundFile.getName().endsWith(endExt))) {
                    // Call the fileset processor
                    FilesetProcessor filProc = new FilesetProcessor(foundFile, new File(config.getOutputDir()));
                }
            }
            // Delete the end file
            foundFile.delete();
        } catch (DirWatchException dwe) {
            // Catch the directory watch exception
            dwe.printStackTrace();

        } catch (InterruptedException ie) {
            // Catch the interrupt exception
            ie.printStackTrace();

        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        } catch (JAXBException jaxbe) {
            jaxbe.printStackTrace();
        }
        catch (configException ce) {
            // Error generated during parsing
            Throwable x = ce;
            do {
                System.out.print(x.getMessage());
                x.printStackTrace();
            }
            while ((x = x.getCause()) != null);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        odfToolSimulator sim = new odfToolSimulator();
    }

    /**
     * Class to open and load the config file into some simple variables
     **/
    private class Config {
        // Holds the path to the config file and the XML doc representation of the file
        private String confFilePath;
        private Document confDoc;
        
        // Holds the paths to the watched and the output directories
        private String watchedDir = null;
        public String getWatchedDir() {return watchedDir;}
        private String outputDir = null;
        public String getOutputDir() {return outputDir;}
        private boolean configValid = false;
        public boolean isConfigValid() {return configValid;}
        
        // Constructor, opens and parses the config file
        public Config(String path) throws configException {
            confFilePath = new String(path);
            parseFile(path);
        }
        
        // public method to parse the  passed config file
        public void parseFile(String path) throws configException {
            // Factories
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            try {
                // Create a new builder and parse the document
                DocumentBuilder builder = factory.newDocumentBuilder();
                confDoc = builder.parse(path);
                confFilePath = path;

                // Get the watched folder, there can be ONLY ONE
                NodeList watchedDirNodes = confDoc.getElementsByTagName("watchedDir");
                if (watchedDirNodes.getLength() != 1) {
                    throw new configException("There should be one watchedDir in the config file: " + path);
                }
                watchedDir = ((Element)watchedDirNodes.item(0)).getAttribute("path");
                
                // Now get the output folder, there can be ONLY ONE
                NodeList outputDirNodes = confDoc.getElementsByTagName("outputDir");
                if (outputDirNodes.getLength() != 1) {
                    throw new configException("There should be one outputDir in the config file: " + path);
                }
                outputDir = ((Element)outputDirNodes.item(0)).getAttribute("path");

            } catch (SAXParseException spe) {
                // Error generated by the parser
                System.out.println("\n** Parsing error"
                  + ", line " + spe.getLineNumber()
                  + ", uri " + spe.getSystemId());
                System.out.println("   " + spe.getMessage() );

                throw new configException(spe);
            } catch (SAXException sxe) {
                throw new configException(sxe);
            } catch (ParserConfigurationException pce) {
                throw new configException(pce);
            } catch (IOException ioe) {
                throw new configException(ioe);
            }
            configValid = true;
        }
    }
    /**
     * Class to implement the directory watcher from Wolfgang's Tool
     */
    public class DirectoryWatcher {
       private File watchedDir;
       /*
        * Constructor for class sets up the watched directory
        */
       public DirectoryWatcher(String watchedDirName) throws DirWatchException {
           // Check that both of the folders exist
           try {
               watchedDir = new File(watchedDirName);
               checkWatchedDir();
           }
           finally {
           }
       }

       /*
        * Method to check that the directory to be watched really is a directory.
        * Throws an excepion if not
        */
       private void checkWatchedDir() throws DirWatchException {
           if (!watchedDir.isDirectory()) {
               throw new DirWatchException(watchedDir.getPath() + " is not a directory");
           }
       }
       
       // Method to poll input directory and flag a file (at first)
       // Returns false if finds an end file, true if a trigger file found (file in parameter)
       public File watchDirectory () throws InterruptedException, DirWatchException {
           checkWatchedDir();
           boolean isFinished = false;
           File[] foundFiles = null;
           
           /*
            * Put together an ad hoc file filter that only returns files that don't end with $TMP$
            */
           FileFilter triggerFilt = new FileFilter() {
               public boolean accept(File file) {
//                   return (file.isFile() &&
//                           (file.getName().endsWith(triggerExt) || file.getName().endsWith(endExt)));
                   return(file.isFile() && !(file.getName().endsWith(tempExt)));
               }
           };

           // Loop looking for files, only finishes if it finds a .$FILESET$
           // or a .$END$ file, sleeps for 5 seconds between iterations
           while (!isFinished)
           {
               foundFiles = watchedDir.listFiles(triggerFilt);
               isFinished = (foundFiles.length > 0);
               Thread.sleep(5000);
           }

           // Return found file
           return foundFiles[0];
       }
    }
    
    /*
     * Class to process a fileset
     */
    public class FilesetProcessor {
        // Looging data
        private ConversionReport conversionReport;

        public FilesetProcessor(File filesetDesc, File outputDir) throws IOException,
                                                                         DirWatchException,
                                                                         JAXBException {
            // Get the root input folder details and check it's a directory
            File rootInDir = new File(filesetDesc.getParent());
            if (!rootInDir.isDirectory())
                throw new DirWatchException("FilesetProcessor:: rootInDir " +
                                            rootInDir.getAbsolutePath() +
                                            " is not a directory");

            // Now the root output folder details and check it's a directory
            File rootOutDir = new File(outputDir.getAbsolutePath());
            if (!rootOutDir.isDirectory())
                throw new DirWatchException("FilesetProcessor:: rootOutDir " +
                                            rootOutDir.getAbsolutePath() +
                                            " is not a directory");

            // Get the processed object name
            String procObjectName =
                (filesetDesc.getName().substring(0,
                                                 filesetDesc.getName().lastIndexOf(".")));

            // Create the conversion report object with the file name
            conversionReport = new ConversionReport(outputDir.getAbsolutePath() + "\\" + procObjectName + ".xml");

            // Now determine if the passed filesetDesc is a trigger file (for a directory) or whether it
            // is just a single file conversion, so if it's a trigger file
            if (filesetDesc.getName().endsWith(triggerExt)) {
                // Delete the trigger file
                filesetDesc.delete();


                File procDir = new File(rootInDir.getAbsolutePath() + "\\" + procObjectName);
                if (!procDir.isDirectory())
                    throw new DirWatchException("FilesetProcessor:: procDir " +
                                                procDir.getAbsolutePath() +
                                                " is not a directory");

                this.processFileset(rootInDir, rootOutDir, procDir);
            }
            // Else its a normal file
            else
            {
                processFile(filesetDesc, rootOutDir);
            }
            ConversionSummary conversionSummary = conversionReport.getConversionSummary();

            // Set the finsh processing time
            conversionSummary.setendtime(new Date());
            this.ouputXMLreport();
        }
        
        private void processFileset (File rootInDir, File rootOutDir, File procDir) throws IOException,
                                                                                           DirWatchException,
                                                                                           JAXBException {
            // Put together the destination directory file by path
            File destDir = new File(rootOutDir.getAbsolutePath() + "\\" + procDir.getName());
            
            // If it exists then delete it, this is expedient for a testing module
            // NOT recomended behaviour for an operational system
            if (destDir.exists()) {
                destDir.delete();
            }
            
            processDirectory(rootInDir, rootOutDir, procDir);
        }
        
        private void processDirectory (File inDir, File outDir, File procDir) throws DirWatchException,
                                                                                     JAXBException {
            // First create the destination directory
            File procOutDir = new File(outDir.getAbsolutePath() + "\\" + procDir.getName());
            System.out.println("processDirectory:: procOutDir is: " + procOutDir.getAbsolutePath());
            if (!procOutDir.mkdir())
                throw new DirWatchException("processDirectory:: Couldn't create proOutDir " + 
                                            procOutDir.getAbsolutePath());

            // Now loop through the files in the subDir
            File[] filesToProc = procDir.listFiles();
            for (File procFile : filesToProc) {
                // If a file, it's easy, just move it
                if (procFile.isFile()) {
                    processFile(procFile, procOutDir);
                }
                // Directory is trickier, try the dreaded recursion
                else if (procFile.isDirectory()) {
                    processDirectory(procDir, procOutDir, procFile);
                }
            }
            // Delete the source directory
            if (!procDir.delete())
                throw new DirWatchException("processDirectory:: Couldn't delete procDir " + 
                                            procDir.getAbsolutePath());
        }

        private void processFile(File procFile, File outputDir) throws DirWatchException {
            ConversionSummary conversionSummary = conversionReport.getConversionSummary();
            File destFile = new File(outputDir.getAbsolutePath() + "\\" + procFile.getName() + "x");
            if (!procFile.renameTo(destFile))
                throw new DirWatchException("processDirectory:: Failed to rename procFile " + 
                                            procFile.getAbsolutePath() +
                                            " to destFile " +
                                            destFile.getAbsolutePath());
            // Increment the summary report file count;
            conversionSummary.incrementFileNum();
            ConvertedFile thisFile = new ConvertedFile(conversionSummary.getFileNum(), procFile.getName(), destFile.getName());
            conversionReport.addConvertedFile(thisFile);
        }

        private void ouputXMLreport() throws JAXBException,
                                             IOException, 
                                             FileNotFoundException {
            JAXBContext context = JAXBContext.newInstance(ConversionReport.class);
            StringWriter writer = new StringWriter();
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(conversionReport, writer);
            File reportFile = new File(conversionReport.reportFileName);
            FileOutputStream reportFileStream = null;
            
            try {
                reportFileStream = new FileOutputStream(reportFile);
                reportFileStream.write(writer.toString().getBytes());
            }
            finally {
                reportFileStream.close();
            }
        }
    }
}
