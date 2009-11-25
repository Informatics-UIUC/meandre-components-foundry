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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.monkproject.utils.CountMapUtils;
import org.monkproject.utils.ReverseScoredString;
import org.monkproject.utils.corpuslinguistics.Frequency;
import org.monkproject.utils.math.ArithUtils;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;


/**
 * @author Amit Kumar
 * @author Loretta Auvil
 * @author Lily Dong
 * @author Boris Capitanu
 */

@Component(
        creator = "Amit Kumar & Loretta Auvil & Lily Dong",
		description = "This component calculates DunningLogLikelihood "+
		              "based on the input of two counts. " +
		              "This major functionality was developed as part of the Monk Project (http://monkproject.org) " +
		              "<p>References: For more information on Dunning's log-likelihood statistic, see "+
		              "Dunning, T. 1993. Accurate methods for the statistics of surprise and coincidence. "+
		              "Computational Linguistics 19.1 (Mar. 1993), 61-74. "+
		              "or Griffiths, D. Head First Statistics. 2009. Second edition. O'Reilly.",
		name = "Dunning Log Likelihood",
		tags = "dunning, likelihood, loglikelihood, monk, comparison, statistics",
		baseURL = "meandre://seasr.org/components/foundry/",
	    dependency = {"protobuf-java-2.2.0.jar"}
)
public class DunningLogLikelihood extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        description = "Token counts for the analysis set of documents.",
			name = Names.PORT_TOKEN_COUNTS
	)
    protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	@ComponentInput(
	        description = "Token counts for the reference set of documents.",
			name = Names.PORT_TOKEN_COUNTS_REFERENCE
	)
	protected static final String IN_REF_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS_REFERENCE;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        description = "Resulting analysis of dunning loglikelihood.",
			name = Names.PORT_TOKEN_COUNTS
	)
    protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;


    //--------------------------------------------------------------------------------------------

    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

	public void executeCallBack(ComponentContext cc) throws Exception {
		Map<String, Number> analysisCounts = new HashMap<String, Number>();
		Map<String, Number> referenceCounts = new HashMap<String, Number>();

		Map<String, Integer> analysis = DataTypeParser.parseAsStringIntegerMap(cc.getDataComponentFromInput(IN_TOKEN_COUNTS));
		Map<String, Integer> reference = DataTypeParser.parseAsStringIntegerMap(cc.getDataComponentFromInput(IN_REF_TOKEN_COUNTS));

		Set<String> set = analysis.keySet();
		Iterator<String> iterator = set.iterator();
		while (iterator.hasNext()) {
			String str = iterator.next();
			analysisCounts.put(str, analysis.get(str));
		}

		set = reference.keySet();
		iterator = set.iterator();
		while (iterator.hasNext()) {
			String str = iterator.next();
			referenceCounts.put(str, reference.get(str));
		}

		Map<ReverseScoredString, double[]>  results = doDunning(analysisCounts,referenceCounts,2);

		double min = Double.MAX_VALUE;
		for (ReverseScoredString key : results.keySet()) {
			double score = key.getScore();
			min = (min<score) ? min : score;
		}

		Map<String, Integer> outputMap = new Hashtable<String, Integer>();
		for (ReverseScoredString key : results.keySet()) {
			double score = key.getScore();
			if (min > 1) //apply score directly
				outputMap.put(key.getString(), new Integer((int)score));
			else //scale score
				outputMap.put(key.getString(), new Integer((int)(score/min)));

			console.fine(String.format("%s\t%s\t%s", key.getString(), score, (int)score));
		}

		cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(outputMap, false));
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------
/*
	@Test
	public void testSampleData(){
		Map<String, Number> analysisCounts = new HashMap<String,Number>();
		analysisCounts.put("i", 20);
		analysisCounts.put("like", 10);
		analysisCounts.put("peanuts", 5);

		Map<String, Number> referenceCounts = new HashMap<String,Number>();

		referenceCounts.put("i",5);
		referenceCounts.put("like",10);
		referenceCounts.put("peanuts",10);
		referenceCounts.put("and",10);
		referenceCounts.put("cake",3);

		Map<ReverseScoredString, double[]>  results = doDunning(analysisCounts,referenceCounts,2);

		System.out.println("Dunnings Log Likely hood\n Spelling\tscore");
		for(ReverseScoredString key:results.keySet()){
			System.out.println(key.getString()+"\t"+ key.getScore() );

		}
	}
*/
	private Map<ReverseScoredString, double[]> doDunning(Map<String, Number> analysisCounts,
			Map<String, Number> referenceCounts,int cutoff) {
		Map<ReverseScoredString, double[]> results;
		int analysisTotalCount = CountMapUtils
		.getTotalWordCount(analysisCounts);
		int refTotalCount = CountMapUtils.getTotalWordCount(referenceCounts);
		// Get combined string list.

		Map<String, Number> combinedCounts = CountMapUtils
		.semiDeepClone(analysisCounts);
		CountMapUtils.addCountMap(combinedCounts, referenceCounts);
		// Holds results of analysis.
		// Each unique input string produces
		// one line of results.
		results = new TreeMap<ReverseScoredString, double[]>();
		// Get set of unique strings
		// in the two input count maps
		// combined.
		Set<String> keySet = combinedCounts.keySet();
		// Counts strings analyzed.
		int stringsDone = 0;
		// Loop over the strings and
		// compute the frequency statistics.
		for (String stringToAnalyze : keySet) {
			// Get string count in analysis text.
			Number stringCount = analysisCounts.get(stringToAnalyze);
			if (stringCount == null)
				stringCount = new Integer(0);
			if (stringCount.intValue() < cutoff)
				continue;
			// Get string count in reference text.
			int refCount = 0;
			if (referenceCounts.containsKey(stringToAnalyze)) {
				refCount = referenceCounts.get(stringToAnalyze).intValue();
			}
			// Compute frequency statistics
			// including Dunning's log-likelihood.
			double[] freqAnal = doFreq(stringToAnalyze, stringCount.intValue(),analysisTotalCount, refCount, refTotalCount);

			// Save results for later reporting.
			results.put(new ReverseScoredString(stringToAnalyze, freqAnal[4]),
					freqAnal);
			// Update count of strings done.
			stringsDone++;
		}


		return results;
	}


	/**
	 *
	 * @param stringToAnalyze
	 * @param analysisCount
	 * @param analysisTotalCount
	 * @param refCount
	 * @param refTotalCount
	 * @return
	 * 		  <p>
	 *         (0) Count of word/lemma appearance in sample.<br /> (1) Percent
	 *         of word/lemma appearance in sample.<br /> (2) Count of word/lemma
	 *         appearance in reference.<br /> (3) Percent of word/lemma
	 *         appearance in reference.<br /> (4) Log-likelihood measure.<br />
	 *         (5) Significance of log-likelihood.<br />
	 *         </p>
	 *
	 */
	private static double[] doFreq(String stringToAnalyze, int analysisCount,
			int analysisTotalCount, int refCount, int refTotalCount) {
		// Compute percents and log-likelihood.

		double freqAnal[] = Frequency.logLikelihoodFrequencyComparison(
				analysisCount, refCount, analysisTotalCount, refTotalCount,
				false);
		// Convert percents to parts per 10,000.
		freqAnal[1] = freqAnal[1] * 100.0D;
		freqAnal[3] = freqAnal[3] * 100.0D;
		freqAnal[6]=Math.pow(2.0D, ArithUtils.asinh(freqAnal[4]));
		return freqAnal;
	}


}
