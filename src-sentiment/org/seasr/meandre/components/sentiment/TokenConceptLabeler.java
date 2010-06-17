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

package org.seasr.meandre.components.sentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.apps.sentiment.PathMetric;
import org.seasr.meandre.support.components.apps.sentiment.PathMetricFinder;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.components.utils.FileResourceUtility;

/**
 *
 * @author Mike Haberman;
 *
 */

/*
 * ASSUMES: the incoming tuples have a field named "token"
 * the value of this field is used to find a concept (using SynNet)
 * that matches closest to the concept properties specified
 *
 * the output is the same as the input except an additional field to the
 * tuple is added: concept.
 *
 * example flow:  sentence detect -> tokenizer -> posTagger --> TokenConceptLabeler
 * NOTE:  it will help to filter out the tuples that do not occur very often
 * You can do this by using TupleValueFrequencyCounter component
 * then filter out those below a certain threshold BEFORE labeling
 *
 * NOTE: this component uses the SynNet service to label tokens,
 * it is NOT fast and can take a very long time to process large bodies of text
 * especially if you don't heed the warning given above
 *
 */

@Component(
		name = "Token Concept Labeler",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, text",
		description = "This component labels a tuple field value with a concept " ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TokenConceptLabeler extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

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
			description = "set of filtered tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (same as input, concept)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
	        description = "synNet service url (include http://)",
			name = "host",
			defaultValue = "http://services.seasr.org/synnet/"
	)
	protected static final String DATA_PROPERTY_HOST = "host";

	@ComponentProperty(
	        description = "concepts",
	        name = "concepts",
	        defaultValue = "love={lovable};anger={hateful,angry}"
	)
    protected static final String DATA_PROPERTY_CONCEPTS = "concepts";

	@ComponentProperty(
	        description = "optional filename of cached concepts to use instead of synNet host",
	        name = "conceptCacheFile",
	        defaultValue = ""
	)
    protected static final String DATA_PROPERTY_CACHE = "conceptCacheFile";

	@ComponentProperty(
	        description = "optional filename of tokens to ignore, words with no concepts",
	        name = "ignoreTokensFile",
	        defaultValue = ""
	)
    protected static final String DATA_PROPERTY_IGNORE = "ignoreTokensFile";

	@ComponentProperty(
	        description = "optional filename of tokens to remap, use to change spellings, etc",
	        name = "remapFile",
	        defaultValue = ""
	)
    protected static final String DATA_PROPERTY_WORDMAP = "remapFile";

	@ComponentProperty(
	        description = "field name for the key field of incoming tuples, its value will be used to label",
	        name = "key",
	        defaultValue = "token"
	)
	protected static final String DATA_PROPERTY_FIELDNAME_KEY = "key";

    //--------------------------------------------------------------------------------------------

	static String NO_VALUE = "N.A.";
	String keyFieldName = "token";

    HashMap<String,List<String>> conceptMap;
	HashMap<String,String> reverseMap;
	List<String> allLabels;

	String cacheFileName;
	String noConceptFileName;
	String wordMapFileName;

	Map<String,String> wordToConceptMap;
	Map<String,String> noConceptMap;
	Map<String,String> wordMap;

	PathMetricFinder finder;

    //--------------------------------------------------------------------------------------------

	public Map<String,String> buildCacheMap(ComponentContextProperties ccp,
			                                String key,
			                                StringBuilder filenameOut)
	{
		filenameOut.setLength(0);
		String filename = ccp.getProperty(key);
		if (filename == null || filename.trim().length() == 0) {
			Map<String,String> map = new HashMap<String,String>();
			return map;
		}

		// key exists, use the given filename
		String defaultDir = ccp.getPublicResourcesDirectory();
		filename   = FileResourceUtility.buildResourcePath(defaultDir, filename);
		FileResourceUtility.createPathToResource(filename, console);

		Map<String,String> map = readFromFile(filename);
		filenameOut.append(filename);
		return map;
	}

	private String host;
	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception
    {
		this.keyFieldName = ccp.getProperty(DATA_PROPERTY_FIELDNAME_KEY);

		//
		// init the synNet host
		//
		this.host   = ccp.getProperty(DATA_PROPERTY_HOST);
		this.finder = new PathMetricFinder(host);


		// create/build the cache file name
		StringBuilder sb = new StringBuilder();

		this.wordToConceptMap = buildCacheMap(ccp, DATA_PROPERTY_CACHE, sb );
		this.cacheFileName = sb.toString();

		this.wordMap = buildCacheMap(ccp, DATA_PROPERTY_WORDMAP, sb );
		this.wordMapFileName = sb.toString();

		this.noConceptMap = buildCacheMap(ccp, DATA_PROPERTY_IGNORE, sb);
		this.wordMapFileName  = sb.toString();


		/*
		this.cacheFileName     = FileResourceUtility.buildResourcePath(defaultDir, ccp.getProperty(DATA_PROPERTY_CACHE));
		this.wordMapFileName   = FileResourceUtility.buildResourcePath(defaultDir, ccp.getProperty(DATA_PROPERTY_WORDMAP));
		this.noConceptFileName = FileResourceUtility.buildResourcePath(defaultDir, ccp.getProperty(DATA_PROPERTY_IGNORE));


		FileResourceUtility.createPathToResource(cacheFileName,     console);
		FileResourceUtility.createPathToResource(wordMapFileName,   console);
		FileResourceUtility.createPathToResource(noConceptFileName, console);

		this.wordToConceptMap = readFromFile(cacheFileName);
		this.noConceptMap     = readFromFile(noConceptFileName);
		this.wordMap          = readFromFile(wordMapFileName);
		*/

		//
		// build the map for concepts
		//
		String toParse = ccp.getProperty(DATA_PROPERTY_CONCEPTS);
        conceptMap = new HashMap<String,List<String>>();
        reverseMap = new HashMap<String,String>();
        allLabels = new ArrayList<String>();

        StringTokenizer tokens = new StringTokenizer(toParse, ";");
        while (tokens.hasMoreTokens()){
        	String kv = tokens.nextToken();
        	int idx = kv.indexOf('=');
        	if (idx > 0) {
        		String key   = kv.substring(0,idx).trim();  /// concept = {a,b,c}
        		String value = kv.substring(idx+1).replaceAll("[}{]", "");
        		StringTokenizer labels = new StringTokenizer(value, ",");

        		ArrayList<String> labelList = new ArrayList<String>();
        		while(labels.hasMoreTokens()) {
        			String label = labels.nextToken().trim();
        			// console.fine("MAP value " + label + ":" + key);
        			allLabels.add(label);
        			labelList.add(label);
        			reverseMap.put(label, key);
        		}
        		conceptMap.put(key, labelList);
        	}
        }

	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		// TODO: pull from properties

		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer inPeer  = new SimpleTuplePeer(inputMeta);
		SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, new String[]{"concept"});

		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

		SimpleTuple tuple    = inPeer.createTuple();
		SimpleTuple outTuple = outPeer.createTuple();

		int TOKEN_IDX   = inPeer.getIndexForFieldName(keyFieldName);
		int CONCEPT_IDX = outPeer.getIndexForFieldName("concept");

		if (TOKEN_IDX == -1) {
			throw new ComponentExecutionException("incoming tuple has no field named " + keyFieldName);
		}

		console.fine("tuple count to label " + in.length);

		int valuesWritten = 0;
		List<Strings> output = new ArrayList<Strings>();
		for (int i = 0; i < in.length; i++) {

			tuple.setValues(in[i]);
			String token = tuple.getValue(TOKEN_IDX);

			token = reMap(token);

			String concept = wordToConceptMap.get(token);
			if (concept == null) {

				if (noConceptMap.get(token) != null) {
					// word has no concept
					continue;
				}


			   console.fine("need to label " + token);
			   List<PathMetric> all = finder.getAllMetric(token, allLabels);
			   if (all == null) {
			    	console.info("Unable to label, service down " + this.host);
			    	break;
			   }

			   PathMetric metric    = finder.getBestMetric(all);
			   if (metric != null) {
			      concept = reverseMap.get(metric.end);
			      console.fine(token + " ==> " + concept);
			      wordToConceptMap.put(token, concept);
			      // console.fine(metric.toString());
			      // label the tuple and save it

			      valuesWritten++;
			   }
			   else {
				   noConceptMap.put(token, NO_VALUE);
				   valuesWritten++;
			   }
			}

			if (concept != null) {
				outTuple.setValue(tuple);
				outTuple.setValue(CONCEPT_IDX, concept);
				output.add(outTuple.convert());
			}


			// temp. flush
			if (valuesWritten%10 == 0) {
				writeToFile(wordToConceptMap, cacheFileName);
		    	writeToFile(noConceptMap, noConceptFileName);
			}
		}

		//
		// push the whole collection, protocol safe
		//

		Strings[] results = new Strings[output.size()];
		output.toArray(results);

		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	    //
		// metaData for this tuple producer
		//
	    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());

	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception
    {
    	writeToFile(wordToConceptMap, cacheFileName);
    	writeToFile(noConceptMap,     noConceptFileName);
    }

    //--------------------------------------------------------------------------------------------

    // change things like hau'ted to haunted
    protected String reMap(String word)
    {
        String replace = wordMap.get(word);
        if (replace != null)
           return replace;
        return word;
    }

    private void writeToFile(Map<String,String>map, String filename) {

    	if (filename == null || filename.length() == 0) {
    		return;
    	}


    	File file = new File(filename);
    	if (! file.exists()) {
    		console.fine(filename + " does not exist");
    	}

		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
	        Iterator<String> it = map.keySet().iterator();
	        while(it.hasNext()) {
	        	String key = it.next();
	        	String concept = map.get(key);
	        	out.write(key +"," + concept + "\n");
	        }
	        out.close();
	    } catch (IOException e) {}
	}

	private Map<String,String> readFromFile(String filename)
	{
		Map<String,String> map = new HashMap<String,String>();

		try {
			File file = new File(filename);
			console.fine("reading from " + filename);

			if (file.exists()) {

				BufferedReader input =  new BufferedReader(new FileReader(file));
			      try {
			        String line = null;
			        /*
			        * readLine is a bit quirky :
			        * it returns the content of a line MINUS the newline.
			        * it returns null only for the END of the stream.
			        * it returns an empty String if two newlines appear in a row.
			        */

			        while (( line = input.readLine()) != null){

			        	// word,concept (word is the key, concept is the value)
			        	int idx = line.indexOf(',');
			        	String word    = line.substring(0,idx).trim();
			        	String concept = line.substring(idx+1).trim();

			        	map.put(word,concept);
			        }

			      }
			      finally {
			        input.close();
			      }
			}
			else {
				// no file exists to read
				console.fine("file is empty: " + filename);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return map;
	}
}

/*
Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
String[] meta = DataTypeParser.parseAsString(inputMeta);
String fields = meta[0];
DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
DynamicTuplePeer outPeer = new DynamicTuplePeer(inPeer, new String[]{"concept"});

Strings input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
String[] tuples = DataTypeParser.parseAsString(input);
DynamicTuple tuple = inPeer.createTuple();
*/

