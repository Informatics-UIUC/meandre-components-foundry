package org.seasr.meandre.components.sentiment;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.StringTokenizer;



import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.BasicDataTypes;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsArray;
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;

import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

import org.seasr.meandre.support.sentiment.PathMetric;
import org.seasr.meandre.support.sentiment.PathMetricFinder;


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
		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
		description = "This component labels a tuple field value with a concept " ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TokenConceptLabeler  extends AbstractExecutableComponent {
	

    //------------------------------ INPUTS ------------------------------------------------------
	
	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "set of tuples"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;
	
	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
	

    //------------------------------ OUTPUTS -----------------------------------------------------
	
	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of filtered tuples"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
	
	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (same as input, concept)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
	
	
	
	//----------------------------- PROPERTIES ---------------------------------------------------
	@ComponentProperty(description = "synNet host",
			   name = "host",
			   defaultValue = "http://services.seasr.org/synnet/")
	protected static final String DATA_PROPERTY_HOST = "host";
	 
	 
   @ComponentProperty(description = "concepts",
		   name = "concepts",
		   defaultValue = "love={lovable};anger={hateful,angry}")
    protected static final String DATA_PROPERTY_CONCEPTS = "concepts";
      
   
   @ComponentProperty(description = "filename of cached concepts to use instead of synNet host",
           name = "conceptCacheFile",
           defaultValue = "posConceptCache.csv")
    protected static final String DATA_PROPERTY_CACHE = "conceptCacheFile";
   
   @ComponentProperty(description = "filename of tokens to ignore, words with no concepts",
           name = "ignoreTokensFile",
           defaultValue = "ignore.csv")
    protected static final String DATA_PROPERTY_IGNORE = "ignoreTokensFile";
   

   @ComponentProperty(description = "filename of tokens to remap, use to change spellings, etc",
           name = "remapFile",
           defaultValue = "remap.csv")
    protected static final String DATA_PROPERTY_WORDMAP = "remapFile";
  
   
   static String NO_VALUE = "N.A.";
   
   
	//--------------------------------------------------------------------------------------------

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
	
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{
		//
		// init the synNet host
		//
		String host   = ccp.getProperty(DATA_PROPERTY_HOST);
		this.finder = new PathMetricFinder(host);
		
		
		// create/build the cache file name
		this.cacheFileName     = ccp.getProperty(DATA_PROPERTY_CACHE);
		this.wordMapFileName   = ccp.getProperty(DATA_PROPERTY_WORDMAP);
		this.noConceptFileName = ccp.getProperty(DATA_PROPERTY_IGNORE);
		
		this.wordToConceptMap = readFromFile(cacheFileName);
		this.noConceptMap     = readFromFile(noConceptFileName);
		this.wordMap          = readFromFile(wordMapFileName);
		
		
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
        			// console.info("MAP value " + label + ":" + key);
        			allLabels.add(label);
        			labelList.add(label);
        			reverseMap.put(label, key);
        		}
        		conceptMap.put(key, labelList);
        	}
        }
        
	}
	
	// change things like hau'ted to haunted
	protected String reMap(String word)
	{
		String replace = wordMap.get(word);
		if (replace != null)
		   return replace;
		return word;
	}

	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		// TODO: pull from poperties
		
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
		
		
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer inPeer  = new SimpleTuplePeer(inputMeta);
		SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, new String[]{"concept"});	
		
		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
		
		SimpleTuple tuple    = inPeer.createTuple();
		SimpleTuple outTuple = outPeer.createTuple();
		
		int TOKEN_IDX   = inPeer.getIndexForFieldName("token");
		int CONCEPT_IDX = outPeer.getIndexForFieldName("concept");
		
		console.info("tuple count to label " + in.length);
		
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
				
			
			   console.info("need to label " + token);
			   List<PathMetric> all = finder.getAllMetric(token, allLabels);
			   if (all == null) {
			    	console.info("Unable to label, service down");
			    	break;
			   }
			
			   PathMetric metric    = finder.getBestMetric(all);
			   if (metric != null) {
			      concept = reverseMap.get(metric.end);
			      console.info(token + " ==> " + concept);
			      wordToConceptMap.put(token, concept);
			      // console.info(metric.toString());
			      // label the tuple and save it
			   }
			   else {
				   noConceptMap.put(token, NO_VALUE);
			   }
			}
			
			if (concept != null) {
				outTuple.setValue(tuple);
				outTuple.setValue(CONCEPT_IDX, concept);
				output.add(outTuple.convert());
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

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception 
    {
    	writeToFile(wordToConceptMap, cacheFileName);
    	writeToFile(noConceptMap, noConceptFileName);
    }
    
    
    private void writeToFile(Map<String,String>map, String filename) {
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
			console.info("reading from " + filename);
			
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
				console.info("file is empty: " + filename);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
}
