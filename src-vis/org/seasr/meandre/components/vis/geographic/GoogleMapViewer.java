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

import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;

import org.meandre.components.abstracts.AbstractExecutableComponent;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

@Component(creator="Lily Dong",
           description="Generates a web page containing rectangular areas " +
           		"filled with addresses in a XML document.",
           name="Google Map Viewer",
           tags="google map, visualization",
           mode=Mode.webui,
           baseURL="meandre://seasr.org/components/")

public class GoogleMapViewer extends AbstractExecutableComponent
implements WebUIFragmentCallback {
	@ComponentProperty(defaultValue=//"",
		//"ABQIAAAAzuMq2M5--KdBKawoLNQWUxQKQLDP3-h3gXPk25Qansm9VUoOPBRzc41cGNCHCFTWrlzNJCHE5Y_9AA", //demo.seasr.org
		"ABQIAAAAzuMq2M5--KdBKawoLNQWUxRi_j0U6kJrkFvY4-OX2XYmEAa76BQS61jzrv4ruAIpkFQs5Qp-fiN3hg", //127.0.0.1
                       description="This property sets Google Maps API key. The default value is only applicable to 127.0.0.1.",
                       name="googleKey")
    final static String DATA_PROPERTY = "googleKey";

    @ComponentInput(description="Read vector of latitude." +
                "<br>TYPE: java.util.Vector",
              		name= "vectorOfLatitude")
    public final static String DATA_INPUT_1 = "vectorOfLatitude";

    @ComponentInput(description="Read vector of longitude." +
            	"<br>TYPE: java.util.Vector",
          		name= "vectorOfLongitude")
    public final static String DATA_INPUT_2	 = "vectorOfLongitude";

    @ComponentInput(description="Read vector of location." +
            	"<br>TYPE: java.util.Vector",
          		name= "vectorOfLocation")
    public final static String DATA_INPUT_3 = "vectorOfLocation";

    @ComponentInput(description="Read vector of context." +
            	"<br>TYPE: java.util.Vector",
          		name= "vectorOfContext")
    public final static String DATA_INPUT_4 = "vectorOfContext";

    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** The instance ID */
    private String sInstanceID = null;

    /** Store Google Maps API key */
    private String googleKey;

	 /** Store latitude, longitude, address and context surrounding the address */
    private Vector<String> lat, lon, location, context;

    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem raised during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
        try {
            response.getWriter().println(getViz());
        } catch (Exception e) {
            throw new WebUIException(e);
        }
    }

    /** A simple message.
    *
    * @return The html containing the page
    */
    private String getViz() {
        StringBuffer sb = new StringBuffer();

        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n");
        sb.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\n");
        sb.append("<head>\n");

        sb.append("<style>\n");
        sb.append(".scrollable {\n");
        sb.append("width: 150px;\n");
        sb.append("height: 450px;\n");
        sb.append("overflow: auto;\n");
        sb.append("}\n");
        sb.append("</style>\n");

        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n");
        sb.append("<title>Google Map Viewer</title>\n");
        sb.append("<script src=\"http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=false&amp;key="+
        		googleKey + "\"\n");
        sb.append("type=\"text/javascript\"></script>\n");
        sb.append("<script type=\"text/javascript\">\n");

        sb.append("function myclick(i) {\n");
        	sb.append("GEvent.trigger(gmarkers[i], \"click\");\n");
        sb.append("}\n");

        sb.append("function toggleVisibility(me){\n");
        	sb.append("var child = me.childNodes.item(1);\n");
        	sb.append("if (child.style.display=='none'){\n");
        		sb.append("child.style.display='';\n");
        	sb.append("}\n");
        	sb.append("else {\n");
        		sb.append("child.style.display='none';\n");
        	sb.append("}\n");
        sb.append("}\n");

        sb.append("var lat = new Array();\n");
        for(int i=0; i<lat.size(); i++)
        	sb.append("lat[").append(i).append("]=").append(lat.elementAt(i)).append(";\n");

        sb.append("var lon = new Array();\n");
        for(int i=0; i<lon.size(); i++)
        	sb.append("lon[").append(i).append("]=").append(lon.elementAt(i)).append(";\n");

        sb.append("var loc = new Array();\n");
        for(int i=0; i<location.size(); i++)
        	sb.append("loc[").append(i).append("]=\"").append(location.elementAt(i)).append("\";\n");

        sb.append("var cxt = new Array();\n");
        for(int i=0; i<context.size(); i++)
        	sb.append("cxt[").append(i).append("]=\"").append(context.elementAt(i)).append("\";\n");

        sb.append("var side_bar_html = \"\";\n");
        sb.append("var gmarkers = [];\n");

        sb.append("function initialize() {\n");

        sb.append("for(var i=0; i<loc.length-1; i++) {\n");
        	sb.append("var min = i;\n");
        	sb.append("for(var j=i+1; j<loc.length; j++) {\n");
        		sb.append("if(loc[min]>loc[j])\n");
        			sb.append("min = j;\n");
        	sb.append("}\n");
        	sb.append("var tmp = loc[i];\n");
        	sb.append("loc[i] = loc[min];\n");
        	sb.append("loc[min] = tmp;\n");
        	sb.append("tmp = lat[i];\n");
        	sb.append("lat[i] = lat[min];\n");
        	sb.append("lat[min] = tmp;\n");
        	sb.append("tmp = lon[i];\n");
        	sb.append("lon[i] = lon[min];\n");
        	sb.append("lon[min] = tmp;\n");
        	sb.append("tmp = cxt[i];\n");
        	sb.append("cxt[i] = cxt[min];\n");
        	sb.append("cxt[min] = tmp;\n");
        sb.append("}\n");

        sb.append("if (GBrowserIsCompatible()) {\n");
        sb.append("var map = new GMap2(document.getElementById(\"map_canvas\"));\n");
        sb.append("var bounds = new GLatLngBounds();\n");
        sb.append("map.setCenter(new GLatLng(0,0),0);\n");
        sb.append("map.addControl(new GSmallMapControl());\n");
        sb.append("map.addControl(new GMapTypeControl());\n");

        sb.append("function createMarker(point, index) {\n");
        sb.append("var marker = new GMarker(point);\n");
        sb.append("GEvent.addListener(marker, \"click\", function() {\n");
        sb.append("var maxContent = cxt[index];\n");
        sb.append("var str = '<b>'+loc[index]+'</b>';\n");
        sb.append("marker.openInfoWindowHtml('<b>'+loc[index]+'</b>', {maxContent:maxContent, maxTitle:str});\n");
        sb.append("});\n");
        sb.append("return marker;\n");
        sb.append("}\n");

        sb.append("for (var i=0; i<loc.length; i++) {\n");
        sb.append("var latlng = new GLatLng(lat[i], lon[i]);\n");
        sb.append("bounds.extend(latlng);\n");
        sb.append("var marker = createMarker(latlng, i);\n");
        sb.append("gmarkers.push(marker);\n");
        sb.append("map.addOverlay(marker);\n");
        sb.append("side_bar_html = side_bar_html.concat(\'<a href=\"javascript:myclick(\' + i + \')\">\' + loc[i] + \"<\\/a><br>\");\n");
        sb.append("}\n");
        sb.append("map.setZoom(map.getBoundsZoomLevel(bounds));\n");
        sb.append("map.setCenter(bounds.getCenter());\n");
        sb.append("document.getElementById(\"side_bar\").innerHTML = side_bar_html;\n");

        sb.append("}\n");
        sb.append("}\n");

        sb.append("</script>\n");
        sb.append("</head>\n");

        sb.append("<body onload=\"initialize()\">\n");

        sb.append("<div align=\"center\">\n");
        sb.append("<table border=1>\n");
        sb.append("<tr>\n");
        sb.append("<td>\n");
        sb.append("<div id=\"map_canvas\" style=\"width: 800px; height: 450px\"></div>\n");
        sb.append("</td>\n");
        sb.append("<td width=150 valign=\"top\" style=\"text-decoration: underline; color:#4444ff;\">\n");
        sb.append("<div id=\"side_bar\" class=\"scrollable\"></div>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");
        sb.append("</div>\n");

        /*sb.append("<div align=\"center\">\n");
        sb.append("<div id=\"map_canvas\" style=\"width: 800px; height: 500px\"></div>\n");
        sb.append("</div>\n");*/

        sb.append("<br/>\n");

        sb.append("<div align=\"center\">\n");
        sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
                     sInstanceID + "?done=true\">DONE</a></font></table>\n");
        sb.append("</div>\n");

        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
    }

    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem arose during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException {
        String sDone = request.getParameter("done");
        if ( sDone!=null ) {
            sem.release();
        }
        else
            emptyRequest(response);
    }

	/** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
    	lat = (Vector<String>)cc.getDataComponentFromInput(DATA_INPUT_1);
        lon = (Vector<String>)cc.getDataComponentFromInput(DATA_INPUT_2);
        location = (Vector<String>)cc.getDataComponentFromInput(DATA_INPUT_3);
        context  = (Vector<String>)cc.getDataComponentFromInput(DATA_INPUT_4);

    	googleKey = cc.getProperty(DATA_PROPERTY);

    	try {
            sInstanceID = cc.getExecutionInstanceID();
            sem.acquire();
            cc.startWebUIFragment(this);
            sem.acquire();
            cc.stopWebUIFragment(this);
        } catch (Exception e) {
            throw new ComponentExecutionException(e);
        }
    }

	/**
     * Call at the end of an execution flow.
     */
    public void initializeCallBack(ComponentContextProperties ccp)
    throws Exception {
    }

    /**
     * Called when a flow is started.
     */
    public void disposeCallBack(ComponentContextProperties ccp)
    throws Exception {
    }
}

