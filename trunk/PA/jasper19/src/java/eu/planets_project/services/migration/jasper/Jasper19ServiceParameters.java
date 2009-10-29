/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.planets_project.services.migration.jasper;

import eu.planets_project.services.migration.*;
import eu.planets_project.services.datatypes.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author onbscs
 */
public class Jasper19ServiceParameters {

    private static List<ServiceParameter> serviceParametersList = null;

    static {

        serviceParametersList = new ArrayList<ServiceParameter>();
        Parameter rateParam = new Parameter.Builder("rate", "1.0")
                .description(
                "Floating point number indicating the quality.").build();
        serviceParametersList.add(new VarsizeListParameter(rateParam,"rate=%s",
                PrimitiveParameterType.DOUBLEPARM, "-"));
    }

    public static List<Parameter> getParameterList() {
        List<Parameter> paramList = new ArrayList<Parameter>();
        for(ServiceParameter servParm : serviceParametersList)
        {
            paramList.add(servParm.getParameter());
        }
        return paramList;
    }

    public ServiceParameter getParameter(String paramName) {
        for(ServiceParameter servParm : serviceParametersList) {
            Parameter parm = servParm.getParameter();
            if(parm.getName().equalsIgnoreCase(paramName))
                return servParm;
        }
        return null;
    }
    public Jasper19ServiceParameters() {
    }

}
