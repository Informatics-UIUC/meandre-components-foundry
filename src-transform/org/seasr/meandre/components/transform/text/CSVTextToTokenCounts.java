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

package org.seasr.meandre.components.transform.text;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 *
 * @author Boris Capitanu
 *
 */

@Component(
        creator = "Boris Capitanu",
        description = "Converts CSV text to tount counts.",
        name = "CSV Text To Token Counts",
        tags = "CSV, text, token count",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class CSVTextToTokenCounts extends AbstractExecutableComponent{

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The text to be converted" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_HEADER,
            description = "Does the input contain a header?",
            defaultValue = "true"
    )
    protected static final String PROP_HEADER = Names.PROP_HEADER;

    @ComponentProperty(
            name = "tokenSeparator",
            description = "The token to use to separate the field values. Use \\t if the separator is the tab character.",
            defaultValue = ","
    )
    protected static final String PROP_TOKEN_SEPARATOR = "tokenSeparator";

    @ComponentProperty(
            name = "token_pos",
            description = "The position of the token (the 'token' column) in the CSV (0=first, 1=second, etc.)",
            defaultValue = "0"
    )
    protected static final String PROP_TOKEN_POS = "token_pos";

    @ComponentProperty(
            name = "count_pos",
            description = "The position of the count (the 'count' column) in the CSV (0=first, 1=second, etc.)",
            defaultValue = "1"
    )
    protected static final String PROP_COUNT_POS = "count_pos";

    @ComponentProperty(
            name = Names.PROP_ORDERED,
            description = "Should the resulting token counts be ordered?",
            defaultValue = "true"
    )
    protected static final String PROP_ORDERED = Names.PROP_ORDERED;

	//--------------------------------------------------------------------------------------------


    private boolean bHeader, bOrdered;
    private String separator;
    private int tokenPos, countPos;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        bHeader = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HEADER, true, true, ccp));
        bOrdered = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_ORDERED, true, true, ccp));
        separator = getPropertyOrDieTrying(PROP_TOKEN_SEPARATOR, false, true, ccp).replaceAll("\\\\t", "\t");
        tokenPos = Integer.parseInt(getPropertyOrDieTrying(PROP_TOKEN_POS, ccp));
        countPos = Integer.parseInt(getPropertyOrDieTrying(PROP_COUNT_POS, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	Hashtable<String,Integer> htCounts = new Hashtable<String,Integer>();

    	for (String text : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))) {
    	    boolean skippedHeader = false;

    	    StringTokenizer st = new StringTokenizer(text, "\n");  // tokenize each line
    	    while (st.hasMoreTokens()) {
    	        if (bHeader && !skippedHeader) {
    	            st.nextToken();
    	            skippedHeader = true;
    	            continue;
    	        }

    	        String line = st.nextToken();
                String[] tokens = line.split(separator);
    	        if (tokens.length < (Math.max(tokenPos, countPos) + 1))
    	            throw new ComponentExecutionException(String.format("CSV line: '%s' does not contain enough values", line));

    	        String token = tokens[tokenPos];
    	        int count = Integer.parseInt(tokens[countPos]);

    	        if (htCounts.containsKey(token))
    	            console.warning(String.format("Token '%s' occurs more than once in the dataset - replacing previous count...", token));

    	        htCounts.put(token, count);
    	    }
        }

    	cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(htCounts, bOrdered));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
