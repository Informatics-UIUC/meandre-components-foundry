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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.meandre.webui.WebUIException;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Bytes;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
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
        tags = "input, data, file, text, url",
        mode = Mode.webui,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar", "org.seasr.meandre.components.vis.gwt.inputdata.InputData.jar"},
        resources = {"InputData.vm"}
)
public class InputData extends AbstractGWTWebUIComponent {

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_RAW_DATA,
            description = "The data" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes"
    )
    protected static final String OUT_DATA = Names.PORT_RAW_DATA;

    @ComponentOutput(
            name = "label",
            description = "The label" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_LABEL = "label";

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "Max number of URLs allowed",
            name = "max_url_count",
            defaultValue = "5"
    )
    protected static final String PROP_MAX_URL_COUNT = "max_url_count";

    @ComponentProperty(
            description = "Max number of files allowed",
            name = "max_file_count",
            defaultValue = "5"
    )
    protected static final String PROP_MAX_FILE_COUNT = "max_file_count";

    @ComponentProperty(
            description = "Max text length in characters (0 = unlimited)",
            name = "max_text_length",
            defaultValue = "0"
    )
    protected static final String PROP_MAX_TEXT_LENGTH = "max_text_length";

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;


    //--------------------------------------------------------------------------------------------


    protected static final String TEMPLATE = "org/seasr/meandre/components/tools/text/io/InputData.vm";

    private String _html;
    private boolean _done;
    private boolean _wrapAsStream;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _wrapAsStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, true, true, ccp));
        int propMaxUrlCount = Integer.parseInt(getPropertyOrDieTrying(PROP_MAX_URL_COUNT, true, true, ccp));
        int propMaxFileCount = Integer.parseInt(getPropertyOrDieTrying(PROP_MAX_FILE_COUNT, true, true, ccp));
        int propMaxTextLength = Integer.parseInt(getPropertyOrDieTrying(PROP_MAX_TEXT_LENGTH, true, true, ccp));

        _context.put("maxUrlCount", propMaxUrlCount);
        _context.put("maxFileCount", propMaxFileCount);
        _context.put("maxTextLength", propMaxTextLength);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {

        VelocityTemplateService velocity = VelocityTemplateService.getInstance();

        console.finest("Applying the Velocity template");
        _html = velocity.generateOutput(_context, TEMPLATE);

        _done = false;

        cc.startWebUIFragment(this);

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

    @SuppressWarnings("unchecked")
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "handle", response);

        console.finer("Request method:\t" + request.getMethod());
        console.finer("Request content-type:\t" + request.getContentType());
        console.finer("Request path:\t" + request.getPathInfo());
        console.finer("Request query string:\t" + request.getQueryString());

        response.setStatus(HttpServletResponse.SC_OK);

        String action = request.getParameter("action");
        console.fine("Action: " + action);

        if (action != null) {
            StreamInitiator si = new StreamInitiator();
            StreamTerminator st = new StreamTerminator();

            if (action.equals("urls")) {
                SortedMap<String, URL> urls = new TreeMap<String, URL>();
                Enumeration<String> paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = paramNames.nextElement();
                    if (paramName.startsWith("url_")) {
                        String sUrl = request.getParameter(paramName);
                        console.fine(paramName + ": " + sUrl);
                        try {
                            urls.put(paramName, new URL(sUrl));
                        }
                        catch (MalformedURLException e) {
                            console.warning(sUrl + " is not a valid URL, ignoring it.");
                            continue;
                        }
                    }
                }

                if (urls.size() == 0)
                    throw new WebUIException("No URLs provided");

                try {
                    if (_wrapAsStream) {
                        componentContext.pushDataComponentToOutput(OUT_LABEL, si);
                        componentContext.pushDataComponentToOutput(OUT_DATA, si);
                    }

                    for (URL url : urls.values()) {
                        componentContext.pushDataComponentToOutput(OUT_LABEL, BasicDataTypesTools.stringToStrings(url.toString()));
                        componentContext.pushDataComponentToOutput(OUT_DATA, url);
                    }

                    if (_wrapAsStream) {
                        componentContext.pushDataComponentToOutput(OUT_LABEL, st);
                        componentContext.pushDataComponentToOutput(OUT_DATA, st);
                    }
                }
                catch (ComponentContextException e) {
                    throw new WebUIException(e);
                }
            }

            else

            if (action.equals("text")) {
                String text = request.getParameter("text");
                if (text == null || text.length() == 0)
                    throw new WebUIException("No text provided");

                try {
                    if (_wrapAsStream) {
                        componentContext.pushDataComponentToOutput(OUT_LABEL, si);
                        componentContext.pushDataComponentToOutput(OUT_DATA, si);
                    }

                    componentContext.pushDataComponentToOutput(OUT_LABEL, BasicDataTypesTools.stringToStrings("text_input"));
                    componentContext.pushDataComponentToOutput(OUT_DATA, text);

                    if (_wrapAsStream) {
                        componentContext.pushDataComponentToOutput(OUT_LABEL, st);
                        componentContext.pushDataComponentToOutput(OUT_DATA, st);
                    }
                }
                catch (ComponentContextException e) {
                    throw new WebUIException(e);
                }
            }

            else

            if (action.equals("upload")) {
                if (!ServletFileUpload.isMultipartContent(request))
                    throw new WebUIException("File upload request needs to be done using a multipart content type");

                ServletFileUpload fileUpload = new ServletFileUpload(new DiskFileItemFactory());
                List<FileItem> uploadedFiles;
                try {
                    uploadedFiles = fileUpload.parseRequest(request);
                }
                catch (FileUploadException e) {
                    throw new WebUIException(e);
                }

                try {
                    if (_wrapAsStream) {
                        componentContext.pushDataComponentToOutput(OUT_LABEL, si);
                        componentContext.pushDataComponentToOutput(OUT_DATA, si);
                    }

                    for (FileItem file : uploadedFiles) {
                        if (file == null || !file.getFieldName().startsWith("file_"))
                            continue;

                        console.fine("isFormField:\t" + file.isFormField());
                        console.fine("fieldName:\t" + file.getFieldName());
                        console.fine("name:\t" + file.getName());
                        console.fine("contentType:\t" + file.getContentType());
                        console.fine("size:\t" + file.getSize());

                        if (file.isFormField())
                            continue;

                        Strings label = BasicDataTypesTools.stringToStrings(file.getName());
                        Bytes data = BasicDataTypesTools.byteArrayToBytes(file.get());

                        componentContext.pushDataComponentToOutput(OUT_LABEL, label);
                        componentContext.pushDataComponentToOutput(OUT_DATA, data);
                    }

                    if (_wrapAsStream) {
                        componentContext.pushDataComponentToOutput(OUT_LABEL, st);
                        componentContext.pushDataComponentToOutput(OUT_DATA, st);
                    }
                }
                catch (ComponentContextException e) {
                    throw new WebUIException(e);
                }
            }

            else
                throw new WebUIException("Unknown action: " + action);

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
        return "/";
    }

    @Override
    public String getGWTModuleJARName() {
        return "org.seasr.meandre.components.vis.gwt.inputdata.InputData.jar";
    }

    //--------------------------------------------------------------------------------------------
}
