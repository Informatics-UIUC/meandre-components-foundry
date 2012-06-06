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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.crypto.Crypto;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;
import org.seasr.meandre.support.generic.io.IOUtils;

/**
 * @author Loretta Auvil
 * @author Boris Capitanu
 * @author Mike Haberman
 */

// Note the differences between HTMLViewer and GenericTemplate
// This class will actually write the html to the local server's filesystem such
// that it can be fetched inside an iFrame
//
// GenericTemplate is very similar except it returns the html in the HttpResponse
// Perhaps both should be looked at and refactored using the best of both
//

@Component(
        creator = "Loretta Auvil",
        description = "Generates a webpage from the HTML text that it receives as input.",
        name = "HTML Viewer",
        tags = "#VIS, html, viewer",
        mode = Mode.webui,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"},
        resources = {"HTMLViewer.vm"}
)
public class HTMLViewer extends AbstractExecutableComponent implements WebUIFragmentCallback {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        name = Names.PORT_HTML,
	        description = "The HTML data" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
	)
    protected static final String IN_HTML = Names.PORT_HTML;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        defaultValue = "org/seasr/meandre/components/vis/html/HTMLViewer.vm",
	        description = "The template to use for wrapping the HTML input",
	        name = Names.PROP_TEMPLATE
	)
    protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;

    //--------------------------------------------------------------------------------------------


    private VelocityContext _context;
    private String _templateName;
    private String _html;
    private boolean _done;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _templateName = ccp.getProperty(PROP_TEMPLATE);

        // sanity check
        if (_templateName.trim().length() == 0) {
            _templateName = null;
            console.fine("No template specified - Velocity will not be invoked");
        }
        else {
            _context = VelocityTemplateService.getInstance().getNewContext();
            _context.put("ccp", ccp);
        }
    }

    public String getWebUIUrl(ComponentContext cc) throws Exception
    {
    	String webUIUrl = cc.getWebUIUrl(true).toString();
        if (webUIUrl.endsWith("/"))
        	webUIUrl = webUIUrl.substring(0, webUIUrl.length()-1);
        return webUIUrl;
    }


    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        List<File> files = new ArrayList<File>();

        for (String html : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_HTML))) {
            _html = html;

            // Check whether Velocity should be used
            if (_templateName != null) {
                String fName = Crypto.toHexString(Crypto.createMD5Hash(_html.getBytes())) + ".html";
                String baseDir = cc.getPublicResourcesDirectory() + File.separator + "html_viewer";
                new File(baseDir).mkdirs();

                File htmlFile = new File(baseDir, fName);
                if (!htmlFile.exists()) {
                    console.finer("Creating HTML file: " + htmlFile.toString());
                    Writer writer = IOUtils.getWriterForResource(htmlFile.toURI());
                    writer.write(_html);
                    writer.close();

                    files.add(htmlFile);
                } else
                    console.finer("HTML file already exists - moving on...");

                VelocityTemplateService velocity = VelocityTemplateService.getInstance();
                _context.put("htmlLocation", "/public/resources/html_viewer/" + fName);

                console.finest("Applying the Velocity template");
                _html = velocity.generateOutput(_context, _templateName);
            }

            _done = false;

            String webURL = getWebUIUrl(cc);
            console.info("webUIUrl " + webURL);

            cc.startWebUIFragment(this);

            while (!cc.isFlowAborting() && !_done)
                Thread.sleep(1000);

            if (cc.isFlowAborting())
                console.info("Flow abort requested - terminating component execution...");

            // Clean up
            for (File file : files)
                file.delete();

            cc.stopWebUIFragment(this);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public void handleStreamInitiators() throws Exception {
        // ignore initiators
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        // ignore terminators
    }

    //--------------------------------------------------------------------------------------------

    /**
     * This method gets called when a request with no parameters is made to this component
     *
     * @param response The response object
     * @throws WebUIException Thrown if a problem occurs when generating the response
     */
    public void emptyRequest(HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "emptyRequest", response);

        try {
            response.getWriter().println(_html);
        } catch (Exception e) {
            throw new WebUIException(e);
        }

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
    public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "handle", response);

        if (request.getParameterMap().isEmpty())
            emptyRequest(response);

        else

        if (request.getParameter("done") != null) {
            _done = true;
            try {
                response.getWriter().println("<html><head><meta http-equiv='REFRESH' content='1;url=/'></head><body></body></html>");
            }
            catch (IOException e) {
                throw new WebUIException(e);
            }
        }

        console.exiting(getClass().getName(), "handle");
    }
}