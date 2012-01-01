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

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.gis.GeoLocation;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Tuple Geocoder",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "tuple, locations, geocoding",
        description = "This component adds two fields to the incoming tuples containing " +
        		"the geocoded coordinates of the names of locations (placemarks) specified in the tuples." ,
        dependency = { "protobuf-java-2.2.0.jar" }
)
public class TupleGeocoder extends AbstractExecutableComponent {

    protected static final String LATITUDE_FIELD = "lat";
    protected static final String LONGITUDE_FIELD = "lon";

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "yahoo_api_key",
            description = "The Yahoo API ID to be used for creating the geocoding request."
    )
    protected static final String IN_YAHOO_KEY = "yahoo_api_key";

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The tuple(s) containing the location" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The set of tuples with the additional '" + LATITUDE_FIELD + "' and '" + LONGITUDE_FIELD + "' fields" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples, with the additional '" + LATITUDE_FIELD + "' and '" + LONGITUDE_FIELD + "' fields" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The tuple field name containing the location information",
            name = "loc_field",
            defaultValue = "text"
    )
    protected static final String PROP_LOC_FIELD = "loc_field";

    @ComponentProperty(
            description = "Remove tuples specifying unknown locations? " +
            		"If this is set to false and a location cannot be geocoded, the values for the latitude and longitude fields will be empty.",
            name = "remove_unknown",
            defaultValue = "true"
    )
    protected static final String PROP_REMOVE_UNKNOWN = "remove_unknown";

    //--------------------------------------------------------------------------------------------


    protected String _locField;
    protected boolean _removeUnknown;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _locField = getPropertyOrDieTrying(PROP_LOC_FIELD, ccp);
        _removeUnknown = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_REMOVE_UNKNOWN, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer inPeer  = new SimpleTuplePeer(inMeta);

        Object input = cc.getDataComponentFromInput(IN_TUPLES);
        Strings[] tuples;

        if (input instanceof StringsArray)
            tuples = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) input);

        else

        if (input instanceof Strings) {
            Strings inTuple = (Strings) input;
            tuples = new Strings[] { inTuple };
        }

        else
            throw new ComponentExecutionException("Don't know how to handle input of type: " + input.getClass().getName());

        if (inPeer.getIndexForFieldName(_locField) < 0)
            throw new ComponentExecutionException("Incoming tuples do not have a field named: '" + _locField + "'");

        String yahooKey = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_YAHOO_KEY))[0];
        GeoLocation.setAPIKey(yahooKey);

        SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, new String[] { LATITUDE_FIELD, LONGITUDE_FIELD });
        StringsArray.Builder outTuples = StringsArray.newBuilder();

        for (Strings t : tuples) {
            SimpleTuple inTuple = inPeer.createTuple();
            inTuple.setValues(t);

            String placeName = inTuple.getValue(_locField);

            GeoLocation[] locations = GeoLocation.geocode(placeName);
            if (locations.length == 0 && _removeUnknown) continue;

            String lat = "";
            String lon = "";

            if (locations.length > 0) {
                GeoLocation location = locations[0];
                lat = "" + location.getLatitude();
                lon = "" + location.getLongitude();
            }

            SimpleTuple outTuple = outPeer.createTuple();
            outTuple.setValue(inTuple);
            outTuple.setValue(LATITUDE_FIELD, lat);
            outTuple.setValue(LONGITUDE_FIELD, lon);

            outTuples.addValue(outTuple.convert());
        }

        cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
        cc.pushDataComponentToOutput(OUT_TUPLES, outTuples.build());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
