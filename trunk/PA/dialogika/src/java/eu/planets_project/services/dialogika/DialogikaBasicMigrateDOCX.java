/**
 * 
 */
package eu.planets_project.services.dialogika;

import java.net.MalformedURLException;
import java.net.URL;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dialogika.planets.planets_webservice.genericmigration.ArrayOfParameter;
import de.dialogika.planets.planets_webservice.genericmigration.GenericMigration;
import de.dialogika.planets.planets_webservice.genericmigration.GenericMigrationSoap;
import de.dialogika.planets.planets_webservice.genericmigration.MigrateOneBinaryResult;
//import de.dialogika.planets.planets_webservice.genericmigration.Parameter;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;

/**
 * A simple service to wrap the Dialogika services, to fill the gap until the Planets Migrate interface stabilises.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
@WebService(
        name = DialogikaBasicMigrateDOCX.NAME, 
        serviceName = BasicMigrateOneBinary.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.BasicMigrateOneBinary" )
@SuppressWarnings("deprecation")
public class DialogikaBasicMigrateDOCX implements BasicMigrateOneBinary {
    
    public static final String NAME = "DialogikaBasicMigrateDOCX";


    private static Log log = LogFactory.getLog(DialogikaBasicMigrateDOCX.class);

    /* (non-Javadoc)
     * @see eu.planets_project.services.migrate.BasicMigrateOneBinary#basicMigrateOneBinary(byte[])
     */
    public byte[] basicMigrateOneBinary(byte[] binary) {
        GenericMigration mob;
        log.info("Initialising GenericMigration...");
        try {
            mob = new GenericMigration(
                    new URL( "http://www.dialogika.de/planets/planets.webservice/GenericMigration.asmx?outtype=docx&WSDL"), 
                    new QName("http://www.dialogika.de/Planets/planets.webservice/GenericMigration", "GenericMigration") 
                    );
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        log.info("Initialised GenericMigration.");
        ArrayOfParameter pars = new ArrayOfParameter();
        GenericMigrationSoap gms = mob.getGenericMigrationSoap();
        log.info("Got SOAP implementation.  Invoking...");
        MigrateOneBinaryResult res = gms.migrateOneBinary(binary, pars);
        log.info("Got result. Returning.");
        return res.getBinary();
    }

}
