package eu.planets_project.ifr.contrib.ait.services;

import eu.planets_project.services.datatypes.Tool;

public class MimeUtilShared {
	
	/** TODO Move this to a shared place */
    public static Tool tool = null;
    static {
        	tool = Tool.create(null, "Mime-Util", "2.1.3", null,
            "https://sourceforge.net/projects/mime-util/");
    }

}
