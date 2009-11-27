/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.planets_project.services.migration.jasper;

import eu.planets_project.ifr.core.services.migration.genericwrapper1.ServiceDescriptionFactory;
import eu.planets_project.services.datatypes.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 *
 * @author onbscs
 */
public class VarsizeListParameter extends ServiceParameter {

    private static Logger log = Logger.getLogger(VarsizeListParameter.class.getName());
    String commaSepExclFromValidation;
    PrimitiveParameterType type;
    List<String> valuesList;
    List<String> excludedValuesList;
    List<ServiceParameter> typedValuesList;

    public VarsizeListParameter(Parameter parameter, String paramPattern,
            PrimitiveParameterType type) {
        super(parameter, paramPattern);
        this.type = type;
    }

    public VarsizeListParameter(Parameter parameter, String paramPattern,
            PrimitiveParameterType type, String excludedVals) {
        this(parameter, paramPattern, type);
        StringTokenizer st = new StringTokenizer(excludedVals, ",");
        excludedValuesList = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            excludedValuesList.add(st.nextToken());
        }

    }

    @Override
    public void setRequestValue(String requestValue) {
        this.requestValue = requestValue;
        createTypedValuesList();
        log.info("requestValue is now: " + this.requestValue);
    }

    @Override
    protected boolean validate() {
        StringTokenizer st = new StringTokenizer(requestValue, ",");
        while (st.hasMoreTokens()) {
            String val = st.nextToken();
            if (!excludedValuesList.contains(val)) {
                switch (this.type) {
                    case BOOLEANPARM:
                        if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
                        } else {
                            log.severe("validate - Parameter '" + getParameter().getName() + "' with value '" + val + "' is not valid.");
                            return false;
                        }
                        return true;
                    case DOUBLEPARM:
                        try {
                            Double.parseDouble(val);
                        } catch (NumberFormatException ex) {
                            log.severe("validate - Parameter '" + getParameter().getName() + "' with value '" + val + "' is not valid.");
                            return false;
                        }
                        return true;
                    case INTEGERPARM:
                        try {
                            new Integer(Integer.parseInt(val));
                        } catch (NumberFormatException ex) {
                            log.severe("validate - Parameter '" + getParameter().getName() + "' with value '" + val + "' is not valid.");
                            return false;
                        }
                        return true;
                }
            }
        }
        return true;
    }

    @Override
    protected String getStatusMessage() {
        if (statusMessage != null) {
            return statusMessage;
        }
        if (valid) {
            return "All values of the list are valid";
        } else {
            return "At least one of the list's values is not valid.";
        }
    }

    @Override
    protected List<String> getCommandListItems() {
        List<String> command = new ArrayList<String>();
        String val = String.format(paramPattern, requestValue);
        StringTokenizer st = new StringTokenizer(val);
        while (st.hasMoreTokens()) {
            command.add(st.nextToken());
        }
        return command;
    }

    private void createTypedValuesList() {
        int i = 0;
        typedValuesList = new ArrayList<ServiceParameter>();
        valuesList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(requestValue, ",");
        log.info("requestValue is now: " + requestValue);
        while (st.hasMoreTokens()) {
            valuesList.add(st.nextToken());
            log.info("adding: " + valuesList.get(i++));
        }
        for (String val : valuesList) {
            if (!excludedValuesList.contains(val)) {
                Parameter parm = new Parameter("parm" + i, val);
                switch (this.type) {
                    case BOOLEANPARM:
                        this.typedValuesList.add(new BooleanParameter(parm, "%s", "yes", "no"));
                        break;
                    case DOUBLEPARM:
                        this.typedValuesList.add(new DoubleParameter(parm, "%s"));
                        break;
                    case INTEGERPARM:
                        this.typedValuesList.add(new IntegerParameter(parm, "%s"));
                        break;
                }
            }
        }
    }
}
