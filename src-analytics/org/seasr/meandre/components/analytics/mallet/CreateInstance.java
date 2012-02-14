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
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

import cc.mallet.types.Instance;

@Component(
        name = "Create Mallet Instance",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "_TRANSFORM_, mallet, instance",
        description = "This component creates a Mallet instance object.<p>" +
        		"This represents a machine learning 'example' to be used in training, testing or " +
        		"performance of various machine learning algorithms.</p> <p>An instance contains four " +
        		"generic fields of predefined name: 'data', 'target', 'name', and 'source'. " +
        		"'Data' holds the data represented by the instance, 'target' is often a label " +
        		"associated with the instance, 'name' is a short identifying name for the instance a" +
        		"(such as a filename), and 'source' is human-readable source information, (such as the original text).</p>" ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class CreateInstance extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "data",
            description = "The data to be represented by the instance" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_DATA = "data";

    @ComponentInput(
            name = "target",
            description = "The label associated with the instance" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TARGET = "target";

    @ComponentInput(
            name = "name",
            description = "A short identifying name for the instance (such as a filename)" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_NAME = "name";

    @ComponentInput(
            name = "source",
            description = "The human-readable source information (such as the original text)" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_SOURCE = "source";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "mallet_instance",
            description = "A Mallet instance" +
                "<br>TYPE: cc.mallet.types.Instance"
    )
    protected static final String OUT_INSTANCE = "mallet_instance";

    //--------------------------------------------------------------------------------------------




    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object data = cc.getDataComponentFromInput(IN_DATA);
        Object target = cc.getDataComponentFromInput(IN_TARGET);
        Object name = cc.getDataComponentFromInput(IN_NAME);
        Object source = cc.getDataComponentFromInput(IN_SOURCE);

        if (target instanceof Strings)
            target = BasicDataTypesTools.stringsToStringArray((Strings)target)[0];

        if (name instanceof Strings)
            name = BasicDataTypesTools.stringsToStringArray((Strings)name)[0];

        if (source instanceof Strings)
            source = BasicDataTypesTools.stringsToStringArray((Strings)source)[0];

        console.fine("Creating instance of type: " + data.getClass().getName());

        Instance instance = new Instance(data, target, name, source);
        cc.pushDataComponentToOutput(OUT_INSTANCE, instance);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
