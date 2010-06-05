/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.seasr.meandre.components.tools.tuples;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 *
 * @author Mike Haberman
 *
 */

@Component(
		name = "CSV To Tuple",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tools, text,",
		description = "This component converts a csv string into tuples.  Each line of the incoming text is a new tuple. It does not handle missing values" ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class CSVToTuple extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "the text to be parsed into tuples.  Each line is a new tuple." +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "set of tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	//----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "column names/labels to be used (comma separated)",
            name = "labels",
            defaultValue = ""
    )
    protected static final String DATA_PROPERTY_COLUMN_NAMES = "labels";

    @ComponentProperty(
            description = "token used to separate the values (ie column data)",
            name = "tokenSeparator",
            defaultValue = ","
    )
    protected static final String DATA_PROPERTY_TOKEN_SEPARATOR = "tokenSeparator";

   	//--------------------------------------------------------------------------------------------

    String tokenSeparator;
    SimpleTuplePeer outPeer;

    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.tokenSeparator = ccp.getProperty(DATA_PROPERTY_TOKEN_SEPARATOR).trim();
		String colNames = ccp.getProperty(DATA_PROPERTY_COLUMN_NAMES).trim();
		String[] vals = colNames.split(",");

		if (colNames.length() == 0 || vals.length == 0) {
			throw new ComponentExecutionException(DATA_PROPERTY_COLUMN_NAMES + " needs to be set");
		}

		outPeer = new SimpleTuplePeer(vals);
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {

		int numOfColumns = outPeer.size();

		String[] text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));
		String toParse = text[0];

		List<Strings> output = new ArrayList<Strings>();
		BufferedReader reader = new BufferedReader(new StringReader(toParse));
		SimpleTuple tuple = outPeer.createTuple();
		while (true) {

			String line = reader.readLine();
			if (line == null) {
				break;
			}

			// skip commented or empty lines
			if (line.indexOf("#") == 0 || line.length() == 0)
				continue;

            String[] parts = line.split(tokenSeparator, numOfColumns);
            if (parts.length == numOfColumns) {
            	tuple.setValues(parts);
    			output.add(tuple.convert());
            }
            else {
            	// else skip the line
            	console.fine("skipping line " + line);
            }
		}

		 Strings[] results = new Strings[output.size()];
		 output.toArray(results);

		 StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
		 cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	     // tuple meta data
		 cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
