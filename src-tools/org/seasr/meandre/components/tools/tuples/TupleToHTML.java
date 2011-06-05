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

package org.seasr.meandre.components.tools.tuples;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.vis.html.VelocityTemplateToHTML;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Creates HTML for displaying a set of tuples in a table",
        name = "Tuple To HTML",
        tags = "tuple, html",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.6.2-dep.jar", "protobuf-java-2.2.0.jar" },
        resources = { "TupleToHTML.vm" }
)
public class TupleToHTML extends VelocityTemplateToHTML {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "The template name",
            name = Names.PROP_TEMPLATE,
            defaultValue = "org/seasr/meandre/components/tools/tuples/TupleToHTML.vm"
    )
    protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        // Do component-specific initialization here
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);

        SimpleTuplePeer inPeer = new SimpleTuplePeer(inMeta);
        Strings[] inTuples = BasicDataTypesTools.stringsArrayToJavaArray(input);

        Map<String, List<String>> data = new LinkedHashMap<String, List<String>>(inPeer.size());
        for (int i = 0, iMax = inPeer.size(); i < iMax; i++)
            data.put(inPeer.getFieldNameForIndex(i), new ArrayList<String>());

        for (Strings inTuple : inTuples) {
            SimpleTuple tuple = inPeer.createTuple();
            tuple.setValues(inTuple);

            for (int i = 0, iMax = inPeer.size(); i < iMax; i++)
                data.get(inPeer.getFieldNameForIndex(i)).add(tuple.getValue(i));
        }

        context.put("_tuplesCount", inTuples.length);
        context.put("_tuples", data);

        super.executeCallBack(cc);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        super.disposeCallBack(ccp);
    }
}
