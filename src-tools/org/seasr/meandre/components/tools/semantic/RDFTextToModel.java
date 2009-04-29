/**
 * 
 */
package org.seasr.meandre.components.tools.semantic;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** Converts a text to a model from disk
 * 
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "RDF Text to model",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, io, read, model",
		dependency = {"protobuf-java-2.0.3.jar"},
		description = "This component reads an RDF model in text form and buids the model. " +
				      "The text to convert is received in its input. The component outputs the semantic model " +
				      "read. A property allows to control the behaviour of the component in " +
				      "front of an IO error, allowing to continue pushing and empty model or " +
				      "throwing and exception forcing the finalization of the flow execution."
)
public class RDFTextToModel implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name=Names.PROP_ERROR_HANDLING,
			description = "If set to true errors will be handled and empty models will be pushed. " +
					      "Otherwise, the component will throw an exception an force the flow to abort.",
		    defaultValue = "true" 
		)
	private final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;
	
	@ComponentProperty(
			name=Names.PROP_BASE_URI,
			description = "The base URI to be used when converting relative URI's to absolute URI's. " +
					      "The base URI may be null if there are no relative URIs to convert. " +
					      "A base URI of \"\" may permit relative URIs to be used in the model.",
		    defaultValue = "seasr://seasr.org/document/base" 
		)
	private final static String PROP_BASE_URI = Names.PROP_BASE_URI;
	
	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "The text containing the model to read"
		)
	private final static String INPUT_TEXT = Names.PORT_TEXT;
	
	@ComponentOutput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document read"
		)
	private final static String OUTPUT_DOCUMENT = Names.PORT_DOCUMENT;
	
	//--------------------------------------------------------------------------------------------
	
	/** The base url to use */
	private String sBaseURI;

	/** The error handling flag */
	private boolean bErrorHandling;

	//--------------------------------------------------------------------------------------------
	
	
	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.sBaseURI = ccp.getProperty(PROP_BASE_URI);
		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
	}
	
	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.sBaseURI = null;
		this.bErrorHandling = false;
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
			String sText = (obj instanceof Strings)?((Strings)obj).getValue(0):obj.toString();
			Model model = ModelFactory.createDefaultModel();
			
			try {
				attemptReadModel(model, sText);
			} catch (Throwable t) {
				String sMessage = "Could not read semantic model from location "+((sText.length()>100)?sText.substring(0, 100):sText);
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( !bErrorHandling ) 
					throw new ComponentExecutionException(t);
				else
					model = ModelFactory.createDefaultModel();
			}
			
			cc.pushDataComponentToOutput(OUTPUT_DOCUMENT, model);	
		}
	}


	//-----------------------------------------------------------------------------------
	
	/** Tries to read the model from any of the supported dialects.
	 * 
	 * @param model The model to read
	 * @param sText The text to parse
	 * @throws IOException The model could not be read
	 */
	protected void attemptReadModel ( Model model, String sText ) 
	throws IOException {
		//
		// Preparing to parse
		//
		byte[] baBytes = sText.getBytes();
		//
		// Read the location and check its consistency
		//
		try {
			model.read(new ByteArrayInputStream(baBytes),sBaseURI,"RDF/XML");
		}
		catch ( Exception eRDF ) {
			try {
				model.read(new ByteArrayInputStream(baBytes),sBaseURI,"TTL");
			}
			catch ( Exception eTTL ) {
				try {
					model.read(new ByteArrayInputStream(baBytes),sBaseURI,"N-TRIPLE");
				}
				catch ( Exception eNT ) {
					IOException ioe = new IOException();
					ioe.setStackTrace(eRDF.getStackTrace());
					throw ioe;
				}
			}
		}
	}

	

}
