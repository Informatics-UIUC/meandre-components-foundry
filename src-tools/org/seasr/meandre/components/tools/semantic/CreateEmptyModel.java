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
import org.seasr.meandre.components.tools.Names;

import com.hp.hpl.jena.rdf.model.ModelFactory;

/** Pushes a property value to the output
 *
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Create empty model",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "io, string",
		dependency = {"protobuf-java-2.0.3.jar"},
		description = "Pushes an empty model. It provides " +
				      "a couple of properties to control how many times it needs to be pushed, " +
				      "and if it needs to be wrapped with terminators "
)
public class CreateEmptyModel implements ExecutableComponent{

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_TIMES,
			description = "The number of times to push the message. ",
		    defaultValue = "1"
		)
	private final static String PROP_TIMES = Names.PROP_TIMES;

	@ComponentProperty(
			name = Names.PROP_WRAP_STREAM,
			description = "Should the pushed message be wrapped as a stram. ",
		    defaultValue = "false"
		)
	private final static String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	//--------------------------------------------------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_DOCUMENT,
			description = "The empty model being pushed"
		)
	private final static String PORT_DOCUMENT = Names.PORT_DOCUMENT;

	//--------------------------------------------------------------------------------------------

	/** The number of times to push the string */
	private long lTimes;

	/** Should be wrapped */
	private boolean bWrapped;

	//--------------------------------------------------------------------------------------------

	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		lTimes = Long.parseLong(ccp.getProperty(PROP_TIMES));
		bWrapped = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		lTimes = 0;
		bWrapped = false;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		if ( bWrapped )
			pushInitiator(cc);
		for ( long l=0 ; l<lTimes ; l++ )
			cc.pushDataComponentToOutput(PORT_DOCUMENT, ModelFactory.createDefaultModel());
		if ( bWrapped )
			pushTerminator(cc);
	}


	//-----------------------------------------------------------------------------------

	/** Pushes an initiator.
	 *
	 * @param cc The component context
	 * @throws ComponentContextException Something went wrong when pushing
	 */
	private void pushInitiator(ComponentContext cc) throws ComponentContextException {
		StreamInitiator si = new StreamInitiator();
		si.put(PROP_TIMES, lTimes);
		cc.pushDataComponentToOutput(PORT_DOCUMENT,si);
	}

	/** Pushes a terminator.
	 *
	 * @param cc The component context
	 * @throws ComponentContextException Something went wrong when pushing
	 */
	private void pushTerminator(ComponentContext cc) throws ComponentContextException {
		StreamTerminator st = new StreamTerminator();
		st.put(PROP_TIMES, lTimes);
		cc.pushDataComponentToOutput(PORT_DOCUMENT,st);
	}

}
