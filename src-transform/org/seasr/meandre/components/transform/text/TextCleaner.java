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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Performs find and replace on text using regular expressions",
        name = "Text Cleaner",
        tags = "#TRANSFORM, text, remove, replace",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TextCleaner extends AbstractExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The text to clean or replace" +
                          "<br>TYPE: java.lang.String" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                          "<br>TYPE: byte[]" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                          "<br>TYPE: java.lang.Object",
            name = Names.PORT_TEXT
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The cleaned or replaced text" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings",
            name = Names.PORT_TEXT
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "The regular expression to find the matched substring. " +
            	          "For example, if specifying the regular expression as 'push' and " +
            	          "the replacement as 'pushing', then the sequence of " +
            	          "characters 'push' contained in any word is substituted with 'pushing'. " +
            	          "Additionally, capturing groups can be used and referenced in the replacement string as " +
            	          "$1 for the first capturing group, $2 for second, and so on.",
            name = Names.PROP_FIND,
            defaultValue = ""
	)
	protected static final String PROP_FIND = Names.PROP_FIND;

	@ComponentProperty(
	        description = "The replacement to substitute the matched substring found by find. " +
	                      "If the replacement string needs to contain the literals $ and \\ then " +
	                      "they should be escaped because they have special meaning. For example, as part of the replacement " +
	                      "string one can use '$1' to refer to the first capturing group defined in the regular expression " +
	                      "for find, $2 for the second, and so on.  If the literal '$1' is desired, then it should be escaped as '\\$1'.",
            name = Names.PROP_REPLACE,
            defaultValue = ""
	)
	protected static final String PROP_REPLACE = Names.PROP_REPLACE;

	@ComponentProperty(
	        description = "The regular expression to find the matched substring.",
            name = Names.PROP_FIND_2,
            defaultValue = ""
	)
	protected static final String PROP_FIND_2 = Names.PROP_FIND_2;

	@ComponentProperty(
	        description = "The replacement to substitute the matched substring found by find2.",
            name = Names.PROP_REPLACE_2,
            defaultValue = ""
	)
	protected static final String PROP_REPLACE_2 = Names.PROP_REPLACE_2;

	@ComponentProperty(
	        description = "The regular expression to find the matched substring.",
            name = Names.PROP_FIND_3,
            defaultValue = ""
	)
	protected static final String PROP_FIND_3 = Names.PROP_FIND_3;

	@ComponentProperty(
	        description = "The replacement to substitute the matched substring found by find3.",
            name = Names.PROP_REPLACE_3,
            defaultValue = ""
	)
	protected static final String PROP_REPLACE_3 = Names.PROP_REPLACE_3;

	@ComponentProperty(
	        description = "The regular expression to find the matched substring.",
            name = Names.PROP_FIND_4,
            defaultValue = ""
	)
	protected static final String PROP_FIND_4 = Names.PROP_FIND_4;

	@ComponentProperty(
	        description = "The replacement to substitute the matched substring found by find4.",
            name = Names.PROP_REPLACE_4,
            defaultValue = ""
	)
	protected static final String PROP_REPLACE_4 = Names.PROP_REPLACE_4;

	//--------------------------------------------------------------------------------------------


	private final Map<Pattern,String> replacements = new LinkedHashMap<Pattern,String>();


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    String find = getPropertyOrDieTrying(PROP_FIND, false, false, ccp);
	    if (find.length() > 0)
	        replacements.put(Pattern.compile(find), getPropertyOrDieTrying(PROP_REPLACE, false, false, ccp));

	    find = getPropertyOrDieTrying(PROP_FIND_2, false, false, ccp);
	    if (find.length() > 0)
	        replacements.put(Pattern.compile(find), getPropertyOrDieTrying(PROP_REPLACE_2, false, false, ccp));

	    find = getPropertyOrDieTrying(PROP_FIND_3, false, false, ccp);
	    if (find.length() > 0)
	        replacements.put(Pattern.compile(find), getPropertyOrDieTrying(PROP_REPLACE_3, false, false, ccp));

	    find = getPropertyOrDieTrying(PROP_FIND_4, false, false, ccp);
	    if (find.length() > 0)
	        replacements.put(Pattern.compile(find), getPropertyOrDieTrying(PROP_REPLACE_4, false, false, ccp));

	    if (replacements.size() == 0)
	        console.warning("No find/replace regular expressions have been set. No action will be taken on the input text.");
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		String[] input = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));
		String[] output = new String[input.length];

		for (int i = 0, iMax = input.length; i < iMax; i++) {
		    String text = input[i];

		    for (Entry<Pattern,String> entry : replacements.entrySet()) {
		        Pattern regexp = entry.getKey();
		        String replacement = entry.getValue();

		        Matcher matcher = regexp.matcher(text);

		        while (matcher.find())
    		        for (int n = 1, nMax = matcher.groupCount(); n <= nMax; n++)
    		            if (matcher.group(n) != null)
    		                console.fine(String.format("Group %2$d ($%2$d) match: '%s'", matcher.group(n), n));

                matcher.reset();

		        text = matcher.replaceAll(replacement);
		    }

		    output[i] = text;
		}

		cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(output));
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
