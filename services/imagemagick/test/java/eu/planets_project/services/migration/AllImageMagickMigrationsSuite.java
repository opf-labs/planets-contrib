package eu.planets_project.services.migration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.planets_project.services.migration.imagemagick.Im4JavaImageMagickMigrateTests;
import eu.planets_project.services.migration.imagemagick.ImageMagickMigrateTests;

/**
 * Suite to run all tests in the ImageMagickMigrate component.
 * @author Peter Melms (peter.melms@uni-koeln.de)
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( { ImageMagickMigrateTests.class, Im4JavaImageMagickMigrateTests.class })
public class AllImageMagickMigrationsSuite {}




