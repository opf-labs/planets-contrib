/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.planets_project.services.migration.xenaservices;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

/**
 *
 * @author onbpeg
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService(/* name = XenaOOMigration.NAME ,*/serviceName = Migrate.NAME,
targetNamespace = PlanetsServices.NS,
endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class XenaOOMigration implements Migrate, Serializable {

    PlanetsLogger log = PlanetsLogger.getLogger(XenaOOMigration.class);
    private static final long serialVersionUID = 3952711367037433051L;
    static final String NAME = "XenaOOMigration";

    public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat, URI outputFormat, Parameters parameters) {
        InputStream inputStream = digitalObject.getContent().read();
        XenaMigrations xena = new XenaMigrations();
        //ODF -> PDF
        xena.setOoffice_import_filter(XenaMigrations.IMPORT_FILTER_NONE);
        xena.setOoffice_export_filter(XenaMigrations.EXPORT_FILTER_PDF);
        
        //DOC -> ODF
//        xena.setOoffice_import_filter(XenaMigrations.IMPORT_FILTER_DOC);
//        xena.setOoffice_export_filter(XenaMigrations.EXPORT_FILTER_NONE);

        byte[] binary = xena.basicMigrateOneBinary(FileUtils.writeInputStreamToBinary(inputStream));

        DigitalObject newDO = null;

        ServiceReport report = new ServiceReport();

        newDO = new DigitalObject.Builder(Content.byValue(binary)).build();

        return new MigrateResult(newDO, report);

    }

    public ServiceDescription describe() {
                ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());

        builder.author("Sven Schlarb <shsschlarb-planets@yahoo.de>, Georg Petz <georg.petz@onb.ac.at");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("XENA OO Wrapper");
        MigrationPath[] mPaths = new MigrationPath []{
            new MigrationPath(Format.extensionToURI("odt"), Format.extensionToURI("pdf"),null),
            new MigrationPath(Format.extensionToURI("odp"), Format.extensionToURI("pdf"),null),
            new MigrationPath(Format.extensionToURI("ods"), Format.extensionToURI("pdf"),null),
            new MigrationPath(Format.extensionToURI("odg"), Format.extensionToURI("pdf"),null),
            new MigrationPath(Format.extensionToURI("odb"), Format.extensionToURI("pdf"),null)};
        builder.paths(mPaths);
        builder.classname(this.getClass().getCanonicalName());
        builder.version("0.1");

        ServiceDescription mds =builder.build();
        
        return mds;
    }
}
