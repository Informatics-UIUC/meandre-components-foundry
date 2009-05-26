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

package org.seasr.meandre.components.analytics.text.filters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.BasicDataTypes;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.exceptions.UnsupportedDataTypeException;
import org.seasr.meandre.support.parsers.DataTypeParser;

/** This component tokenizes the text contained in the input model using OpenNLP.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */
@SuppressWarnings("unchecked")
@Component(
		name = "Token Filter",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.any,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "semantic, tools, text, filter, tokenizer",
		description = "This component filters the tokens on the inputs based " +
				      "based on the list of tokens provided. If new tokens to " +
				      "filter are provide they either replace the current ones " +
				      "or add them to the black list. The component outputs the " +
				      "filtered tokens."
)
public class TokenFilter extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKEN_BLACKLIST,
			description = "The list of tokens defining the blacklist."
	)
	protected static final String IN_TOKEN_BLACKLIST = Names.PORT_TOKEN_BLACKLIST;

	@ComponentInput(
			name = Names.PORT_TOKENS,
			description = "The sequence of tokens to filter"
	)
	protected static final String IN_TOKENS = Names.PORT_TOKENS;

	@ComponentInput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts to filter"
	)
	protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	@ComponentInput(
			name = Names.PORT_TOKENIZED_SENTENCES,
			description = "The tokenized sentences to filter"
	)
	protected static final String IN_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKENS,
			description = "The filtered of tokens"
	)
	protected static final String OUT_TOKENS = Names.PORT_TOKENS;

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The filtered token counts"
	)
	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	@ComponentOutput(
			name = Names.PORT_TOKENIZED_SENTENCES,
			description = "The filtered tokenized sentences"
	)
	protected static final String OUT_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;

    //------------------------------ PROPERTIES --------------------------------------------------

	// Inherited PROP_IGNORE_ERRORS from AbstractExecutableComponent

    @ComponentProperty(
            name = Names.PROP_REPLACE,
            description = "If set to true errors blacklisted tokens get replaced when a new set is provided. " +
                          "When set to false, tokens keep being appended to the blacklist. ",
            defaultValue = "true"
    )
    protected static final String PROP_REPLACE = Names.PROP_REPLACE;

	//--------------------------------------------------------------------------------------------


	/** The error handling flag */
	protected boolean bIgnoreErrors;

	/** Should the token black list be replaced */
	protected boolean bReplace;

	/** The temporary initial queue */
	protected Queue<Object>[] queues  = new Queue[3];
	protected final int PORT_TOKENS = 0;
	protected final int PORT_TOKEN_COUNTS = 1;
	protected final int PORT_TOKENIZE_SENTENCES = 2;

	/** The list of available inputs */
	protected String [] saInputName = null;

	/** The set of blacklisted tokens */
	protected Set<String> setBlacklist = null;

	private Logger _console;


	//--------------------------------------------------------------------------------------------

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    _console = getConsoleLogger();

		this.bIgnoreErrors = Boolean.parseBoolean(ccp.getProperty(PROP_IGNORE_ERRORS));
		this.bReplace = Boolean.parseBoolean(ccp.getProperty(PROP_REPLACE));
		this.queues[PORT_TOKENS] = new LinkedList<Object>();
		this.queues[PORT_TOKEN_COUNTS] = new LinkedList<Object>();
		this.queues[PORT_TOKENIZE_SENTENCES] = new LinkedList<Object>();
		this.saInputName = ccp.getInputNames();
		this.setBlacklist = null;
	}

	public void executeCallBack(ComponentContext cc) throws Exception {
		if ( this.setBlacklist == null && !cc.isInputAvailable(IN_TOKEN_BLACKLIST) ) {
			// No blacklist received yet, so queue the objects
			queueObjects(cc);
		}
		else if ( this.setBlacklist == null && cc.isInputAvailable(IN_TOKEN_BLACKLIST) ) {
			// Process blacklist and pending
			processBlacklistAndQueued(cc);
		}
		else {
			// Process normally with the incoming information
			processNormally(cc);
		}
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        this.bReplace = false;
        this.queues[PORT_TOKENS] = this.queues[PORT_TOKEN_COUNTS] = this.queues[PORT_TOKENIZE_SENTENCES] = null;
        this.queues = null;
        this.saInputName = null;
        this.setBlacklist = null;
        this.bIgnoreErrors = false;
    }

	//--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators(ComponentContext cc, Set<String> inputPortsWithInitiators)
            throws ComponentContextException, ComponentExecutionException {

        if (inputPortsWithInitiators.contains(IN_TOKENIZED_SENTENCES))
            cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));

        if (inputPortsWithInitiators.contains(IN_TOKEN_COUNTS))
            cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, cc.getDataComponentFromInput(IN_TOKEN_COUNTS));

        if (inputPortsWithInitiators.contains(IN_TOKENS))
            cc.pushDataComponentToOutput(OUT_TOKENS, cc.getDataComponentFromInput(IN_TOKENS));
    }

    @Override
    protected void handleStreamTerminators(ComponentContext cc, Set<String> inputPortsWithTerminators)
            throws ComponentContextException, ComponentExecutionException {

        if (inputPortsWithTerminators.contains(IN_TOKENIZED_SENTENCES))
            cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));

        if (inputPortsWithTerminators.contains(IN_TOKEN_COUNTS))
            cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, cc.getDataComponentFromInput(IN_TOKEN_COUNTS));

        if (inputPortsWithTerminators.contains(IN_TOKENS))
            cc.pushDataComponentToOutput(OUT_TOKENS, cc.getDataComponentFromInput(IN_TOKENS));
    }

    //--------------------------------------------------------------------------------------------

	/**
	 * No blacklist currently available, just queue the objects in the inputs.
	 *
	 * @param cc The component context object
	 * @throws ComponentContextException Invalid access to the component context
	 */
	private void queueObjects(ComponentContext cc) throws ComponentContextException {
		if ( cc.isInputAvailable(IN_TOKENS) )
			this.queues[PORT_TOKENS].offer(cc.getDataComponentFromInput(IN_TOKENS));
		if ( cc.isInputAvailable(IN_TOKEN_COUNTS) )
			this.queues[PORT_TOKEN_COUNTS].offer(cc.getDataComponentFromInput(IN_TOKEN_COUNTS));
		if ( cc.isInputAvailable(IN_TOKENIZED_SENTENCES) )
			this.queues[PORT_TOKENIZE_SENTENCES].offer(cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));
	}

	/**
	 * Process the black list and catches up with the pending objects.
	 *
	 * @param cc The component context object
	 * @throws Exception Thrown if something goes wrong
	 */
	protected void processBlacklistAndQueued(ComponentContext cc) throws Exception {
		processBlacklist(cc.getDataComponentFromInput(IN_TOKEN_BLACKLIST), cc);
		processQueuedObjects(cc);
	}

	/**
	 * Process the blacklist.
	 *
	 * @param objBlackList The black list to process
	 * @param cc The component context
	 * @throws Exception Thrown if something goes wrong
	 */
	protected void processBlacklist(Object objBlackList, ComponentContext cc ) throws Exception {
	    String[] words = DataTypeParser.parseAsString(objBlackList);
		if ( this.setBlacklist==null )
			this.setBlacklist = new HashSet<String>(100);
		if ( this.bReplace )
			this.setBlacklist.clear();
		for ( String s : words )
			this.setBlacklist.add(s);
	}

	/**
	 * Process the queued object.
	 *
	 * @param cc The component context
	 * @throws ComponentContextException Something went wrong while executing
	 * @throws ComponentExecutionException Invalid casts
	 */
	protected void processQueuedObjects(ComponentContext cc) throws Exception {
		Iterator<Object> iterTok = this.queues[PORT_TOKENS].iterator();
		while ( iterTok.hasNext() ) processTokens(iterTok.next(),cc);

		Iterator<Object> iterTokCnts = this.queues[PORT_TOKEN_COUNTS].iterator();
		while ( iterTokCnts.hasNext() ) processTokenCounts(iterTokCnts.next(),cc);

		Iterator<Object> iterTS = this.queues[PORT_TOKENIZE_SENTENCES].iterator();
		while ( iterTS.hasNext() ) processTokenizedSentences(iterTS.next(),cc);
	}


	/**
	 * Process the inputs normally.
	 *
	 * @param cc The component context
	 * @throws ComponentExecutionException Problem while executing
	 * @throws ComponentContextException  The component context was improperly exception
	 *
	 */
	protected void processNormally(ComponentContext cc) throws Exception {
		if ( cc.isInputAvailable(IN_TOKEN_BLACKLIST)) {
			processBlacklist(cc.getDataComponentFromInput(IN_TOKEN_BLACKLIST),cc);
		}
		if ( cc.isInputAvailable(IN_TOKENS) ) {
			processTokens(cc.getDataComponentFromInput(IN_TOKENS),cc);
		}

		if ( cc.isInputAvailable(IN_TOKEN_COUNTS) ) {
			processTokenCounts(cc.getDataComponentFromInput(IN_TOKEN_COUNTS),cc);
		}

		if ( cc.isInputAvailable(IN_TOKENIZED_SENTENCES) ) {
			processTokenizedSentences(cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES),cc);
		}
	}

	/**
	 * Process tokenized sentences.
	 *
	 * @param next The object to process
	 * @param cc The component context
	 * @throws ComponentContextException Invalid access to the component context
	 * @throws ComponentExecutionException Invalid cast
	 */
	protected void processTokenizedSentences(Object next, ComponentContext cc) throws Exception {
		StringsMap im = safeStringsMapCast(next, cc);
		org.seasr.datatypes.BasicDataTypes.StringsMap.Builder res = BasicDataTypes.StringsMap.newBuilder();
		for ( int i=0, iMax=im.getKeyCount() ; i<iMax ; i++ ) {
			String sKey = im.getKey(i);
			Strings sVals = im.getValue(i);
			org.seasr.datatypes.BasicDataTypes.Strings.Builder resFiltered = BasicDataTypes.Strings.newBuilder();
			for ( String s:sVals.getValueList())
				if ( !this.setBlacklist.contains(s) )
					resFiltered.addValue(s);
			res.addKey(sKey);
			res.addValue(resFiltered.build());
		}
		cc.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, res.build());
	}

	/**
	 * Process token counts sentences.
	 *
	 * @param next The object to process
	 * @param cc The component context
	 * @throws ComponentContextException Invalid access to the component context
	 * @throws ComponentExecutionException Class cast exception
	 */
	protected void processTokenCounts(Object next, ComponentContext cc) throws Exception {
		Map<String, Integer> im;
		try {
		    im = DataTypeParser.parseAsStringIntegerMap(next);
		}
		catch (UnsupportedDataTypeException e) {
		    if (bIgnoreErrors) {
		        _console.warning("processTokenCounts: UnsupportedDataTypeException ignored - input data was not in the correct format");
		        im = new HashMap<String, Integer>();
		    }
		    else
		        throw e;
		}

		Map<String, Integer> res = new HashMap<String, Integer>();
		for ( Entry<String, Integer> entry : im.entrySet()) {
			String sKey = entry.getKey();
			Integer iVal = entry.getValue();
			if ( !this.setBlacklist.contains(sKey) )
				res.put(sKey, iVal);
		}

		cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(res, false));
	}

	/**
	 * Process tokens sentences.
	 *
	 * @param next The object to process
	 * @param cc The component context
	 * @throws Exception Invalid cast
	 */
	private void processTokens(Object next, ComponentContext cc) throws Exception {
		String[] tokens;
		try {
            tokens = DataTypeParser.parseAsString(next);
        }
        catch (UnsupportedDataTypeException e) {
            if (bIgnoreErrors) {
                _console.warning("processTokens: UnsupportedDataTypeException ignored - input data was not in the correct format");
                tokens = new String[] {};
            }
            else
                throw e;
        }

		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
		for ( String sTok : tokens )
			if ( !this.setBlacklist.contains(sTok) )
				res.addValue(sTok);

		cc.pushDataComponentToOutput(OUT_TOKENS, res.build());
	}

	/**
	 * Safe cast of strings map.
	 *
	 * @param next The object to cast into strings
	 * @param cc The component context
	 * @return The strings
	 * @throws ComponentExecutionException
	 */
	private StringsMap safeStringsMapCast(Object next, ComponentContext cc) throws Exception {
		try {
			return (StringsMap)next;
		}
		catch ( ClassCastException e ) {
			_console.warning("Input data is not from the basic type Strings required for blacklists");
			if ( !bIgnoreErrors )
				throw new ComponentExecutionException(e);
			return BasicDataTypesTools.buildEmptyStringsMap();
		}
	}
}
