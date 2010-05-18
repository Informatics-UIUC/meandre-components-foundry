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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.tools.text.io.GenericTemplate;
import org.seasr.meandre.support.components.geographic.GeoLocation;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;
import org.seasr.meandre.support.generic.io.JARInstaller;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

/**
 *
 * @author Mike Haberman  (DO NOT DELETE, I (Mike H is using this class)
 *
 */

/*
var cars = [
{ name:"chevrolet chevelle malibu", 
mpg:18, 
cylinders:8, 
displacement:307, 
horsepower:130, 
weight:3504, 
acceleration:12, 
year:70, 
origin:1},
{name:"buick skylark 320", mpg:15, cylinders:8, displacement:350, horsepower:165, weight:3693, acceleration:11.5, year:70, origin:1},
{name:"plymouth satellite", mpg:18, cylinders:8, displacement:318, horsepower:150, weight:3436, acceleration:11, year:70, origin:1},
{name:"amc rebel sst", mpg:16, cylinders:8, displacement:304, horsepower:150, weight:3433, acceleration:12, year:70, origin:1},
];
var flowers = 
[
    {
        "petallength": 1.4,
        "sepalwidth": 3.5,
        "class": "Iris-setosa",
        "sepallength": 5.1,
        "petalwidth": 0.2
    },
    {
        "petallength": 1.4,
        "sepalwidth": 3,
        "class": "Iris-setosa",
        "sepallength": 4.9,
        "petalwidth": 0.2
    },
    {
        "petallength": 1.3,
        "sepalwidth": 3.2,
        "class": "Iris-setosa",
        "sepallength": 4.7,
        "petalwidth": 0.2
    },
    {
        "petallength": 1.5,
        "sepalwidth": 3.1,
        "class": "Iris-setosa",
        "sepallength": 4.6,
        "petalwidth": 0.2
    }
 ];
    


*/

@Component(
        creator = "Mike Haberman",
        description = "Protovis Parallel Coordinates",
        name = "Parallel Coordinates",
        tags = "string, visualization, protovis",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.6.2-dep.jar" },
        resources  = { "protovis-r3.2.js, ParallelCoordinates.vm" }
)
public class ParallelCoordinates extends GenericTemplate 
{

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
	            name = "json",
	            description = "text output as JSON" +
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
	        description = "The template name",
	        name = GenericTemplate.PROP_TEMPLATE,
	        defaultValue = DEFAULT_TEMPLATE
	)
    protected static final String PROP_TEMPLATE = GenericTemplate.PROP_TEMPLATE;

    //--------------------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{
	    super.initializeCallBack(ccp);
	    
	    
	    String path = GenericTemplate.writeResourceFromJarToFilesystem(this.getClass(), 
	    		                                                       ccp.getPublicResourcesDirectory(), 
	    		                                                       "js", 
	    		                                                       "protovis-r3.2.js");
	    
	    console.info(path);
	    
	    context.put("title",   ccp.getProperty(PROP_TITLE));
	    context.put("addDone", true);
	    context.put("path", "/public/resources/" + path);

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
        
    	StringBuilder sb = new StringBuilder();
    	sb.append("petallength");
    	
    	context.put("data", json);
    	
    	
    	/*
    			  "cylinders":    {unit: ""},
    			  "displacement": {unit: " cubic inch"},
    			  "weight":       {unit: " lbs"},
    			  "horsepower":   {unit: " hp"},
    			  "acceleration": {unit: " (0 to 60mph)"},
    			  "mpg":          {unit: " miles/gallon"},
    			  "year":         {unit: ""}
    	*/
    		
    	
    	

		// context.put("geoList", geos);

		// console.info("Ready to view google maps with locations " + geos.size());

		//
    	// now wait for the user to access the webUI
		//
    	super.executeCallBack(cc);
    }

	@Override
	protected boolean processRequest(HttpServletRequest request) throws IOException {
	   return true;
	}

	
}
