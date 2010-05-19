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

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.components.test.framework.ComponentTesterBase;

/** The base class for performing component testing.
 *
 * @author Loretta Auvil;
 *
 */
public class ExamplesTest {

	/** The component test base supporting class for these tests. */
	private static ComponentTesterBase ctb = null;

	/** Generates the descriptors for the specified folders */
	@BeforeClass
	public static void initializeTestResources () {
		ctb = new ComponentTesterBase();
		ctb.setBaseTestPort(50000);
		ctb.setFlowsFolder("test" + File.separator + "flows" + File.separator + "examples");
		ctb.setTempDescriptorFolder("tmp");
		ctb.setTempDescriptorFolder("tmp" + File.separator + "desc" + File.separator + "examples");
		ctb.setSourceFolders(new String [] { "src-analytics", "src-evernote", "src-jstor", "src-nlp", "src-sentiment", "src-tools", "src-transform", "src-vis", "src-zotero"});
		ctb.initialize();
	}


	/** Generates the descriptors for the specified folders */
	@AfterClass
	public static void destroyTestResources () {
		// Clean the temp folder
		ctb.printCoverageReport();
		//ctb.destroy();
	}

	/** The test of the Date_Entities_To_Simile_Timeline_With_Date_Filtering.zz flow. */
	@Test
	public void testDateEntities() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		//run test
		try {
			ctb.runZigZag(ctb.getZigZag("Date_Entities_To_Simile_Timeline_With_Date_Filtering.zz"),out,err);
		}
		catch ( Throwable t ){
			fail(t.getMessage());
		}
	}
}
