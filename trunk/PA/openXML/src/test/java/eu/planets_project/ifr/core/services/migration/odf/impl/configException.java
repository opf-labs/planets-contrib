/*
 * configException.java
 *
 * Created on 03 July 2007, 08:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eu.planets_project.ifr.core.services.migration.odf.impl;

/**
 *
 * @author CFwilson
 */
public class configException extends Exception {
	private static final long serialVersionUID = 6219329033530800283L;

	// Constructor taking a message and a cause
    configException(String s, Throwable t) {
        super(s, t);
    }
    
    // Constructor taking just a message, cause is null
    configException(String s) {
        super(s);
    }
    
    // Constructor with just a cause, message = message of cause if cause is not null
    configException(Throwable t) {
        super(t);
    }
}
