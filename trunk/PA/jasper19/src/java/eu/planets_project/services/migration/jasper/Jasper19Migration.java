/*
 * 
 */
package eu.planets_project.services.migration.jasper;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.ProcessRunner;

/**
 * The Jasper19Migration migrates JPEG files to JP2 files and vice versa.
 * 
 * @author Sven Schlarb <shsschlarb-planets@yahoo.de>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService(name = Jasper19Migration.NAME,
serviceName = Migrate.NAME,
targetNamespace = PlanetsServices.NS,
endpointInterface = "eu.planets_project.services.migrate.Migrate")
public final class Jasper19Migration implements Migrate {

    private static Logger log = Logger.getLogger(Jasper19Migration.class.getName());
    /** The dvi ps installation dir */
    public String jasper19_install_dir;
    /** The jasper19 application name */
    public String jasper19_app_name;
    /** The output file extension */
    //public String jasper19_outfile_ext;
    private File tmpInFile;
    private File tmpOutFile;
    String inputFmtExt = null;
    String outputFmtExt = null;
    /***/
    static final String NAME = "Jasper19Migration";
    List<String> inputFormats = null;
    List<String> outputFormats = null;
    HashMap<String, String> formatMapping = null;
    List<Parameter> serviceParametersList;
    List<Parameter> requestParametersList;
    StringBuffer serviceMessage = null;
    Jasper19ServiceParameters jasperServiceParameters = null;
    /***/
    private static final long serialVersionUID = 2127494848765937613L;

    private void init() {

        // input formats
        inputFormats = new ArrayList<String>();
        inputFormats.add("bmp"); // Windows BMP
        inputFormats.add("jp2"); // JPEG-2000 JP2
        inputFormats.add("jpc"); // JPEG-2000 Code Stream
        inputFormats.add("jpg"); // JPEG
        inputFormats.add("pgx"); // PGX
        inputFormats.add("pnm"); // PNM/PGM/PPM
        inputFormats.add("pgm"); // PNM/PGM/PPM
        inputFormats.add("ppm"); // PNM/PGM/PPM
        inputFormats.add("mif"); // My Image Format
        inputFormats.add("ras"); // Sun Rasterformat
        inputFormats.add("tif"); // Sun Rasterformat

        // output formats and associated output parameters
        outputFormats = new ArrayList<String>();
        outputFormats.add("bmp"); // Windows BMP
        outputFormats.add("jp2"); // JPEG-2000 JP2
        outputFormats.add("jpc"); // JPEG-2000 Code Stream
        outputFormats.add("jpg"); // JPEG
        outputFormats.add("pgx"); // PGX
        outputFormats.add("pnm"); // PNM/PGM/PPM
        outputFormats.add("pgm"); // PNM/PGM/PPM
        outputFormats.add("ppm"); // PNM/PGM/PPM
        outputFormats.add("mif"); // My Image Format
        outputFormats.add("ras"); // Sun Rasterformat
        outputFormats.add("tif"); // Sun Rasterformat

        // Disambiguation of extensions, e.g. {"JPG","JPEG"} to {"JPEG"}
        // FIXIT This should be supported by the FormatRegistryImpl class, but
        // it does not provide the complete set at the moment.
        formatMapping = new HashMap<String, String>();
        formatMapping.put("jpeg", "jpg");

        jasperServiceParameters = new Jasper19ServiceParameters();

        serviceMessage = new StringBuffer();
    }

    /**
     * Initialize the parameters list for all migration file formats. Every
     * parameter has a default value which is overridden where requested
     * from the user (parameters contains the parameters passed to the
     * service by the user).
     */
    private void initParameters() {
        serviceParametersList = Jasper19ServiceParameters.getParameterList();
    }

    /**
     * {@inheritDoc}
     * 
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate(final DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, List<Parameter> parameters) {
            requestParametersList = parameters;
        Properties props = new Properties();

        try {

            String strRsc = "/eu/planets_project/services/migration/jasper19/jasper19.properties";
            props.load(this.getClass().getResourceAsStream(strRsc));
            // config vars
            this.jasper19_install_dir = props.getProperty("jasper19.install.dir");
            this.jasper19_app_name = props.getProperty("jasper19.app.name");
            //this.jasper19_outfile_ext = props.getProperty("jasper19.outfile.ext");

        } catch (Exception e) {
            // // config vars
            this.jasper19_install_dir = "/usr/local/bin/"; // This Uses jasper-1.701.0.GEO!!!!
            this.jasper19_app_name = "jasper";
        }
	
	init();
	

	String m1 = "Using jasper19 install directory: " + this.jasper19_install_dir + ". ";;
        log.info(m1); serviceMessage.append(m1+"\n");
	String m2 = "Using jasper19 application name: " + this.jasper19_app_name + ". ";
        log.info(m2); serviceMessage.append(m2+"\n");
        
        initParameters();
        getExtensions(inputFormat, outputFormat);

        /*
         * We just return a new digital object with the same required arguments
         * as the given:
         */
        byte[] binary = null;
        InputStream inputStream = digitalObject.getContent().read();

        DigitalObject newDO = null;
        ServiceReport report = null;

        // write input stream to temporary file
        tmpInFile = FileUtils.writeInputStreamToTmpFile(inputStream, "planets", inputFmtExt);
        if (!(tmpInFile.exists() && tmpInFile.isFile() && tmpInFile.canRead())) {
            String msg = "Error: Unable to create temporary input file " + tmpInFile.getAbsolutePath();
            log.severe(msg);
            report = new ServiceReport(Type.ERROR, Status.TOOL_ERROR, msg);
            return new MigrateResult(null, report);
        }
	
	String m3 = "Temporary input file created: " + tmpInFile.getAbsolutePath() + ". ";
        log.info(m3); serviceMessage.append(m3+"\n");
	String m31 = "Input file size: " + tmpInFile.length() + " bytes. ";
        log.info(m31); serviceMessage.append(m31+"\n");

        // outfile name
        String outFileStr = tmpInFile.getAbsolutePath() + "." + outputFmtExt;
	String m4 = "Output file name: " + outFileStr + ". ";
        log.info(m4); serviceMessage.append(m4+" \n");

        // run command
        ProcessRunner runner = new ProcessRunner();
        List<String> command = new ArrayList<String>();
        // setting up command
        // Example: jasper --input testin.jpeg --input-format jpg --output-format jp2 --output testout.jp2
        // Example (short version): jasper -f testin.jpg -t jpg -F testin.jp2 -T jp2
        command.add(this.jasper19_app_name);
        command.add("--input");
        command.add(tmpInFile.getAbsolutePath());
        command.add("--input-format");
        command.add(inputFmtExt);
        command.add("--output");
        command.add(outFileStr);
        command.add("--output-format");
        command.add(outputFmtExt);
        if( requestParametersList != null ) {
        for (Parameter requestParm : requestParametersList) {
            ServiceParameter servParm = jasperServiceParameters.getParameter(requestParm.getName());
            if (servParm != null) {
                servParm.setRequestValue(requestParm.getValue());
                if (servParm.isValid()) {
                    command.addAll(servParm.getCommandListItems());
                } else {
                    this.serviceMessage.append(servParm.getStatusMessage());
                }
            } else {
                this.serviceMessage.append("Parameter skipped: Service does not support parameter '" + requestParm.getName() + "'. \n");
            }
        }
        }
        runner.setCommand(command);
        runner.setInputStream(inputStream);
	
	String m5 = "Command: " + command.toString() + ". ";
        log.info(m5); serviceMessage.append(m5+"\n");

	long startMillis = System.currentTimeMillis();
        runner.run();
        int return_code = runner.getReturnCode();
	long endMillis = System.currentTimeMillis();
        if (return_code != 0) {
            String errMsg = "Jasper conversion error code: " + Integer.toString(return_code) +
                    ". Jasper error message: "+runner.getProcessErrorAsString();
            log.severe(errMsg);
            String msg = null;
            if(serviceMessage.toString().equals("")) {
                msg = errMsg;
            } else {
                msg = "Service message(s): "+serviceMessage.toString()+". "+errMsg;
            }
            report = new ServiceReport(Type.ERROR, Status.TOOL_ERROR, msg);
            return new MigrateResult(null, report);
        }

        tmpOutFile = new File(outFileStr);
        // read byte array from temporary file
        if (tmpOutFile.isFile() && tmpOutFile.canRead()) {
            	binary = FileUtils.readFileIntoByteArray(tmpOutFile);
		String m60 = "Processing time: " + (endMillis-startMillis) + " milliseconds. ";
		log.info(m60); serviceMessage.append(m60+"\n");
		String m6 = "Output file: " + tmpOutFile.getAbsolutePath() + " created successfully. ";
		log.info(m6); serviceMessage.append(m6+"\n");
		String m61 = "Output file size: " + tmpOutFile.length() + " bytes. ";
        	log.info(m61); serviceMessage.append(m61+"\n");
        } else {
            String msg = "Error: Unable to read temporary file \"" + tmpOutFile.getAbsolutePath()+"\"";
            log.severe(msg);
            report = new ServiceReport(Type.ERROR, Status.TOOL_ERROR, msg);
            return new MigrateResult(null, report);
        }

        report = new ServiceReport(Type.INFO, Status.SUCCESS, "Success: "+serviceMessage);
        newDO = new DigitalObject.Builder(Content.byValue(binary)).build();

        return new MigrateResult(newDO, report);
    }

    /**
     * Get value of one parameter.
     *
     * @param ext Extension for which we retrieve the parameter list.
     * @return Paramter string
     */
    private String getRequestParameterValue(String parmName) {
        Iterator<Parameter> itr = this.requestParametersList.iterator();
        String paramValue = null;
        while (itr.hasNext()) {
            Parameter param = (Parameter) itr.next();
            if (param.getName() != null && param.getName().equals(parmName)) {
                paramValue = param.getValue();
                return paramValue;
            }
        }
        return null;
    }

    private void getExtensions(URI inputFormat, URI outputFormat) {
        if (inputFormat != null && outputFormat != null) {
            inputFmtExt = getFormatExt(inputFormat, false);
            outputFmtExt = getFormatExt(outputFormat, true);
        }
    }

    /**
     * Gets one extension from a set of possible extensions for the incoming
     * request planets URI (e.g. planets:fmt/ext/jpeg) which matches with
     * one format of the set of jasper's supported input/output formats. If
     * isOutput is false, it checks against the gimp input formats ArrayList,
     * otherwise it checks against the gimp output formats HashMap.
     *
     * @param formatUri Planets URI (e.g. planets:fmt/ext/jpeg)
     * @param isOutput Is the format an input or an output format
     * @return Format extension (e.g. "JPEG")
     */
    private String getFormatExt(URI formatUri, boolean isOutput) {
        String fmtStr = null;
        // status variable which indicates if an input/out format has been found
        // while iterating over possible matches
        boolean fmtFound = false;
        // Extensions which correspond to the format
        // planets:fmt/ext/jpg -> { "JPEG", "JPG" }
        // or can be found in the list of supported formats
        Set<String> reqInputFormatExts = FormatRegistryFactory.getFormatRegistry().getExtensions(formatUri);
        Iterator<String> itrReq = reqInputFormatExts.iterator();
        // Iterate either over input formats ArrayList or over output formats
        // HasMap
        Iterator<String> itrJasper = (isOutput) ? outputFormats.iterator() : inputFormats.iterator();
        // Iterate over possible extensions that correspond to the request
        // planets uri.
        while (itrReq.hasNext()) {
            // Iterate over the different extensions of the planets:fmt/ext/jpg
            // format URI, note that the relation of Planets-format-URI to
            // extensions is 1 : n.
            String reqFmtExt = normalizeExt((String) itrReq.next());
            while (itrJasper.hasNext()) {
                // Iterate over the formats that jasper offers either as input or
                // as output format.
                // See input formats in the this.init() method to see the
                // jasper input/output formats offered by this service.
                String gimpFmtStr = (String) itrJasper.next();
                if (reqFmtExt.equalsIgnoreCase(gimpFmtStr)) {
                    // select the gimp supported format
                    fmtStr = gimpFmtStr;
                    fmtFound = true;
                    break;
                }
                if (fmtFound) {
                    break;
                }
            }
        }
        return fmtStr;
    }

    /**
     * Disambiguation (e.g. JPG -> JPEG) according to the formatMapping
     * datas structure defined in this class.
     *
     * @param ext
     * @return Uppercase disambiguized extension string
     */
    private String normalizeExt(String ext) {
        String normExt = ext.toUpperCase();
        return ((formatMapping.containsKey(normExt)) ? (String) formatMapping.get(normExt) : normExt);
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
        ServiceDescription.Builder builder = new ServiceDescription.Builder(NAME, Migrate.class.getName());
        init();
        initParameters();
        List<Parameter> parameters = serviceParametersList;
        builder.author("Sven Schlarb <shsschlarb-planets@yahoo.de>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Simple service for Jasper Transcoder Version 1.900.1 for JPG to JP2 (JPEG2000)" +
                "and, vice versa, JP2 to JPG conversion." +
                "Jasper is a file format converter specialized in JPEG-2000 encoding" +
                "Copyright (c) 1999-2000 Image Power, Inc. and the University of" +
                "British Columbia." +
                "All rights reserved." +
                "For more information about this software, please visit the following" +
                "web sites/pages:" +
                "http://www.ece.uvic.ca/~mdadams/jasper" +
                "http://www.jpeg.org/software");
        FormatRegistry format = FormatRegistryFactory.getFormatRegistry();
        MigrationPath[] mPaths = new MigrationPath[]{
            new MigrationPath(format.createExtensionUri("tif"), format.createExtensionUri("jp2"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("jpg"), format.createExtensionUri("jp2"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("bmp"), format.createExtensionUri("jp2"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("pgx"), format.createExtensionUri("jp2"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("pnm"), format.createExtensionUri("jp2"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("pgm"), format.createExtensionUri("jp2"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("ppm"), format.createExtensionUri("jp2"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("mif"), format.createExtensionUri("jp2"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("ras"), format.createExtensionUri("jp2"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("jpg"), format.createExtensionUri("jpc"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("bmp"), format.createExtensionUri("jpc"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("pgx"), format.createExtensionUri("jpc"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("pnm"), format.createExtensionUri("jpc"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("mif"), format.createExtensionUri("jpc"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("ras"), format.createExtensionUri("jpc"), Jasper19ServiceParameters.getParameterList()),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("bmp"), null),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("jpg"), null),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("pgx"), null),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("pnm"), null),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("pgm"), null),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("ppm"), null),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("mif"), null),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("ras"), null),
            new MigrationPath(format.createExtensionUri("jp2"), format.createExtensionUri("tif"), null),
            new MigrationPath(format.createExtensionUri("jpc"), format.createExtensionUri("bmp"), null),
            new MigrationPath(format.createExtensionUri("jpc"), format.createExtensionUri("jpg"), null),
            new MigrationPath(format.createExtensionUri("jpc"), format.createExtensionUri("pgx"), null),
            new MigrationPath(format.createExtensionUri("jpc"), format.createExtensionUri("pnm"), null),
            new MigrationPath(format.createExtensionUri("jpc"), format.createExtensionUri("pgm"), null),
            new MigrationPath(format.createExtensionUri("jpc"), format.createExtensionUri("ppm"), null),
            new MigrationPath(format.createExtensionUri("jpc"), format.createExtensionUri("mif"), null),
            new MigrationPath(format.createExtensionUri("jpc"), format.createExtensionUri("ras"), null)};
        builder.paths(mPaths);
        builder.classname(this.getClass().getCanonicalName());
        builder.version("0.1");

        ServiceDescription mds = builder.build();

        return mds;
    }
}
