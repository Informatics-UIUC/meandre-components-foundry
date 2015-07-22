package org.seasr.meandre.components.tools.tuples;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Tuple Partitioner",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tuple",
        description = "This component partitions a large tuple into smaller partitions of a specified size" ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TuplePartitioner extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The tuples" +
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
            description = "The partitioned tuples" +
            		"<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "meta data for the tuples (same as input)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The number desired output partitions [partition size calculated as Math.ceil(input_length/num_partitions)]",
            name = "num_partitions",
            defaultValue = ""
    )
    protected static final String PROP_NUM_PARTITIONS = "num_partitions";
    
    @ComponentProperty(
            description = "The number of elements per partition [number of partitions calculated as Math.ceil(input_length/partition_size)]",
            name = "partition_size",
            defaultValue = ""
    )
    protected static final String PROP_PARTITION_SIZE = "partition_size";
    
    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

    //--------------------------------------------------------------------------------------------


    protected boolean _wrapStream;
    protected Integer _numPartitions = null;
    protected Integer _partitionSize = null;
    

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        String sNumPartitions = getPropertyOrDieTrying(PROP_NUM_PARTITIONS, true, false, ccp);
        String sPartitionSize = getPropertyOrDieTrying(PROP_PARTITION_SIZE, true, false, ccp);
        
        if ((sNumPartitions.isEmpty() && sPartitionSize.isEmpty()) || 
        		(!sNumPartitions.isEmpty() && !sPartitionSize.isEmpty()))
        	throw new ComponentContextException(
        			String.format("You must set a value for only one of the following properties: [%s, %s]",
        					PROP_NUM_PARTITIONS, PROP_PARTITION_SIZE));
        
        _wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
		
        if (!sNumPartitions.isEmpty()) {
			_numPartitions = Integer.parseInt(sNumPartitions);
			if (_numPartitions <= 0)
				throw new IllegalArgumentException(PROP_NUM_PARTITIONS + " cannot be <= 0");
        }
        
        if (!sPartitionSize.isEmpty()) {
        	_partitionSize = Integer.parseInt(sPartitionSize);
        	if (_partitionSize <= 0)
				throw new IllegalArgumentException(PROP_PARTITION_SIZE + " cannot be <= 0");
        }        
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);

        int size = input.getValueCount();
        Integer partitionSize = _partitionSize;
        
        if (_wrapStream) {
            StreamDelimiter sd = new StreamInitiator(streamId);
            cc.pushDataComponentToOutput(OUT_META_TUPLE, sd);
            cc.pushDataComponentToOutput(OUT_TUPLES, sd);
        }
        
        if (partitionSize == null)
        	partitionSize = (int) Math.round((float) size / _numPartitions);
        
        if (size <= partitionSize) {
        	// optimization
        	cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
        	cc.pushDataComponentToOutput(OUT_TUPLES, input);
        } else {
        	for (int i = 0; i < size; i += partitionSize) {
        		StringsArray.Builder tuplesBuilder = StringsArray.newBuilder();
        		for (int j = i, jMax = Math.min(i+partitionSize, size); j < jMax; j++)
        			tuplesBuilder.addValue(input.getValue(j));

        		cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
        		cc.pushDataComponentToOutput(OUT_TUPLES, tuplesBuilder.build());
        	}
        }

        if (_wrapStream) {
            StreamDelimiter sd = new StreamTerminator(streamId);
            cc.pushDataComponentToOutput(OUT_META_TUPLE, sd);
            cc.pushDataComponentToOutput(OUT_TUPLES, sd);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }
}
