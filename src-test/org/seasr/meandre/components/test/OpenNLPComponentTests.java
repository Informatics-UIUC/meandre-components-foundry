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

package org.seasr.meandre.components.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.components.test.framework.ComponentTesterBase;
import org.seasr.meandre.support.generic.io.IOUtils;

/** The base class for performing component testing.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class OpenNLPComponentTests {

	/** The component test base supporting class for these tests. */
	private static ComponentTesterBase ctb = null;

	/** Generates the descriptors for the specified folders */
	@BeforeClass
	public static void initializeTestResources () {
		ctb = new ComponentTesterBase();
		ctb.setBaseTestPort(50000);
		ctb.setFlowsFolder("."+File.separator+"test"+File.separator+"flows"+File.separator+"opennlp");
		ctb.setTempDescriptorFolder("."+File.separator+"tmp");
		ctb.setTempDescriptorFolder("."+File.separator+"tmp"+File.separator+"desc"+File.separator+"opennlp");
		ctb.setSourceFolders(new String [] {"src-analytics","src-nlp","src-sentiment","src-tools","src-transform","src-vis"} );
		ctb.initialize();
	}

    /** Generates the descriptors for the specified folders */
	@AfterClass
	public static void destroyTestResources () {
		// Clean the temp folder
		ctb.printCoverageReport();
		//ctb.destroy();
	}

	/** Test the tokenizer on dummy text components. */
	@Test
	public void testTokenizer() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ctb.runZigZag(ctb.getZigZag("text-wrapped-model-with-tokenizer.zz"),out,err);

		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertTrue(sa.length==29);
		assertEquals("This",sa[0]);
		assertEquals("better",sa[14]);
		assertEquals(".",sa[28]);
	}


	/** Test the sentence detection on dummy text components. */
	@Test
	public void testSentenceDetection() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ctb.runZigZag(ctb.getZigZag("text-wrapped-model-with-sentence-detector.zz"),out,err);

		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertTrue(sa.length==3);
		assertTrue(sa[0].startsWith("This isn't the"));
		assertTrue(sa[1].startsWith("Neither is"));
		assertTrue(sa[2].startsWith("This one's not bad"));
	}


	/** Test the sentence detection on dummy text components.  */
	@Test
	public void testSentenceTokenizer() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ctb.runZigZag(ctb.getZigZag("text-sentence-tokenizer.zz"),out,err);

		String expected = IOUtils.getTextFromReader(new FileReader(
                "test"+File.separator+"flows"+File.separator+"opennlp"+File.separator+"text-sentence-tokenizer.zz.out"))
                .replaceAll("\r|\n", "");
        String actual = out.toString().replaceAll("\r|\n", "");

        assertEquals(expected, actual);
	}
}
