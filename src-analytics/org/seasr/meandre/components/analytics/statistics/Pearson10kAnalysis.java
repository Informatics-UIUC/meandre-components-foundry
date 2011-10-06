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

package org.seasr.meandre.components.analytics.statistics;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * 
 * @author Loretta Auvil
 * 
 */

@Component(name = "Pearson 10k Analysis", creator = "Loretta Auvil", 
		baseURL = "meandre://seasr.org/components/foundry/", 
		firingPolicy = FiringPolicy.all, mode = Mode.compute, rights = Licenses.UofINCSA, 
		tags = "tuple, tools, text, pearson", 
		description = "This component takes the incoming set of tuples and compares each word to every other word based on Pearson's analysis", 
		dependency = {"trove-2.0.3.jar", "protobuf-java-2.2.0.jar" })
		
		
public class Pearson10kAnalysis extends AbstractStreamingExecutableComponent {

	// ------------------------------ INPUTS
	// ------------------------------------------------------

	// ------------------------------ OUTPUTS
	// -----------------------------------------------------

	@ComponentOutput(name = Names.PORT_QUERY, description = "The query to be sent to the database."
		+ "<br>TYPE: ")
		protected static final String OUT_QUERY = Names.PORT_QUERY;

	// ----------------------------- PROPERTIES
	// ---------------------------------------------------

	@ComponentProperty(name = Names.PROP_WRAP_STREAM, description = "Should the output be wrapped as a stream?", defaultValue = "true")
	protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

/*	@ComponentProperty(name = Names.PROP_MIN_VALUE, description = "Indicates the minimum year to be used in the analysis.", defaultValue = "1700")
	protected static final String PROP_MIN_VALUE = Names.PROP_MIN_VALUE;

	@ComponentProperty(name = Names.PROP_MAX_VALUE, description = "Indicates the maximum year to be used in the analysis.", defaultValue = "1899")
	protected static final String PROP_MAX_VALUE = Names.PROP_MAX_VALUE;
*/
	@ComponentProperty(name = "block_size", description = "Indicates the block size used for each loop.", defaultValue = "100")
	protected static final String PROP_BLOCK_SIZE = "block_size";

	@ComponentProperty(name = "num_years", description = "Indicates the number of years in the Pearson calculation", defaultValue = "200")
	protected static final String PROP_NUM_YEARS = "num_years";

	@ComponentProperty(name = Names.PROP_MAX_SIZE, description = "Indicates the maximum number of unique ngrams in the frequency table.", defaultValue = "1000")
	protected static final String PROP_MAX_SIZE = Names.PROP_MAX_SIZE;
	
	@ComponentProperty(name = "frequency_db_table", description = "Indicates the name of the frequency database table.", defaultValue = "freq")
	protected static final String PROP_FREQUENCY_TABLE = "frequency_db_table";
	
	@ComponentProperty(name = "pearson_db_table", description = "Indicates the name of the pearson database table where results will be saved.", defaultValue = "pearson")
	protected static final String PROP_PEARSON_TABLE = "pearson_db_table";


	// --------------------------------------------------------------------------------------------

	protected boolean _wrapStream;
	protected int _min_year;
	protected int _max_year;
	protected int _block_size = 100;
	protected int _num_years = 200;
	protected int _max_size = 1000;
	protected String _freq_table;
	protected String _pearson_table;
	
	// --------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp)
	throws Exception {
		super.initializeCallBack(ccp);

		_wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(
				PROP_WRAP_STREAM, ccp));
		_block_size = Integer.parseInt(getPropertyOrDieTrying(
				PROP_BLOCK_SIZE, ccp));
		_num_years = Integer.parseInt(getPropertyOrDieTrying(
				PROP_NUM_YEARS, ccp));
		_max_size = Integer.parseInt(getPropertyOrDieTrying(
				PROP_MAX_SIZE, ccp));
		_freq_table = getPropertyOrDieTrying(PROP_FREQUENCY_TABLE, ccp);
		_pearson_table = getPropertyOrDieTrying(PROP_PEARSON_TABLE, ccp);
		
/*		_min_year = Integer.parseInt(getPropertyOrDieTrying(
				PROP_MIN_VALUE, ccp));
		_max_year = Integer.parseInt(getPropertyOrDieTrying(
				PROP_MAX_VALUE, ccp));
*/	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {

		if (_wrapStream) {
			StreamDelimiter sd = new StreamInitiator(streamId);
			cc.pushDataComponentToOutput(OUT_QUERY, sd);
		}
		
		for (int i = 0; i < _max_size/_block_size; i++) {
			for (int j = i; j < _max_size/_block_size; j++) {
						console.fine("i = "+i+"j = "+ j);

						String query = "insert ignore into "+
						_pearson_table +
						" (ngram1_id, ngram2_id, ngram1, ngram2, pearson) " +
						"SELECT t1.ngram_spelling_id as ngram1_id, t2.ngram_spelling_id as ngram2_id, "+
						"t1.ngram_spelling as ngram1, t2.ngram_spelling as ngram2, stat_pmcc_samp(t1.freq, t2.freq) as pearson " +
						"from  (SELECT * FROM "+
						_freq_table +
						" order by ngram_spelling_id, year limit "+
						i*_block_size*_num_years + ", " + _block_size*_num_years +
						") t1 inner join (SELECT * FROM "+
						_freq_table +
						" order by ngram_spelling_id, year limit "+
						j*_block_size*_num_years + ", " + _block_size*_num_years +
						") t2 using (year) where t1.ngram_spelling_id < t2.ngram_spelling_id " +
						"group by t1.ngram_spelling_id, t2.ngram_spelling_id;";

						console.finer("query = "+query);
						cc.pushDataComponentToOutput(OUT_QUERY, query);
				
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
