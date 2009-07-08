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
import java.io.FileInputStream;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

import opennlp.tools.lang.english.PosTagger;
import opennlp.tools.postag.POSDictionary;

//
// NOTE for openNLP versions:
// latest
// import opennlp.tools.dictionary.Dictionary;
// old version == see constructor
import opennlp.tools.ngram.Dictionary;

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
import org.seasr.datatypes.BasicDataTypes;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.parsers.DataTypeParser;

/**
 * This component perform POS tagging on the text passed using OpenNLP.
 *
 * @author Mike Haberman;
 *
 */

//
// General Path:  Text -> SentenceDetector -> SentenceTokenizer -> PosTagger 
//


@Component(
		name = "OpenNLP POS Tagger",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
		description = "This component tags the incoming set of tokenized sentences " +
				      "unsing OpenNLP pos facilities.",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.0.3.jar", "maxent-models.jar"}
)
public class OpenNLPPosTagger extends OpenNLPBaseUtilities {

    //------------------------------ INPUTS ------------------------------------------------------
	
	@ComponentInput(
			name = Names.PORT_TOKENS,
			description = "The sequence of tokens"
	)
	protected static final String IN_TOKENS = Names.PORT_TOKENS;
	

    //------------------------------ OUTPUTS -----------------------------------------------------
	
	@ComponentOutput(
			name = Names.PORT_POS_TUPLES,
			description = "set of tuples: (POS,sentenceId,offset,token)"
	)
	protected static final String OUT_POS_TUPLES = Names.PORT_POS_TUPLES;
	
	
	//----------------------------- PROPERTIES ---------------------------------------------------
	@ComponentProperty(
			name = Names.PROP_POS_FILTER_REGEX,
			description = "optional regular expression to inline filter POS (e.g. JJ|RB)",
		    defaultValue = ""
		)
	protected static final String PROP_POS_FILTER_REGEX = Names.PROP_POS_FILTER_REGEX;

	
	/** The OpenNLP tokenizer to use */
	private PosTagger tagger = null;
	Pattern pattern = null;

	//--------------------------------------------------------------------------------------------

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		
		super.initializeCallBack(ccp);
		
		String regex = ccp.getProperty(PROP_POS_FILTER_REGEX).trim();
		if (regex.length() > 0) {
			pattern = Pattern.compile(regex);
		}
		
		
		try {
			// from maxent-models.jar
			String tagPath          = // e.g.  /opennlp/models/English/parser/tag.bin.gz
			    sOpenNLPDir + "parser" + File.separator+"tag.bin.gz";
			
			String dictionaryPath    = // e.g. /opennlp/models/English/parser/dict.bin.gz
				sOpenNLPDir + "parser"+ File.separator+"dict.bin.gz";
			
			String tagDictionaryPath = // e.g. /opennlp/models/English/parser/tagdict
				sOpenNLPDir + "parser"+ File.separator+"tagdict";
			
			File tagFile     = new File(tagPath);
			File dictFile    = new File(dictionaryPath);
			File tagDictFile = new File(tagDictionaryPath);
			
			if (! tagFile.canRead()) {
				console.severe("Failed to open tag file for " + tagPath);
				throw new ComponentExecutionException();
			}
			if (! dictFile.canRead()) {
				console.severe("Failed to open dictionary model for " + dictionaryPath);
				throw new ComponentExecutionException();
			}
			if (! tagDictFile.canRead()) {
				console.severe("Failed to open tag dictionary model for " + tagDictionaryPath);
				throw new ComponentExecutionException();
			}
			
			
			/*  NEW WAY, untested */
			/*
			InputStream dIs  = new FileInputStream(dictFile);
			tagger = new PosTagger(tagPath, 
					               new Dictionary(dIs), 
					               new POSDictionary(tagDictionaryPath, true));
					               
		    */
			
			/* OLD WAY */
			tagger = new PosTagger(tagPath, 
					               new Dictionary(dictionaryPath), 
					               new POSDictionary(tagDictionaryPath, true));
			
		}
		catch ( Throwable t ) {
			console.severe("Failed to open tokenizer model for " + sLanguage);
			throw new ComponentExecutionException(t);
		}
	}

	
	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		
		List<String> output = new ArrayList<String>();
		
		
		// input was encoded via :
		// cc.pushDataComponentToOutput(OUT_TOKENS, BasicDataTypesTools.stringToStrings(ta));
		//
		
		//
		// NEED a parser here ? DataTypeParser.parseAsMap ???
		//
		StringsMap input = (StringsMap) cc.getDataComponentFromInput(IN_TOKENS);
		
        int globalOffset = 0;
		int count = input.getKeyCount();
		console.fine("processing " + count);
		
		for (int i = 0; i < count; i++) {
			String key    = input.getKey(i);    // this is the entire sentence
			Strings value = input.getValue(i);  // this is the set of tokens for that sentence
			
			String[] tokens = DataTypeParser.parseAsString(value);
			String[] tags   = tagger.tag(tokens);
			
			int withinSentenceOffset = 0;
			for (int j = 0; j < tags.length; j++) {
				
				if ( pattern == null || 
					(pattern != null && pattern.matcher(tags[j]).matches()))
				{
					
					// find where the token is in the sentence
					int tokenStart = key.indexOf(tokens[j], withinSentenceOffset);
					// add in the global offset
					tokenStart += globalOffset;
					
					
					String result = PosTuple.toString(tags[j], i, tokenStart, tokens[j]);
					
					//
					// we have a choice at this point:
					// we can push out each result
					// or we can collect all the results
					// and push out an array of results
					//
					console.info(result);
					
					output.add(result);
					
					/*
					cc.pushDataComponentToOutput(OUT_POS_TUPLE,  // note a SINGLE tuple
							BasicDataTypesTools.stringToStrings(result));
					*/

				}
				
				withinSentenceOffset += tokens[j].length();
				
			}
			// add the key's length, not the offset
			// since the key will contain white space
			// we need a true index
			globalOffset += key.length();
		}
		
		// push the whole collection, protocol safe
	    String[] results = new String[output.size()];
	    output.toArray(results);
	    Strings outputSafe = BasicDataTypesTools.stringToStrings(results);
	    cc.pushDataComponentToOutput(OUT_POS_TUPLES, outputSafe);
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        super.disposeCallBack(ccp);
        this.tagger = null;
    }
}
