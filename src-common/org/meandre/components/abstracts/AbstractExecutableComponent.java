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

/**
 *
 *
 * @author bernie acs
 * @author Boris Capitanu
 *
 */
public abstract class AbstractExecutableComponent implements
        ExecutableComponent {

    @ComponentProperty(description = "Controls ConsoleOutput during runtime; values may be:\n"
            + "(off, severe, warning, info, config, fine, finer, finest, all)", name = "ConsoleOutput", defaultValue = "info")
    public static final String ConsoleOutput = "ConsoleOutput";

    @ComponentProperty(description = "Controls ConsoleOutput should be mirrored Logger() facility on server; values may be:\n"
            + "(true = {do mirror output} | any other value = {do not mirror output} )", name = "ConsoleOutputMirrorToLogs", defaultValue = "off")
    public static final String ConsoleOutputMirrorToLogs = "ConsoleOutputMirrorToLogs";

    private ComponentContext _componentContext = null;
    private Logger _consoleLogger;

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

        try {
            Level consoleOutputLevel = Level.parse(ccp.getProperty(ConsoleOutput).toUpperCase());
            _consoleLogger.setLevel(consoleOutputLevel);
        }
        catch (IllegalArgumentException e) {
            _consoleLogger.throwing(getClass().getName(), "initialize", e);
            throw new ComponentContextException(e);
        }

        boolean mirrorConsoleOutput = Boolean.parseBoolean(ccp.getProperty(ConsoleOutputMirrorToLogs));
        _consoleLogger.setUseParentHandlers(mirrorConsoleOutput);

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
            throw e;
        }
        catch (Exception e) {
            _consoleLogger.throwing(getClass().getName(), "initializeCallBack", e);
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
            _consoleLogger.entering(getClass().getName(), "executeCallBack", cc);
            executeCallBack(cc);
            _consoleLogger.exiting(getClass().getName(), "executeCallBack");
        }
        catch (ComponentContextException e) {
            _consoleLogger.throwing(getClass().getName(), "executeCallBack", e);
            throw e;
        }
        catch (ComponentExecutionException e) {
            _consoleLogger.throwing(getClass().getName(), "executeCallBack", e);
            throw e;
        }
        catch (Exception e) {
            _consoleLogger.throwing(getClass().getName(), "executeCallBack", e);
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
            throw e;
        }
        catch (Exception e) {
            _consoleLogger.throwing(getClass().getName(), "disposeCallBack", e);
            throw new ComponentContextException(e);
        }
    }

    public abstract void disposeCallBack(ComponentContextProperties ccp)
            throws Exception;

    public abstract void executeCallBack(ComponentContext cc)
            throws Exception;

    public abstract void initializeCallBack(ComponentContextProperties ccp)
            throws Exception;

    public ComponentContext getComponentContext() {
        return _componentContext;
    }

    public Logger getConsoleLogger() {
        return _consoleLogger;
    }
}