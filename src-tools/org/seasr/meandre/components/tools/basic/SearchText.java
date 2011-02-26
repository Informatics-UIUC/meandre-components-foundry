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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * Searches text using regular expressions
 *
 * @author Loretta Auvil
 * @author Boris Capitanu
 *
 */

@Component(
        name = "Search Text",
        creator = "Loretta Auvil",
		description = "Searches the text input for the regular expression pattern. "+
		              "If the pattern is found and the regular expression specifies a capturing group, " +
		              "then the matching text for the capturing group is pushed out on port Text_Found. " +
		              "If the pattern is found and there are no capturing groups defined, the maching text for the " +
		              "pattern is pushed out on port Text_Found.  In both cases multiple outputs are possible." +
		              "If the pattern is not found, it outputs the original text on port Text.",
		tags = "text, string, search",
		rights = Licenses.UofINCSA,
		baseURL = "meandre://seasr.org/components/foundry/",
		dependency = { "protobuf-java-2.2.0.jar" }
)
public class SearchText extends AbstractExecutableComponent {

	// ------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        name = Names.PORT_TEXT,
	        description = "Text to be searched." +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_TEXT = Names.PORT_TEXT;

	// ------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        name = Names.PORT_TEXT,
	        description = "The searched text." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

	@ComponentOutput(
	        name = Names.PORT_TEXT_FOUND,
	        description = "The matching text that satisfies the regular expression." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_MATCHED_TEXT = Names.PORT_TEXT_FOUND;

	// ------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        name = Names.PROP_EXPRESSION,
	        description = "The regular expression to use as search criteria. ",
	        defaultValue = ".+\\.pdf")
	protected static final String PROP_EXPRESSION = Names.PROP_EXPRESSION;

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

    //--------------------------------------------------------------------------------------------


    protected boolean _wrapStream;

	/** The regular expression */
	protected Pattern _regexp;


	// --------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		_regexp = Pattern.compile(getPropertyOrDieTrying(PROP_EXPRESSION, false, true, ccp));
        _wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		String[] input = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));

		if (_wrapStream) {
		    StreamDelimiter sd = new StreamInitiator();
		    cc.pushDataComponentToOutput(OUT_TEXT, sd);
		    cc.pushDataComponentToOutput(OUT_MATCHED_TEXT, sd);
		}

		for (String text : input) {
    		console.finest(String.format("Input text: %s", text));
    		console.finer(String.format("Using regular expression: %s", _regexp.pattern()));

    		Matcher matcher = _regexp.matcher(text);
    		boolean found = false;

    		while (matcher.find()) {
                found = true;

                console.finer(String.format("Match: start=%d, end=%d", matcher.start(), matcher.end()));
                for (int i = 0, iMax = matcher.groupCount() + 1; i < iMax; i++)
                    console.finer(String.format("Group %d: match='%s'", i, matcher.group(i)));

                String matchText = matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
                if (matchText == null) continue;

    			componentContext.pushDataComponentToOutput(OUT_MATCHED_TEXT, BasicDataTypesTools.stringToStrings(matchText));
    		}

    		if (!found)
    		    componentContext.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
		}

		if (_wrapStream) {
		    StreamDelimiter sd = new StreamTerminator();
		    cc.pushDataComponentToOutput(OUT_TEXT, sd);
		    cc.pushDataComponentToOutput(OUT_MATCHED_TEXT, sd);
		}
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		_regexp = null;
	}

	// --------------------------------------------------------------------------------------------

	@Override
	protected void handleStreamInitiators() throws Exception {
		Object input = componentContext.getDataComponentFromInput(IN_TEXT);
        componentContext.pushDataComponentToOutput(OUT_TEXT, input);
		componentContext.pushDataComponentToOutput(OUT_MATCHED_TEXT, input);
	}

	@Override
	protected void handleStreamTerminators() throws Exception {
	    Object input = componentContext.getDataComponentFromInput(IN_TEXT);
        componentContext.pushDataComponentToOutput(OUT_TEXT, input);
	    componentContext.pushDataComponentToOutput(OUT_MATCHED_TEXT, input);
	}
}