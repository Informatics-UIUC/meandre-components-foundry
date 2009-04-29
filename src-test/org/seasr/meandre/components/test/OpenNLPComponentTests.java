package org.seasr.meandre.components.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.components.test.framework.ComponentTesterBase;

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
		ctb.setFlowsFolder("./test/flows/opennlp");
		ctb.setTempDescriptorFolder("./tmp");
		ctb.setTempDescriptorFolder("./tmp/desc/opennlp");
		ctb.setSourceFolders(new String [] { "./src-opennlp", "./src-tools"} );
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
	

	/** Test the sentence detection on dummy text components. */
	@Test
	public void testSentenceTokenizer() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("text-sentence-tokenizer.zz"),out,err);
	
		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertTrue(sa.length==38);
		assertTrue(sa[0].startsWith("key"));
		assertTrue(sa[1].startsWith("key"));
		assertTrue(sa[2].startsWith("key"));
		assertTrue(sa[3].startsWith("value {"));
		assertTrue(sa[20].startsWith("}"));
		assertTrue(sa[21].startsWith("value {"));
		assertTrue(sa[27].startsWith("}"));
		assertTrue(sa[28].startsWith("value {"));
		assertTrue(sa[37].startsWith("}"));
	}
}
