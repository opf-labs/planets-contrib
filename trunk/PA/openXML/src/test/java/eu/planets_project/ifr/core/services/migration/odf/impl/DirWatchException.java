/*
 * DirWatchException.java
 *
 * Created on 19 June 2007, 20:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eu.planets_project.ifr.core.services.migration.odf.impl;

/**
 *
 * @author CFWilson
 */
    /**
     * DirWatcherException : Exception class for app defined errors
     */
    public class DirWatchException extends Exception {
        DirWatchException(String s, Throwable t) {
            super(s, t);
        }
        DirWatchException(String s) {
            super(s);
        }
        DirWatchException(Throwable t) {
            super(t);
        }
    }
