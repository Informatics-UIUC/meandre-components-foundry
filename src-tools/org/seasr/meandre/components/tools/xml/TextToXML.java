/**
 * 
 */
package org.seasr.meandre.components.tools.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.filters.StringInputStream;
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
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.Names;
import org.w3c.dom.Document;

/** Converts text into a XML document
 * 
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Text to XML",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "xml, io, text",
		description = "This component reads a XML in text form and buids a manipulatable document object. " +
				      "The text to convert is received in its input. The component outputs the XML object " +
				      "read. A property allows to control the behaviour of the component in " +
				      "front of an IO error, allowing to continue pushing and empty XML or " +
				      "throwing and exception forcing the finalization of the flow execution."
)
public class TextToXML implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name=Names.PROP_ERROR_HANDLING,
			description = "If set to true errors will be handled and empty models will be pushed. " +
					      "Otherwise, the component will throw an exception an force the flow to abort.",
		    defaultValue = "true" 
		)
	private final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;
	
	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "The text containing the XML to read"
		)
	private final static String INPUT_TEXT = Names.PORT_TEXT;
	
	@ComponentOutput(
			name = Names.PORT_XML,
			description = "The XML containing the XML document read"
		)
	private final static String OUTPUT_DOCUMENT = Names.PORT_XML;
	
	//--------------------------------------------------------------------------------------------
	
	/** The error handling flag */
	private boolean bErrorHandling;

	/** The document builder factory */
	private DocumentBuilderFactory factory;

	/** The document builder instance */
	private DocumentBuilder parser;

	//--------------------------------------------------------------------------------------------
	
	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		
		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
		try {
			this.factory = DocumentBuilderFactory.newInstance();
			this.parser = factory.newDocumentBuilder();
		}
		catch (Throwable t) {
			String sMessage = "Could not initialize the XML parser";
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
		this.factory = null;
		this.parser = null;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		Object obj = cc.getDataComponentFromInput(INPUT_TEXT);
		if ( obj instanceof StreamDelimiter )
			cc.pushDataComponentToOutput(OUTPUT_DOCUMENT, obj);
		else {	
			String sText  = (obj instanceof Strings)?((Strings)obj).getValue(0):obj.toString();
			Document doc = null;
			try {
				doc = parser.parse(new StringInputStream(sText)); 
			} catch (Throwable t) {
				String sMessage = "Could not read XML from text "+((sText.length()>100)?sText.substring(0, 100):sText);
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( !bErrorHandling ) 
					throw new ComponentExecutionException(t);
				else {
					doc = parser.newDocument();
				}
			}
			cc.pushDataComponentToOutput(OUTPUT_DOCUMENT, doc);	
		}
	}


	//-----------------------------------------------------------------------------------

	

}
