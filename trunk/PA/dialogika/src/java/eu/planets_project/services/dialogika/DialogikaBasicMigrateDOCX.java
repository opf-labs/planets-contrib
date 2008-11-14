/**
 * 
 */
package eu.planets_project.services.dialogika;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import de.dialogika.planets.planets_webservice.genericmigration.ArrayOfParameter;
import de.dialogika.planets.planets_webservice.genericmigration.GenericMigration;
import de.dialogika.planets.planets_webservice.genericmigration.MigrateOneBinaryResult;
import de.dialogika.planets.planets_webservice.genericmigration.Parameter;
import eu.planets_project.services.migrate.BasicMigrateOneBinary;

/**
 * A simple service to wrap the Dialogika services, to fill the gap until the Planets Migrate interface stabilises.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
@SuppressWarnings("deprecation")
public class DialogikaBasicMigrateDOCX implements BasicMigrateOneBinary {

    /* (non-Javadoc)
     * @see eu.planets_project.services.migrate.BasicMigrateOneBinary#basicMigrateOneBinary(byte[])
     */
    public byte[] basicMigrateOneBinary(byte[] binary) {
        GenericMigration mob;
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
        MigrateOneBinaryResult res = mob.getGenericMigrationSoap12().migrateOneBinary(binary, null);
        return res.getBinary();
    }

}
