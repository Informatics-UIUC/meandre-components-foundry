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

import java.io.ByteArrayOutputStream;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;

import com.hp.hpl.jena.rdf.model.Model;

/** Converts a model to text
 *
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Model to RDF text",
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
public class ModelToRDFText implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_ERROR_HANDLING,
			description = "If set to true errors will be handled and they will be reported to the screen ." +
					      "Otherwise, the component will throw an exception an force the flow to abort. ",
		    defaultValue = "true"
		)
	private final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;

	@ComponentProperty(
			name = Names.PROP_RDF_DIALECT,
			description = "The RDF language dialect to use. Predefined values for lang are " +
					     "\"RDF/XML\", \"N-TRIPLE\", \"TURTLE\" (or \"TTL\") and \"N3\". null " +
					     "represents the default language, \"RDF/XML\". \"RDF/XML-ABBREV\" is a synonym for \"RDF/XML\".",
		    defaultValue = "TTL"
		)
	private final static String PROP_RDF_DIALECT = Names.PROP_RDF_DIALECT;

	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document to print"
		)
	private final static String INPUT_DOCUMENT = Names.PORT_DOCUMENT;

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The semantic document converted into text"
		)
	private final static String OUTPUT_TEXT = Names.PORT_TEXT;

	//--------------------------------------------------------------------------------------------

	/** The RDF language dialect */
	private String sRDFDialect;

	/** The error handling flag */
	private boolean bErrorHandling;

	//--------------------------------------------------------------------------------------------


	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.sRDFDialect = ccp.getProperty(PROP_RDF_DIALECT);
		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.sRDFDialect = null;
		this.bErrorHandling = false;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		Object obj = cc.getDataComponentFromInput(INPUT_DOCUMENT);

		if ( obj instanceof StreamDelimiter )
			cc.pushDataComponentToOutput(OUTPUT_TEXT, obj);
		else {
			Model model = null;
			String sOutput = null;
			try {
				model = (Model) obj;
				sOutput = modelToDialect(model);
			}
			catch ( ClassCastException e ) {
				String sMessage = "Input data is not a semantic model";
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( !bErrorHandling )
					throw new ComponentExecutionException(e);
				else
					sOutput = obj.toString();
			}
			cc.pushDataComponentToOutput(OUTPUT_TEXT, BasicDataTypesTools.stringToStrings(sOutput));
		}
	}


	//-----------------------------------------------------------------------------------

	/** Converts the model to a string.
	 *
	 * @param model The model to read
	 * @return The dialect version of the model
	 */
	protected String modelToDialect ( Model model ) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos,sRDFDialect);
		return baos.toString();
	}



}
