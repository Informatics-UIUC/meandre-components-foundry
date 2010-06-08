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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;

@Component(
        creator = "Mike Haberman",
        description = "Generates and displays a webpage via a Velocity Template ",
        name = "Generic Viewer",
        tags = "string, visualization",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/foundry/",
        resources = { "GenericViewer.vm" },
        dependency = { "velocity-1.6.2-dep.jar" }
)
public class GenericViewer extends GenericTemplate {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        name = Names.PORT_HTML,
	        description = "The HTML data" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
	)
    protected static final String IN_HTML = Names.PORT_HTML;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "The template name",
	        name = Names.PROP_TEMPLATE,
	        defaultValue = "org/seasr/meandre/components/tools/text/io/GenericViewer.vm"
	)
	protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;

    //--------------------------------------------------------------------------------------------

    

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	
    	/*
    	Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_HTML);
    	String[] data = BasicDataTypesTools.stringsToStringArray(inputMeta);
    	String html = data[0];
    	*/
    	
    	String[] html = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_HTML));

    	context.put("html", html[0]);

	    super.executeCallBack(cc);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

   

    //
    // not only check errors, but process the input at this step
    // return false on errors, bad input, or you want to regenerate the template/html
    // return true if all processing is completed

    // NOTE: subclasses SHOULD override this method
    @Override
	protected boolean processRequest(HttpServletRequest request) throws IOException {
    	return false;
    }
}