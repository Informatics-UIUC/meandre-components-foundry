package org.seasr.meandre.components.abstracts.python;

import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.seasr.datatypes.core.Names;


public abstract class AbstractPythonExecutableComponent implements ExecutableComponent {

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "This port is used to output any unhandled errors encountered during the execution of this component",
            name = Names.PORT_ERROR
    )
    public static final String OUT_ERROR = Names.PORT_ERROR;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "Controls the verbosity of debug messages printed by the component during execution.<br/>" +
                          "Possible values are: off, severe, warning, info, config, fine, finer, finest, all<br>" +
                          "Append ',mirror' to any of the values above to mirror that output to the server logs.",
            defaultValue = "info",
            name = "_debug_level"
    )
    public static final String PROP_DEBUG_LEVEL = "_debug_level";

    @ComponentProperty(
            description = "Set to 'true' to ignore all unhandled exceptions and prevent the flow from being terminated. " +
                          "Setting this property to 'false' will result in the flow being terminated in the event " +
                          "an unhandled exception is thrown during the execution of this component",
            defaultValue = "false",
            name = "_ignore_errors"
    )
    public static final String PROP_IGNORE_ERRORS = "_ignore_errors";

    //--------------------------------------------------------------------------------------------


    private PythonInterpreter _pythonInterpreter;

    protected AbstractPythonExecutableComponent() {
        PySystemState sys = new PySystemState();
        sys.setClassLoader(getClass().getClassLoader());

        _pythonInterpreter = new PythonInterpreter(null, sys);
    }

    public void initialize(ComponentContextProperties ccp) throws ComponentExecutionException, ComponentContextException {
      _pythonInterpreter.exec(String.format("from %1$s import %1$s", getClass().getSimpleName()));
      _pythonInterpreter.exec(String.format("component = %s()", getClass().getSimpleName()));
      _pythonInterpreter.set("ccp", ccp);
      _pythonInterpreter.exec("component.initialize(ccp)");
    }

    public void execute(ComponentContext cc) throws ComponentExecutionException, ComponentContextException {
      _pythonInterpreter.set("cc", cc);
      _pythonInterpreter.exec("component.execute(cc)");
    }

    public void dispose(ComponentContextProperties ccp) throws ComponentExecutionException, ComponentContextException {
      _pythonInterpreter.set("ccp", ccp);
      _pythonInterpreter.exec("component.dispose(ccp)");
      _pythonInterpreter.cleanup();
      _pythonInterpreter = null;
    }
}
