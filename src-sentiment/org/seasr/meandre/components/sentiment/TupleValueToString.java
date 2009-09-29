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
		name = "Tuple Value To String",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tools, text,",
		description = "This component converts a tuple field/value to a string" ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleValueToString extends AbstractExecutableComponent {
	

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
			name = Names.PORT_TEXT,
			description = "text of the tuple value"
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;
	
		
	
	
	//----------------------------- PROPERTIES ---------------------------------------------------
   @ComponentProperty(description = "the field whose value will be written to the output",
		   name = "fieldname",
		   defaultValue = "")
    protected static final String DATA_PROPERTY_FIELD = "fieldname";
   	//--------------------------------------------------------------------------------------------

    
    
    String fieldname;
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{
		this.fieldname = ccp.getProperty(DATA_PROPERTY_FIELD).trim(); 
	}
	
	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		String[] meta = DataTypeParser.parseAsString(inputMeta);
		String fields = meta[0];
		DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
		
		Strings input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
		String[] tuples = DataTypeParser.parseAsString(input);
		DynamicTuple tuple = inPeer.createTuple();
		
		
		int FIELD_IDX = inPeer.getIndexForFieldName(fieldname);
		if (FIELD_IDX == -1) {
			throw new RuntimeException("tuple has no field named " + fieldname);
		}
		console.info("Tuple peer " + inPeer.toString());
		for (int i = 0; i < tuples.length; i++) {
			
			tuple.setValues(tuples[i]);	
			String value = tuple.getValue(FIELD_IDX);
			
			//cc.pushDataComponentToOutput(OUT_TEXT, value);
			
		    Strings outputSafe = BasicDataTypesTools.stringToStrings(value);
		    cc.pushDataComponentToOutput(OUT_TEXT, outputSafe);
		}
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception 
    {
    }
    	
}
