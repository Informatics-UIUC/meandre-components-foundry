package org.seasr.meandre.components.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seasr.meandre.components.ComponentTesterBase;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** The base class for performing component testing.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ToolsComponentTests {
	
	/** The component test base supporting class for these tests. */
	private static ComponentTesterBase ctb = null;
	
	/** Generates the descriptors for the specified folders */
	@BeforeClass
	public static void initializeTestResources () {
		ctb = new ComponentTesterBase();
		ctb.setBaseTestPort(50000);
		ctb.setFlowsFolder("./test/flows/tools");
		ctb.setTempDescriptorFolder("./tmp");
		ctb.setTempDescriptorFolder("./tmp/desc/tools");
		ctb.setSourceFolders(new String [] { "./src-tools"} );
		ctb.initialize();
	}

    
    /** Generates the descriptors for the specified folders */
	@AfterClass
	public static void destroyTestResources () {
		// Clean the temp folder
		ctb.printCoverageReport();
		//ctb.destroy();
	}

	/** The test of the basic functionality. */
	@Test
	public void basicFunctionalityTest() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("component-tester-base.zz"),out,err);
		
		assertEquals(1,out.toString().split(ComponentTesterBase.NEW_LINE).length);
		assertEquals(3,err.toString().split(ComponentTesterBase.NEW_LINE).length);
		
		assertEquals(0,out.toString().indexOf("Hello World!"));
			
	}


	/** The test of the basic functionality. */
	@Test
	public void fileDirectoryListTest() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("file-lister.zz"),out,err);
		
		String [] saList = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertEquals(4,saList.length);
			
	}
	

	/** The test an empty RDF model reading and to text components. */
	@Test
	public void emptyRDFTest() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("empty-model-to-text.zz"),out,err);
	
		Model modelFlow = ModelFactory.createDefaultModel();
		modelFlow.read(new StringReader(out.toString()),null,"RDF/XML");
	
		assertEquals(0,modelFlow.size());			
	}


	/** The test of the basic RDF model reading and to text components. */
	@Test
	public void testRDFReadingTest() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("read-model-to-text-multiple.zz"),out,err);

		Model modelFlow = ModelFactory.createDefaultModel();
		modelFlow.read(new StringReader(out.toString()),null,"N-TRIPLE");

		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(new FileReader(new File("./test/data/samples/rdf/sample-rdf.nt")),null,"N-TRIPLE");
		} catch (FileNotFoundException e) {
			fail(e.toString());
		}
		
		assertEquals(model.size(),modelFlow.size());			
	}


	/** The test of the basic RDF model reading and writing to text components. */
	@Test
	public void testRDFReadingWrittingTest() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("read-write-model-to-text-multiple.zz"),out,err);
	
		try {
			Model modelFlow = ModelFactory.createDefaultModel();
			modelFlow.read(new FileReader(new File("./tmp/sample.nt")),null,"N-TRIPLE");
		
			Model model = ModelFactory.createDefaultModel();
			model.read(new FileReader(new File("./test/data/samples/rdf/sample-rdf.nt")),null,"N-TRIPLE");
			assertEquals(model.size(),modelFlow.size());	
		} catch (FileNotFoundException e) {
			fail(e.toString());
		}
		
				
	}


	/** The test a RDF model wrapping text. */
	@Test
	public void wrappedRDFTextTest() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("text-wrapped-model-to-text.zz"),out,err);
	
		Model modelFlow = ModelFactory.createDefaultModel();
		modelFlow.read(new StringReader(out.toString()),null,"RDF/XML");
	
		assertEquals(1,modelFlow.size());	
		assertTrue(modelFlow.listStatements().nextStatement().asTriple().getObject().getLiteral().getValue().toString().startsWith("Hello World!"));
	}

	/** The test of the basic txt reading and writing to text components. */
	@Test
	public void testTextReadingWrittingTest() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("read-write-text-multiple.zz"),out,err);
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			InputStreamReader isr = new FileReader("./tmp/sample.txt");
			LineNumberReader lnr = new LineNumberReader(isr); 
			String sTmp;
			while ( (sTmp=lnr.readLine())!=null ) ps.println(sTmp);
			isr.close();
			String sRes = baos.toString();
			assertTrue(sRes.startsWith("Hello World!"));
		}
		catch ( Throwable t ){
			fail(t.getMessage());
		}
	}
	
	/** The test of the basic XML reading and writing to text components. */
	@Test
	public void testXMLReadingWrittingTest() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("read-write-xml-to-text-multiple.zz"),out,err);
	
		try {
			StringBuffer sb = new StringBuffer();
			LineNumberReader lir = new LineNumberReader(new FileReader("./tmp/sample.xml"));
			String sTmp = null;
			while ( (sTmp=lir.readLine())!=null ) sb.append(sTmp);
			lir.close();
			String sXML = sb.toString();
			assertTrue(sXML.contains("Empire Burlesque"));
			assertTrue(sXML.contains("Hide your heart"));
			assertTrue(sXML.contains("Greatest Hits"));
			assertTrue(sXML.contains("Still got the blues"));
			assertTrue(sXML.contains("Eros"));
			assertTrue(sXML.contains("One night only"));
			assertTrue(sXML.contains("Sylvias Mother"));
			assertTrue(sXML.contains("Maggie May"));
			assertTrue(sXML.contains("When a man loves a woman"));
			assertTrue(sXML.contains("Unchain my heart"));
			assertTrue(sXML.contains("CATALOG>"));
			assertTrue(sXML.contains("CD>"));
			assertTrue(sXML.contains("ARTIST>"));
		} catch (Throwable t) {
			fail(t.toString());
		}		
	}
	
	/** The test of the basic model reducer. */
	@Test
	public void testWrappedModelReducer() { 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		
		ctb.runZigZag(ctb.getZigZag("accumulate-wrapped-models.zz"),out,err);

		String [] sa = out.toString().split(ComponentTesterBase.NEW_LINE);
		assertEquals(8,sa.length);
		assertTrue(sa[0].indexOf("count=5")>0);
		assertTrue(sa[0].indexOf("accumulated=1")>0);

		assertTrue(sa[1].indexOf("Hello World!")>0);
		assertTrue(sa[2].indexOf("Hello World!")>0);
		assertTrue(sa[3].indexOf("Hello World!")>0);
		assertTrue(sa[4].indexOf("Hello World!")>0);
		assertTrue(sa[5].indexOf("Hello World!")>0);
		
		assertEquals("",sa[6]);
	
		assertTrue(sa[7].indexOf("count=5")>0);
		assertTrue(sa[7].indexOf("accumulated=1")>0);
	}
}
