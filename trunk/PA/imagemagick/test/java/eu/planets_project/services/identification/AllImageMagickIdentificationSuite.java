package eu.planets_project.services.identification;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.planets_project.services.identification.imagemagick.ImageMagickIdentifyTests;

/**
 * Suite to run all tests in the ImageMagickIdentify component.
 * @author Peter Melms (peter.melms@uni-koeln.de)
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( { ImageMagickIdentifyTests.class })
public class AllImageMagickIdentificationSuite {}




