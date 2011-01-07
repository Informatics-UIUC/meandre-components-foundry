/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */


package org.seasr.meandre.components.vis.protovis;

import java.util.Arrays;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.meandre.components.vis.html.VelocityTemplateToHTML;

/**
 *
 * @author Lily Dong
 * @author Boris Capitanu
 *
 */

@Component(
        creator = "Lily Dong",
        description = "This components creates a Protovis arc diagram based on JSON data that specifies the nodes and links.",
        name = "Arc Diagram",
        tags = "visualization, protovis",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.6.2-dep.jar", "protovis-r3.2.jar" },
        resources  = { "ArcDiagram.vm", "ArcDiagramUI.vm" }
)
public class ArcDiagram extends AbstractProtovisComponent {

	//------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
	            name = Names.PORT_JSON,
	            description = "JSON input data. Must be an two arrays of fields nodes and links" +
	            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
	protected static final String IN_JSON = Names.PORT_JSON;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	           name = Names.PORT_HTML,
	           description = "Text containing the transformed input to html via a velocity template" +
	           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_HTML = Names.PORT_HTML;

	//------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "The title for the page",
	        name = Names.PROP_TITLE,
	        defaultValue = "Arc Diagram"
	)
	protected static final String PROP_TITLE = Names.PROP_TITLE;

	private static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/protovis/ArcDiagram.vm";
	@ComponentProperty(
	        description = "The template name",
	        name = VelocityTemplateToHTML.PROP_TEMPLATE,
	        defaultValue = DEFAULT_TEMPLATE
	)
    protected static final String PROP_TEMPLATE = VelocityTemplateToHTML.PROP_TEMPLATE;

	//--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

	    context.put("title", getPropertyOrDieTrying(PROP_TITLE, true, true, ccp));
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		//
	    // fetch the input, push it to the context
	    //
	    Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_JSON);
	    String[] data = BasicDataTypesTools.stringsToStringArray(inputMeta);
	    String json = data[0];

	    context.put("data", json);

	    console.finest("data: " + json);

		super.executeCallBack(cc);
	}

	//--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_JSON })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_HTML, componentContext.getDataComponentFromInput(IN_JSON));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_JSON })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_HTML, componentContext.getDataComponentFromInput(IN_JSON));
    }
}
