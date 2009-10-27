package eu.planets_project.services.migration.kakadu;

import eu.planets_project.services.datatypes.Parameter;

public class IntegerParameter extends NumberParameter<Integer> {

    private Integer maxValue;
    private Integer minValue;
    private String statusMessage;

    public IntegerParameter(Parameter parameter, String parameterPattern) {
        super(parameter, parameterPattern);
        minValue = Integer.MIN_VALUE;
        maxValue = Integer.MAX_VALUE;
    }

    @Override
    public boolean validate() {
        if (requestValue == null) {
            statusMessage = "Request value is not defined for parameter " +
                    "'" + getParameter().getName() + "'";
            return false;
        }
        try {
            Integer intVal = new Integer(Integer.parseInt(requestValue));

            if (intVal == null) {
                statusMessage = "Error creating parameter " +
                        "'" + getParameter().getName() + "': " +
                        "Unable to create Integer value";
            }
            if (intVal > maxValue || intVal < minValue) {
                log.info("here " + requestValue);
                return false;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    @Override
    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    @Override
    protected String getStatusMessage() {
        if (statusMessage == null) {
            return (valid ? "VALID" : "NOT VALID") + ": " +
                    "Value " + "'" + getParameter().getValue() + "' of " +
                    "parameter " + "'" + getParameter().getName() + "' is" +
                    (valid ? "" : " not") + " a valid Integer";
        } else {
            return statusMessage;
        }
    }
}
