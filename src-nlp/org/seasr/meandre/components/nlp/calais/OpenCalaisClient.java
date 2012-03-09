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

package org.seasr.meandre.components.nlp.calais;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * A wrapper for the OpenCalais service: http://www.opencalais.com
 *
 * Note: Current limits for OpenCalais allow only documents of 100,000 characters or less to be submitted,
 * otherwise a "Request Entity Too Large" error will occur.
 *
 * @author Boris Capitanu
 */

@Component(
        name = "OpenCalais Client",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#ANALYICS, nlp, text, opencalais",
        description = "This component processes text through the OpenCalais service. " +
        		"Note: Current limits for OpenCalais allow only documents of 100,000 characters or less to be processed. ",
        dependency = {
                "protobuf-java-2.2.0.jar"
        }
)
public class OpenCalaisClient extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The text to be processed" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The response. The format of the response depends on the output_format property." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_RESPONSE = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "api_key",
            description = "The API key to use for OpenCalais. An API key can be requested here: " +
            		"<a href='http://www.opencalais.com/GetStarted'>http://www.opencalais.com/GetStarted</a>",
            defaultValue = ""
    )
    protected static final String PROP_API_KEY = "api_key";

    @ComponentProperty(
            name = "content_type",
            description = "Format of the input content, as described here: " +
                    "<a href='http://www.opencalais.com/documentation/calais-web-service-api/forming-api-calls/input-content'>http://www.opencalais.com/documentation/calais-web-service-api/forming-api-calls/input-content</a> " +
                    "<br>Possible values: <ul><li>text/xml</li><li>text/html</li><li>text/htmlraw</li><li>text/raw</li></ul>",
            defaultValue = "text/raw"
    )
    protected static final String PROP_CONTENT_TYPE = "content_type";

    @ComponentProperty(
            name = "charset",
            description = "Encoding of the input content.",
            defaultValue = "UTF-8"
    )
    protected static final String PROP_CHARSET = "charset";

    @ComponentProperty(
            name = "output_format",
            description = "Format of the returned results, as described here: " +
            		"<a href='http://www.opencalais.com/documentation/calais-web-service-api/forming-api-calls/input-parameters'>http://www.opencalais.com/documentation/calais-web-service-api/forming-api-calls/input-parameters</a> " +
            		"<br>Possible values: <ul><li>xml/rdf</li><li>text/n3</li><li>application/json</li><li>text/simple</li><li>text/microformats</li></ul>",
            defaultValue = "application/json"
    )
    protected static final String PROP_OUTPUT_FORMAT = "output_format";

    @ComponentProperty(
            name = "reltag_baseurl",
            description = "Base URL to be put in rel-tag microformats.",
            defaultValue = ""
    )
    protected static final String PROP_RELTAG_BASEURL = "reltag_baseurl";

    @ComponentProperty(
            name = "calculate_relevance_score",
            description = "Indicates whether the extracted metadata should include relevance score for each unique entity.",
            defaultValue = "true"
    )
    protected static final String PROP_CALC_RELEVANCE_SCORE = "calculate_relevance_score";

    @ComponentProperty(
            name = "include_metadata_types",
            description = "The comma-separated metadata type(s) to include in the output. " +
            		"<br>Possible values: <ul><li>GenericRelations</li><li>SocialTags</li></ul>",
            defaultValue = ""
    )
    protected static final String PROP_METADATA_TYPES = "include_metadata_types";

    @ComponentProperty(
            name = "doc_rdf_accessible",
            description = "Indicates whether the entire XML/RDF document should be saved in the Calais Linked Data repository.",
            defaultValue = ""
    )
    protected static final String PROP_RDF_ACCESSIBLE = "doc_rdf_accessible";

    @ComponentProperty(
            name = "allow_distribution",
            description = "Indicates whether the extracted metadata can be distributed.",
            defaultValue = "false"
    )
    protected static final String PROP_ALLOW_DISTRIBUTION = "allow_distribution";

    @ComponentProperty(
            name = "allow_search",
            description = "Indicates whether future searches can be performed on the extracted metadata.",
            defaultValue = "false"
    )
    protected static final String PROP_ALLOW_SEARCH = "allow_search";

    @ComponentProperty(
            name = "external_id",
            description = "User-generated ID for the submission.",
            defaultValue = ""
    )
    protected static final String PROP_EXTERNAL_ID = "external_id";

    @ComponentProperty(
            name = "submitter",
            description = "Identifier for the content submitter.",
            defaultValue = ""
    )
    protected static final String PROP_SUBMITTER = "submitter";

    @ComponentProperty(
            name = "omit_output_original_text",
            description = "Should the original text be omitted from the results?",
            defaultValue = "true"
    )
    protected static final String PROP_OMIT_ORIGINAL = "omit_output_original_text";

    //--------------------------------------------------------------------------------------------


    protected static final String CALAIS_URL = "http://api.opencalais.com/tag/rs/enrich";

    protected Map<String,String> _headers;
    protected String _charset;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        String apiKey = getPropertyOrDieTrying(PROP_API_KEY, ccp);
        _charset = getPropertyOrDieTrying(PROP_CHARSET, ccp);

        _headers = new HashMap<String, String>();
        _headers.put("x-calais-licenseID", apiKey);
        _headers.put("Content-Type", getPropertyOrDieTrying(PROP_CONTENT_TYPE, ccp) + "; charset=" + _charset);
        _headers.put("Accept", getPropertyOrDieTrying(PROP_OUTPUT_FORMAT, ccp));

        String reltagBaseUrl = getPropertyOrDieTrying(PROP_RELTAG_BASEURL, true, false, ccp);
        if (reltagBaseUrl.length() > 0) _headers.put("reltagBaseURL", reltagBaseUrl);

        String calculateRelevanceScore = Boolean.toString(Boolean.parseBoolean(getPropertyOrDieTrying(PROP_CALC_RELEVANCE_SCORE, ccp)));
        _headers.put("calculateRelevanceScore", calculateRelevanceScore);

        String metadataType = getPropertyOrDieTrying(PROP_METADATA_TYPES, true, false, ccp);
        if (metadataType.length() > 0) _headers.put("enableMetadataType", metadataType);

        String rdfAccessible = getPropertyOrDieTrying(PROP_RDF_ACCESSIBLE, true, false, ccp);
        if (rdfAccessible.length() > 0) _headers.put("docRDFaccessible", Boolean.toString(Boolean.parseBoolean(rdfAccessible)));

        String allowDistribution = Boolean.toString(Boolean.parseBoolean(getPropertyOrDieTrying(PROP_ALLOW_DISTRIBUTION, ccp)));
        _headers.put("allowDistribution", allowDistribution);

        String allowSearch = Boolean.toString(Boolean.parseBoolean(getPropertyOrDieTrying(PROP_ALLOW_SEARCH, ccp)));
        _headers.put("allowDistribution", allowSearch);

        String externalId = getPropertyOrDieTrying(PROP_EXTERNAL_ID, true, false, ccp);
        if (externalId.length() > 0) _headers.put("externalID", externalId);

        String submitter = getPropertyOrDieTrying(PROP_SUBMITTER, true, false, ccp);
        if (submitter.length() > 0) _headers.put("submitter", submitter);

        String omitOriginal = Boolean.toString(Boolean.parseBoolean(getPropertyOrDieTrying(PROP_OMIT_ORIGINAL, ccp)));
        _headers.put("omitOutputtingOriginalText", omitOriginal);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];

        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter("http.useragent", "SEASR " + getClass().getSimpleName());
        try {
            HttpPost post = new HttpPost(CALAIS_URL);
            post.setEntity(new StringEntity(text, _charset));
            for (Entry<String, String> entry : _headers.entrySet())
                post.setHeader(entry.getKey(), entry.getValue());

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = httpClient.execute(post, responseHandler);

            cc.pushDataComponentToOutput(OUT_RESPONSE, BasicDataTypesTools.stringToStrings(response));
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _headers = null;
    }
}
