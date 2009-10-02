package org.seasr.meandre.components.sentiment;


import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
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
		name = "Tuple Sentiment Labeler",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
		description = "This component labels the incoming set of pos tuples " ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleSentimentLabeler  extends AbstractExecutableComponent {
	

    //------------------------------ INPUTS ------------------------------------------------------
	
	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "set of tuples to be labeled"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;
	
	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples to be labeled"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
	
	@ComponentInput(
			name = "conceptTuples",
			description = "set of tuples (labeled concepts)"
	)
	protected static final String IN_CONCEPT_TUPLES = "conceptTuples";
	
	@ComponentInput(
			name = "conceptMetaTuple",
			description = "meta data for tuples "
	)
	protected static final String IN_CONCEPT_META_TUPLE = "conceptMetaTuple";
	

    //------------------------------ OUTPUTS -----------------------------------------------------
	
	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of labeled tuples"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
	
	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (same as input, concept)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
	
	
	
	//----------------------------- PROPERTIES ---------------------------------------------------
   @ComponentProperty(description = "field name for the key",
		   name = "key",
		   defaultValue = "concept")
    protected static final String DATA_PROPERTY_CONCEPTS = "concept";
	//--------------------------------------------------------------------------------------------

   
	
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{
	}

	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		/*
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_CONCEPT_META_TUPLE);
		String[] meta = DataTypeParser.parseAsString(inputMeta);
		String fields = meta[0];
		DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
		
		Strings input = (Strings) cc.getDataComponentFromInput(IN_CONCEPT_TUPLES);
		String[] tuples = DataTypeParser.parseAsString(input);
		DynamicTuple tuple = inPeer.createTuple();
		*/
		
		//
		// Process the concept Map data
		//
		
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_CONCEPT_META_TUPLE);
		SimpleTuplePeer inPeer = new SimpleTuplePeer(inputMeta);
		
		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_CONCEPT_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		SimpleTuple tuple = inPeer.createTuple();
		
		//
		// convert the list of concept tokens to a map for easy access
		//
		
		int TOKEN_IDX   = inPeer.getIndexForFieldName("token");
		int CONCEPT_IDX = inPeer.getIndexForFieldName("concept");
		
		Map<String,String> wordToConceptMap = new HashMap<String,String>();
		for (int i = 0; i < in.length; i++) {
			
			tuple.setValues(in[i]);	
			String key   = tuple.getValue(TOKEN_IDX);
			String value = tuple.getValue(CONCEPT_IDX);
			
			wordToConceptMap.put(key, value);
		}
		
		
		
		//
		// now label the entire set of incoming tuples
		// (append the concept to the tuple)
		//
		/*
		inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		meta = DataTypeParser.parseAsString(inputMeta);
		fields = meta[0];
		inPeer = new DynamicTuplePeer(fields);
		DynamicTuplePeer outPeer = new DynamicTuplePeer(inPeer, new String[]{"concept"});		
		
		input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
		tuples = DataTypeParser.parseAsString(input);
		tuple = inPeer.createTuple();
		*/
		
		//
		// Process the tuple data
		//
		inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		inPeer = new SimpleTuplePeer(inputMeta);
		SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, new String[]{"concept"});	
		
		input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		tuple = inPeer.createTuple();
		
		
		TOKEN_IDX   = inPeer.getIndexForFieldName("token");
		CONCEPT_IDX = outPeer.getIndexForFieldName("concept");
		
		List<Strings> output = new ArrayList<Strings>();
		SimpleTuple outTuple = outPeer.createTuple();
		for (int i = 0; i < in.length; i++) {
			tuple.setValues(in[i]);	
			String key = tuple.getValue(TOKEN_IDX);
			String concept = wordToConceptMap.get(key);
			
			if (concept != null) {
				outTuple.setValue(tuple);
				outTuple.setValue(CONCEPT_IDX, concept);
				output.add(outTuple.convert());
			}
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

