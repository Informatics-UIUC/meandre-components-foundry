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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.generic.encoding.Base64;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;
import org.seasr.meandre.support.generic.io.ClasspathUtils;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.seasr.meandre.support.generic.io.IOUtils;
import org.seasr.meandre.support.generic.io.JARInstaller;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;
import org.w3c.dom.Document;

import de.schlichtherle.io.FileInputStream;

/**
 * @author Lily Dong
 * @author Boris Capitanu
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
        baseURL="meandre://seasr.org/components/tools/",
        firingPolicy = FiringPolicy.all,
        dependency = {"protobuf-java-2.2.0.jar", "simile-timeline.jar"},
        resources = {"SimileTimelineGenerator.vm"}
)
public class SimileTimelineGenerator extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        description = "The minimum year in input document.",
	        name = Names.PORT_MIN_VALUE
	)
    protected static final String IN_MIN_YEAR = Names.PORT_MIN_VALUE;

	@ComponentInput(
	        description = "The maximum year in input document.",
	        name = Names.PORT_MAX_VALUE
	)
    protected static final String IN_MAX_YEAR = Names.PORT_MAX_VALUE;

	@ComponentInput(
	        description = "The source XML document",
	        name = Names.PORT_XML
	)
    protected static final String IN_XML = Names.PORT_XML;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        description = "The HTML for the Simile Timeline viewer",
	        name = Names.PORT_HTML
	)
	protected static final String OUT_HTML = Names.PORT_HTML;

    //--------------------------------------------------------------------------------------------

	protected static final String SIMILE_API_PATH = "simile-timeline-api";   // this path is assumed to be appended to the published_resources location

	protected static final String simileVelocityTemplate =
	    "org/seasr/meandre/components/vis/temporal/SimileTimelineGenerator.vm";

    /** Store the minimum value of year */
    private int minYear;

    /** Store the maximum value of year */
    private int maxYear;

    private VelocityContext _context;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    	_context = VelocityTemplateService.getInstance().getNewContext();
        _context.put("ccp", ccp);

        File simileApiJar = null;
        URL simileJarDepUrl = ClasspathUtils.findDependencyInClasspath("simile-timeline.jar", getClass());
        if (simileJarDepUrl != null)
            simileApiJar = new File(simileJarDepUrl.toURI());

        if (!simileApiJar.exists())
            throw new ComponentContextException("Could not find simile-timeline.jar");

        console.fine("Installing Simile Timeline API from: " + simileApiJar.toString());

        String simileApiDir = ccp.getPublicResourcesDirectory() + File.separator + SIMILE_API_PATH;
        InstallStatus status = JARInstaller.installFromStream(new FileInputStream(simileApiJar), simileApiDir, false);
        switch (status) {
            case SKIPPED:
                console.fine("Installation skipped - Simile Timeline API is already installed");
                break;

            case FAILED:
                throw new ComponentContextException("Failed to install the Simile Timeline API at " + new File(simileApiDir).getAbsolutePath());
        }

        _context.put("simileTimelineAPI", "/public/resources/" + SIMILE_API_PATH + "/timeline-api.js");
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	String simileXml = DataTypeParser.parseAsString(
    			cc.getDataComponentFromInput(IN_XML))[0];
    	minYear = DataTypeParser.parseAsInteger(
    			cc.getDataComponentFromInput(IN_MIN_YEAR))[0].intValue();
    	maxYear = DataTypeParser.parseAsInteger(
    			cc.getDataComponentFromInput(IN_MAX_YEAR))[0].intValue();

        String dirName = cc.getPublicResourcesDirectory() + File.separator;
        dirName += "simile" + File.separator;

        // make sure the folder exists
        new File(dirName).mkdirs();

        console.finest("Set storage location to " + dirName);

        String webUiUrl = cc.getWebUIUrl(true).toString();
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String htmlFileName = "my" + formatter.format(now) + ".html",
               xmlFileName = "my" + formatter.format(now) + ".xml";
        String htmlLocation = webUiUrl + "public/resources/simile/" + htmlFileName,
               xmlLocation  = webUiUrl + "public/resources/simile/" + xmlFileName;

        console.finest("htmlFileName=" + htmlFileName);
        console.finest("xmlFileName=" + xmlFileName);
        console.finest("htmlLocation=" + htmlLocation);
        console.finest("xmlLocation=" + xmlLocation);

        URI xmlURI = DataTypeParser.parseAsURI(new File(dirName + xmlFileName).toURI());
        URI htmlURI = DataTypeParser.parseAsURI(new  File(dirName + htmlFileName).toURI());

        Document xmlDoc = DOMUtils.createDocument(simileXml);
        xmlDoc.normalize();
        if (xmlDoc.getDocumentElement().getElementsByTagName("event").getLength() == 0) {
            outputError("No dates could be extracted from your item(s) - Nothing to display", Level.WARNING);
            return;
        }

        Writer xmlWriter = IOUtils.getWriterForResource(xmlURI);
        xmlWriter.write(simileXml);
        xmlWriter.close();

        String simileHtml = generateHTML(simileXml, xmlLocation);

        Writer htmlWriter = IOUtils.getWriterForResource(htmlURI);
        htmlWriter.write(simileHtml);
        htmlWriter.close();

        console.info("The Simile Timeline HTML content was created at " + htmlLocation);
        console.info("The Simile Timeline XML content was created at " + xmlLocation);

        cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(simileHtml));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
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
