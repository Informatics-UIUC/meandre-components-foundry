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
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.generic.io.ModelUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Converts a text to a model
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */

@Component(
		name = "RDF Text To Model",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, io, read, model",
		description = "This component reads an RDF model in text form and buids the model. " +
				      "The text to convert is received in its input. The component outputs the semantic model " +
				      "read. A property allows to control the behaviour of the component in " +
				      "front of an IO error, allowing to continue pushing and empty model or " +
				      "throwing and exception forcing the finalization of the flow execution.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class RDFTextToModel extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "The text containing the model to read" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document read" +
                "<br>TYPE: com.hp.hpl.jena.rdf.model.Model"
	)
	protected static final String OUT_DOCUMENT = Names.PORT_DOCUMENT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name=Names.PROP_BASE_URI,
            description = "The base URI to be used when converting relative URI's to absolute URI's. " +
                          "The base URI may be null if there are no relative URIs to convert. " +
                          "A base URI of \"\" may permit relative URIs to be used in the model.",
            defaultValue = "seasr://seasr.org/document/base"
    )
    protected static final String PROP_BASE_URI = Names.PROP_BASE_URI;

    // Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent

	//--------------------------------------------------------------------------------------------


	/** The base url to use */
	private String sBaseURI;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.sBaseURI = ccp.getProperty(PROP_BASE_URI);
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
	    for (String modelText : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))) {
            Model model;

            try {
                model = ModelUtils.getModel(modelText, sBaseURI);
            } catch (Exception e) {
                if (ignoreErrors)
                    model = ModelFactory.createDefaultModel();
                else
                    throw new ComponentExecutionException(e);
            }

    	    cc.pushDataComponentToOutput(OUT_DOCUMENT, model);
	    }
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        this.sBaseURI = null;
        this.ignoreErrors = false;
    }
}
