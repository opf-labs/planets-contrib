package eu.planets_project.services.modification;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.planets_project.services.modification.imagemagick.ImageMagickCropTest;
import eu.planets_project.services.modification.imagemagick.ImageMagickRotateTest;


@RunWith(Suite.class)
@Suite.SuiteClasses( { ImageMagickCropTest.class, ImageMagickRotateTest.class })
public class AllImageMagickModificationSuite {}
