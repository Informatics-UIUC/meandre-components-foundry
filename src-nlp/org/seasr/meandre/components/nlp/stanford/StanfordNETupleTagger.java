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

package org.seasr.meandre.components.nlp.stanford;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.nlp.opennlp.OpenNLPBaseUtilities;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;



/**
 * This component perform Named Entity Extraction via Stanford's NER facility
 *
 * @author Mike Haberman;
 *
 */

//
// This performs just like Stanford NE Tagger, but on values in a tuple
// AND it then associates those togged tuples with the parent tuple
// it adds a pid field to the tagged tuple
//

@Component(
		name = "Stanford NE Tuple Tagger",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, text, nlp, information extraction, entity, entity extraction",
		description = "This component performs named entity tagging using Stanford's NLP facilities",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "stanfordModels.jar", "seasr-commons.jar"}
)
public class StanfordNETupleTagger extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------


	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "incoming tuples, from which a field will be used to process text" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;

	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for incoming tuples" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------
	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "same as input" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "same as input" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
	
	@ComponentOutput(
			name = "neTuples",
			description = "set of ne tuples: (sentenceId,type,textStart,text, pid)" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_NE_TUPLES = "neTuples";

	@ComponentOutput(
			name = "neMeta_tuple",
			description = "meta data for ne tuples: (sentenceId,type,textStart,text, pid)" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_NE_META_TUPLE = "neMeta_tuple";

	//----------------------------- PROPERTIES ---------------------------------------------------

	/*
	 * 	ner-eng-ie.crf-3-all2008-distsim.ser.gz
		ner-eng-ie.crf-3-all2008.ser.gz
		ner-eng-ie.crf-4-conll-distsim.ser.gz
		ner-eng-ie.crf-4-conll.ser.gz

	 */

	@ComponentProperty(
			name = "classifierModel",
			description = "The classifier model to be used ",
		    defaultValue = "modelsEnglish/ner-eng-ie.crf-3-all2008.ser.gz"
		)
	protected static final String PROP_TAGGER = "classifierModel";

	@ComponentProperty(
			name = "modelsDir",
			description = "models directory, if non-empty, skip install",
		    defaultValue = ""
		)
	protected static final String PROP_MODELS_DIR = "modelsDir";
	
	@ComponentProperty(
			name = "textField",
			description = "the field name of the incoming tuples that you want to do N.E. tagging on",
		    defaultValue = ""
		)
	protected static final String PROP_TEXT = "textField";
	
	@ComponentProperty(
			name = "idField",
			description = "the field name of the incoming tuples that you want to use as a key for the labelled tuples",
		    defaultValue = ""
		)
	protected static final String PROP_ID = "idField";
	
	


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


	StanfordNEWrapper neHelper;

	protected String modelsDir;
	protected String taggerFile;
 

	String pidField = "pid";
	int pidFieldIdx = 0;
	
	
	
	
    String textFieldName;
	String idFieldName;
	//--------------------------------------------------------------------------------------------
    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception
    {
        taggerFile = getPropertyOrDieTrying(PROP_TAGGER, true, true, ccp);

		modelsDir = getPropertyOrDieTrying(PROP_MODELS_DIR, true, false, ccp);
		if (modelsDir.length() == 0)
		    modelsDir = ccp.getRunDirectory()+File.separator+"stanfordNLP";

		OpenNLPBaseUtilities.installJARModelContainingResource(modelsDir, taggerFile, console, getClass());
		console.fine("Installed models into: " + modelsDir);

		AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions(modelsDir + File.separator + taggerFile);
		
		
		textFieldName = getPropertyOrDieTrying(PROP_TEXT, true, true,ccp);
		idFieldName   = getPropertyOrDieTrying(PROP_ID,   true, true, ccp);
		
		
		String[] fields = new String[]{pidField};
		
		neHelper = new StanfordNEWrapper(classifier, fields);
		SimpleTuplePeer peer = neHelper.getTuplePeer();
		pidFieldIdx = peer.getIndexForFieldName(pidField);
		
		if (pidFieldIdx == -1) {
			throw new RuntimeException("BAD programmer");
		}
		
		
	}
    

    
	@Override
    public void executeCallBack(ComponentContext cc) throws Exception
    {
		
		int idFieldIdx = 0;
	    int textIdx = 0;
		
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		

		idFieldIdx = tuplePeer.getIndexForFieldName(idFieldName);
		textIdx    = tuplePeer.getIndexForFieldName(textFieldName);
		
		if (idFieldIdx == -1) {
			console.info("WARNING no field named " + idFieldName);
			console.info(tuplePeer.toString());
		}
		if (textIdx == -1) {
			console.info("WARNING no field named " + textFieldName);
			console.info(tuplePeer.toString());
		}
		
		

		SimpleTuple tuple = tuplePeer.createTuple();
		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		
		List<Strings> output = new ArrayList<Strings>();
		
		
		for (int i = 0; i < in.length; i++) {
			tuple.setValues(in[i]);
			
			String text = tuple.getValue(textIdx);
			String id   = tuple.getValue(idFieldIdx);
			
			List<SimpleTuple> neTuples = neHelper.toTuples(text);
			for (SimpleTuple t : neTuples) {
				t.setValue(pidFieldIdx, id);
				output.add(t.convert());
			}
			
			// do any custom labeling here
		}
		
		
		// push the original input
		cc.pushDataComponentToOutput(OUT_TUPLES, input);
		cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
		
		
		// now push the new N.E. tuples
		
		// push the whole collection, protocol safe
        Strings[] results = new Strings[output.size()];
        output.toArray(results);

        StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
        cc.pushDataComponentToOutput(OUT_NE_TUPLES, outputSafe);

        //
    	// metaData for this tuple producer
    	//
        tuplePeer = neHelper.getTuplePeer();
        cc.pushDataComponentToOutput(OUT_NE_META_TUPLE, tuplePeer.convert());
        
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------
/*
    @Override
    protected void handleStreamInitiators() throws Exception {
        StreamInitiator si = (StreamInitiator)componentContext.getDataComponentFromInput(IN_TEXT);
        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, si);
        componentContext.pushDataComponentToOutput(OUT_TUPLES, ComponentUtils.cloneStreamDelimiter(si));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        StreamTerminator st = (StreamTerminator)componentContext.getDataComponentFromInput(IN_TEXT);
        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, st);
        componentContext.pushDataComponentToOutput(OUT_TUPLES, ComponentUtils.cloneStreamDelimiter(st));
    }
    */

    //--------------------------------------------------------------------------------------------



}
