/*
 *
 */
package eu.planets_project.services.migration.ps2pdf;

import eu.planets_project.ifr.core.techreg.api.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.api.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.*;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.Properties;


/**
 * The Pdf2PsMigration migrates PDF files to PS files.
 *
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService( name = Ps2PdfMigration.NAME ,
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate" )
public final class Ps2PdfMigration implements Migrate, Serializable {

    PlanetsLogger log = PlanetsLogger.getLogger(Ps2PdfMigration.class);


    private String ps2pdf_app_name;

    /** The service name */
    public static final String NAME = "Ps2PdfBasicMigration";

    private  Set<URI> inputformats;
    private Set<URI> outputformats;


    public Ps2PdfMigration() throws URISyntaxException, IOException {

        inputformats = new HashSet<URI>();
        outputformats = new HashSet<URI>();
        Properties props = new Properties();


        String strRsc = "ps2pdf.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL resourceURL = loader.getResource(strRsc);

        props.load(resourceURL.openStream());
        // config vars

        this.ps2pdf_app_name = props.getProperty("ps2pdf.app.name");
        String[] iformats = props.getProperty("ps2pdf.inputformats").split(",");
        for (String format: iformats){
            inputformats.add(new URI(format.trim()));
        }

        String[] oformats = props.getProperty("ps2pdf.outputformats").split(",");
        for (String format: oformats){
            outputformats.add(new URI(format.trim()));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameters)
     */
    public MigrateResult migrate( final DigitalObject digitalObject, URI inputFormat,
                                  URI outputFormat, Parameters parameters) {


        ServiceReport report = new ServiceReport();

        if (inputFormat != null && !inputformats.contains(inputFormat)){
            report.setError("Unknown inputformat set, use another tool that accepts this format");
            return new MigrateResult(null,report);
        }

        if (outputFormat == null || !outputformats.contains(outputFormat)){
            report.setError("Unknown outputformat set, use another tool that accepts this format");
            return new MigrateResult(null,report);
        }

        /*
        * We just return a new digital object with the same required arguments
        * as the given:
        */

        InputStream psfile = digitalObject.getContent().read();

        ProcessRunner runner = new ProcessRunner();
        List<String> command = new ArrayList<String>();
        command.add(ps2pdf_app_name);
        command.add("-");
        command.add("-");

        runner.setCommand(command);
        runner.setInputStream(psfile);
        runner.setCollection(true);
        runner.setOutputCollectionByteSize(-1);

        runner.run();
        int return_code = runner.getReturnCode();

        if (return_code != 0){
            report.setErrorState(return_code);
            report.setError(runner.getProcessOutputAsString()+"\n"+runner.getProcessErrorAsString());
            return new MigrateResult(null,report);
        }


        InputStream pdfFileStream = runner.getProcessOutput();
        byte[] pdfbytes = FileUtils.writeInputStreamToBinary(pdfFileStream);

        DigitalObject pdfFile = new DigitalObject.
                Builder(Content.byValue(pdfbytes))
                .format(outputFormat)
                .manifestationOf(digitalObject.getManifestationOf())
                .metadata((Metadata[]) digitalObject.getMetadata().toArray())
                .title(digitalObject.getTitle())
                .build();

        return new MigrateResult(pdfFile,report);

    }

    public ServiceDescription describe() {
        ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());

        builder.author("Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Simple ps2pdf wrapper for Ps (postscript) to PDF conversions.");
        FormatRegistry fm= FormatRegistryFactory.getFormatRegistry();

        MigrationPath[] paths = MigrationPath.constructPaths(inputformats,outputformats);
        builder.paths(paths);
        builder.version("0.1");

        ServiceDescription mds =builder.build();

        return mds;


    }
}