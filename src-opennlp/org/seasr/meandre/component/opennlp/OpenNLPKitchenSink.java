package org.seasr.meandre.component.opennlp;

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



import java.io.File;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;

import opennlp.tools.lang.english.SentenceDetector;
import opennlp.tools.lang.english.Tokenizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.util.Span;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 * This component tokenizes the text contained in the input model using OpenNLP.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */

@Component(
		name = "OpenNLP Kitchen Sink",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, tools, text, opennlp, tokenizer",
		description = "This component breaks the text contained in the input " +
				      "unsing OpenNLP tokenizing facilities.",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "maxent-models.jar"}
)

public class OpenNLPKitchenSink extends OpenNLPBaseUtilities {

  //------------------------------ INPUTS ------------------------------------------------------
	
	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "set of tuples"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;
	
	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
	

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKENS,
			description = "The sequence of tokens"
	)
	protected static final String OUT_TOKENS = Names.PORT_TOKENS;

	//--------------------------------------------------------------------------------------------


	/** The OpenNLP tokenizer to use */
	private Tokenizer tokenizer;
	private SentenceDetectorME sdetector;
	private NameFinderME[] finders;

	//--------------------------------------------------------------------------------------------
	String[] types = {"person", "location", "date", "organization"};
	
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		super.initializeCallBack(ccp);

		// Initialize the tokenizer
		try {
			
			console.info("loading OPENNlp");
			
			sdetector = OpenNLPSentenceDetector.build(sOpenNLPDir, sLanguage);
			
			tokenizer = OpenNLPTokenizer.build(sOpenNLPDir, sLanguage);
			
			finders   = OpenNLPNamedEntity.build(sOpenNLPDir, types);
			
			console.info("DONE OPENNlp");
		    
		}
		catch ( Throwable t ) {
			console.log(Level.SEVERE,"Failed to open tokenizer model for " + sLanguage, t);
			throw new ComponentExecutionException(t);
		}
	}

	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = tuplePeer.createTuple();
		
		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		int TEXT_IDX = tuplePeer.getIndexForFieldName("text");
		
		TextSpan textSpan = new TextSpan();
		List<TextSpan> spans = new ArrayList<TextSpan>();
		
		for (int i = 0; i < in.length; i++) {
			tuple.setValues(in[i]);	
			
			String text = tuple.getValue(TEXT_IDX);
			// console.info("==>" + text);
            text = text.replaceAll("[\\.\\?,;!]+", " .");
            
            String[] sentences = sdetector.sentDetect(text);
            
            spans.clear();
            
            Boolean didPrint = false;
            int numCount = 0;
            for (String sentence : sentences) {
            	
            	// console.info("  ==>" + sentence);
            	String[] tokens = tokenizer.tokenize(sentence);
            	
            	for (String t : tokens) {
            		if (t.matches("[0-9]+")) {numCount++;}
            	}
            	
            	
            	for (int j = 0; j < finders.length; j++) {
    				String type = types[j];
    				Span[] span = finders[j].find(tokens);
    				textSpan.reset();
    				for (Span s : span) {
    					
    					/*
    					if (!didPrint) {
    						console.info(sentence);
    						didPrint = true;
    					}
    					*/
    					
    					TextSpan tSpan = OpenNLPNamedEntity.label(sentence, tokens, s, textSpan);
    					console.info(tSpan.getStart() + " " + tSpan.getEnd() + " " + type + " " + tSpan.getText());
    					// tuple.setValue(text, type, textSpan.text, textSpan.start, textSpan.end, )
    				}
            	}
            	
            } // end over sentences
            
            if (numCount == 2) {
        		console.info("PAIR " + text);
        	}
            
		
		} // end of tuples
		

		// cc.pushDataComponentToOutput(OUT_TOKENS, BasicDataTypesTools.stringToStrings(ta));
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        super.disposeCallBack(ccp);
        this.tokenizer = null;
    }

	

}

