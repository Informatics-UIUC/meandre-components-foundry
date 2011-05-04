package org.seasr.meandre.components.tools;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

@Component(
        name = "GC",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "jvm, garbage collection",
        description = "Asks the JVM to perform garbage collection"
)
public class GC extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_OBJECT,
            description = "The object" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_OBJECT = Names.PORT_OBJECT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_OBJECT,
            description = "The unmodified input object"
    )
    protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "frequency",
            description = "Invoke garbage collection once every 'frequency' inputs",
            defaultValue = "100"
    )
    protected static final String PROP_FREQUENCY = "frequency";

    //--------------------------------------------------------------------------------------------


    private static int COUNT = 0;
    private int _frequency;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _frequency = Integer.parseInt(getPropertyOrDieTrying(PROP_FREQUENCY, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (++COUNT % _frequency == 0) {
            console.fine("Requesting garbage collection...");
            System.gc();
        }

        cc.pushDataComponentToOutput(OUT_OBJECT, cc.getDataComponentFromInput(IN_OBJECT));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
