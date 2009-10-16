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
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.lang.english.Tokenizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.postag.POSTaggerME;
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
 * @author Mike Haberman
 *
 */

//
// DO NOT USE THIS, a work in progress
//

@Component(
		name = "OpenNLP Kitchen Sink",
		creator = "Mike Haberman",
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
			name = Names.PORT_TUPLES,
			description = "The sequence of tuples"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
	

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples: (sentenceId,type,text)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	

	//--------------------------------------------------------------------------------------------


	/** The OpenNLP tokenizer to use */
	private Tokenizer tokenizer;
	private SentenceDetectorME sdetector;
	private NameFinderME[] finders;
	private POSTaggerME tagger = null;
	private ChunkerME chunker = null;

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
			
			tagger    = OpenNLPPosTagger.build(sOpenNLPDir, sLanguage);
			
			chunker   = OpenNLPChunker.build(sOpenNLPDir, sLanguage);
			
			console.info("DONE OPENNlp");
		    
		}
		catch ( Throwable t ) {
			console.log(Level.SEVERE,"Failed to open tokenizer model for " + sLanguage, t);
			throw new ComponentExecutionException(t);
		}
		
		
		
		//
		// build the tuple (output) data
		//
		String[] fields = 
			new String[] {TUPLE_ID_FIELD, SENTENCE_ID_FIELD, TYPE_FIELD, TEXT_FIELD, TEXTCLEAN_FIELD};
		
		tuplePeer = new SimpleTuplePeer(fields);
		
		TUPLE_ID_IDX    = tuplePeer.getIndexForFieldName(TUPLE_ID_FIELD);
		SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(SENTENCE_ID_FIELD);
		TYPE_IDX        = tuplePeer.getIndexForFieldName(TYPE_FIELD);
		
		TEXT_IDX        = tuplePeer.getIndexForFieldName(TEXT_FIELD);
		TEXTCLEAN_IDX   = tuplePeer.getIndexForFieldName(TEXTCLEAN_FIELD);
	}
	
	SimpleTuplePeer tuplePeer;
	public static final String TUPLE_ID_FIELD    = "tupleId";
	public static final String SENTENCE_ID_FIELD = "sentenceId";
	public static final String TYPE_FIELD        = "type";
	public static final String TEXT_FIELD        = "text";
	public static final String TEXTCLEAN_FIELD   = "textClean";
	
	int TUPLE_ID_IDX;
	int TYPE_IDX        ;
	int SENTENCE_ID_IDX ;
	int TEXT_IDX        ;
	int TEXTCLEAN_IDX;

	static int ID = 1;
	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer inPeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple inTuple    = inPeer.createTuple();
		
		int IN_TEXT_IDX = inPeer.getIndexForFieldName("text");
		
		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		console.info("processing " + in.length);
		
		TextSpan textSpan = new TextSpan();
		List<TextSpan> spans = new ArrayList<TextSpan>();
		
		List<Strings> output = new ArrayList<Strings>();
		SimpleTuple outTuple = tuplePeer.createTuple();
		
		for (int i = 0; i < in.length; i++) {
			inTuple.setValues(in[i]);	
			
			String text = inTuple.getValue(IN_TEXT_IDX);
			console.info("==>" + text);
			
			// make it a bit easier for openNLP to tokenize
			text = text.replaceAll("-", " - ");
            
            String[] sentences = sdetector.sentDetect(text);
            
            spans.clear();
            
            for (String sentence : sentences) {
            	
            	
            	String[] tokens = tokenizer.tokenize(sentence);
            	
            	// Named Entities
            	for (int j = 0; j < finders.length; j++) {
    				String type = types[j];
    				Span[] span = finders[j].find(tokens);
    				textSpan.reset();
    				

    				for (Span s : span) {
    					
    					TextSpan tSpan = OpenNLPNamedEntity.label(sentence, tokens, s, textSpan);
    					outTuple.setValue(TUPLE_ID_IDX, ID++);
    					outTuple.setValue(SENTENCE_ID_IDX, i);
    					outTuple.setValue(TYPE_IDX, type);
    					outTuple.setValue(TEXT_IDX, tSpan.getText());
    					outTuple.setValue(TEXTCLEAN_IDX, "");
    					output.add(outTuple.convert());
    					
    					
    					/*
    					String out = tSpan.getStart() + "," + tSpan.getEnd() + "," + type + "," + tSpan.getText();
    					System.out.println("## " + out);    					
    					if (previous.equals(s)) {
    						System.out.println("## WOW " + text);
    						System.out.println("## WOW " + out);
    					}
    					previous = s;
    					*/
    				}
            	}
            	
            	// find URLS
            	List<TextSpan> urls = OpenNLPNamedEntity.findURLS(sentence);
    			for (TextSpan s : urls) {
    				
    				// String out = s.getStart() + "," + s.getEnd() + ",URL," + s.getText();
    				outTuple.setValue(TUPLE_ID_IDX, ID++);
    				outTuple.setValue(SENTENCE_ID_IDX, i);
					outTuple.setValue(TYPE_IDX, "URL");
					outTuple.setValue(TEXT_IDX, s.getText());
					outTuple.setValue(TEXTCLEAN_IDX, "");
					output.add(outTuple.convert());
					
    			}
            	
      
            	
            	//
            	// chunking
            	//
            	
            	/*
            	String[] tags   = tagger.tag(tokens);
    			String[] chunks = chunker.chunk(tokens, tags);
    			console.info(OpenNLPChunker.toString(tokens,tags,chunks));
    			 * 
    			 */
    
            	
            } // end over sentences
            
		
		} // end of tuples
		
		
		// push the whole collection, protocol safe   
	    Strings[] results = new Strings[output.size()];
	    output.toArray(results);
	    
	    StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
	    cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	    //
		// metaData for this tuple producer
		//
	    cc.pushDataComponentToOutput(OUT_META_TUPLE, tuplePeer.convert());
	   
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        super.disposeCallBack(ccp);
        this.tokenizer = null;
    }

	

}

