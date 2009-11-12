/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.planets_project.services.migration.jasper;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import eu.planets_project.services.datatypes.Parameter;

/**
 *
 * @author onbscs
 */
public abstract class NumberParameter<T> extends ServiceParameter {

    public NumberParameter(Parameter parameter, String paramPattern) {
        super(parameter, paramPattern);
    }

    public abstract boolean validate();

    public abstract void setMaxValue(T maxValue);
    public abstract void setMinValue(T minValue);

    @Override
    protected List<String> getCommandListItems() {
       List<String> command = new ArrayList<String>();
       String val = String.format(paramPattern, requestValue);
       StringTokenizer st = new StringTokenizer(val);
       while(st.hasMoreTokens())
            command.add(st.nextToken());
       return command;
    }

    @Override
    public void setRequestValue(String requestValue) {
        this.requestValue = requestValue;
    }
}
