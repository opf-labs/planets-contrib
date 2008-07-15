/**
 * 
 */
package eu.planets_project.services.migration.xenaservices;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import eu.planets_project.ifr.core.common.api.PlanetsException;
import eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;

/**
 * Normalisation of Image files to Xena files
 */
@WebService()
public class ImageToXena implements BasicMigrateOneBinary {

    public synchronized byte[] basicMigrateOneBinary(byte[] binary) throws PlanetsException {
        try {
            Xena xena = new Xena();
            xena.loadPlugin("au.gov.naa.digipres.xena.plugin.image.ImagePlugin");
            File inputFile = new File("XenaNormalisation");
            FileOutputStream xenaInputStream = new FileOutputStream(inputFile);
            xenaInputStream.write(binary);
            xenaInputStream.close();
            XenaInputSource xis = new XenaInputSource(inputFile);
            File destDir = new File("output");
            if (!destDir.mkdir()) {
                if (destDir.exists() && destDir.isDirectory()) {
                    Logger.getLogger(ImageToXena.class.getName()).log(Level.INFO, "Using existing destination folder.");
                } else {
                    Logger.getLogger(ImageToXena.class.getName()).log(Level.INFO, "Error creating destination folder. Exiting.");
                }
            }
            NormaliserResults results = xena.normalise(xis, destDir);
            File outputFile = new File(destDir.getAbsolutePath() + File.separator + results.toString());
            FileInputStream fileInputStream = new FileInputStream(outputFile);
            byte[] returnData = new byte[(int) outputFile.length()];
            fileInputStream.read(returnData);
            fileInputStream.close();
            outputFile.delete();

            return returnData;

        } catch (Exception ex) {
            Logger.getLogger(ImageToXena.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
