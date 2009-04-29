/**
 * 
 */
package org.seasr.meandre.components.tools.text.io;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
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

/** Reads a Jena Model from disk
 * 
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Read text",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "semantic, io, read, text",
		description = "This component reads a text from the location. The text location is specified " +
				      "in the input. Also, it is able to read from URLs and local files " +
				      "using URL of file syntax. The component outputs the semantic model " +
				      "read. A property allows to control the behaviour of the component in " +
				      "front of an IO error, allowing to continue pushing and empty text or " +
				      "throwing and exception forcing the finalization of the flow execution."
)
public class ReadText implements ExecutableComponent {

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
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model to read"
		)
	private final static String INPUT_LOCATION = Names.PORT_LOCATION;
	
	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model read"
		)
	private final static String OUTPUT_LOCATION = Names.PORT_LOCATION;
	
	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The text read"
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

		Object obj = cc.getDataComponentFromInput(INPUT_LOCATION);
		if ( obj instanceof StreamDelimiter ) {
			pushDelimiters(cc, (StreamDelimiter)obj);	
		}
		else {
			String sLocation = (obj instanceof Strings)?((Strings)obj).getValue(0):obj.toString();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String sRes = "";
			try {
				PrintStream ps = new PrintStream(baos);
				InputStreamReader isr = openLocation(sLocation);
				LineNumberReader lnr = new LineNumberReader(isr); 
				String sTmp;
				while ( (sTmp=lnr.readLine())!=null ) ps.println(sTmp);
				isr.close();
				sRes = baos.toString();
			} catch (Throwable t) {
				String sMessage = "Could not read XML from location "+sLocation.substring(0, 100);
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( !bErrorHandling ) 
					throw new ComponentExecutionException(t);
			}
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, BasicDataTypesTools.stringToStrings(sLocation));
			cc.pushDataComponentToOutput(OUTPUT_TEXT, BasicDataTypesTools.stringToStrings(sRes));		
		}
	}

	/** Push the delimiters 
	 * 
	 * @param cc The component context
	 * @param sdLoc The delimiter object
	 * @throws ComponentContextException
	 */
	private void pushDelimiters(ComponentContext cc, StreamDelimiter sdLoc)
			throws ComponentContextException {
		cc.pushDataComponentToOutput(OUTPUT_LOCATION, sdLoc);
		try {
			StreamDelimiter sd = (StreamDelimiter) sdLoc.getClass().newInstance();
			for ( String sKey:sd.keySet() )
				sd.put(sKey, sdLoc.get(sKey));
			cc.pushDataComponentToOutput(OUTPUT_TEXT, sd);
		} catch (Exception e) {
			String sMsg = "[WARNING] Failed to create a new delimiter reusing current one";
			cc.getOutputConsole().println(sMsg);
			cc.getLogger().warning(sMsg);
			cc.pushDataComponentToOutput(OUTPUT_TEXT, sdLoc);
		}
	}


	//-----------------------------------------------------------------------------------

	
	/** Opens the location from where to read.
	 * 
	 * @param sLocation The location to read from
	 * @return The reader for this location
	 * @throws IOException The location could not be read 
	 */
	public static InputStreamReader openLocation(String sLocation) throws IOException {
		try {
			// Try too pull it as a URL
			URL url = new URL(sLocation);
			return new InputStreamReader(url.openStream());
		} catch (MalformedURLException e) {
			// Badly formated URL. Trying as a local file
			try {
				return new FileReader(sLocation);
			} catch (FileNotFoundException e1) {
				throw e1;
			}
		} catch (IOException e) {
			throw e;
		}
	}

	

}
