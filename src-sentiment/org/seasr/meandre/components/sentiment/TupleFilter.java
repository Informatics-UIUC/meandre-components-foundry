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
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.parsers.DataTypeParser;


import org.seasr.meandre.support.tuples.DynamicTuple;
import org.seasr.meandre.support.tuples.DynamicTuplePeer;



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
		dependency = {"trove-2.0.3.jar","protobuf-java-2.0.3.jar"}
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
		String[] meta = DataTypeParser.parseAsString(inputMeta);
		String fields = meta[0];
		DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
		
		
		int FILTER_FIELD_IDX = inPeer.getIndexForFieldName(FILTER_FIELD);
		console.info("filter FIELD " + FILTER_FIELD);
		console.info("filter field index " + FILTER_FIELD_IDX);
		
		
		Strings input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
		String[] tuples = DataTypeParser.parseAsString(input);
		DynamicTuple tuple = inPeer.createTuple();
		
		List<String> output = new ArrayList<String>();
		for (int i = 0; i < tuples.length; i++) {
			
			tuple.setValues(tuples[i]);	
			String fieldValue = tuple.getValue(FILTER_FIELD_IDX);
			
			if (pattern == null || pattern.matcher(fieldValue).matches())
			{
				output.add(tuple.toString());
			}
		}
				
		
		String[] results = new String[output.size()];
		output.toArray(results);
		Strings outputSafe = BasicDataTypesTools.stringToStrings(results);
		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
		 
		cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
		 
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        
    }
}