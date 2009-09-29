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
public class AnalyticsComponentTests {

	/** The component test base supporting class for these tests. */
	private static ComponentTesterBase ctb = null;

	/** Generates the descriptors for the specified folders */
	@BeforeClass
	public static void initializeTestResources () {

		ctb = new ComponentTesterBase();
		ctb.setBaseTestPort(50000);
		ctb.setFlowsFolder("./test/flows/analytics");
		ctb.setTempDescriptorFolder("./tmp");
		ctb.setTempDescriptorFolder("./tmp/desc/analytics");
		ctb.setSourceFolders(new String [] { "./src-jstor", "./src-tools", "./src-opennlp", "./src-analytics"} );
		ctb.initialize();
	}


    /** Generates the descriptors for the specified folders */
	@AfterClass
	public static void destroyTestResources () {
		// Clean the temp folder
		ctb.printCoverageReport();
		//ctb.destroy();
	}


	/** Test token counter on a sample text and then convert the text to text.  */
	@Test
	public void testTokenCounterToText() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ctb.runZigZag(ctb.getZigZag("token-counter-to-text.zz"),out,err);

		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertEquals(24,sa.length);
		assertTrue(sa[0].startsWith("Available token counts in the model (ordered by count)"));
	}


	/** Test token counter on a sample text and then convert the text to text.  */
	@Test
	public void testFilteredTokenCounterToText() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ctb.runZigZag(ctb.getZigZag("token-counter-to-text-with-filter.zz"),out,err);

		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertEquals(9,sa.length);
		assertTrue(sa[0].startsWith("Available token counts in the model (ordered by count)"));
		assertTrue(sa[8].startsWith(",: 1"));
	}


	/** Test token counter on a sample text and then convert the text to text.  */
	@Test
	public void testFilteredAfterTokenCounterToText() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ctb.runZigZag(ctb.getZigZag("token-counter-to-text-with-filter-counts.zz"),out,err);

		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertEquals(9,sa.length);
		assertTrue(sa[0].startsWith("Available token counts in the model (ordered by count)"));
		assertTrue(sa[8].startsWith(",: 1"));
	}

	/** Test tokenized sentences on a sample text and then convert the text to text. */
	@Test
	public void testFilteredTokenizedSentencesToText() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ctb.runZigZag(ctb.getZigZag("text-sentence-tokenizer-with-filter.zz"),out,err);

		String expected = IOUtils.getTextFromReader(new FileReader(
		        "test/flows/analytics/text-sentence-tokenizer-with-filter.zz.out".replaceAll("/", File.separator)))
		        .replaceAll("\r|\n", "");
		String actual = out.toString().replaceAll("\r|\n", "");

		assertEquals(expected, actual);
	}

	/** Test tokenized sentences and HITS summarization.  */
	@Test
	public void testFilteredTokenizedSentencesAndHits() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ctb.runZigZag(ctb.getZigZag("text-sentence-tokenizer-with-hits.zz"),out,err);

		String sRes = out.toString();
		assertTrue(sRes.indexOf("This isn't the greatest example sentence in the world because I've seen better.")<sRes.indexOf("This one's not bad, though."));
		assertTrue(sRes.indexOf("This one's not bad, though.")<sRes.indexOf("Neither is this one."));
	}

	/** Test token counter on a sample text and then convert the text to text.  */
	@Test
	public void testTokenCounterWithReducerText() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ctb.runZigZag(ctb.getZigZag("token-counter-with-reducer.zz"),out,err);

		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertEquals(28,sa.length);
		assertTrue(sa[0].indexOf("(accumulated=1)")>0);
		assertTrue(sa[0].indexOf("(count=9)")>0);
		assertTrue(sa[27].indexOf("(accumulated=1)")>0);
		assertTrue(sa[27].indexOf("(count=9)")>0);
	}


}
