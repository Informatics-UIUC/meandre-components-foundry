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

package org.seasr.meandre.components.tools.basic;

import java.io.File;

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
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.Names;

/** Given a directory pushes all the file name available that match a certain
 * regular expression given in the properties
 *
 * @author Xavier Llor&agrave;
 *
 */
@Component(
		name = "List directory files",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "io, string",
		dependency = {"protobuf-java-2.0.3.jar"},
		description = "Given a directory pushes all the file name available that " +
				      "match a certain regular expression given in the properties."
)
public class ListDirectoryFiles implements ExecutableComponent{

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_EXPRESSION,
			description = "The regular expression to use as a filter. ",
		    defaultValue = ".*"
		)
	private final static String PROP_EXPRESSION = Names.PROP_EXPRESSION;

	@ComponentProperty(
			name = Names.PROP_RECURSIVE,
			description = "Should the directory be processed recursively? ",
		    defaultValue = "true"
		)
	private final static String PROP_RECURSIVE = Names.PROP_RECURSIVE;

	@ComponentProperty(
			name = Names.PROP_WRAP_STREAM,
			description = "Should the pushed message be wrapped as a stream. ",
		    defaultValue = "false"
		)
	private final static String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The location of the directory to push"
		)
	private final static String INPUT_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The location being pushed"
		)
	private final static String OUTPUT_LOCATION = Names.PORT_LOCATION;

	//--------------------------------------------------------------------------------------------

	/** The regular expression */
	private String sExpression;

	/** Should recurse? */
	private boolean bRecursive;

	/** Should be wrapped */
	private boolean bWrapped;

	//--------------------------------------------------------------------------------------------

	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		sExpression = ccp.getProperty(PROP_EXPRESSION);
		bRecursive = Boolean.parseBoolean(ccp.getProperty(PROP_RECURSIVE));
		bWrapped = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		sExpression = null;
		bWrapped = bRecursive = false;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		Object obj = cc.getDataComponentFromInput(INPUT_LOCATION);
		if ( obj instanceof StreamDelimiter )
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, obj);
		else {
			String sLoc = (obj instanceof Strings)?((Strings)obj).getValue(0):obj.toString();
			if ( bWrapped )
				pushInitiator(cc,sLoc);
			pushLocations(cc,new File(sLoc));
			if ( bWrapped )
				pushTerminator(cc,sLoc);
		}
	}


	//-----------------------------------------------------------------------------------

	/** Pushes an initiator.
	 *
	 * @param cc The component context
	 * @param sLoc The location being processed
	 * @throws ComponentContextException Something went wrong when pushing
	 */
	private void pushInitiator(ComponentContext cc, String sLoc)
	throws ComponentContextException {
		StreamInitiator si = new StreamInitiator();
		si.put(INPUT_LOCATION, sLoc);
		cc.pushDataComponentToOutput(OUTPUT_LOCATION,si);
	}

	/** Pushes a terminator.
	 *
	 * @param cc The component context
	 * @param sLoc The location being processed
	 * @throws ComponentContextException Something went wrong when pushing
	 */
	private void pushTerminator(ComponentContext cc, String sLoc)
	throws ComponentContextException {
		StreamTerminator st = new StreamTerminator();
		st.put(INPUT_LOCATION, sLoc);
		cc.pushDataComponentToOutput(OUTPUT_LOCATION,st);
	}

	/** Pushed the locations that mached the given expression.
	 *
	 * @param cc The component context
	 * @param fileLoc The location being processed
	 * @throws ComponentContextException Failed to push the the file name to the output
	 */
	private boolean pushLocations(ComponentContext cc, File fileLoc)
	throws ComponentContextException {
		if ( fileLoc.isDirectory() ) {
            String[] children = fileLoc.list();
            for (int i=0; bRecursive && i<children.length ; i++) {
                boolean success = pushLocations(cc,new File(fileLoc, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
		else {
			String sLoc = fileLoc.toString();
			if ( sLoc.matches(sExpression) )
				cc.pushDataComponentToOutput(OUTPUT_LOCATION, BasicDataTypesTools.stringToStrings(sLoc));
		}
		return true;
	}


}
