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
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.nlp.opennlp.OpenNLPBaseUtilities;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;


/**
 * Provides a statistical parser using the Stanford's NER components.
 *  DO NOT USE.  It is a hack job for a specific use case.
 *  This component will be refactored into an actual usable statistical parser
 * @author Mike Haberman
 */


/*
 * NOTES:
 * see http://nlp.stanford.edu/software/parser-faq.shtml for details
 *
 * based on the 2010-02-26 distribution
 *
 * stanfordParsers.jar was built by hand by including the following models:
   arabicFactored.ser.gz
   wsjFactored.ser.gz
   atbP3FactoredBuckwalter.ser.gz
   wsjPCFG.ser.gz
   chineseFactored.ser.gz
   xinhuaFactored.ser.gz
   englishFactored.ser.gz
   xinhuaFactoredSegmenting.ser.gz
   englishPCFG.ser.gz
   xinhuaPCFG.ser.gz
   germanFactored.ser.gz
 *
 */


@Component(
		name = "Stanford Parser",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
		description = "This component is NOT for general Use. DO NOT USE.",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "stanfordParsers.jar"}
)
public class StanfordStatisticalParser extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The text to be parsed"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS ------------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The processed text (this will be replaced with tuples soon"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;


    /*
    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "set of tuples: (pos,sentenceId,offset,token)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;
    */

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "meta data for tuples: (text)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ PROPERTIES --------------------------------------------------

    // Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent

	static final String DEFAULT_PARSER = "parsers/englishPCFG.ser.gz";

	@ComponentProperty(
			name = "parserModel",
			description = "The parser model to be used ",
		    defaultValue = DEFAULT_PARSER
		)
	protected static final String PROP_PARSER = "parserModel";

	@ComponentProperty(
			name = "modelsDir",
			description = "models directory, if non-empty, skip install",
		    defaultValue = ""
		)
	protected static final String PROP_MODELS_DIR = "modelsDir";

	//--------------------------------------------------------------------------------------------


	SimpleTuplePeer tuplePeer;

	public static final String POS_FIELD         = "pos";
	public static final String SENTENCE_ID_FIELD = "sentenceId";
	public static final String TOKEN_START_FIELD = "tokenStart";
	public static final String TOKEN_FIELD       = "token";

	int count        = 0;
	int sentenceId   = 0;
	int globalOffset = 0;
	int startIdx     = 0;


	TreePrint  treePrint;
    LexicalizedParser parser = null;


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception
    {

		parser    = buildParser(ccp, console, getClass());
		treePrint = new TreePrint("typedDependencies");

		sentenceId   = 0;
		startIdx     = 0;

		String[] fields = new String[]{"text"};

    		// new String[] {POS_FIELD, SENTENCE_ID_FIELD, TOKEN_START_FIELD, TOKEN_FIELD};

    	this.tuplePeer = new SimpleTuplePeer(fields);
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception
    {
		Strings input = (Strings) cc.getDataComponentFromInput(IN_TEXT);
		String[] val = BasicDataTypesTools.stringsToStringArray (input);


		// console.info(count++ + " attempt to parse\n" + val[0]);

		String SEP = "|";

		String text = val[0];
		int idx = text.lastIndexOf(SEP);
		String prefix = text.substring(0,idx+1); // safe even if idx == -1
		text = text.substring(idx+1);


		// String[] tokens = prepForSplitting(sentence).split(" ");


		StringReader reader = new StringReader(text);
		List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(reader);

		/*
		SimpleTuple tuple   = tuplePeer.createTuple();
		int POS_IDX         = tuplePeer.getIndexForFieldName(POS_FIELD);
		int SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(SENTENCE_ID_FIELD);
		int TOKEN_START_IDX = tuplePeer.getIndexForFieldName(TOKEN_START_FIELD);
		int TOKEN_IDX       = tuplePeer.getIndexForFieldName(TOKEN_FIELD);
		*/


		List<Strings> output = new ArrayList<Strings>();
		StringBuilder sb = new StringBuilder();

		for (Sentence<? extends HasWord> sentence : sentences) {

			if (sentence.length() > 128) {
				// TODO make this a property
				continue;
			}

			Sentence<? extends HasWord> fixed = fixSentence(sentence);

			console.fine("Processing\n" + fixed);
			Tree parse = parser.apply(fixed);
			List<String[]> parts = singleSentenceParse(parse);

			sb.setLength(0);
	        for (String[] col : parts) {
	        	String rest = StringUtils.join(col, SEP);
	        	sb.append(prefix);
	 	        sb.append(rest);
	 	        sb.append(SEP).append("\n");
	        }
	        output.add(BasicDataTypesTools.stringToStrings(sb.toString()));

			//String sText = sentence.toString();
			//console.info("text is " + sText);

			/*
		      Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);

		      for (TaggedWord word : tSentence) {

		    	   String text = word.value();
		    	   int indexOfLastWord = originalText.indexOf(text, startIdx);

		    	   tuple.setValue(POS_IDX,         word.tag());
				   tuple.setValue(SENTENCE_ID_IDX, sentenceId);  // keep this zero based
				   tuple.setValue(TOKEN_START_IDX, indexOfLastWord);
				   tuple.setValue(TOKEN_IDX,       text);


		    	   // console.info(tuple.toString());

		    	   startIdx = indexOfLastWord + text.length();

		    	   if ( pattern == null || pattern.matcher(word.tag()).matches())
				   {
		    		   output.add(tuple.convert());
		    	   }


		      }
		      */
		      sentenceId++;
		      //console.info(tSentence.toString(false));
		}


		 Strings[] results = new Strings[output.size()];
		 output.toArray(results);

		 StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
		 cc.pushDataComponentToOutput(OUT_TEXT, outputSafe);



		/*
		// push the whole collection, protocol safe
	    Strings[] results = new Strings[output.size()];
	    output.toArray(results);

	    StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
	    cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
	    */

	    //
		// metaData for this tuple producer
		//
	    Strings metaData = tuplePeer.convert();
	    cc.pushDataComponentToOutput(OUT_META_TUPLE, metaData);

    }

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}

    //--------------------------------------------------------------------------------------------

    /* untested
    public static String prepForSplitting(String text)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int type = Character.getType(c);
            if (Character.isWhitespace(c)     ||
                Character.isLetterOrDigit(c)  ||
                (type != Character.END_PUNCTUATION)) {
                sb.append(c);
            }
            else {
                // assume it's not part of a word/token
                sb.append(" ").append(c).append(" ");
            }
        }
        String out = sb.toString().replaceAll("\\s+", " ");
        return out;
    }
    */

    public List<String[]> singleSentenceParse(Tree parse)
    {
        StringWriter sw = new StringWriter();
        treePrint.printTree(parse, new PrintWriter(sw));
        String sws = sw.toString();

        List<String[]> output = new ArrayList<String[]>();
        String[] list = sws.split("\n");
        for (int x = 0; x < list.length; x++) {
            String a = list[x].trim();

            // e.g. nsubj(attributable-12  features-bob-6)
            a = a.replaceFirst("\\(", " ");      // rid the ()
            a = a.replaceFirst("\\)$", " ");
            a = a.replace(",",  " ");            // rid the ,
            a = a.replaceAll("-(\\d+)", " $1 "); // rid the -[0-9]+
            String[] cols = a.split("\\s+");     // pos word offset word
            output.add(cols);
        }
        return output;
    }

    LexicalizedParser buildParser(ComponentContextProperties ccp, Logger logger, Class<?> myClass)
       throws Exception
    {
        String parserFile = getPropertyOrDieTrying(PROP_PARSER, true, true, ccp);

        String modelsDir = getPropertyOrDieTrying(PROP_MODELS_DIR, true, false, ccp);
        if (modelsDir.length() == 0)
            modelsDir = ccp.getRunDirectory()+File.separator+"stanfordNLP";

        OpenNLPBaseUtilities.installJARModelContainingResource(modelsDir, parserFile, logger, myClass);
        logger.fine("Installed models into: " + modelsDir);

        return new LexicalizedParser(modelsDir + File.separator + parserFile.trim());
    }

	public static Sentence<? extends HasWord> fixSentence(Sentence<? extends HasWord> sentence)
	{
		//
		// MaxEnt Tagger replaces () with -LRB- and -RRB-
		//
		// want to keep bob(neal) as a single token
		// but NOT bob(a friend of neal)
		//
		// sentences contain -LRB- -RRB-
		//

		String tmp = sentence.toString();
		if (tmp.indexOf("-LRB-") == -1) {
			return sentence;
		}

		Sentence<Word> fixed = new Sentence<Word>();

		int size = sentence.size();
		for (int i = 0; i < size; i++) {

			HasWord w = sentence.get(i);

			Word word = null;
			if (i+3 < size) {

				HasWord n = sentence.get(i+1);

				// this only handles one level of ()'s
				if (n.toString().equals("-LRB-")) {
					HasWord content = sentence.get(i+2);
					HasWord rrb     = sentence.get(i+3);
					if (rrb.toString().equals("-RRB-")) {
						word = new Word(w + "(" + content + ")");
						i += 3;
					}
				}
			}

			if (word == null) {
				word = new Word(w.toString());
			}
			fixed.add(word);
		}
		return fixed;
	}
}

