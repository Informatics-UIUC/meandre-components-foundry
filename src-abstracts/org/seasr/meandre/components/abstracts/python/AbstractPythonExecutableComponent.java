package org.seasr.meandre.components.abstracts.python;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;


public abstract class AbstractPythonExecutableComponent implements ExecutableComponent {

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
