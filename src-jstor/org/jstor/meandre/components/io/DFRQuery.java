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

package org.jstor.meandre.components.io;

import java.io.InputStreamReader;
import java.net.URL;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.io.IOUtils;

/**
 * Performs a query against the JSTOR 'Data For Research' data
 * API available at: http://dfr.jstor.org/api
 *
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This component performs a query against the JSTOR 'Data For Research' data",
        name = "JSTOR DFR Query",
        tags = "jstor, dfr, research, data for research",
        rights = Licenses.UofINCSA,
        mode = Mode.compute,
        firingPolicy = FiringPolicy.all,
        baseURL = "meandre://seasr.org/components/jstor/",
        dependency = {"protobuf-java-2.0.3.jar"}
)
public class DFRQuery extends AbstractExecutableComponent {

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The JSTOR DFR XML response",
            name = Names.PORT_XML
    )
    protected static final String OUT_RESPONSE_XML = Names.PORT_XML;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            defaultValue = "",
            description = "The query",
            name = Names.PROP_QUERY
    )
    protected static final String PROP_QUERY = Names.PROP_QUERY;

    //--------------------------------------------------------------------------------------------

    protected static final String JSTOR_DFR_QUERY_URL = "http://dfr.jstor.org/sru/?operation=searchRetrieve&query=";

    private String _query;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _query = ccp.getProperty(PROP_QUERY).trim();

        if (_query.length() == 0)
            throw new ComponentContextException(
                    String.format("The query string cannot be empty - please set the '%s' property to a valid query!", PROP_QUERY));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {

        String xml = IOUtils.getTextFromReader(new InputStreamReader(new URL(JSTOR_DFR_QUERY_URL + _query).openStream()));
        console.finest(xml);
        cc.pushDataComponentToOutput(OUT_RESPONSE_XML, xml);

    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {

    }
}