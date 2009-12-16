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

package org.seasr.meandre.components.vis.gwt;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractGWTWebUIComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.webui.WebUIException;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Example component that uses GWT",
        name = "HelloWorld",
        tags = "gwt",
        mode = Mode.webui,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar", "org.seasr.meandre.components.vis.gwt.helloworld.HelloWorld.jar"},
        resources = {"HelloWorld.vm"}
)
public class HelloWorld extends AbstractGWTWebUIComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The text to pass to the GWT app" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_HTML,
            description = "The HTML to view" +
                "<br>TYPE: java.lang.String"
    )
    protected static final String OUT_HTML = Names.PORT_HTML;

    //--------------------------------------------------------------------------------------------

    protected static final String TEMPLATE = "org/seasr/meandre/components/vis/gwt/HelloWorld.vm";

    private String _message;
    private String _html;
    private boolean _done;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        // Add more initialization code here if needed
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        _message = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];

        JSONObject person = new JSONObject();
        person.put("firstName", "John");
        person.put("lastName", "Doe");
        person.put("age", 23);

        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        _context.put("json", person.toString());

        console.finest("Applying the Velocity template");
        _html = velocity.generateOutput(_context, TEMPLATE);

        _done = false;

        cc.startWebUIFragment(this);

        cc.pushDataComponentToOutput(OUT_HTML, _html);

        while (!cc.isFlowAborting() && !_done)
            Thread.sleep(1000);

        if (cc.isFlowAborting())
            console.info("Flow abort requested - terminating component execution...");

        cc.stopWebUIFragment(this);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    /**
     * This method gets called when a request with no parameters is made to this component
     *
     * @param response The response object
     * @throws WebUIException Thrown if a problem occurs when generating the response
     */
    @Override
    public void emptyRequest(HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "emptyRequest", response);

// NOTE: Uncomment the following lines to make this component a standalone WebUI that does not need HTMLViewer to be displayed
//        try {
//            response.getWriter().println(_html);
//        } catch (Exception e) {
//            throw new WebUIException(e);
//        }

        console.exiting(getClass().getName(), "emptyRequest");
    }

    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem occurred during the call back
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "handle", response);

        String reqPath = request.getPathInfo();
        console.fine("Request path: " + reqPath);
        console.fine("query string: " + request.getQueryString());

        response.setStatus(HttpServletResponse.SC_OK);

        String action;

        if (request.getParameter("done") != null) {
            _done = true;
            try {
                response.getWriter().println("<html><head><meta http-equiv='REFRESH' content='1;url=/'></head><body></body></html>");
            }
            catch (IOException e) {
                throw new WebUIException(e);
            }
        }

        else

        if ((action = request.getParameter("action")) != null) {
            console.fine("action: " + action);

            if (action.equals("getMessage")) {
                try {
                    response.getWriter().print(_message);
                    response.flushBuffer();
                }
                catch (IOException e) {
                    throw new WebUIException(e);
                }
            }

            else {
                try {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    console.exiting(getClass().getName(), "handle");
                    return;
                }
                catch (IOException e) {
                    throw new WebUIException(e);
                }
            }
        }

        else
            emptyRequest(response);

        console.exiting(getClass().getName(), "handle");
    }

    //--------------------------------------------------------------------------------------------

    public String getContextPath() {
        return "/helloworld";
    }

    @Override
    public String getGWTModuleJARName() {
        return "org.seasr.meandre.components.vis.gwt.helloworld.HelloWorld.jar";
    }
}