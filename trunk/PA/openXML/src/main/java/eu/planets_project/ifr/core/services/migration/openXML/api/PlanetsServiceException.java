/*
 * PlanetsServiceException.java
 *
 * Created on 03 July 2007, 09:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eu.planets_project.ifr.core.services.migration.openXML.api;

/**
 *
 * @author CFwilson
 */
public class PlanetsServiceException extends Exception {
    /**
     * Creates a new instance of PlanetsServiceException with a throwable cause and a message 
     */
    public PlanetsServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Creates a new instance of PlanetsServiceException with a throwable cause.
     * Message set to that of cause if the cause is not null
     */
    public PlanetsServiceException(Throwable cause) {
        super(cause);
    }
    /**
     * Creates a new instance of PlanetsServiceException with a message and no cause
     */
    public PlanetsServiceException(String message) {
        super(message);
    }
}
