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

package org.seasr.meandre.components.tools.semantic.io;

import java.net.URI;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.components.utils.ComponentUtils;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.generic.io.StreamUtils;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Reads a Jena Model from disk
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */
@Component(
		name = "Write Semantic Model",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, io, read, model",
		description = "This component writes a RDF model. The model name is specified " +
				      "in the input. Also, it is able to read from URLs and local files " +
				      "using URL of file syntax. The component outputs the semantic model " +
				      "read. A property allows to control the behaviour of the component in " +
				      "front of an IO error, allowing to continue pushing and empty model or " +
				      "throwing and exception forcing the finalization of the flow execution.",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class WriteModel extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model to write"
	)
	protected static final String IN_LOCATION = Names.PORT_LOCATION;

	@ComponentInput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document written"
	)
	protected static final String IN_DOCUMENT = Names.PORT_DOCUMENT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model to write"
	)
	protected static final String OUTPUT_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document written"
	)
	protected static final String OUT_DOCUMENT = Names.PORT_DOCUMENT;

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
		URI location = DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_LOCATION));

		model.write(StreamUtils.getOutputStreamForResource(location), sRDFDialect);

		cc.pushDataComponentToOutput(OUTPUT_LOCATION, BasicDataTypesTools.stringToStrings(location.toString()));
		cc.pushDataComponentToOutput(OUT_DOCUMENT, model);
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        this.sRDFDialect = null;
    }

    //-----------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        pushDelimiters(componentContext.getDataComponentFromInput(IN_LOCATION),
                componentContext.getDataComponentFromInput(IN_DOCUMENT));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        pushDelimiters(componentContext.getDataComponentFromInput(IN_LOCATION),
                componentContext.getDataComponentFromInput(IN_DOCUMENT));
    }

    //-----------------------------------------------------------------------------------

	/**
	 * Pushes the obtained delimiters
	 *
	 * @param objLoc The location delimiter
	 * @param objDoc The document delimiter
	 * @throws Exception Push failed
	 */
	private void pushDelimiters(Object objLoc, Object objDoc) throws Exception {
		if ( objLoc instanceof StreamDelimiter &&  objDoc instanceof StreamDelimiter)  {
			componentContext.pushDataComponentToOutput(OUTPUT_LOCATION, objLoc);
			componentContext.pushDataComponentToOutput(OUT_DOCUMENT, objDoc);
		}
		else
			pushMissalignedDelimiters(objLoc, objDoc);
	}

	/**
	 * Push the delimiters to the outputs as needed.
	 *
	 * @param objLoc The location delimiter
	 * @param objDoc The document delimiter
	 * @throws Exception Push failed
	 */
	private void pushMissalignedDelimiters(Object objLoc, Object objDoc) throws ComponentExecutionException {
		console.warning("Missaligned delimiters received - reusing delimiters to balance the streams");

		if ( objLoc instanceof StreamDelimiter ) {
		    try {
                StreamDelimiter clone = ComponentUtils.cloneStreamDelimiter((StreamDelimiter) objLoc);
                componentContext.pushDataComponentToOutput(OUTPUT_LOCATION, objLoc);
                componentContext.pushDataComponentToOutput(OUT_DOCUMENT, clone);
            }
            catch (Exception e) {
                throw new ComponentExecutionException(e);
            }

		}
		else {
		    try {
                StreamDelimiter clone = ComponentUtils.cloneStreamDelimiter((StreamDelimiter) objDoc);
                componentContext.pushDataComponentToOutput(OUTPUT_LOCATION, clone);
                componentContext.pushDataComponentToOutput(OUT_DOCUMENT, objDoc);
            }
            catch (Exception e) {
                throw new ComponentExecutionException(e);
            }
		}
	}
}
