package eu.planets_project.services.shotgun;

import java.io.File;
import java.net.URI;
import java.util.List;

import javax.ejb.Stateless;
import javax.jws.WebService;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ImmutableContent;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.modify.Modify;
import eu.planets_project.services.modify.ModifyResult;
import eu.planets_project.services.shotgun.FileShotgun.Action;
import eu.planets_project.services.shotgun.FileShotgun.Key;
import eu.planets_project.services.utils.DigitalObjectUtils;

/**
 * Planets service Java re-implementation of the C++ shotgun file modification
 * tool by Manfred Thaller (http://www.hki.uni-koeln.de/material/shotGun).
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */
@WebService(name = ShotgunModify.NAME, serviceName = Modify.NAME, targetNamespace = PlanetsServices.NS, endpointInterface = "eu.planets_project.services.modify.Modify")
@Stateless
public final class ShotgunModify implements Modify {

    public static final String NAME = "ShotgunModify";

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.modify.Modify#modify(eu.planets_project.services.datatypes.DigitalObject,
     *      java.net.URI, java.net.URI, java.util.List)
     */
    public ModifyResult modify(DigitalObject digitalObject, URI inputFormat,
            URI actionURI, List<Parameter> parameters) {
        DigitalObject modified = modify(digitalObject, parameters);
        ModifyResult result = new ModifyResult(modified, new ServiceReport(
                Type.INFO, Status.SUCCESS, "OK"));
        return result;
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.PlanetsService#describe()
     */
    public ServiceDescription describe() {
        return new ServiceDescription.Builder(this.getClass().getSimpleName(),
                Modify.class.getName())
                .description(
                        "A service to simulate digital aging (aka 'shoot a file')")
                .furtherInfo(
                        URI
                                .create("http://www.hki.uni-koeln.de/material/shotGun/"))
                .build();
    }

    private DigitalObject modify(DigitalObject digitalObject,
            List<Parameter> parameters) {
        int seqCount = -1;
        int seqLength = -1;
        String action = null;
        for (Parameter parameter : parameters) {
            if (parameter.getName().equals(Key.SEQ_COUNT.toString())) {
                seqCount = Integer.parseInt(parameter.getValue());
            } else if (parameter.getName().equals(Key.SEQ_LENGTH.toString())) {
                seqLength = Integer.parseInt(parameter.getValue());
            } else if (parameter.getName().equals(Key.ACTION.toString())) {
                action = parameter.getValue();
            }
        }
        return modify(digitalObject, seqCount, seqLength, action);
    }

    private DigitalObject modify(DigitalObject digitalObject, int seqCount,
            int seqLength, String action) {
        File inputFile = DigitalObjectUtils.getContentAsTempFile(digitalObject);
        File outputFile = new FileShotgun().shoot(inputFile, seqCount,
                seqLength, Action.valueOf(action));
        return new DigitalObject.Builder(ImmutableContent.byValue(outputFile))
                .build();
    }
}
