package org.seasr.meandre.components.sentiment;


import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;


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

import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;



/**
 * This component perform POS tagging on the text passed using OpenNLP.
 *
 * @author Mike Haberman;
 *
 */


@Component(
		name = "tuple value filter",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple, tools, text, filter",
		description = "This component filters the incoming set of tuples based on a regular expression" ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleFilter  extends AbstractExecutableComponent {
	

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
			description = "set of filtered tuples"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
	
	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (same as input)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_FILTER_REGEX,
			description = "regular expression to filter tuples",
		    defaultValue = "*"
		)
	protected static final String PROP_FILTER_REGEX = Names.PROP_FILTER_REGEX;
	
	@ComponentProperty(
			name = "tupleFilterField",
			description = "to which field of the tuple to apply the filter",
		    defaultValue = ""
		)
	protected static final String PROP_FILTER_FIELD = "tupleFilterField";
	//--------------------------------------------------------------------------------------------

	Pattern pattern = null;
	String FILTER_FIELD;
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{	
		FILTER_FIELD = ccp.getProperty(PROP_FILTER_FIELD).trim();
		if (FILTER_FIELD.length() == 0) {
			throw new ComponentContextException("Property not set " + PROP_FILTER_FIELD);
		}
		
		String regex = ccp.getProperty(PROP_FILTER_REGEX).trim();
		if (regex.length() > 0) {
			pattern = Pattern.compile(regex);
		}
	}

	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = tuplePeer.createTuple();
		
		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		int FILTER_FIELD_IDX = tuplePeer.getIndexForFieldName(FILTER_FIELD);
		console.info("filter FIELD " + FILTER_FIELD);
		console.info("filter field index " + FILTER_FIELD_IDX);
		
		List<Strings> output = new ArrayList<Strings>();
		
		for (int i = 0; i < in.length; i++) {
			tuple.setValues(in[i]);	

            String fieldValue = tuple.getValue(FILTER_FIELD_IDX);
			
			if (pattern == null || pattern.matcher(fieldValue).matches())
			{
				output.add(tuple.convert());
			}
			
		}
		
		// push out the data, protocol safe
		Strings[] results = new Strings[output.size()];
		output.toArray(results);
		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
		 
		// push out the meta data
		cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
		 
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        
    }
}