/**
 * 
 */
package org.seasr.meandre.components.tools.semantic.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

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
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.Names;

import com.hp.hpl.jena.rdf.model.Model;

/** Reads a Jena Model from disk
 * 
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Write semantic model",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "semantic, io, read, model",
		description = "This component writes a RDF model. The model name is specified " +
				      "in the input. Also, it is able to read from URLs and local files " +
				      "using URL of file syntax. The component outputs the semantic model " +
				      "read. A property allows to control the behaviour of the component in " +
				      "front of an IO error, allowing to continue pushing and empty model or " +
				      "throwing and exception forcing the finalization of the flow execution."
)
public class WriteModel implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name=Names.PROP_ERROR_HANDLING,
			description = "If set to true errors will be handled and empty models will be pushed. " +
					      "Otherwise, the component will throw an exception an force the flow to abort.",
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
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model to write"
		)
	private final static String INPUT_LOCATION = Names.PORT_LOCATION;
	
	@ComponentInput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document written"
		)
	private final static String INPUT_DOCUMENT = Names.PORT_DOCUMENT;
	
	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model to write"
		)
	private final static String OUTPUT_LOCATION = Names.PORT_LOCATION;
	
	@ComponentOutput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document written"
		)
	private final static String OUTPUT_DOCUMENT = Names.PORT_DOCUMENT;
	
	//--------------------------------------------------------------------------------------------
	
	/** The error handling flag */
	private boolean bErrorHandling;

	/** The RDF language dialect */
	private String sRDFDialect;

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

		Object objLoc = cc.getDataComponentFromInput(INPUT_LOCATION);
		Object objDoc = cc.getDataComponentFromInput(INPUT_DOCUMENT);	
		
		if ( objLoc instanceof StreamDelimiter || objDoc instanceof StreamDelimiter ) {
			pushDelimiters(cc, objLoc, objDoc);	
		}
		else {
			String sLocation = (objLoc instanceof Strings)?((Strings)objLoc).getValue(0):objLoc.toString();
			try {
				Model model = (Model)objDoc;
				attemptWriteModel(model, sLocation);
			} catch (IOException e) {
				String sMessage = "Could not write the semantic model to location "+sLocation;
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( !bErrorHandling ) 
					throw new ComponentExecutionException(e);
			}
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, BasicDataTypesTools.stringToStrings(sLocation));
			cc.pushDataComponentToOutput(OUTPUT_DOCUMENT, objDoc);	
		}
			
	}

	/** Pushes the obtained delimiters
	 * 
	 * @param cc The component context
	 * @param objLoc The location delimiter
	 * @param objDoc The document delimiter
	 * @throws ComponentContextException Push failed
	 */
	private void pushDelimiters(ComponentContext cc, Object objLoc,
			Object objDoc) throws ComponentContextException {
		if ( objLoc instanceof StreamDelimiter &&  objDoc instanceof StreamDelimiter)  {
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, objLoc);
			cc.pushDataComponentToOutput(OUTPUT_DOCUMENT, objDoc);
		}
		else
			pushMissalignedDelimiters(cc,objLoc, objDoc);
	}

	/** Push the delimiters to the outputs as needed.
	 * 
	 * @param cc The component context
	 * @param objLoc The location delimiter
	 * @param objDoc The document delimiter
	 * @throws ComponentContextException Push failed
	 */
	private void pushMissalignedDelimiters(ComponentContext cc, Object objLoc, Object objDoc) 
	throws ComponentContextException {
		String sMsg = "Missaligned delimiters receive, reusing delimiters to banlance the streams";
		cc.getOutputConsole().println("[WARNING] "+sMsg);
		cc.getLogger().warning(sMsg);
		if ( objLoc instanceof StreamDelimiter ) {
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, objLoc);
			cc.pushDataComponentToOutput(OUTPUT_DOCUMENT, objLoc);
		}
		else {
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, objDoc);
			cc.pushDataComponentToOutput(OUTPUT_DOCUMENT, objDoc);
		}
	}

	//-----------------------------------------------------------------------------------
	
	/** Tries to read the model from any of the supported dialects.
	 * 
	 * @param model The model to read
	 * @param sLocation The location to read
	 * @throws IOException The model could not be read
	 */
	protected void attemptWriteModel ( Model model, String sLocation ) 
	throws IOException {
		//
		// Read the content of the location
		//
		Writer wrt = openWriter(sLocation);
		
		//
		// Read the location and check its consistency
		//
		try {
			model.write(wrt,sRDFDialect);
		}
		catch ( Exception eRDF ) {
			IOException ioe = new IOException();
			ioe.setStackTrace(eRDF.getStackTrace());
			throw ioe;
		}
	}
	
	/** Opens a writer to the location where to write.
	 * 
	 * @param sLocation The location to write to
	 * @return The writer for this location
	 * @throws IOException The location could not be read 
	 */
	private Writer openWriter(String sLocation) throws IOException {
		try {
			// Try too pull it as a URL
			URL url = new URL(sLocation);
			if ( url.getProtocol().equalsIgnoreCase("file") )
				return new FileWriter(new File(url.toString().substring(7)));
			else
				// Not a file, assuming unsupported format
				throw new MalformedURLException();
		} catch (MalformedURLException e) {
			// Badly formated UR or remoteL. Trying as a local file
			try {
				return new FileWriter(sLocation);
			} catch (FileNotFoundException e1) {
				throw e1;
			}
		} catch (IOException e) {
			throw e;
		}
	}

	

}
