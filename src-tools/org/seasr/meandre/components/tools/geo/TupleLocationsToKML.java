/**
 * University of Illinois/NCSA
 * Open Source Limport org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
for Supercomputing Applications
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

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.components.nlp.opennlp.OpenNLPNamedEntity;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.gis.GeoLocation;
import org.seasr.meandre.support.generic.gis.LatLngCoord;
import org.xml.sax.SAXException;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Tuple Locations To KML",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tuple, locations, geocoding, kml, map",
        description = "This component geocodes the names of locations (placemarks) specified " +
        		"in the tuples and creates a KML structure with contextual details about each placemark." ,
        dependency = { "protobuf-java-2.2.0.jar", "activation-1.1.jar", "JavaAPIforKml-2.2.0-SNAPSHOT.jar",
                       "jaxb-api-2.2.jar", "jaxb-impl-2.2.jar", "jaxb-xjc-2.2.jar", "stax-api-1.0.1.jar" }
)
public class TupleLocationsToKML extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_LOCATION,
            description = "The document location to be used as context for the placemarks" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_LOCATION = Names.PORT_LOCATION;

    @ComponentInput(
            name = Names.PORT_SENTENCES,
            description = "The sentences to be used as context for the placemarks" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_SENTENCES = Names.PORT_SENTENCES;

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
            name = "kml",
            description = "The KML document" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_KML = "kml";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The attribute containing the location information",
            name = "loc_attribute",
            defaultValue = "text"
    )
    protected static final String PROP_LOC_ATTRIBUTE = "loc_attribute";

    @ComponentProperty(
            description = "The CSS style data to be used for the &lt;span&gt; element surrounding the place name",
            name = "span_css",
            defaultValue = "color:red;"
    )
    protected static final String PROP_SPAN_CSS = "span_css";


    @ComponentProperty(
            description = "Should the generated KML be indented?",
            name = "indent",
            defaultValue = "false"
    )
    protected static final String PROP_INDENT = "indent";

    @ComponentProperty(
            defaultValue = "yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--",
            description = "This property sets the Yahoo API ID to be used for creating the geocoding request.",
            name = Names.PROP_YAHOO_API_KEY
    )
    protected static final String PROP_YAHOO_KEY = Names.PROP_YAHOO_API_KEY;

    //--------------------------------------------------------------------------------------------


    protected String _locAttr;
    protected String _spanCSS;
    protected OutputFormat _outputFormat;
    protected boolean _isStreaming;

    protected Kml _kml;
    protected Document _document;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _locAttr = getPropertyOrDieTrying(PROP_LOC_ATTRIBUTE, ccp);
        _spanCSS = getPropertyOrDieTrying(PROP_SPAN_CSS, true, false, ccp);
        if (_spanCSS.contains("'"))
            throw new ComponentContextException(String.format("'%s' property cannot contain single quote characters!", PROP_SPAN_CSS));

        _outputFormat = new OutputFormat();
        _outputFormat.setPreserveSpace(false);
        _outputFormat.setIndenting(Boolean.parseBoolean(getPropertyOrDieTrying(PROP_INDENT, ccp)));

        GeoLocation.setAPIKey(getPropertyOrDieTrying(PROP_YAHOO_KEY, ccp));

        initializeKml();

        _isStreaming = false;
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String ctxLocation = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LOCATION))[0];
        String[] ctxSentences = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_SENTENCES));

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

        if (!Arrays.asList(inPeer.getFieldNames()).containsAll(Arrays.asList(new String[] {
                _locAttr, OpenNLPNamedEntity.SENTENCE_ID_FIELD, OpenNLPNamedEntity.TEXT_START_FIELD }))) {
            String dump = inPeer.toString();
            throw new ComponentExecutionException(String.format(
                    "The tuple is missing a required attribute. Required: %s, %s, %s%nActual attributes: %s",
            		OpenNLPNamedEntity.SENTENCE_ID_FIELD, _locAttr, OpenNLPNamedEntity.TEXT_START_FIELD, dump));
        }

        SimpleTuple tuple = inPeer.createTuple();
        Map<LatLngCoord, Placemark> places = new HashMap<LatLngCoord, Placemark>();
        Folder folder = _document.createAndAddFolder().withName(ctxLocation).withOpen(true);

        for (Strings t : tuples) {
            tuple.setValues(t);

            String placeName = tuple.getValue(_locAttr);
            int sentenceId = Integer.parseInt(tuple.getValue(OpenNLPNamedEntity.SENTENCE_ID_FIELD));

            GeoLocation[] locations = GeoLocation.geocode(placeName);
            if (locations.length == 0) continue;

            GeoLocation location = locations[0];

            Placemark placemark = places.get(location.getCoord());
            if (placemark == null) {
                placemark = folder.createAndAddPlacemark().withName(StringEscapeUtils.escapeHtml4(placeName));
                placemark.createAndSetPoint().addToCoordinates(location.getLongitude(), location.getLatitude());
                placemark.createAndSetExtendedData();
                places.put(location.getCoord(), placemark);
            }

            String sentence = ctxSentences[sentenceId];
            int offset = Integer.parseInt(tuple.getValue(OpenNLPNamedEntity.TEXT_START_FIELD));
            sentence = String.format("%s<span style='%s'>%s</span>%s",
                    StringEscapeUtils.escapeHtml4(sentence.substring(0, offset)),
                    _spanCSS,
                    StringEscapeUtils.escapeHtml4(placeName),
                    StringEscapeUtils.escapeHtml4(sentence.substring(offset + placeName.length())));

            placemark.getExtendedData().addToData(
                    KmlFactory.createData(sentence).withName(Integer.toString(sentenceId)));
        }

        for (Placemark placemark : places.values()) {
            int n = placemark.getExtendedData().getData().size();
            placemark.setName(String.format("%s (%d)", placemark.getName(), n));
        }

        if (!_isStreaming)
            endStream();
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return true;
    }

    @Override
    public void startStream() throws Exception {
        initializeKml();
        _isStreaming = true;
    }

    @Override
    public void endStream() throws Exception {
        StringWriter writer = new StringWriter();
        if (_kml.marshal(new CDataContentHandler(writer, _outputFormat).asContentHandler()))
            componentContext.pushDataComponentToOutput(OUT_KML, BasicDataTypesTools.stringToStrings(writer.toString()));
        else
            throw new ComponentExecutionException("Could not create the KML file");

        _isStreaming = false;
        initializeKml();
    }

    //--------------------------------------------------------------------------------------------

    private void initializeKml() {
        _kml = KmlFactory.createKml();
        _document = _kml.createAndSetDocument().withName("Locations").withOpen(true);
    }

    public static class CDataContentHandler extends XMLSerializer {
        // see http://www.w3.org/TR/xml/#syntax
        private static final Pattern XML_CHARS = Pattern.compile("[<>&]");

        public CDataContentHandler(OutputStream outputStream, OutputFormat outputFormat) {
            super (outputStream, outputFormat);
        }

        public CDataContentHandler(Writer writer, OutputFormat outputFormat) {
            super(writer, outputFormat);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            boolean useCData = XML_CHARS.matcher(new String(ch, start, length)).find();
            if (useCData) super.startCDATA();
            super.characters(ch, start, length);
            if (useCData) super.endCDATA();
        }
    }
}
