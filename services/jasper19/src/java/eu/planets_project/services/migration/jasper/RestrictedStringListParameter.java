package eu.planets_project.services.migration.jasper;

import eu.planets_project.services.datatypes.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author onbscs
 */
public class RestrictedStringListParameter extends ServiceParameter {

    List<String> stringList;

    public RestrictedStringListParameter(Parameter parameter, String paramPattern) {
        super(parameter, paramPattern);
        stringList = new ArrayList<String>();
    }

    public boolean add(String e) {
        return stringList.add(e);
    }

    @Override
    public boolean validate() {
        if(stringList.contains(requestValue))
            return true;
        else
            return false;
    }

    @Override
    protected String getStatusMessage() {
        return "Value " + "'" + getParameter().getValue() + "' of " +
                "parameter " + "'" +getParameter().getName() + "' is" +
                (valid ? "" : " not") + " in the restricted value list.";
    }

    @Override
    protected List<String> getCommandListItems() {
        List<String> command = new ArrayList<String>();
        String result = String.format(paramPattern, requestValue);
        command.add(result);
        return command;
    }

    @Override
    public void setRequestValue(String requestValue) {
        this.requestValue = requestValue;
    }
}
