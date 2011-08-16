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

package org.seasr.meandre.components.vis.d3;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.vis.html.StreamingVelocityTemplateToHTML;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This components uses the D3 library to generate a line chart that can contain multiple lines.",
        name = "Multi Line Chart",
        tags = "visualization, d3, line chart",
        rights = Licenses.UofINCSA,
        firingPolicy = FiringPolicy.all,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.7-dep.jar", "d3-v1.29.3.jar" },
        resources  = { "MultiLineChart.vm" }
)
public class MultiLineChart extends AbstractStreamingD3Component {

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
                name = Names.PORT_JSON,
                description = "JSON data" +
                "Format: { x : [ 1, 2, 3 ], y : [ 10, 12, 14 ] }" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_JSON = Names.PORT_JSON;

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The label to display" +
                    "<br>TYPE: java.lang.String" +
                    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                    "<br>TYPE: byte[]" +
                    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                    "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_LABEL = Names.PORT_TEXT;

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
            defaultValue = "Line Chart"
    )
    protected static final String PROP_TITLE = Names.PROP_TITLE;

    private static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/d3/MultiLineChart.vm";
    @ComponentProperty(
            description = "The template name",
            name = StreamingVelocityTemplateToHTML.PROP_TEMPLATE,
            defaultValue = DEFAULT_TEMPLATE
    )
    protected static final String PROP_TEMPLATE = StreamingVelocityTemplateToHTML.PROP_TEMPLATE;

    //--------------------------------------------------------------------------------------------

    private JSONArray _jsonData = new JSONArray();
    private JSONArray _jsonLabels = new JSONArray();

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        context.put("title", getPropertyOrDieTrying(PROP_TITLE, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String json = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_JSON))[0];
        String label = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LABEL))[0];

        _jsonData.put(new JSONObject(json));
        _jsonLabels.put(label);
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return true;
    }

    @Override
    public void startStream() throws Exception {
        _jsonData = new JSONArray();
        _jsonLabels = new JSONArray();
    }

    @Override
    public void endStream() throws Exception {
        context.put("data", _jsonData.toString());
        context.put("labels", _jsonLabels.toString());

        super.executeCallBack(componentContext);
    }
}
