/*
 *
 */
package eu.planets_project.services.migration.ps2pdf.impl;

import eu.planets_project.ifr.core.techreg.api.formats.Format;

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
@WebService(/* name = Pdf2PsMigration.NAME ,*/
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate" )
public final class Ps2PdfMigration implements Migrate, Serializable {

    PlanetsLogger log = PlanetsLogger.getLogger(Ps2PdfMigration.class);




    /** The pdf2ps installation dir */
    public String pdf2ps_install_dir;
    /** The pdf2ps application name */
    public String pdf2ps_app_name;
    /** The output file extension */
    public String pdf2ps_outfile_ext;
    private File tmpInFile;
    private File tmpOutFile;

    /** The service name */
    public static final String NAME = "Ps2PdfBasicMigration";

    /***/
    private static final long serialVersionUID = 1878137433497934255L;




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
            this.pdf2ps_install_dir = props.getProperty("pdf2ps.install.dir");
            this.pdf2ps_app_name = props.getProperty("pdf2ps.app.name");
            this.pdf2ps_outfile_ext = props.getProperty("pdf2ps.outfile.ext");

        } catch( Exception e ) {
            // // config vars
            this.pdf2ps_install_dir  = "/usr/bin/";
            this.pdf2ps_app_name = "pdf2ps";
            this.pdf2ps_outfile_ext = "ps";
        }
        log.info("Using pdf2ps install directory: "+this.pdf2ps_install_dir);
        log.info("Using pdf2ps application name: "+this.pdf2ps_app_name);
        log.info("Using pdf2ps outfile extension: "+this.pdf2ps_outfile_ext);

        /*
         * We just return a new digital object with the same required arguments
         * as the given:
         */

        InputStream psfile = digitalObject.getContent().read();

        ProcessRunner runner = new ProcessRunner();
        List<String> command = new ArrayList<String>();
        command.add("ps2pdf");
        command.add("-");
        command.add("-");

        runner.setCommand(command);
        runner.setInputStream(psfile);
        runner.setCollection(true);
        runner.setOutputCollectionByteSize(-1);





        runner.run();
        int return_code = runner.getReturnCode();

        if (return_code != 0){


            throw new RuntimeException("Execution failed:" + runner.getProcessErrorAsString());
        }
        InputStream pdfFileStream = runner.getProcessOutput();
        byte[] pdfbytes = FileUtils.writeInputStreamToBinary(pdfFileStream);

        DigitalObject pdfFile = new DigitalObject.Builder(Content.byValue(pdfbytes)).build();

        ServiceReport report = new ServiceReport();
        return new MigrateResult(pdfFile,report);



    }

    @WebMethod(operationName = Migrate.NAME + "_" + "describe", action = PlanetsServices.NS + "/" + Migrate.NAME + "/" + "describe")
    @WebResult(name = Migrate.NAME + "Description", targetNamespace = PlanetsServices.NS + "/" + Migrate.NAME, partName = Migrate.NAME + "Description")
    @ResponseWrapper(className = "eu.planets_project.services.migrate." + Migrate.NAME + "DescribeResponse")
    public ServiceDescription describe() {
        ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());

        builder.author("Asger Blekinge-Rasmussen <abr@statsbiblioteket.dk>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Simple ps2pdf wrapper for Ps (postscript) to PDF conversions.");
        MigrationPath[] mPaths = new MigrationPath []{new MigrationPath(Format.extensionToURI("pdf"), Format.extensionToURI("ps"),null)};
        builder.paths(mPaths);
        builder.classname(this.getClass().getCanonicalName());
        builder.version("0.1");

        ServiceDescription mds =builder.build();

        return mds;

    }
}