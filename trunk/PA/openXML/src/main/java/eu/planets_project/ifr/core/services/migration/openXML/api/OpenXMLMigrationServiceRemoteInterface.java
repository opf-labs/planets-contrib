/*
 * OpenXMLMigrationServiceRemoteInterface.java
 *
 * Created on 02 July 2007, 09:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eu.planets_project.ifr.core.services.migration.openXML.api;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author CFwilson
 */

public interface OpenXMLMigrationServiceRemoteInterface {
    public String convertFileRef(String toConvert_) throws PlanetsServiceException;
    public String[] convertFileRefs(String[] toConvert_) throws PlanetsServiceException;
}
