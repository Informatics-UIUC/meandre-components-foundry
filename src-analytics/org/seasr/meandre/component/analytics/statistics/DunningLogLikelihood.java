package org.seasr.meandre.component.analytics.statistics;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;
import org.monkproject.utils.CountMapUtils;
import org.monkproject.utils.ReverseScoredString;
import org.monkproject.utils.corpuslinguistics.Frequency;
import org.monkproject.utils.math.ArithUtils;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

/*
 * @(#) TestDunning.java @VERSION@
 *
 * Copyright (c) 2009+ Amit Kumar
 *
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */

@Component(creator="Amit Kumar & Loretta Auvil & Lily Dong",
		description="This component calculates DunningLogLikelihood "+
		"based on the input of two counts. " +
		"This major functionality was developed as part of the Monk Project (http://monkproject.org) " +
		"<p>References: For more information on Dunning's log-likelihood statistic, see "+
		"Dunning, T. 1993. Accurate methods for the statistics of surprise and coincidence. "+
		"Computational Linguistics 19.1 (Mar. 1993), 61-74. "+
		"or Griffiths, D. Head First Statistics. 2009. Second edition. O'Reilly.",
		name="DunningLogLikelihood",
		tags="dunning, likelihood, loglikelihood, monk, comparison, statistics",
		baseURL="meandre://seasr.org/components/")

public class DunningLogLikelihood implements ExecutableComponent {

	@ComponentInput(description="Map of counts for the analysis set of documents." +
			"<br>TYPE: Map",
			name="Analysis_Set")
			public final static String DATA_INPUT_ANALYSIS = "Analysis_Set";

	@ComponentInput(description="Map of counts for the reference set of documents." +
			"<br>TYPE: Map",
			name="Reference_Set")
			public final static String DATA_INPUT_REFERENCE = "Reference_Set";

	@ComponentOutput(description="Map for the resulting analysis of dunning loglikelihood." +
			"<br>TYPE: Map",
			name="Map")
			public final static String DATA_OUTPUT = "Map";

	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {
		Map<String, Number> analysisCounts = new HashMap<String, Number>();
		Map<String, Number> referenceCounts = new HashMap<String, Number>();

		Map<String, Integer> analysis = (Map<String, Integer>)cc.getDataComponentFromInput(DATA_INPUT_ANALYSIS);
		Map<String, Integer> reference = (Map<String, Integer>)cc.getDataComponentFromInput(DATA_INPUT_REFERENCE);

		Set<String> set = analysis.keySet();
		Iterator<String> iterator = set.iterator();
		while(iterator.hasNext()) {
			String str = iterator.next();
			analysisCounts.put(str, analysis.get(str));
		}
		set = reference.keySet();
		iterator = set.iterator();
		while(iterator.hasNext()) {
			String str = iterator.next();
			referenceCounts.put(str, reference.get(str));
		}

		Map<ReverseScoredString, double[]>  results = doDunning(analysisCounts,referenceCounts,2);

		double min = Double.MAX_VALUE;
		for(ReverseScoredString key:results.keySet()){
			double score = key.getScore();
			min = (min<score)? min: score;
		}
		Map<String, Integer> outputMap = new Hashtable<String, Integer>();
		for(ReverseScoredString key:results.keySet()){
			double score = key.getScore();
			if(min>1) //apply score directly
				outputMap.put(key.getString(), new Integer((int)score));
			else //scale score
				outputMap.put(key.getString(), new Integer((int)(score/min)));
			cc.getOutputConsole().println(key.getString()+"\t"+
					score+"\t"+
					(int)score);
		}

		cc.pushDataComponentToOutput(DATA_OUTPUT, outputMap);
	}
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


	public void dispose(ComponentContextProperties arg0)
	throws ComponentExecutionException, ComponentContextException {
		// TODO Auto-generated method stub
	}


	public void initialize(ComponentContextProperties arg0)
	throws ComponentExecutionException, ComponentContextException {
		// TODO Auto-generated method stub
	}

}
