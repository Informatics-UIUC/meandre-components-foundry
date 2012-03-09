package org.seasr.meandre.components.transform.text;



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

import java.net.URI;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.generic.io.IOUtils;

/**
 * @author Loretta Auvil
 */

@Component(
        creator = "Loretta Auvil",
        description = "Custom VAM component to query and requery until all results are received. And to select JSON attribute of records and return its value.",
        name = "Custom VAM Query",
        tags = "#TRANSFORM, JSON, transformation, select",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class CustomVAMQuery extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------

    @ComponentInput(
            name = "JSON_location",
            description = "The attribute" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_LOCATION = "JSON_location";

    @ComponentInput(
            name = "JSON_data",
            description = "The JSON data" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_JSON = "JSON_data";

    //------------------------------ OUTPUTS ------------------------------

    @ComponentOutput(
            name = "JSON_value",
            description = "text output as JSON" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_JSON = "JSON_value";

    //--------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {

        String json = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_JSON))[0];
        String location = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LOCATION))[0];

        StreamInitiator si = new StreamInitiator(streamId);
        componentContext.pushDataComponentToOutput(OUT_JSON, si);

        int size;
        //Handle via JSONObject
        JSONObject results = new JSONObject(json);
        size = (results.getJSONObject("meta")).getInt("result_count");
        console.info("Total number of objects: "+size);

        int cnt = 0;

        JSONArray recs = results.getJSONArray("records");
//        JSONArray results = new JSONArray(recs);

        //JSONArray recs = results.getJSONArray("records");
        console.info("Chunk size: = "+recs.length());
        cnt += recs.length();
        componentContext.pushDataComponentToOutput(OUT_JSON, recs);

        while (cnt<size) {

           //Get next chunk
           String text = location+"&offset="+cnt;
           URI uri = new URI(text);
           String sRes =  IOUtils.getTextFromReader(IOUtils.getReaderForResource(uri));

            console.info("Reading: " + uri.toString());

            results = new JSONObject(sRes);
            recs = results.getJSONArray("records");
            console.info("Chunk size: = "+recs.length());
            cnt += recs.length();
            componentContext.pushDataComponentToOutput(OUT_JSON, recs);
       }

       StreamTerminator st = new StreamTerminator(streamId);
       componentContext.pushDataComponentToOutput(OUT_JSON, st);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }
}
