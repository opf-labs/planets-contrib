/**
 * 
 */
package eu.planets_project.services.dialogika;

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
        GenericMigration mob = new GenericMigration();
        ArrayOfParameter parameters = new ArrayOfParameter();
        Parameter par = new Parameter();
        par.setName("outType");
        par.setValue("DOCX");
        parameters.getParameter().add(par);
        MigrateOneBinaryResult res = mob.getGenericMigrationSoap().migrateOneBinary(binary, parameters);
        return res.getBinary();
    }

}
