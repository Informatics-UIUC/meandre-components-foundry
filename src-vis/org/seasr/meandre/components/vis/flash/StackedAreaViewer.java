package org.seasr.meandre.components.vis.flash;

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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.components.tools.text.io.GenericTemplate;


/**
 * @author Mike Haberman
 * 
 * Latest version:  now the dataURL is an input not a property
 * Push Text -> StackedAreaViewer
 * 
 */

@Component(
        creator = "Mike Haberman",
        description = "flash vis for stacked areas",
        name = "Stacked Area Viewer",
        tags = "string, visualization",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        firingPolicy = FiringPolicy.all,
        baseURL = "meandre://seasr.org/components/foundry/",
        resources = { "StackedAreaViewer.vm", "StackedAreaViewer.swf"}
)

public class StackedAreaViewer extends GenericTemplate {

    // the swf file will be unjar'ed and written to public_resources/SWF_DIR/SWF_FILE
	protected static String SWF_DIR  = "flash";
	protected static String SWF_FILE = "StackedAreaViewer.swf";

	
	@ComponentInput(
			name = "dataUrl",
			description = "data url"
	)
	protected static final String INPUT_URL = "dataUrl";
	
	
	//------------------------------ OUTPUTS -----------------------------------------------------
	
    //------------------------------ PROPERTIES --------------------------------------------------

	//
	// specific to this component
	//
	@ComponentProperty(
	        description = "The title for the page",
	        name = Names.PROP_TITLE,
	        defaultValue = "flash visualization"
	)
	protected static final String PROP_TITLE = Names.PROP_TITLE;

	@ComponentProperty(
	        description = "names for the values",
	        name = "nodeNames",
	        defaultValue = "anger,fear,joy,love,sadness,surprise"
	)
	protected static final String PROP_NODE_NAMES = "nodeNames";

	/*
	@ComponentProperty(
	        description = "data url",
	        name = "dataUrl",
	        defaultValue = ""
	)
	protected static final String PROP_DATA_URL= "dataUrl";
	*/
	

	@ComponentProperty(
	        description = "x axis column",
	        name = "fieldX",
	        defaultValue = "windowId"
	)
	protected static final String PROP_FIELD_X= "fieldX";

	@ComponentProperty(
	        description = "y axis column",
	        name = "fieldY",
	        defaultValue = "frequency"
	)
	protected static final String PROP_FIELD_Y= "fieldY";


	@ComponentProperty(
	        description = "The template name",
	        name = GenericTemplate.PROP_TEMPLATE,
	        defaultValue = "org/seasr/meandre/components/vis/flash/StackedAreaViewer.vm"
	)
    protected static final String PROP_TEMPLATE = GenericTemplate.PROP_TEMPLATE;

    //--------------------------------------------------------------------------------------------




	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception
	{
	    super.initializeCallBack(ccp);

	    //
	    // get the swf from jar to public resources
	    //
	    String destination = ccp.getPublicResourcesDirectory();
	    String path = unjarFlashSWF(destination);

	    
	    /*
	    String url = ccp.getProperty(PROP_DATA_URL).trim();
	    if (url.length() == 0) {
	    	throw new RuntimeException(PROP_DATA_URL + " needs to be specified");
	    }
	    context.put(PROP_DATA_URL,url);
	    */
	    

	    context.put("swf",           path);
	    context.put(PROP_TITLE,      ccp.getProperty(PROP_TITLE));
	    context.put(PROP_FIELD_X,    ccp.getProperty(PROP_FIELD_X));
	    context.put(PROP_FIELD_Y,    ccp.getProperty(PROP_FIELD_Y));
	    context.put(PROP_NODE_NAMES, ccp.getProperty(PROP_NODE_NAMES));
	}
	
	
    @Override
	public void executeCallBack(ComponentContext cc) throws Exception 
	{
        Strings inputURL = (Strings) cc.getDataComponentFromInput(INPUT_URL);
		String[] in = BasicDataTypesTools.stringsToStringArray (inputURL);
		context.put(INPUT_URL, in[0]);
		  
		super.executeCallBack(cc);  
	}

	@Override
	protected boolean processRequest(HttpServletRequest request) throws IOException
	{
	    return true;
	}

	protected String unjarFlashSWF(String dir)
	{
	    dir = dir + File.separator + SWF_DIR;

	    File swfDir = new File(dir);
	    if (! swfDir.exists()) {

	    	if (! swfDir.mkdir() ) {
	    		String msg = "Unable to create " + dir;
	    		console.info(msg);
	    		throw new RuntimeException(msg);
	    	}

	    }

	    // unjar the swf and write to dir + SWF_FILE
	    String dest = dir + File.separator + SWF_FILE;
	    console.info("Writing " + dest);

	    InputStream  in = null;
	    OutputStream out = null;

	    try{
            in = this.getClass().getResourceAsStream(SWF_FILE);
		    out = new FileOutputStream(dest);

            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1){
                out.write(buf, 0, len);
            }

		 }catch (IOException e){
			 throw new RuntimeException(e);
		 }
		 finally {

			 try {
				 in.close();
		         out.close();
			 }
			 catch (Exception ignore){}
		 }


		 return SWF_DIR + File.separator + SWF_FILE;
	}

}
