package org.seasr.meandre.components.analytics.statistics.r;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;

	@Component(
	        name = "R Serve Wrapper",
	        creator = "Loretta Auvil",
	        baseURL = "meandre://seasr.org/components/foundry/",
	        firingPolicy = FiringPolicy.all,
	        mode = Mode.compute,
	        rights = Licenses.UofINCSA,
	        tags = "statistics, r, rserve, script",
	        description = "This component sends an R script that it receives as input to the R Server at the connection it receives as input.",
	        dependency = {"REngine.jar", "RserveEngine.jar", "protobuf-java-2.2.0.jar"}
	)

public class RServeWrapper extends AbstractExecutableComponent {

		@ComponentInput(
	            name = "RServeConnection",
	            description = "The connection object for RServe"
	    )
	    protected static final String IN_OBJECT = "RServeConnection";

		@ComponentInput(
	            name = "RScript",
	            description = "The RScript"
	    )
	    protected static final String IN_SCRIPT_OBJECT = "RScript";

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

			RConnection conn = (RConnection) cc.getDataComponentFromInput(IN_OBJECT);
			String[] script = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_SCRIPT_OBJECT));
			console.info(script[0]);
			REXP d = conn.eval(script[0]);
			console.info(d.asString());
			cc.pushDataComponentToOutput(OUT_OBJECT, conn);
		}

		@Override
		public void initializeCallBack(ComponentContextProperties cc)
				throws Exception {
		}
	}
