/*== Mdb2SiardMigrate.java ===========================================
PLANETS migration service converting .mdb files to .siard files.
Version     : $Id$
Application : PLANETS migration services
Description : Mdb2SiardMigrate implements the PLANETS 
              (v. www.planets-project.eu) Integration Framework's (IF)
              Migrate interface.
------------------------------------------------------------------------
Copyright  : Swiss Federal Archives, Berne, Switzerland 
             (pending PLANETS copyright agreements)
Created    : 05.12.2008, Hartwig Thomas, Enter AG, Zurich
Sponsor    : Swiss Federal Archives, Berne, Switzerland
======================================================================*/

package eu.planets_project.services.migration.mdb2siard;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.*;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Properties;

/*====================================================================*/
/**
 * Mdb2SiardMigrate implements Migrate to migrate .mdb files to .siard 
 * files in a Web Service. 
 * 
 * @author Hartwig Thomas <hartwig.thomas@enterag.ch>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@WebService(name = Mdb2SiardMigrate.NAME, serviceName = Migrate.NAME, targetNamespace = PlanetsServices.NS, endpointInterface = "eu.planets_project.services.migrate.Migrate")
public final class Mdb2SiardMigrate implements Migrate, Serializable
{
	/** constants */
	private static int iBUFFER_SIZE = 8192;
	private static int iEOS = -1;
	public static final String sMDB_EXTENSION = ".mdb";  
	public static final String sSIARD_EXTENSION = ".siard";  
	private static final String sPROPERTIES_RESOURCE = "/eu/planets_project/services/migration/mdb2siard/mdb2siard.properties";
	private static final String sKEY_CONVMDB_DIR ="convmdb.dir"; 

	/***/
	static final String NAME = "Mdb2SiardMigrate";

	/***/
	private static final long serialVersionUID = 2127494848765937613L;

	/** data members */
	PlanetsLogger log = PlanetsLogger.getLogger(Mdb2SiardMigrate.class);
	
	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * @see eu.planets_project.ifr.core.common.services.migrate.MigrateOneDigitalObject#describe()
	 */
	public ServiceDescription describe()
	{
		ServiceDescription mds =
	    new ServiceDescription.Builder(NAME, Migrate.class.getName())
        .author("Hartwig Thomas <hartwig.thomas@enterag.ch>")
        .classname(this.getClass().getCanonicalName())
        .description("PLANETS IF wrapper for .mdb (MS Access) to .siard (Swiss Federal Archives) migration.")
        .build();
		return mds;
	} /* describe */

	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * add error string to ServiceReport 
	 */
	private static ServiceReport appendError(ServiceReport sr, String sError)
	{
		System.err.println(sError);
		/* append to previous error description */
		String s = sr.getMessage();
		if (sr.getType()==Type.ERROR)
			sError = s + "\n" + sError;
		return new ServiceReport(Type.ERROR, Status.TOOL_ERROR, sError);
	} /* appendError */
	
	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * add warning string to ServiceReport 
	 */
	@SuppressWarnings("unused")
  private static ServiceReport appendWarn(ServiceReport sr, String sWarn)
	{
		System.out.println(sWarn);
		/* append to previous warning description */
		String s = sr.getMessage();
		if (sr.getType()==Type.WARN)
			sWarn = s + "\n" + sWarn;
		return new ServiceReport(Type.WARN, Status.SUCCESS, sWarn);
	} /* appendWarn */
	
	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * add info string to ServiceReport 
	 */
	private static ServiceReport appendInfo(ServiceReport sr, String sInfo)
	{
		System.out.println(sInfo);
		/* append to previous info description */
		String s = sr.getMessage();
		if (sr.getType()==Type.INFO)
			sInfo = s + "\n" + sInfo;
		return new ServiceReport(Type.INFO, Status.SUCCESS, sInfo);
	} /* appendInfo */
	
	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * write the bytes to the given file.
	 */
	static void writeByteArrayToFile(byte[] buffer, File file)
    throws IOException
	{
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
    bos.write(buffer);
    bos.flush();
    bos.close();
	} /* writeByteArrayToFile */

	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * write the bytes to the given file.
	 */
	static void writeByteContentToFile(Content content, File file)
    throws IOException
	{
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
    InputStream is = content.read();
    byte[] buffer = new byte[iBUFFER_SIZE];
    for (int iRead = is.read(buffer); iRead != iEOS; iRead = is.read(buffer))
    	bos.write(buffer, 0, iRead);
    bos.flush();
    bos.close();
	} /* writeByteContentToFile */

	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * reads a file into a byte array.
	 */
	static byte[] readByteArrayFromFile(File file)
	  throws IOException, IllegalArgumentException
	{
		byte[] buffer = new byte[0]; // avoid returning null
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
    if (file.length() <= Integer.MAX_VALUE)
    {
    	buffer = new byte[(int) file.length()];
    	bis.read(buffer);
    	bis.close();
    }
    else
    {
      throw new IllegalArgumentException("The file at " + file.getAbsolutePath()
            + " is too large to be represented as a byte array!");
    }
    return buffer;
	} /* readByteArrayFromFile */

	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * execute the conversion on files in the file system.
	 */
	static ServiceReport migrate(File fileInput, File fileOutput, ServiceReport sr)
	{
    Properties props = new Properties();
    try
    {
  		/* get properties */
      props.load( Mdb2SiardMigrate.class.getResourceAsStream(sPROPERTIES_RESOURCE));
      /* configuration variable */
      String sConvMdbDir = props.getProperty(sKEY_CONVMDB_DIR);
      /* check its correct termination */
      if (sConvMdbDir.endsWith("/"))
      {
        /* report in ServiceReport */
        sr = appendInfo(sr,"Using ConvMdb directory: "+sConvMdbDir);
    		/* ODBC DSN must be as unique as the temporary file name to support concurrent
    		 * execution of several service calls (and must be removed in the end) */
        String sOdbcDsn = fileInput.getName();
    		/* set up the command line */
  			sOdbcDsn = sOdbcDsn.substring(0,sOdbcDsn.lastIndexOf("."));
        /* run the command */
        List<String> listCommand = new ArrayList<String>();
        listCommand.add("cscript");
        listCommand.add(sConvMdbDir + "convmdb.js");
        listCommand.add("/t:0");
        listCommand.add("/dsn:"+sOdbcDsn);
        listCommand.add(fileInput.getAbsolutePath());
        listCommand.add(fileOutput.getAbsolutePath());
        ProcessRunner pr = new ProcessRunner(listCommand);
        pr.run();
    		/* analyze result */
        int iResult = pr.getReturnCode();
        if (iResult == 0)
        {
        	sr = appendInfo(sr,"ConvMdb conversion output:\n"+pr.getProcessOutputAsString());
        }
        else
        	sr = appendError(sr,"ConvMdb conversion error code: " + Integer.toString(iResult)+"\n"+
        			pr.getProcessErrorAsString());
      }
      else
      	sr = appendError(sr,"Invalid value for "+sKEY_CONVMDB_DIR+" in "+sPROPERTIES_RESOURCE+":\n"+sConvMdbDir+" must terminate with '/'!");
    }
    catch(Exception e)
    {
    	sr = appendError(sr,e.getClass().getName()+": "+e.getMessage());
    	e.printStackTrace();
    }
		return sr;
	} /* migrate */
	
	/*--------------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * 
	 * @see eu.planets_project.ifr.core.common.services.migrate.MigrateOneDigitalObject#migrate(eu.planets_project.ifr.core.common.services.datatypes.DigitalObject)
	 */
	public MigrateResult migrate(
			final DigitalObject doInput,
	    URI inputFormat, URI outputFormat,
	    List<Parameter> parameters)
	{
		/* empty doOutput in case of error ... */
		DigitalObject doOutput = new DigitalObject.Builder(ImmutableContent.byValue(new byte[] {})).build();
		File fileInput = null;
		File fileOutput = null;
		ServiceReport sr = new ServiceReport(Type.INFO, Status.SUCCESS, "OK");
		/* display banner */
		log.info("Mdb2Siard migrate starts");
		try
		{
			/* write to temporary file */
	    fileInput = File.createTempFile("planets", sMDB_EXTENSION);
	    /* make sure, it is at least deleted, when the Web Service is stopped */
	    fileInput.deleteOnExit();
	        FileUtils.writeInputStreamToFile(doInput.getContent().read(), fileInput);
			/* output file has same unique file name with different extension */
			String sInputFile = fileInput.getAbsolutePath();
			String sOutputFile = sInputFile.substring(0,sInputFile.lastIndexOf("."))+sSIARD_EXTENSION;
			fileOutput = new File(sOutputFile);
			if (fileOutput.exists())
				fileOutput.delete();
			/* make sure it is at least deleted when Web Service is stopped */
			fileOutput.deleteOnExit();
			/* convert files, noting results in the service report */
			sr = migrate(fileInput, fileOutput, sr);
	    /* read do from temporary file */
			if (sr.getStatus() == Status.SUCCESS)
			  doOutput = new DigitalObject.Builder(ImmutableContent.byValue(readByteArrayFromFile(fileOutput))).build();
		}
		catch(Exception e)
		{
			sr = appendError(sr,e.getClass().getName()+": "+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			/* clean up temporary files in any case */
			if ((fileInput != null) && (fileInput.exists()))
				fileInput.delete();
			if ((fileOutput != null) && (fileOutput.exists()))
				fileOutput.delete();
			/* if no output was generated, create a zero length byte array */
			if (doOutput == null)
				doOutput = new DigitalObject.Builder(ImmutableContent.byValue(new byte[0])).build();
		}
		/* display success */
		if (sr.getStatus() == Status.SUCCESS)
		  log.info("Mdb2Siard migrate succeeded");
		else
		  log.info("Mdb2Siard migrate failed!");
		MigrateResult mr = new MigrateResult(doOutput,sr);
		return mr;
	} /* migrate */

} /* class Mdb2SiardMigrate */
