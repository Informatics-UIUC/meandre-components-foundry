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

import java.util.ArrayList;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

@Component(
        creator="Lily Dong",
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
	        defaultValue = "ABQIAAAAzuMq2M5--KdBKawoLNQWUxRi_j0U6kJrkFvY4-OX2XYmEAa76BQS61jzrv4ruAIpkFQs5Qp-fiN3hg",
            description = "This property sets the Google Maps API key of your web site.",
            name = Names.PROP_GOOGLE_APIS_KEY
	)
    protected static final String PROP_GOOGLE_KEY = Names.PROP_GOOGLE_APIS_KEY;

    //--------------------------------------------------------------------------------------------


    static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/geographic/GoogleMapViewer.vm";

    private VelocityContext _context;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    	_context = VelocityTemplateService.getInstance().getNewContext();
    	_context.put("key", ccp.getProperty(PROP_GOOGLE_KEY));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	Vector<String> latitude, longitude, location, context;

    	latitude  = (Vector<String>)cc.getDataComponentFromInput(IN_LATITUDE);
        longitude = (Vector<String>)cc.getDataComponentFromInput(IN_LONGITUDE);
        location  = (Vector<String>)cc.getDataComponentFromInput(IN_LOCATION);
        context   = (Vector<String>)cc.getDataComponentFromInput(IN_CONTEXT);

        ArrayList<String> listForLatitude  = new ArrayList<String>();
        ArrayList<String> listForLongitude = new ArrayList<String>();
        ArrayList<String> listForLocation  = new ArrayList<String>();
        ArrayList<String> listForContext   = new ArrayList<String>();

        int len = latitude.size();
        for(int index=0; index<len; index++) {
        	listForLatitude.add(latitude.elementAt(index));
    		listForLongitude.add(longitude.elementAt(index));
        	listForLocation.add(location.elementAt(index));
        	listForContext.add(context.elementAt(index));
        }

        _context.put("lat_list", listForLatitude);
        _context.put("lon_list", listForLongitude);
        _context.put("loc_list", listForLocation);
        _context.put("cxt_list", listForContext);

        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        String html = velocity.generateOutput(_context, DEFAULT_TEMPLATE);

        cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(html));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void handleStreamInitiators() throws Exception {
        if (inputPortsWithInitiators.contains(IN_LATITUDE)) {
            componentContext.pushDataComponentToOutput(OUT_HTML, new StreamInitiator());
         }
        else
            throw new Exception("Unbalanced or unexpected StreamInitiator received");
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        if (inputPortsWithTerminators.contains(IN_LATITUDE)){
            componentContext.pushDataComponentToOutput(OUT_HTML, new StreamTerminator());
        }
        else
            throw new Exception("Unbalanced or unexpected StreamTerminator received");
    }
}
