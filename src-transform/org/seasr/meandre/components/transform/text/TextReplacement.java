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
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

//
// FORMAT:  newText = {old1, old2, old3}; newText2 = {old4,old5}; newText3=old6"
// newValue = {oldValueA, oldValueB} OR newValue=oldValue
//
// lines are separated with ';'
// {} are optional
// ',' separate the values

/**
 * @author Mike Haberman
 * @author Boris Capitanu
 */

@Component(
        creator = "Mike Haberman",
        description = "Performs simple text replacement, based on input configuration",
        name = "Text Replacement",
        tags = "#TRANSFORM, text, remove, replace",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TextReplacement extends AbstractExecutableComponent {

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
            description = "mapData format: newText = {old1, old2, old3}; newText2 = {old4,old5}; newText3=old6; = deleteText" +
                " If you need to use an equals sign, use := to separate values (e.g.  newtext=blah := {old1=A,old2} )" +
                "Note this replacement does NOT use regular expressions and is token based.  Hence it will attemp to do " +
                "matching based on whole tokens (not prefixes, suffix, parts)" +
                "see Text Cleaner for a component that uses regular expressions." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings",
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

    //--------------------------------------------------------------------------------------------


    boolean ignoreCase = true;


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		String ic = ccp.getProperty(PROP_IGNORE_CASE).trim();
		ignoreCase = Boolean.valueOf(ic);
		console.info(PROP_IGNORE_CASE + " " + ignoreCase);
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
	    String[] val = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_MAP_DATA));

	    Map<String,String> dictionary = buildDictionary(val[0], console);
	    Map<String,String> phraseReplaceDictionary = new HashMap<String,String>();
	    for (String key : dictionary.keySet()) {
	        if (key.indexOf(" ") > 1) {
	            String value = dictionary.get(key);
	            phraseReplaceDictionary.put(key, value);
	        }
	    }
	    for (String key : phraseReplaceDictionary.keySet()) {
	        dictionary.remove(key);
	    }


	    if (ignoreCase) {
	        HashMap<String,String>tmp = new HashMap<String,String>();
	        for (String key : dictionary.keySet()) {
	            String v = dictionary.get(key);
	            tmp.put(key.toLowerCase(),v);
	        }
	        dictionary = tmp;
	    }

	    
			for (String key : dictionary.keySet()) {
				String v = dictionary.get(key);
				console.finest("mapping " + key + " --> " + v);
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
		// adding a few more characters that should mark an end of a word for replacement
		StringTokenizer tokens = new StringTokenizer(text, " \t\n\r\f.,;!?\"\':(){}[]*-<>~", true);
		StringBuilder sb = new StringBuilder();
		while(tokens.hasMoreTokens()) {

			String t = tokens.nextToken();
			String key = t;
			if (ignoreCase) {
				key = key.toLowerCase();
			}
			// console.info("look at " + key);
			String r = dictionary.get(key);
			r = (r == null) ? (t) : (r);
			sb.append(r);
		}

		text = cleanPhrases(sb.toString(), phraseReplaceDictionary);




		//
		// Option B: use Pattern (RegEx) and just do a replaceAll or match
		// not implemented, yet
		//

		// push the output
		cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));

		// console.info(sb.toString());
	}

	String cleanPhrases(String text, Map<String,String> phraseReplaceDictionary)
	{
		for (String key : phraseReplaceDictionary.keySet()) {
			String replace = phraseReplaceDictionary.get(key);

			//
			// TODO: for ignoreCase, you have to make a new RegEx
			// out of key such that case is ignored
			//
			text = text.replaceAll(key, replace);
		}
		return text;
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}

    //--------------------------------------------------------------------------------------------

    public static Map<String,String> buildDictionary(String configData, Logger console)
	{
		configData = configData.replaceAll("\r\n", "\n").replaceAll("\n", "");
	    Map<String,String> map = new HashMap<String,String>();
	    StringTokenizer tokens = new StringTokenizer(configData,";");
	    while (tokens.hasMoreTokens()) {
	        String line = tokens.nextToken();
	        String[] parts = line.split("=");

	        if (parts.length != 2) {            // lots of = signs
	        	parts = line.split(":=");
	        }

	        if (parts.length != 2) {
	        	console.warning("unable to build dictionary " + configData);
	        	return map;
	        }

	        String key    = parts[0].trim();
	        String values = parts[1].trim();

	        // if (ignoreCase) {values = values.toLowerCase();}

	        values = values.replace("{","");
	        values = values.replace("}","");
	        StringTokenizer vTokens = new StringTokenizer(values,",");
	        while(vTokens.hasMoreTokens()) {
	            String value = vTokens.nextToken().trim();
	            // this is a reverse map
	            // e.g. the KEY is the value, the value is the key
	            map.put(value, key);
	            console.finest("mapping " + value + " --> " + key);
	        }
	    }
	    return map;
	}
}
