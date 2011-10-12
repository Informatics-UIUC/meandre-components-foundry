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
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * @author Loretta Auvil
 * @author Boris Capitanu
 */

@Component(
        name = "Compute Pearson",
        creator = "Loretta Auvil",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "tools, db, pearson",
        description = "This component generates the necessary SQL queries to compute Pearson for the data in the specified table",
        dependency = { "protobuf-java-2.2.0.jar"}
)
public class ComputePearson extends AbstractStreamingExecutableComponent {

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        name = Names.PORT_QUERY,
	        description = "The query to be sent to the database." +
	            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
	protected static final String OUT_QUERY = Names.PORT_QUERY;

    //----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
	        name = Names.PROP_WRAP_STREAM,
	        description = "Should the output be wrapped as a stream?",
	        defaultValue = "true"
	)
	protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	@ComponentProperty(
	        name = "block_size",
	        description = "The block size used for each loop.",
	        defaultValue = "100"
    )
	protected static final String PROP_BLOCK_SIZE = "block_size";

	@ComponentProperty(
	        name = "num_years",
	        description = "The number of years in the Pearson calculation",
	        defaultValue = "200"
    )
	protected static final String PROP_NUM_YEARS = "num_years";

	@ComponentProperty(
	        name = Names.PROP_MAX_SIZE,
	        description = "The total number of unique ngrams in the frequency table.",
	        defaultValue = "10000"
    )
	protected static final String PROP_MAX_SIZE = Names.PROP_MAX_SIZE;

	@ComponentProperty(
	        name = "db_frequency_table",
	        description = "The name of the frequency database table.",
	        defaultValue = "freq"
    )
	protected static final String PROP_FREQUENCY_TABLE = "db_frequency_table";

	@ComponentProperty(
	        name = "db_pearson_table",
	        description = "The name of the pearson database table where results will be saved.",
	        defaultValue = "pearson"
    )
	protected static final String PROP_PEARSON_TABLE = "db_pearson_table";

	// --------------------------------------------------------------------------------------------


	protected boolean _wrapStream;
	protected int _blockSize;
	protected int _numYears;
	protected int _maxSize;
	protected String _freqTable;
	protected String _pearsonTable;


	// --------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

		_wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
		_blockSize = Integer.parseInt(getPropertyOrDieTrying(PROP_BLOCK_SIZE, ccp));
		_numYears = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_YEARS, ccp));
		_maxSize = Integer.parseInt(getPropertyOrDieTrying(PROP_MAX_SIZE, ccp));
		_freqTable = getPropertyOrDieTrying(PROP_FREQUENCY_TABLE, ccp);
		_pearsonTable = getPropertyOrDieTrying(PROP_PEARSON_TABLE, ccp);

		if (_maxSize % _blockSize != 0)
		    throw new ComponentContextException("The block size specified does not exactly divide the maximum size! Please fix and re-run!");
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		if (_wrapStream) {
			StreamDelimiter sd = new StreamInitiator(streamId);
			cc.pushDataComponentToOutput(OUT_QUERY, sd);
		}

		int nBlocks = _maxSize / _blockSize;

		for (int i = 0; i < nBlocks; i++) {
		    String t1 = String.format("bi_%d", i+1);
            String q1 =
                    String.format("CREATE TEMPORARY TABLE %s" +
                                  "  SELECT ngram_spelling_id, year, freq" +
                                  "  FROM %s ORDER BY ngram_spelling_id LIMIT %d, %d;",
                                  t1, _freqTable, i * _blockSize * _numYears, _blockSize * _numYears);
            String c1_year =
                    String.format("CREATE INDEX %s ON %s (year);", String.format("%s_year_idx", t1), t1);

            StringBuilder sb = new StringBuilder();
            sb.append(q1).append("\n").append(c1_year).append("\n");

		    for (int j = i; j < nBlocks; j++) {
		        String t2 = String.format("bj_%d_%d", i+1, j+1);
	            String q2 =
	                    String.format("CREATE TEMPORARY TABLE %s" +
	                                  "  SELECT ngram_spelling_id, year, freq" +
	                                  "  FROM %s ORDER BY ngram_spelling_id LIMIT %d, %d;",
	                                  t2, _freqTable, j * _blockSize * _numYears, _blockSize * _numYears);
	            String c2_year =
	                    String.format("CREATE INDEX %s ON %s (year);", String.format("%s_year_idx", t2), t2);

		        String pearson =
		                String.format("INSERT INTO %s (ngram1_id, ngram2_id, pearson)" +
		                              "  SELECT t1.ngram_spelling_id as ngram1_id, t2.ngram_spelling_id as ngram2_id," +
		                              "         stat_pmcc_samp(t1.freq, t2.freq) as pearson" +
		                              "  FROM" +
		                              "         %s t1 INNER JOIN %s t2" +
		                              "            ON (t1.year = t2.year AND t1.ngram_spelling_id < t2.ngram_spelling_id)" +
		                              "  GROUP BY" +
		                              "         t1.ngram_spelling_id, t2.ngram_spelling_id;",
		                              _pearsonTable, t1, t2);

		        sb.append(q2).append("\n").append(c2_year).append("\n").append(pearson).append("\n");
			}

		    String query = sb.toString();
            console.fine("Pushing query: " + query);
            cc.pushDataComponentToOutput(OUT_QUERY, query);
		}

		if (_wrapStream) {
			StreamDelimiter sd = new StreamTerminator(streamId);
			cc.pushDataComponentToOutput(OUT_QUERY, sd);
		}
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	// --------------------------------------------------------------------------------------------

	@Override
	public boolean isAccumulator() {
		return false;
	}
}
