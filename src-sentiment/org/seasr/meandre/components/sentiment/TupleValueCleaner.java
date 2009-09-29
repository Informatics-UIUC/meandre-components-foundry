package org.seasr.meandre.components.sentiment;



import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


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
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;

import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.components.tuples.DynamicTuple;
import org.seasr.meandre.support.components.tuples.DynamicTuplePeer;




/**
 *
 * @author Mike Haberman;
 *
 */


@Component(
		name = "Tuple Value Cleaner",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple, clean",
		description = "This component cleans a value of a tuple " ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleValueCleaner  extends AbstractExecutableComponent {
	

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
			description = "set of cleaned tuples"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
	
	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (same as input, orgValue)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
	
	
	
	//----------------------------- PROPERTIES ---------------------------------------------------
   @ComponentProperty(description = "field to be cleanded",
		   name = "fieldname",
		   defaultValue = "")
    protected static final String DATA_PROPERTY_FIELD = "fieldname";
   
   @ComponentProperty(description = "regEx",
		   name = "regex",
		   defaultValue = "")
    protected static final String DATA_PROPERTY_REG_EX = "regex";
      
   
   
	//--------------------------------------------------------------------------------------------

    
    
    String fieldname = null;
    String toFindPattern = null;
    String toReplacePattern = null;
   	
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{
		this.fieldname = ccp.getProperty(DATA_PROPERTY_FIELD).trim();
		console.info("cleaning field " + fieldname);
		
		String toParse = ccp.getProperty(DATA_PROPERTY_REG_EX).trim();
		if (toParse.length() > 0) {
			StringTokenizer tokens = new StringTokenizer(toParse, "/");
			// s/<pattern>/<pattern>/
			String prefix         = tokens.nextToken();
			this.toFindPattern    = tokens.nextToken();
			this.toReplacePattern = tokens.nextToken();
			console.info("replace :" + toFindPattern + ": with :" + toReplacePattern);
		}
		
		
	}
	

    protected String clean(String value)
    {
    	if (toReplacePattern != null) {
    		return value.replaceAll(toFindPattern, toReplacePattern);
    	}
    	return value;
    	
    	/*
    	Matcher m = pattern.matcher(value);
    	
    	if (m.matches()) {
    		return m.replaceAll(replacement);
    	}
    	
    	return value;
    	*/
    }

	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		String newField = fieldname + "Org";
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		String[] meta = DataTypeParser.parseAsString(inputMeta);
		String fields = meta[0];
		DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
		DynamicTuplePeer outPeer = new DynamicTuplePeer(inPeer, new String[]{newField});		
		
		Strings input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
		String[] tuples = DataTypeParser.parseAsString(input);
		DynamicTuple tuple = inPeer.createTuple();
		
		
		int FIELD_IDX = inPeer.getIndexForFieldName(fieldname);
		if (FIELD_IDX == -1) {
			throw new RuntimeException(inPeer.size() + " tuple has no field named " + fieldname + " " + inPeer.toString());
		}
		
		int NEW_IDX  = outPeer.getIndexForFieldName(newField);
		
		DynamicTuple outTuple = outPeer.createTuple();
		
		List<String> output = new ArrayList<String>();
		for (int i = 0; i < tuples.length; i++) {
			
			tuple.setValues(tuples[i]);	
			String value = tuple.getValue(FIELD_IDX);
			String cleanValue = clean(value);
			
			outTuple.setValue(tuple);
			outTuple.setValue(NEW_IDX, value);        // new field is the original value
			outTuple.setValue(FIELD_IDX, cleanValue); // org field is the cleaned value
			
		    output.add(outTuple.toString());
		}
		
		//
		// push the whole collection, protocol safe
		//
	    String[] results = new String[output.size()];
	    output.toArray(results);
	    Strings outputSafe = BasicDataTypesTools.stringToStrings(results);
	    cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	    
	    //
		// metaData for this tuple producer
		//
	    Strings metaData;
		metaData = BasicDataTypesTools.stringToStrings(outPeer.getFieldNames());
	    cc.pushDataComponentToOutput(OUT_META_TUPLE, metaData);
		
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception 
    {
    }
    
   
	
}

