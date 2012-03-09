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

package org.seasr.meandre.components.transform.filters;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.core.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * This component tokenizes the text contained in the input model using OpenNLP.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */

@Component(
		name = "Token Filter",
		creator = "Boris Capitanu",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.any,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "#TRANSFORM, text, filter, token, token count, tokenized sentence, word",
		description = "This component filters the tokens of the input based " +
				      "on the list of tokens provided. The component has 3 inputs for the " +
				      "type of data to be filtered (tokens, token counts or tokenized sentences" +
				      "and one input for the list of tokens to filter. "+
				      "It will output the same data type it received. If new tokens to " +
				      "filter are provide they either replace the current ones " +
				      "or add them to the black list. The component waits for a black list and then " +
				      "begins processing the data it receives. The component outputs the " +
				      "filtered tokens, token counts or tokenized sentences. The comparison of blacklisted "+
				      "tokens to the data will ignore case by default. Set ignore_case=false to work in case sensitive mode.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class TokenFilter extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKEN_BLACKLIST,
			description = "The list of tokens defining the blacklist." +
    			 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_TOKEN_BLACKLIST = Names.PORT_TOKEN_BLACKLIST;

	@ComponentInput(
			name = Names.PORT_TOKENS,
			description = "The sequence of tokens to filter." +
    			 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_TOKENS = Names.PORT_TOKENS;

	@ComponentInput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts to filter." +
			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap" +
			"<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>"
	)
	protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	@ComponentInput(
			name = Names.PORT_TOKENIZED_SENTENCES,
			description = "The tokenized sentences to filter." +
			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
	)
	protected static final String IN_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKENS,
			description = "The filtered tokens." +
			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_TOKENS = Names.PORT_TOKENS;

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The filtered token counts." +
			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	@ComponentOutput(
			name = Names.PORT_TOKENIZED_SENTENCES,
			description = "The filtered tokenized sentences." +
			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
	)
	protected static final String OUT_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;

    //------------------------------ PROPERTIES --------------------------------------------------

	// Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent

    @ComponentProperty(
            name = Names.PROP_REPLACE,
            description = "If set to true then blacklisted tokens get replaced when a new set is provided. " +
                          "When set to false, tokens keep being appended to the blacklist. ",
            defaultValue = "true"
    )
    protected static final String PROP_REPLACE = Names.PROP_REPLACE;

    @ComponentProperty(
            name = "ignore_case",
            description = "If set to true then the comparison between the blacklisted tokens and data " +
                          "will ignore case, otherwise case sensitivity will be respected.",
            defaultValue = "true"
    )
    protected static final String PROP_IGNORE_CASE = "ignore_case";

	//--------------------------------------------------------------------------------------------


    protected boolean _replaceBlacklist = false;
    protected boolean _ignoreCase = false;

    protected Set<String> _blackList = null;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    _replaceBlacklist = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_REPLACE, ccp));
	    _ignoreCase = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_IGNORE_CASE, ccp));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
	    for (String portName : new String[] { IN_TOKENS, IN_TOKEN_COUNTS, IN_TOKENIZED_SENTENCES })
	        componentInputCache.storeIfAvailable(cc, portName);

	    if (cc.isInputAvailable(IN_TOKEN_BLACKLIST)) {
	        if (_replaceBlacklist || _blackList == null)
	            _blackList = new HashSet<String>(100);

	        for (String word : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TOKEN_BLACKLIST))) {
	            if (_ignoreCase) word = word.toLowerCase();
	            _blackList.add(word);
	        }
	    }

	    if (_blackList == null)
	        // We can't process anything at this point until the blacklist "arrives"
	        return;

        // Process queued tokens
        Object input;
        while ((input = componentInputCache.retrieveNext(IN_TOKENS)) != null) {
            if (input instanceof StreamDelimiter) {
                // Forward the delimiter
                cc.pushDataComponentToOutput(OUT_TOKENS, input);
                continue;
            } else
                processInputTokens(DataTypeParser.parseAsString(input));
        }

        // Process queued token counts
        while ((input = componentInputCache.retrieveNext(IN_TOKEN_COUNTS)) != null) {
            if (input instanceof StreamDelimiter) {
                // Forward the delimiter
                cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, input);
                continue;
            } else
                processInputTokenCounts(DataTypeParser.parseAsStringIntegerMap(input));
        }

        // Process queued tokenized sentences
        while ((input = componentInputCache.retrieveNext(IN_TOKENIZED_SENTENCES)) != null) {
            if (input instanceof StreamDelimiter) {
                // Forward the delimiter
                cc.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, input);
                continue;
            } else
                processInputTokenizedSentences((StringsMap) input);
        }
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _blackList = null;
    }

	//--------------------------------------------------------------------------------------------

    @Override
    public void handleStreamInitiators() throws Exception {
        executeCallBack(componentContext);
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        executeCallBack(componentContext);
    }

    //--------------------------------------------------------------------------------------------

    protected void processInputTokens(String[] tokens) throws ComponentContextException {
        int nRemovedTokens = 0;

        Strings.Builder outTokens = Strings.newBuilder();
        for (String token : tokens) {
            String s = (_ignoreCase) ? token.toLowerCase() : token;
            if (!_blackList.contains(s))
                outTokens.addValue(token);
            else
                nRemovedTokens++;
        }

        int nKeptTokens = outTokens.getValueCount();

        console.fine(String.format("tokens: Removed: %,d  Kept: %,d  Total: %,d",
                nRemovedTokens, nKeptTokens, nRemovedTokens + nKeptTokens));

        componentContext.pushDataComponentToOutput(OUT_TOKENS, outTokens.build());
    }

    protected void processInputTokenCounts(Map<String, Integer> tokenCounts) throws ComponentContextException {
        int nRemovedTokens = 0;

        IntegersMap.Builder outTokenCounts = IntegersMap.newBuilder();
        for (Entry<String, Integer> entry : tokenCounts.entrySet()) {
            String token = entry.getKey();
            String s = (_ignoreCase) ? token.toLowerCase() : token;
            if (!_blackList.contains(s)) {
                outTokenCounts.addKey(token);
                outTokenCounts.addValue(BasicDataTypesTools.integerToIntegers(entry.getValue()));
            } else
                nRemovedTokens++;
        }

        int nKeptTokens = outTokenCounts.getKeyCount();

        console.fine(String.format("tokenCounts: Removed: %,d  Kept: %,d  Total: %,d",
                nRemovedTokens, nKeptTokens, nRemovedTokens + nKeptTokens));

        componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS, outTokenCounts.build());
    }

    protected void processInputTokenizedSentences(StringsMap tokenizedSentences) throws ComponentContextException {
        int nRemovedTokens = 0;
        int nKeptTokens = 0;

        StringsMap.Builder outTokenizedSentences = StringsMap.newBuilder();
        for (int i = 0, iMax = tokenizedSentences.getKeyCount(); i < iMax; i++) {
            String sentence = tokenizedSentences.getKey(i);
            Strings tokens = tokenizedSentences.getValue(i);

            Strings.Builder filteredTokens = Strings.newBuilder();
            for (String token : tokens.getValueList()) {
                String s = (_ignoreCase) ? token.toLowerCase() : token;
                if (!_blackList.contains(s))
                    filteredTokens.addValue(token);
                else
                    nRemovedTokens++;
            }

            nKeptTokens += filteredTokens.getValueCount();

            outTokenizedSentences.addKey(sentence);
            outTokenizedSentences.addValue(filteredTokens);
        }

        console.fine(String.format("sentences: Removed: %,d  Kept: %,d  Total: %,d",
                nRemovedTokens, nKeptTokens, nRemovedTokens + nKeptTokens));

        componentContext.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, outTokenizedSentences.build());
    }
}
