/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.seasr.meandre.components.tools.webservice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

import com.google.gdata.util.ContentType;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Validates webservice requests. If the request validates, the original " +
        		"request_handler and response_handler are pushed on the output ports. If the request does " +
        		"NOT validate (i.e. an error was detected), ONLY the response_handler_error is pushed on the output port.",
        name = "Request Validator",
        tags = "webservice, validator",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar", "gdata-core-1.0.jar"}
)
public class RequestValidator extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "request_handler",
            description = "The request object." +
                "<br>TYPE: javax.servlet.http.HttpServletRequest"
    )
    protected static final String IN_REQUEST = "request_handler";

    @ComponentInput(
            name = Names.PORT_RESPONSE_HANDLER,
            description = "The response object." +
                "<br>TYPE: javax.servlet.http.HttpServletResponse"
    )
    protected static final String IN_RESPONSE = Names.PORT_RESPONSE_HANDLER;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "request_handler",
            description = "The request object." +
                "<br>TYPE: javax.servlet.http.HttpServletRequest"
    )
    protected static final String OUT_REQUEST = "request_handler";

    @ComponentOutput(
            name = Names.PORT_RESPONSE_HANDLER,
            description = "The response object." +
                "<br>TYPE: javax.servlet.http.HttpServletResponse"
    )
    protected static final String OUT_RESPONSE = Names.PORT_RESPONSE_HANDLER;

    @ComponentOutput(
            name = "response_handler_error",
            description = "The response object for situations when an error occurred." +
                "<br>TYPE: javax.servlet.http.HttpServletResponse"
    )
    protected static final String OUT_RESPONSE_ERROR = "response_handler_error";

    //------------------------------ PROPERTIES ---------------------------------------------------

    @ComponentProperty (
            description = "The comma-separated list of supported request methods (GET, POST...)",
            name = "supported_request_methods",
            defaultValue = ""
    )
    protected static final String PROP_SUPPORTED_METHODS = "supported_request_methods";

    @ComponentProperty (
            description = "The comma-separated list of supported request content types in descending order of preference " +
                    "(non-empty, and each entry is of the form type/subtype without the wildcard char '*'). " +
                    "An empty value means all request types are supported.",
            name = "supported_request_types",
            defaultValue = ""
    )
    protected static final String PROP_SUPPORTED_REQ_TYPES = "supported_request_types";

    @ComponentProperty (
            description = "The comma-separated list of supported response content types in descending order of preference " +
                    "(non-empty, and each entry is of the form type/subtype without the wildcard char '*'). " +
                    "An empty value means all response types are supported.",
            name = "supported_response_types",
            defaultValue = ""
    )
    protected static final String PROP_SUPPORTED_RESP_TYPES = "supported_response_types";

    @ComponentProperty (
            description = "The comma-separated list of expected headers that each request must have. " +
            		"An empty value means that no headers are expected.",
            name = "expected_headers",
            defaultValue = ""
    )
    protected static final String PROP_EXPECTED_HEADERS = "expected_headers";

    @ComponentProperty (
            description = "The maximum request size allowed (in bytes). Can be empty.",
            name = "max_request_size",
            defaultValue = ""
    )
    protected static final String PROP_MAX_REQ_SIZE = "max_request_size";

    //--------------------------------------------------------------------------------------------


    protected Set<String> _supportedRequestMethods = new HashSet<String>();
    protected List<ContentType> _supportedRequestTypes = new ArrayList<ContentType>();
    protected List<ContentType> _supportedResponseTypes = new ArrayList<ContentType>();
    protected Set<String> _expectedHeaders = new HashSet<String>();
    protected Integer _maxReqSize;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        String[] supportedMethods = getPropertyOrDieTrying(PROP_SUPPORTED_METHODS, ccp).split(",");
        for (String method : supportedMethods)
            _supportedRequestMethods.add(method.trim().toLowerCase());

        String propReqTypes = getPropertyOrDieTrying(PROP_SUPPORTED_REQ_TYPES, true, false, ccp);
        if (propReqTypes.length() > 0)
            for (String reqType : propReqTypes.split(","))
                _supportedRequestTypes.add(new ContentType(reqType.trim()));

        String propRespTypes = getPropertyOrDieTrying(PROP_SUPPORTED_RESP_TYPES, true, false, ccp);
        if (propRespTypes.length() > 0)
            for (String respType : propRespTypes.split(","))
                _supportedResponseTypes.add(new ContentType(respType.trim()));

        String propExpectedHeaders = getPropertyOrDieTrying(PROP_EXPECTED_HEADERS, true, false, ccp);
        if (propExpectedHeaders.length() > 0)
            for (String header : propExpectedHeaders.split(","))
                _expectedHeaders.add(header.trim());

        String propMaxReqSize = getPropertyOrDieTrying(PROP_MAX_REQ_SIZE, true, false, ccp);
        if (propMaxReqSize.length() > 0)
            _maxReqSize = Integer.parseInt(propMaxReqSize);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        HttpServletRequest request = (HttpServletRequest) componentContext.getDataComponentFromInput(IN_REQUEST);
        HttpServletResponse response = (HttpServletResponse) componentContext.getDataComponentFromInput(IN_RESPONSE);

        if (validate(request, response)) {
            // If we got here it means the request satisfies the specified constraints
            componentContext.pushDataComponentToOutput(OUT_REQUEST, request);
            componentContext.pushDataComponentToOutput(OUT_RESPONSE, response);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _supportedRequestMethods.clear();
        _supportedRequestMethods = null;

        _supportedRequestTypes.clear();
        _supportedRequestTypes = null;

        _supportedResponseTypes.clear();
        _supportedResponseTypes = null;

        _expectedHeaders.clear();
        _expectedHeaders = null;
    }

    //--------------------------------------------------------------------------------------------

    protected boolean validate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Check whether the request method is supported
        String reqMethod = request.getMethod();
        if (!_supportedRequestMethods.contains(reqMethod.toLowerCase())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.setContentType("text/plain");
            componentContext.pushDataComponentToOutput(OUT_ERROR, String.format("METHOD_NOT_ALLOWED: %s", reqMethod));
            componentContext.pushDataComponentToOutput(OUT_RESPONSE_ERROR, response);
            return false;
        }

        if (!_supportedRequestTypes.isEmpty()) {
            // Check whether the request Content-Type specified is supported
            boolean match = false;
            String reqMediaType = request.getContentType();

            if (reqMediaType != null) {
                ContentType reqContentType = new ContentType(reqMediaType);
                reqMediaType = reqContentType.getMediaType();
                for (ContentType supportedType : _supportedRequestTypes)
                    if (reqContentType.match(supportedType)) {
                        match = true;
                        break;
                    }
            }

            if (!match) {
                response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
                response.setContentType("text/plain");
                componentContext.pushDataComponentToOutput(OUT_ERROR, String.format("UNSUPPORTED_MEDIA_TYPE: %s", reqMediaType));
                componentContext.pushDataComponentToOutput(OUT_RESPONSE_ERROR, response);
                return false;
            }
        }

        if (!_supportedResponseTypes.isEmpty()) {
            // Check whether the Accept header value specified is supported
            String accept = request.getHeader("Accept");
            ContentType bestContentType = null;

            if (accept != null)
                bestContentType = ContentType.getBestContentType(accept, _supportedResponseTypes);

            if (bestContentType == null) {
                response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                response.setContentType("text/plain");
                componentContext.pushDataComponentToOutput(OUT_ERROR, String.format("NOT_ACCEPTABLE: %s", accept));
                componentContext.pushDataComponentToOutput(OUT_RESPONSE_ERROR, response);
                return false;
            }
        }

        if (!_expectedHeaders.isEmpty()) {
            // Check whether all the required headers are present
            for (String header : _expectedHeaders) {
                String value = request.getHeader(header);
                if (value == null || value.length() == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain");
                    componentContext.pushDataComponentToOutput(OUT_ERROR, String.format("BAD_REQUEST: Missing value for header %s", header));
                    componentContext.pushDataComponentToOutput(OUT_RESPONSE_ERROR, response);
                    return false;
                }
            }
        }

        if (_maxReqSize != null) {
            // Check whether the request is within required limits
            int contentLength = request.getContentLength();
            if (contentLength < 0) {
                response.setStatus(HttpServletResponse.SC_LENGTH_REQUIRED);
                response.setContentType("text/plain");
                componentContext.pushDataComponentToOutput(OUT_ERROR, "LENGTH_REQUIRED: The request cannot be handled without a defined Content-Length");
                componentContext.pushDataComponentToOutput(OUT_RESPONSE_ERROR, response);
                return false;
            }

            if (contentLength > _maxReqSize) {
                response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                response.setContentType("text/plain");
                componentContext.pushDataComponentToOutput(OUT_ERROR, String.format("REQUEST_ENTITY_TOO_LARGE: " +
                        "Request size: %,d bytes. Maximum allowed: %,d bytes.", contentLength, _maxReqSize));
                componentContext.pushDataComponentToOutput(OUT_RESPONSE_ERROR, response);
                return false;
            }
        }

        return true;
    }
}
