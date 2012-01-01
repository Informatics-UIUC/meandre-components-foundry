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

package org.seasr.meandre.components.tools.control;

import java.util.HashMap;
import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This component acts as a switch, outputing the input object on a " +
        		"different output port, depending on the value of the switch. If the switch " +
        		"value does not match any of the rules set in the switch_rules property, the " +
        		"object will be output via the no_match port.",
        name = "Switch",
        tags = "switch",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/"
)
public class Switch extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The object to switch",
            name = Names.PORT_OBJECT
    )
    protected static final String IN_OBJECT = Names.PORT_OBJECT;

    @ComponentInput(
            description = "The switch value",
            name = "switch"
    )
    protected static final String IN_SWITCH = "switch";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "Switch port 1",
            name = Names.PORT_OBJECT
    )
    protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    @ComponentOutput(
            description = "Switch port 2",
            name = Names.PORT_OBJECT_2
    )
    protected static final String OUT_OBJECT_2 = Names.PORT_OBJECT_2;

    @ComponentOutput(
            description = "Switch port 3",
            name = Names.PORT_OBJECT_3
    )
    protected static final String OUT_OBJECT_3 = Names.PORT_OBJECT_3;

    @ComponentOutput(
            description = "Switch port 4",
            name = Names.PORT_OBJECT_4
    )
    protected static final String OUT_OBJECT_4 = Names.PORT_OBJECT_4;

    @ComponentOutput(
            description = "Default when no match",
            name = "no_match"
    )
    protected static final String OUT_NO_MATCH = "no_match";

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            defaultValue = "",
            description = "The comma-separated switching rule(s) formatted as <switch_value> = <output_port_name>. " +
            		"For example, the rules: 1=" + OUT_OBJECT_2 + ", 3=" + OUT_OBJECT + " mean that when the switch " +
            		"has value 1, the object will be sent via  " + OUT_OBJECT_2 + " port, and when the switch has " +
            		"value 3, the object will be sent via " + OUT_OBJECT + " port.",
            name = "switch_rules"
    )
    protected static final String PROP_SWITCH_RULES = "switch_rules";

    //--------------------------------------------------------------------------------------------


    protected Map<String, String> _rules = new HashMap<String, String>();


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        String rules = getPropertyOrDieTrying(PROP_SWITCH_RULES, ccp);
        for (String rule : rules.split(",")) {
            String[] kv = rule.split("=");
            if (kv.length != 2) throw new ComponentContextException("Invalid rule format: " + rule);
            _rules.put(kv[0].trim(), kv[1].trim());
        }
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String switchValue = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_SWITCH))[0];
        Object input = cc.getDataComponentFromInput(IN_OBJECT);

        String outputPort = _rules.get(switchValue);
        if (outputPort == null) outputPort = OUT_NO_MATCH;

        cc.pushDataComponentToOutput(outputPort, input);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _rules.clear();
        _rules = null;
    }
}
