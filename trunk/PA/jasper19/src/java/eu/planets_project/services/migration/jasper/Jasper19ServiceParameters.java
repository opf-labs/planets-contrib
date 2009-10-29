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
                "Comma separated positive floating point numbers indicating " +
                "the compression rate. For example: 1.0 means irreversible " +
                "compression to 1 bit/sample and 1.0,0.5,0.25 means " +
                "irreversible compression to a 3 layer code-stream (3 " +
                "embedded bit-rates) and  -,1,0.5,0.25 togehter with " +
                "reversible=yes means reversible (lossless) compression with " +
                "a progressive lossy to lossless code-stream having 4 " +
                "layers (Note the use of the dash (-) to specify that the " +
                "final layer should include all remaining compressed bits, " +
                "not included in previous layers. Specifying a large " +
                "bit-rate for one of the layers does not have exactly the " +
                "same effect and may leave the code-stream not quite " +
                "lossless.) , and 0.5 means irreversible colour compression " +
                "(with visual weights) to 0.5 bit/pixel. In this case, the " +
                "post-compression rate-distortion optimization algorithm is " +
                "used to discard coding passes until the rate target is met. " +
                "This is done for each quality layer.  Although more coding " +
                "passes are processed by the block coder than are ultimately " +
                "required, Kakadu still uses a predictive algorithm to " +
                "reduce the number of wasted coding passes, so that the " +
                "processing speed is essentially independent of Qstep so " +
                "long as it is very small.").build();
        serviceParametersList.add(new VarsizeListParameter(rateParam,"-rate %s",
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
