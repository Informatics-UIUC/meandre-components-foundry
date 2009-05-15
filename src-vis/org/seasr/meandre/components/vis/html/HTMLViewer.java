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

package org.seasr.meandre.components.vis.html;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.html.VelocityTemplateService;
import org.seasr.meandre.support.parsers.DataTypeParser;

@Component(creator = "Loretta Auvil",
           description = "Generates a webpage from the HTML text that it receives as input.",
           name = "HTML Viewer",
           tags = "html, viewer",
           mode = Mode.webui,
           baseURL = "meandre://seasr.org/components/",
           dependency = {"velocity-1.6.1-dep.jar"},
           resources = {"HTMLViewer.vm"})

/**
 * @author Loretta Auvil
 * @author Boris Capitanu
 */
public class HTMLViewer extends AbstractExecutableComponent implements WebUIFragmentCallback {

	@ComponentInput(description = "The HTML data" +
                                  "<br>TYPE: String, Text, byte[]",
                    name = Names.PORT_HTML)
    protected static final String IN_HTML = Names.PORT_HTML;

	@ComponentProperty(defaultValue = "org/meandre/components/vis/html/HTMLViewer.vm",
	                   description = "The template to use for wrapping the HTML input",
	                   name = Names.PROP_TEMPLATE)
    protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;


    private Logger _console;
    private VelocityContext _context;
    private String _templateName;
    private String _html;
    private boolean _done;


    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _console = getConsoleLogger();
        _templateName = ccp.getProperty(PROP_TEMPLATE);

        // sanity check
        if (_templateName.trim().length() == 0) {
            _templateName = null;
            _console.fine("No template specified - Velocity will not be invoked");
        }
        else {
            _context = VelocityTemplateService.getInstance().getNewContext();
            _context.put("ccp", ccp);
        }
    }

    public void executeCallBack(ComponentContext cc) throws Exception {
        Object data = cc.getDataComponentFromInput(IN_HTML);
        _console.fine("Received input of type: " + data.getClass().toString());

        _html = DataTypeParser.parseAsString(data);

        // Check whether Velocity should be used
        if (_templateName != null) {
            VelocityTemplateService velocity = VelocityTemplateService.getInstance();
            _context.put("rawHtml", _html);

            _console.finest("Applying the Velocity template");
            _html = velocity.generateOutput(_context, _templateName);
        }

        _done = false;

        cc.startWebUIFragment(this);

        while (!cc.isFlowAborting() && !_done)
            Thread.sleep(1000);

        cc.stopWebUIFragment(this);
    }

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    /**
     * This method gets called when a request with no parameters is made to this component
     *
     * @param response The response object
     * @throws WebUIException Thrown if a problem occurs when generating the response
     */
    public void emptyRequest(HttpServletResponse response) throws WebUIException {
        _console.entering(getClass().getName(), "emptyRequest", response);

        try {
            response.getWriter().println(_html);
        } catch (Exception e) {
            throw new WebUIException(e);
        }

        _console.exiting(getClass().getName(), "emptyRequest");
    }

    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem occurred during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException {
        _console.entering(getClass().getName(), "handle", response);

        if (request.getParameter("done") != null)
            _done = true;
        else
            emptyRequest(response);

        _console.exiting(getClass().getName(), "handle");
    }

}