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

package org.seasr.meandre.component.opennlp;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.util.regex.Pattern;


import opennlp.tools.lang.english.PosTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.lang.english.TreebankChunker;


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

import org.seasr.meandre.components.tools.Names;


import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;


import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsArray;
import org.seasr.datatypes.BasicDataTypes.StringsMap;


/**
 * This component perform POS tagging on the text passed using OpenNLP.
 *
 * @author Mike Haberman;
 *
 */

//
// General Path:  Text -> SentenceDetector -> SentenceTokenizer -> Chunker
//

@Component(
		name = "OpenNLP Chunker",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
		description = "This component performs treebank chunking on the incoming set of tokenized sentences " +
				      "unsing OpenNLP " +
				      "facilities.",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "maxent-models.jar"}
)
public class OpenNLPChunker extends OpenNLPBaseUtilities {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKENIZED_SENTENCES,
			description = "The sequence of tokenized sentences"
	)
	protected static final String IN_TOKENS = Names.PORT_TOKENIZED_SENTENCES;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of tuples: (sentenceId,text)"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples: (sentenceId,text)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	//----------------------------- PROPERTIES ---------------------------------------------------

	

	//--------------------------------------------------------------------------------------------


	/** The OpenNLP tokenizer to use */
	private POSTaggerME tagger = null;
	private ChunkerME chunker = null;
 
   public static ChunkerME build(String sOpenNLPDir, String sLanguage) throws IOException 
   {
	   
	    // from maxent-models.jar
	    // ==> opennlp.1.4.3/models/English/chunker/EnglishChunk.bin.gz
	    // NOT opennlp/models/English/parser/chunk.bin.gz
	   
		String chunkPath = 
		    sOpenNLPDir + "chunker" + File.separator + "EnglishChunk.bin.gz";

		
		File chunkFile = new File(chunkPath);

		if (! chunkFile.canRead()) {
			throw new IOException("Failed to open chunk file for " + chunkPath);
		}
		
		ChunkerME tagger = new TreebankChunker(chunkPath);
		return tagger;
		
   }
   
   public static String toString(String[] tokens, String[] tags, String[] chunks) 
   {
	    // see TreebankChunker.java 	
		StringBuffer out = new StringBuffer();
		for (int j=0; j < chunks.length; j++) {
			if (j > 0 && !chunks[j].startsWith("I-") && !chunks[j-1].equals("O")) {
				out.append("]");
			}
			if (chunks[j].startsWith("B-")) {
				out.append("[" + chunks[j].substring(2) );
			}
			out.append(":" + tokens[j] + "/" + tags[j]);
		}
		if (!chunks[chunks.length-1].equals("O")) {
			out.append("]");
		} 
		return out.toString();
   }
   
   public static List<TextChunk> toChunks(String[] tokens, String[] tags, String[] chunks) 
   {
	   List<TextChunk> allChunks = new ArrayList<TextChunk>();
	   TextChunk currentChunk = new TextChunk();
	   
	    // see above
		for (int j=0; j < chunks.length; j++) {
			if (j > 0 && !chunks[j].startsWith("I-") && !chunks[j-1].equals("O")) {
				// end of a chunk
				allChunks.add(currentChunk);
			}
			if (chunks[j].startsWith("B-")) {
				// start of a chunk
				currentChunk = new TextChunk(chunks[j].substring(2));
			}
			currentChunk.add(tokens[j], tags[j]);
		}
		if (!chunks[chunks.length-1].equals("O")) {
			// end of a chunk
			allChunks.add(currentChunk);
		}
		return allChunks;
   }

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {

		super.initializeCallBack(ccp);

		try {	
			tagger  = OpenNLPPosTagger.build(sOpenNLPDir, sLanguage);
			chunker = build(sOpenNLPDir, sLanguage);
		}
		catch ( Throwable t ) {
			console.severe("Failed to open tokenizer model for " + sLanguage);
			throw new ComponentExecutionException(t);
		}

		
		String[] fields = 
			new String[] {SENTENCE_ID_FIELD, TEXT_FIELD};
		
		this.tuplePeer = new SimpleTuplePeer(fields);
		
	}
	
	
	SimpleTuplePeer tuplePeer;
	
	public static final String SENTENCE_ID_FIELD = "sentenceId";
	public static final String TEXT_FIELD        = "text";

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception
	{

		List<Strings> output = new ArrayList<Strings>();
		SimpleTuple tuple = tuplePeer.createTuple();

		StringsMap input = (StringsMap) cc.getDataComponentFromInput(IN_TOKENS);

        int count = input.getKeyCount();
		console.fine("processing " + count);

		int SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(SENTENCE_ID_FIELD);
		int TEXT_IDX        = tuplePeer.getIndexForFieldName(TEXT_FIELD);
		
		
		 
		for (int i = 0; i < count; i++) {
			String key    = input.getKey(i);    // this is the entire sentence
			Strings value = input.getValue(i);  // this is the set of tokens for that sentence

			String[] tokens = DataTypeParser.parseAsString(value);
			String[] tags   = tagger.tag(tokens);
			String[] chunks = chunker.chunk(tokens, tags);
			
			// console.info(key);
		    String markUp = toString(tokens, tags, chunks);
		    
		    tuple.setValue(SENTENCE_ID_IDX, i);
		    tuple.setValue(TEXT_IDX, markUp);
		    output.add(tuple.convert());
		}

		
		// push the whole collection, protocol safe
	    Strings[] results = new Strings[output.size()];
	    output.toArray(results);
	    
	    StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
	    cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	    //
		// metaData for this tuple producer
		//
	    Strings metaData = tuplePeer.convert();
	    cc.pushDataComponentToOutput(OUT_META_TUPLE, metaData);
	    
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        super.disposeCallBack(ccp);
        this.tagger = null;
        this.chunker = null;
    }
}
