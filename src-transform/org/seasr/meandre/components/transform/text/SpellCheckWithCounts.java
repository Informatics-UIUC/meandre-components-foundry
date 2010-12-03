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

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.core.ComponentContext;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;

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
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class SpellCheckWithCounts extends SpellCheck {
    
    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The token counts used for figuring out the most probable replacement for " +
                          "a misspelled word"
    )
    protected static final String IN_TOKEN_COUTNS = Names.PORT_TOKEN_COUNTS;
    
    //--------------------------------------------------------------------------------------------

    protected Map<String, Integer> _tokenCounts;
    
    //--------------------------------------------------------------------------------------------

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (cc.isInputAvailable(IN_TOKEN_COUTNS))
            _tokenCounts = DataTypeParser.parseAsStringIntegerMap(cc.getDataComponentFromInput(IN_TOKEN_COUTNS));
        
        super.executeCallBack(cc);
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected boolean isReadyToProcessInputs() {
        return _tokenCounts != null && super.isReadyToProcessInputs();
    }
    
    @Override
    protected SuggestionListener getSuggestionListener() {
        return new SuggestionListenerWithCounts(_tokenCounts, _doCorrection, console);
    }
    
    //--------------------------------------------------------------------------------------------

    public class SuggestionListenerWithCounts extends SuggestionListener {
        
        private final Map<String,Integer> _tokenCounts;
        
        public SuggestionListenerWithCounts(Map<String,Integer> tokenCounts, boolean doCorrection) {
            this(tokenCounts, doCorrection, null);
        }
        
        public SuggestionListenerWithCounts(Map<String,Integer> tokenCounts, boolean doCorrection, Logger logger) {
            super(doCorrection, logger);
            
            _tokenCounts = tokenCounts;
        }
        
        @Override
        protected String getReplacement(String invalidWord, List<?> suggestions) {
            String replacement = null;
            int maxCount = 0;
            
            for (Object o : suggestions) {
                String suggestion = o.toString();
                Integer count = _tokenCounts.get(suggestion);
                if (count != null && count > maxCount) {
                    maxCount = count;
                    replacement = suggestion;
                }
            }
            
            if (replacement == null) {
                _logger.warning(String.format("None of the suggestions for the misspelled word '%s' " +
                		"has been found in the supplied token counts. Using first suggestion.", invalidWord));
                replacement = super.getReplacement(invalidWord, suggestions);
            }
            
            return replacement;
        }
    }
}
