/**
 * 
 */
package org.jstor.meandre.components.xml.extractors;

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
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** A base class for JSTOR extractors
 * 
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Page text extractor",
		creator = "Xavier Llora",
		baseURL = "meandre://jstor.org/components/xml/extractor/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "jstor, xml, extractor, text",
		dependency = {"protobuf-java-2.0.3.jar"},
		description = "Extract the text of the pages in an XML JSTOR document."
)
public class PageTextExtractor implements ExecutableComponent {

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
			name = Names.PORT_XML,
			description = "The JSTOR XML document"
		)
	private final static String INPUT_XML = Names.PORT_XML;
	
	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The extracted text based on the pages tag"
		)
	private final static String OUTPUT_TEXT = Names.PORT_TEXT;
	
	//--------------------------------------------------------------------------------------------
	
	/** The error handling flag */
	private boolean bErrorHandling;

	//--------------------------------------------------------------------------------------------
	
	
	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
	}
	
	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.bErrorHandling = false;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		String sText = "";
		try {
			Document doc = (Document)cc.getDataComponentFromInput(INPUT_XML);
			NodeList nl = doc.getChildNodes().item(0).getChildNodes();
			StringBuffer sb = new StringBuffer();
			boolean bContinue = true;
			for ( int i=0 ; bContinue && i<nl.getLength() ; i++ ) {
				Node node = nl.item(i);
				if ( node.getNodeName().equalsIgnoreCase("pages")) {
					NodeList nlPages = node.getChildNodes();
					for ( int j=0 ; j<nlPages.getLength() ; j++ ) 
						if (nlPages.item(j).getNodeName().equalsIgnoreCase("page")) 
							sb.append(nlPages.item(j).getFirstChild().getNodeValue()+" ");
					bContinue = false;
				}
			}
			sText = sb.toString();
		} 
		catch (Throwable t) {
			String sMessage = "Could not extract text from JSTOR XML document";
			cc.getLogger().warning(sMessage);
			cc.getOutputConsole().println("WARNING: "+sMessage);
			if ( !bErrorHandling ) 
				throw new ComponentExecutionException(t);
		}
		
		cc.pushDataComponentToOutput(OUTPUT_TEXT, BasicDataTypesTools.stringToStrings(sText));	
	
	}


	//-----------------------------------------------------------------------------------
	


}
