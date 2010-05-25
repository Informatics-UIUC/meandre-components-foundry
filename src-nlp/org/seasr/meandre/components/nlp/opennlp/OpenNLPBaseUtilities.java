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

package org.seasr.meandre.components.nlp.opennlp;

import java.io.File;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.io.JARInstaller;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

/**
 * Provides basic utilities for the OpenNLP components.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 * @author Mike Haberman
 */

 /*
  *  NOTES:
  *  this class will unjar the maxent-models.jar file IFF it exists
  *  in the <public resources directory>/contexts/java/ directory
  *  if openNLPdir is set, it is assumed that it has already been unjared in that location
  *  Note:  as a potential improvement, we could pull the maxent-models.jar file from
  *  the classpath, copy it to public resources, and then unjar
  *
  *  TODO:  if openNLPdir/maxent-models.jar exists BUT it has not been unjared there,
  *  we should unjar it.
  *
  */
public abstract class OpenNLPBaseUtilities extends AbstractExecutableComponent {

    //------------------------------ PROPERTIES --------------------------------------------------

    // Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent

	@ComponentProperty(
			name = Names.PROP_LANGUAGE,
			description = "The language to use in the tokenizer. ",
		    defaultValue = "english"
	)
	protected static final String PROP_LANGUAGE = Names.PROP_LANGUAGE;

	@ComponentProperty(
			name = "openNLPdir",
			description = "OpenNLP directory, if non-empty, skip install",
		    defaultValue = ""
	)
	protected static final String PROP_OPENNLP_DIR = "openNLPdir";

	//--------------------------------------------------------------------------------------------


	protected String sOpenNLPDir;

	/** The language of the text being processed */
	protected String sLanguage;


	//--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        this.sLanguage = ccp.getProperty(PROP_LANGUAGE).trim().toLowerCase();

        sOpenNLPDir = ccp.getProperty(PROP_OPENNLP_DIR).trim();
        if (sOpenNLPDir.length() == 0) {
        	
           sOpenNLPDir = ccp.getRunDirectory()+File.separator+"opennlp";

           installJARModelContainingResource(sOpenNLPDir, "models/English/chunker/EnglishChunk.bin.gz", console, getClass());
           console.fine("Installed " + sLanguage + " models into: " + sOpenNLPDir);
        }
        // else 
        // if the user DID specify an openNLPDir
        // we can assume that it already exists and is installed on the server
        // so there is no need to do any install
        // M.E.H

        // constructs the final OpenNLP models path based on the language chosen
        // example:  <sOpenNLPDir>/models/English/
        sOpenNLPDir += (sOpenNLPDir.endsWith(File.separator) ? "" : File.separator) + "models" + File.separator
                        + sLanguage.substring(0,1).toUpperCase() + sLanguage.substring(1) + File.separator;
        
        
        console.fine("open NLP Dir: " + sOpenNLPDir);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        this.sLanguage = null;
    }

    //--------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public static void installJARModelContainingResource(String dir, String resName, Logger console, Class caller) throws Exception {
    	InputStream modelsInputStream = null;

	    URL modelsResDepURL = caller.getClassLoader().getResource(resName);

	    if (modelsResDepURL != null) {
	        console.fine(String.format("Found resource '%s' as %s", resName, modelsResDepURL));

	        if (modelsResDepURL.getProtocol().equals("jar")) {
	            String sFile = modelsResDepURL.toString().split("!")[0] + "!/";
	            modelsResDepURL = new URL(sFile);
	            JarURLConnection jarConnection = (JarURLConnection)modelsResDepURL.openConnection();
	            modelsInputStream = jarConnection.getJarFileURL().openStream();
	        } else
	            modelsInputStream = modelsResDepURL.openStream();
	    }

		if (modelsInputStream == null)
		    throw new ComponentContextException(String.format("Could not find '%s' in the class path!", resName));

		console.fine("Installing models from: " + modelsResDepURL);

		InstallStatus status = JARInstaller.installFromStream(modelsInputStream, dir, false);
		if (status == InstallStatus.SKIPPED)
		    console.fine("Installation skipped - models already installed");

		if (status == InstallStatus.FAILED)
			throw new ComponentContextException("Failed to install models at " + new File(dir).getAbsolutePath());
    }
}
