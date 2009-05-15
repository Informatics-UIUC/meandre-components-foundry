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

import opennlp.tools.lang.english.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;

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
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.Names;

/** This component does sentence detection on the text contained in the input model using OpenNLP.
 *
 * @author Xavier Llor�
 *
 */

@Component(
		name = "OpenNLP sentence detector",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"trove.jar","protobuf-java-2.0.3.jar"},
		resources = "opennlp-english-models.jar",
		tags = "semantic, tools, text, opennlp, sentence detector",
		description = "This component splits sentences of the text contained in the input  " +
				      "unsing OpenNLP tokenizing facilities."
)
public class OpenNLPSentenceDetector
extends OpenNLPBaseUtilities
implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------

	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "The text to be split into sentences"
		)
	private final static String INPUT_TEXT = Names.PORT_TEXT;

	@ComponentOutput(
			name = Names.PORT_SENTENCES,
			description = "The sequence of sentences"
		)
	private final static String OUTPUT_SENTENCES = Names.PORT_SENTENCES;

	//--------------------------------------------------------------------------------------------

	/** The OpenNLP tokenizer to use */
	private SentenceDetectorME sdetector;

	//--------------------------------------------------------------------------------------------

	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		super.initialize(ccp);
		try {
			sdetector = new SentenceDetector(
					ccp.getRunDirectory()+File.separator+
					"opennlp"+File.separator+"models"+File.separator+
					sLanguage+File.separator+"sentdetect"+File.separator+
					sLanguage.substring(0,1).toUpperCase()+sLanguage.substring(1)+"SD.bin.gz");
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
		this.sdetector = null;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		Object obj = cc.getDataComponentFromInput(INPUT_TEXT);
		if ( obj instanceof StreamDelimiter )
			cc.pushDataComponentToOutput(OUTPUT_SENTENCES, obj);
		else {
			Strings strRes = BasicDataTypesTools.stringToStrings("");
			try {
				Strings strText = (Strings)obj;
				StringBuffer sb = new StringBuffer();
				for ( String s:strText.getValueList() ) sb.append(s);
				String sText = sb.toString();
				String[] sa = sdetector.sentDetect(sText);
				// Creating the token sequence
				strRes = BasicDataTypesTools.stringToStrings(sa);
			} catch (ClassCastException e ) {
				String sMessage = "Input data is not from the basic type Strings";
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( !bErrorHandling )
					throw new ComponentExecutionException(e);
			}
			cc.pushDataComponentToOutput(OUTPUT_SENTENCES, strRes);
		}
	}

	//--------------------------------------------------------------------------------------------

//	public static void main ( String [] saArgs ) throws IOException {
//		new OpenNLPSentenceDetector();
//		String paragraph  = "This isn't the greatest example sentence in the world because I've seen better. Neither is this one. This one's not bad, though.";
//
//		// the sentence detector and tokenizer constructors
//		// take paths to their respective models
//		SentenceDetectorME sdetector = new SentenceDetector("/Users/xavier/KK/english/sentdetect/EnglishSD.bin.gz");
//		Tokenizer tokenizer = new Tokenizer("/Users/xavier/KK/english/tokenize/EnglishTok.bin.gz");
//
//		String [] ta = tokenizer.tokenize(paragraph);
//		for ( String t:ta )
//			System.out.println(t);
//
//		String [] sa = sdetector.sentDetect(paragraph);
//		for ( String s:sa )
//			System.out.println(s);
//
//
//
//	}

}
