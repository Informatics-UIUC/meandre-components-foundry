package org.seasr.meandre.components.sentiment;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


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
import org.seasr.datatypes.BasicDataTypes.StringsArray;
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;

import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;



/**
 * This component perform POS tagging on the text passed using OpenNLP.
 *
 * @author Mike Haberman;
 *
 */


@Component(
		name = "tuple to csv file",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple, tools, text, filter",
		description = "This component writes the incoming set of tuples to a file (CSV, default)" ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleToCSVFile  extends AbstractExecutableComponent {
	

    //------------------------------ INPUTS ------------------------------------------------------
	
	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "set of tuples"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;
	
	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
	

    //------------------------------ OUTPUTS -----------------------------------------------------
	
	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of tuples (same as input)"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
	
	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (same as input)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
	
	//----------------------------- PROPERTIES ---------------------------------------------------
	@ComponentProperty(
			name = "tokenSeparator",
			description = "token to use to separate field values",
		    defaultValue = ","
		)
	protected static final String PROP_TOKEN_SEPARATOR = "tokenSeparator";
	
	
	@ComponentProperty(
			name = Names.PORT_FILENAME,
			description  = "filename to write to",
		    defaultValue = ""
		)
	protected static final String PROP_FILENAME = Names.PORT_FILENAME;
	
    //--------------------------------------------------------------------------------------------

	BufferedWriter output;
	String tokenSep;
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{	
		String filename = ccp.getProperty(PROP_FILENAME).trim();
		if (filename.length() == 0) {
			throw new ComponentContextException("Property not set " + PROP_FILENAME);
		}
		
		try {
	         output = new BufferedWriter(new FileWriter(filename));
	    } catch (IOException e) {
	    	throw new ComponentContextException("Unable to write to" + filename);
	    }
	    
	    tokenSep= ccp.getProperty(PROP_TOKEN_SEPARATOR).trim();
		
	}

	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = tuplePeer.createTuple();
		
		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		//
		// write out the fieldnames as the first row
		//
		int size = tuplePeer.size();
		for (int i = 0; i < size; i++) {
			output.write(tuplePeer.getFieldNameForIndex(i));
			if (i + 1 < size) {
				output.write(tokenSep);
			}
		}
		output.write("\n");
		
		
		for (int i = 0; i < in.length; i++) {
			
			tuple.setValues(in[i]);	
			
			for (int j = 0; j < size; j++) {
				output.write(tuple.getValue(j));
				if (j + 1 < size) {
					output.write(tokenSep);
				}
			}
			output.write("\n");
		}
				
		
		cc.pushDataComponentToOutput(OUT_TUPLES, input);
		cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
		 
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception 
    {
        output.close();
    }
}

