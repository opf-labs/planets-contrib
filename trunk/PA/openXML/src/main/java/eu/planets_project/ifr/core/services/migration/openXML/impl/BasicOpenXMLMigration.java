/*
 * migrationService.java
 *
 * Created on 27 June 2007, 15:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eu.planets_project.ifr.core.services.migration.openXML.impl;

import eu.planets_project.ifr.core.common.api.PlanetsException;
import eu.planets_project.ifr.core.common.logging.PlanetsLogger;
import eu.planets_project.ifr.core.common.services.PlanetsServices;
import eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.ifr.core.services.migration.openXML.api.PlanetsServiceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;

/**
 *
 * @author CFWilson
 */
@Stateless
@Local(BasicMigrateOneBinary.class)
@LocalBinding(jndiBinding = "planets-project.eu/OOXMLService/BasicMigrateOneBinary/BasicOpenXMLMigration")
@Remote(BasicMigrateOneBinary.class)
@RemoteBinding(jndiBinding = "planets-project.eu/OOXMLService/BasicMigrateOneBinary/BasicOpenXMLMigration")

// Web Service Annotations, copied in from the inherited interface.
@WebService(
        name = "BasicOpenXMLMigration", 
        serviceName = BasicMigrateOneBinary.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary" )
public class BasicOpenXMLMigration implements BasicMigrateOneBinary {
    
    PlanetsLogger log = PlanetsLogger.getLogger(BasicOpenXMLMigration.class);

    /* (non-Javadoc)
     * @see eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary#basicMigrateOneBinary(byte[])
     */
    public byte[] basicMigrateOneBinary ( 
            byte[] binary ) throws PlanetsException {
        // Serialise the file:
        File input;
        try {
            input = File.createTempFile("input",".doc");
            input.deleteOnExit();
        } catch ( IOException e ) {
            log.error("Could not create temporary files! "+e);
            throw new PlanetsException(e);
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(input);
            fos.write(binary);
            fos.flush();
            fos.close();
        } catch( FileNotFoundException e ) {
            log.error("Creating "+input.getAbsolutePath()+" :: " +e);
            throw new PlanetsException(e);
        } catch( IOException e ) {
            log.error("Creating "+input.getAbsolutePath()+" :: " +e);
            throw new PlanetsException(e);
        }

        // Do the conversion:
        String outputFile;
        try {
            OpenXMLMigration ooxm = new OpenXMLMigration();
            String inputFile = input.getCanonicalPath().toString();
            log.info("Attepting to convert: "+inputFile);
            outputFile = ooxm.convertFileRef(inputFile);
        } catch (IOException e) {
            log.error("Migration: IOException :: " +e);
            throw new PlanetsException(e);
        } catch (PlanetsServiceException e) {
            log.error("Migration: Planets Service Exception :: " +e);
            throw new PlanetsException(e);
        }
        
        // Get the result as a byte array:
        byte[] result = null;
        try {
            result = getByteArrayFromFile(new File(outputFile));
        } catch( IOException e ) {
            log.error("Returning "+outputFile+" :: " +e);
            throw new PlanetsException(e);
        }
        
        // Delete the temporaries:
        input.delete();
        
        // Return the result.
        return result; 
    }

    private static byte[] getByteArrayFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // throw new
            // IllegalArgumentException("getBytesFromFile@JpgToTiffConverter::
            // The file is too large (i.e. larger than 2 GB!");
            System.out.println("Datei ist zu gross (e.g. groesser als 2GB)!");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

}
