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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 * @author Surya Kallumadi
 *
 */

@Component(
        creator = "Surya Kallumadi",
        description = "Performs simple tuple text replacement, based on input configuration",
        name = "Tuple Text Replacement",
        tags = "Tuple text, remove, replace",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TupleTextReplacement extends AbstractExecutableComponent{

	//------------------------------ INPUTS ------------------------------------------------------


    @ComponentInput(
            description = "mapData format: newText = {old1, old2, old3}; newText2 = {old4,old5}; newText3=old6; = deleteText" +
                " If you need to use an equals sign, use := to separate values (e.g.  newtext=blah := {old1=A,old2} )" +
                "Note this replacement does NOT use regular expressions and is token based.  Hence it will attemp to do " +
                "matching based on whole tokens (not prefixes, suffix, parts)" +
                "see Text Cleaner for a component that uses regular expressions." +
            "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object",
            name = "mapData"
    )
    protected static final String IN_MAP_DATA = "mapData";

    @ComponentInput(
			name = Names.PORT_TUPLES,
			description = "set of tuples" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;

	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of tuples (same as input)" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (same as input)" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;


    //------------------------------ PROPERTIES --------------------------------------------------
    @ComponentProperty(
			name = "ignoreCase",
			description = "ignore letter case of the matched text",
		    defaultValue = "true"
		)
	protected static final String PROP_IGNORE_CASE = "ignoreCase";

    //--------------------------------------------------------------------------------------------


    boolean ignoreCase = true;
    Map<String,String> dictionary;
    Map<String,String> phraseReplaceDictionary;
    //--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name = "columnSet",
			description = "optional, specifiy a subset of fields to print (e.g 1,3,5) ",
		    defaultValue = "3"
	)
	protected static final String PROP_COL = "columnSet";

	List<Integer> idxList = null;
	String[] values = null;

    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception
    {
		String ic = ccp.getProperty(PROP_IGNORE_CASE).trim();
		ignoreCase = Boolean.valueOf(ic);
		console.info(PROP_IGNORE_CASE + " " + ignoreCase);

		String cols = ccp.getProperty(PROP_COL);
		if (cols != null && cols.trim().length() > 0) {

			idxList = new ArrayList<Integer>();
			StringTokenizer tokens = new StringTokenizer(cols.trim(), ",");
			while (tokens.hasMoreTokens()) {
				int i = Integer.parseInt(tokens.nextToken());
				idxList.add(new Integer(i));
			}

			values = new String[idxList.size()];
		}
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {

		if (dictionary == null) {

			Strings input = (Strings) cc.getDataComponentFromInput(IN_MAP_DATA);
			String[] val = BasicDataTypesTools.stringsToStringArray (input);

			dictionary = buildDictionary(val[0]);
			phraseReplaceDictionary = new HashMap<String,String>();
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

			/*
			for (String key : dictionary.keySet()) {
				String v = dictionary.get(key);
				console.info(key + "-->" + v);
			}
			*/
		}
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = tuplePeer.createTuple();
		StringsArray tinput = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(tinput);
		List<Strings> output = new ArrayList<Strings>();
		for (int i = 0; i < in.length; i++)
		{
			tuple.setValues(in[i]);

			if (idxList == null)
			{
			   //console.info(tuple.toString());
			   //console.info("idx empty---------");
			}

			else {
				//console.info("index 1 --"+tuplePeer.getFieldNameForIndex(1)+">index 2 --"+tuplePeer.getFieldNameForIndex(2)+"index 3 --"+tuplePeer.getFieldNameForIndex(3)+"index 0 --"+tuplePeer.getFieldNameForIndex(0));
				for (int j = 0; j < idxList.size(); j++) {
					int idx = idxList.get(j);
					if (idx < tuplePeer.size()) {
						String text = tuple.getValue(idx);

						String key = text;
						//if (ignoreCase)
						{
							key = key.toLowerCase();
						}
						// console.info("look at " + key);
						String r = dictionary.get(key);
						if(r==null)
						{
							r=text;
						}

						//console.info("replaced >"+text+" with >"+r);
						//sb.append(r);
						tuple.setValue(idx,r);
						//tuple.setValue(idx,r);

						//tinput[idx]=r;
                       // console.info(tuple.toString());

					}
					else {
						console.info("WARNING, index beyond tuple field");
					}
				}

				//console.info(SimpleTuplePeer.toString(values));
			}
			output.add(tuple.convert());
		}



		 Strings[] results = new Strings[output.size()];
	        output.toArray(results);

	        StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
	     //   StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(tuple.convert());
	       // console.info(tinput.toString());
	        console.finest(outputSafe.toString());
	        cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);







		//
		// Option B: use Pattern (RegEx) and just do a replaceAll or match
		// not implemented, yet
		//

		// push the output
//		cc.pushDataComponentToOutput(OUT_TUPLES, output);
		cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
		// console.info(sb.toString());
	}

	String cleanPhrases(String text)
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

    @Override
    public void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_META_TUPLE, IN_TUPLES })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_META_TUPLE));
        componentContext.pushDataComponentToOutput(OUT_TUPLES, componentContext.getDataComponentFromInput(IN_TUPLES));
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_META_TUPLE, IN_TUPLES })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_META_TUPLE));
        componentContext.pushDataComponentToOutput(OUT_TUPLES, componentContext.getDataComponentFromInput(IN_TUPLES));
    }

    //--------------------------------------------------------------------------------------------

    public Map<String,String> buildDictionary(String configData)
	{
		configData = configData.replaceAll("\n","");
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
	            //console.info("mapping " + value + " to " + key);
	        }
	    }
	    return map;
	}
}
