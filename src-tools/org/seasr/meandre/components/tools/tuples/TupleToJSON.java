package org.seasr.meandre.components.tools.tuples;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Tuple To JSON",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "tuple, json",
        description = "This component converts the rows of a tuple into an array of JSON objects that are keyed on the tuple column labels" ,
        dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleToJSON extends AbstractExecutableComponent {

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

    //------------------------------ OUTPUTS ------------------------------

    @ComponentOutput(
            name = "json",
            description = "The JSON encoding of the tuples" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_JSON = "json";

    //--------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);

        SimpleTuplePeer inPeer = new SimpleTuplePeer(inMeta);
        SimpleTuple tuple = inPeer.createTuple();

        Strings[] inTuples = BasicDataTypesTools.stringsArrayToJavaArray(input);

        JSONArray jaTuples = new JSONArray();
        for (Strings inTuple : inTuples) {
            tuple.setValues(inTuple);

            JSONObject joTuple = new JSONObject();
            for (String fieldName : inPeer.getFieldNames())
                joTuple.put(fieldName, tuple.getValue(fieldName));

            jaTuples.put(joTuple);
        }

        cc.pushDataComponentToOutput(OUT_JSON, BasicDataTypesTools.stringToStrings(jaTuples.toString(3)));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
