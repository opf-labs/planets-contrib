package eu.planets_project.services.migration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.planets_project.services.migration.dioscuri.DioscuriArjMigrationTest;
import eu.planets_project.services.migration.dioscuri.DioscuriPnmToPngMigrationTest;

@RunWith (Suite.class)
@Suite.SuiteClasses({DioscuriArjMigrationTest.class, DioscuriPnmToPngMigrationTest.class})
public class AllDioscuriMigrationSuite {}
