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

package org.seasr.meandre.components.transform.text;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;

@Component(
        creator = "Mike Haberman",
        description = "Performs simple text replacement, based on input configuration",
        name = "Simple Text Cleaner",
        tags = "text, remove, replace",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class SimpleTextCleaner extends AbstractExecutableComponent{

	//------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The text to be cleaned" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object",
            name = Names.PORT_TEXT
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;
    
    @ComponentInput(
            description = "configuation map format: newText = {old1, old2, old3}; newText2 = {old4,old5}; newText3=old6; = deleteText" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object",
            name = "mapData"
    )
    protected static final String IN_MAP_DATA = "mapData";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The cleaned text" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings",
            name = Names.PORT_TEXT
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------
    @ComponentProperty(
			name = "ignoreCase",
			description = "ignore letter case of the matched text",
		    defaultValue = "true"
		)
	protected static final String PROP_IGNORE_CASE = "ignoreCase";
    
    //
    // FORMAT:  newText = {old1, old2, old3}; newText2 = {old4,old5}; newText3=old6"
    // newValue = {oldValueA, oldValueB} OR newValue=oldValue
    //
    // lines are separated with ';'
    // {} are optional
    // ',' separate the values
   
    public  Map<String,String> buildDictionary(String configData, boolean ignoreCase) 
    {
    	Map<String,String> map = new HashMap<String,String>();
    	StringTokenizer tokens = new StringTokenizer(configData,";");
    	while (tokens.hasMoreTokens()) {
    		String line = tokens.nextToken();
    		String[] parts = line.split("=");
    		String key    = parts[0].trim();
    		String values = parts[1].trim();
    		
    		if (ignoreCase) {
    			values = values.toLowerCase();
    		}
    		
    		values = values.replace("{","");
    		values = values.replace("}","");
    		StringTokenizer vTokens = new StringTokenizer(values,",");
    		while(vTokens.hasMoreTokens()) {
    			String value = vTokens.nextToken().trim();
    			// this is a reverse map
    			// e.g. the KEY is the value, the value is the key
    			map.put(value, key);
    			// console.info("mapping " + value + " to " + key);
    		}
    	}
    	return map;
    }
    
    boolean ignoreCase = true;
    
	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
    {
		String ic = ccp.getProperty(PROP_IGNORE_CASE).trim();
		ignoreCase = Boolean.valueOf(ic);
		console.info(PROP_IGNORE_CASE + " " + ignoreCase);
	}

	Map<String,String> dictionary;
	
	@Override
    public void executeCallBack(ComponentContext cc) throws Exception 
    {
		if (dictionary == null) {
			
			Strings input = (Strings) cc.getDataComponentFromInput(IN_MAP_DATA);
			String[] val = BasicDataTypesTools.stringsToStringArray (input);
			
			dictionary = buildDictionary(val[0], ignoreCase);
			
		}
		
		String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];
		
		
		//
		// TODO: measure for speed:  cycle through the dictionary keys, doing a replaceAll 
		// OR cycle through the tokens, do an individual lookup/replace
		//
		
		//
		// Option A, parse the text based on whitespace and punctuation
		// cycle through these tokens, match against the dictionary
		// we want tokens that typically mark an end of a word
		StringTokenizer tokens = new StringTokenizer(text, " \t\n\r\f.,;!?\"\':()", true);
		StringBuilder sb = new StringBuilder();
		while(tokens.hasMoreTokens()) {
			
			String t = tokens.nextToken();
			String key = t;
			if (ignoreCase) {
				key = key.toLowerCase();
			}
			String r = dictionary.get(key);			
			r = (r == null ? (t) : (r));
			sb.append(r);
		}
		
		
		//
		// Option B: use Pattern (RegEx) and just do a replaceAll or match
		// not implemented, yet
		//
		
		// push the output
		cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(sb.toString()));
		
		// console.info(sb.toString());
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
