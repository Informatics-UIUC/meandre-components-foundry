package org.seasr.meandre.components.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seasr.meandre.components.ComponentTesterBase;

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
	public void testFileterdTokenCounterToText() { 
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
	public void testFileterdAfterTokenCounterToText() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("token-counter-to-text-with-filter-counts.zz"),out,err);
	
		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertEquals(9,sa.length);
		assertTrue(sa[0].startsWith("Available token counts in the model (ordered by count)"));
		assertTrue(sa[8].startsWith(",: 1"));
	}

	/** Test tokenized sentences on a sample text and then convert the text to text.  */
	@Test
	public void testFileterdTokenizedSentencesToText() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("text-sentence-tokenizer-with-filter.zz"),out,err);
	
		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertEquals(18,sa.length);
		assertTrue(sa[0].startsWith("key"));
		assertTrue(sa[1].startsWith("key"));
		assertTrue(sa[2].startsWith("key"));
		assertTrue(sa[3].startsWith("value {"));
		assertTrue(sa[4].startsWith("}"));
		assertTrue(sa[5].startsWith("value {"));
		assertTrue(sa[9].startsWith("}"));
		assertTrue(sa[10].startsWith("value {"));
		assertTrue(sa[17].startsWith("}"));
	}

	/** Test tokenized sentences and HITS summarization.  */
	@Test
	public void testFileterdTokenizedSentencesAndHits() { 
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
