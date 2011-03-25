/**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */

package org.seasr.meandre.components.analytics.statistics.r;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

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

    //------------------------------ INPUTS ------------------------------------------------------

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

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
            name = "RServeConnection",
            description = "The connection object for RServe"
    )
    protected static final String OUT_OBJECT = "RServeConnection";

	//--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties cc) throws Exception {
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
    public void disposeCallBack(ComponentContextProperties cc) throws Exception {
    }
}
