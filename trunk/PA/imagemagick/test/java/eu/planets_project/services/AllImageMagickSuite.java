package eu.planets_project.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.planets_project.services.identification.AllImageMagickIdentificationSuite;
import eu.planets_project.services.migration.AllImageMagickMigrationsSuite;
import eu.planets_project.services.modification.AllImageMagickModificationSuite;


@RunWith(Suite.class)
@Suite.SuiteClasses( { AllImageMagickIdentificationSuite.class, AllImageMagickMigrationsSuite.class, AllImageMagickModificationSuite.class })
public class AllImageMagickSuite {}
