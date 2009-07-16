/*== Mdb2SiardMigrate.java ===========================================
JUnit test of class Mdb2SiardMigrate.
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

public class Mdb2SiardMigrateTester
{
	 private static String sINPUT_FILE = "PA/mdb2siard/test/testfiles/testin.mdb";
//	private static String sINPUT_FILE = "PA/mdb2siard/test/testfiles/newspaper.tif";
	
  // private static String sOUTPUT_FILE = "PA/mdb2siard/test/testfiles/testout.siard";
  private static String sOUTPUT_FILE = "PA/mdb2siard/test/testfiles/testout.siard";
  /* The location of this service when deployed. */
  private static String sWSDL_LOC = "/pserv-pa-mdb2siard/Mdb2SiardMigrate?wsdl";
  
  private static FormatRegistry format = FormatRegistryFactory.getFormatRegistry();

  /* A holder for the object to be tested */
  static Migrate dom = null;

	/*--------------------------------------------------------------------*/
	@BeforeClass
	public static void setUp() throws Exception
	{
//		 this is configured by the system properties
    	System.setProperty("pserv.test.context", "server");
    	System.setProperty("pserv.test.host", "localhost");
     	 System.setProperty("pserv.test.port", "8080");
		 
    dom = ServiceCreator.createTestService(Migrate.QNAME, 
    		  Mdb2SiardMigrate.class, sWSDL_LOC);
	} /* setUp */

	/*--------------------------------------------------------------------*/
	@After
	public void tearDown() throws Exception
	{
	} /* tearDown */

	/*--------------------------------------------------------------------*/
	@Test
	public void testDescribe()
	{
    ServiceDescription desc = dom.describe();
    System.out.println("Received service description: " + desc.toXmlFormatted());
    assertTrue("The ServiceDescription should not be NULL.", desc != null );
	} /* testDescribe */

	
	
//	/*--------------------------------------------------------------------*/
//	@Test
//	public void testWriteByteArrayToTmpFile()
//	{
//    File fileOutput = new File(sOUTPUT_FILE);
//    if (fileOutput.exists())
//        FileUtils.delete(fileOutput);
//    try
//    {
//      byte[] buffer = new byte[] { 0, 1, 2, 3};
//      Mdb2SiardMigrate.writeByteArrayToFile(buffer,fileOutput);
//  		assertTrue(fileOutput.exists());
//  		assertTrue(fileOutput.length() == buffer.length);
//    }
//    catch(IOException ie)
//    {
//    	fail(ie.getClass().getName()+": "+ie.getMessage());
//    }
//	} /* testWriteByteArrayToTmpFile */
//
//	/*--------------------------------------------------------------------*/
	
	
	
	
//	@Test
//	public void testReadByteArrayFromTmpFile()
//	{
//    File fileInput = new File(sINPUT_FILE);
//    try
//    {
//      byte[] buffer = Mdb2SiardMigrate.readByteArrayFromFile(fileInput);
//  		assertTrue(buffer.length == fileInput.length());
//  		assertTrue(buffer[0] == 0);
//  		assertTrue(buffer[1] == 1);
//  		assertTrue(buffer[2] == 0);
//  		assertTrue(buffer[3] == 0);
//    }
//    catch(IOException ie)
//    {
//    	fail(ie.getClass().getName()+": "+ie.getMessage());
//    }
//	} /* testReadByteArrayFromTmpFile */
//
//	/*--------------------------------------------------------------------*/
//	@Test
//	public void testMigrateFileFileServiceReport()
//	{
//    File fileOutput = new File(sOUTPUT_FILE);
//    if (fileOutput.exists())
//        FileUtils.delete(fileOutput);
//    File fileInput = new File(sINPUT_FILE);
//		ServiceReport sr = new ServiceReport(Type.INFO, Status.SUCCESS, "OK");
//		sr = Mdb2SiardMigrate.migrate(fileInput, fileOutput, sr);
//		assertTrue((sr.getStatus() == Status.TOOL_ERROR) || fileOutput.exists());
//	} /* testMigrateFileFileServiceReport */
	
	
	
	

	/*--------------------------------------------------------------------*/
	@Test
	public void testMigrateDigitalObjectURIURIParameters()
	{
    try
    {
      File fileOutput = new File(sOUTPUT_FILE);
      if (fileOutput.exists())
          FileUtils.delete(fileOutput);
      File fileInput = new File(sINPUT_FILE);
      DigitalObject doInput = new DigitalObject.Builder(
      		Content.byReference(FileUtils.getUrlFromFile(fileInput))).build();
      MigrateResult mr = dom.migrate(doInput, format.createExtensionUri("mdb"), format.createExtensionUri("siard"), null);
      DigitalObject doOutput = mr.getDigitalObject();
      assertTrue("Resulting digital object is null.", doOutput != null);
      if (mr.getReport().getStatus() != Status.TOOL_ERROR)
      {
        FileUtils.writeInputStreamToFile(doOutput.getContent().read(), fileOutput);
        if (mr.getReport().getType() == Type.WARN)
        	System.out.println("Warning: "+mr.getReport().getMessage());
        if (mr.getReport().getType() == Type.INFO)
        	System.out.println("Information: "+mr.getReport().getMessage());
      }
      else
      	System.out.println("Error: "+mr.getReport().getMessage());
  		assertTrue((mr.getReport().getStatus() == Status.TOOL_ERROR) || fileOutput.exists());
    }
    catch (Exception e) 
    {
      fail(e.getClass().getName()+": "+e.getMessage());
	  }
	} /* testMigrateDigitalObjectURIURIParameters */

} /* Mdb2SiardMigrateTester */
