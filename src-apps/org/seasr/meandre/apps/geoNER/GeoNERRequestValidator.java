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

package org.seasr.meandre.apps.geoNER;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.seasr.meandre.components.tools.webservice.RequestValidator;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Validates requests to the geoNER service. If the request validates, the original " +
        		"request_handler and response_handler are pushed on the output ports. If the request does " +
        		"NOT validate (i.e. an error was detected), ONLY the response_handler_error is pushed on the output port.",
        name = "geoNER Request Validator",
        tags = "webservice, geoner, validator",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar", "gdata-core-1.0.jar"}
)
public class GeoNERRequestValidator extends RequestValidator {

    @Override
    protected boolean validate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!super.validate(request, response))
            return false;

        // Perform additional app-specific validations
        String algId = request.getHeader("x-geoNER-alg");
        boolean validAlg = false;
        try {
            int alg = Integer.parseInt(algId);
            if (alg > 0 && alg < 4)
                validAlg = true;
        }
        catch (NumberFormatException e) { }

        if (!validAlg) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            componentContext.pushDataComponentToOutput(OUT_ERROR, String.format("BAD_REQUEST: Invalid NER algorithm specified: %s.", algId));
            componentContext.pushDataComponentToOutput(OUT_RESPONSE_ERROR, response);
            return false;
        }

        return true;
    }
}
