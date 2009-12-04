package org.seasr.meandre.components.tools.tuples;



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


/**
 *
 * @author Mike Haberman;
 *
 */


@Component(
		name = "Tuple Value To String",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tools, text,",
		description = "This component converts a tuple field/value to a single output string" ,
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
			description = "the field value of the tuple"
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;
	
	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuple (the fieldname) "
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
		
	
	
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
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = tuplePeer.createTuple();
		
		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		int FIELD_IDX = tuplePeer.getIndexForFieldName(fieldname);
		if (FIELD_IDX == -1) {
			throw new RuntimeException("tuple has no field named " + fieldname);
		}
		
		SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[] {fieldname});
		
		for (int i = 0; i < in.length; i++) {
			tuple.setValues(in[i]);	
			String value = tuple.getValue(FIELD_IDX);
			
		    Strings outputSafe = BasicDataTypesTools.stringToStrings(value);
		    cc.pushDataComponentToOutput(OUT_TEXT, outputSafe);
		    
		    //
		    // Also push out the meta data here
		    //
		    
		    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
		}
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception 
    {
    }
    	
}
