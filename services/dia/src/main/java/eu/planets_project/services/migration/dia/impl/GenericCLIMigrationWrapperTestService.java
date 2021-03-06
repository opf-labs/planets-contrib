package eu.planets_project.services.migration.dia.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingType;

import org.w3c.dom.Document;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;

/**
 * DiaMigrationService testing service.
 * 
 * @author Thomas Skou Hansen &lt;tsh@statsbiblioteket.dk&gt;
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService(
		name = GenericCLIMigrationWrapperTestService.NAME, 
		serviceName = Migrate.NAME, 
		targetNamespace = PlanetsServices.NS, 
		endpointInterface = "eu.planets_project.services.migrate.Migrate")

@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public final class GenericCLIMigrationWrapperTestService implements Migrate,
        Serializable {

    /** The unique class id */
    private static final long serialVersionUID = 2764657361729506384L;

    /** The service name */
    static final String NAME = "GenericCLIMigrationWrapperTestService";

    private Logger log = Logger.getLogger(GenericCLIMigrationWrapperTestService.class.getName());

    /**
     * {@inheritDoc}
     * 
     * <b>Needs a parameter that specifies which configuration file the test
     * service should use.</b>
     * 
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject,
     *      java.net.URI, java.net.URI,
     *      eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate(final DigitalObject digitalObject,
            URI inputFormat, URI outputFormat, List<Parameter> parameters) {

        MigrateResult migrationResult;
        try {
            // Get the full file path to the configuration file to test from the
            // parameter list.
            String configResourceName = getConfigFileName(parameters);

            ResourceLocator configurationLocator = new ResourceLocator(
                    configResourceName);
            InputStream configurationStream = configurationLocator
                    .getResourceStream();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document configDocument = builder.parse(configurationStream);

            
            
            GenericCLIMigrationWrapper genericWrapper = new GenericCLIMigrationWrapper(
                    configDocument);
            migrationResult = genericWrapper.migrate(digitalObject,
                    inputFormat, outputFormat, parameters);
        } catch (Exception e) {
            log.severe("Migration failed for object with title '"
                            + digitalObject.getTitle()
                            + "' from input format URI: " + inputFormat
                            + " to output format URI: " + outputFormat+": "+e.getMessage());
            return new MigrateResult(null, null); // FIXME! Report failure in a
            // proper way.
        }

        return migrationResult;
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {

        try {
            ServiceDescription.Builder serviceDescriptionBuilder = new ServiceDescription.Builder(
                    NAME, Migrate.class.getCanonicalName());
            serviceDescriptionBuilder.classname(this.getClass()
                    .getCanonicalName());
            serviceDescriptionBuilder
                    .description("File migration service using Dia.");
            serviceDescriptionBuilder
                    .author("Bolette Ammitzbøll Jurik <bam@statsbiblioteket.dk>, Thomas Skou Hansen <tsh@statsbiblioteket.dk>");
            serviceDescriptionBuilder.furtherInfo(null);
            serviceDescriptionBuilder.inputFormats(getAllowedInputFormatURIs()
                    .toArray(new URI[] {}));
            serviceDescriptionBuilder.paths(MigrationPath.constructPaths(
                    getAllowedInputFormatURIs(), getAllowedOutputFormatURIs())
                    .toArray(new MigrationPath[] {}));
            // serviceDescriptionBuilder.furtherInfo(null);
            // serviceDescriptionBuilder.identifier(null);

            // serviceDescriptionBuilder.inputFormats(null);
            // serviceDescriptionBuilder.instructions(null);
            // serviceDescriptionBuilder.name(null);
            // serviceDescriptionBuilder.parameters(null);

            // serviceDescriptionBuilder.paths(new
            // GenericCLIMigrationWrapper(configfile).getMigrationPaths().getAsPlanetsPaths());
            // serviceDescriptionBuilder.properties(null);
            // serviceDescriptionBuilder.serviceProvider(null);
            // serviceDescriptionBuilder.tool(null);
            // serviceDescriptionBuilder.type(null);
            // serviceDescriptionBuilder.version(null);

            return serviceDescriptionBuilder.build();
        } catch (Exception e) {
            throw new Error("Failed building migration path information.", e);
        }
    }

    /**
     * Get the value of the &quot;configfile&quot; parameter.
     * 
     * @param parameters
     *            The parameter list passed to this migration service at
     *            invokation.
     * @return The string value of the &quot;configfile&quot; parameter.
     * @throws MigrationException
     *             if the &quot;configfile&quot; parameter was not defined in
     *             the parameter list.
     */
    private String getConfigFileName(List<Parameter> parameters)
            throws MigrationException {
        for (Parameter parameter : parameters) {
            if ("configfile".equals(parameter.getName())) {
                return parameter.getValue();
            }
        }
        throw new MigrationException(
                "No \"configfile\" parameter was specified. Please specify the full path to the configuration file to test.");
    }

    private Set<URI> getAllowedInputFormatURIs() throws URISyntaxException {

        final HashSet<URI> inputFormatURIs = new HashSet<URI>();

        // DIA URI
        inputFormatURIs.add(new URI("info:pronom/x-fmt/381"));

        // SVG URIs
        inputFormatURIs.add(new URI("info:pronom/fmt/91"));
        inputFormatURIs.add(new URI("info:pronom/fmt/92"));

        // PNG URIs
        inputFormatURIs.add(new URI("info:pronom/x-fmt/11"));
        inputFormatURIs.add(new URI("info:pronom/x-fmt/12"));
        inputFormatURIs.add(new URI("info:pronom/x-fmt/13"));

        // DXF URIs
        inputFormatURIs.add(new URI("info:pronom/fmt/63"));
        inputFormatURIs.add(new URI("info:pronom/fmt/64"));
        inputFormatURIs.add(new URI("info:pronom/fmt/65"));
        inputFormatURIs.add(new URI("info:pronom/fmt/66"));
        inputFormatURIs.add(new URI("info:pronom/fmt/67"));
        inputFormatURIs.add(new URI("info:pronom/fmt/68"));
        inputFormatURIs.add(new URI("info:pronom/fmt/69"));
        inputFormatURIs.add(new URI("info:pronom/fmt/70"));
        inputFormatURIs.add(new URI("info:pronom/fmt/71"));
        inputFormatURIs.add(new URI("info:pronom/fmt/72"));
        inputFormatURIs.add(new URI("info:pronom/fmt/73"));
        inputFormatURIs.add(new URI("info:pronom/fmt/74"));
        inputFormatURIs.add(new URI("info:pronom/fmt/75"));
        inputFormatURIs.add(new URI("info:pronom/fmt/76"));
        inputFormatURIs.add(new URI("info:pronom/fmt/77"));
        inputFormatURIs.add(new URI("info:pronom/fmt/78"));
        inputFormatURIs.add(new URI("info:pronom/fmt/79"));
        inputFormatURIs.add(new URI("info:pronom/fmt/80"));
        inputFormatURIs.add(new URI("info:pronom/fmt/81"));
        inputFormatURIs.add(new URI("info:pronom/fmt/82"));
        inputFormatURIs.add(new URI("info:pronom/fmt/83"));
        inputFormatURIs.add(new URI("info:pronom/fmt/84"));
        inputFormatURIs.add(new URI("info:pronom/fmt/85"));

        // TODO: FIG URIs are not provided by PRONOM. Add these when possible.

        // BMP URIs
        inputFormatURIs.add(new URI("info:pronom/x-fmt/25"));
        inputFormatURIs.add(new URI("info:pronom/fmt/114"));
        inputFormatURIs.add(new URI("info:pronom/fmt/115"));
        inputFormatURIs.add(new URI("info:pronom/fmt/116"));
        inputFormatURIs.add(new URI("info:pronom/fmt/117"));
        inputFormatURIs.add(new URI("info:pronom/fmt/118"));
        inputFormatURIs.add(new URI("info:pronom/fmt/119"));
        inputFormatURIs.add(new URI("info:pronom/x-fmt/270"));

        // GIF URIs
        inputFormatURIs.add(new URI("info:pronom/fmt/3"));
        inputFormatURIs.add(new URI("info:pronom/fmt/4"));

        // TODO: PNM URIs are not provided by PRONOM. Add these when possible.

        // RAS URI
        inputFormatURIs.add(new URI("info:pronom/x-fmt/184"));

        // TIF URIs
        inputFormatURIs.add(new URI("info:pronom/fmt/7"));
        inputFormatURIs.add(new URI("info:pronom/fmt/8"));
        inputFormatURIs.add(new URI("info:pronom/fmt/9"));
        inputFormatURIs.add(new URI("info:pronom/fmt/10"));
        inputFormatURIs.add(new URI("info:pronom/fmt/152"));
        inputFormatURIs.add(new URI("info:pronom/fmt/153"));
        inputFormatURIs.add(new URI("info:pronom/fmt/154"));
        inputFormatURIs.add(new URI("info:pronom/fmt/155"));
        inputFormatURIs.add(new URI("info:pronom/fmt/156"));
        inputFormatURIs.add(new URI("info:pronom/x-fmt/399"));
        inputFormatURIs.add(new URI("info:pronom/x-fmt/387"));
        inputFormatURIs.add(new URI("info:pronom/x-fmt/388"));

        return inputFormatURIs;
    }

    private Set<URI> getAllowedOutputFormatURIs() throws URISyntaxException {

        final HashSet<URI> outputFormatURIs = new HashSet<URI>();

        // CGM URI
        outputFormatURIs.add(new URI("info:pronom/x-fmt/142"));

        // DIA URI
        outputFormatURIs.add(new URI("info:pronom/x-fmt/381"));

        // TODO: SHAPE URI is not provided by PRONOM. Add when possible.

        // PNG URIs
        outputFormatURIs.add(new URI("info:pronom/fmt/11"));
        outputFormatURIs.add(new URI("info:pronom/fmt/12"));
        outputFormatURIs.add(new URI("info:pronom/fmt/13"));

        // DXF URIs
        outputFormatURIs.add(new URI("info:pronom/fmt/63"));
        outputFormatURIs.add(new URI("info:pronom/fmt/64"));
        outputFormatURIs.add(new URI("info:pronom/fmt/65"));
        outputFormatURIs.add(new URI("info:pronom/fmt/66"));
        outputFormatURIs.add(new URI("info:pronom/fmt/67"));
        outputFormatURIs.add(new URI("info:pronom/fmt/68"));
        outputFormatURIs.add(new URI("info:pronom/fmt/69"));
        outputFormatURIs.add(new URI("info:pronom/fmt/70"));
        outputFormatURIs.add(new URI("info:pronom/fmt/71"));
        outputFormatURIs.add(new URI("info:pronom/fmt/72"));
        outputFormatURIs.add(new URI("info:pronom/fmt/73"));
        outputFormatURIs.add(new URI("info:pronom/fmt/74"));
        outputFormatURIs.add(new URI("info:pronom/fmt/75"));
        outputFormatURIs.add(new URI("info:pronom/fmt/76"));
        outputFormatURIs.add(new URI("info:pronom/fmt/77"));
        outputFormatURIs.add(new URI("info:pronom/fmt/78"));
        outputFormatURIs.add(new URI("info:pronom/fmt/79"));
        outputFormatURIs.add(new URI("info:pronom/fmt/80"));
        outputFormatURIs.add(new URI("info:pronom/fmt/81"));
        outputFormatURIs.add(new URI("info:pronom/fmt/82"));
        outputFormatURIs.add(new URI("info:pronom/fmt/83"));
        outputFormatURIs.add(new URI("info:pronom/fmt/84"));
        outputFormatURIs.add(new URI("info:pronom/fmt/85"));

        // PLT URI
        outputFormatURIs.add(new URI("info:pronom/x-fmt/83"));

        // HPGL URI
        outputFormatURIs.add(new URI("info:pronom/fmt/293"));

        // EPS URIs
        outputFormatURIs.add(new URI("info:pronom/fmt/122"));
        outputFormatURIs.add(new URI("info:pronom/fmt/123"));
        outputFormatURIs.add(new URI("info:pronom/fmt/124"));

        // TODO: EPSI URI is not provided by PRONOM. Add when possible.

        // SVG URIs
        outputFormatURIs.add(new URI("info:pronom/fmt/91"));
        outputFormatURIs.add(new URI("info:pronom/fmt/92"));

        // SVGZ URI
        outputFormatURIs.add(new URI("info:pronom/x-fmt/109"));

        // TODO: MP URI is not provided by PRONOM. Add when possible.

        // TODO: TEX URI is not provided by PRONOM. Add when possible.

        // WPG URI
        outputFormatURIs.add(new URI("info:pronom/x-fmt/395"));

        // TODO: FIG URI is not provided by PRONOM. Add when possible.

        // TODO: CODE URI is not provided by PRONOM. Add when possible.

        return outputFormatURIs;
    }

}
