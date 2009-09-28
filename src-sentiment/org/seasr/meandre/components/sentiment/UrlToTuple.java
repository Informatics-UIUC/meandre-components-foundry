package org.seasr.meandre.components.sentiment;





import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.BasicDataTypes;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;




import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;

import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.components.tuples.DynamicTuple;
import org.seasr.meandre.support.components.tuples.DynamicTuplePeer;


/**
 * This component reads from a URL(http or file) and pushes its content inside of a tuple
 *
 * @author Mike Haberman;
 *
 */


@Component(
		name = "url to tuple",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple, tools, text, filter",
		description = "This component reads a text resources via file or http (no authentication required) " ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.0.3.jar"}
)
public class UrlToTuple extends AbstractExecutableComponent {
	

    //------------------------------ INPUTS ------------------------------------------------------
	
	//------------------------------ OUTPUTS -----------------------------------------------------
	
	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "tuples"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
	
	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuple"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
	
	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "text of the tuple field"
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;
	
	
	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_LOCATION,
			description = "url or file to read (http:// or file://)",
		    defaultValue = ""
		)
	protected static final String PROP_LOCATION = Names.PROP_LOCATION;
	
	@ComponentProperty(
			name = "title",
			description = "given title of the data",
		    defaultValue = ""
		)
	protected static final String PROP_TITLE = "title";

	//--------------------------------------------------------------------------------------------

	DynamicTuple outTuple;
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{	
	    String location = ccp.getProperty(PROP_LOCATION).trim();
	    String title    = ccp.getProperty(PROP_TITLE).trim();

		
		URL url = new URL(location);
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(url.openStream()));

		String inputLine;
		StringBuffer sb = new StringBuffer();
		while ((inputLine = in.readLine()) != null)
			sb.append(inputLine).append("\n");
		in.close();
		
		DynamicTuplePeer outPeer = new DynamicTuplePeer(new String[]{"title", "location", "content"});
		outTuple = outPeer.createTuple();
		outTuple.setValue(0, title);
		outTuple.setValue(1, location);
		outTuple.setValue(2, sb.toString()); // actual content
		
		//console.info(sb.toString());
	}

	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		String[] results = new String[]{outTuple.toString()};
		Strings outputSafe = BasicDataTypesTools.stringToStrings(results);
		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
		 
		Strings metaData;
		metaData = BasicDataTypesTools.stringToStrings(outTuple.getPeer().getFieldNames());
	    cc.pushDataComponentToOutput(OUT_META_TUPLE, metaData);
	    
	    // convenience
	    Strings content = BasicDataTypesTools.stringToStrings(outTuple.getValue("content"));
		cc.pushDataComponentToOutput(OUT_TEXT, content);
		
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        
    }
}
