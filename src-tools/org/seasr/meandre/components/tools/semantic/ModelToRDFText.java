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
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.io.ModelUtils;
import org.seasr.meandre.support.parsers.DataTypeParser;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Converts a model to text
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */
@Component(
		name = "Model To RDF Text",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "semantic, io, transform, model, text",
		description = "This component takes the input semantic model and converts it into " +
				      "a text form. Properties allow to specify the dialect to use"
)
public class ModelToRDFText extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document to convert"
		)
	protected static final String IN_DOCUMENT = Names.PORT_DOCUMENT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The semantic document converted into text"
		)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_RDF_DIALECT,
            description = "The RDF language dialect to use. Predefined values for lang are " +
                         "\"RDF/XML\", \"N-TRIPLE\", \"TURTLE\" (or \"TTL\") and \"N3\". null " +
                         "represents the default language, \"RDF/XML\". \"RDF/XML-ABBREV\" is a synonym for \"RDF/XML\".",
            defaultValue = "TTL"
        )
    protected static final String PROP_RDF_DIALECT = Names.PROP_RDF_DIALECT;

	//--------------------------------------------------------------------------------------------


	/** The RDF language dialect */
	private String sRDFDialect;


	//--------------------------------------------------------------------------------------------

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.sRDFDialect = ccp.getProperty(PROP_RDF_DIALECT);
	}

	public void executeCallBack(ComponentContext cc) throws Exception {
		Model model = DataTypeParser.parseAsModel(cc.getDataComponentFromInput(IN_DOCUMENT));
		cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(
		        ModelUtils.modelToDialect(model, sRDFDialect)));
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        this.sRDFDialect = null;
    }
}
