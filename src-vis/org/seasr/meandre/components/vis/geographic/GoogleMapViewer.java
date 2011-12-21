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

package org.seasr.meandre.components.vis.geographic;

import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.json.JSONArray;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

@Component(
        creator="Boris Capitanu",
        description="Generates a web page containing google map " +
        		"marked with locations from the original XML document.",
        name="Google Map Viewer",
        tags="google map, visualization",
        mode=Mode.webui,
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/",
        resources = {"GoogleMapViewer.vm"}
)
public class GoogleMapViewer extends AbstractExecutableComponent {

	//-------------------------- INPUTS --------------------------

	@ComponentInput(
	        name = Names.PORT_LATITUDE_VECTOR,
	        description = "Read vector of latitude." +
	            "<br>TYPE: java.util.Vector<java.lang.String>"
	)
    public final static String IN_LATITUDE = Names.PORT_LATITUDE_VECTOR;

	@ComponentInput(
	        name = Names.PORT_LONGITUDE_VECTOR,
	        description = "Read vector of longitude." +
         	    "<br>TYPE: java.util.Vector<java.lang.String>"
	)
    public final static String IN_LONGITUDE = Names.PORT_LONGITUDE_VECTOR;

	@ComponentInput(
	        name = Names.PORT_LOCATION_VECTOR,
	        description = "Read vector of location." +
         	    "<br>TYPE: java.util.Vector<java.lang.String>"
	)
    public final static String IN_LOCATION = Names.PORT_LOCATION_VECTOR;

	@ComponentInput(
	        name = Names.PORT_CONTEXT_VECTOR,
	        description = "Read vector of context." +
         	    "<br>TYPE: java.util.Vector<java.lang.String>"
	)
    public final static String IN_CONTEXT = Names.PORT_CONTEXT_VECTOR;

	//-------------------------- OUTPUTS --------------------------

	@ComponentOutput(
	        name = Names.PORT_HTML,
	        description = "The HTML for the Google Map viewer" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_HTML = Names.PORT_HTML;

	//-------------------------- PROPERTIES --------------------------

	@ComponentProperty(
	        defaultValue = "",
            description = "This property sets the Google Maps API key of your web site.",
            name = Names.PROP_GOOGLE_APIS_KEY
	)
    protected static final String PROP_GOOGLE_KEY = Names.PROP_GOOGLE_APIS_KEY;

   @ComponentProperty(
            defaultValue = "SATELLITE",
            description = "This property controls the type of map displayed by default. " +
            		"Valid values are:<br><ul>" +
            		"<li>ROADMAP - displays the normal, default 2D tiles of Google Maps.</li>" +
            		"<li>SATELLITE displays photographic tiles.</li>" +
            		"<li>HYBRID displays a mix of photographic tiles and a tile layer for prominent features (roads, city names).</li>" +
            		"<li>TERRAIN displays physical relief tiles for displaying elevation and water features (mountains, rivers, etc.).</li>" +
            		"</ul>",
            name = "map_type"
    )
    protected static final String PROP_MAP_TYPE = "map_type";

    //--------------------------------------------------------------------------------------------


    static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/geographic/GoogleMapViewer.vm";

    private VelocityContext _context;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    	_context = VelocityTemplateService.getInstance().getNewContext();
    	_context.put("key", getPropertyOrDieTrying(PROP_GOOGLE_KEY, ccp));
    	_context.put("map_type", getPropertyOrDieTrying(PROP_MAP_TYPE, ccp));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	Vector<String> latitude, longitude, location, context;

    	latitude  = (Vector<String>)cc.getDataComponentFromInput(IN_LATITUDE);
        longitude = (Vector<String>)cc.getDataComponentFromInput(IN_LONGITUDE);
        location  = (Vector<String>)cc.getDataComponentFromInput(IN_LOCATION);
        context   = (Vector<String>)cc.getDataComponentFromInput(IN_CONTEXT);

        JSONArray latList = new JSONArray(latitude);
        JSONArray lonList = new JSONArray(longitude);
        JSONArray locList = new JSONArray(location);
        JSONArray ctxList = new JSONArray(context);

        _context.put("lat_list", latList.toString());
        _context.put("lon_list", lonList.toString());
        _context.put("loc_list", locList.toString());
        _context.put("ctx_list", ctxList.toString());

        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        String html = velocity.generateOutput(_context, DEFAULT_TEMPLATE);

        cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(html));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
