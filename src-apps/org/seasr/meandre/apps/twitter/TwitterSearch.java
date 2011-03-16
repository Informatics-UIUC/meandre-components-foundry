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

package org.seasr.meandre.apps.twitter;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.io.IOUtils;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Twitter Search",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "twitter, search",
        description = "This component searches Twitter for stuff" ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TwitterSearch extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The query string" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The search results" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //--------------------------------------------------------------------------------------------


    protected static final String TWITTER_SEARCH_API_URL = "http://search.twitter.com/search.json";
    private String _nextPage;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String query = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];

        SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[] { "from_user", "from_user_id_str", "profile_image_url", "created_at", "text" });
        StringsArray.Builder tweetsBuilder = StringsArray.newBuilder();

        _nextPage = String.format("?q=%s&since_id=0", URLEncoder.encode(query, "UTF-8"));
        while (_nextPage != null) {
            URL twitterSearchUrl = new URL(String.format("%s%s", TWITTER_SEARCH_API_URL, _nextPage));
            console.fine("Querying: " + twitterSearchUrl);
            String response = IOUtils.getTextFromReader(new InputStreamReader(twitterSearchUrl.openStream()));

            JSONObject joResponse = new JSONObject(response);
            _nextPage = (joResponse.has("next_page")) ? joResponse.getString("next_page") : null;

            JSONArray jaResults = joResponse.getJSONArray("results");

            for (int i = 0, iMax = jaResults.length(); i < iMax; i++) {
                JSONObject joTweet = jaResults.getJSONObject(i);

                SimpleTuple outTuple = outPeer.createTuple();
                outTuple.setValue("from_user", joTweet.getString("from_user"));
                outTuple.setValue("from_user_id_str", joTweet.getString("from_user_id_str"));
                outTuple.setValue("profile_image_url", joTweet.getString("profile_image_url"));
                outTuple.setValue("created_at", joTweet.getString("created_at"));
                outTuple.setValue("text", StringEscapeUtils.unescapeHtml(joTweet.getString("text")));

                tweetsBuilder.addValue(outTuple.convert());
            }
        }

        cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
        cc.pushDataComponentToOutput(OUT_TUPLES, tweetsBuilder.build());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_TEXT })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        Object si = componentContext.getDataComponentFromInput(IN_TEXT);
        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, si);
        componentContext.pushDataComponentToOutput(OUT_TUPLES, si);
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_TEXT })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        Object st = componentContext.getDataComponentFromInput(IN_TEXT);
        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, st);
        componentContext.pushDataComponentToOutput(OUT_TUPLES, st);
    }
}
