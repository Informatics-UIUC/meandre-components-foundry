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

package org.seasr.meandre.components.abstracts.util;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;

/**
 * @author Bernie Acs
 *
 * Basic cache abstract that can be used to collect, store, and retrieve objects
 * made available to during the execute() of org.meandre.component.ExecutableComponent.
 * A general purpose set of mechanics to aid in handle logic where the a compute
 * component uses FiringPolicy.any and some inputs are required, some may be optional,
 * OR where some set of inputs are required to handle a logical processing cycle. This
 * Object is intended to be couple with a single ExecutableComponent.
 *
 */
public abstract class AbstractComponentInputCache {

	/**
	 * Internal storage container for a Collection of java.util.Queue Objects keyed by input Name.
	 */
	private Map<String, Queue<Object>> _inputCacheMap;

	private Logger _logger = null;


	protected AbstractComponentInputCache(Set<String> portNames) {
	    _inputCacheMap = new Hashtable<String, Queue<Object>>(portNames.size());
	    for (String portName : portNames) _inputCacheMap.put(portName, new LinkedList<Object>());
	}

	synchronized public void storeIfAvailable(ComponentContext cc, String portName) throws ComponentContextException {
	    if (!cc.isInputAvailable(portName)) return;
	    store(cc, portName);
	}

	synchronized public void store(ComponentContext cc, String portName) throws ComponentContextException {
	    Object input = cc.getDataComponentFromInput(portName);
	    _inputCacheMap.get(portName).add(input);
	}

	synchronized public Object retrieveNext(String portName) throws ComponentContextException {
	    return hasData(portName) ? _inputCacheMap.get(portName).poll() : null;
	}

	synchronized public Object peek(String portName) throws ComponentContextException {
	    return hasData(portName) ? _inputCacheMap.get(portName).peek() : null;
	}

	synchronized public boolean hasData(String portName) throws ComponentContextException {
	    if (!_inputCacheMap.containsKey(portName))
	        throw new ComponentContextException("Unknown port name specified: " + portName);

	    return !_inputCacheMap.get(portName).isEmpty();
	}

	synchronized public boolean hasDataAll(String[] portNames) throws ComponentContextException {
	    boolean hasData = true;
	    for (String portName : portNames)
	        hasData &= hasData(portName);

	    return hasData;
	}

	synchronized public Integer getDataCount(String portName) throws ComponentContextException {
	    return hasData(portName) ? _inputCacheMap.get(portName).size() : 0;
	}

	public void setLogger(Logger logger) {
	    _logger = logger;
	}

	public void dispose() {
	    for (Queue<Object> queue : _inputCacheMap.values())
	        queue.clear();

	    _inputCacheMap.clear();
	    _inputCacheMap = null;

	    _logger = null;
	}
}
