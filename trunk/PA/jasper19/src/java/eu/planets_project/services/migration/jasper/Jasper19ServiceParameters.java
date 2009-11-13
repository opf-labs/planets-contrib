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
                .description("Floating point number indicating the quality. " +
                "The argument is a positive real number. " +
                "Since a rate of one corresponds to no compression, one " +
                "should never need to explicitly specify a rate greater " +
                "than one. By default, the target rate is considered to " +
                "be infinite.").build();
        serviceParametersList.add(new DoubleParameter(rateParam, "-O rate=%s"));

        Parameter numrlvlsParam = new Parameter.Builder("numrlvls", "6")
                .description("Positive integer greater than or equal to 1 which sets the number of resolution levels. The default value is 6.").build();
        serviceParametersList.add(new IntegerParameter(numrlvlsParam, "-O numrlvls=%s"));

        Parameter imgareatlxParam = new Parameter.Builder("imgareatlx", "10")
                .description("Set the x-coordinate of the top-left corner of the image area to x.").build();
        serviceParametersList.add(new IntegerParameter(imgareatlxParam, "-O imgareatlx=%s"));


        Parameter imgareatlyParam = new Parameter.Builder("imgareatly", "10")
                .description("Set the y-coordinate of the top-left corner of the image area to this value.").build();
        serviceParametersList.add(new IntegerParameter(imgareatlyParam, "-O imgareatly=%s"));

        Parameter tilegrdtlxParam = new Parameter.Builder("tilegrdtlx", "10")
                .description("Set the x-coordinate of the top-left corner of the tiling grid to this value.").build();
        serviceParametersList.add(new IntegerParameter(tilegrdtlxParam, "-O tilegrdtlx=%s"));

        Parameter tilegrdtlyParam = new Parameter.Builder("tilegrdtly", "10")
                .description("Set the y-coordinate of the top-left corner of the tiling grid to this value.").build();
        serviceParametersList.add(new IntegerParameter(tilegrdtlyParam, "-O tilegrdtly=%s"));

        Parameter tilewidthParam = new Parameter.Builder("tilewidth", "10")
                .description("Set the nominal tile width to this value.").build();
        serviceParametersList.add(new IntegerParameter(tilewidthParam, "-O tilewidth=%s"));

        Parameter tileheightParam = new Parameter.Builder("tileheight", "10")
                .description("Set the nominal tile height to this value.").build();
        serviceParametersList.add(new IntegerParameter(tileheightParam, "-O tileheight=%s"));

        Parameter prcwidthParam = new Parameter.Builder("prcwidth", "32768")
                .description("Set the precinct width to this value. The argument w must be an integer power of two. The default value is 32768.").build();
        serviceParametersList.add(new IntegerParameter(prcwidthParam, "-O prcwidth=%s"));

        Parameter prcheightParam = new Parameter.Builder("prcheight", "32768")
                .description("Set the precinct height to this value. The argument h must be an integer power of two. The default value is 32768.").build();
        serviceParametersList.add(new IntegerParameter(prcheightParam, "-O prcheight=%s"));

        Parameter cdblkwidthParam = new Parameter.Builder("cdblkwidth", "64")
                .description("Set the cdblk width to this value. The argument w must be an integer power of two. The default value is 32768.").build();
        serviceParametersList.add(new IntegerParameter(cdblkwidthParam, "-O cdblkwidth=%s"));

        Parameter cdblkheightParam = new Parameter.Builder("cdblkheight", "64")
                .description("Set the cdblk height to this value. The argument h must be an integer power of two. The default value is 32768.").build();
        serviceParametersList.add(new IntegerParameter(cdblkheightParam, "-O cdblkheight=%s"));

//        Parameter prgParam = new Parameter.Builder("prg", "lrcp").description(
//                "Set the progression order to one of lrcp, rlcp, rpcl, pcrl, " +
//                "cprl. with the following meaning: lrcp is " +
//                "layer-resolution-component-position (LRCP) progressive " +
//                "(i.e., rate scalable), rlcp is " +
//                "resolution-layer-component-position (RLCP) progressive " +
//                "(i.e., resolution scalable), rpcl is " +
//                "resolution-position-component-layer (RPCL) progressive, " +
//                "pcrl is position-component-resolution-layer (PCRL) " +
//                "progressive, and cprl is component-position-resolution-layer " +
//                "(CPRL) progressive. By default, LRCP progressive ordering " +
//                "is employed. Note that the RPCL and PCRL progressions are " +
//                "not valid for all possible image geometries.").build();
//        RestrictedStringListParameter prgServiceParam =
//                new RestrictedStringListParameter(prgParam,"-O prg=%s");
//        prgServiceParam.add("lrcp");
//        prgServiceParam.add("rlcp");
//        prgServiceParam.add("rpcl");
//        prgServiceParam.add("pcrl");
//        prgServiceParam.add("cprl");
//        serviceParametersList.add(prgServiceParam);


//        Parameter nomctParam = new Parameter.Builder("nomct", "false")
//                .description("Disallow the use of any multicomponent transform.").build();
//        serviceParametersList.add(new BooleanParameter(nomctParam,"-O nomct", "true", "false"));

//        Parameter sopParam = new Parameter.Builder("sop", "false")
//                .description("Generate SOP marker segments.").build();
//        serviceParametersList.add(new BooleanParameter(sopParam,"-O sop", "true", "false"));
//
//
//        Parameter ephParam = new Parameter.Builder("eph", "false")
//                .description("Generate EPH marker segments.").build();
//        serviceParametersList.add(new BooleanParameter(ephParam,"-O eph", "true", "false"));

        
//        Parameter lazyParam = new Parameter.Builder("lazy", "false")
//                .description("Enable lazy coding mode (a.k.a. arithmetic coding bypass).").build();
//        serviceParametersList.add(new BooleanParameter(lazyParam,"-O lazy", "true", "false"));
//
//
//        Parameter termallParam = new Parameter.Builder("termall", "false")
//                .description("Terminate all coding passes.").build();
//        serviceParametersList.add(new BooleanParameter(termallParam,"-O termall", "true", "false"));
//
//
//        Parameter segsymParam = new Parameter.Builder("segsym", "false")
//                .description("Use segmentation symbols.").build();
//        serviceParametersList.add(new BooleanParameter(segsymParam,"-O segsym", "true", "false"));
//
//
//        Parameter vcausalParam = new Parameter.Builder("vcausal", "false")
//                .description("Use vertically stripe causal contexts.").build();
//        serviceParametersList.add(new BooleanParameter(vcausalParam,"-O vcausal", "true", "false"));
//
//
//        Parameter ptermParam = new Parameter.Builder("pterm", "false")
//                .description("Use predictable termination.").build();
//        serviceParametersList.add(new BooleanParameter(ptermParam,"-O pterm", "true", "false"));
//
//
//        Parameter resetprobParam = new Parameter.Builder("resetprob", "false")
//                .description("Reset the probability models after each coding pass.").build();
//        serviceParametersList.add(new BooleanParameter(resetprobParam,"-O resetprob", "true", "false"));


        Parameter numgbitsParam = new Parameter.Builder("numgbits", "6")
                .description("Set the number of guard bits to this value.").build();
        serviceParametersList.add(new IntegerParameter(numgbitsParam, "-O numgbits=%s"));

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
