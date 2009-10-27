package eu.planets_project.services.migration.kakadu;

import eu.planets_project.services.datatypes.Parameter;

public class DoubleParameter extends NumberParameter<Double> {

    private Double maxValue;
    private Double minValue;

    public DoubleParameter(Parameter parameter, String parameterPattern) {
        super(parameter,parameterPattern);
        minValue = Double.MIN_VALUE;
        maxValue = Double.MAX_VALUE;
    }

    @Override
    public boolean validate() {
        try {
            Double doubleVal = Double.valueOf(requestValue);
            if(doubleVal > maxValue || doubleVal < minValue)
                return false;
        } catch(NumberFormatException ex) {
            return false;
        }
        return true;
    }

    @Override
    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    @Override
    protected String getStatusMessage() {
        return "Value " + "'" + getParameter().getValue() + "' of " +
                "parameter " + "'" +getParameter().getName() + "' is" +
                (valid ? "" : " not") + " a valid floating point number.";
    }
}
