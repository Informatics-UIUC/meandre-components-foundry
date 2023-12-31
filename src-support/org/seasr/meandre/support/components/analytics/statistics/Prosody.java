package org.seasr.meandre.support.components.analytics.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.util.KeyValuePair;

public class Prosody {

	/* Parameters */
	List<Integer> focusedComparisonIndexes = new ArrayList<Integer>();
	int maxNumPhonemesPerVolume = 999999999;  /* max number of examples to use */
	public int numThreads = 16;
	int numRounds = 1;  /* relevant when using sampling */
	boolean useSampling = false;
	double weightingPower = 32.0; /* main parameter to be controlled - valid values: greater than zero to 100 */
	int windowSizeInPhonemes = 8;
	int part_of_speech_weight = 1;
	int accent_weight = 1;
	int stress_weight = 1;
	int tone_weight = 1;
	int phrase_id_weight = 1;
	int break_index_weight = 1;
	int phoneme_id_weight = 0;

	/* Internal variables */
	public static final String DOC_NAME 	  = "doc_name";
	public static final String TEI_SECTION_ID = "tei_section_id";
	public static final String TEI_NODE_ID 	  = "tei_node_id";
	public static final String TEI_NODE_TYPE  = "tei_node_type";
	public static final String SENTENCE_ID 	  = "sentence_id";
	public static final String PHRASE_ID 	  = "phrase_id";
	public static final String WORD 		  = "word";
	public static final String PART_OF_SPEECH = "part_of_speech";
	public static final String ACCENT 		  = "accent";
	public static final String PHONEME 		  = "phoneme";
	public static final String STRESS 		  = "stress";
	public static final String TONE 		  = "tone";
	public static final String BREAK_INDEX 	  = "break_index";

	int[] windowFeatureWeights;
	double[] differenceSumToWeight;
	double[][] similarityWeights;

	int numTexts = -1;
	public int reportIntervalInProblems = (int) 1e1;
	ArrayList<ProsodyProblem> prosodyProblems = null;
	int numProblems;
	ProsodyProblemSolver[] problemSolvers = null;

	int maxNumPhonemes = (int) 20e6;
	int numFeatures = 7;
	int maxNumSymbols = maxNumPhonemes * numFeatures;
	int maxNumTexts = (int) 1e3;
	int windowSizeInFeatures = -1;

	int[] featureWeights = null;

	@SuppressWarnings("unchecked")
	HashMap<String, Integer>[] symbolToIndex = new HashMap[numFeatures];
	int numSymbols = 0;
	int numExamples = 0;
	int numPhrases = 0;
	int numPhonemes = 0;

	int[] symbols = new int[maxNumSymbols];
	int symbolIndex = 0;
	int stringIndex = 0;

	int[] textEndSymbolIndex = new int[maxNumTexts];
	int textIndex = 0;

	List<KeyValuePair<SimpleTuplePeer, Strings[]>> output = new ArrayList<KeyValuePair<SimpleTuplePeer,Strings[]>>();

	Logger logger = Logger.getLogger(Prosody.class.getName());

	/******************************************************************************************/

	public Prosody() {
		for (int i = 0; i < numFeatures; i++)
			symbolToIndex[i] = new HashMap<String,Integer>();
	}

	public List<Integer> getFocusedComparisonIndexes() {
		return focusedComparisonIndexes;
	}

	public void addIndexToFocusedComparison(int index) {
		this.focusedComparisonIndexes.add(index);
	}

	public int getMaxNumPhonemesPerVolume() {
		return maxNumPhonemesPerVolume;
	}

	public void setMaxNumPhonemesPerVolume(int maxNumPhonemesPerVolume) {
		this.maxNumPhonemesPerVolume = maxNumPhonemesPerVolume;
	}

	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public int getNumRounds() {
		return numRounds;
	}

	public void setNumRounds(int numRounds) {
		this.numRounds = numRounds;
	}

	public boolean getUseSampling() {
		return useSampling;
	}

	public void setUseSampling(boolean useSampling) {
		this.useSampling = useSampling;
	}

	public double getWeightingPower() {
		return weightingPower;
	}

	public void setWeightingPower(double weightingPower) {
		this.weightingPower = weightingPower;
	}

	public int getWindowSizeInPhonemes() {
		return windowSizeInPhonemes;
	}

	public void setWindowSizeInPhonemes(int windowSizeInPhonemes) {
		this.windowSizeInPhonemes = windowSizeInPhonemes;
	}

	public int getPartOfSpeechWeight() {
		return part_of_speech_weight;
	}

	public void setPartOfSpeechWeight(int part_of_speech_weight) {
		this.part_of_speech_weight = part_of_speech_weight;
	}

	public int getAccentWeight() {
		return accent_weight;
	}

	public void setAccentWeight(int accent_weight) {
		this.accent_weight = accent_weight;
	}

	public int getStressWeight() {
		return stress_weight;
	}

	public void setStressWeight(int stress_weight) {
		this.stress_weight = stress_weight;
	}

	public int getToneWeight() {
		return tone_weight;
	}

	public void setToneWeight(int tone_weight) {
		this.tone_weight = tone_weight;
	}

	public int getPhraseIdWeight() {
		return phrase_id_weight;
	}

	public void setPhraseIdWeight(int phrase_id_weight) {
		this.phrase_id_weight = phrase_id_weight;
	}

	public int getBreakIndexWeight() {
		return break_index_weight;
	}

	public void setBreakIndexWeight(int break_index_weight) {
		this.break_index_weight = break_index_weight;
	}

	public int getPhonemeIdWeight() {
		return phoneme_id_weight;
	}

	public void setPhonemeIdWeight(int phoneme_id_weight) {
		this.phoneme_id_weight = phoneme_id_weight;
	}

	public List<KeyValuePair<SimpleTuplePeer, Strings[]>> getOutput() {
		return output;
	}

	public int addData(SimpleTuplePeer tuplePeer, Strings[] tuples) {
		output.add(new KeyValuePair<SimpleTuplePeer, Strings[]>(tuplePeer, tuples));

		SimpleTuple tuple = tuplePeer.createTuple();

		String lastUniquePhraseId = null;
		String[] featurePatterns = new String[numFeatures];

		for (int i = 0, iMax = tuples.length; i < iMax; i++) {
			tuple.setValues(tuples[i]);

			String doc_name = tuple.getValue(DOC_NAME);
			String tei_section_id = tuple.getValue(TEI_SECTION_ID);
			String sentence_id = tuple.getValue(SENTENCE_ID);
			String phrase_id = tuple.getValue(PHRASE_ID);
			String part_of_speech = tuple.getValue(PART_OF_SPEECH);
			String accent = tuple.getValue(ACCENT);
			String stress = tuple.getValue(STRESS);
			String tone = tuple.getValue(TONE);
			String break_index = tuple.getValue(BREAK_INDEX);
			String phoneme_id = tuple.getValue(PHONEME);

			String uniquePhraseId = String.format("%s:%s:%s:%s", doc_name, tei_section_id, sentence_id, phrase_id);
			if (!uniquePhraseId.equals(lastUniquePhraseId)) {
				numPhrases++;
				lastUniquePhraseId = uniquePhraseId;
			}
			numPhonemes++;

			featurePatterns[0] = part_of_speech;
			featurePatterns[1] = accent;
			featurePatterns[2] = stress;
			featurePatterns[3] = tone;
			featurePatterns[4] = phrase_id;
			featurePatterns[5] = break_index;
			featurePatterns[6] = phoneme_id;


			for (int f = 0; f < numFeatures; f++)
				if (!symbolToIndex[f].containsKey(featurePatterns[f]))
					symbolToIndex[f].put(featurePatterns[f], numSymbols++);

			for (int j = 0; j < numFeatures; j++) {
				int symbol = symbolToIndex[j].get(featurePatterns[j]);
				symbols[symbolIndex++] = symbol;
			}
		}

		textEndSymbolIndex[textIndex] = symbolIndex;

		return textIndex++;
	}

	public void computeSimilarities() {

		featureWeights = new int[] { part_of_speech_weight, accent_weight, stress_weight, tone_weight, phrase_id_weight, break_index_weight, phoneme_id_weight };


		reportStats();

		numTexts = textIndex;

		optimizeMemoryFootprint();

		//////////////////////////////
		// COMPUTE CONFUSION MATRIX //
		//////////////////////////////
		computeConfusionMatrix();

		/////////////////////
		// CREATE PROBLEMS //
		/////////////////////
		createProblems();

		////////////////////
		// SOLVE PROBLEMS //
		////////////////////
		solveProblems();

		String[] docNames = new String[output.size()];
		for (int i = 0, iMax = output.size(); i < iMax; i++) {
			KeyValuePair<SimpleTuplePeer, Strings[]> doc = output.get(i);
			SimpleTuple tuple = doc.getKey().createTuple();
			tuple.setValues(doc.getValue()[0]);
			docNames[i] = tuple.getValue(DOC_NAME);
		}

		int phonemeIndex = 0;
		int lastVolumeIndex = -1;
		KeyValuePair<SimpleTuplePeer, Strings[]> outputDoc = null;
		SimpleTuple tuple = null, outTuple = null;
		Strings[] docData = null, outData = null;

		for (int i = 0; i < numProblems; i++) {

			ProsodyProblem problem = prosodyProblems.get(i);

			if (problem.seedTextIndex != lastVolumeIndex) {
				lastVolumeIndex = problem.seedTextIndex;
				phonemeIndex = 0;

				KeyValuePair<SimpleTuplePeer, Strings[]> document = output.get(problem.seedTextIndex);
				SimpleTuplePeer outPeer = new SimpleTuplePeer(document.getKey(), docNames);
				int count = 0;
				for (ProsodyProblem p : prosodyProblems)
					if (p.seedTextIndex == problem.seedTextIndex)
						count++;
				outputDoc = new KeyValuePair<SimpleTuplePeer, Strings[]>(outPeer, new Strings[count]);
				output.set(problem.seedTextIndex, outputDoc);
				tuple = document.getKey().createTuple();
				outTuple = outPeer.createTuple();
				docData = document.getValue();
				outData = outputDoc.getValue();
			}

			tuple.setValues(docData[phonemeIndex]);
			outTuple.setValue(tuple);

			for (int j = 0; j < numTexts; j++) {
				if (phonemeIndex < windowSizeInPhonemes / 2)
					outTuple.setValue(docNames[j], "0");
				else {
					double similarityValue = prosodyProblems.get(i - windowSizeInPhonemes / 2).similarities[j];
					outTuple.setValue(docNames[j], Double.toString(similarityValue));
				}
			}

			outData[phonemeIndex] = outTuple.convert();

			phonemeIndex++;
		}
	}

	private void reportStats() {
		logger.fine("numPhrases = " + numPhrases);
		logger.fine("numPhonemes = " + numPhonemes);

		double phonemsPerPhrase = (double) numPhonemes / numPhrases;
		logger.fine("phonemsPerPhrase = " + phonemsPerPhrase);

		int numTextSymbols = symbolIndex;

		logger.fine("numTextSymbols = " + numTextSymbols);
		logger.fine("numTexts = " + numTexts);
	}

	private void createProblems() {
		int numProblemsToCreate = -1;

		boolean useAllText = !useSampling;
		int problemIndex = 0;

		if (useSampling) {
			numProblemsToCreate = numRounds * numTexts;

			prosodyProblems = new ArrayList<ProsodyProblem>(numProblemsToCreate);
			for (int roundIndex = 0; roundIndex < numRounds; roundIndex++) {
				for (int seedTextIndex = 0; seedTextIndex < numTexts; seedTextIndex++) {
					// pick seed window
					int seedStartSymbolIndex = -1;
					if (seedTextIndex == 0) {
						seedStartSymbolIndex = 0;
					} else {
						seedStartSymbolIndex = textEndSymbolIndex[seedTextIndex - 1];
					}
					int seedNumSymbols = textEndSymbolIndex[seedTextIndex] - seedStartSymbolIndex;
					int seedWindowSymbolIndex = (int) (Math.random() * (seedNumSymbols - windowSizeInFeatures)) + seedStartSymbolIndex;
					seedWindowSymbolIndex = seedWindowSymbolIndex / numFeatures * numFeatures;

					ProsodyProblem prosodyProblem = new ProsodyProblem(problemIndex++, seedTextIndex, seedWindowSymbolIndex, numTexts);

					prosodyProblems.add(prosodyProblem);
				}
			}
		}

		if (useAllText) {
			prosodyProblems = new ArrayList<ProsodyProblem>(0);

			for (int seedVolumeIndex : focusedComparisonIndexes) {

				int seedStartSymbolIndex = -1;
				if (seedVolumeIndex == 0) {
					seedStartSymbolIndex = 0;
				} else {
					seedStartSymbolIndex = textEndSymbolIndex[seedVolumeIndex - 1];
				}

				int seedNumSymbols = textEndSymbolIndex[seedVolumeIndex] - seedStartSymbolIndex;
				logger.fine("seedVolumeIndex = " + seedVolumeIndex);
				logger.fine("seedNumSymbols  = " + seedNumSymbols);

				int numWindows = (seedNumSymbols - windowSizeInFeatures) / numFeatures + 1;

				// int numWindows = seedNumSymbols / numFeatures;

				if (numWindows > maxNumPhonemesPerVolume) {
					numWindows = maxNumPhonemesPerVolume;
				}

				for (int i = 0; i < numWindows; i++) {

					int seedWindowSymbolIndex = i * numFeatures + seedStartSymbolIndex;

					ProsodyProblem prosodyProblem = new ProsodyProblem(problemIndex++, seedVolumeIndex, seedWindowSymbolIndex, numTexts);

					prosodyProblems.add(prosodyProblem);
				}

			}

		}

		numProblems = prosodyProblems.size();
	}

	private void computeConfusionMatrix() {
		windowSizeInFeatures = windowSizeInPhonemes * numFeatures;
		similarityWeights = new double[numTexts][numTexts];

		windowFeatureWeights = new int[windowSizeInFeatures];
		for (int i = 0; i < windowFeatureWeights.length; i++) {
			windowFeatureWeights[i] = featureWeights[i % numFeatures];
		}

		int maxWeight = windowSizeInFeatures;
		differenceSumToWeight = new double[maxWeight + 1];
		for (int i = 0; i < differenceSumToWeight.length; i++) {
			double similarity = 1.0 - (double) i / windowSizeInFeatures;
			similarity = Math.pow(similarity, weightingPower);
			differenceSumToWeight[i] = similarity;
		}
	}

	private void optimizeMemoryFootprint() {
		// Resizing the arrays to fit the data
		int[] temp = null;

		temp = new int[symbolIndex];
		System.arraycopy(symbols, 0, temp, 0, symbolIndex);
		symbols = temp;

		temp = new int[textIndex];
		System.arraycopy(textEndSymbolIndex, 0, temp, 0, textIndex);
		textEndSymbolIndex = temp;
	}


	int nextProblemIndex = 0;

	int getNextProblemIndexAndIncrement() {

		synchronized (this) {

			if (nextProblemIndex < numProblems) {

				int problemIndex = nextProblemIndex;

				nextProblemIndex++;

				return problemIndex;
			} else {
				return -1;
			}
		}

	}

	public void solveProblem(ProsodyProblem prosodyProblem) {

		int seedTextIndex = prosodyProblem.seedTextIndex;
		int seedWindowSymbolIndex = prosodyProblem.seedWindowSymbolIndex;

		double bestOverallSimilarity = Double.NEGATIVE_INFINITY;
		int bestCandidateTextIndex = -1;
		int numTies = 0;
		int[] tiedCandidateTextIndices = new int[numTexts];
		for (int candidateTextIndex = 0; candidateTextIndex < numTexts; candidateTextIndex++) {

			int candidateTextNumWindows = -1;
			int candidateWindowSymbolIndex = -1;
			if (candidateTextIndex == 0) {
				candidateTextNumWindows = textEndSymbolIndex[0] / numFeatures - windowSizeInFeatures / numFeatures + 1;
				candidateWindowSymbolIndex = 0;
			} else {
				candidateTextNumWindows = (textEndSymbolIndex[candidateTextIndex] - textEndSymbolIndex[candidateTextIndex - 1]) / numFeatures - windowSizeInFeatures / numFeatures + 1;
				candidateWindowSymbolIndex = textEndSymbolIndex[candidateTextIndex - 1];
			}

			double candidateTextWeightSum = 0.0;
			int candidateTextWeightCount = 0;
			for (int candidateWindowIndex = 0; candidateWindowIndex < candidateTextNumWindows; candidateWindowIndex++) {

				// ensure non overlapping windows
				// if (Math.abs(seedWindowSymbolIndex - candidateWindowSymbolIndex) > windowSizeInFeatures) {

				// compare windows
				int differenceSum = 0;
				int index1 = seedWindowSymbolIndex;
				int index2 = candidateWindowSymbolIndex;
				int featureIndex = 0;
				for (; featureIndex < windowSizeInFeatures; index1++, index2++, featureIndex++) {
					if (symbols[index1] != symbols[index2]) {
						differenceSum += windowFeatureWeights[featureIndex];
					}
				}

				double weight = differenceSumToWeight[differenceSum];

				// double similarity = 1.0 - (double) differenceSum / windowSizeInFeatures;

				// Double weight = differenceToWeightMap.get(similarity);
				// if (weight == null) {
				// weight = Math.pow(similarity, weightingPower);
				// differenceToWeightMap.put(differenceSum, weight);
				// }
				// similarity = weight;

				// similarity = Math.pow(similarity, weightingPower);

				// candidateTextWeightSum += similarity;

				candidateTextWeightSum += weight;
				candidateTextWeightCount++;

				// }

				candidateWindowSymbolIndex += numFeatures;

			}

			double candidateTextSimilarity = candidateTextWeightSum / candidateTextWeightCount;

			prosodyProblem.similarities[candidateTextIndex] = candidateTextSimilarity;

			if (candidateTextSimilarity > bestOverallSimilarity) {
				numTies = 0;
				tiedCandidateTextIndices[numTies++] = candidateTextIndex;
				bestOverallSimilarity = candidateTextSimilarity;
			} else if (candidateTextSimilarity == bestOverallSimilarity) {
				tiedCandidateTextIndices[numTies++] = candidateTextIndex;
			}

		}

		bestCandidateTextIndex = tiedCandidateTextIndices[(int) (Math.random() * numTies)];

		synchronized (this) {
			similarityWeights[seedTextIndex][bestCandidateTextIndex]++;
		}

		prosodyProblem.complete = true;
	}

	public void solveProblems() {

		nextProblemIndex = 0;

		/************************/
		/* start worker threads */
		/************************/

		problemSolvers = new ProsodyProblemSolver[numThreads];
		for (int i = 0; i < numThreads; i++) {
			problemSolvers[i] = new ProsodyProblemSolver(this, i);
			problemSolvers[i].start();
		}

		/*************************************************/
		/* wait until all worker threads have terminated */
		/*************************************************/
		int nextReportNumProblemsSolved = reportIntervalInProblems;

		//Clock clock = new Clock();
		long startTime = System.currentTimeMillis();
		long lastReportTime = startTime;
		while (true) {

			int numProblemsSolved = countNumContiguousProblemsSolved();

			if (logger.isLoggable(Level.FINER)) {
				if (numProblemsSolved >= nextReportNumProblemsSolved) {
					long time = System.currentTimeMillis();

					double duration = (time - startTime) / 1000.0;

					double seedsPerSecond = numProblemsSolved / duration;

					double timePerSeed = duration / numProblemsSolved;
					double totalTimeEst = timePerSeed * numProblems;
					double timeLeft = totalTimeEst - duration;

					logger.finer("seedsPerSecond   = " + seedsPerSecond);
					logger.finer("timePerSeed      = " + timePerSeed);
					logger.finer("totalTimeEst   = " + totalTimeEst);
					logger.finer("timeLeft (s)     = " + timeLeft);
					logger.finer("timeLeft (m)     = " + timeLeft / 60);
					logger.finer("timeLeft (h)     = " + timeLeft / 3600);
					logger.finer("timeLeft (d)     = " + timeLeft / 3600 / 24);

					logger.finer("numProblemsSolved = " + numProblemsSolved);
					logger.finer("numProblems       = " + numProblems);
					nextReportNumProblemsSolved += reportIntervalInProblems;
				}
			}

			if (numProblemsSolved == numProblems) {
				break;
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	int countNumContiguousProblemsSolved() {

		int numProblemsSolved = 0;
		for (Iterator<ProsodyProblem> iterator = prosodyProblems.iterator(); iterator.hasNext();) {

			ProsodyProblem prosodyProblem = iterator.next();

			if (prosodyProblem.complete) {
				numProblemsSolved++;
			} else {
				break;
			}
		}

		return numProblemsSolved;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}

class ProsodyProblem implements Serializable {

	private static final long serialVersionUID = 516261951424506571L;

	public boolean complete;
	int overallIndex;

	int seedTextIndex;
	int seedWindowSymbolIndex;
	double similarities[];

	ProsodyProblem(int overallIndex, int seedTextIndex, int seedWindowSymbolIndex, int numTexts) {
		this.overallIndex = overallIndex;
		this.seedTextIndex = seedTextIndex;
		this.seedWindowSymbolIndex = seedWindowSymbolIndex;
		this.similarities = new double[numTexts];
	}

}

class ProsodyProblemSolver extends Thread {

	Prosody prosody;

	int numThreads = -1;
	int threadIndex = -1;

	boolean stopNow = false;
	boolean terminated = false;

	ProsodyProblemSolver(Prosody prosody, int threadIndex) {
		this.prosody = prosody;
		this.numThreads = prosody.numThreads;
		this.threadIndex = threadIndex;
	}

	@Override
	public void run() {

		while (true) {

			if (stopNow) {
				break;
			}

			int problemIndex = prosody.getNextProblemIndexAndIncrement();

			if (problemIndex == -1) {
				break;
			}

			/*****************/
			/* SOLVE PROBLEM */
			/*****************/

			prosody.solveProblem(prosody.prosodyProblems.get(problemIndex));

		}

		terminated = true;

		return;
	}

	public void terminate() {
		stopNow = true;
	}

}
