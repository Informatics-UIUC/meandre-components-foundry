/**
 * 
 */
package org.seasr.meandre.components.tools.xml;

import java.io.StringWriter;

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
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.w3c.dom.Document;

/** Converts XML into text document
 * 
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "XML to text",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "xml, io, text",
		description = "This component write a XML in text form and generates it text form. " +
				      "The XML document to convert is received in its input. The component outputs the text " +
				      "generated. A property allows to control the behaviour of the component in " +
				      "front of an IO error, allowing to continue pushing and empty model or " +
				      "throwing and exception forcing the finalization of the flow execution."
)
public class XMLToText implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name=Names.PROP_ERROR_HANDLING,
			description = "If set to true errors will be handled and empty models will be pushed. " +
					      "Otherwise, the component will throw an exception an force the flow to abort.",
		    defaultValue = "true" 
		)
	private final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;
	
	@ComponentProperty(
			name=Names.PROP_ENCODING,
			description = "The encoding to use on the outputed text.",
		    defaultValue = "UTF-8" 
		)
	private final static String PROP_ENCODING = Names.PROP_ENCODING;
	
	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_XML,
			description = "The XML containing the XML to be read"
		)
	private final static String INPUT_XML = Names.PORT_XML;
	
	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The text containing the XML read"
		)
	private final static String OUTPUT_TEXT = Names.PORT_TEXT;
	
	//--------------------------------------------------------------------------------------------
	
	/** The error handling flag */
	private boolean bErrorHandling;

	/** The transformer for the document */
	private Transformer transformer;
	
	/** The string encoding to use */
	private String sEncoding;

	//--------------------------------------------------------------------------------------------
	
	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		
		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
		this.sEncoding = ccp.getProperty(PROP_ENCODING);
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty( OutputKeys.ENCODING, sEncoding); 
		}
		catch (Throwable t) {
			String sMessage = "Could not initialize the XML transformer";
			ccp.getLogger().warning(sMessage);
			ccp.getOutputConsole().println("WARNING: "+sMessage);
			throw new ComponentExecutionException(sMessage+" "+t.toString());
		}
		
	}
	
	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.bErrorHandling = false;
		this.transformer = null;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		Object obj = cc.getDataComponentFromInput(INPUT_XML);
		if ( obj instanceof StreamDelimiter )
			cc.pushDataComponentToOutput(OUTPUT_TEXT, obj);
		else {	
			String sRes = null;
			try {
				Document doc = ( Document )obj;
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(doc);
				transformer.transform(source, result);
				sRes = result.getWriter().toString();
			}
			catch (Throwable t) {
				String sMessage = "Could not transform XML document into text";
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( bErrorHandling )
					sRes = "";
				else
					throw new ComponentExecutionException(sMessage+" "+t.toString());
			}
			cc.pushDataComponentToOutput(OUTPUT_TEXT, BasicDataTypesTools.stringToStrings(sRes));
		}
	}


	//-----------------------------------------------------------------------------------

	

}
