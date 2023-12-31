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

import java.util.ArrayList;
import java.util.List;

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
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.components.analytics.statistics.Prosody;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.util.KeyValuePair;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Prosody Similarity",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#ANALYTICS, similarity, prosody, tuple",
        description = "This component calculates prosody similarity between documents",
        dependency = { "protobuf-java-2.2.0.jar" }
)
public class ProsodySimilarity extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The modified tuple(s)" +
                "<br>TYPE: same as input"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the modified tuples (same as input plus the new attribute)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
	        name = "comparison_range",
	        description = "The comma-separated set of indices of the documents that should be compared with all " +
	        		"other documents. For example, using '1' means that only the first document will " +
	        		"be compared with all others. Using '1,3' means that the first and third document " +
	        		"submitted will be compared with all others. Using 'all' means that everything will " +
	        		"be compared with everything else.",
	        defaultValue = "all"
    )
	protected static final String PROP_COMP_RANGE = "comparison_range";

	@ComponentProperty(
	        name = "max_phonemes_per_vol",
	        description = "The maximum number of phonemes allowed per volume",
	        defaultValue = "999999999"
    )
	protected static final String PROP_MAX_PHONEMES_PER_VOL = "max_phonemes_per_vol";

	@ComponentProperty(
	        name = "num_threads",
	        description = "The number of CPU threads to use",
	        defaultValue = "16"
    )
	protected static final String PROP_NUM_THREADS = "num_threads";

	@ComponentProperty(
	        name = "num_rounds",
	        description = "The number of sampling rounds to use (relevant when using sampling)",
	        defaultValue = "1"
    )
	protected static final String PROP_NUM_ROUNDS = "num_rounds";

	@ComponentProperty(
	        name = "use_sampling",
	        description = "Should sampling be used? (useful for very large data sets)",
	        defaultValue = "false"
    )
	protected static final String PROP_USE_SAMPLING = "use_sampling";

	@ComponentProperty(
	        name = "weighting_power",
	        description = "Main parameter to be controlled. Valid values are in the range 0 to 100. " +
	        		"High values cause it to behave like nearest-neighbor - finds the closest window; " +
	        		"At lower values, it uses a larger neighborhood size to make the instance-based prediction; " +
	        		"When set to zero, it equally weighs all examples.",
	        defaultValue = "32.0"
    )
	protected static final String PROP_WEIGHTING_POWER = "weighting_power";

	@ComponentProperty(
	        name = "phonemes_window_size",
	        description = "Window size in phonemes",
	        defaultValue = "8"
    )
	protected static final String PROP_PHONEMES_WIN_SIZE = "phonemes_window_size";

	@ComponentProperty(
	        name = "pos_weight",
	        description = "Weight for part of speech (set to 0 to ignore this feature)",
	        defaultValue = "1"
    )
	protected static final String PROP_POS_WEIGHT = "pos_weight";

	@ComponentProperty(
	        name = "accent_weight",
	        description = "Weight for accent (set to 0 to ignore this feature)",
	        defaultValue = "1"
    )
	protected static final String PROP_ACCENT_WEIGHT = "accent_weight";

	@ComponentProperty(
	        name = "stress_weight",
	        description = "Weight for stress (set to 0 to ignore this feature)",
	        defaultValue = "1"
    )
	protected static final String PROP_STRESS_WEIGHT = "stress_weight";

	@ComponentProperty(
	        name = "tone_weight",
	        description = "Weight for tone (set to 0 to ignore this feature)",
	        defaultValue = "1"
    )
	protected static final String PROP_TONE_WEIGHT = "tone_weight";

	@ComponentProperty(
	        name = "phraseId_weight",
	        description = "Weight for phrase id (set to 0 to ignore this feature)",
	        defaultValue = "1"
    )
	protected static final String PROP_PHRASEID_WEIGHT = "phraseId_weight";

	@ComponentProperty(
	        name = "breakIndex_weight",
	        description = "Weight for break index (set to 0 to ignore this feature)",
	        defaultValue = "1"
    )
	protected static final String PROP_BREAKINDEX_WEIGHT = "breakIndex_weight";

	@ComponentProperty(
	        name = "phonemeId_weight",
	        description = "Weight for phoneme id (set to 0 to ignore this feature)",
	        defaultValue = "0"
    )
	protected static final String PROP_PHONEMEID_WEIGHT = "phonemeId_weight";


	//--------------------------------------------------------------------------------------------


	protected Prosody _prosody;

	protected List<Integer> _focusedComparisonIndexes = new ArrayList<Integer>();

	protected int _numThreads;

	protected int _maxPhonemesPerVol;
	protected double _weightingPower;
	protected int _phonemesWinSize;

	protected int _posWeight;
	protected int _accentWeight;
	protected int _stressWeight;
	protected int _toneWeight;
	protected int _phraseIdWeight;
	protected int _breakIdxWeight;
	protected int _phonemeIdWeight;

	protected boolean _useSampling;
	protected int _numRounds;

	protected boolean _isStreaming = false;


	//--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		super.initializeCallBack(ccp);

		String cmpRange = getPropertyOrDieTrying(PROP_COMP_RANGE, ccp);
		if (!cmpRange.equalsIgnoreCase("all")) {
			String[] indexes = cmpRange.split(",");
			for (String index : indexes)
				_focusedComparisonIndexes.add(Integer.parseInt(index.trim()) - 1); // convert to zero-based
		}

		_maxPhonemesPerVol = Integer.parseInt(getPropertyOrDieTrying(PROP_MAX_PHONEMES_PER_VOL, ccp));
		_numThreads = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_THREADS, ccp));
		_weightingPower = Double.parseDouble(getPropertyOrDieTrying(PROP_WEIGHTING_POWER, ccp));
		_phonemesWinSize = Integer.parseInt(getPropertyOrDieTrying(PROP_PHONEMES_WIN_SIZE, ccp));
		_posWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_POS_WEIGHT, ccp));
		_accentWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_ACCENT_WEIGHT, ccp));
		_stressWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_STRESS_WEIGHT, ccp));
		_toneWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_TONE_WEIGHT, ccp));
		_phraseIdWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_PHRASEID_WEIGHT, ccp));
		_breakIdxWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_BREAKINDEX_WEIGHT, ccp));
		_phonemeIdWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_PHONEMEID_WEIGHT, ccp));
		_numRounds = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_ROUNDS, ccp));
		_useSampling = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_USE_SAMPLING, ccp));

		reset();
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		if (!_isStreaming)
			throw new ComponentExecutionException("This component can only run in streaming mode!");

		Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        StringsArray inTuple = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);

		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inMeta);
		Strings[] tuples = BasicDataTypesTools.stringsArrayToJavaArray(inTuple);

		int index = _prosody.addData(tuplePeer, tuples);
		if (_focusedComparisonIndexes.size() == 0)
			_prosody.addIndexToFocusedComparison(index);
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		_prosody = null;
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public boolean isAccumulator() {
		return true;
	}

	@Override
	public void startStream() throws Exception {
		reset();
		_isStreaming = true;
	}

	@Override
	public void endStream() throws Exception {
		console.fine("Data received, now computing similarities...");

		for (int index : _focusedComparisonIndexes)
			_prosody.addIndexToFocusedComparison(index);

		_prosody.computeSimilarities();

		List<KeyValuePair<SimpleTuplePeer, Strings[]>> output = _prosody.getOutput();
		for (KeyValuePair<SimpleTuplePeer, Strings[]> doc : output) {
			componentContext.pushDataComponentToOutput(OUT_META_TUPLE, doc.getKey().convert());
			componentContext.pushDataComponentToOutput(OUT_TUPLES, BasicDataTypesTools.javaArrayToStringsArray(doc.getValue()));
		}

		_prosody = null;
		_isStreaming = false;
	}

	//--------------------------------------------------------------------------------------------

	protected void reset() {
		_prosody = new Prosody();
		_prosody.setLogger(console);
		_prosody.setMaxNumPhonemesPerVolume(_maxPhonemesPerVol);
		_prosody.setNumThreads(_numThreads);
		_prosody.setNumRounds(_numRounds);
		_prosody.setWeightingPower(_weightingPower);
		_prosody.setWindowSizeInPhonemes(_phonemesWinSize);
		_prosody.setPartOfSpeechWeight(_posWeight);
		_prosody.setAccentWeight(_accentWeight);
		_prosody.setStressWeight(_stressWeight);
		_prosody.setToneWeight(_toneWeight);
		_prosody.setPhraseIdWeight(_phraseIdWeight);
		_prosody.setBreakIndexWeight(_breakIdxWeight);
		_prosody.setPhonemeIdWeight(_phonemeIdWeight);
		_prosody.setUseSampling(_useSampling);
	}
}
