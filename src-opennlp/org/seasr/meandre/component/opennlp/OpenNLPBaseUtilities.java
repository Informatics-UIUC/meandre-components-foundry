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

import org.meandre.annotations.ComponentProperty;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.meandre.components.tools.Names;

/**
 * Provides basic utilities for the OpenNLP components.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */
public abstract class OpenNLPBaseUtilities extends AbstractExecutableComponent {

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_LANGUAGE,
			description = "The language to use in the tokenizer. ",
		    defaultValue = "english"
		)
	protected static final String PROP_LANGUAGE = Names.PROP_LANGUAGE;

	//--------------------------------------------------------------------------------------------


	/** The error handling flag */
	protected boolean bIgnoreErrors;

	/** The language of the text being processed */
	protected String sLanguage;


	//--------------------------------------------------------------------------------------------

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.sLanguage = ccp.getProperty(PROP_LANGUAGE).trim().toLowerCase();
		this.bIgnoreErrors = Boolean.parseBoolean(ccp.getProperty(PROP_IGNORE_ERRORS));

		String sRunFile = ccp.getRunDirectory() + File.separator + "opennlp" + File.separator + "models";
		boolean bRes = ModelInstaller.installJar(sRunFile, "opennlp-" + sLanguage + "-models.jar", false);
		if ( !this.bIgnoreErrors && !bRes )
			throw new ComponentExecutionException("Failed to install OpenNLP models at "
			        + new File(sRunFile).getAbsolutePath());
	}

	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		this.sLanguage = null;
		this.bIgnoreErrors = false;
	}
}
