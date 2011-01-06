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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.exceptions.UnsupportedDataTypeException;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/** This component tokenizes the text contained in the input model using OpenNLP.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */

@Component(
		name = "Token Filter",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.any,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "text, filter, token, token count, tokenized sentence, word",
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
@SuppressWarnings("unchecked")
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


	/** Should the token black list be replaced */
	protected boolean bReplace;

	protected boolean bIgnoreCase;

	/** The temporary initial queue */
	protected Queue<Object>[] queues  = new Queue[3];
	protected final int PORT_TOKENS = 0;
	protected final int PORT_TOKEN_COUNTS = 1;
	protected final int PORT_TOKENIZED_SENTENCES = 2;

	/** The set of blacklisted tokens */
	protected Set<String> setBlacklist = null;

	protected Map<String,StreamTerminator> stMap = new HashMap<String,StreamTerminator>();


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    bIgnoreCase = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_IGNORE_CASE, true, true, ccp));
		bReplace = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_REPLACE, true, true, ccp));
		queues[PORT_TOKENS] = new LinkedList<Object>();
		queues[PORT_TOKEN_COUNTS] = new LinkedList<Object>();
		queues[PORT_TOKENIZED_SENTENCES] = new LinkedList<Object>();
		setBlacklist = null;
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
	    if (cc.isInputAvailable(IN_TOKEN_BLACKLIST)) {
	        String[] words = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TOKEN_BLACKLIST));

	        if (setBlacklist == null)
	            setBlacklist = new HashSet<String>(100);

	        if (bReplace) setBlacklist.clear();

	        for (String s : words) {
	            if (bIgnoreCase) s = s.toLowerCase();
	            setBlacklist.add(s.trim());
	        }
	    }

	    if (cc.isInputAvailable(IN_TOKENS))
	        queues[PORT_TOKENS].offer(cc.getDataComponentFromInput(IN_TOKENS));

	    if (cc.isInputAvailable(IN_TOKEN_COUNTS) )
	        queues[PORT_TOKEN_COUNTS].offer(cc.getDataComponentFromInput(IN_TOKEN_COUNTS));

	    if (cc.isInputAvailable(IN_TOKENIZED_SENTENCES) )
	        queues[PORT_TOKENIZED_SENTENCES].offer(cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));

	    if (setBlacklist != null)
	        processQueuedObjects();

        // Check if we already got a terminator and forward it after the data has been processed
        if (stMap.size() > 0 && this.setBlacklist != null) {
            for (Entry<String,StreamTerminator> entry : stMap.entrySet())
                componentContext.pushDataComponentToOutput(entry.getKey(), entry.getValue());
            stMap.clear();
        }
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        bReplace = false;
        queues[PORT_TOKENS] = queues[PORT_TOKEN_COUNTS] = queues[PORT_TOKENIZED_SENTENCES] = null;
        queues = null;
        setBlacklist = null;
        stMap.clear();
    }

	//--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        if (inputPortsWithInitiators.contains(IN_TOKENIZED_SENTENCES))
            componentContext.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES,
                    componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));

        if (inputPortsWithInitiators.contains(IN_TOKEN_COUNTS))
            componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS,
                    componentContext.getDataComponentFromInput(IN_TOKEN_COUNTS));

        if (inputPortsWithInitiators.contains(IN_TOKENS))
            componentContext.pushDataComponentToOutput(OUT_TOKENS,
                    componentContext.getDataComponentFromInput(IN_TOKENS));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (inputPortsWithTerminators.contains(IN_TOKENIZED_SENTENCES))
            stMap.put(OUT_TOKENIZED_SENTENCES, (StreamTerminator)componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));

        if (inputPortsWithTerminators.contains(IN_TOKEN_COUNTS))
            stMap.put(OUT_TOKEN_COUNTS, (StreamTerminator)componentContext.getDataComponentFromInput(IN_TOKEN_COUNTS));

        if (inputPortsWithTerminators.contains(IN_TOKENS))
            stMap.put(OUT_TOKENS, (StreamTerminator)componentContext.getDataComponentFromInput(IN_TOKENS));

        if (this.setBlacklist != null) {
            for (Entry<String,StreamTerminator> entry : stMap.entrySet())
                componentContext.pushDataComponentToOutput(entry.getKey(), entry.getValue());
            stMap.clear();
        }
    }

    //--------------------------------------------------------------------------------------------

	/**
	 * Process the queued object.
	 *
	 * @throws Exception Something went wrong while executing
	 */
	protected void processQueuedObjects() throws Exception {
		Iterator<Object> iterTok = queues[PORT_TOKENS].iterator();
		while (iterTok.hasNext()) processTokens(iterTok.next());

		Iterator<Object> iterTokCnts = queues[PORT_TOKEN_COUNTS].iterator();
		while (iterTokCnts.hasNext()) processTokenCounts(iterTokCnts.next());

		Iterator<Object> iterTS = queues[PORT_TOKENIZED_SENTENCES].iterator();
		while (iterTS.hasNext()) processTokenizedSentences(iterTS.next());
	}

	/**
	 * Process tokenized sentences.
	 *
	 * @param next The object to process
	 * @throws Exception
	 */
	protected void processTokenizedSentences(Object next) throws Exception {
	    int nFilteredTokens = 0;
	    int nKeptTokens = 0;

		StringsMap im = safeStringsMapCast(next);
		org.seasr.datatypes.core.BasicDataTypes.StringsMap.Builder res = BasicDataTypes.StringsMap.newBuilder();
		for ( int i=0, iMax=im.getKeyCount() ; i<iMax ; i++ ) {
			String sKey = im.getKey(i);
			Strings sVals = im.getValue(i);
			org.seasr.datatypes.core.BasicDataTypes.Strings.Builder resFiltered = BasicDataTypes.Strings.newBuilder();
			for ( String s:sVals.getValueList()) {
			    String token = (bIgnoreCase) ? s.toLowerCase().trim() : s.trim();
				if ( !this.setBlacklist.contains(token) )
					resFiltered.addValue(s);
				else
				    nFilteredTokens++;
			}

			nKeptTokens += resFiltered.getValueCount();

			res.addKey(sKey);
			res.addValue(resFiltered.build());
		}

		console.fine(String.format("sentences: Filtered: %,d  Kept: %,d  Total: %,d",
		        nFilteredTokens, nKeptTokens, nFilteredTokens + nKeptTokens));

		componentContext.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, res.build());
	}

	/**
	 * Process token counts sentences.
	 *
	 * @param next The object to process
	 * @throws Exception
	 */
	protected void processTokenCounts(Object next) throws Exception {
		Map<String, Integer> im;
		try {
		    im = DataTypeParser.parseAsStringIntegerMap(next);
		}
		catch (UnsupportedDataTypeException e) {
		    if (ignoreErrors) {
		        console.warning("processTokenCounts: UnsupportedDataTypeException ignored - input data was not in the correct format");
		        im = new HashMap<String, Integer>();
		    }
		    else
		        throw e;
		}

		int nFilteredTokens = 0;

		Map<String, Integer> res = new HashMap<String, Integer>();
		for ( Entry<String, Integer> entry : im.entrySet()) {
			String sKey = entry.getKey();
			Integer iVal = entry.getValue();
			String token = (bIgnoreCase) ? sKey.toLowerCase().trim() : sKey.trim();
			if ( !this.setBlacklist.contains(token) )
				res.put(sKey, iVal);
			else
			    nFilteredTokens++;
		}

		console.fine(String.format("counts: Filtered: %,d  Kept: %,d  Total: %,d",
		        nFilteredTokens, res.size(), nFilteredTokens + res.size()));

		componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(res, false));
	}

	/**
	 * Process tokens sentences.
	 *
	 * @param next The object to process
	 * @throws Exception
	 */
	private void processTokens(Object next) throws Exception {
		String[] tokens;
		try {
            tokens = DataTypeParser.parseAsString(next);
        }
        catch (UnsupportedDataTypeException e) {
            if (ignoreErrors) {
                console.warning("processTokens: UnsupportedDataTypeException ignored - input data was not in the correct format");
                tokens = new String[] {};
            }
            else
                throw e;
        }

        int nFilteredTokens = 0;

		org.seasr.datatypes.core.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
		for ( String sTok : tokens ) {
		    String token = (bIgnoreCase) ? sTok.toLowerCase().trim() : sTok.trim();
			if ( !this.setBlacklist.contains(token) )
				res.addValue(sTok);
			else
			    nFilteredTokens++;
		}

		console.fine(String.format("tokens: Filtered: %,d  Kept: %,d  Total: %,d",
		        nFilteredTokens, res.getValueCount(), nFilteredTokens + res.getValueCount()));

		componentContext.pushDataComponentToOutput(OUT_TOKENS, res.build());
	}

	/**
	 * Safe cast of strings map.
	 *
	 * @param next The object to cast into strings
	 * @return The strings
	 * @throws Exception
	 */
	private StringsMap safeStringsMapCast(Object next) throws Exception {
		try {
			return (StringsMap)next;
		}
		catch ( ClassCastException e ) {
			console.warning("Input data is not from the basic type Strings required for blacklists");
			if ( !ignoreErrors )
				throw new ComponentExecutionException(e);
			return BasicDataTypesTools.buildEmptyStringsMap();
		}
	}
}
