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

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
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
	/** The MDB file extension */
	public static final String sMDB_EXTENSION = ".mdb";
	/** The siard file extension */
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
	/**
	 * @see eu.planets_project.services.PlanetsService#describe()
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
	private static void appendError(ServiceReport sr, String sError)
	{
		System.err.println(sError);
		sr.setErrorState(ServiceReport.ERROR);
		/* append to previous error description */
		String s = sr.getError();
		if ((s != null) && (s.length() > 0))
			sError = s + "\n" + sError;
		sr.setError(sError);
	} /* appendError */
	
	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * add warning string to ServiceReport 
	 */
	@SuppressWarnings("unused")
  private static void appendWarn(ServiceReport sr, String sWarn)
	{
		System.out.println(sWarn);
		/* append to previous warning description */
		String s = sr.getWarn();
		if ((s != null) && (s.length() > 0))
			sWarn = s + "\n" + sWarn;
		sr.setWarn(sWarn);
	} /* appendWarn */
	
	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * add info string to ServiceReport 
	 */
	private static void appendInfo(ServiceReport sr, String sInfo)
	{
		System.out.println(sInfo);
		/* append to previous info description */
		String s = sr.getInfo();
		if ((s != null) && (s.length() > 0))
			sInfo = s + "\n" + sInfo;
		sr.setInfo(sInfo);
	} /* appendInfo */
	
	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * log error string and set it in ServiceReport 
	 */
	private void setError(ServiceReport sr, String sError)
	{
		log.error(sError);
		appendError(sr,sError);
	} /* setError */
	
	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * write the bytes to the given file.
	 */
	static void writeByteArrayToTmpFile(byte[] buffer, File file)
    throws IOException
	{
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
    bos.write(buffer);
    bos.flush();
    bos.close();
	} /* writeByteArrayToTmpFile */

	/*--------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * reads a temporary file into a byte array.
	 */
	static byte[] readByteArrayFromTmpFile(File file)
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
		sr.setErrorState(ServiceReport.ERROR);
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
        appendInfo(sr,"Using ConvMdb directory: "+sConvMdbDir);
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
        	appendInfo(sr,"ConvMdb conversion output:\n"+pr.getProcessOutputAsString());
    			/* signal success */
      		sr.setErrorState(ServiceReport.SUCCESS);
        }
        else
        	appendError(sr,"ConvMdb conversion error code: " + Integer.toString(iResult)+"\n"+
        			pr.getProcessErrorAsString());
      }
      else
      	appendError(sr,"Invalid value for "+sKEY_CONVMDB_DIR+" in "+sPROPERTIES_RESOURCE+":\n"+sConvMdbDir+" must terminate with '/'!");
    }
    catch(Exception e)
    {
    	appendError(sr,e.getClass().getName()+": "+e.getMessage());
    }
		return sr;
	} /* migrate */
	
	/*--------------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * 
	 * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameters)
	 */
	public MigrateResult migrate(
			final DigitalObject doInput,
	    URI inputFormat, URI outputFormat,
	    Parameters parameters)
	{
		DigitalObject doOutput = null;
		File fileInput = null;
		File fileOutput = null;
		ServiceReport sr = new ServiceReport();
		/* display banner */
		log.info("Mdb2Siard migrate starts");
		try
		{
			/* write to temporary file */
	    fileInput = File.createTempFile("planets", sMDB_EXTENSION);
	    /* make sure, it is at least deleted, when the Web Service is stopped */
	    fileInput.deleteOnExit();
			writeByteArrayToTmpFile(doInput.getContent().getValue(),fileInput);
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
			if (sr.error_state == ServiceReport.SUCCESS)
			  doOutput = new DigitalObject.Builder(Content.byValue(readByteArrayFromTmpFile(fileOutput))).build();
		}
		catch(Exception e)
		{
			setError(sr,e.getClass().getName()+": "+e.getMessage());
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
				doOutput = new DigitalObject.Builder(Content.byValue(new byte[0])).build();
		}
		/* display success */
		if (sr.error_state == ServiceReport.SUCCESS)
		  log.info("Mdb2Siard migrate succeeded");
		else
		  log.info("Mdb2Siard migrate failed!");
		return new MigrateResult(doOutput,sr);
	} /* migrate */

} /* class Mdb2SiardMigrate */
