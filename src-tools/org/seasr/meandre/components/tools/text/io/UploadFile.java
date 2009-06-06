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

package org.seasr.meandre.components.tools.text.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.io.JARInstaller;
import org.seasr.meandre.support.io.JARInstaller.InstallStatus;

/** NOTES
 *
 * This component takes NO inputs,
 * User selects one or more files to upload
 * Each file is uploaded to this component
 * on Execute, this component then pushes each of those uploaded files
 * to the next component
 *
 * It is assumed that the server running this component
 * has installed the fluid flash stuff in published_resources
 * see FluidUploader.vm for more details on the html
 *
 * The code here is based on FluidUploader.java in components.io.file
 *
 * @author Lily Dong
 * @author Mike Haberman
 * @author Boris Capitanu
 */

@Component(
        creator = "Lily Dong",
        description = "Uploads and submits text files from user's own machine",
        name = "Upload File",
        tags = "file input upload text",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/",
        dependency={ "velocity-1.6.1-dep.jar", "protobuf-java-2.0.3.jar", "fluid-components-0.8.jar" },
        resources={ "FluidUploadFile.vm" }
)
public class UploadFile extends GenericTemplate {

    //------------------------------ PROPERTIES --------------------------------------------------

	//
	// reset the defaults inherited from TemplateGUI,  to this component
	//
	@ComponentProperty(
	        description = "title",
	        name = Names.PROP_TITLE,
	        defaultValue = "Uploader"
	)
	protected static final String PROP_TITLE = Names.PROP_TITLE;

	@ComponentProperty(
	        description = "The template name",
	        name = GenericTemplate.PROP_TEMPLATE,
	        defaultValue = "org/seasr/meandre/components/tools/text/io/FluidUploadFile.vm"
	)
    protected static final String PROP_TEMPLATE = GenericTemplate.PROP_TEMPLATE;

	//--------------------------------------------------------------------------------------------


	private ArrayList<String> output;


    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    templateVariables = new String[] { PROP_TITLE };
        //
        // velocity could always access these via $ccp.getProperty("title")
        // but now they will be visible as $title
        //

	    super.initializeCallBack(ccp);

	    output = new ArrayList<String>();

	    context.put("FPath", "/public/resources/fluid/fluid-components");
	    String sFluidDir = ccp.getPublicResourcesDirectory() + File.separator + "fluid";

	    File fluidJar = new File(ccp.getPublicResourcesDirectory() + File.separator +
	            "contexts" + File.separator + "java" + File.separator + "fluid-components-0.8.jar");
	    if (!fluidJar.exists())
	        throw new ComponentContextException("Could not find dependency: " + fluidJar.toString());

	    console.fine("Installing Fluid components from: " + fluidJar.toString());

        InstallStatus status = JARInstaller.installFromStream(new FileInputStream(fluidJar), sFluidDir, false);
        if (status == InstallStatus.SKIPPED)
            console.fine("Installation skipped - Fluid components already installed");

        if (status == InstallStatus.FAILED)
            throw new ComponentContextException("Failed to install Fluid components at " + new File(sFluidDir).getAbsolutePath());
	}

    //--------------------------------------------------------------------------------------------

	@Override
    protected boolean processRequest(HttpServletRequest request) throws IOException
    {
    	// if this request is the last request, return
    	if (request.getParameter(formInputName) != null) return true;

    	BufferedReader br = null;
    	br = request.getReader();

		if (br == null) return false;

		console.fine("processing input");

		String line = br.readLine();
		String boundary = line.trim();

		//continue reading until the beginning of file
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0) continue;
			if (line.startsWith("Content-Type:")) break;
		}

		StringBuffer buf = new StringBuffer();
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0) continue;
			if(line.startsWith(boundary)) //end of file
			    break;
			buf.append(line).append("\n");
		}
		br.close();

		// now save this input in an array
		String outputString = buf.toString();
		output.add(outputString);

		console.fine("got input: " + outputString);

		// all is good, no errors
		return true;
    }

	@Override
    protected void subPushOutput() throws Exception
    {
	    for (String s : output)
    		componentContext.pushDataComponentToOutput(GenericTemplate.OUT_OBJECT,
    		        BasicDataTypesTools.stringToStrings(s));
    }
}