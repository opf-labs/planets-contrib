/**
 * 
 */
package eu.planets_project.services.jj2000;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import jj2000.j2k.decoder.CmdLnDecoder;
import jj2000.j2k.encoder.CmdLnEncoder;

import org.apache.log4j.Logger;

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
import eu.planets_project.services.utils.ServiceUtils;

/**
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
@WebService(
        name = JJ2000MigrateService.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate" )
public class JJ2000MigrateService implements Migrate {

    /** The service name */
    public static final String NAME = "JJ2000 Migration Service";
    
    Logger log = Logger.getLogger(JJ2000MigrateService.class);
    
    /** A reference to the web service context. */
    @Resource 
    WebServiceContext wsc;

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
        ServiceDescription.Builder sd = new ServiceDescription.Builder( NAME, Migrate.class.getCanonicalName());
        sd.description("A JPEG 2000 viewer service. Uses the JJ2000 reference implementation. See <a href=\"http://jj2000.epfl.ch/\">jj2000.epfl.ch</a> for copyright information.");
        sd.author("Andrew Jackson <Andrew.Jackson@bl.uk>");
        sd.classname(this.getClass().getCanonicalName());
        // Add a link to the JJ2000 homepage.
        sd.tool(URI.create("http://jj2000.epfl.ch/"));
        // Add links to this service:
        URI baseURI = JJ2000ViewerService.getBaseURIFromWSContext(wsc);
        if( baseURI != null ) {
            sd.furtherInfo( URI.create( ""+baseURI ) );
            sd.logo( URI.create( baseURI + "logos/jj2000_logo_150w.png") );
        }
        sd.version("0.1");
        
        // Migration Paths: List all combinations:
        List<MigrationPath> paths = new ArrayList<MigrationPath>();
        paths.add( new MigrationPath( Format.extensionToURI("PPM"), Format.extensionToURI("JP2"), null) );
        paths.add( new MigrationPath( Format.extensionToURI("JP2"), Format.extensionToURI("PPM"), null) );
        sd.paths(paths.toArray(new MigrationPath[]{}));
        
        return sd.build();
    }
    
    /**
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameters)
     */
    public MigrateResult migrate(DigitalObject dob, URI inputFormat,
            URI outputFormat, Parameters parameters) {
        
        // Can only cope if the object is 'simple':
        if( dob.getContent() == null ) {
            return this.returnWithErrorMessage("The Content of the DigitalObject should not be NULL.", null);
        }
        
        // Store DO in a temporary file.
        Format inf = new Format(inputFormat);
        File inFile = FileUtils.writeInputStreamToTmpFile(dob.getContent().read(), "jj2000-conv-in", "."+ inf.getExtensions().iterator().next() );
        inFile.deleteOnExit();
        
        // Output file:
        Format outf = new Format(outputFormat);
        File outFile = null;
        try {
            outFile = File.createTempFile("jj2000-conv-out", "." + outf.getExtensions().iterator().next() );
        } catch (IOException e) {
            e.printStackTrace();
            return this.returnWithErrorMessage("Could not open output file. "+e, null);
        }
        outFile.deleteOnExit();


        // Invoking as command-line, but internally.  This means we can't get System.out/err!
        String[] argv = new String[6];
        argv[0] = "-i"; argv[1] = inFile.getAbsolutePath();
        argv[2] = "-o"; argv[3] = outFile.getAbsolutePath();
        argv[4] = "-verbose"; argv[5] = "on";

        // Invoke the 
        if( "jp2".equalsIgnoreCase( outf.getExtensions().iterator().next() ) ) {
            CmdLnEncoder.main(argv);
        } else {
            CmdLnDecoder.main(argv);
        }
        
        // Grab the file and pass it back.
        byte[] bytes = FileUtils.readFileIntoByteArray(outFile);
        ServiceReport rep = new ServiceReport();
        rep.setErrorState(0);
        DigitalObject ndo = new DigitalObject.Builder(Content.byValue(bytes)).build();
        return new MigrateResult( ndo, rep );
    }

    /**
     * 
     * @param message
     * @return
     */
    private MigrateResult returnWithErrorMessage(String message, Exception e ) {
        if( e == null ) {
            return new MigrateResult(null, ServiceUtils.createErrorReport(message));
        } else {
            return new MigrateResult(null, ServiceUtils.createExceptionErrorReport(message, e));
        }
    }

}
