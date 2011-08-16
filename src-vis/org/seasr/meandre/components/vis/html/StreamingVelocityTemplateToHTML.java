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

package org.seasr.meandre.components.vis.html;

import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

/**
 * @author Mike Haberman
 */

/*
 * NOTES:  This component is meant to be subclassed.  The subclass will add an input (the
 * data needed to do the transform.  The subclass will also provide a template name via
 * a property.
 *
 * See GenericTemplate.java for notes on how the template is loaded/found
 *
 */
@Component(
        creator = "Mike Haberman",
        description = "Generates output (html) via Velocity using a template",
        name = "Velocity Template To HTML",
        rights = Licenses.UofINCSA,
        tags = "velocity,template,html",
        baseURL="meandre://seasr.org/components/foundry/",
        resources  = { "SampleTemplate.vm" },
        dependency = { "velocity-1.7-dep.jar", "protobuf-java-2.2.0.jar" }
)
public abstract class StreamingVelocityTemplateToHTML extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	//
	// subclasses should provide the necessary inputs
	//

	//------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "The template name",
	        name = Names.PROP_TEMPLATE,
	        defaultValue = "org/seasr/meandre/components/vis/html/SampleTemplate.vm"
	)
	protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;

	@ComponentProperty(
	        description = "User supplied property list (key=value comma-separated pairs)",
	        name = Names.PROP_PROPERTIES,
	        defaultValue = ""
	)
	protected static final String PROP_TEMPLATE_PROPERTIES = Names.PROP_PROPERTIES;

   //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_HTML,
            description = "Text containing the transformed input to html via a velocity template" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_HTML = Names.PORT_HTML;

    //--------------------------------------------------------------------------------------------


	protected VelocityContext context;
    protected String templateName;

    // convenience properties to easily push additional properties
    // not needed, template can always do $ccp.getProperty("title")
    protected String[] templateVariables = {};


    //--------------------------------------------------------------------------------------------

    // subclasses should call super.initializeCallback BEFORE
    // they add component specific data to the context
    //
    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        templateName = getPropertyOrDieTrying(PROP_TEMPLATE, ccp);

        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        context = velocity.getNewContext();

        /*
         *  Make a context object and populate with the data.  This
         *  is where the Velocity engine gets the data to resolve the
         *  references (ex. $date) in the template
         */

        context.put("_dir",  System.getProperty("user.dir"));
        context.put("_date", new Date());
        context.put("_ccp",  ccp);

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
        context.put("_userMap", map);

        // push property values to the context
        for (String name: templateVariables) {
            String value = ccp.getProperty(name);
            context.put(name,value);
        }
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	//
    	// subclasses should call super.executeCallback()
    	// AFTER they process and put any necessary data in the context
    	//

    	String sInstanceId = cc.getExecutionInstanceID();
        context.put("_sInstanceId", sInstanceId);
        context.put("_cc", cc);
        context.put("_converter", this);

        // render the template
        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        String html = velocity.generateOutput(context, templateName);
 		cc.pushDataComponentToOutput(OUT_HTML, html);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        context = null;
    }
}
