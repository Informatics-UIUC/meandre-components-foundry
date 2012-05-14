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
	        name = "cmp_start_idx",
	        description = "Start index to start comparing",
	        defaultValue = "0"
	)
	protected static final String PROP_COMP_START_INDEX = "cmp_start_idx";

	@ComponentProperty(
	        name = "cmp_end_idx",
	        description = "End index for focused comparison",
	        defaultValue = "1"
    )
	protected static final String PROP_COMP_END_INDEX = "cmp_end_idx";

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
	        name = "weighting_power",
	        description = "Main parameter to be controlled. Valid values are in the range 0 to 100",
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
	        name = "normalizeForThreeLives",
	        description = "Normalize for Three Lives analysis (sum to 1)",
	        defaultValue = "false"
    )
	protected static final String PROP_NORM_3LIVES = "normalizeForThreeLives";

	@ComponentProperty(
	        name = "normalizeForShakespeare",
	        description = "Normalize for Shakespeare analysis",
	        defaultValue = "true"
    )
	protected static final String PROP_NORM_SHAKESPEARE = "normalizeForShakespeare";

	//--------------------------------------------------------------------------------------------


	protected Prosody _prosody;

	protected int _cmpStartIdx;
	protected int _cmpEndIdx;

	protected int _maxPhonemesPerVol;
	protected int _numThreads;
	protected int _numRounds;
	protected double _weightingPower;
	protected int _phonemesWinSize;

	protected int _posWeight;
	protected int _accentWeight;
	protected int _stressWeight;
	protected int _toneWeight;
	protected int _phraseIdWeight;
	protected int _breakIdxWeight;

	protected boolean _normalizeForThreeLivesAnalysis;
	protected boolean _normalizeForShakespeareAnalysis;

	protected boolean _isStreaming = false;


	//--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		super.initializeCallBack(ccp);

		_cmpStartIdx = Integer.parseInt(getPropertyOrDieTrying(PROP_COMP_START_INDEX, ccp));
		_cmpEndIdx = Integer.parseInt(getPropertyOrDieTrying(PROP_COMP_END_INDEX, ccp));
		_maxPhonemesPerVol = Integer.parseInt(getPropertyOrDieTrying(PROP_MAX_PHONEMES_PER_VOL, ccp));
		_numThreads = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_THREADS, ccp));
		_numRounds = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_ROUNDS, ccp));
		_weightingPower = Double.parseDouble(getPropertyOrDieTrying(PROP_WEIGHTING_POWER, ccp));
		_phonemesWinSize = Integer.parseInt(getPropertyOrDieTrying(PROP_PHONEMES_WIN_SIZE, ccp));
		_posWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_POS_WEIGHT, ccp));
		_accentWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_ACCENT_WEIGHT, ccp));
		_stressWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_STRESS_WEIGHT, ccp));
		_toneWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_TONE_WEIGHT, ccp));
		_phraseIdWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_PHRASEID_WEIGHT, ccp));
		_breakIdxWeight = Integer.parseInt(getPropertyOrDieTrying(PROP_BREAKINDEX_WEIGHT, ccp));
		_normalizeForThreeLivesAnalysis = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_NORM_3LIVES, ccp));
		_normalizeForShakespeareAnalysis = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_NORM_SHAKESPEARE, ccp));

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

		_prosody.addData(tuplePeer, tuples);
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
		console.info("Data received, now computing similarities...");
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
		_prosody.setProblemGenerationStartTableIndex(_cmpStartIdx);
		_prosody.setProblemGenerationEndTableIndex(_cmpEndIdx);
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
		_prosody.setNormalizeForThreeLivesAnalysis(_normalizeForThreeLivesAnalysis);
		_prosody.setNormalizeForShakespearAnalysis(_normalizeForShakespeareAnalysis);
	}
}
