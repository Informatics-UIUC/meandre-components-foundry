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

package org.seasr.meandre.components.tools.semantic;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Boris Capitanu
 */

@Component(
		name = "Model Accumulator",
		creator = "Boris Capitanu",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "#TRANSFORM, semantic, io, model, accumulator",
		description = "This component is intended to work on wrapped model streams. " +
				      "Given a sequence of wrapped models, it will create a new model that " +
				      "accumulates/reduces all the information and then push them the resulting model. " +
				      "If no wrapped model is provided it will act as a simple pass through. ",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class ModelAccumulator extends AbstractStreamingExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document to accumulate" +
                "<br>TYPE: com.hp.hpl.jena.rdf.model.Model" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL"
	)
	protected static final String IN_DOCUMENT = Names.PORT_DOCUMENT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_DOCUMENT,
			description = "The semantic document accumulated" +
			    "<br>TYPE: com.hp.hpl.jena.rdf.model.Model"
	)
	protected static final String OUT_DOCUMENT = Names.PORT_DOCUMENT;

    //------------------------------ PROPERTIES --------------------------------------------------

	// Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent

	//--------------------------------------------------------------------------------------------


	protected Model _accumulator;
	protected boolean _isStreaming;

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);
	    initializeAccumulator();
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
	    _accumulator.add(DataTypeParser.parseAsModel(cc.getDataComponentFromInput(IN_DOCUMENT)));

	    if (!_isStreaming)
	        endStream();
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _accumulator = null;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public void startStream() throws Exception {
        initializeAccumulator();
        _isStreaming = true;
    }

    @Override
    public void endStream() throws Exception {
        componentContext.pushDataComponentToOutput(OUT_DOCUMENT, _accumulator);

        _isStreaming = false;
        initializeAccumulator();
    }

	//-----------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return true;
    }

    //-----------------------------------------------------------------------------------

    protected void initializeAccumulator() {
        _accumulator = ModelFactory.createDefaultModel();
    }
}
