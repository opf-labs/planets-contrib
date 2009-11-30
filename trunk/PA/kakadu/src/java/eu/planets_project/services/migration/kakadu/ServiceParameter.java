/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.planets_project.services.migration.kakadu;

import eu.planets_project.services.datatypes.Parameter;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author onbscs
 */
public abstract class ServiceParameter {

    private Parameter parameter;
    protected boolean valid;
    protected String paramPattern;

    protected String requestValue;

    protected String statusMessage;

    Logger log = Logger.getLogger(ServiceParameter.class.getName());

    protected String getParamPattern() {
        return paramPattern;
    }

    public void setParamPattern(String paramPattern) {
        this.paramPattern = paramPattern;
    }


    private ServiceParameter() {
    }

    public ServiceParameter(Parameter parameter,String paramPattern) {
        this.parameter = parameter;
        this.paramPattern = paramPattern;
        requestValue = "";
        valid = false;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public abstract void setRequestValue(String requestValue);
    
    protected abstract boolean validate();

    protected abstract String getStatusMessage();

    protected abstract List<String> getCommandListItems();

    public boolean isValid() {
        valid = validate();
        if(!valid)
            log.severe("Invalid parameter "+getParameter().getName()+" with " +
                    "value "+getParameter().getValue());
        return valid;
    }

}
