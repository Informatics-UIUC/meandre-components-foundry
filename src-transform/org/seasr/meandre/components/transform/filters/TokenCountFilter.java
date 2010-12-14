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

package org.seasr.meandre.components.transform.filters;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Integers;
import org.seasr.datatypes.core.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

import bsh.Interpreter;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Token Count Filter",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "filter, token, token count",
        description = "This component filters (removes) the tokens with token counts satisfying the given constraints",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TokenCountFilter extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The token counts to filter." +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap" +
                          "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>"
    )
    protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The filtered token counts." +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
    )
    protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "condition",
            description = "The condition that needs to be true for tokens to be removed. " +
            		      "Use 'n' in the condition to represent the token count. " +
            		      "For example, if 'condition' is set to 'n >= 3 && n < 10' " +
            		      "it means it will filter out all the tokens with counts greater " +
            		      "than or equal to 3 but less than 10",
            defaultValue = "n == 1"
    )
    protected static final String PROP_CONDITION = "condition";

    //--------------------------------------------------------------------------------------------

    protected Interpreter interpreter = new Interpreter();
    protected String condition;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        condition = getPropertyOrDieTrying(PROP_CONDITION, ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        IntegersMap in_tokenCounts = (IntegersMap)cc.getDataComponentFromInput(IN_TOKEN_COUNTS);
        IntegersMap.Builder out_tokenCounts = IntegersMap.newBuilder();

        for (int i = 0, iMax = in_tokenCounts.getValueCount(); i < iMax; i++) {
            Integers counts = in_tokenCounts.getValue(i);
            int n = counts.getValue(0);
            interpreter.set("n", n);

            if (!(Boolean)interpreter.eval(condition)) {
                out_tokenCounts.addKey(in_tokenCounts.getKey(i));
                out_tokenCounts.addValue(counts);
            }
        }

        cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, out_tokenCounts.build());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
