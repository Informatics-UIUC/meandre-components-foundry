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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.parsers.DataTypeParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 */

@Component(
        creator = "Lily Dong",
        description = "Calculates latitude and longitude for an address contained in the input XML document.",
        name = "Google Map Generator",
        tags = "google map, latitude, longitude",
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/tools/",
        dependency = {"protobuf-java-2.0.3.jar"}
)
public class GoogleMapMaker	extends AbstractExecutableComponent
{

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The source XML document",
            name = Names.PORT_XML
    )
    protected static final String IN_XML = Names.PORT_XML;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "Output latitude",
	        name = Names.PORT_LATITUDE_VECTOR
    )
	protected static final String OUT_LATITUDE = Names.PORT_LATITUDE_VECTOR;

    @ComponentOutput(
            description = "Output longitude",
            name = Names.PORT_LONGITUDE_VECTOR
    )
	protected static final String OUT_LONGITUDE = Names.PORT_LONGITUDE_VECTOR;

    @ComponentOutput(
            description = "Output location.",
            name = Names.PORT_LOCATION_VECTOR
    )
	protected static final String OUT_LOCATION = Names.PORT_LOCATION_VECTOR;

    @ComponentOutput(
            description = "Output context",
            name = Names.PORT_CONTEXT_VECTOR
    )
	protected static final String OUT_CONTEXT = Names.PORT_CONTEXT_VECTOR;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            defaultValue = "yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--",
            description = "This property sets Yahoo API ID. The default value is applicable to all applications.",
            name = Names.PROP_YAHOO_API_KEY
    )
    protected static final String PROP_YAHOO_KEY = Names.PROP_YAHOO_API_KEY;

    //--------------------------------------------------------------------------------------------


    private final static String STRING_DELIMITER = System.getProperty("line.separator");

    private String yahooAPIKey;

    protected class MapData {
        public Vector<String> lat_vector;
        public Vector<String> lon_vector;
        public Vector<String> location_vector;
        public Vector<String> context_vector;
    }


    //--------------------------------------------------------------------------------------------

    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        yahooAPIKey = ccp.getProperty(PROP_YAHOO_KEY);
    }

    public void executeCallBack(ComponentContext cc) throws Exception {
        Document doc = DataTypeParser.parseAsDomDocument(cc.getDataComponentFromInput(IN_XML));

        MapData mapData = extractMapDataFromDocument(doc);

		cc.pushDataComponentToOutput(OUT_LATITUDE, mapData.lat_vector);
		cc.pushDataComponentToOutput(OUT_LONGITUDE, mapData.lon_vector);
		cc.pushDataComponentToOutput(OUT_LOCATION, mapData.location_vector);
		cc.pushDataComponentToOutput(OUT_CONTEXT, mapData.context_vector);
    }

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    protected MapData extractMapDataFromDocument(Document document) throws Exception {
        Vector<String> lat = new Vector<String>();
        Vector<String> lon = new Vector<String>();
        Vector<String> location = new Vector<String>();
        Vector<String> context = new Vector<String>();

        document.getDocumentElement().normalize();
        console.finer("Root element : " + document.getDocumentElement().getNodeName());

        NodeList nodeLst = document.getElementsByTagName("location");
        console.finer("Information of all addresses");

        for (int k = 0; k < nodeLst.getLength(); k++) {
            Node fstNode = nodeLst.item(k);

            String str = fstNode.getTextContent();

            Pattern p = Pattern.compile("[a-zA-Z .]+");
            Matcher m = p.matcher(str);
            if(!m.matches()) //illegal characters
                continue;

            StringBuffer sb = new StringBuffer();
            sb.append("http://local.yahooapis.com/MapsService/V1/geocode?appid=");
            sb.append(yahooAPIKey);
            //String str = fstNode.getTextContent();
            str = str.replaceAll(" ", "%20");
            sb.append("&location=").append(str);

            URL url = new URL(sb.toString());
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(
                        url.openConnection().getInputStream()));
            }catch(java.io.IOException ex) {
                console.warning("bad query : " + str);
                br = null;
            }
            if(br == null)
                continue;
            StringBuffer buffer = new StringBuffer();
            String line;
            while((line = br.readLine())!= null) {
                line = line.trim();
                if(line.length() == 0)
                    continue;
                buffer.append(line).append(STRING_DELIMITER);
            }
            br.close();

            String s = buffer.toString();
            while(true) {//valid location
                if(s.indexOf("<Latitude>") == -1)
                    break;

                int beginIndex = s.indexOf("<Latitude>") + 10,
                    endIndex = s.indexOf("</Latitude>");
                lat.add(s.substring(beginIndex, endIndex));

                beginIndex = s.indexOf("<Longitude>") + 11;
                endIndex = s.indexOf("</Longitude>");
                lon.add(s.substring(beginIndex, endIndex));

                NamedNodeMap nnp = fstNode.getAttributes();

                String sentence = nnp.getNamedItem("sentence").getNodeValue();

                StringTokenizer st = new StringTokenizer(sentence, "|");
                StringBuffer buf = new StringBuffer();
                int nr = 0;
                while(st.hasMoreTokens()) {
                    String nt = st.nextToken();
                    int pos = nt.toLowerCase().indexOf(fstNode.getTextContent());
                    int offset = pos+fstNode.getTextContent().length();
                    nt = new StringBuffer(nt).insert(offset, "</font>").toString();
                    offset = pos;
                    nt = new StringBuffer(nt).insert(offset, "<font color='red'>").toString();
                    buf.append("<div onclick='toggleVisibility(this)' style='position:relative' ALIGN='LEFT'><b>Sentence ").append(++nr).append("</b>");
                    buf.append("<span style='display: ' ALIGN='LEFT'><table><tr><td>").append(nt).append("</td></tr></table></span></div>");
                }

                /*sentence = "<p align=left>" + sentence;
                sentence = sentence.replaceAll("[|]", "</p><hr><p align=left>");
                sentence = sentence + "</p>";*/

                location.add(fstNode.getTextContent()+"("+nr+")");
                context.add(buf.toString());//sentence);

                s = s.substring(endIndex+12);
            }
        }

        MapData mapData = new MapData();
        mapData.lat_vector = lat;
        mapData.lon_vector = lon;
        mapData.context_vector = context;
        mapData.location_vector = location;

        return mapData;
    }
}
