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
public class KakaduServiceParameters {

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
        // Creversible=yes
        Parameter reversibleParam = new Parameter.Builder("reversible", "yes")
                .description(
                "true/false. It indicates whether the compression is " +
                "reversible (lossless) or not.").build();
        Parameter layersParam = new Parameter.Builder("layers", "5")
                .description(
                "One positive Integer. Number of wavelet decomposition " +
                "levels, or stages.").build();
        Parameter levelsParam = new Parameter.Builder("levels", "5")
                .description(
                "One positive Integer. Embedded quality layers. Using rate " +
                "1.0,0.04, for example, the quality would roughly " +
                "logarithmically spaced between 0.04 " +
                "and 1.0 bits per pixel").build();
        // Stiles={1024,1024}
        Parameter tilesParam = new Parameter.Builder("tiles", "1024,1024")
                .description(
                "Two comma-separated positive Integers. After color " +
                "transformation, the image is split into so-called " +
                "tiles, rectangular regions of the image that are transformed " +
                "and encoded separately. Tiles can be any size, and it is also " +
                "possible to consider the whole image as one single tile. Once " +
                "the size is chosen, all the tiles will have the same size " +
                "(except optionally those on the right and bottom borders). " +
                "Dividing the image into tiles is advantageous in that the " +
                "decoder will need less memory to decode the image and it can " +
                "opt to decode only selected tiles to achieve a partial " +
                "decoding of the image.").build();
        // Cblk={64,64}
        Parameter cblkParam = new Parameter.Builder("cblk", "64,64").description(
                "Two comma-separated positive Integers. Codeblocks are used " +
                "to partition the image for processing and make it possible " +
                "to access portions of the datastream corresponding to " +
                "sub-regions of the image. This data ordering means that " +
                "the data for a thumbnail image occurs in a contiguous block " +
                "at the start of the datastream where it can be easily and " +
                "speedily accessed. This data organization makes it possible " +
                "to obtain a screen-resolution image quickly from a megabyte " +
                "or gigiabyte sized image compressed using JPEG 2000.").build();
        Parameter orderParam = new Parameter.Builder("order", "LRCP").description(
                "String of 4 characters 'L', 'R', 'C', 'P, e.g. LRCP, in " +
                "arbitrary order. Indicates the progression order. The four " +
                "character identifiers have the following interpretation: " +
                "L=layer; R=resolution; C=component; P=position. The first " +
                "character in the identifier refers to the index which " +
                "progresses most slowly, while the last refers to the index " +
                "which progresses most quickly.").build();
        serviceParametersList.add(new VarsizeListParameter(rateParam,"-rate %s",
                PrimitiveParameterType.DOUBLEPARM, "-"));
        serviceParametersList.add(new BooleanParameter(reversibleParam,
                "Creversible=%s","yes","no"));
        serviceParametersList.add(new IntegerParameter(layersParam,"Clayers=%s"));
        serviceParametersList.add(new IntegerParameter(levelsParam,"Clevels=%s"));
        ComplexParameter tilesServiceParm = new ComplexParameter(tilesParam,
                "Stiles={%s,%s}");
        tilesServiceParm.add(new IntegerParameter(new Parameter.Builder("xtiles", "1024").
                description("X dimension of tiles").build(),"%s"));
        tilesServiceParm.add(new IntegerParameter(new Parameter.Builder("ytiles", "1024").
                description("Y dimension of tiles").build(),"%s"));
        serviceParametersList.add(tilesServiceParm);
        ComplexParameter cblkServiceParam = new ComplexParameter(cblkParam,"Cblk={%s,%s}");
        cblkServiceParam.add(new IntegerParameter(new Parameter.Builder("xclbk", "64").
                description("X dimension of codeblocks").build(),"%s"));
        cblkServiceParam.add(new IntegerParameter(new Parameter.Builder("yclbk", "64").
                description("Y dimension of codeblocks").build(),"%s"));
        serviceParametersList.add(cblkServiceParam);
        RestrictedStringListParameter orderServiceParm =
                new RestrictedStringListParameter(orderParam,"Corder=%s");
        orderServiceParm.add("LRCP");
        orderServiceParm.add("LRPC");
        orderServiceParm.add("LCPR");
        orderServiceParm.add("LCRP");
        orderServiceParm.add("LPRC");
        orderServiceParm.add("LPCR");
        orderServiceParm.add("RLPC");
        orderServiceParm.add("RLCP");
        orderServiceParm.add("RCPL");
        orderServiceParm.add("RCLP");
        orderServiceParm.add("RPCL");
        orderServiceParm.add("RPLC");
        orderServiceParm.add("CLRP");
        orderServiceParm.add("CLPR");
        orderServiceParm.add("CRLP");
        orderServiceParm.add("CRPL");
        orderServiceParm.add("CPLR");
        orderServiceParm.add("CLRL");
        orderServiceParm.add("PLRC");
        orderServiceParm.add("PLCR");
        orderServiceParm.add("PRLC");
        orderServiceParm.add("PRCL");
        orderServiceParm.add("PCLR");
        orderServiceParm.add("PCRL");
         serviceParametersList.add(orderServiceParm);
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
    public KakaduServiceParameters() {
    }

}
