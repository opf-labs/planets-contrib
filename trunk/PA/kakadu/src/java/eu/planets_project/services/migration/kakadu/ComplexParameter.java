package eu.planets_project.services.migration.kakadu;

import eu.planets_project.services.datatypes.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ComplexParameter extends ServiceParameter {

    List<ServiceParameter> paramList;

    public ComplexParameter(Parameter parameter, String paramPattern) {
        super(parameter, paramPattern);
        paramList = new ArrayList<ServiceParameter>();
    }

    public boolean add(ServiceParameter e) {
        boolean isAdded = paramList.add(e);
        return isAdded;
    }

    @Override
    public boolean validate() {
        if (paramList != null) {
            for (ServiceParameter servParm : paramList) {
                if (!servParm.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected String getStatusMessage() {
        if(valid)
            return "All constituent parts of the parameter are valid";
        else
            return "At least one of the parameter's constituent " +
                    "parts is not valid.";
    }

    @Override
    protected List<String> getCommandListItems() {
        String[] strArr = new String[paramList.size()];
        int i = 0;
        for(ServiceParameter servParm : paramList)
            strArr[i++] = servParm.requestValue;
        String result = String.format(paramPattern, (Object[])strArr);
        List<String> command = new ArrayList<String>();
        command.add(result);
        return command;
    }

    @Override
    public void setRequestValue(String requestValue) {
        StringTokenizer st = new StringTokenizer(requestValue,",");
        int index = 0;
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            ServiceParameter servParm = paramList.get(index);
            servParm.setRequestValue(token);
            index++;
        }
    }
}
