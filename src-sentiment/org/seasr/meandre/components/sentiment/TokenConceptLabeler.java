package org.seasr.meandre.components.sentiment;







import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;



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
import org.seasr.meandre.support.parsers.DataTypeParser;


/**
 * This component perform POS tagging on the text passed using OpenNLP.
 *
 * @author Mike Haberman;
 *
 */

//
// General Path:   PosTagger -->PosTokenFrequencyCounter
//


@Component(
		name = "Token Concept Labeler",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
		description = "This component counts the incoming set of pos tuples " ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.0.3.jar"}
)
public class TokenConceptLabeler  extends AbstractExecutableComponent {
	

    //------------------------------ INPUTS ------------------------------------------------------
	
	@ComponentInput(
			name = "tokenCountTuples",
			description = "set of token tuples sorted by frequency: (count, token)"
	)
	protected static final String IN_FREQ_TUPLES = "tokenCountTuples";
	

    //------------------------------ OUTPUTS -----------------------------------------------------
	
   @ComponentOutput(
			name = "tokenCountTuplesLabeled",
			description = "set of token tuples (count, token, label)"
	)
	protected static final String OUT_FREQ_TUPLES = "tokenCountTuplesLabeled";
	
	
	
	
	
	//----------------------------- PROPERTIES ---------------------------------------------------

	//--------------------------------------------------------------------------------------------

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	//
	// this could come from properties
	// 
	// this is the ONLY dependency on TokenCountTuple
	//
	// int KEY_FIELD_IDX = TokenCountTuple.TUPLE_FIELDS.token.ordinal();
	
	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		
		/*
		int idx = StringTuple.findIndex("token");
		
		Strings input = (Strings) cc.getDataComponentFromInput(IN_FREQ_TUPLES);
		String[] tuples = DataTypeParser.parseAsString(input);
		List<StringTuple> output = new ArrayList<StringTuple>();
		
		
		
		for (int i = 0; i < tuples.length; i++) {
			
			StringTuple t = new StringTuple(tuples[i]);
			int labelIndex = t.fieldCount();
			
			StringTuple tupleWithLabel = new StringTuple(t, 1);
			tupleWithLabel.setValue(labelIndex, "");
			
			
			String token = tupleWithLabel.getValue(KEY_FIELD_IDX);
			
			
		}
		*/
		
		
		
		/*
		for (TokenCountTuple t:output) {
			console.info(t.toString());
		}
		*/
		
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        
    }
}
