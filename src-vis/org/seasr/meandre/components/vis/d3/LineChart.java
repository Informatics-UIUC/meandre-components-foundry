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

import java.util.Map;

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
import org.seasr.meandre.components.vis.html.VelocityTemplateToHTML;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This components uses the D3 library to generate a line chart.",
        name = "Line Chart",
        tags = "#VIS, visualization, d3, line chart",
        rights = Licenses.UofINCSA,
        firingPolicy = FiringPolicy.all,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.7-dep.jar", "d3-v2.8.1.jar" },
        resources  = { "LineChart.vm" }
)
public class LineChart extends AbstractD3Component {

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

    private static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/d3/LineChart.vm";
    @ComponentProperty(
            description = "The template name",
            name = VelocityTemplateToHTML.PROP_TEMPLATE,
            defaultValue = DEFAULT_TEMPLATE
    )
    protected static final String PROP_TEMPLATE = VelocityTemplateToHTML.PROP_TEMPLATE;

    @ComponentProperty(
            description = "X axis label",
            name = "xlabel",
            defaultValue = "X"
    )
    protected static final String PROP_X_AXIS_LABEL = "xlabel";

    @ComponentProperty(
            description = "Y axis label",
            name = "ylabel",
            defaultValue = "Y"
    )
    protected static final String PROP_Y_AXIS_LABEL = "ylabel";

    @ComponentProperty(
            description = "The smoothing value to use by default",
            name = "smoothing",
            defaultValue = "0"
    )
    protected static final String PROP_SMOOTHING = "smoothing";

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        context.put("title", getPropertyOrDieTrying(PROP_TITLE, ccp));
        context.put("xlabel", getPropertyOrDieTrying(PROP_X_AXIS_LABEL, ccp));
        context.put("ylabel", getPropertyOrDieTrying(PROP_Y_AXIS_LABEL, ccp));
        context.put("smoothing", getPropertyOrDieTrying(PROP_SMOOTHING, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String json = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_JSON))[0];
        String label = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LABEL))[0];

        context.put("data", new JSONArray().put(new JSONObject(json)).toString());
        context.put("labels", new JSONArray().put(label).toString());
        context.put("Integer", Integer.class);

        @SuppressWarnings("unchecked")
        Map<String,String> userMap = (Map<String,String>) context.get("_userMap");

        if (!userMap.containsKey("width"))
            userMap.put("width", "800");

        if (!userMap.containsKey("height"))
            userMap.put("height", "600");

        super.executeCallBack(componentContext);
    }
}
