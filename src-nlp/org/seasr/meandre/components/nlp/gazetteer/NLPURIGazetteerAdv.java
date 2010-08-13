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

package org.seasr.meandre.components.nlp.gazetteer;


import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.components.utils.ComponentUtils;
import org.seasr.meandre.support.generic.io.StreamUtils;


/**
 * This component perform Named Entity Extraction via Stanford's NER facility
 *
 * @author Surya Kallumadi;
 *
 */

//
// General Path:  Text -> SentenceDetector -> SentenceTokenizer -> Gazetteer
//

@Component(
		name = "OpenNLPGazetteer",
		creator = "Surya Kallumadi",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, text, nlp, information extraction, entity, entity extraction",
		description = "This component performs named entity tagging using Stanford's NLP facilities",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "StandAloneGaz.jar", "seasr-commons.jar"}
)
public class NLPURIGazetteerAdv extends AbstractExecutableComponent {

	//------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_MESSAGE,
            description = "The localtion of the list file ",
            defaultValue = "file://"
    )
    protected static final String PROP_MESSAGE = Names.PROP_MESSAGE;

    //------------------------------ INPUTS ------------------------------------------------------

//	@ComponentInput(
//			name = Names.PORT_TEXT,
//			description = "The text to be split into sentences"
//	)
//	protected static final String IN_TEXT = Names.PORT_TEXT;

	@ComponentInput(
            name = Names.PORT_LOCATION,
            description = "The Sentence ID"
    )
    protected static final String IN_LOCATION = Names.PORT_LOCATION;

	@ComponentInput(
			name = Names.PORT_SENTENCES,
			description = "The collection of sentence to be tokenized" +
    			 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_SENTENCES = Names.PORT_SENTENCES;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of tuples: (sentenceId,type,textStart,text)" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples: (sentenceId,type,textStart,text)" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	//----------------------------- PROPERTIES ---------------------------------------------------

	/*
	 * 	ner-eng-ie.crf-3-all2008-distsim.ser.gz
		ner-eng-ie.crf-3-all2008.ser.gz
		ner-eng-ie.crf-4-conll-distsim.ser.gz
		ner-eng-ie.crf-4-conll.ser.gz

	 */
//modify this to the list files and list files directory to be used
//	@ComponentProperty(
//			name = "classifierModel",
//			description = "The classifier model to be used ",
//		    defaultValue = "modelsEnglish/ner-eng-ie.crf-3-all2008.ser.gz"
//		)
//	protected static final String PROP_TAGGER = "classifierModel";
//
//	@ComponentProperty(
//			name = "modelsDir",
//			description = "models directory, if non-empty, skip install",
//		    defaultValue = ""
//		)
//	protected static final String PROP_MODELS_DIR = "modelsDir";


   /*
	@ComponentProperty(
			name = "ExtendedNETypes",
			description = "Extended Named Entties types:(url).",
		    defaultValue = "url"
	)
	protected static final String PROP_EX_NE_TYPES = "ExtendedNETypes";


	@ComponentProperty(
			name = "LocationMap",
	        description = "values for locations to be remapped. These are values missed or skipped by OpenNLP. " +
	        "values are comma separated:  <text to find> = <replacement>, ",
	        defaultValue = "Ill.=Illinois, VA=Virginia"
	)
    protected static final String PROP_LOCATION_MAP = "LocationMap";
    String[] extendedFinders = {"url"};
	StaticTextSpanFinder[] simpleFinders = null;
	simpleFinders = OpenNLPNamedEntity.buildExtendedFinders(types,null);

    */

	//--------------------------------------------------------------------------------------------


	AdvGazetteerWrapper gazHelper;

	protected String modelsDir;
	protected String taggerFile;
	protected String sMessage;


	//--------------------------------------------------------------------------------------------
    int count = 0;
    int id=0;

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception
    {
        //taggerFile = getPropertyOrDieTrying(PROP_TAGGER, true, true, ccp);

//		modelsDir = getPropertyOrDieTrying(PROP_MODELS_DIR, true, false, ccp);
		//if (modelsDir.length() == 0)
		  //  modelsDir = ccp.getRunDirectory()+File.separator+"stanfordNLP";//modify this when details available

		//OpenNLPBaseUtilities.installJARModelContainingResource(modelsDir, taggerFile, console, getClass());
		//console.fine("Installed models into: " + modelsDir);

		//AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions(modelsDir + File.separator + taggerFile);
    	 sMessage = ccp.getProperty(PROP_MESSAGE);
		gazHelper = new AdvGazetteerWrapper();//not required
	}


    @Override
    public void executeCallBack(ComponentContext cc) throws Exception
    {

    	// input was encoded via :
    	// cc.pushDataComponentToOutput(OUT_TOKENS, BasicDataTypesTools.stringToStrings(ta));

    	//
    	String[] inputs = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_SENTENCES));

		//String[] val = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));
		String[] location = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LOCATION));
		//String[] msg=DataTypeParser.parseAsString(cc.getDataComponentFromInput(PROP_MESSAGE));
		//String text = val[0];
		//console.finest(count++ + " attempt to parse\n" + text);
		URL loc = StreamUtils.getURLforResource(DataTypeParser.parseAsURI(sMessage));



		List<Strings> output = new ArrayList<Strings>();

	//	StringTokenizer st =new StringTokenizer(text,".;");
		count=0;
		for (String sentence : inputs)

		{
			count++;
	    List<SimpleTuple> tuples = gazHelper.toTuples(sentence,count,location[0],loc.getPath());
		for (SimpleTuple tuple : tuples) {
		   output.add(tuple.convert());
		}
		}
        // push the whole collection, protocol safe
        Strings[] results = new Strings[output.size()];
        output.toArray(results);

        StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
        cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

        //
    	// metaData for this tuple producer
    	//
        SimpleTuplePeer tuplePeer = gazHelper.getTuplePeer();
        cc.pushDataComponentToOutput(OUT_META_TUPLE, tuplePeer.convert());
    }

    @Override

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {

    	sMessage = null;
    }
    //--------------------------------------------------------------------------------------------

	@Override
	protected void handleStreamInitiators() throws Exception {
	    if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_SENTENCES, IN_LOCATION })))
	        console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

	    componentContext.pushDataComponentToOutput(OUT_TUPLES,
	            componentContext.getDataComponentFromInput(IN_SENTENCES));
	    componentContext.pushDataComponentToOutput(OUT_META_TUPLE,
	            componentContext.getDataComponentFromInput(IN_LOCATION));
	}

	@Override
	protected void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_SENTENCES, IN_LOCATION })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

	    componentContext.pushDataComponentToOutput(OUT_TUPLES,
                componentContext.getDataComponentFromInput(IN_SENTENCES));
	    componentContext.pushDataComponentToOutput(OUT_META_TUPLE,
                componentContext.getDataComponentFromInput(IN_LOCATION));
	}
	//--------------------------------------------------------------------------------------------
//    @Override
//    protected void handleStreamInitiators() throws Exception {
//        StreamInitiator si = (StreamInitiator)componentContext.getDataComponentFromInput(IN_TEXT);
//        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, si);
//        componentContext.pushDataComponentToOutput(OUT_TUPLES, ComponentUtils.cloneStreamDelimiter(si));
//    }
//
//    @Override
//    protected void handleStreamTerminators() throws Exception {
//        StreamTerminator st = (StreamTerminator)componentContext.getDataComponentFromInput(IN_TEXT);
//        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, st);
//        componentContext.pushDataComponentToOutput(OUT_TUPLES, ComponentUtils.cloneStreamDelimiter(st));
//    }


}