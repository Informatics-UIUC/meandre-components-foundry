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

package org.seasr.meandre.components.analytics.mallet;

import java.io.File;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

import cc.mallet.types.InstanceList;

/**
 * Sampled Online LDA
 *
 * This component wraps code created by David Mimno.  Reference paper: http://www.cs.princeton.edu/~mimno/papers/mimno2012sparse.pdf
 * Original code: http://www.cs.princeton.edu/~mimno/code/SampledOnlineLDA.java
 *
 * Adapted for Meandre by Loretta Auvil and Boris Capitanu
 */

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
public class SampledOnlineLDA extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "mallet_instance_list",
            description = "The list of machine learning instances" +
                "<br>TYPE: cc.mallet.types.InstanceList"
    )
    protected static final String IN_INSTANCE_LIST = "mallet_instance_list";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "mallet_instance_list",
            description = "Same as input" +
                "<br>TYPE: cc.mallet.types.InstanceList"
    )
    protected static final String OUT_INSTANCE_LIST = "mallet_instance_list";

    //----------------------------- PROPERTIES ---------------------------------------------------

    //  static cc.mallet.util.CommandOption.Integer numTopicsOption = new cc.mallet.util.CommandOption.Integer
    //	(SampledOnlineLDA.class, "num-topics", "INTEGER", true, 10, "The number of topics", null);
    @ComponentProperty(
            name = "num_topics",
            description = "The number of topics to fit",
            defaultValue = "10"
    )
    protected static final String PROP_NUM_TOPICS = "num_topics";

    //	static cc.mallet.util.CommandOption.Double docTopicSmoothingOption = new cc.mallet.util.CommandOption.Double
    //	(SampledOnlineLDA.class, "alpha", "POS NUMBER", true, 0.1, "Dirichlet parameter for symmetric prior over document-topic distributions. This is the value for each dimension.", null);
    @ComponentProperty(
            name = "alpha",
            description = "(DocTopicSmoothing) Dirichlet parameter for symmetric prior over document-topic distributions. This is the value for each dimension.",
            defaultValue = "0.1"
    )
    protected static final String PROP_ALPHA = "alpha";

    //  static cc.mallet.util.CommandOption.Double topicWordSmoothingOption = new cc.mallet.util.CommandOption.Double
    //	(SampledOnlineLDA.class, "beta", "POS NUMBER", true, 0.1, "Dirichlet parameter for symmetric prior over topic-word distributions. This is the value for each dimension.", null);
    @ComponentProperty(
            name = "beta",
            description = "(TopicWordSmoothing) Dirichlet parameter for symmetric prior over topic-word distributions. This is the value for each dimension.",
            defaultValue = "0.1"
    )
    protected static final String PROP_BETA = "beta";

    //	static cc.mallet.util.CommandOption.Integer randomSeed = new cc.mallet.util.CommandOption.Integer
    //	(SampledOnlineLDA.class, "random-seed", "INTEGER", true, 1, "An initial seed for the random number generator", null);
    @ComponentProperty(
            name = "random_seed",
            description = "The random seed for the Gibbs sampler.  Default is 0, which will use the clock.",
            defaultValue = "1"
    )
    protected static final String PROP_RANDOM_SEED = "random_seed";

    //	static cc.mallet.util.CommandOption.Double momentumOption = new cc.mallet.util.CommandOption.Double
    //	(SampledOnlineLDA.class, "momentum", "FILENAME", true, 0.0, "Weighting of the previous gradient.", null);
    @ComponentProperty(
            name = "momentum",
            description = "Weighting of the previous gradient.",
            defaultValue = "0.0"
    )
    protected static final String PROP_MOMENTUM = "momentum";

    //	static cc.mallet.util.CommandOption.Integer numSamplesOption = new cc.mallet.util.CommandOption.Integer
    //	(SampledOnlineLDA.class, "num-samples", "INTEGER", true, 5, "The number of topics", null);
    @ComponentProperty(
            name = "num_samples",
            description = "The number of topics.",
            defaultValue = "5"
    )
    protected static final String PROP_NUM_SAMPLES = "num_samples";

    //	static cc.mallet.util.CommandOption.Integer sampleBurnInOption = new cc.mallet.util.CommandOption.Integer
    //	(SampledOnlineLDA.class, "sample-burn-in", "INTEGER", true, 2, "The number of topics", null);
    @ComponentProperty(
            name = "sample_burn_in",
            description = "The number of topics.",
            defaultValue = "2"
    )
    protected static final String PROP_SAMPLE_BURN_IN = "sample_burn_in";

    //	static cc.mallet.util.CommandOption.Integer batchSizeOption = new cc.mallet.util.CommandOption.Integer
    //	(SampledOnlineLDA.class, "batch-size", "INTEGER", true, 100, "The number of instances to examine before updating parameters", null);
    @ComponentProperty(
            name = "batch_size",
            description = "The number of instances to examine before updating parameters.",
            defaultValue = "100"
    )
    protected static final String PROP_BATCH_SIZE = "batch_size";

    //	static cc.mallet.util.CommandOption.Double learningRateOption = new cc.mallet.util.CommandOption.Double
    //	(SampledOnlineLDA.class, "learning-rate", "INTEGER", true, 100.0, "The learning rate will be 1.0 / ([this value] + t)", null);
    @ComponentProperty(
            name = "learning_rate",
            description = "The learning rate will be 1.0 / ([this value] + t).",
            defaultValue = "100.0"
    )
    protected static final String PROP_LEARNING_RATE = "learning_rate";

    //  static cc.mallet.util.CommandOption.Double learningRateExponentOption = new cc.mallet.util.CommandOption.Double
    //	(SampledOnlineLDA.class, "learning-rate-exponent", "NUMBER", true, 0.6, "The learning rate will be pow(1.0 / (offset + t), [this value]). Must be between 0.5 and 1.0", null);
    @ComponentProperty(
            name = "learning_rate_exponent",
            description = "The learning rate will be pow(1.0 / (offset + t), [this value]). Must be between 0.5 and 1.0.",
            defaultValue = "0.6"
    )
    protected static final String PROP_LEARNING_RATE_EXP = "learning_rate_exponent";

    //  static cc.mallet.util.CommandOption.Integer savedStatesCountOption = new cc.mallet.util.CommandOption.Integer
    //	(SampledOnlineLDA.class, "saved-wordtopics", "INTEGER", true, 100, "The total number of instances to examine", null);
    @ComponentProperty(
            name = "saved_wordtopics",
            description = "The total number of instances to examine.",
            defaultValue = "100"
    )
    protected static final String PROP_SAVED_WORDTOPICS = "saved_wordtopics";

    //  static cc.mallet.util.CommandOption.Integer docsBetweenSavedStates = new cc.mallet.util.CommandOption.Integer
    //	(SampledOnlineLDA.class, "wordtopics-interval", "INTEGER", true, 500000, "The number of instances to examine between saved states", null);
    @ComponentProperty(
            name = "wordtopics_interval",
            description = "The number of instances to examine between saved states.",
            defaultValue = "500000"
    )
    protected static final String PROP_WORDTOPICS_INTERVAL = "wordtopics_interval";

    //  static cc.mallet.util.CommandOption.Integer foldCountOption = new cc.mallet.util.CommandOption.Integer
    //	(SampledOnlineLDA.class, "total-folds", "INTEGER", true, 0, "The number of equal-sized held-out cross validation folds. A value 0 will use all data.", null);
    @ComponentProperty(
            name = "total_folds",
            description = "The number of equal-sized held-out cross validation folds. A value 0 will use all data.",
            defaultValue = "0"
    )
    protected static final String PROP_TOTAL_FOLDS = "total_folds";

    //  static cc.mallet.util.CommandOption.Integer heldOutFoldOption = new cc.mallet.util.CommandOption.Integer
    //	(SampledOnlineLDA.class, "held-out-fold", "INTEGER", true, 0, "The index of the cross validation fold to hold out, starting with 0.", null);
    @ComponentProperty(
            name = "held_out_fold",
            description = "The index of the cross validation fold to hold out, starting with 0.",
            defaultValue = "0"
    )
    protected static final String PROP_HELD_OUT_FOLD = "held_out_fold";

    //  static cc.mallet.util.CommandOption.String outputPrefix = new cc.mallet.util.CommandOption.String
    //  (SampledOnlineLDA.class, "output-prefix", "STRING", true, "o-lda", "The prefix for output files (sampling states, parameters, etc)", null);
    @ComponentProperty(
            name = "output_prefix",
            description = "The prefix for output files (sampling states, parameters, etc)",
            defaultValue = "o-lda"
    )
    protected static final String PROP_OUTPUT_PREFIX = "output_prefix";

    @ComponentProperty(
            name = Names.PROP_DEFAULT_FOLDER,
            description = "The folder to write to. If the specified location " +
                    "is not an absolute path, it will be assumed relative to the " +
                    "published_resources folder.",
            defaultValue = ""
    )
    protected static final String PROP_DEFAULT_FOLDER = Names.PROP_DEFAULT_FOLDER;

    //--------------------------------------------------------------------------------------------


    protected int _numTopics;
    protected double _alpha;
    protected double _beta;
    protected int _randomSeed;
    protected double _momentum; //never used
    protected int _numSamples;
    protected int _sampleBurnIn;
    protected int _batchSize;
    protected double _learningRate;
    protected double _learningRateExp;
    protected int _savedWordTopics;
    protected int _wordTopicsInterval;
    protected int _totalFolds;
    protected int _heldOutFold;
    protected String _outputPrefix;

    protected String defaultFolder;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _numTopics 			= Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_TOPICS, ccp));
        _alpha 				= Double.parseDouble(getPropertyOrDieTrying(PROP_ALPHA, ccp));
        _beta 				= Double.parseDouble(getPropertyOrDieTrying(PROP_BETA, ccp));
        _randomSeed 		= Integer.parseInt(getPropertyOrDieTrying(PROP_RANDOM_SEED, ccp));
        _momentum 			= Double.parseDouble(getPropertyOrDieTrying(PROP_MOMENTUM, ccp));
        _numSamples 		= Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_SAMPLES, ccp));
        _sampleBurnIn 		= Integer.parseInt(getPropertyOrDieTrying(PROP_SAMPLE_BURN_IN, ccp));
        _batchSize 			= Integer.parseInt(getPropertyOrDieTrying(PROP_BATCH_SIZE, ccp));
        _learningRate 		= Double.parseDouble(getPropertyOrDieTrying(PROP_LEARNING_RATE, ccp));
        _learningRateExp 	= Double.parseDouble(getPropertyOrDieTrying(PROP_LEARNING_RATE_EXP, ccp));
        _savedWordTopics 	= Integer.parseInt(getPropertyOrDieTrying(PROP_SAVED_WORDTOPICS, ccp));
        _wordTopicsInterval = Integer.parseInt(getPropertyOrDieTrying(PROP_WORDTOPICS_INTERVAL, ccp));
        _totalFolds 		= Integer.parseInt(getPropertyOrDieTrying(PROP_TOTAL_FOLDS, ccp));
        _heldOutFold 		= Integer.parseInt(getPropertyOrDieTrying(PROP_HELD_OUT_FOLD, ccp));
        _outputPrefix		= getPropertyOrDieTrying(PROP_OUTPUT_PREFIX, ccp);

        defaultFolder = getPropertyOrDieTrying(PROP_DEFAULT_FOLDER, true, false, ccp);
        if (defaultFolder.length() == 0)
            defaultFolder = ccp.getPublicResourcesDirectory();
        else
            if (!defaultFolder.startsWith(File.separator))
                defaultFolder = new File(ccp.getPublicResourcesDirectory(), defaultFolder).getAbsolutePath();

        if (!defaultFolder.endsWith(File.separator))
            defaultFolder += File.separator;

        console.fine("Default folder set to: " + defaultFolder);

        _outputPrefix = defaultFolder + _outputPrefix;
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        InstanceList instances = (InstanceList) cc.getDataComponentFromInput(IN_INSTANCE_LIST);

        console.fine("docs loaded " + instances.size());

        edu.princeton.cs.mimno.topics.SampledOnlineLDA.setLogger(console);
        edu.princeton.cs.mimno.topics.SampledOnlineLDA trainer =
                new edu.princeton.cs.mimno.topics.SampledOnlineLDA(instances, _numTopics, _alpha, _beta,
                        _batchSize, _learningRate, _learningRateExp, _randomSeed);

        trainer.setOutputPrefix(_outputPrefix);
        trainer.setHeldOutFold(_heldOutFold, _totalFolds);
        trainer.setSampling(_numSamples, _sampleBurnIn);

        trainer.train(_savedWordTopics, _wordTopicsInterval);

        cc.pushDataComponentToOutput(OUT_INSTANCE_LIST, instances);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
