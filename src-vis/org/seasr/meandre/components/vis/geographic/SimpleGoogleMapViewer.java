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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.tools.text.io.GenericTemplate;
import org.seasr.meandre.support.components.geographic.GeoLocation;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

/**
 *
 * @author Mike Haberman  (DO NOT DELETE, I (Mike H is using this class)
 *
 */

@Component(
        creator = "Mike Haberman",
        description = "Presents a simple google map based on location inputs",
        name = "Simple Google Map Viewer",
        tags = "string, visualization",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.6.2-dep.jar" },
        resources = { "SimpleGoogleMapViewer.vm" }
)
public class SimpleGoogleMapViewer extends GenericTemplate {

    //
    // google key for ncsa.uiuc.edu
    //
    static final String NCSA_KEY = "ABQIAAAADV1H5JfZH41B6yxB1yGVFhQmgYtTImkVBc-VhblDgOLOdwhVaBSPwcEgsBl7atDdDJjnfl51p9fU5A";


    //
    // google key for localhost
    //
    static final String LOCALHOST_KEY = "ABQIAAAADV1H5JfZH41B6yxB1yGVFhT2yXp_ZAY8_ufC3CFXhHIE1NvwkxR-tpPz23kSE2A2buBtYPTRubh20w";
    static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/geographic/SimpleGoogleMapViewer.vm";


    //------------------------------ INPUTS -----------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "set of tuples" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;

	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ PROPERTIES --------------------------------------------------

	//
	// specific to this component
	//
	@ComponentProperty(
	        description = "The title for the page",
	        name = Names.PROP_TITLE,
	        defaultValue = "Google Maps"
	)
	protected static final String PROP_TITLE = Names.PROP_TITLE;


	@ComponentProperty(
	        description = "max locations to query, -1 no max, yahoo restricts query access",
	        name = "maxLocations",
	        defaultValue = "100"
	)
	protected static final String PROP_LOC_MAX = "maxLocations";


   @ComponentProperty(
	        description = "Default key for google maps",
	        name = "googleMapKey",
	        defaultValue = LOCALHOST_KEY
	)
	protected static final String PROP_KEY = "googleMapKey";

	@ComponentProperty(
	        description = "The template name",
	        name = GenericTemplate.PROP_TEMPLATE,
	        defaultValue = DEFAULT_TEMPLATE
	)
    protected static final String PROP_TEMPLATE = GenericTemplate.PROP_TEMPLATE;

    //--------------------------------------------------------------------------------------------

    int maxLocations = 100;

    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

	    context.put("key",          ccp.getProperty(PROP_KEY));
	    context.put("title",        ccp.getProperty(PROP_TITLE));
	    context.put("addDone", true);


	    maxLocations = Integer.parseInt(ccp.getProperty(PROP_LOC_MAX));
	    if (maxLocations < 0) {
	    	maxLocations = -1;
	    }

	    console.info("max locations " + maxLocations);
	}

	
	//
	// TODO: type/text/location should be properties
	//
	// the tuple must have either:
	// 1. a latitude and longitude field
	// 2. location field (who's value is be looked up)
	// 3. type field (who's value is "location") and a text field (who's value is to be looked up)
	//
	
	String locationField = "location";
	String typeField     = "type";
	String textField     = "text";
	String latField      = "latitude";
	String lngField      = "longitude";
	
    @Override
    public void executeCallBack(ComponentContext cc) throws Exception 
    {
    	//
    	// fetch the input, push it to the context
    	//

    	Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = tuplePeer.createTuple();

		//
		// we can get location values directly from the data
		// or by looking at a "type" and "text" values
		//
		int LAT_IDX  = tuplePeer.getIndexForFieldName(latField);
		int LONG_IDX = tuplePeer.getIndexForFieldName(lngField);
		
		int TYPE_IDX     = -1;
		int TEXT_IDX     = -1;
		int LOCATION_IDX = -1;
		
		if (LAT_IDX == -1 || LONG_IDX == -1) {
			
			console.info("tuple has no fields " + latField + " " + lngField);
			console.info("trying " + locationField);
			
			LOCATION_IDX = tuplePeer.getIndexForFieldName(locationField);
			if (LOCATION_IDX == -1) {
				
				console.info("tuple has no field named " + locationField);
				TYPE_IDX = tuplePeer.getIndexForFieldName(typeField);
				TEXT_IDX = tuplePeer.getIndexForFieldName(textField);

				if (TYPE_IDX == -1 || TEXT_IDX == -1) {
					console.info(tuplePeer.toString());
					throw new ComponentExecutionException("tuple has no fields: " + typeField + "," + textField);
				}
			}
		}

		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

		List<GeoLocation> geos = new ArrayList<GeoLocation>();

		for (int i = 0; i < in.length; i++) {
			
			tuple.setValues(in[i]);
			
			
			if (LAT_IDX != -1 && LONG_IDX != -1) {
				
				String lat = tuple.getValue(LAT_IDX);
				String lng = tuple.getValue(LONG_IDX);
				
				GeoLocation geo = new GeoLocation(Double.parseDouble(lat), Double.parseDouble(lng));
				geos.add(geo);
				
				continue;
			}

			//
			// get the location data
			//
			String location = null;
			if (LOCATION_IDX != -1) {
				location = tuple.getValue(LOCATION_IDX);
			}
			else {
				String type = tuple.getValue(TYPE_IDX);
				if (! type.equals(locationField)) {
					continue;
				}
				location = tuple.getValue(TEXT_IDX);
			}


			// convert to a geo location
			try {
				
				List<GeoLocation> locations = GeoLocation.getAllLocations(location);
				for (GeoLocation g: locations) {
					// console.info("found " + g.toString());
					if (g.isValid()) {
						geos.add(g);
					}
					else {
						console.info("Unable to find " + location);
					}
					
				}

			}
			catch (IOException e) {
				console.info("unable to contact yahoo " + location);
				break;
			}

			if (maxLocations > 0 && geos.size() > maxLocations) {
				break;
			}

		}

		context.put("geoList", geos);

		console.info("Ready to view google maps with locations " + geos.size());

		//
    	// now wait for the user to access the webUI
		//
    	super.executeCallBack(cc);
    }

	@Override
	protected boolean processRequest(HttpServletRequest request) throws IOException {
	   return true;
	}

	public static String buildDefaultViz(List<GeoLocation> list) throws Exception {
        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        VelocityContext context;

        context = velocity.getNewContext();
        context.put("key",    LOCALHOST_KEY);
        context.put("title",  "Geo");
        context.put("geoList", list);

        String html = velocity.generateOutput(context, DEFAULT_TEMPLATE);
        return html;
    }
}
