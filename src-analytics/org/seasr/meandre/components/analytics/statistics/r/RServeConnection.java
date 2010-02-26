package org.seasr.meandre.components.analytics.statistics.r;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

@Component(
        name = "R Serve Connection",
        creator = "Loretta Auvil",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "statistics, r, rserve",
        description = "This component establishes the connection to the R Server for using the R statistics package.",
        dependency = {"REngine.jar", "RserveEngine.jar", "protobuf-java-2.2.0.jar"}
)

public class RServeConnection extends AbstractExecutableComponent {

	@ComponentOutput(
            name = "RServeConnection",
            description = "The connection object for RServe"
    )
    protected static final String OUT_OBJECT = "RServeConnection";

	@Override
	public void disposeCallBack(ComponentContextProperties cc)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception, REXPMismatchException, REngineException {
		// TODO Auto-generated method stub
		RConnection conn = new RConnection();
		REXP x = conn.eval("R.version.string");
		console.info(x.asString());
		cc.pushDataComponentToOutput(OUT_OBJECT, conn);
	}

	@Override
	public void initializeCallBack(ComponentContextProperties cc)
			throws Exception {
		// TODO Auto-generated method stub

	}
}
