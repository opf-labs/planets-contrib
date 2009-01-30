/*
 *
 */
package eu.planets_project.services.migration.ps2pdf.impl;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import eu.planets_project.ifr.core.techreg.api.formats.FormatRegistryFactory;
import eu.planets_project.ifr.core.techreg.api.formats.FormatRegistry;

import java.net.URI;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.xml.ws.ResponseWrapper;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.*;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;
import eu.planets_project.services.utils.FileUtils;

import java.io.*;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;


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


    /**
     * {@inheritDoc}
     *
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameters)
     */
    public MigrateResult migrate( final DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, Parameters parameters) {

        Properties props = new Properties();
        try {

            String strRsc = "/eu/planets_project/services/migration/ps2pdf/ps2pdf.properties";
            props.load( this.getClass().getResourceAsStream(strRsc));
            // config vars


            this.ps2pdf_app_name = props.getProperty("pdf2ps.app.name");


        } catch( Exception e ) {
            // // config vars
            this.ps2pdf_app_name = "ps2pdf";
        }

        log.info("Using ps2pdf application name: "+this.ps2pdf_app_name);

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

        ServiceReport report = new ServiceReport();

        if (return_code != 0){
            report.setErrorState(return_code);
            report.setError(runner.getProcessOutputAsString()+"\n"+runner.getProcessErrorAsString());
            return new MigrateResult(null,report);
        }
        InputStream pdfFileStream = runner.getProcessOutput();
        byte[] pdfbytes = FileUtils.writeInputStreamToBinary(pdfFileStream);

        DigitalObject pdfFile = new DigitalObject.Builder(Content.byValue(pdfbytes)).build();
        return new MigrateResult(pdfFile,report);

    }

    public ServiceDescription describe() {
        ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());

        builder.author("Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Simple ps2pdf wrapper for Ps (postscript) to PDF conversions.");
        FormatRegistry fm= FormatRegistryFactory.getFormatRegistry();
        MigrationPath[] paths = MigrationPath.constructPaths(fm.getURIsForExtension("ps"),fm.getURIsForExtension("pdf"));
        builder.paths(paths);
        builder.version("0.1");

        ServiceDescription mds =builder.build();

        return mds;

    }
}