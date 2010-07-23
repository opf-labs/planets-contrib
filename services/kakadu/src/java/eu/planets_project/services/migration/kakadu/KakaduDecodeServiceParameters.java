/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.planets_project.services.migration.kakadu;

import eu.planets_project.services.datatypes.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author onbscs
 */
public class KakaduDecodeServiceParameters {

    private static List<ServiceParameter> serviceParametersList = null;

    static {
        serviceParametersList = new ArrayList<ServiceParameter>();
        Parameter layersParam = new Parameter.Builder("layers", "1")
                .description("Maximum layers to decode.").build();
        serviceParametersList.add(new IntegerParameter(layersParam,"-layers %s"));
        Parameter rateParam = new Parameter.Builder("rate", "1")
                .description("Bits per pixel.").build();
        serviceParametersList.add(new DoubleParameter(rateParam,"-rate %s"));
        Parameter reduceParam = new Parameter.Builder("reduce", "1")
                .description("Discard layers.").build();
        serviceParametersList.add(new IntegerParameter(reduceParam,"-reduce %s"));
        Parameter fussyParam = new Parameter.Builder("fussy", "false")
                .description("check for conformance with standard.  Checks for " +
                "appearance of marker codes in the wrong places and so forth.").build();
        serviceParametersList.add(new BooleanParameter(fussyParam,"-fussy", "true", "false"));
        Parameter resilientParam = new Parameter.Builder("resilient", "1")
                .description("Check for conformance with standard. Recovers " +
                "from and/or conceals errors to the best of its ability.").build();
        serviceParametersList.add(new BooleanParameter(resilientParam,"-resilient", "true", "false"));
        Parameter regionParm = new Parameter.Builder("region", "64,64,64,64").description(
                "4 integers indicating <top>,<left>,<height>,<width> in this " +
                "exact order to extract a region of the image, numbers.").build();
        ComplexParameter regionServiceParam = new ComplexParameter(regionParm,"-region \"{%s,%s},{%s,%s}\"");
        regionServiceParam.add(new DoubleParameter(new Parameter.Builder("top", "64").
                description("top").build(),"%s"));
        regionServiceParam.add(new DoubleParameter(new Parameter.Builder("left", "64").
                description("left").build(),"%s"));
        regionServiceParam.add(new DoubleParameter(new Parameter.Builder("height", "64").
                description("height").build(),"%s"));
        regionServiceParam.add(new DoubleParameter(new Parameter.Builder("width", "64").
                description("width").build(),"%s"));
        serviceParametersList.add(regionServiceParam);
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
    public KakaduDecodeServiceParameters() {
    }

}
