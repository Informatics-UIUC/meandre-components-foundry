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

package org.seasr.meandre.components.tools.control;

import java.util.HashSet;
import java.util.Set;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * @author Boris Capitanu
 *
 */

@Component(
        creator = "Boris Capitanu",
        description = "This component filters out some or all stream delimiters",
        name = "Stream Delimiter Filter",
        tags = "#CONTROL, filter, delimiter",
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/"
)
public class StreamDelimiterFilter extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "Input object",
            name = Names.PORT_OBJECT
    )
    protected static final String IN_OBJECT = Names.PORT_OBJECT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "Output object",
            name = Names.PORT_OBJECT
    )
    protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "advanced_filter",
            description = "Use this filter for more fine grained control as to what is filtered - " +
            		"for example, you can filter out (i.e. remove) only stream initiator markers or " +
            		"only stream terminator markers. Valid values are 0 or 1. A value of 0 specifies " +
            		"that only stream initiators with the specified id should be removed (stream " +
            		"terminators are unaffected). A value of 1 filters out only stream terminators " +
            		"with the specified id. Leaving this property empty means both initiator and terminator " +
            		"markers will be filtered out.",
            defaultValue = ""
    )
    protected static final String PROP_ADVANCED_FILTER = "advanced_filter";

    //--------------------------------------------------------------------------------------------


    private final Set<Integer> streamIds = new HashSet<Integer>();
    private Integer _advancedFilter = null;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        for (String id : getPropertyOrDieTrying(PROP_STREAM_ID, true, false, ccp).split(",")) {
            if (id.trim().length() == 0) continue;
            streamIds.add(Integer.parseInt(id.trim()));
        }

        String advanced = getPropertyOrDieTrying(PROP_ADVANCED_FILTER, true, false, ccp);
        if (advanced.length() > 0) _advancedFilter = Integer.parseInt(advanced);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        cc.pushDataComponentToOutput(OUT_OBJECT,
                cc.getDataComponentFromInput(IN_OBJECT));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }

    @Override
    public void handleStreamInitiators() throws Exception {
        StreamInitiator si = (StreamInitiator) componentContext.getDataComponentFromInput(IN_OBJECT);
        if ((streamIds.size() == 0 || streamIds.contains(si.getStreamId())) && (_advancedFilter == null || _advancedFilter == 0))
            console.fine(String.format("Ignoring %s received on ports %s",
                    StreamInitiator.class.getSimpleName(), inputPortsWithInitiators));
        else {
            console.fine(String.format("Forwarding the %s (id: %d) on all output ports...", StreamInitiator.class.getSimpleName(), si.getStreamId()));
            componentContext.pushDataComponentToOutput(OUT_OBJECT, si);
        }
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        StreamTerminator st = (StreamTerminator) componentContext.getDataComponentFromInput(IN_OBJECT);
        if ((streamIds.size() == 0 || streamIds.contains(st.getStreamId())) && (_advancedFilter == null || _advancedFilter == 1))
            console.fine(String.format("Ignoring %s received on ports %s",
                    StreamTerminator.class.getSimpleName(), inputPortsWithTerminators));
        else {
            console.fine(String.format("Forwarding the %s (id: %d) on all output ports...", StreamTerminator.class.getSimpleName(), st.getStreamId()));
            componentContext.pushDataComponentToOutput(OUT_OBJECT, st);
        }
    }
}
