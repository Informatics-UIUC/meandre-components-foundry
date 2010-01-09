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
import org.meandre.components.test.framework.ComponentTesterBase;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** The base class for performing component testing.
 *
 * @author Loretta Auvil;
 *
 */
public class ToolsWriteText {

	/** The component test base supporting class for these tests. */
	private static ComponentTesterBase ctb = null;

	/** Generates the descriptors for the specified folders */
	@BeforeClass
	public static void initializeTestResources () {
		ctb = new ComponentTesterBase();
		ctb.setBaseTestPort(50000);
		ctb.setFlowsFolder("."+File.separator+"test"+File.separator+"flows"+File.separator+"tools");
		ctb.setTempDescriptorFolder("."+File.separator+"tmp");
		ctb.setTempDescriptorFolder("."+File.separator+"tmp"+File.separator+"desc"+File.separator+"tools");
		ctb.setSourceFolders(new String [] { "."+File.separator+"src-tools"} );
		ctb.initialize();
	}


	/** Generates the descriptors for the specified folders */
	@AfterClass
	public static void destroyTestResources () {
		// Clean the temp folder
		ctb.printCoverageReport();
		//ctb.destroy();
	}

	/** The test of the basic writing to file at url file:///tmp/write-text/test.txt. */
	@Test
	public void testWriteText1() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		//delete file
		String fileName = "/tmp"+File.separator+"write-text"+File.separator+"test.txt";
		File f = new File(fileName);

		// Make sure the file or directory does not exist
		if (!f.exists()){
			fileName = "/tmp"+File.separator+"write-text";
			File dir = new File(fileName);

			// Make sure the file or directory exists and isn't write protected
			if (!dir.exists()){

				fileName = "file:///tmp"+File.separator+"write-text"+File.separator+"test.txt";

				//run test
				ctb.runZigZag(ctb.getZigZag("write_text_1.zz"),out,err);

				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(baos);
					InputStreamReader isr = new FileReader(fileName);
					LineNumberReader lnr = new LineNumberReader(isr);
					String sTmp;
					while ( (sTmp=lnr.readLine())!=null ) ps.println(sTmp);
					isr.close();
					String sRes = baos.toString();
					assertTrue(sRes.startsWith("Hello World!"));
					//need to remove file
					deleteFile("/tmp"+File.separator+"write-text"+File.separator+"test.txt");
					deleteFile("/tmp"+File.separator+"write-text");
				}
				catch ( Throwable t ){
					fail(t.getMessage());
				}
			}
		}
	}

	/** The test of the basic writing to file with full path at /tmp/write-text/test.txt. */
	@Test
	public void testWriteText2() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		//delete file
		String fileName = File.separator+"tmp"+File.separator+"write-text"+File.separator+"test.txt";
		File f = new File(fileName);

		// Make sure the file or directory does not exist
		if (!f.exists()){
			fileName = File.separator+"tmp"+File.separator+"write-text";
			File dir = new File(fileName);

			// Make sure the file or directory exists and isn't write protected
			if (!dir.exists()){

				fileName = File.separator+"tmp"+File.separator+"write-text"+File.separator+"test.txt";

				//run test
				ctb.runZigZag(ctb.getZigZag("write_text_2.zz"),out,err);

				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(baos);
					InputStreamReader isr = new FileReader(fileName);
					LineNumberReader lnr = new LineNumberReader(isr);
					String sTmp;
					while ( (sTmp=lnr.readLine())!=null ) ps.println(sTmp);
					isr.close();
					String sRes = baos.toString();
					assertTrue(sRes.startsWith("Hello World!"));
					deleteFile("/tmp"+File.separator+"write-text"+File.separator+"test.txt");
					deleteFile("/tmp"+File.separator+"write-text");
				}
				catch ( Throwable t ){
					fail(t.getMessage());
				}
			}
		}
	}

	/** The test of the basic writing to file at relative path write-text/test.txt. */
	@Test
	public void testWriteText3() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		//delete file
		String fileName = "write-text"+File.separator+"test.txt";
		File f = new File(fileName);

		// Make sure the file or directory does not exist
		if (!f.exists()){
			fileName = "write-text";
			File dir = new File(fileName);

			// Make sure the file or directory exists and isn't write protected
			if (!dir.exists()){

				fileName = "write-text"+File.separator+"test.txt";

				//run test
				ctb.runZigZag(ctb.getZigZag("write_text_3.zz"),out,err);

				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(baos);
					InputStreamReader isr = new FileReader(fileName);
					LineNumberReader lnr = new LineNumberReader(isr);
					String sTmp;
					while ( (sTmp=lnr.readLine())!=null ) ps.println(sTmp);
					isr.close();
					String sRes = baos.toString();
					assertTrue(sRes.startsWith("Hello World!"));
					deleteFile("write-text"+File.separator+"test.txt");
					deleteFile("write-text");

				}
				catch ( Throwable t ){
					fail(t.getMessage());
				}
			}
		}
	}

	public void deleteFile(String fileName){
		// A File object to represent the filename
		File f = new File(fileName);

		// Make sure the file or directory exists and isn't write protected
		if (!f.exists())
			throw new IllegalArgumentException(
					"Delete: no such file or directory: " + fileName);

		if (!f.canWrite())
			throw new IllegalArgumentException("Delete: write protected: "
					+ fileName);

		// If it is a directory, make sure it is empty
		if (f.isDirectory()) {
			String[] files = f.list();
			if (files.length > 0)
				throw new IllegalArgumentException(
						"Delete: directory not empty: " + fileName);
		}

		// Attempt to delete it
		boolean success = f.delete();

		if (!success)
			throw new IllegalArgumentException("Delete: deletion failed");
	}
}
