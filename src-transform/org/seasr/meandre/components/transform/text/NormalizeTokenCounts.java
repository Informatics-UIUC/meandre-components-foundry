package org.seasr.meandre.components.transform.text;

import java.util.HashMap;
import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
*
* @author Boris Capitanu
*
*/

@Component(
       name = "Normalize Token Counts",
       creator = "Boris Capitanu",
       baseURL = "meandre://seasr.org/components/foundry/",
       firingPolicy = FiringPolicy.all,
       mode = Mode.compute,
       rights = Licenses.UofINCSA,
       tags = "#TRANSFORM, token count, boolean, normalize",
       description = "This component sets the count of tokens with a count greater than zero to 1.",
       dependency = {"protobuf-java-2.2.0.jar"}
)
public class NormalizeTokenCounts extends AbstractExecutableComponent {
    
    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The token counts to be normalized" +
                "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
    )
    protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ OUTPUTS -----------------------------------------------------
    
    @ComponentOutput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The normalized token counts" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
    )
    protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Map<String, Integer> tokenCounts = DataTypeParser.parseAsStringIntegerMap(cc.getDataComponentFromInput(IN_TOKEN_COUNTS));
        Map<String, Integer> normalizedTokenCounts = new HashMap<String, Integer>();
        
        for (Map.Entry<String, Integer> entry : tokenCounts.entrySet())
            if (entry.getValue() > 0)
                normalizedTokenCounts.put(entry.getKey(), 1);
        
        cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(normalizedTokenCounts, false));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
