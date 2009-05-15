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

package org.seasr.meandre.components.analytics.text.statistics;

import java.util.Hashtable;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.IntegersMap;
import org.seasr.meandre.components.tools.Names;

/** This class reads all the token counts inputed and accumulates the counts
 *
 * @author Xavier Llor&agrave;
 *
 */
@Component(
		name = "Token counter reducer",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "semantic, model, accumulate, reduce, token counts",
		description = "This component is intended to work on wrapped model streams. " +
				      "Given a sequence of wrapped models, it will create a new model that " +
				      "accumulates/reduces the token counts and then pushes them the resulting model. " +
				      "If no wrapped model is provided it will act as a simple pass through. This " +
				      "component is based on Wrapped models reducer."
)
public class TokenCounterReducer
implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_ERROR_HANDLING,
			description = "If set to true errors will be handled and they will be reported to the screen ." +
					      "Otherwise, the component will throw an exception an force the flow to abort. ",
		    defaultValue = "true"
		)
	protected final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;


	@ComponentProperty(
			name = Names.PROP_ORDERED,
			description = "Should the token counts be ordered?",
			defaultValue = "true"
		)
	private final static String PROP_ORDERED = Names.PROP_ORDERED;

	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts to accumulate"
		)
	protected final static String INPUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The accumulated token counts"
		)
	protected final static String OUTPUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	//--------------------------------------------------------------------------------------------

	/** The error handling flag */
	protected boolean bErrorHandling;

	/** The accumulated counts */
	protected Hashtable<String,Integer> htAcc;

	/** Number of models accumulated */
	protected int iCnt;

	/** Should the tokens be ordered */
	private boolean bOrdered;

	//--------------------------------------------------------------------------------------------


	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
		this.htAcc = null;
		this.iCnt = 0;
		this.bOrdered = Boolean.parseBoolean(ccp.getProperty(PROP_ORDERED));
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.bErrorHandling = false;
		this.htAcc = null;
		this.iCnt = 0;
		this.bOrdered = false;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		Object obj = cc.getDataComponentFromInput(INPUT_TOKEN_COUNTS);

		if ( obj instanceof StreamInitiator ) {
			// Try to revalance a stream
			if ( this.htAcc!=null ) {
				String sMessage = "Unbalanced wrapped stream. Got a new initiator without a terminator.";
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( this.bErrorHandling )
					pushReduction(cc);
				else
					throw new ComponentExecutionException(sMessage);
			}
			// Initialize the accumulation model
			initializeReduction();
		}
		else if ( obj instanceof StreamTerminator ) {
			if ( this.htAcc==null ) {
				String sMessage = "Unbalanced wrapped stream. Got a new terminator without an initiator. Dropping it to try to rebalance.";
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( !this.bErrorHandling )
					throw new ComponentExecutionException(sMessage);
			}
			pushReduction(cc);
			initializeReduction();
		}
		else {
			if ( this.htAcc==null )
				cc.pushDataComponentToOutput(OUTPUT_TOKEN_COUNTS, obj);
			else {
				try {
					IntegersMap im = (IntegersMap) obj;
					reduceModel(im);
				}
				catch ( ClassCastException e ) {
					String sMessage = "Input data is not an integer map";
					cc.getLogger().warning(sMessage);
					cc.getOutputConsole().println("WARNING: "+sMessage);
					if ( !bErrorHandling )
						throw new ComponentExecutionException(e);
				}

			}
		}
	}


	//-----------------------------------------------------------------------------------


	/** Initializes the basic information about the reduction
	 *
	 */
	protected void initializeReduction() {
		this.htAcc = new Hashtable<String, Integer>(1000);
		this.iCnt = 0;
	}

	/** Pushes the accumulated model.
	 *
	 * @param cc The component context
	 * @throws ComponentContextException Failed to push the accumulated model
	 */
	protected void pushReduction(ComponentContext cc) throws ComponentContextException {
		// Create the delimiters
		StreamInitiator si = new StreamInitiator();
		StreamTerminator st = new StreamTerminator();
		si.put("count", this.iCnt); si.put("accumulated", 1);
		st.put("count", this.iCnt); st.put("accumulated", 1);

		// Push
		cc.pushDataComponentToOutput(OUTPUT_TOKEN_COUNTS, si);
		cc.pushDataComponentToOutput(OUTPUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(htAcc,bOrdered));
		cc.pushDataComponentToOutput(OUTPUT_TOKEN_COUNTS, st);

	}


	/** Accumulates the model.
	 *
	 * @param im The model to accumulate
	 */
	protected void reduceModel(IntegersMap im) {
		for ( int i=0, iMax=im.getKeyCount() ; i<iMax ; i++ ) {
			String sToken = im.getKey(i);
			int  iCounts = im.getValue(i).getValue(0);
			if ( htAcc.containsKey(sToken) )
				htAcc.put(sToken, htAcc.get(sToken)+iCounts);
			else
				htAcc.put(sToken, iCounts);
		}
		this.iCnt++;
	}

}


