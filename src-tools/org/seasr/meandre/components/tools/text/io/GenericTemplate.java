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

package org.seasr.meandre.components.tools.text.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.html.VelocityTemplateService;

/**
 * template loading notes:
 *
 *  templates are searched in
 *     1 local file system on the server: published_resources/templates (under the server install)
 *     2 local file system on the server: ./templates  where . is user.path
 *     3 on the classpath
 *     4 in any jars on the classpath
 *
 *     the default template is GUITemplate.vm
 *
 *  local files take precedence over class path files
 *
 *  ComponentContextProperties  are in the velocity context as ccp
 *  ComponentContext             is in the velocity context as cc
 *  HttpResponse                 is in the velocity context as response
 *  User supplied key=value pairs are in the velocity context as userMap
 *
 *  TODO: it would be nice to be able to put the HttpRequest object
 *  in the velocity context
 *
 *  @author Mike Haberman
 *  @author Boris Capitanu
 */

@Component(
        creator = "Mike Haberman",
        description = "Generates and displays a webpage via a Velocity Template ",
        name = "Generic Template",
        tags = "string, visualization",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/",
        resources = { "GenericTemplate.vm" },
        dependency = { "velocity-1.6.1-dep.jar" }
)
public class GenericTemplate extends AbstractExecutableComponent implements WebUIFragmentCallback {

    //------------------------------ INPUTS ------------------------------------------------------

    //
    // this is a generic input, doesn't have to be used, up to the template
    //
    /*
    @ComponentInput(description="Name of input port", name= "input")
    public final static String DATA_INPUT = "input";
    */

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "Name of output port",
            name = Names.PORT_OBJECT
    )
    protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "The template name",
	        name = Names.PROP_TEMPLATE,
	        defaultValue = "org/seasr/meandre/components/tools/text/io/GenericTemplate.vm"
	)
	protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;

	@ComponentProperty(
	        description = "User supplied property list",
	        name = Names.PROP_PROPERTIES,
	        defaultValue = "key=value,author=mike"
	)
	protected static final String PROP_TEMPLATE_PROPERTIES = Names.PROP_PROPERTIES;

	@ComponentProperty(
	        description = "Generate meta refresh html after execute",
	        name = Names.PROP_REFRESH,
	        defaultValue = "true"
	)
    protected static final String PROP_REFRESH_AFTER_EXECUTE = Names.PROP_REFRESH;

    //--------------------------------------------------------------------------------------------


    protected VelocityContext context;
    protected String templateName;

    /* what field will we check for in the HTTPRequest */
    // the template should actually set this variable via $gui.setPushValue()
    protected String formInputName = "done";

    // convenience properties to easily push additional properties
    // not needed, template can always do $ccp.getProperty("title")
    protected String[] templateVariables = {};

    // generate meta refresh html after execute
    protected boolean doRefresh = true;

    protected boolean done;
    private Map<String, String[]> parameterMap;


    //--------------------------------------------------------------------------------------------

    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        templateName = ccp.getProperty(PROP_TEMPLATE);
        doRefresh = Boolean.parseBoolean(ccp.getProperty(PROP_REFRESH_AFTER_EXECUTE));


        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        context = velocity.getNewContext();

        /*
         *  Make a context object and populate with the data.  This
         *  is where the Velocity engine gets the data to resolve the
         *  references (ex. $date) in the template
         */

        context.put("dir",  System.getProperty("user.dir"));
        context.put("date", new Date());
        context.put("ccp",  ccp);

        String toParse = ccp.getProperty(PROP_TEMPLATE_PROPERTIES);
        HashMap<String,String> map = new HashMap<String,String>();
        StringTokenizer tokens = new StringTokenizer(toParse, ",");
        while (tokens.hasMoreTokens()){
            String kv = tokens.nextToken();
            int idx = kv.indexOf('=');
            if (idx > 0) {
                String key   = kv.substring(0,idx);
                String value = kv.substring(idx+1);
                map.put(key.trim(), value.trim());
            }
        }
        context.put("userMap", map);

        // push property values to the context
        for (String name: templateVariables) {
            String value = ccp.getProperty(name);
            context.put(name,value);
        }
    }

    public void executeCallBack(ComponentContext cc) throws Exception {
        String sInstanceId = cc.getExecutionInstanceID();
        context.put("sInstanceId", sInstanceId);
        context.put("cc", cc);
        context.put("gui", this);

        done = false;

        cc.startWebUIFragment(this);

        while (!cc.isFlowAborting() && !done)
            Thread.sleep(1000);

        if (cc.isFlowAborting())
            console.info("Flow abort requested - terminating component execution...");

        cc.stopWebUIFragment(this);

        // allow subclasses to determine what to push to the output
        subPushOutput();
    }

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    public void emptyRequest(HttpServletResponse response) throws WebUIException
    {
    	// TODO: it would be nice to see the request object here
    	generateContent(null, response);
    }

    @SuppressWarnings("unchecked")
    public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException
    {
        parameterMap = (Map<String, String[]>) request.getParameterMap();

        //
        // this is the workhorse method
        // process the input, determine any errors,
        // set up any output that will be pushed
        //
        try {
           if (!processRequest(request)) {
                // TODO: populate the velocity context with error objects
                // regenerate the template
                // TODO: put the request parameters or a wrapper in the context
               context.put("hasErrors", new Boolean(true));
               generateContent(request, response);
               return;
            }
        } catch(IOException ioe) {
            throw new WebUIException(ioe);
        }

        //
        // see if this component can handle multiple requests before
        // releasing the semaphore,
        if (expectMoreRequests(request)) return;

        // No Errors,
        // just push the browser to the "next" component
        try{
            // when the page has been handled, generate a browser refresh
            if (doRefresh) {
               generateMetaRefresh(response);
            }
         }catch (IOException e) {
            throw new WebUIException("unable to generate redirect response");
         }
         finally {
            done = true;
         }
    }

    //--------------------------------------------------------------------------------------------

    protected void subPushOutput() throws Exception
    {
        // default is to push the formInputName value
        // now push the output to the next component
        // allow subclasses to push a form value or ParameterParser object
        //
        String defaultOutput = parameterMap.get(formInputName)[0];
        //console.println("pushing " + defaultOutput);
        //console.flush();
        componentContext.pushDataComponentToOutput(OUT_OBJECT, defaultOutput);
    }

    //--------------------------------------------------------------------------------------------

    protected void generateContent(HttpServletRequest request, HttpServletResponse response) throws WebUIException
    {
        try {
            // request could be null on the "first" request
            context.put("request",  request);
            context.put("response", response);

            // render the template
            VelocityTemplateService velocity = VelocityTemplateService.getInstance();
            String html = velocity.generateOutput(context, templateName);

            // write the template to the response stream
            response.getWriter().println(html);

        } catch (Exception e) {
            throw new WebUIException(e);
        }
    }

    // exposed to the template
    public void setPushValue(String parameterName)
    {
    	formInputName = parameterName;
    }

    protected void generateMetaRefresh(HttpServletResponse response) throws IOException
    {
 	   PrintWriter writer = response.getWriter();
       writer.println("<html><head><title>Refresh Page</title>");
       writer.println("<meta http-equiv='REFRESH' content='0;url=/'></head>");
       writer.println("<body>Refreshing Page</body></html>");
    }

    //
    // not only check errors, process the input at this step
    // return false on errors, bad input, etc
    // return true if all is good
    protected boolean processRequest(HttpServletRequest request) throws IOException
    {
    	// server side form validation goes here
    	// any processing of the request parameters
    	return true;
    }

    protected boolean expectMoreRequests(HttpServletRequest request)
    {
    	// we are done, if the 'formInputName' is in the request
    	// e.g. ?done=true or whatever
    	// if the request does NOT contain this parameter
    	// we will assume, we are expecting more input
    	return (request.getParameter(formInputName) == null);
    }
}