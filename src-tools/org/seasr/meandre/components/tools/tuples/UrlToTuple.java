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

package org.seasr.meandre.components.tools.tuples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;


/**
 * This component reads from a URL(http or file) and pushes its content inside of a tuple
 *
 * @author Mike Haberman
 *
 */

@Component(
		name = "URL To Tuple",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple, tools, text, filter",
		description = "This component reads a text resources via file or http (no authentication required)",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class UrlToTuple extends AbstractExecutableComponent {

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "tuples (one tuple: title, location, content/text)"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuple (title, location, content)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "text from the URL fetch, for convenience.  Same as the other input."
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_LOCATION,
			description = "url or file to read (http:// or file://)",
		    defaultValue = ""
	)
	protected static final String PROP_LOCATION = Names.PROP_LOCATION;

	@ComponentProperty(
			name = "title",
			description = "given title of the data",
		    defaultValue = ""
	)
	protected static final String PROP_TITLE = "title";

	//--------------------------------------------------------------------------------------------

	SimpleTuple outTuple;

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    String location = ccp.getProperty(PROP_LOCATION).trim();
	    String title    = ccp.getProperty(PROP_TITLE).trim();

		URL url = new URL(location);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		String inputLine;
		StringBuffer sb = new StringBuffer();
		while ((inputLine = in.readLine()) != null)
			sb.append(inputLine).append("\n");
		in.close();

		SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[]{"title", "location", "content"});
		outTuple = outPeer.createTuple();
		outTuple.setValue(0, title);
		outTuple.setValue(1, location);
		outTuple.setValue(2, sb.toString()); // actual content
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		// push the meta data
		cc.pushDataComponentToOutput(OUT_META_TUPLE, outTuple.getPeer().convert());

		// this array only has one element/tuple in it
		Strings[] results = new Strings[]{outTuple.convert()};
		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	    // convenience
	    Strings content = BasicDataTypesTools.stringToStrings(outTuple.getValue("content"));
		cc.pushDataComponentToOutput(OUT_TEXT, content);
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
