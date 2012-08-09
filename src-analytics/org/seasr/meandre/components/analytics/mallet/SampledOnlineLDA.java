package org.seasr.meandre.components.analytics.mallet;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

import cc.mallet.types.*;
import cc.mallet.util.*;

import java.util.*;
import java.util.zip.*;
import java.io.*;

@Component(
        name = "Sampled Online LDA",
        creator = "Loretta Auvil based on code from David Mimno and Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#ANALYTICS, mallet, topic model",
        description = "This component perform topic analysis in the style of LDA with sampling." ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class SampledOnlineLDA extends AbstractExecutableComponent{

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "mallet_instance_list",
            description = "The list of machine learning instances" +
                "<br>TYPE: cc.mallet.types.InstanceList"
    )
    protected static final String IN_INSTANCE_LIST = "mallet_instance_list";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "topic_model",
            description = "The topic model" +
                "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
    )
    protected static final String OUT_TOPIC_MODEL = "topic_model";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "num_topics",
            description = "The number of topics to fit",
            defaultValue = "10"
    )
    protected static final String PROP_NUM_TOPICS = "num_topics";
//  static cc.mallet.util.CommandOption.Integer numTopicsOption = new cc.mallet.util.CommandOption.Integer
//	(SampledOnlineLDA.class, "num-topics", "INTEGER", true, 10,
//	"The number of topics", null);

    @ComponentProperty(
            name = "doc_topic_smoothing",
            description = "Alpha parameter: smoothing over topic distribution; Dirichlet parameter for symmetric prior over document-topic distributions.",
            defaultValue = "0.1"
    )
    protected static final String PROP_DOC_TOPIC_SMOOTHING = "doc_topic_smoothing";
//	static cc.mallet.util.CommandOption.Double docTopicSmoothingOption = new cc.mallet.util.CommandOption.Double
//	(SampledOnlineLDA.class, "alpha", "POS NUMBER", true, 0.1,
//	 "Dirichlet parameter for symmetric prior over document-topic distributions. This is the value for each dimension.", null);

    @ComponentProperty(
            name = "topic_word_smoothing",
            description = "Beta parameter: smoothing over topic-word distribution; Dirichlet parameter for symmetric prior over topic-word distributions.",
            defaultValue = "0.1"
    )
    protected static final String PROP_TOPIC_WORD_SMOOTHING = "topic_word_smoothing";
//static cc.mallet.util.CommandOption.Double topicWordSmoothingOption = new cc.mallet.util.CommandOption.Double
//	(SampledOnlineLDA.class, "beta", "POS NUMBER", true, 0.1,
//	 "Dirichlet parameter for symmetric prior over topic-word distributions. This is the value for each dimension.", null);

    @ComponentProperty(
            name = "random_seed",
            description = "The random seed for the Gibbs sampler.  Default is 0, which will use the clock.",
            defaultValue = "1"
    )
    protected static final String PROP_RANDOM_SEED = "random_seed";
//	static cc.mallet.util.CommandOption.Integer randomSeed = new cc.mallet.util.CommandOption.Integer
//	(SampledOnlineLDA.class, "random-seed", "INTEGER", true, 1,
//	 "An initial seed for the random number generator", null);
    
    @ComponentProperty(
            name = "momentum",
            description = "Weighting of the previous gradient.",
            defaultValue = "0.0"
    )
    protected static final String PROP_MOMENTUM = "momentum";
//	static cc.mallet.util.CommandOption.Double momentumOption = new cc.mallet.util.CommandOption.Double
//	(SampledOnlineLDA.class, "momentum", "FILENAME", true, 0.0,
//	 "Weighting of the previous gradient.", null);

    @ComponentProperty(
            name = "num_samples",
            description = "The number of topics.",
            defaultValue = "5"
    )
    protected static final String PROP_NUM_SAMPLES = "num_samples";
//	static cc.mallet.util.CommandOption.Integer numSamplesOption = new cc.mallet.util.CommandOption.Integer
//	(SampledOnlineLDA.class, "num-samples", "INTEGER", true, 5,
//	 "The number of topics", null);

    @ComponentProperty(
            name = "sample_burn_in",
            description = "The number of topics.",
            defaultValue = "2"
    )
    protected static final String PROP_SAMPLE_BURN_IN = "sample_burn_in";
//	static cc.mallet.util.CommandOption.Integer sampleBurnInOption = new cc.mallet.util.CommandOption.Integer
//	(SampledOnlineLDA.class, "sample-burn-in", "INTEGER", true, 2,
//	 "The number of topics", null);

    @ComponentProperty(
            name = "batch_size",
            description = "The number of instances to examine before updating parameters.",
            defaultValue = "100"
    )
    protected static final String PROP_BATCH_SIZE = "batch_size";
//	static cc.mallet.util.CommandOption.Integer batchSizeOption = new cc.mallet.util.CommandOption.Integer
//	(SampledOnlineLDA.class, "batch-size", "INTEGER", true, 100,
//	 "The number of instances to examine before updating parameters", null);

    @ComponentProperty(
            name = "learning_rate",
            description = "The learning rate will be 1.0 / ([this value] + t).",
            defaultValue = "100"
    )
    protected static final String PROP_LEARNING_RATE = "learning_rate";
//	static cc.mallet.util.CommandOption.Double learningRateOption = new cc.mallet.util.CommandOption.Double
//	(SampledOnlineLDA.class, "learning-rate", "INTEGER", true, 100.0,
//	 "The learning rate will be 1.0 / ([this value] + t)", null);

    @ComponentProperty(
            name = "learning_rate_exponent",
            description = "The learning rate will be pow(1.0 / (offset + t), [this value]). Must be between 0.5 and 1.0.",
            defaultValue = "0.6"
    )
    protected static final String PROP_LEARNING_RATE_EXP = "learning_rate_exponent";
//static cc.mallet.util.CommandOption.Double learningRateExponentOption = new cc.mallet.util.CommandOption.Double
//	(SampledOnlineLDA.class, "learning-rate-exponent", "NUMBER", true, 0.6,
//	 "The learning rate will be pow(1.0 / (offset + t), [this value]). Must be between 0.5 and 1.0", null);

    @ComponentProperty(
            name = "saved_word_topics",
            description = "The total number of instances to examine.",
            defaultValue = "100"
    )
    protected static final String PROP_SAVED_WORD_TOPICS = "saved_word_topics";
//static cc.mallet.util.CommandOption.Integer savedStatesCountOption = new cc.mallet.util.CommandOption.Integer
//	(SampledOnlineLDA.class, "saved-wordtopics", "INTEGER", true, 100,
//	 "The total number of instances to examine", null);

    @ComponentProperty(
            name = "word_topics_interval",
            description = "The learning rate will be 1.0 / ([this value] + t).",
            defaultValue = "500000"
    )
    protected static final String PROP_WORD_TOPICS_INTERVAL = "word_topics_interval";
//static cc.mallet.util.CommandOption.Integer docsBetweenSavedStates = new cc.mallet.util.CommandOption.Integer
//	(SampledOnlineLDA.class, "wordtopics-interval", "INTEGER", true, 500000,
//	 "The number of instances to examine between saved states", null);

    @ComponentProperty(
            name = "total_folds",
            description = "The number of equal-sized held-out cross validation folds. A value 0 will use all data.",
            defaultValue = "0"
    )
    protected static final String PROP_TOTAL_FOLDS = "total_folds";
//static cc.mallet.util.CommandOption.Integer foldCountOption = new cc.mallet.util.CommandOption.Integer
//	(SampledOnlineLDA.class, "total-folds", "INTEGER", true, 0,
//	 "The number of equal-sized held-out cross validation folds. A value 0 will use all data.", null);

    @ComponentProperty(
            name = "held_out_fold",
            description = "The index of the cross validation fold to hold out, starting with 0.",
            defaultValue = "0"
    )
    protected static final String PROP_HELD_OUT_FOLD = "held_out_fold";
//static cc.mallet.util.CommandOption.Integer heldOutFoldOption = new cc.mallet.util.CommandOption.Integer
//	(SampledOnlineLDA.class, "held-out-fold", "INTEGER", true, 0,
//	 "The index of the cross validation fold to hold out, starting with 0.", null);

    protected int _numTopics;
    protected int _randomSeed;
    protected double _docTopicSmoothing; //formerly _alpha
    protected double _topicWordSmoothing; // formerly _beta;
    protected double _momentum; //never used
    protected int _numSamples;
    protected int _sampleBurnIn;
    protected int _batchSize;
    protected int _learningRate;
    protected double _learningRateExp;
    protected int _savedWordTopics;
    protected int _wordTopicsInterval;
    protected int _totalFolds;
    protected int _heldOutFold;
    
//	static cc.mallet.util.CommandOption.String outputPrefix = new cc.mallet.util.CommandOption.String
//	(SampledOnlineLDA.class, "output-prefix", "STRING", true, "o-lda",
//	 "The prefix for output files (sampling states, parameters, etc)", null);

		public Alphabet _vocabulary;
		double[][] _typeWeights;
		int[][] _typeTopics;

//		//double[][] typeTopicTokens;
		double[] _topicTokenTotals;
		
//		double[][] typeTopicGradients;

		int[] _wordGradientQueueTopics;
		int[] _wordGradientQueueWords;

//		int _numDocs;
		int _numTypes;
//		int numTokens;

//		double _sampleWeight = 1.0 / (_numSamples - _sampleBurnIn);

//		int _heldOutFold = 0;
//		int _numHeldOutFolds = 0;

		Randoms _random;
		
//		public void setHeldOutFold(int fold, int totalFolds) {
//			this._heldOutFold = fold;
//			this._numHeldOutFolds = totalFolds;
//		}

//		public void setSampling(int n, int bi) {
//			_numSamples = n;
//			_sampleBurnIn = bi;
//			_sampleWeight = 1.0 / (_numSamples - _sampleBurnIn);
//		}

		public double approximateExpDigamma(double x) {
			double correction = 0.0;
			while (x < 5.0) {
				correction += 1.0 / x;
				x++;
			}

			return (x - 0.5) * Math.exp(-correction);
		}

		public void train(InstanceList training, int numOutputs, int docsBetweenOutput) throws IOException {
			
			long trainingStartTime = System.currentTimeMillis();
			long tokensProcessed = 0;
			long docsProcessed = 0;
			int _numDocs = training.size();
			int wordGradientLimit = 0;

			// To regularize output relative to batch sizes, output 
			// topics after N documents have been processed.
			// This variable is numDocsProcessed / N (integer division)
			long currentOutputMultiple = 0;

			double[] samplingWeights = new double[_numTopics];

			double digamma_topic_word_smoothing = Dirichlet.digamma(_topicWordSmoothing);
			double expDigammaTopicWordSmoothing = Math.exp(digamma_topic_word_smoothing);
			double[] topicCoefficients = new double[_numTopics];
			double coefficientSum = 0.0;

			double[] topicNormalizers = new double[_numTopics];

			// The main loop

			double scale = 1.0;

			int[] docTopicCounts = new int[_numTopics];

			int iteration = 0;
			while (docsProcessed < numOutputs * docsBetweenOutput) {

				long startTime = System.currentTimeMillis();
			
				wordGradientLimit = 0;

				// Process a minibatch

				for (int topic = 0; topic < _numTopics; topic++) {
					topicNormalizers[topic] = 1.0 / (_numTypes * _topicWordSmoothing + scale * _topicTokenTotals[topic] - 0.5);
				}

				int totalSamples = 0;
				int totalChanges = 0;

				for (int batchDoc = 0; batchDoc < _batchSize; batchDoc++) {
					
					int docIndex = _random.nextInt(_numDocs);

					// rejection sample for held-out validation
					if (_totalFolds > 0) {
						while ((17 * docIndex) % _totalFolds == _heldOutFold) {
							//						logger.info("rejecting " + docIndex);
							docIndex = _random.nextInt(_numDocs);
						}
					}

					Instance instance = training.get(docIndex);
					FeatureSequence tokens = (FeatureSequence) instance.getData();

					int[] topics = new int[tokens.size()];
					//boolean[] hasLargeCounts = new boolean[tokens.size()];
					Arrays.fill(docTopicCounts, 0);

					if ((_numSamples - _sampleBurnIn) * tokens.size() + wordGradientLimit >= _wordGradientQueueTopics.length) {
						int newSize = 2 * _wordGradientQueueTopics.length;
						int[] tempWordGradientQueueTopics = new int[newSize];
						int[] tempWordGradientQueueWords = new int[newSize];
						System.arraycopy(_wordGradientQueueTopics, 0, tempWordGradientQueueTopics, 0, _wordGradientQueueTopics.length);
						System.arraycopy(_wordGradientQueueWords, 0, tempWordGradientQueueWords, 0, _wordGradientQueueWords.length);
						
						_wordGradientQueueTopics = tempWordGradientQueueTopics;
						_wordGradientQueueWords = tempWordGradientQueueWords;
					}

					tokensProcessed += tokens.size();

					coefficientSum = 0.0;
					for (int topic = 0; topic < _numTopics; topic++) {
						topicCoefficients[topic] = (_docTopicSmoothing + docTopicCounts[topic]) * topicNormalizers[topic]; // / (numTypes * topicWordSmoothing + scale * topicTokenTotals[topic] - 0.5);
						coefficientSum += topicCoefficients[topic];
					}
					
					for (int sweep = 0; sweep < _numSamples; sweep++) {
						//System.out.println(sweep + "\t" + UnicodeBarplot.getBars(docTopicCounts));

						for (int position = 0; position < tokens.size(); position++) {

							int type = tokens.getIndexAtPosition(position);
							//double[] currentTypeTopicTokens = typeTopicTokens[type];
							double[] currentTypeWeights = _typeWeights[type];
							int[] currentTypeTopics = _typeTopics[type];
												
							int oldTopic = topics[position];

							if (sweep > 0) {
								docTopicCounts[oldTopic]--;
								coefficientSum -= topicCoefficients[oldTopic];
								topicCoefficients[oldTopic] = (_docTopicSmoothing + docTopicCounts[oldTopic]) * topicNormalizers[oldTopic];
								coefficientSum += topicCoefficients[oldTopic];
							}

							int newTopic = 0;
							
							// At this level, exp(digamma(beta)) is large enough that we don't need
							// to sample in log space.
							int samplingLimit = 0;
							double sparseSamplingSum = 0;
							while (samplingLimit < currentTypeWeights.length && currentTypeWeights[samplingLimit] > 0.0) {
								if (scale * currentTypeWeights[samplingLimit] > 5.0) {
									samplingWeights[samplingLimit] =
										(_topicWordSmoothing + scale * currentTypeWeights[samplingLimit] - 0.5 - expDigammaTopicWordSmoothing) *
										topicCoefficients[ currentTypeTopics[samplingLimit] ];
								}
								else {
									samplingWeights[samplingLimit] =
										(approximateExpDigamma(_topicWordSmoothing + scale * currentTypeWeights[samplingLimit]) - expDigammaTopicWordSmoothing) *
										topicCoefficients[ currentTypeTopics[samplingLimit] ];
								}
								sparseSamplingSum += samplingWeights[samplingLimit];
								samplingLimit++;
							}
							
							double sample = (sparseSamplingSum + expDigammaTopicWordSmoothing * coefficientSum) * _random.nextUniform();
							
							if (sample < sparseSamplingSum) {
								int index = 0;
								while (sample > samplingWeights[index] && index < samplingLimit) {
									sample -= samplingWeights[index];
									index++;
								}
								newTopic = currentTypeTopics[index];
							}
							else {
								sample = (sample - sparseSamplingSum) / expDigammaTopicWordSmoothing;
								newTopic = 0;
								while (sample > topicCoefficients[ newTopic ]) {
									sample -= topicCoefficients[ newTopic ];
									newTopic++;
								}
							}
														
							if (sweep >= _sampleBurnIn) {
								totalSamples++;
								if (newTopic != oldTopic) { totalChanges++; }
							}							

							topics[position] = newTopic;
							docTopicCounts[newTopic]++;
							coefficientSum -= topicCoefficients[newTopic];
							topicCoefficients[newTopic] = (_docTopicSmoothing + docTopicCounts[newTopic]) * topicNormalizers[newTopic];
							coefficientSum += topicCoefficients[newTopic];
							_wordGradientQueueWords[wordGradientLimit] = type;
							_wordGradientQueueTopics[wordGradientLimit] = newTopic;
							wordGradientLimit++;
						}
					}
				}

				docsProcessed += _batchSize;

				//System.out.format("numSparse %d init %d digamma %d exp %d sampling %d setup %d sparse %d dense %d coeff %d total %d\n", numSparseTopics, initNanos/1000, digammaNanos/1000, expNanos/1000, samplingNanos/1000, setupNanos/1000, sparseNanos/1000, denseNanos/1000, coefficientNanos/1000, (System.nanoTime() - overallStartNanos)/1000);

				// Step in the direction of the gradient

				double learningRate = Math.pow(_learningRate + iteration, -_learningRateExp);
				double oneMinusLearningRate = 1.0 - learningRate;

				scale *= oneMinusLearningRate;

				//System.out.println((System.currentTimeMillis() - startTime) + "\tsampling");
				// Now add the gradient

				double wordWeight = learningRate * _numDocs / (scale * _numSamples * _batchSize);

				for (int i = 0; i < wordGradientLimit; i++) {
					int type = _wordGradientQueueWords[i];
					int topic = _wordGradientQueueTopics[i];
					double[] weights = _typeWeights[type];
					int[] topics = _typeTopics[type];
					
					// Search through the sparse list to find position of the topic
					int index = 0;
					boolean found = false;
					while (! found && topics[index] != topic && weights[index] > 0.0) {
						index++;
					}
					// At this point either we've found the topic and stopped or found an empty position
					if (! found) { topics[index] = topic; }
					weights[index] += wordWeight;

					//typeTopicTokens[ wordGradientQueueWords[i] ][ wordGradientQueueTopics[i] ] += wordWeight;
					_topicTokenTotals[ _wordGradientQueueTopics[i] ] += wordWeight;
				}
				wordGradientLimit = 0;

				if (scale < 0.01) {
					console.info("rescaling " + iteration);
					rescale(scale);
					scale = 1.0;
					sortAndPrune(0.1);
				}

				if ((iteration + 1) % (10000 / _batchSize) == 0) {
					System.out.format("iteration %d: %dms/iter %dms %f %d tokens\n", iteration + 1, System.currentTimeMillis() - startTime, System.currentTimeMillis() - trainingStartTime, scale, tokensProcessed);
					System.out.format("%d / %d = %f\n", totalChanges, totalSamples, (double) totalChanges / totalSamples);
//					System.out.println(UnicodeBarplot.getBars(_topicTokenTotals));
					totalSamples = 0;
				}

				if ((iteration + 1) % (50000 / _batchSize) == 0) {
					console.info(topWords(30, scale));
				}

				if (docsProcessed / docsBetweenOutput > currentOutputMultiple) {
					currentOutputMultiple = docsProcessed / docsBetweenOutput; // note the int division
					
					try {
						writeWordTopicParameters("lda_" + ".wordtopics." + currentOutputMultiple, scale);
					} catch (Exception e) {
						console.info("can't write to file: " + e.getMessage());
					}
					//logger.info(topWords(30));
				}

				iteration++;
			}
		}
		
		/**
		 *  Return an array of sorted sets (one set per topic). Each set 
		 *   contains IDSorter objects with integer keys into the alphabet.
		 *   To get direct access to the Strings, use getTopWords().
		 */
		public ArrayList<TreeSet<IDSorter>> getSortedWords () {
	        
			ArrayList<TreeSet<IDSorter>> topicSortedWords = new ArrayList<TreeSet<IDSorter>>(_numTopics);

			// Initialize the tree sets
			for (int topic = 0; topic < _numTopics; topic++) {
				topicSortedWords.add(new TreeSet<IDSorter>());
			}

			// Collect counts
			for (int type = 0; type < _numTypes; type++) {

				int[] topics = _typeTopics[type];
				double[] weights = _typeWeights[type];

				int index = 0;
				while (index < topics.length && weights[index] > 0.0) {
					topicSortedWords.get(topics[index]).add(new IDSorter(type, weights[index]));
					index++;
				}
			}

			return topicSortedWords;
		}

		public String topWords (int numWords, double scale) {

			StringBuilder output = new StringBuilder();
			ArrayList<TreeSet<IDSorter>> topicSortedWords = getSortedWords();

			for (int topic = 0; topic < _numTopics; topic++) {

				TreeSet<IDSorter> sortedWords = topicSortedWords.get(topic);
				Iterator<IDSorter> iterator = sortedWords.iterator();
	                        
				output.append(topic + "\t" + (scale * _topicTokenTotals[topic]) + "\t");

				int i = 0;
				while (i < numWords && iterator.hasNext()) {
					IDSorter sorter = iterator.next();
					output.append(_vocabulary.lookupObject(sorter.getID()) + " ");
					i++;
				}
				output.append("\n");
			}

			return output.toString();
		}

		public void rescale(double scale) {
			for (int type = 0; type < _numTypes; type++) {
				double[] weights = _typeWeights[type];
				int index = 0;
				while (index < weights.length && weights[index] > 0.0) {
					weights[index] *= scale;
					index++;
				}
			}

			for (int topic = 0; topic < _numTopics; topic++) {
				_topicTokenTotals[topic] *= scale;
			}
		}

		public void sortAndPrune(double cutoff) {
			for (int type = 0; type < _numTypes; type++) {
				double[] weights = _typeWeights[type];
				int[] topics = _typeTopics[type];

				// Do a simple bubble sort, clearing low values as we go
				int sortedLimit = 0;
				while (sortedLimit < weights.length && weights[sortedLimit] > 0.0) {

					if (weights[sortedLimit] < cutoff) { 
						// Zero out low weights
						weights[sortedLimit] = 0.0;
						topics[sortedLimit] = 0;
					}
					else {
						// Make sure the current value is less than any previous values
						int i = sortedLimit - 1;
						while (i >= 0 && weights[i+1] > weights[i]) {
							int tempTopic = topics[i];
							double tempWeight = weights[i];
							topics[i] = topics[i+1];
							weights[i] = weights[i+1];
							topics[i+1] = tempTopic;
							weights[i+1] = tempWeight;
							i--;
						}
					}
					sortedLimit++;
				}
			}	
		}

		public void writeWordTopicParameters(String filename, double scale) throws Exception {
			PrintStream out = new PrintStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(filename))));

			for (int type = 0; type < _numTypes; type++) {
				Formatter formatter = new Formatter(new StringBuilder(), Locale.US);

				double[] weights = _typeWeights[type];
				int index = 0;
				while (index < weights.length && weights[index] > 0.0) {
					formatter.format("%d:%.2f\t", _typeTopics[type][index], (scale * weights[index]));
					index++;
				}
				out.println(formatter);
			}
			
			out.close();
		}

//	 	public static void main (String[] args) throws Exception {
//			CommandOption.setSummary (SampledOnlineLDA.class,
//									  "Gibbs sampling within variational inference.");
//			CommandOption.process (SampledOnlineLDA.class, args);
//		}

		@Override
		public void initializeCallBack(ComponentContextProperties ccp)
				throws Exception {
	        _numTopics = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_TOPICS, ccp));
	        _randomSeed = Integer.parseInt(getPropertyOrDieTrying(PROP_RANDOM_SEED, ccp));
	        _docTopicSmoothing = Double.parseDouble(getPropertyOrDieTrying(PROP_DOC_TOPIC_SMOOTHING, ccp));
	        _topicWordSmoothing = Double.parseDouble(getPropertyOrDieTrying(PROP_TOPIC_WORD_SMOOTHING, ccp));
	        
	        _momentum = Double.parseDouble(getPropertyOrDieTrying(PROP_MOMENTUM, ccp));;
	        _numSamples = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_SAMPLES, ccp));;
	        _sampleBurnIn = Integer.parseInt(getPropertyOrDieTrying(PROP_SAMPLE_BURN_IN, ccp));;
	        _batchSize = Integer.parseInt(getPropertyOrDieTrying(PROP_BATCH_SIZE, ccp));;
	        _learningRate = Integer.parseInt(getPropertyOrDieTrying(PROP_LEARNING_RATE, ccp));;
	        _learningRateExp = Double.parseDouble(getPropertyOrDieTrying(PROP_LEARNING_RATE_EXP, ccp));;
	        _savedWordTopics = Integer.parseInt(getPropertyOrDieTrying(PROP_SAVED_WORD_TOPICS, ccp));;
	        _wordTopicsInterval = Integer.parseInt(getPropertyOrDieTrying(PROP_WORD_TOPICS_INTERVAL, ccp));;
	        _totalFolds = Integer.parseInt(getPropertyOrDieTrying(PROP_TOTAL_FOLDS, ccp));;
	        _heldOutFold = Integer.parseInt(getPropertyOrDieTrying(PROP_HELD_OUT_FOLD, ccp));;
		}

		@Override
		public void executeCallBack(ComponentContext cc) throws Exception {
	        InstanceList instances = (InstanceList) cc.getDataComponentFromInput(IN_INSTANCE_LIST);

	        if (instances.size() > 0 && instances.get(0) != null) {
	            Object data = instances.get(0).getData();
	            if (! (data instanceof FeatureSequence))
	                throw new ComponentExecutionException("Topic modeling currently only supports feature sequences!");
	        } else {
	            console.warning("Empty instance list received - nothing to do!");
	            return;
	        }

			console.info("docs loaded " + instances.size());

//			SampledOnlineLDA trainer = new SampledOnlineLDA(instances);
			_vocabulary = instances.getDataAlphabet();
			_numTypes = _vocabulary.size();
		
	        _typeWeights = new double[_numTypes][_numTopics];
	        _typeTopics = new int[_numTypes][_numTopics];

			_topicTokenTotals = new double[_numTopics];
			
			_wordGradientQueueTopics = new int[5000000];
			_wordGradientQueueWords = new int[5000000];

			_random = new Randoms(_randomSeed);

//			trainer.setHeldOutFold(_heldOutFold, _total_folds);
//			setSampling(_num_samples, _sample_burn_in);
//			_sampleWeight = 1.0 / (_numSamples - _sampleBurnIn);
			
			train(instances,_savedWordTopics, _wordTopicsInterval);
		}

		@Override
		public void disposeCallBack(ComponentContextProperties ccp)
				throws Exception {
			// TODO Auto-generated method stub
			
		}
	}