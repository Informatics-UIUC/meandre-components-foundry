package org.seasr.meandre.components.sentiment;



import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;


import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;




/**
 *
 * @author Mike Haberman;
 *
 */


@Component(
		name = "Tuple Value Cleaner",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple, clean",
		description = "This component cleans a value of a tuple based on a regular expression " ,
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
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer inPeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = inPeer.createTuple();
		
		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		String newField = fieldname + "Org";
		SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, new String[]{newField});		
				
		
		int FIELD_IDX = inPeer.getIndexForFieldName(fieldname);
		if (FIELD_IDX == -1) {
			throw new RuntimeException(inPeer.size() + " tuple has no field named " + fieldname + " " + inPeer.toString());
		}
		
		int NEW_IDX  = outPeer.getIndexForFieldName(newField);
		
		SimpleTuple outTuple = outPeer.createTuple();
		
		List<Strings> output = new ArrayList<Strings>();
		for (int i = 0; i < in.length; i++) {
			
			tuple.setValues(in[i]);	
			String value = tuple.getValue(FIELD_IDX);
			String cleanValue = clean(value);
			
			outTuple.setValue(tuple);
			outTuple.setValue(NEW_IDX, value);        // new field is the original value
			outTuple.setValue(FIELD_IDX, cleanValue); // org field is the cleaned value
			
		    output.add(outTuple.convert());
		}
		
		//
		// push the whole collection, protocol safe
		//
		Strings[] results = new Strings[output.size()];
		output.toArray(results);
		    
		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	    
	    //
		// metaData for this tuple producer
		//
	    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
		
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception 
    {
    }
    
   
	
}

