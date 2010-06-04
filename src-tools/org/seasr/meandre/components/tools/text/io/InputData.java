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

package org.seasr.meandre.components.tools.text.io;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.webui.WebUIException;
import org.seasr.meandre.components.abstracts.AbstractGWTWebUIComponent;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This component allows the user to specify the dataset(s) to be processed. " +
        		      "The user can use URL(s), file(s), or input the raw text to be processed.",
        name = "Input Data",
        tags = "input, data, file",
        mode = Mode.webui,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar", "org.seasr.meandre.components.vis.gwt.inputdata.InputData.jar"},
        resources = {"InputData.vm"}
)
public class InputData extends AbstractGWTWebUIComponent {

    //--------------------------------------------------------------------------------------------


    protected static final String TEMPLATE = "org/seasr/meandre/components/tools/text/io/InputData.vm";

    private String _html;
    private boolean _done;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        //_context.put("pageSize", Integer.parseInt(ccp.getProperty(PROP_PAGE_SIZE)));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {

        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        //_context.put("columnFormatData", jaColumnFormat.toString());

        console.finest("Applying the Velocity template");
        _html = velocity.generateOutput(_context, TEMPLATE);

        _done = false;

        cc.startWebUIFragment(this);

        while (!cc.isFlowAborting() && !_done)
            Thread.sleep(1000);

        if (cc.isFlowAborting())
            console.info("Flow abort requested - terminating component execution...");

//        if (_done)
//            cc.pushDataComponentToOutput(OUT_TABLE, _origTable);

        cc.stopWebUIFragment(this);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        // TODO Auto-generated method stub

    }

    //--------------------------------------------------------------------------------------------

    @Override
    public void emptyRequest(HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "emptyRequest", response);

        try {
            response.getWriter().println(_html);
        } catch (Exception e) {
            throw new WebUIException(e);
        }

        console.exiting(getClass().getName(), "emptyRequest");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "handle", response);

        String reqPath = request.getPathInfo();
        console.fine("Request path: " + reqPath);
        console.fine("query string: " + request.getQueryString());

        response.setStatus(HttpServletResponse.SC_OK);

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
            emptyRequest(response);

        console.exiting(getClass().getName(), "handle");
    }

    //--------------------------------------------------------------------------------------------

    public String getContextPath() {
        return "/inputdata";
    }

    @Override
    public String getGWTModuleJARName() {
        return "org.seasr.meandre.components.vis.gwt.inputdata.InputData.jar";
    }

    //--------------------------------------------------------------------------------------------
}
