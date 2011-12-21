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

package org.seasr.meandre.components.tools.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.generic.gis.GeoLocation;

@Component(
		name = "Geo Locator",
		creator = "Boris Capitanu",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple, group",
		description = "This component resolves names of locations into latitude/longitude coordinates" ,
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class GeoLocator extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "place_name",
            description = "The name of a geographic location (city, country, state, address, etc.)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: java.lang.String"
    )
    protected static final String IN_PLACE_NAME = "place_name";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "latitude",
            description = "The latitude of the location given" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_LATITUDE = "latitude";

    @ComponentOutput(
            name = "longitude",
            description = "The longitude of the location given" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_LONGITUDE = "longitude";

    @ComponentOutput(
            name = "place_name",
            description = "Same as input" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_PLACE_NAME = "place_name";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "Return one coordinate only?",
            name = "return_one_coordinate",
            defaultValue = "false"
    )
    protected static final String PROP_RETURN_ONE_COORDINATE = "return_one_coordinate";

    @ComponentProperty(
            defaultValue = "yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--",
            description = "This property sets the Yahoo API ID to be used for creating the geocoding request.",
            name = Names.PROP_YAHOO_API_KEY
    )
    protected static final String PROP_YAHOO_KEY = Names.PROP_YAHOO_API_KEY;

    //--------------------------------------------------------------------------------------------


    private boolean _returnOneValue;
    private boolean _isStreaming = false;
    private final List<GeoLocation> _locations = new ArrayList<GeoLocation>();


    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		super.initializeCallBack(ccp);

		_returnOneValue = Boolean.parseBoolean(ccp.getProperty(PROP_RETURN_ONE_COORDINATE));
		GeoLocation.setAPIKey(getPropertyOrDieTrying(PROP_YAHOO_KEY, ccp));
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		Object input = cc.getDataComponentFromInput(IN_PLACE_NAME);
		String[] locArr = DataTypeParser.parseAsString(input);

		for (String loc : locArr) {
			GeoLocation[] locations = GeoLocation.geocode(loc);

			if (locations == null) {
				console.warning(String.format("The location '%s' could not be geocoded - ignoring it...", loc));
				continue;
			}

			if (_returnOneValue)
				_locations.add(locations[0]);
			else
				_locations.addAll(Arrays.asList(locations));
		}

		if (!_isStreaming)
			endStream();
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		_locations.clear();
		GeoLocation.disposeCache();
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
		for (GeoLocation location : _locations) {
			String latitude = Double.toString(location.getLatitude());
			String longitude = Double.toString(location.getLongitude());
			String placeName = location.getQueryPlaceName();

			componentContext.pushDataComponentToOutput(OUT_LATITUDE, BasicDataTypesTools.stringToStrings(latitude));
			componentContext.pushDataComponentToOutput(OUT_LONGITUDE, BasicDataTypesTools.stringToStrings(longitude));
			componentContext.pushDataComponentToOutput(OUT_PLACE_NAME, BasicDataTypesTools.stringToStrings(placeName));
		}

		_isStreaming = false;
		_locations.clear();
	}
}
