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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import opennlp.tools.lang.english.Tokenizer;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.BasicDataTypes;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;

/** This component tokenizes the senteces passed on the input model using OpenNLP.
 *
 * @author Xavier Llor�
 *
 */
@Component(
		name = "OpenNLP sentence tokenizer",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"trove.jar","protobuf-java-2.0.3.jar"},
		resources = "opennlp-english-models.jar",
		tags = "semantic, tools, text, opennlp, tokenizer, sentences",
		description = "This component breaks the text contained in the input sentences " +
				      "unsing OpenNLP tokenizing facilities."
)
public class OpenNLPSentenceTokenizer
extends OpenNLPBaseUtilities
implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------


	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_SENTENCES,
			description = "The collection of sentence to be tokenized"
		)
	private final static String INPUT_SENTENCES = Names.PORT_SENTENCES;

	@ComponentOutput(
			name = Names.PORT_TOKENIZED_SENTENCES,
			description = "The tokenized sentences"
		)
	private final static String OUTPUT_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;

	//--------------------------------------------------------------------------------------------

	/** The OpenNLP tokenizer to use */
	private Tokenizer tokenizer;

	//--------------------------------------------------------------------------------------------

	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		super.initialize(ccp);
		try {
			tokenizer = new Tokenizer(
					ccp.getRunDirectory()+File.separator+
					"opennlp"+File.separator+"models"+File.separator+
					sLanguage+File.separator+"tokenize"+File.separator+
					sLanguage.substring(0,1).toUpperCase()+sLanguage.substring(1)+"Tok.bin.gz");
		}
		catch ( Throwable t ) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(baos));
			throw new ComponentExecutionException("Failed to open tokenizer model for "+sLanguage+". Cannot recover from this error. "+baos.toString());
		}
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		super.dispose(ccp);
		this.tokenizer = null;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		Object obj = cc.getDataComponentFromInput(INPUT_SENTENCES);
		if ( obj instanceof StreamDelimiter )
			cc.pushDataComponentToOutput(OUTPUT_TOKENIZED_SENTENCES, obj);
		else {
			StringsMap smRes = BasicDataTypesTools.buildEmptyStringsMap();
			try {
				org.seasr.datatypes.BasicDataTypes.StringsMap.Builder res = BasicDataTypes.StringsMap.newBuilder();
				for ( String sSent:((Strings)obj).getValueList() ) {
					String [] ta = tokenizer.tokenize(sSent);
					Strings tokens = BasicDataTypesTools.stringToStrings(ta);
					res.addKey(sSent);
					res.addValue(tokens);
				}
				smRes = res.build();
			} catch (ClassCastException e ) {
				String sMessage = "Input data is not a semantic model";
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( !bErrorHandling )
					throw new ComponentExecutionException(e);
			}

			cc.pushDataComponentToOutput(OUTPUT_TOKENIZED_SENTENCES, smRes);
		}
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
