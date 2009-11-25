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
import java.util.logging.Level;

import opennlp.tools.lang.english.Tokenizer;

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
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;

/**
 * This component tokenizes the text contained in the input model using OpenNLP.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */

@Component(
		name = "OpenNLP Tokenizer",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, tools, text, opennlp, tokenizer",
		description = "This component breaks the text contained in the input " +
				      "unsing OpenNLP tokenizing facilities.",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "maxent-models.jar", "seasr-commons.jar"}
)
public class OpenNLPTokenizer extends OpenNLPBaseUtilities {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "The text to be tokenized"
	)
	protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKENS,
			description = "The sequence of tokens"
	)
	protected static final String OUT_TOKENS = Names.PORT_TOKENS;

	//--------------------------------------------------------------------------------------------


	/** The OpenNLP tokenizer to use */
	private Tokenizer tokenizer;


	//--------------------------------------------------------------------------------------------
    public static Tokenizer build(String sOpenNLPDir, String sLanguage) throws Exception
    {
    	return new Tokenizer(sOpenNLPDir+"tokenize"+File.separator+
				sLanguage.substring(0,1).toUpperCase()+sLanguage.substring(1)+"Tok.bin.gz");
    }

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		super.initializeCallBack(ccp);

		// Initialize the tokenizer
		try {
			tokenizer = build(sOpenNLPDir,sLanguage);
		}
		catch ( Throwable t ) {
			console.log(Level.SEVERE,"Failed to open tokenizer model for " + sLanguage, t);
			throw new ComponentExecutionException(t);
		}
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		String[] inputs = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));

		StringBuilder sb = new StringBuilder();

		for (String text : inputs)
		    sb.append(text).append(" ");

		String[] ta = tokenizer.tokenize(sb.toString());

		console.fine(String.format("Extracted %,d tokens", ta.length));

		cc.pushDataComponentToOutput(OUT_TOKENS, BasicDataTypesTools.stringToStrings(ta));
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        super.disposeCallBack(ccp);
        this.tokenizer = null;
    }

	//--------------------------------------------------------------------------------------------

//	public static void main ( String [] saArgs ) throws IOException {
//		LineNumberReader lnr = new LineNumberReader(new FileReader("/Users/xavier/Desktop/1342.txt"));
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		PrintStream ps = new PrintStream(baos);
//		String sLine;
//
//		long longStart = System.currentTimeMillis();
//		while ( (sLine=lnr.readLine())!=null ) ps.println(sLine);
//		System.out.println(System.currentTimeMillis()-longStart);
//
//		longStart = System.currentTimeMillis();
//		String paragraph  = baos.toString();
//		System.out.println(System.currentTimeMillis()-longStart);
//
//		// the sentence detector and tokenizer constructors
//		// take paths to their respective models
//		longStart = System.currentTimeMillis();
//		Tokenizer tokenizer = new Tokenizer("/Users/xavier/KK/english/tokenize/EnglishTok.bin.gz");
//		System.out.println(System.currentTimeMillis()-longStart);
//
//
//		longStart = System.currentTimeMillis();
//		String[] taPB = tokenizer.tokenize(paragraph);
//		System.out.print(System.currentTimeMillis()-longStart);
//		System.out.println(" --> "+taPB.length);
//
//		for ( int i=1000, iMax=6000 ; i<=iMax ; i+=1000 ) {
//			Strings res = addToProtocolBuffer(taPB, i);
//			for ( int j=0 ; j<i ; j++ )
//				if ( !taPB[j].equals(res.getValue(j)) )
//					System.out.println("Oops");
//		}
//
//		longStart = System.currentTimeMillis();
//		String [] ta = tokenizer.tokenize(paragraph);
//		System.out.print(System.currentTimeMillis()-longStart);
//		System.out.println(" --> "+ta.length);
//
//		for ( int i=1000, iMax=6000 ; i<=iMax ; i+=1000 )
//			addToSequence(ta, i);
//
//	}
//
//	private static Strings addToProtocolBuffer(String[] ta, int j) {
//		long longStart = System.currentTimeMillis();
//		Builder top = BasicDataTypes.Strings.newBuilder();
//		for (int i=0 ; i<j ; i++ ) {
//			top.addValue(ta[i]);
//		}
//		Strings res = top.build();
//		System.out.println(System.currentTimeMillis()-longStart);
//		return res;
//	}
//
//	/**
//	 * @param ta
//	 * @param j
//	 */
//	private static void addToSequence(String[] ta, int j) {
//		long longStart;
//		Seq seqTokens;
//		longStart = System.currentTimeMillis();
//		seqTokens = ModelFactory.createDefaultModel().createSeq();
//		for ( int i=0, iMax=j ; i<iMax ; i++ )
//			seqTokens.add(ta[i]);
//		System.out.println(System.currentTimeMillis()-longStart);
//	}

}
