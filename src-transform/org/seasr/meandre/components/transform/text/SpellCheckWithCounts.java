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

package org.seasr.meandre.components.transform.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.core.ComponentContext;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.support.generic.util.KeyValuePair;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.event.SpellChecker;

/**
 *
 * @author Boris Capitanu
 *
 */

@Component(
        creator = "Boris Capitanu",
        description = "Performs spell checking on the input and optionally replaces misspelled words " +
                      "with the top ranked suggestion based on the supplied token counts. " + "" +
                      "The component also produces a list of the misspellings " +
                      "in the document.",
        name = "Spell Check with Counts",
        tags = "dictionary, word, spell check, token count",
        firingPolicy = FiringPolicy.any,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar", "jazzy-core.jar"}
)
public class SpellCheckWithCounts extends SpellCheck {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The token counts used for figuring out the most probable replacement for " +
                          "a misspelled word"
    )
    protected static final String IN_TOKEN_COUTNS = Names.PORT_TOKEN_COUNTS;

    @ComponentInput(
            name = "transformations",
            description = "The transformations that should be tried on misspelled words before taking the spell checker's suggestions"
    )
    protected static final String IN_TRANSFORMATIONS = "transformations";

    //--------------------------------------------------------------------------------------------

    protected Map<String, Integer> _tokenCounts;
    protected Map<String, String> _transformations;

    //--------------------------------------------------------------------------------------------

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (cc.isInputAvailable(IN_TOKEN_COUTNS))
            _tokenCounts = DataTypeParser.parseAsStringIntegerMap(cc.getDataComponentFromInput(IN_TOKEN_COUTNS));

        if (cc.isInputAvailable(IN_TRANSFORMATIONS)) {
            String[] inputs = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TRANSFORMATIONS));
            _transformations = TextReplacement.buildDictionary(inputs[0], console);
        }

        super.executeCallBack(cc);
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected boolean isReadyToProcessInputs() {
        return _tokenCounts != null && _transformations != null && super.isReadyToProcessInputs();
    }

    @Override
    protected SuggestionListener getSuggestionListener() {
        return new SuggestionListenerWithCounts(_spellDictionary, _tokenCounts, _doCorrection, console);
    }

    //--------------------------------------------------------------------------------------------

    public class SuggestionListenerWithCounts extends SuggestionListener {

        private final Map<String,Integer> _tokenCounts;
        private final SpellChecker _spellChecker;

        public SuggestionListenerWithCounts(SpellDictionary dictionary, Map<String,Integer> tokenCounts, boolean doCorrection) {
            this(dictionary, tokenCounts, doCorrection, null);
        }

        public SuggestionListenerWithCounts(SpellDictionary dictionary, Map<String,Integer> tokenCounts, boolean doCorrection, Logger logger) {
            super(doCorrection, logger);

            _tokenCounts = tokenCounts;
            _spellChecker = new SpellChecker(dictionary);
        }

        @Override
        protected String getReplacement(String invalidWord, List<?> suggestions) {
            List<KeyValuePair<Integer,Entry<String,String>>> transformData =
                computeApplicableTransformations(invalidWord, _transformations);

            List<String> transformSuggestions = checkTransformations(invalidWord, transformData);
            if (transformSuggestions.size() > 0)
                console.fine("Transform suggestions: " + transformSuggestions);

            if (transformSuggestions.size() == 1)
                return transformSuggestions.get(0);

            String replacement = null;
            int maxCount = 0;

            if (transformSuggestions.size() > 0)
                suggestions = transformSuggestions;

            for (Object o : suggestions) {
                String suggestion = o.toString();
                Integer count = _tokenCounts.get(suggestion);
                if (count != null && count > maxCount) {
                    maxCount = count;
                    replacement = suggestion;
                }
            }

            if (replacement == null) {
                if (_logger != null)
                    _logger.finer(String.format("None of the suggestions for the misspelled word '%s' " +
                            "has been found in the supplied token counts. Using first suggestion.", invalidWord));

                replacement = super.getReplacement(invalidWord, suggestions);
            }

            return replacement;
        }

        private List<KeyValuePair<Integer,Entry<String,String>>> computeApplicableTransformations(String invalidWord, Map<String,String> tokens) {
            List<KeyValuePair<Integer, Entry<String,String>>> transformations = new ArrayList<KeyValuePair<Integer,Entry<String,String>>>();

            for (Entry<String,String> entry : tokens.entrySet()) {
                int n;

                for (int i = 0; (n = invalidWord.indexOf(entry.getKey(), i)) != -1; i = n + 1)
                    transformations.add(new KeyValuePair<Integer, Entry<String,String>>(n, entry));
            }

            Collections.sort(transformations, new Comparator<KeyValuePair<Integer,Entry<String,String>>>() {
                public int compare(KeyValuePair<Integer, Entry<String, String>> o1, KeyValuePair<Integer, Entry<String, String>> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });

            return transformations;
        }

        private List<String> checkTransformations(String invalidWord, List<KeyValuePair<Integer, Entry<String, String>>> transformData) {
            List<String> suggestions = new ArrayList<String>();

            for (int i = 0, size = transformData.size(), iMax = (int) Math.pow(2, size); i < iMax; i++) {
                int lastIndex = -1;
                String suggestion = invalidWord;
                int adjust = 0, countTransformations = 0;
                boolean abort = false;

                for (int j = 1; j <= i; j *= 2)
                    if ((i & j) == j) {
                        int index = (int)(Math.log(j) / Math.log(2));

                        KeyValuePair<Integer,Entry<String,String>> kvp = transformData.get(index);
                        int n = kvp.getKey();
                        if (n < lastIndex) {
                            abort = true;
                            break;
                        }

                        Entry<String,String> transform = kvp.getValue();
                        lastIndex = n + transform.getKey().length();

                        n += adjust; lastIndex += adjust; countTransformations++;
                        suggestion = suggestion.substring(0, n) + transform.getValue() + suggestion.substring(lastIndex);
                        adjust += transform.getValue().length() - transform.getKey().length();
                    }

                if (abort) continue;

                if (_spellChecker.isCorrect(suggestion)) {
                    suggestions.add(suggestion);
                    console.finer(String.format("Transformed '%s' into '%s' after %d transformations", invalidWord, suggestion, countTransformations));
                } else
                    console.finest(String.format("Discarding transformation '%s' - not found in dictionary", suggestion));
            }

            return suggestions;
        }
    }
}
