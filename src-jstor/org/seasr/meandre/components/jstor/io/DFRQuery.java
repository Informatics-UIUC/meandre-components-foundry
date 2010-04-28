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

package org.seasr.meandre.components.jstor.io;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.seasr.meandre.support.generic.io.IOUtils;
import org.seasr.meandre.support.generic.jstor.DFRNamespaceContext;
import org.w3c.dom.Document;

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
        tags = "jstor, dfr, data for research",
        rights = Licenses.UofINCSA,
        mode = Mode.compute,
        firingPolicy = FiringPolicy.all,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class DFRQuery extends AbstractExecutableComponent {

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_XML,
            description = "The JSTOR DFR XML response" +
                "<br>TYPE: java.lang.String"
    )
    protected static final String OUT_RESPONSE_XML = Names.PORT_XML;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            defaultValue = "",
            description = "The query",
            name = Names.PROP_QUERY
    )
    protected static final String PROP_QUERY = Names.PROP_QUERY;

    @ComponentProperty(
            defaultValue = "100",
            description = "Maximum number of records to retrieve (set to -1 to retrieve all records)",
            name = Names.PROP_MAX_SIZE
    )
    protected static final String PROP_MAX_RECORDS = Names.PROP_MAX_SIZE;

    //--------------------------------------------------------------------------------------------

    protected static final String JSTOR_DFR_QUERY_URL = "http://dfr.jstor.org/sru/?operation=searchRetrieve&version=1.1&query=";

    private String _query;
    private int _maxRecords;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _query = URLEncoder.encode(ccp.getProperty(PROP_QUERY).trim(), "UTF-8");

        if (_query.length() == 0)
            throw new ComponentContextException(
                    String.format("The query string cannot be empty - please set the '%s' property to a valid query!", PROP_QUERY));

        _maxRecords = Integer.parseInt(ccp.getProperty(PROP_MAX_RECORDS));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String sQuery = String.format("%s%s&maximumRecords=", JSTOR_DFR_QUERY_URL, _query);

        URL queryURL = new URL(sQuery + "1");
        Document doc = DOMUtils.createDocument(queryURL.openStream());

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new DFRNamespaceContext());

        XPathExpression exprNRecords = xpath.compile("/ns1:searchRetrieveResponse/ns1:numberOfRecords/text()");
        int nRecords = ((Double) exprNRecords.evaluate(doc, XPathConstants.NUMBER)).intValue();

        console.fine("Available records: " + nRecords);

        if (nRecords > 0) {
            if (_maxRecords == -1)
                _maxRecords = nRecords;

            queryURL = new URL(sQuery + Math.min(nRecords, _maxRecords));

            console.fine("Query URL set to: " + queryURL);

            String xml = IOUtils.getTextFromReader(new InputStreamReader(queryURL.openStream()));

            console.finest(xml);

            cc.pushDataComponentToOutput(OUT_RESPONSE_XML, xml);
        } else
            cc.pushDataComponentToOutput(OUT_ERROR, "Your query did not return any results");
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {

    }
}