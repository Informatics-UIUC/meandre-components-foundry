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
import java.util.Map;
import java.util.Map.Entry;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;

/**
 * This class reads all the token counts inputed and accumulates the counts
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */

@Component(
		name = "Token Counter Reducer",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "accumulate, reduce, token, count, token count",
		description = "This component is intended to work on wrapped model streams. " +
				      "Given a sequence of wrapped models, it will create a new model that " +
				      "accumulates/reduces the token counts and then pushes the resulting model. " +
				      "If no wrapped model is provided it will act as a simple pass through. This " +
				      "component is based on Wrapped models reducer.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class TokenCounterReducer extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts to accumulate" +
    			"<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>" +
    			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The accumulated token counts" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ PROPERTIES --------------------------------------------------

    // Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent

    @ComponentProperty(
            name = Names.PROP_ORDERED,
            description = "Should the token counts be ordered?",
            defaultValue = "true"
    )
    protected static final String PROP_ORDERED = Names.PROP_ORDERED;

	//--------------------------------------------------------------------------------------------


	/** The accumulated counts */
	protected Hashtable<String,Integer> htAcc;

	/** Number of models accumulated */
	protected int iCnt;

	/** Should the tokens be ordered */
	private boolean bOrdered;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    this.bOrdered = Boolean.parseBoolean(ccp.getProperty(PROP_ORDERED));

	    initializeReduction();
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		Object obj = cc.getDataComponentFromInput(IN_TOKEN_COUNTS);

		if ( this.htAcc==null )
			cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, obj);
		else
		    reduceModel(DataTypeParser.parseAsStringIntegerMap(obj));
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        this.htAcc = null;
        this.iCnt = 0;
        this.bOrdered = false;
    }

	//-----------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        console.entering(getClass().getName(), "handleStreamInitiators");

        // Try to revalance a stream
        if ( this.htAcc!=null ) {
            String sMessage = "Unbalanced wrapped stream. Got a new initiator without a terminator.";
            console.warning(sMessage);
            if ( this.ignoreErrors )
                pushReduction();
            else
                throw new ComponentExecutionException(sMessage);
        }
        htAcc = new Hashtable<String, Integer>();

        console.exiting(getClass().getName(), "handleStreamInitiators");
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        console.entering(getClass().getName(), "handleStreamTerminators");

        if ( this.htAcc==null ) {
            String sMessage = "Unbalanced wrapped stream. Got a new terminator without an initiator. Dropping it to try to rebalance.";
            console.warning(sMessage);
            if ( !this.ignoreErrors )
                throw new ComponentExecutionException(sMessage);
        }

        pushReduction();
        initializeReduction();

        console.exiting(getClass().getName(), "handleStreamTerminators");
    }

    //-----------------------------------------------------------------------------------

	/**
	 * Initializes the basic information about the reduction
	 *
	 */
	protected void initializeReduction() {
		this.htAcc = null;
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
		componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS, si);
		if (htAcc != null && htAcc.size() > 0)
		    componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(htAcc,bOrdered));
		componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS, st);
	}

	/**
	 * Accumulates the model.
	 *
	 * @param im The model to accumulate
	 */
	protected void reduceModel(Map<String, Integer> im) {
	    for (Entry<String, Integer> entry : im.entrySet()) {
			String sToken = entry.getKey();
			int  iCounts = entry.getValue();
			if ( htAcc.containsKey(sToken) )
				htAcc.put(sToken, htAcc.get(sToken)+iCounts);
			else
				htAcc.put(sToken, iCounts);
		}
		this.iCnt++;
	}
}