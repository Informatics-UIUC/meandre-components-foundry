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

package org.seasr.meandre.components.analytics.mallet;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

@Component(
        name = "Aggregate Mallet Instances",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "mallet, instance, aggregator",
        description = "This component accumulates the instances belonging to the " +
        		"specified stream and produces a list of machine learning instances, " +
        		"typically used for training or testing of a machine learning algorithm." ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class AggregateInstances extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "mallet_instance",
            description = "The Mallet instance" +
                "<br>TYPE: cc.mallet.types.Instance"
    )
    protected static final String IN_INSTANCE = "mallet_instance";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "mallet_instance_list",
            description = "The list of accumulated machine learning instances, " +
                "typically used for training or testing of a machine learning algorithm" +
                "<br>TYPE: cc.mallet.types.InstanceList"
    )
    protected static final String OUT_INSTANCE_LIST = "mallet_instance_list";

    //--------------------------------------------------------------------------------------------


    protected InstanceList _instanceList;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (_instanceList == null)
            throw new ComponentExecutionException("No stream was detected. This component only works in streaming mode.");

        Instance instance = (Instance) cc.getDataComponentFromInput(IN_INSTANCE);
        _instanceList.add(instance);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return true;
    }

    @Override
    public void startStream() throws Exception {
        _instanceList = new InstanceList(null, null);
    }

    @Override
    public void endStream() throws Exception {
        componentContext.pushDataComponentToOutput(OUT_INSTANCE_LIST, new StreamInitiator(streamId));
        componentContext.pushDataComponentToOutput(OUT_INSTANCE_LIST, _instanceList);
        componentContext.pushDataComponentToOutput(OUT_INSTANCE_LIST, new StreamTerminator(streamId));

        _instanceList = null;
    }
}
