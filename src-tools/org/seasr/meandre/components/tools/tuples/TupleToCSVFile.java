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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.components.utils.FileResourceUtility;

/**
 *
 * @author Mike Haberman
 *
 */

@Component(
		name = "Tuple To CSV File",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple, tools, text, filter",
		description = "This component writes the incoming set of tuples to a file (CSV, default)" ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleToCSVFile  extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "set of tuples" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;

	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of tuples (same as input)" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (same as input)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	@ComponentOutput(
			name = Names.PORT_FILENAME,
			description = "name of the file written" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_FILENAME = Names.PORT_FILENAME;

	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
			name = "tokenSeparator",
			description = "token to use to separate field values",
		    defaultValue = ","
	)
	protected static final String PROP_TOKEN_SEPARATOR = "tokenSeparator";


	@ComponentProperty(
			name = Names.PORT_FILENAME,
			description  = "filename to write to",
		    defaultValue = ""
	)
	protected static final String PROP_FILENAME = Names.PORT_FILENAME;

    //--------------------------------------------------------------------------------------------

	BufferedWriter output;
	String tokenSep;
	String filename;

    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception
    {
		filename = ccp.getProperty(PROP_FILENAME).trim();
		if (filename.length() == 0) {
			throw new ComponentContextException("Property not set " + PROP_FILENAME);
		}

		String path = filename;
		try {

			path = FileResourceUtility.createPathToPublishedResources(ccp, filename, console);
			console.info("writing data to " + path);
		    output = new BufferedWriter(new FileWriter(path));

	    } catch (IOException e) {
	    	throw new ComponentContextException("Unable to write to" + path);
	    }

	    console.info("tuple file " + filename);

	    tokenSep = ccp.getProperty(PROP_TOKEN_SEPARATOR).trim();
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = tuplePeer.createTuple();

		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

		//
		// write out the fieldnames as the first row
		//
		int size = tuplePeer.size();
		for (int i = 0; i < size; i++) {
			output.write(tuplePeer.getFieldNameForIndex(i));
			if (i + 1 < size) {
				output.write(tokenSep);
			}
		}
		output.write("\n");

		for (int i = 0; i < in.length; i++) {

			tuple.setValues(in[i]);

			for (int j = 0; j < size; j++) {
				output.write(tuple.getValue(j));
				if (j + 1 < size) {
					output.write(tokenSep);
				}
			}
			output.write("\n");
		}
		output.flush();

		Strings fn = BasicDataTypesTools.stringToStrings(filename);

		cc.pushDataComponentToOutput(OUT_FILENAME,   fn);
		cc.pushDataComponentToOutput(OUT_TUPLES,     input);
		cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);

	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    	console.info("CLosing output ");
    	output.close();
    }
}

