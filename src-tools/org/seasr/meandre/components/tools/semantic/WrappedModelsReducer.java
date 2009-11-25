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

package org.seasr.meandre.components.tools.semantic;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */

@Component(
		name = "Wrapped Models Reducer",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, io, transform, model, accumulate, reduce",
		description = "This component is intended to work on wrapped model streams. " +
				      "Given a sequence of wrapped models, it will create a new model that " +
				      "accumulates/reduces all the information and then push them the resulting model. " +
				      "If no wrapped model is provided it will act as a simple pass through. ",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class WrappedModelsReducer extends AbstractExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document to accumulate"
	)
	protected static final String IN_DOCUMENT = Names.PORT_DOCUMENT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_DOCUMENT,
			description = "The semantic document accumulated"
	)
	protected static final String OUT_DOCUMENT = Names.PORT_DOCUMENT;

    //------------------------------ PROPERTIES --------------------------------------------------

	// Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent

	//--------------------------------------------------------------------------------------------


	/** The accumulating model */
	protected Model modelAcc;

	/** Number of models accumulated */
	protected int iCnt;


	//--------------------------------------------------------------------------------------------

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.modelAcc = null;
		this.iCnt = 0;
	}

	public void executeCallBack(ComponentContext cc) throws Exception {
	    Model model = DataTypeParser.parseAsModel(cc.getDataComponentFromInput(IN_DOCUMENT));

		if ( this.modelAcc==null )
		    cc.pushDataComponentToOutput(OUT_DOCUMENT, model);
		else
		    reduceModel(model);
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        this.modelAcc = null;
        this.iCnt = 0;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        // Try to revalance a stream
        if ( this.modelAcc != null ) {
            String sMessage = "Unbalanced wrapped stream. Got a new initiator without a terminator.";
            console.warning(sMessage);
            if ( this.ignoreErrors )
                pushReduction();
            else
                throw new ComponentExecutionException(sMessage);
        }

        // Initialize the accumulation model
        initializeReduction();
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if ( this.modelAcc==null ) {
            String sMessage = "Unbalanced wrapped stream. Got a new terminator without an initiator. Dropping it to try to rebalance.";
            console.warning(sMessage);
            if ( !this.ignoreErrors )
                throw new ComponentExecutionException(sMessage);
        }

        pushReduction();
        initializeReduction();
    }

	//-----------------------------------------------------------------------------------

	/**
	 * Initializes the basic information about the reduction
	 *
	 */
	protected void initializeReduction() {
		this.modelAcc = ModelFactory.createDefaultModel();
		this.iCnt = 0;
	}

	/**
	 * Pushes the accumulated model.
	 *
	 * @throws Exception Failed to push the accumulated model
	 */
	protected void pushReduction() throws Exception {
		// Create the delimiters
		StreamInitiator si = new StreamInitiator();
		StreamTerminator st = new StreamTerminator();
		si.put("count", this.iCnt); si.put("accumulated", 1);
		st.put("count", this.iCnt); st.put("accumulated", 1);

		// Push
		componentContext.pushDataComponentToOutput(OUT_DOCUMENT, si);
		componentContext.pushDataComponentToOutput(OUT_DOCUMENT, this.modelAcc);
		componentContext.pushDataComponentToOutput(OUT_DOCUMENT, st);

	}

	/**
	 * Accumulates the model.
	 *
	 * @param model The model to accumulate
	 */
	protected void reduceModel(Model model) {
		this.modelAcc.add(model);
		this.iCnt++;
	}
}
