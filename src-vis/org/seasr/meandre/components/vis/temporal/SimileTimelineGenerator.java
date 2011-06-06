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

package org.seasr.meandre.components.vis.temporal;

import java.io.File;
import java.io.Writer;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
import org.seasr.meandre.support.generic.encoding.Base64;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.seasr.meandre.support.generic.io.FileUtils;
import org.seasr.meandre.support.generic.io.IOUtils;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;
import org.w3c.dom.Document;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 *
 * NOTE: If this component is used in a flow for the Zotero environment do not forget to
 *       set the 'timeline_api_url' property to http://simile.mit.edu/timeline/api/timeline-api.js
 */

@Component(
        creator = "Lily Dong",
        description = "Generates the necessary HTML and XML files " +
                      "for viewing timeline and store them on the local machine. " +
                      "The two files will be stored under public/resources/timeline/. " +
                      "For fast browse, dates are grouped into different time slices. ",
        name = "Simile Timeline Generator",
        tags = "simile, timeline",
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        dependency = {"protobuf-java-2.2.0.jar", "simile-timeline.jar"},
        resources = {"SimileTimelineGenerator.vm"}
)
public class SimileTimelineGenerator extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        name = Names.PORT_MIN_VALUE,
	        description = "The minimum year in input document." +
                "<br>TYPE: java.lang.Integer" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Integers" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
    protected static final String IN_MIN_YEAR = Names.PORT_MIN_VALUE;

	@ComponentInput(
	        name = Names.PORT_MAX_VALUE,
	        description = "The maximum year in input document." +
                "<br>TYPE: java.lang.Integer" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Integers" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
    protected static final String IN_MAX_YEAR = Names.PORT_MAX_VALUE;

	@ComponentInput(
	        name = Names.PORT_XML,
	        description = "The source XML document" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
	)
    protected static final String IN_XML = Names.PORT_XML;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        name = Names.PORT_HTML,
	        description = "The HTML for the Simile Timeline viewer" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_HTML = Names.PORT_HTML;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
            name = "timeline_api_url",
            description = "The URL to the Simile Timline API, or leave empty to use the embedded one",
            defaultValue = ""
    )
    protected static final String PROP_TIMELINE_API_URL = "timeline_api_url";

	@ComponentProperty(
            name = "save_output_to_file",
            description = "Save the Simile XML output to file?",
            defaultValue = "false"
    )
    protected static final String PROP_SAVE_OUTPUT_TO_FILE = "save_output_to_file";

	@ComponentProperty(
            name = "inline_simile_xml",
            description = "Generate inline Simile data?",
            defaultValue = "true"
    )
    protected static final String PROP_INLINE_SIMILE_XML = "inline_simile_xml";

    //--------------------------------------------------------------------------------------------

	protected static final String SIMILE_API_PATH = "simile-timeline-api";   // this path is assumed to be appended to the published_resources location
	protected static final String SIMILE_JS = "timeline-api.js";

	protected static final String simileVelocityTemplate =
	    "org/seasr/meandre/components/vis/temporal/SimileTimelineGenerator.vm";

    /** Store the minimum value of year */
    private int minYear;

    /** Store the maximum value of year */
    private int maxYear;

    private boolean inlineSimileXml;
    private boolean saveOutputToFile;

    private final List<File> tmpFiles = new ArrayList<File>();

    private VelocityContext _context;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        saveOutputToFile = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_SAVE_OUTPUT_TO_FILE, ccp));
        inlineSimileXml = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_INLINE_SIMILE_XML, ccp));
        String timelineAPI = getPropertyOrDieTrying(PROP_TIMELINE_API_URL, true, false, ccp);

    	_context = VelocityTemplateService.getInstance().getNewContext();
        _context.put("ccp", ccp);

        if (timelineAPI.length() == 0) {
            String timelineAPIDir = ccp.getPublicResourcesDirectory() + File.separator + SIMILE_API_PATH;
            InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), SIMILE_JS, timelineAPIDir, false);
            switch (status) {
                case SKIPPED:
                    console.fine(String.format("Installation skipped - %s is already installed", SIMILE_JS));
                    break;

                case FAILED:
                    throw new ComponentContextException(String.format("Failed to install %s at %s",
                            SIMILE_JS, new File(timelineAPIDir).getAbsolutePath()));
            }

            timelineAPI = "/public/resources/" + SIMILE_API_PATH.replaceAll("\\\\", "/") + "/" + SIMILE_JS;
        }

        console.fine("Using Simile Timeline API from: " + timelineAPI);
        _context.put("simileTimelineAPI", timelineAPI);
        _context.put("inlineSimileXml", inlineSimileXml);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	String simileXml = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_XML))[0];
    	minYear = DataTypeParser.parseAsInteger(cc.getDataComponentFromInput(IN_MIN_YEAR))[0].intValue();
    	maxYear = DataTypeParser.parseAsInteger(cc.getDataComponentFromInput(IN_MAX_YEAR))[0].intValue();

        Document xmlDoc = DOMUtils.createDocument(simileXml);
        xmlDoc.normalize();
        if (xmlDoc.getDocumentElement().getElementsByTagName("event").getLength() == 0) {
            outputError("No dates could be extracted from your item(s) - Nothing to display", Level.WARNING);
            return;
        }

        String webUiUrl = cc.getWebUIUrl(true).toString();
        String dirName = cc.getPublicResourcesDirectory() + File.separator;
        String xmlLocation = webUiUrl + "public/resources/simile/";

        if (!inlineSimileXml || saveOutputToFile) {
            dirName += "simile" + File.separator;
            console.finest("Set storage location to " + dirName);

            // make sure the folder exists
            new File(dirName).mkdirs();

            File xmlFile = null;

	        if (saveOutputToFile) {
		        Date now = new Date();
		        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		        String xmlFileName = "my" + formatter.format(now) + ".xml";
		        xmlLocation += xmlFileName;
		        xmlFile = new File(dirName + xmlFileName);
	        } else {
	        	xmlFile = File.createTempFile("simile_", ".xml", new File(dirName));
	        	tmpFiles.add(xmlFile);
	        	xmlLocation += xmlFile.getName();
	        }

	        URI xmlURI = xmlFile.toURI();

            Writer xmlWriter = IOUtils.getWriterForResource(xmlURI);
            xmlWriter.write(simileXml);
            xmlWriter.close();
        }

        String simileHtml = generateHTML(simileXml, xmlLocation);

        if (saveOutputToFile) {
        	Date now = new Date();
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	        String htmlFileName = "my" + formatter.format(now) + ".html";
	        String htmlLocation = webUiUrl + "public/resources/simile/" + htmlFileName;

            URI htmlURI = new  File(dirName + htmlFileName).toURI();

            Writer htmlWriter = IOUtils.getWriterForResource(htmlURI);
            htmlWriter.write(simileHtml);
            htmlWriter.close();

            console.info("The Simile Timeline HTML content was created at " + htmlLocation);
            console.info("The Simile Timeline XML content was created at " + xmlLocation);
        }

        cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(simileHtml));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    	for (File tmpFile : tmpFiles)
    		FileUtils.deleteFileOrDirectory(tmpFile);
    }

    //--------------------------------------------------------------------------------------------

    private String generateHTML(String simileXml, String simileXmlUrl) throws Exception {
        VelocityTemplateService velocity = VelocityTemplateService.getInstance();

        int range = maxYear-minYear;
        int interval;
        ArrayList<Integer> list = new ArrayList<Integer>();

        if(range<20)  //for every single year
        	interval = 1;
        else  if(range>=20 && range<100) //for every decade
        	interval = 10;
        else { //for every century
        	interval = 100;
        }

        for(int year=minYear; year<=maxYear; year+=interval)
    		list.add(new Integer(year));

        _context.put("interval",  (int)Math.ceil((float)range/interval));
        _context.put("items", list);

        _context.put("maxYear", maxYear);
        _context.put("minYear", minYear);
        _context.put("simileXmlBase64", Base64.encodeString(simileXml));
        _context.put("simileXmlUrl", simileXmlUrl);

        return velocity.generateOutput(_context, simileVelocityTemplate);
    }
}
