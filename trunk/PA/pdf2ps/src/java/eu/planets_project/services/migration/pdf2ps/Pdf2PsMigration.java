/*
 * 
 */
package eu.planets_project.services.migration.pdf2ps;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import java.io.Serializable;
import java.net.URI;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.ArrayList;
import java.util.List;



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
public final class Pdf2PsMigration implements Migrate, Serializable {
    
    PlanetsLogger log = PlanetsLogger.getLogger(Pdf2PsMigration.class);
    
    
    /** The pdf2ps installation dir */
    public String pdf2ps_install_dir;
    /** The pdf2ps application name */
    public String pdf2ps_app_name;
    /** The output file extension */
    public String pdf2ps_outfile_ext;
    private File tmpInFile;
    private File tmpOutFile;
    
    /***/
    static final String NAME = "Pdf2PsMigration";
    
    /***/
    private static final long serialVersionUID = 2127494848765937613L;

    /**
     * {@inheritDoc}
     * 
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameters)
     */
    public MigrateResult migrate( final DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, Parameters parameters) {
        
        Properties props = new Properties();
        try {
            
            String strRsc = "/eu/planets_project/services/migration/pdf2ps/pdf2ps.properties";
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
        
        byte[] binary = digitalObject.getContent().getValue();
        
        try {
            
            // write binary array to temporary file
            writeByteArrayToTmpFile( binary );

            // outfile name = infilename + ".ps" extension
            String outFileStr = tmpInFile.getAbsolutePath()+"."+this.pdf2ps_outfile_ext;
            // temporary outfile
            tmpOutFile = new File(outFileStr);
            
            InputStream inputStream = new ByteArrayInputStream(binary);
            
            ProcessRunner runner = new ProcessRunner();
            List<String> command = new ArrayList<String>();
            
            // setting up command
            command.add(this.pdf2ps_app_name);
            command.add(tmpInFile.getAbsolutePath());
            command.add(tmpOutFile.getAbsolutePath());

            runner.setCommand(command);
            runner.setInputStream(inputStream);
            
            runner.run();
            
            int return_code = runner.getReturnCode();

            if (return_code != 0){
                log.error("Pdf2ps conversion error code: " + Integer.toString(return_code));
            }
            
            // read byte array from temporary file
            if( tmpInFile.isFile() && tmpInFile.canRead() )
                binary = readByteArrayFromTmpFile(); 
            else
                log.error( "Error: Unable to read temporary file "
                        +tmpInFile.getPath()+tmpInFile.getName() );
            
        } catch(IOException e) {
            log.error( "IO Error:" + e.toString() );
        } finally {
            
        }
        DigitalObject newDO = null;
        
        ServiceReport report = new ServiceReport();
        
        newDO = new DigitalObject.Builder(Content.byValue(binary)).build();
        
        return new MigrateResult(newDO, report);
    }

    
    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
        ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());

        builder.author("Sven Schlarb <shsschlarb-planets@yahoo.de>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Simple pdf2ps wrapper for PDF to PS (postscript) conversions.");
        MigrationPath[] mPaths = new MigrationPath []{new MigrationPath(Format.extensionToURI("pdf"), Format.extensionToURI("ps"),null)};
        builder.paths(mPaths);
        builder.classname(this.getClass().getCanonicalName());
        builder.version("0.1");

        ServiceDescription mds =builder.build();
        
        return mds;
    }
    
    /* (non-Javadoc)
     */
    synchronized void writeByteArrayToTmpFile( byte[] binary ) throws IOException {
            tmpInFile = ByteArrayHelper.write(binary);
            if( tmpInFile.exists() )
                log.info("Temporary input file created: " + tmpInFile.getAbsolutePath());
            else
                log.error("Unable to create temp file");
    }
    
    /* (non-Javadoc)
     */
    synchronized byte[] readByteArrayFromTmpFile() throws IOException {
        byte[] binary = ByteArrayHelper.read(tmpOutFile);
        return binary;
    }


    private byte[] getByteArrayFromInputStream( InputStream is ) throws IOException
    {
        int bytesRead=0;
        int bytesToRead=1024;
        byte[] input = new byte[bytesToRead];
        while (bytesRead < bytesToRead) {
          int result = is.read(input, bytesRead, bytesToRead - bytesRead);
          if (result == -1) break;
          bytesRead += result;
        }
        return input;
    }

}
