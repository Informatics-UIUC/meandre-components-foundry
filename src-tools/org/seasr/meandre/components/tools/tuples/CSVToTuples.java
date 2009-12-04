package org.seasr.meandre.components.tools.tuples;



import java.util.ArrayList;
import java.util.List;

import java.io.StringReader;
import java.io.BufferedReader;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.BasicDataTypes;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.tools.Names;

import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

import com.ibm.icu.util.StringTokenizer;


/**
 *
 * @author Mike Haberman;
 *
 */


@Component(
		name = "CSV To Tuples",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tools, text,",
		description = "This component converts a csv string into tuples.  Each line of the incoming text is a new tuple. It does not handle missing values" ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class CSVToTuples extends AbstractExecutableComponent {
	
	
	//----------------------------- PROPERTIES ---------------------------------------------------
   @ComponentProperty(description = "column names/labels to be used (comma separated)",
		   name = "labels",
		   defaultValue = "")
    protected static final String DATA_PROPERTY_COLUMN_NAMES = "labels";
   
   @ComponentProperty(description = "token used to separate the values (ie column data)",
		   name = "tokenSeparator",
		   defaultValue = ",")
    protected static final String DATA_PROPERTY_TOKEN_SEPARATOR = "tokenSeparator";
   
   
    //------------------------------ INPUTS ------------------------------------------------------
	
	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "the text to be parsed into tuples.  Each line is a new tuple."
	)
	protected static final String IN_TEXT = Names.PORT_TEXT;


    //------------------------------ OUTPUTS -----------------------------------------------------
	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of tuples"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
	
	
	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
   	//--------------------------------------------------------------------------------------------    
    
    String tokenSeparator;
    SimpleTuplePeer outPeer;
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{
		this.tokenSeparator = ccp.getProperty(DATA_PROPERTY_TOKEN_SEPARATOR).trim();
		String colNames = ccp.getProperty(DATA_PROPERTY_COLUMN_NAMES).trim();
		String[] vals = colNames.split(",");
		
		if (colNames.length() == 0 || vals.length == 0) {
			throw new RuntimeException(DATA_PROPERTY_COLUMN_NAMES + " needs to be set");
		}
		
		outPeer = new SimpleTuplePeer(vals);
	}
	
	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		
		int numOfColumns = outPeer.size();
		
		Strings input = (Strings) cc.getDataComponentFromInput(IN_TEXT);
		String[] text = BasicDataTypesTools.stringsToStringArray(input);
		String toParse = text[0];
		
		List<Strings> output = new ArrayList<Strings>();
		BufferedReader reader = new BufferedReader(new StringReader(toParse));
		SimpleTuple tuple = outPeer.createTuple();
		while (true) {
			
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			
			// skip commented or empty lines
			if (line.indexOf("#") == 0 || line.length() == 0) 
				continue;
			
            String[] parts = line.split(tokenSeparator, numOfColumns);
            if (parts.length == numOfColumns) {
            	tuple.setValues(parts);
    			output.add(tuple.convert());
            }
            else {
            	// else skip the line
            	console.fine("skipping line " + line);
            }
            
			
		}
			
		 Strings[] results = new Strings[output.size()];
		 output.toArray(results);

		 StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
		 cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	     // tuple meta data
		 cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception 
    {
    }
    	
}
