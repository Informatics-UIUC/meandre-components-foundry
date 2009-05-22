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

package org.seasr.meandre.components.tools.text.io;

import java.net.URI;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.components.utils.ComponentUtils;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.io.IOUtils;
import org.seasr.meandre.support.parsers.DataTypeParser;

/**
 * Reads text from a local or remote location
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 */
@Component(
		name = "Read Text",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "semantic, io, read, text",
		description = "This component reads text from a local or remote location. The text location is specified " +
				      "in the input. The component outputs the text " +
				      "read. A property controls the behavior of the component in " +
				      "the event of an IO error, allowing it to ignore the error and continue, or " +
				      "throw an exception, forcing the finalization of the flow execution."
)
public class ReadText extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the text to read"
	)
	protected static final String IN_LOCATION = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The location that the text was read from"
		)
	protected static final String OUT_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The text read"
		)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

	//--------------------------------------------------------------------------------------------


	private Logger _console;


	//--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _console = getConsoleLogger();
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        URI uri = DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_LOCATION));
        String sRes =  IOUtils.getTextFromReader(IOUtils.getReaderForResource(uri));

        cc.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(uri.toString()));
        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(sRes));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators(ComponentContext cc, Set<String> inputPortsWithInitiators)
            throws ComponentContextException, ComponentExecutionException {
        pushDelimiters(cc, (StreamInitiator)cc.getDataComponentFromInput(IN_LOCATION));
    }

    @Override
    protected void handleStreamTerminators(ComponentContext cc, Set<String> inputPortsWithTerminators)
            throws ComponentContextException, ComponentExecutionException {
        pushDelimiters(cc, (StreamTerminator)cc.getDataComponentFromInput(IN_LOCATION));
    }

    //--------------------------------------------------------------------------------------------

    /** Push the delimiters
     *
     * @param cc The component context
     * @param sdLoc The delimiter object
     * @throws ComponentContextException
     */
    private void pushDelimiters(ComponentContext cc, StreamDelimiter sdLoc)
            throws ComponentContextException {
        cc.pushDataComponentToOutput(OUT_LOCATION, sdLoc);
        try {
            cc.pushDataComponentToOutput(OUT_TEXT, ComponentUtils.cloneStreamDelimiter(sdLoc));
        } catch (Exception e) {
            _console.warning("Failed to create a new delimiter - reusing current one");
            cc.pushDataComponentToOutput(OUT_TEXT, sdLoc);
        }
    }
}
