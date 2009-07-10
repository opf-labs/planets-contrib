package eu.planets_project.services.migration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.planets_project.services.migration.graphicsmagick.GraphicsMagickMigrateTest;


@RunWith(Suite.class)
@Suite.SuiteClasses( { GraphicsMagickMigrateTest.class })
public class AllGraphicsMagickSuite {}
