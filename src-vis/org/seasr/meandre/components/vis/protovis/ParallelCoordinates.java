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

package org.seasr.meandre.components.vis.protovis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.meandre.components.vis.html.VelocityTemplateToHTML;
import org.seasr.meandre.support.components.utils.ComponentUtils;


/**
 *
 * @author Mike Haberman
 *
 */



@Component(
        creator = "Mike Haberman",
        description = "Protovis Parallel Coordinates",
        name = "Parallel Coordinates",
        tags = "string, visualization, protovis",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.6.2-dep.jar" },
        resources  = { "protovis-r3.2.js", "ParallelCoordinates.vm" }
)
public class ParallelCoordinates extends VelocityTemplateToHTML
{

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
	            name = "json",
	            description = "JSON input data.  Must be an array of fields e.g. [{a:1,b:2,c:3}, {a:4,b:5,c:6}]" +
	            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	    )
	    protected static final String IN_JSON = "json";


	static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/protovis/ParallelCoordinates.vm";

    //------------------------------ PROPERTIES --------------------------------------------------

	//
	// specific to this component
	//
	@ComponentProperty(
	        description = "The title for the page",
	        name = Names.PROP_TITLE,
	        defaultValue = "Parallel Coordinates"
	)
	protected static final String PROP_TITLE = Names.PROP_TITLE;


	@ComponentProperty(
	        description = "The attribute that will be highlighted",
	        name = "active",
	        defaultValue = ""
	)
	protected static final String PROP_ACTIVE = "active";


	@ComponentProperty(
	        description = "The template name",
	        name = VelocityTemplateToHTML.PROP_TEMPLATE,
	        defaultValue = DEFAULT_TEMPLATE
	)
    protected static final String PROP_TEMPLATE = VelocityTemplateToHTML.PROP_TEMPLATE;

    //--------------------------------------------------------------------------------------------

	protected static final String PROTOVIS_JS = "protovis-r3.2.js";

    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception
	{
	    super.initializeCallBack(ccp);

	    ComponentUtils.writePublicResource(getClass(), PROTOVIS_JS, "js", ccp, false);
	    
	    /*
	    String path = VelocityTemplateToHTML.writeResourceFromJarToFilesystem(this.getClass(),
                ccp.getPublicResourcesDirectory(),
                "js",
                PROTOVIS_JS);
                */

	    context.put("title",   ccp.getProperty(PROP_TITLE));
	    context.put("path",    "/public/resources/js/" + PROTOVIS_JS);


	    String a = ccp.getProperty(PROP_ACTIVE).trim();
	    context.put("active", a);
	}


    @Override
    public void executeCallBack(ComponentContext cc) throws Exception
    {
    	//
    	// fetch the input, push it to the context
    	//

    	Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_JSON);
    	String[] data = BasicDataTypesTools.stringsToStringArray(inputMeta);
    	String json = data[0];

        Map<String,String> unitMap;
    	try {

    	   unitMap = parseForFields(json);

    	   String active = (String) context.get("active");
    	   if (active.length() == 0) {
    		   // choose one of the attributes
    		   active = unitMap.keySet().iterator().next();
    		   context.put("active", active);
    	   }

    	}catch (JSONException e) {

    		console.info("Json parsing error");

            unitMap = new HashMap<String,String>();
            context.put("errorMsg", "There was an error in the data");


    	}


    	context.put("data", json);
    	context.put("unitMap", unitMap);
    	
        // let velocity take over
    	super.executeCallBack(cc);
    }
    
    @Override
    protected void handleStreamInitiators() throws Exception {
        StreamInitiator si = (StreamInitiator)componentContext.getDataComponentFromInput(IN_JSON);
        componentContext.pushDataComponentToOutput(OUT_TEXT, si);
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        StreamTerminator st = (StreamTerminator)componentContext.getDataComponentFromInput(IN_JSON);
        componentContext.pushDataComponentToOutput(OUT_TEXT, st);
    }
    
 	/*
	  "cylinders":    {unit: ""},
	  "displacement": {unit: " cubic inch"},
	  "weight":       {unit: " lbs"},
	  "horsepower":   {unit: " hp"},
	  "acceleration": {unit: " (0 to 60mph)"},
	  "mpg":          {unit: " miles/gallon"},
	  "year":         {unit: ""}
*/

	@SuppressWarnings("unchecked")
	public Map<String,String>  parseForFields(String jsonData)
	throws JSONException
	{

		HashMap map = new HashMap<String,String>();
		HashMap<String, List<String>> categories = new HashMap<String,List<String>>();

		// JSONObject json   = new JSONObject(jsonData);
		JSONArray results = new JSONArray(jsonData);
		int size = results.length();

		for (int i = 0; i < size;i++) {

			JSONObject fields = results.getJSONObject(i);

			Iterator it = fields.keys();
		    while(it.hasNext()) {

		    	String key = (String) it.next();
		    	String value = fields.getString(key);

		    	if (! map.containsKey(key)) {

		    		try {
		    			Double d = Double.parseDouble(value);
		    			map.put(key, "");
		    		}
		    		catch (Exception e) {

		    			//
		    			// category data
		    			//
		    			List list = categories.get(key);
		    			if (list == null) {
		    				list = new ArrayList<String>();
		    				categories.put(key, list);
		    			}
		    			if (!list.contains(value)) {
		    				list.add(value);
		    			}

		    		}

		    	}

				//console.info(key + " " + value);
			}

		}

		// TODO:  fix template to handle category data or attach a legend
		// now remap all the category data to numerical values
		//

		return map;
	}


}
