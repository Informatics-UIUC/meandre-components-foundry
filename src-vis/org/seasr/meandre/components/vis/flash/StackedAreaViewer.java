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

package org.seasr.meandre.components.vis.flash;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
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
        tags = "#VIS, flash, visualization, bar chart, stacked bar chart",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        firingPolicy = FiringPolicy.all,
        baseURL = "meandre://seasr.org/components/foundry/",
        resources = { "StackedAreaViewer.vm", "StackedAreaViewer.swf"}
)
public class StackedAreaViewer extends GenericTemplate {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = "dataUrl",
			description = "data url" +
    		      "<br>TYPE: java.net.URI" +
                  "<br>TYPE: java.net.URL" +
                  "<br>TYPE: java.lang.String" +
                  "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String INPUT_URL = "dataUrl";

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

	@ComponentProperty(
	        description = "field name for the column that is the label category",
	        name = "nodeNameField",
	        defaultValue = "concept"
	)
	protected static final String PROP_NODE_NAME_FIELD = "nodeNameField";

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


    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

	    ComponentUtils.writePublicResource(getClass(),
	            "org/seasr/meandre/components/vis/flash/StackedAreaViewer.swf",
	            "flash" + File.separator + "StackedAreaViewer.swf", ccp, false);

	    context.put("swf",           "flash/StackedAreaViewer.swf");
	    context.put(PROP_TITLE,      ccp.getProperty(PROP_TITLE));
	    context.put(PROP_FIELD_X,    ccp.getProperty(PROP_FIELD_X));
	    context.put(PROP_FIELD_Y,    ccp.getProperty(PROP_FIELD_Y));
	    context.put(PROP_NODE_NAMES, ccp.getProperty(PROP_NODE_NAMES));
	    context.put(PROP_NODE_NAME_FIELD, ccp.getProperty(PROP_NODE_NAME_FIELD));
	}

    @Override
	public void executeCallBack(ComponentContext cc) throws Exception {
        URI uri = DataTypeParser.parseAsURI(cc.getDataComponentFromInput(INPUT_URL));
		String fileOrURL = uri.toString();

		// if this is a fullURL, we are done, we assume the data is available
		// via http
		//
		if (! fileOrURL.startsWith("http")) {

			fileOrURL = "/public/resources/" + fileOrURL;

			/* if you need to prepend the host :
			URL host = cc.getWebUIUrl(true);
			console.info("Host " + host);
			fileOrURL = host.toString() + fileOrURL;
			*/

		}

		console.info("Resource is at " + fileOrURL);
		context.put(INPUT_URL, fileOrURL);

		super.executeCallBack(cc);
	}

    //--------------------------------------------------------------------------------------------

	@Override
	protected boolean processRequest(HttpServletRequest request) throws IOException {
	    return true;
	}
}