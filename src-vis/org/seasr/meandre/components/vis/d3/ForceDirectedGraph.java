package org.seasr.meandre.components.vis.d3;

import java.util.Map;

import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.vis.html.VelocityTemplateToHTML;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This components uses the D3 library to generate a force directed graph.",
        name = "Force Directed Graph",
        tags = "#VIS, visualization, d3, force directed graph",
        rights = Licenses.UofINCSA,
        firingPolicy = FiringPolicy.all,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.7-dep.jar", "d3-v2.10.1.jar" },
        resources  = { "ForceDirectedGraph.vm" }
)
public class ForceDirectedGraph extends AbstractD3Component {

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
                name = Names.PORT_JSON,
                description = "JSON data" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_JSON = Names.PORT_JSON;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
               name = Names.PORT_HTML,
               description = "The force directed graph HTML data" +
               "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_HTML = Names.PORT_HTML;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "The title for the page",
            name = Names.PROP_TITLE,
            defaultValue = "Force-Directed Graph"
    )
    protected static final String PROP_TITLE = Names.PROP_TITLE;

    private static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/d3/ForceDirectedGraph.vm";
    @ComponentProperty(
            description = "The template name",
            name = VelocityTemplateToHTML.PROP_TEMPLATE,
            defaultValue = DEFAULT_TEMPLATE
    )
    protected static final String PROP_TEMPLATE = VelocityTemplateToHTML.PROP_TEMPLATE;

    @ComponentProperty(
            description = "The value to use for force 'charge' parameter",
            name = "charge",
            defaultValue = "-120"
    )
    protected static final String PROP_CHARGE = "charge";

    @ComponentProperty(
            description = "The minimum distance between two linked nodes, in pixels",
            name = "link_distance",
            defaultValue = "80"
    )
    protected static final String PROP_LINK_DISTANCE = "link_distance";

    @ComponentProperty(
            description = "Circle node radius",
            name = "node_radius",
            defaultValue = "5"
    )
    protected static final String PROP_RADIUS = "node_radius";

    @ComponentProperty(
            description = "The text offset from the center of the node, specified as x,y. " +
            		"Example: 7,3 will offset the text 7 pixels to the right and 3 pixels down from center.",
            name = "text_offset",
            defaultValue = "7,3"
    )
    protected static final String PROP_TEXT_OFFSET = "text_offset";

    @ComponentProperty(
            description = "Should the text be drawn next to the node?",
            name = "draw_text",
            defaultValue = "true"
    )
    protected static final String PROP_DRAW_TEXT = "draw_text";

    @ComponentProperty(
            description = "Which attribute from the data should be used to display in the tooltip for each node? " +
            		"Example valid values: 'name', 'type', 'group' (no quotes)",
            name = "tooltip_attribute",
            defaultValue = "type"
    )
    protected static final String PROP_TOOLTIP_ATTR = "tooltip_attribute";

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        context.put("title", getPropertyOrDieTrying(PROP_TITLE, ccp));
        context.put("charge", getPropertyOrDieTrying(PROP_CHARGE, ccp));
        context.put("linkDistance", getPropertyOrDieTrying(PROP_LINK_DISTANCE, ccp));
        context.put("radius", getPropertyOrDieTrying(PROP_RADIUS, ccp));

        String[] textOffset = getPropertyOrDieTrying(PROP_TEXT_OFFSET, ccp).split(",");
        if (textOffset.length != 2) throw new ComponentContextException("Invalid value for property: " + PROP_TEXT_OFFSET);

        context.put("textOffsetX", textOffset[0]);
        context.put("textOffsetY", textOffset[1]);

        context.put("tooltipAttr", getPropertyOrDieTrying(PROP_TOOLTIP_ATTR, ccp));
        context.put("drawText", Boolean.parseBoolean(getPropertyOrDieTrying(PROP_DRAW_TEXT, ccp)));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String json = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_JSON))[0];

        context.put("data", new JSONObject(json).toString());

        @SuppressWarnings("unchecked")
        Map<String,String> userMap = (Map<String,String>) context.get("_userMap");

        if (!userMap.containsKey("width"))
            userMap.put("width", "800");

        if (!userMap.containsKey("height"))
            userMap.put("height", "600");

        super.executeCallBack(componentContext);
    }
}
