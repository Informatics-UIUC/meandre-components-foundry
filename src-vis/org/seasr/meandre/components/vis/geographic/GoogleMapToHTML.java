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

package org.seasr.meandre.components.vis.geographic;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

@Component(
        creator="Boris Capitanu",
        description="Generates a Google Maps HTML document containing markers to the locations received via the inputs.",
        name="Google Map To HTML",
        tags="google map, html",
        mode=Mode.compute,
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/",
        resources = {"GoogleMapToHTML.vm"}
)
public class GoogleMapToHTML extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        name = "latitude",
	        description = "The latitude"
	)
    public final static String IN_LATITUDE = "latitude";

	@ComponentInput(
	        name = "longitude",
	        description = "The longitude"
	)
    public final static String IN_LONGITUDE = "longitude";

	@ComponentInput(
	        name = "place_name",
	        description = "The place name"
	)
    public final static String IN_PLACE_NAME = "place_name";

	@ComponentInput(
	        name = "marker_info",
	        description = "Text or HTML to be displayed to the user when the user clicks the location marker"
	)
    public final static String IN_MARKER_INFO = "marker_info";

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        name = Names.PORT_HTML,
	        description = "The HTML for the Google Map viewer" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_HTML = Names.PORT_HTML;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The template name",
            name = Names.PROP_TEMPLATE,
            defaultValue = "org/seasr/meandre/components/vis/geographic/GoogleMapToHTML.vm"
    )
    protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;

	@ComponentProperty(
	        defaultValue = "",
            description = "This property sets the Google Maps API key of your web site" +
            	"<br>To obtain one, visit <a href='http://code.google.com/apis/maps/signup.html'>Google Maps API Signup Page</a>",
            name = "google_maps_api_key"
	)
    protected static final String PROP_GOOGLE_KEY = "google_maps_api_key";

    //--------------------------------------------------------------------------------------------


    protected String _template;
    private String _googleApiKey;
    private boolean _isStreaming = false;

    private final List<GoogleLocationMarker> _locations = new ArrayList<GoogleMapToHTML.GoogleLocationMarker>();


    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		super.initializeCallBack(ccp);

		_googleApiKey = getPropertyOrDieTrying(PROP_GOOGLE_KEY, ccp);
        _template = getPropertyOrDieTrying(PROP_TEMPLATE, ccp);
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		String[] latStr = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LATITUDE));
		String[] lonStr = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LONGITUDE));
		String[] placeName = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_PLACE_NAME));
		String[] markerInfo = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_MARKER_INFO));

		if (latStr.length == lonStr.length && lonStr.length == placeName.length && placeName.length == markerInfo.length)
			for (int i = 0, iMax = latStr.length; i < iMax; i++)
				_locations.add(new GoogleLocationMarker(latStr[i], lonStr[i], placeName[i], markerInfo[i]));
		else {
			console.warning("Multiple locations given in input data, but data lengths are inconsistent! Using first data location only!");
			_locations.add(new GoogleLocationMarker(latStr[0], lonStr[0], placeName[0], markerInfo[0]));
		}

		if (!_isStreaming)
			endStream();
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		_locations.clear();
	}

    //--------------------------------------------------------------------------------------------

	@Override
	public boolean isAccumulator() {
		return true;
	}

	@Override
	public void startStream() throws Exception {
		_isStreaming = true;
		_locations.clear();
	}

	@Override
	public void endStream() throws Exception {
        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        VelocityContext context = velocity.getNewContext();

        context.put("locations", _locations);
		context.put("google_maps_api_key", _googleApiKey);

        String html = velocity.generateOutput(context, _template);
        componentContext.pushDataComponentToOutput(OUT_HTML, html);

		_isStreaming = false;
		_locations.clear();
	}

    //--------------------------------------------------------------------------------------------

	public class GoogleLocationMarker {
		private final String _latitude;
		private final String _longitude;
		private final String _placeName;
		private final String _markerInfo;

		public GoogleLocationMarker(String latitude, String longitude, String placeName, String markerInfo) {
			_latitude = latitude;
			_longitude = longitude;
			_placeName = placeName;
			_markerInfo = markerInfo;
		}

		public String getLatitude() {
			return _latitude;
		}

		public String getLongitude() {
			return _longitude;
		}

		public String getPlaceName() {
			return _placeName;
		}

		public String getMarkerInfo() {
			return _markerInfo;
		}
	}
}
