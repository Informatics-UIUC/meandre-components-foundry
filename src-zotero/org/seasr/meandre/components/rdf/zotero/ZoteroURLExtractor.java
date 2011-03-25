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

package org.seasr.meandre.components.rdf.zotero;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.generic.io.ModelUtils;
import org.seasr.meandre.support.generic.zotero.ZoteroUtils;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * This class extracts the list of urls per entry from a Zotero RDF
 * For each Zotero item, we have an output for the url, and title. We output
 * a message on the "PORT_NO_DATA" if there were no urls extracted.
 *
 * @author Xavier Llor&agrave;
 * @author Loretta Auvil
 * @author Boris Capitanu
 */

@Component(
		creator = "Xavier Llora",
		description = "Extract the urls for each of the entry of a Zotero RDF",
		name = "Zotero URL Extractor",
		tags = "zotero, url",
		rights = Licenses.UofINCSA,
		mode = Mode.compute,
		firingPolicy = FiringPolicy.all,
		baseURL = "meandre://seasr.org/components/foundry/",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class ZoteroURLExtractor extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        name = Names.PORT_REQUEST_DATA,
			description = "A mapping between request parameters and their values" +
    			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap" +
    			"<br>TYPE: java.util.Map<java.lang.String, java.lang.String[]>"
	)
	protected static final String IN_REQUEST = Names.PORT_REQUEST_DATA;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        name = Names.PORT_LOCATION,
			description = "Item location" +
			    "<br>TYPE: java.lang.String"
	)
	protected static final String OUT_ITEM_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
	        name = Names.PORT_TEXT,
			description = "Item title" +
                "<br>TYPE: java.lang.String"
	)
	protected static final String OUT_ITEM_TITLE = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

    //--------------------------------------------------------------------------------------------


    private boolean bWrapped;
    private int itemCount;


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

	    bWrapped = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		Map<String, String[]> map = DataTypeParser.parseAsStringStringArrayMap(cc.getDataComponentFromInput(IN_REQUEST));
        String[] zoteroRdfs = map.get("zoterordf");

        if (bWrapped) pushInitiator();

		if (zoteroRdfs != null) {
		    itemCount = 0;

		    for (String rdf : zoteroRdfs) {
		        int count = 0;
		        Map<String, String> mapURLs;

		        try {
		            Model model = ModelUtils.getModel(rdf, "meandre://specialUri");
		            mapURLs = ZoteroUtils.extractURLs(model);
		            count = mapURLs.size();
		            itemCount += count;
		        }
		        catch (Exception e) {
		            outputError("Error in data format", e, Level.WARNING);
		            break;
		        }

		        if (count == 0)
		            continue;

		        for (Entry<String, String> item : mapURLs.entrySet()) {
		            String sURI = item.getKey().replaceAll(" ", "%20");
		            String sTitle = item.getValue();
		            console.fine("[ uri = '" + sURI + "' title = '" + sTitle + "' ]");

		            cc.pushDataComponentToOutput(OUT_ITEM_LOCATION, sURI);
		            cc.pushDataComponentToOutput(OUT_ITEM_TITLE, sTitle);
		        }
		    }

		    console.fine("Pushed out a total of " + itemCount + " URLs extracted from the Zotero input");

		    if (itemCount == 0)
	            outputError("Your items contained no URL information. Check to see that the URL attribute contains a valid url.", Level.WARNING);
		} else
		    outputError("Cannot find Zotero request information in the input data", Level.WARNING);

		if (bWrapped) pushTerminator();
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }

    //--------------------------------------------------------------------------------------------

    /**
     * Pushes an initiator.
     *
     * @throws Exception Something went wrong when pushing
     */
    private void pushInitiator() throws Exception {
        StreamInitiator si = new StreamInitiator(streamId);
        componentContext.pushDataComponentToOutput(OUT_ITEM_LOCATION, si);
        componentContext.pushDataComponentToOutput(OUT_ITEM_TITLE, si);
    }

    /**
     * Pushes a terminator.
     *
     * @throws Exception Something went wrong when pushing
     */
    private void pushTerminator() throws Exception {
        StreamTerminator st = new StreamTerminator(streamId);
        componentContext.pushDataComponentToOutput(OUT_ITEM_LOCATION, st);
        componentContext.pushDataComponentToOutput(OUT_ITEM_TITLE, st);
    }
}
