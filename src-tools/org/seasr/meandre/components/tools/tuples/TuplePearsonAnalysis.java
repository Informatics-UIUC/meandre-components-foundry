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
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 * 
 * @author Loretta Auvil
 * 
 */

@Component(name = "Tuple Pearson Analysis", creator = "Loretta Auvil", 
		baseURL = "meandre://seasr.org/components/foundry/", 
		firingPolicy = FiringPolicy.all, mode = Mode.compute, 
		rights = Licenses.UofINCSA, 
		tags = "#ANALYTICS, tuple, tools, text, pearson", 
		description = "This component takes the incoming set of tuples and compares each word to every other word based on Pearson's analysis", 
		dependency = {"trove-2.0.3.jar", "protobuf-java-2.2.0.jar" })
		
		public class TuplePearsonAnalysis extends AbstractStreamingExecutableComponent {

	// ------------------------------ INPUTS
	// ------------------------------------------------------

	@ComponentInput(name = Names.PORT_TUPLES, description = "The tuple(s)"
		+ "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
		+ "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray")
		protected static final String IN_TUPLES = Names.PORT_TUPLES;

	@ComponentInput(name = Names.PORT_META_TUPLE, description = "The meta data for tuples"
		+ "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings")
		protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

	@ComponentInput(name = "tuples_list_2", description = "The tuple(s) for list 2"
		+ "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
		+ "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray")
		protected static final String IN_TUPLES_2 = "tuples_list_2";

	@ComponentInput(name = "meta_tuple_list_2", description = "The meta data for tuples for list 2"
		+ "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings")
		protected static final String IN_META_TUPLE_2 = "meta_tuple_list_2";
	// ------------------------------ OUTPUTS
	// -----------------------------------------------------

	@ComponentOutput(name = Names.PORT_QUERY, description = "The query to be sent to the database."
		+ "<br>TYPE: ")
		protected static final String OUT_QUERY = Names.PORT_QUERY;

	// ----------------------------- PROPERTIES
	// ---------------------------------------------------

	@ComponentProperty(name = Names.PROP_WRAP_STREAM, description = "Should the output be wrapped as a stream?", defaultValue = "true")
	protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	@ComponentProperty(name = Names.PROP_MIN_VALUE, description = "Indicates the minimum year to be used in the analysis.", defaultValue = "1700")
	protected static final String PROP_MIN_VALUE = Names.PROP_MIN_VALUE;

	@ComponentProperty(name = Names.PROP_MAX_VALUE, description = "Indicates the maximum year to be used in the analysis.", defaultValue = "1899")
	protected static final String PROP_MAX_VALUE = Names.PROP_MAX_VALUE;

	private static final Boolean FALSE = null;

	private static final Boolean TRUE = null;

	// --------------------------------------------------------------------------------------------

	protected boolean _wrapStream;
	protected int _min_year;
	protected int _max_year;

	// --------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp)
	throws Exception {
		super.initializeCallBack(ccp);

		_wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(
				PROP_WRAP_STREAM, ccp));
		_min_year = Integer.parseInt(getPropertyOrDieTrying(
				PROP_MIN_VALUE, ccp));
		_max_year = Integer.parseInt(getPropertyOrDieTrying(
				PROP_MAX_VALUE, ccp));
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer inPeer = new SimpleTuplePeer(inputMeta);

		Object input = cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] tuples;

		Strings inputMeta_2 = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE_2);
		SimpleTuplePeer inPeer_2 = new SimpleTuplePeer(inputMeta_2);

		Object input_2 = cc.getDataComponentFromInput(IN_TUPLES_2);
		Strings[] tuples_2;

		if (input instanceof StringsArray)
			tuples = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) input);
		else
			//this is only using one tuple instead, so not useful
			if (input instanceof Strings) {
				Strings inTuple = (Strings) input;
				tuples = new Strings[] { inTuple };
			} else
				throw new ComponentExecutionException(
						"Don't know how to handle input of type: "
						+ input.getClass().getName());

		if (input_2 instanceof StringsArray)
			tuples_2 = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) input_2);
		else
			//this is only using one tuple instead, so not useful
			if (input_2 instanceof Strings) {
				Strings inTuple_2 = (Strings) input_2;
				tuples_2 = new Strings[] { inTuple_2 };
			} else
				throw new ComponentExecutionException(
						"Don't know how to handle input of type: "
						+ input.getClass().getName());

		if (_wrapStream) {
			StreamDelimiter sd = new StreamInitiator(streamId);
			cc.pushDataComponentToOutput(OUT_QUERY, sd);
		}

		SimpleTuple tuple_i = inPeer.createTuple();
		SimpleTuple tuple_j = inPeer_2.createTuple();
		SimpleTuple tuple_k = inPeer.createTuple();

		for (int i = 0; i < tuples.length; i++) {
			tuple_i.setValues(tuples[i]);
			String word_i = tuple_i.getValue(0);
			for (int j = 0; j < tuples_2.length; j++) {
				tuple_j.setValues(tuples_2[j]);
				String word_j = tuple_j.getValue(0);
				// skip the calculation if the word in list 1 is the same as the word in list 2
				if (!word_i.equalsIgnoreCase(word_j)) {
					
					// skip the calculation if we have already made the comparison of the word in list 2 because it occurs earlier in list 1
					boolean found = false;
					for (int k=0; k<i; k++) {
						tuple_k.setValues(tuples[k]);
						String word_k = tuple_k.getValue(0);
						if (word_j.equalsIgnoreCase(word_k)) {
							found = true;
							break;
						}
					}

					if (!found){

						console.fine("word 1 = " + tuple_i.toString() + " word 2 = " + tuple_j.toString());

						String query = "SELECT \"" 
						+tuple_i.toString()
						+"\" as ngram1, \""
						+tuple_j.toString()
						+"\" as ngram2, stat_pmcc_samp(n1, n2) as pearson_analysis from "
						+"(SELECT year, max(if(ngram_label=\""
						+tuple_i.toString()
						+"\",y,0)) as n1, max(if(ngram_label=\"" 
						+tuple_j.toString()
						+"\",y,0)) as n2 from "
						+"(SELECT ngram_label, year, sum(match_count) / sum_match_count * 1000000 as y "
						+"FROM ("
						+"SELECT ngram, ifnull(ngram_spelling_checked,ngram_normalized) as ngram_label "
						+"FROM ngram_summary ns "
						+"WHERE ngram_normalized IN (\""
						+tuple_i.toString()
						+"\",\"" 
						+tuple_j.toString()
						+"\") OR ngram_spelling_checked IN (\""
						+tuple_i.toString()
						+"\",\"" 
						+tuple_j.toString()
						+"\")) t1 "
						+"LEFT JOIN ngrams n USING(ngram) LEFT JOIN yearly_summary s USING(year) "
						+"GROUP BY ngram_label, year HAVING year >= "+ _min_year+" AND year <= "+_max_year+") t2 group by year) t3;";

						console.finer("query = "+query);
						cc.pushDataComponentToOutput(OUT_QUERY, query);
					}
				}
			}
		}

		if (_wrapStream) {
			StreamDelimiter sd = new StreamTerminator(streamId);
			cc.pushDataComponentToOutput(OUT_QUERY, sd);
		}
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp)
	throws Exception {
	}

	// --------------------------------------------------------------------------------------------

	@Override
	public void handleStreamInitiators() throws Exception {
		console.finer("### BEGIN marker ###");
		super.handleStreamInitiators();
	}

	@Override
	public void handleStreamTerminators() throws Exception {
		console.finer("### END marker ###");
		super.handleStreamTerminators();
	}

	@Override
	public boolean isAccumulator() {
		// TODO Auto-generated method stub
		return false;
	}
}
