/*
 * OpenXMLMigrationServiceRemoteInterface.java
 *
 * Created on 02 July 2007, 09:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eu.planets_project.ifr.core.services.migration.openXML.api;


/**
 *
 * @author CFwilson
 */

public interface OpenXMLMigrationServiceRemoteInterface {
    /**
     * @param toConvert_
     * @return the ref to the converted file
     * @throws PlanetsServiceException
     */
    public String convertFileRef(String toConvert_) throws PlanetsServiceException;
    /**
     * @param toConvert_
     * @return an array of refs to converted files
     * @throws PlanetsServiceException
     */
    public String[] convertFileRefs(String[] toConvert_) throws PlanetsServiceException;
}
