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

package org.seasr.meandre.components.tools.xml.io;

import java.io.Writer;
import java.net.URI;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.seasr.meandre.support.generic.io.IOUtils;
import org.w3c.dom.Document;

/**
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */

@Component(
		name = "Write XML",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, io, read, model",
		description = "This component write a XML in a file by generating it text form. " +
				      "The XML document to convert is received in its input as well as the targeted " +
				      "file. The component outputs the original document. A property allows to control " +
				      "the behaviour of the component in front of an IO error, allowing to continue " +
				      "pushing and empty model or throwing and exception forcing the finalization of " +
				      "the flow execution.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class WriteXML extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model to write" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_LOCATION = Names.PORT_LOCATION;

	@ComponentInput(
			name = Names.PORT_XML,
			description = "The XML document" +
                "<br>TYPE: org.w3c.dom.Document" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_XML = Names.PORT_XML;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the written XML" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
			name = Names.PORT_XML,
			description = "The XML document" +
                "<br>TYPE: org.w3c.dom.Document"
	)
	protected static final String OUT_XML= Names.PORT_XML;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name=Names.PROP_ENCODING,
            description = "The encoding to use on the outputed text.",
            defaultValue = "UTF-8"
    )
    protected static final String PROP_ENCODING = Names.PROP_ENCODING;

    //--------------------------------------------------------------------------------------------


	/** The transformer for the document */
	private Transformer transformer;

	/** The string encoding to use */
	private String sEncoding;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.sEncoding = ccp.getProperty(PROP_ENCODING);

		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, sEncoding);
		}
		catch (Throwable t) {
			String sMessage = "Could not initialize the XML transformer";
			console.warning(sMessage);
			throw new ComponentExecutionException(sMessage + " " + t.toString());
		}
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
	    URI sLocation = DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_LOCATION));
	    Document doc = DataTypeParser.parseAsDomDocument(cc.getDataComponentFromInput(IN_XML));

		Writer wrtr = IOUtils.getWriterForResource(sLocation);
		StreamResult result = new StreamResult(wrtr);
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		wrtr.close();

		cc.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(sLocation.toString()));
		cc.pushDataComponentToOutput(OUT_XML, doc);
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        this.transformer = null;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        pushDelimiters(
                componentContext.getDataComponentFromInput(IN_LOCATION),
                componentContext.getDataComponentFromInput(IN_XML));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        pushDelimiters(
                componentContext.getDataComponentFromInput(IN_LOCATION),
                componentContext.getDataComponentFromInput(IN_XML));
    }

    //--------------------------------------------------------------------------------------------

	/**
	 * Pushes the obtained delimiters
	 *
	 * @param objLoc The location delimiter
	 * @param objDoc The document delimiter
	 * @throws Exception Push failed
	 */
	private void pushDelimiters(Object objLoc, Object objDoc) throws Exception {
		if ( objLoc instanceof StreamDelimiter &&  objDoc instanceof StreamDelimiter)  {
		    componentContext.pushDataComponentToOutput(OUT_LOCATION, objLoc);
		    componentContext.pushDataComponentToOutput(OUT_XML, objDoc);
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
    private void pushMissalignedDelimiters(Object objLoc, Object objDoc) throws Exception {
        console.warning("Missaligned delimiters received");

        if ( objLoc instanceof StreamDelimiter ) {
            try {
                StreamDelimiter clone = ComponentUtils.cloneStreamDelimiter((StreamDelimiter) objLoc);
                componentContext.pushDataComponentToOutput(OUT_LOCATION, objLoc);
                componentContext.pushDataComponentToOutput(OUT_XML, clone);
            }
            catch (Exception e) {
                throw new ComponentExecutionException(e);
            }
        }
        else {
            try {
                StreamDelimiter clone = ComponentUtils.cloneStreamDelimiter((StreamDelimiter) objDoc);
                componentContext.pushDataComponentToOutput(OUT_LOCATION, clone);
                componentContext.pushDataComponentToOutput(OUT_XML, objDoc);
            }
            catch (Exception e) {
                throw new ComponentExecutionException(e);
            }
        }
    }
}
