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

package org.seasr.meandre.components.transform.xml;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

/**
 *
 * @author Lily Dong
 *
 */

@Component(
		name = "XML Tag Filter",
		creator = "Lily Dong",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "xml, tag, xsl, filter",
		description = "This component generates an xsl template for extracting the structed content " +
		    "under the specific tag. The tag is set up through property.",
		dependency = {"protobuf-java-2.2.0.jar", "velocity-1.6.2-dep.jar"},
		resources = { "XMLTagFilter.vm" }
)
public class XMLTagFilter extends AbstractExecutableComponent {

   //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_XSL,
			description = "The XSL template for filtering dates." +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_XSL = Names.PORT_XSL;

	//------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_TAG,
            description = "The tag to be extracted.",
            defaultValue = "body"
    )
    protected static final String PROP_TAG = Names.PROP_TAG;

    //--------------------------------------------------------------------------------------------

    private static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/transform/xml/XMLTagFilter.vm";
    private static final VelocityTemplateService velocity = VelocityTemplateService.getInstance();

    private String tag;

    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    tag = ccp.getProperty(PROP_TAG);
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		VelocityContext context = velocity.getNewContext();
		context.put("tag", tag);

		String xsl = velocity.generateOutput(context, DEFAULT_TEMPLATE);

		cc.pushDataComponentToOutput(OUT_XSL, BasicDataTypesTools.stringToStrings(xsl));
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
