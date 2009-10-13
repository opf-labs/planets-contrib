package eu.planets_project.services.migration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.planets_project.services.migration.soxservices.SoXTests;

/**
 * Suite to run all tests in the sox component.
 * 
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( { SoXTests.class})
public class AllSoxSuite {}
