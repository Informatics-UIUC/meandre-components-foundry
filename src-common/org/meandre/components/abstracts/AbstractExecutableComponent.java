/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.meandre.components.abstracts;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.annotations.ComponentProperty;
import org.meandre.components.ComponentInputCache;
import org.meandre.components.PackedDataComponents;
import org.meandre.components.abstracts.util.WebConsoleHandler;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.meandre.components.tools.Names;

/**
 * @author Bernie Acs
 * @author Boris Capitanu
 */
public abstract class AbstractExecutableComponent implements ExecutableComponent {

    @ComponentProperty(description = "Controls the verbosity of debug messages printed by the component during execution.<br/>" +
                                     "Possible values are: off, severe, warning, info, config, fine, finer, finest, all<br>" +
                                     "Append ',mirror' to any of the values above to mirror that output to the server logs.",
                       defaultValue = "info",
                       name = Names.PROP_DEBUG_LEVEL)
    protected static final String PROP_DEBUG_LEVEL = Names.PROP_DEBUG_LEVEL;

    @ComponentProperty(description = "Set to 'true' to ignore all unhandled exceptions and prevent the flow from being terminated. " +
                                     "Setting this property to 'false' will result in the flow being terminated in the event " +
                                     "an unhandled exception is thrown during the execution of this component",
                       defaultValue = "false",
                       name = Names.PROP_ERROR_HANDLING)
    protected static final String PROP_IGNORE_ERRORS = Names.PROP_ERROR_HANDLING;

    private ComponentContext _componentContext = null;
    private Logger _consoleLogger;
    private boolean _ignoreErrors;

    private Set<String> _connectedInputs = new HashSet<String>();
    private Set<String> _connectedOutputs = new HashSet<String>();
    //
    protected ComponentInputCache componentInputCache = new ComponentInputCache();
    //
    protected PackedDataComponents packedDataComponentsInput = null;
    protected PackedDataComponents packedDataComponentsOutput = null;

    //

    /**
     * Enables runtime interrogation to determine if a ComponentInput is
     * connected in a flow.
     *
     * @param componentInputName
     * @return
     */
    public boolean isComponentInputConnected(String componentInputName) {
        return _connectedInputs.contains(componentInputName);
    }

    /**
     * Enables runtime interrogation to determine if a ComponentOutput is
     * connected in a flow.
     *
     * @param componentOutputName
     * @return
     */
    public boolean isComponentOutputConnected(String componentOutputName) {
        return _connectedOutputs.contains(componentOutputName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
     */
    public void initialize(ComponentContextProperties ccp)
            throws ComponentExecutionException, ComponentContextException {

        Handler consoleHandler = new WebConsoleHandler(ccp.getOutputConsole(), Logger.getLogger("").getHandlers()[0].getFormatter());
        consoleHandler.setLevel(Level.ALL);

        _consoleLogger = Logger.getLogger(ccp.getFlowExecutionInstanceID() + "/" + ccp.getExecutionInstanceID());
        _consoleLogger.addHandler(consoleHandler);

        _consoleLogger.setParent(ccp.getLogger());
        _consoleLogger.setLevel(Level.ALL);

        String debugLevel = ccp.getProperty(PROP_DEBUG_LEVEL).trim();
        StringTokenizer st = new StringTokenizer(debugLevel, " ,;/+&");
        if (st.countTokens() > 2)
            throw new ComponentContextException("Invalid value for property '" + PROP_DEBUG_LEVEL + "' specified: " + debugLevel);

        Vector<String> tokens = new Vector<String>();
        while (st.hasMoreTokens())
            tokens.add(st.nextToken().trim().toUpperCase());

        boolean mirrorConsoleOutput = false;
        if (tokens.contains("MIRROR")) {
            mirrorConsoleOutput = true;
            tokens.remove("MIRROR");
        }

        _consoleLogger.setUseParentHandlers(mirrorConsoleOutput);

        try {
            Level consoleOutputLevel = Level.parse(tokens.get(0));
            _consoleLogger.setLevel(consoleOutputLevel);
        }
        catch (IllegalArgumentException e) {
            _consoleLogger.throwing(getClass().getName(), "initialize", e);
            throw new ComponentContextException(e);
        }

        _ignoreErrors = Boolean.parseBoolean(ccp.getProperty(PROP_IGNORE_ERRORS));
        if (_ignoreErrors)
            _consoleLogger.info("Exceptions are being ignored per user's request.");

        for (String componentInput : ccp.getInputNames())
            _connectedInputs.add(componentInput);

        for (String componentOutput : ccp.getOutputNames())
            _connectedOutputs.add(componentOutput);

        componentInputCache.setLogger(_consoleLogger);

        try {
            _consoleLogger.entering(getClass().getName(), "initializeCallBack", ccp);
            initializeCallBack(ccp);
            _consoleLogger.exiting(getClass().getName(), "initializeCallBack");
        }
        catch (ComponentContextException e) {
            _consoleLogger.throwing(getClass().getName(), "initializeCallBack", e);
            throw e;
        }
        catch (ComponentExecutionException e) {
            _consoleLogger.throwing(getClass().getName(), "initializeCallBack", e);
            if (!_ignoreErrors)
                throw e;
        }
        catch (Exception e) {
            _consoleLogger.throwing(getClass().getName(), "initializeCallBack", e);
            if (!_ignoreErrors)
                throw new ComponentContextException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
     */
    public void execute(ComponentContext cc)
            throws ComponentExecutionException, ComponentContextException {

        _componentContext = cc;

        // Initialize the PackedDataComponent variables each iteration
        packedDataComponentsInput = new PackedDataComponents();
        packedDataComponentsOutput = new PackedDataComponents();

        try {
            boolean callExecute = true;

            Set<String> inputPortsWithInitiators = new HashSet<String>();
            Set<String> inputPortsWithTerminators = new HashSet<String>();

            for (String inputPort : _connectedInputs) {
                Object data = cc.getDataComponentFromInput(inputPort);

                // show the inputs and data-types received on each input in "debug" mode
                _consoleLogger.finer(String.format("Input port '%s' has data of type '%s'",
                        inputPort, data.getClass().getName()));

                if (data instanceof StreamInitiator)
                    inputPortsWithInitiators.add(inputPort);

                else

                if (data instanceof StreamTerminator)
                    inputPortsWithTerminators.add(inputPort);
            }

            if (inputPortsWithInitiators.size() > 0) {
                callExecute = false;
                handleStreamInitiators(cc, inputPortsWithInitiators);
            }

            if (inputPortsWithTerminators.size() > 0) {
                callExecute = false;
                handleStreamTerminators(cc, inputPortsWithTerminators);
            }

            if (callExecute) {
                _consoleLogger.entering(getClass().getName(), "executeCallBack", cc);
                executeCallBack(cc);
                _consoleLogger.exiting(getClass().getName(), "executeCallBack");
            }
        }
        catch (ComponentContextException e) {
            _consoleLogger.throwing(getClass().getName(), "executeCallBack", e);
            throw e;
        }
        catch (ComponentExecutionException e) {
            _consoleLogger.throwing(getClass().getName(), "executeCallBack", e);
            if (!_ignoreErrors)
                throw e;
        }
        catch (Exception e) {
            _consoleLogger.throwing(getClass().getName(), "executeCallBack", e);
            if (!_ignoreErrors)
                throw new ComponentExecutionException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
     */
    public void dispose(ComponentContextProperties ccp)
            throws ComponentExecutionException, ComponentContextException {

        try {
            _consoleLogger.entering(getClass().getName(), "disposeCallBack", ccp);
            disposeCallBack(ccp);
            _consoleLogger.exiting(getClass().getName(), "disposeCallBack");
        }
        catch (ComponentContextException e) {
            _consoleLogger.throwing(getClass().getName(), "disposeCallBack", e);
            throw e;
        }
        catch (ComponentExecutionException e) {
            _consoleLogger.throwing(getClass().getName(), "disposeCallBack", e);
            if (!_ignoreErrors)
                throw e;
        }
        catch (Exception e) {
            _consoleLogger.throwing(getClass().getName(), "disposeCallBack", e);
            if (!_ignoreErrors)
                throw new ComponentContextException(e);
        }
    }

    public abstract void initializeCallBack(ComponentContextProperties ccp)
        throws Exception;

    public abstract void executeCallBack(ComponentContext cc)
        throws Exception;

    public abstract void disposeCallBack(ComponentContextProperties ccp)
        throws Exception;

    public ComponentContext getComponentContext() {
        return _componentContext;
    }

    public Logger getConsoleLogger() {
        return _consoleLogger;
    }

    /**
     * Forwards or ignores the delimiter depending on the number of inputs/outputs the component has;
     * Override if needing to handle the delimiters differently.
     *
     * @param cc The component context
     * @param inputPortsWithInitiators The set of ports where stream initiators arrived
     * @throws ComponentContextException Thrown in the event of an error
     */
    protected void handleStreamInitiators(ComponentContext cc, Set<String> inputPortsWithInitiators)
        throws ComponentContextException, ComponentExecutionException {

        _consoleLogger.entering(getClass().getName(), "handleStreamInitiators", inputPortsWithInitiators);

        if (_connectedInputs.size() == 1 && _connectedOutputs.size() == 1) {
            _consoleLogger.fine("Forwarding " + StreamInitiator.class.getSimpleName() + " to the next component...");
            cc.pushDataComponentToOutput(cc.getOutputNames()[0], cc.getDataComponentFromInput(cc.getInputNames()[0]));
        } else
            _consoleLogger.fine("Ignoring " + StreamInitiator.class.getSimpleName() + " received on ports " + inputPortsWithInitiators);

        _consoleLogger.exiting(getClass().getName(), "handleStreamInitiators");
    }

    /**
     * Forwards or ignores the delimiter depending on the number of inputs/outputs the component has;
     * Override if needing to handle the delimiters differently.
     *
     * @param cc The component context
     * @param inputPortsWithTerminators The set of ports where stream terminators arrived
     * @throws ComponentContextException Thrown in the event of an error
     */
    protected void handleStreamTerminators(ComponentContext cc, Set<String> inputPortsWithTerminators)
        throws ComponentContextException, ComponentExecutionException {

        _consoleLogger.entering(getClass().getName(), "handleStreamTerminators", inputPortsWithTerminators);

        if (_connectedInputs.size() == 1 && _connectedOutputs.size() == 1) {
            _consoleLogger.fine("Forwarding " + StreamTerminator.class.getSimpleName() + " to the next component...");
            cc.pushDataComponentToOutput(cc.getOutputNames()[0], cc.getDataComponentFromInput(cc.getInputNames()[0]));
        } else
            _consoleLogger.fine("Ignoring " + StreamTerminator.class.getSimpleName() + " received on ports " + inputPortsWithTerminators);

        _consoleLogger.exiting(getClass().getName(), "handleStreamTerminators");
    }
}
