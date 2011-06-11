package org.seasr.meandre.components.tools.geo;

import java.util.ArrayList;
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
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.components.geographic.GeoLocation;

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

    //--------------------------------------------------------------------------------------------


    private boolean _returnOneValue;
    private boolean _isStreaming = false;
    private final List<GeoLocation> _locations = new ArrayList<GeoLocation>();


    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		super.initializeCallBack(ccp);

		_returnOneValue = Boolean.parseBoolean(ccp.getProperty(PROP_RETURN_ONE_COORDINATE));
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		Object input = cc.getDataComponentFromInput(IN_PLACE_NAME);
		String[] locArr = DataTypeParser.parseAsString(input);

		for (String loc : locArr) {
			List<GeoLocation> locations = GeoLocation.getAllLocations(loc);

			if (locations.size() == 0) {
				console.warning(String.format("The location '%s' could not be geocoded - ignoring it...", loc));
				continue;
			}

			if (_returnOneValue)
				_locations.add(locations.get(0));
			else
				_locations.addAll(locations);
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
		for (GeoLocation location : _locations) {
			String latitude = Double.toString(location.getLatitude());
			String longitude = Double.toString(location.getLongitude());
			String placeName = location.getQueryLocation();

			componentContext.pushDataComponentToOutput(OUT_LATITUDE, BasicDataTypesTools.stringToStrings(latitude));
			componentContext.pushDataComponentToOutput(OUT_LONGITUDE, BasicDataTypesTools.stringToStrings(longitude));
			componentContext.pushDataComponentToOutput(OUT_PLACE_NAME, BasicDataTypesTools.stringToStrings(placeName));
		}

		_isStreaming = false;
		_locations.clear();
	}
}
