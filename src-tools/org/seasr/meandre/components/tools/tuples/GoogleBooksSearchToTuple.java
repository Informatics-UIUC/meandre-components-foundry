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

package org.seasr.meandre.components.tools.tuples;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.components.tuples.TupleUtilities;
import org.seasr.meandre.support.generic.io.HttpUtils;

/**
 * This component uses the Google AJAX/REST API for doing searches (web only)
 *
 * @author Mike Haberman
 * @author Devin Griffiths
 *
 */

@Component(
		name = "Google Books Search To Tuple",
		creator = "Devin Griffiths",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple, google, books, search, text",
		description = "This component, based on Mike Haberman's Google Search to Tuple, performs a Google Books "+
					  "search by accessing the Google Books search API."+
		              "The search term is an input and the search results are output as tuples,"+
		              "With the following fields: url, title, authors, bookId, publishedYear, tbUrl,"+
		              "tbHeight, tbWidth, pageCount.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class GoogleBooksSearchToTuple extends AbstractExecutableComponent {

	//------------------------------ INPUTS -----------------------------------------------------

	@ComponentInput(
			name = "query",
			description = "Search query"
	)
	protected static final String IN_QUERY = "query";

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "Search results represented as tuples" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuple (url, title, content)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
			name = "count",
			description = "Number of search results to output",
		    defaultValue = "5"
	)
	protected static final String PROP_COUNT = "count";

	/*@ComponentProperty(
	*		name = "filter",
	*		description = "Google Books API Search Filter",
	*	    defaultValue = "full"
	*)
	*protected static final String PROP_FILTER = "filter";
	*/
	//--------------------------------------------------------------------------------------------


	SimpleTuplePeer tuplePeer;
    Map<String,String> columnMap = new HashMap<String,String>();
    int count;
    //String filter;
    String queryPlus;
    int start = 0; // offset for page queries


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		count = Integer.parseInt(ccp.getProperty(PROP_COUNT).trim());
		//filter = String.valueOf(ccp.getProperty(PROP_FILTER).trim());
	    String fields[] = {"url", "title", "authors", "bookId", "publishedYear",
	    		"tbUrl", "tbHeight", "tbWidth", "pageCount"};
		tuplePeer = new SimpleTuplePeer(fields);

		columnMap.put("titleNoFormatting", "title");
	}

	protected String buildURL(String query) {
		StringBuilder location = new StringBuilder();
	    location.append("https://ajax.googleapis.com/ajax/services/search/books?v=1.0");
	    String queryPlus = query.replaceAll(" ", "+");
		location.append("&q=").append(queryPlus);
	    /*location.append("&filter=").append(filter);
	    * location.append("&start=").append(start); // TODO, add a paging property ?
	    *location.append("&rsz=large");            // small == 4 results, large == 8 results
	    *location.append("&q=").append(query);
		*/

	    String url = location.toString();
	    start += 8; // 8 is what large

	    return url;
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		String[] input = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_QUERY));
		String query = input[0];
		query = URLEncoder.encode(query, "UTF-8");

		List<Strings> output = new ArrayList<Strings>();

		while (output.size() < count) {
			String url = buildURL(query);
			console.fine("Reading location: " + url);
			String json = HttpUtils.doGET(url, null);

			List<SimpleTuple> tuples = jsonToTuples(json, tuplePeer, columnMap);

			for (SimpleTuple tuple : tuples) {
				output.add(tuple.convert());

				if (output.size() == count)
					break;
			}
		}

		Strings[] results = new Strings[output.size()];
		output.toArray(results);
		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	    //
		// metaData for this tuple producer
		//
	    cc.pushDataComponentToOutput(OUT_META_TUPLE, tuplePeer.convert());

	    //
	    // TODO: push this functionality to the abstract
	    // to reset variables for next execution (if in streaming mode) ?
	    //
	    start = 0;
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    public List<SimpleTuple> jsonToTuples(String jsonData,
    		                              SimpleTuplePeer peer,
    		                              Map<String,String> columnMap)
    throws Exception {
    	JSONObject json   = new JSONObject(jsonData);
    	JSONObject res    = json.getJSONObject("responseData");
    	JSONArray results = res.getJSONArray("results");
    	ArrayList<SimpleTuple> out = new ArrayList<SimpleTuple>();

    	int urlIdx     = TupleUtilities.getFieldIndexFromName(peer, "url",               	columnMap);
    	int titleIdx   = TupleUtilities.getFieldIndexFromName(peer, "titleNoFormatting", 	columnMap);
    	int authorsIdx   = TupleUtilities.getFieldIndexFromName(peer, "authors",         	columnMap);
    	int bookIdIdx = TupleUtilities.getFieldIndexFromName(peer, "bookId",           	 	columnMap);
    	int publishedYearIdx = TupleUtilities.getFieldIndexFromName(peer, "publishedYear",  columnMap);
    	int tbUrlIdx = TupleUtilities.getFieldIndexFromName(peer, "tbUrl",           	 	columnMap);
    	int tbHeightIdx = TupleUtilities.getFieldIndexFromName(peer, "tbHeight",            columnMap);
    	int tbWidthIdx = TupleUtilities.getFieldIndexFromName(peer, "tbWidth",           	columnMap);
    	int pageCountIdx = TupleUtilities.getFieldIndexFromName(peer, "pageCount",          columnMap);




    	int size = results.length();

		for (int i = 0; i < size;i++) {

			SimpleTuple tuple = peer.createTuple();
			JSONObject fields = results.getJSONObject(i);

			String url      = fields.getString("url");
            String title    = fields.getString("titleNoFormatting");
            String authors  = fields.getString("authors");
            String bookId  = fields.getString("bookId");
            String publishedYear  = fields.getString("publishedYear");
            String tbUrl  = fields.getString("tbUrl");
            String tbHeight  = fields.getString("tbHeight");
            String tbWidth  = fields.getString("tbWidth");
            String pageCount  = fields.getString("pageCount");

            tuple.setValue(urlIdx,     url);
            tuple.setValue(titleIdx,   title);
	        tuple.setValue(authorsIdx, authors);
	        tuple.setValue(bookIdIdx, bookId);
	        tuple.setValue(publishedYearIdx, publishedYear);
	        tuple.setValue(tbUrlIdx, tbUrl);
	        tuple.setValue(tbHeightIdx, tbHeight);
	        tuple.setValue(tbWidthIdx, tbWidth);
	        tuple.setValue(pageCountIdx, pageCount);

            out.add(tuple);
		}

    	return out;
    }

    //--------------------------------------------------------------------------------------------

    @Override
	public void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_QUERY })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_QUERY));
        componentContext.pushDataComponentToOutput(OUT_TUPLES, componentContext.getDataComponentFromInput(IN_QUERY));
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_QUERY })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_QUERY));
        componentContext.pushDataComponentToOutput(OUT_TUPLES, componentContext.getDataComponentFromInput(IN_QUERY));
    }
}
