package org.seasr.meandre.components.tools.tuples;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
*
* @author Boris Capitanu
*
*/

@Component(
       name = "Tuple Random Sample",
       creator = "Boris Capitanu",
       baseURL = "meandre://seasr.org/components/foundry/",
       firingPolicy = FiringPolicy.all,
       mode = Mode.compute,
       rights = Licenses.UofINCSA,
       tags = "#TRANSFORM, tuple, tools, sample",
       description = "This component creates a random sample of a specified size " +
       		"from the tuples received as input" ,
       dependency = { "protobuf-java-2.2.0.jar" }
)
public class TupleRandomSample extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The set of tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The random sample set of tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
    
    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "sample_size",
            description = "The size of the random sample to generate",
            defaultValue = ""
    )
    protected static final String PROP_SAMPLE_SIZE = "sample_size";
    
    @ComponentProperty(
            name = Names.PROP_SEED,
            description = "The random seed value (can be empty)",
            defaultValue = ""
    )
    protected static final String PROP_RANDOM_SEED = Names.PROP_SEED;

    //--------------------------------------------------------------------------------------------


    protected int _sampleSize;
    protected ThreadLocalRandom _random;


    //--------------------------------------------------------------------------------------------
	
    @Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    	_sampleSize = Integer.parseInt(getPropertyOrDieTrying(PROP_SAMPLE_SIZE, ccp));
    	String sRandomSeed = getPropertyOrDieTrying(PROP_RANDOM_SEED, true, false, ccp);
		Long randomSeed = sRandomSeed.isEmpty() ? null : Long.parseLong(sRandomSeed);
		
    	_random = ThreadLocalRandom.current();
    	if (randomSeed != null) {
    		console.fine("Using random seed value: " + randomSeed);
    		_random.setSeed(randomSeed);
    	}
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
        List<Strings> tuples = input.getValueList();
        StringsArray output = input;
       
        int size = input.getValueCount();
       
        if (size > _sampleSize) {
        	StringsArray.Builder tuplesBuilder = StringsArray.newBuilder();
        	for (int i = 0; i < _sampleSize; i++) {
        		int pos = _random.nextInt(i, size);
        		tuplesBuilder.addValue(tuples.get(pos));
        		tuples.set(pos, tuples.get(i));
        	}
        	output = tuplesBuilder.build();
        }
        
        cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
        cc.pushDataComponentToOutput(OUT_TUPLES, output);
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {		
	}

}
