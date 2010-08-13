package org.seasr.meandre.components.nlp.gazetteer;




import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.io.StreamUtils;

import com.ontotext.gate.gazetteer.*;



/*
 * simple wrapper class to process text into tuples using Stanford's named entity classifier
 */


public class AdvGazetteerWrapper implements AnnotationReceiver {

	SimpleTuplePeer tuplePeer;

    public static final String TYPE_FIELD        = "type";
    public static final String SENTENCE_ID_FIELD = "sentenceId";
    public static final String LOCATION_ID_FIELD = "locationId";
    public static final String TEXT_START_FIELD  = "textStart";
    public static final String TEXT_FIELD        = "text";

    int TYPE_IDX        ;
    int SENTENCE_ID_IDX ;
    int LOCATION_ID_IDX;
    int TEXT_START_IDX  ;
    int TEXT_IDX        ;

//    AbstractSequenceClassifier classifier;
	int sentenceId   = 0;
	int startIdx     = 0;
	int locationId =0;

	public AdvGazetteerWrapper()
    {
    	String[] fields = {};
    	this.init(fields);
    	m_annotationSet = new ArrayList();
    }

	public void annotationFound(Annotation annotation) {
	    m_annotationSet.add(annotation);

	  }


	private static ArrayList m_annotationSet = null;

    public void init(String[] addFields)
	{
		List<String> fields = new ArrayList<String>();
		fields.add(SENTENCE_ID_FIELD);
		fields.add(LOCATION_ID_FIELD);
		fields.add(TYPE_FIELD);
		fields.add(TEXT_START_FIELD);
		fields.add(TEXT_FIELD);

		for (String f : addFields) {
			fields.add(f);
		}
		String[] f = fields.toArray(new String[0]);

    	//
    	// build the tuple (output) data
    	//


    	tuplePeer = new SimpleTuplePeer(f);

    	TYPE_IDX        = tuplePeer.getIndexForFieldName(TYPE_FIELD);
    	SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(SENTENCE_ID_FIELD);
    	LOCATION_ID_IDX = tuplePeer.getIndexForFieldName(LOCATION_ID_FIELD);
    	TEXT_START_IDX  = tuplePeer.getIndexForFieldName(TEXT_START_FIELD);
    	TEXT_IDX        = tuplePeer.getIndexForFieldName(TEXT_FIELD);


	}

    public SimpleTuplePeer getTuplePeer()
    {
    	return tuplePeer;
    }

    public List<SimpleTuple> clean(List<SimpleTuple> tuples, String src)
    {
    	if (tuples.size() < 2) {
    		return tuples;
    	}


    	// partition the list into the varous types
    	String[] types = new String[]{"person", "location", "organization"};
    	List<SimpleTuple> tmp = new ArrayList<SimpleTuple>();
    	List<SimpleTuple> output = new ArrayList<SimpleTuple>();

    	for (String type : types) {

    		tmp.clear();
    		for (SimpleTuple tuple: tuples) {
    			if (tuple.getValue(TYPE_IDX).equals(type)) {
    				tmp.add(tuple);
    			}
    		}

    		if (tmp.size() < 2) {
    			for(SimpleTuple tuple: tmp) {
    				output.add(tuple);
    			}
    			continue;
    		}


    		//
    		// now process the list
    		//
    		int idx0 = 0;
    		int idx1 = idx0 + 1;

    		while(idx1 < tmp.size()) {

    			SimpleTuple a = tmp.get(idx0);
    			SimpleTuple b = tmp.get(idx1);
    			int i = Integer.parseInt(a.getValue(TEXT_START_IDX));
    			int j = Integer.parseInt(b.getValue(TEXT_START_IDX));
    			String at = a.getValue(TEXT_IDX);
    			String bt = b.getValue(TEXT_IDX);
    			if ( j - (i + at.length()) < 2) {

    				a.setValue(TEXT_IDX, at + " " + bt);

    				//output.add(a);
    				// idx0 = idx1 + 1;

    				idx1 = idx1 + 1;

    				// System.out.println("COMBO " + a.toString());
    				// System.out.println(src);

    				if (idx1 == tmp.size()) {
    					output.add(a);
    					// we are done
    				}

    			}
    			else {

    				output.add(a);
    				idx0 = idx1;
    				idx1 = idx0 + 1;

    				if (idx1 == tmp.size()) {
    					// we are done
    					output.add(b);
    				}
    			}


    		}


    	}

    	// person: abe tuple N
    	// person: lincoln: tuple N+1
    	// same sentenceId
    	// b.startIdx - (a.startIdx + text.lenght) <= 3
    	// combine the tuples into one
    	//


    	return output;
    }

    public List<SimpleTuple> toTuples(String stringToParse, int count, String inLocation, String propMessage) throws IOException {

    	List<SimpleTuple> output = new ArrayList<SimpleTuple>();

    	String strLine = stringToParse;
    	AdvGazetteerWrapper sample = new AdvGazetteerWrapper();
    	boolean doPrint = false;
    	StandAloneGaz gaz = new StandAloneGaz();



        // set the filename for intitalization of the gazetteer
        gaz.setListsFile(propMessage);

        //output the status ?
        gaz.setOutputStatus(false);

         gaz.init();
		 CharArrayReader reader = new CharArrayReader(strLine.toCharArray());
		 strLine=strLine.replaceAll(","," , ");
	     strLine=strLine.replaceAll(":"," : ");
	     strLine=strLine.replaceAll(";"," ; ");
		 strLine=strLine.replaceAll("\\."," . ");
		 strLine=strLine.replaceAll("\\("," \\( ");
		 strLine=strLine.replaceAll("\\)"," \\) ");
		 gaz.execute(reader,sample);
		 for (int i = 0; i < m_annotationSet.size(); i++)
			{

			String mention= m_annotationSet.get(i).toString();
			String cat=mention.split(":")[2];
			cat=cat.replaceAll(".null","");
			//System.out.println(cat);
			//System.out.println(("<"+","+mention.split(":")[0]+","+cat+","+mention.split(":")[1].split("-")[0]+","+mention.split(":")[1].split("-")[1]+">"+"\n"));
			SimpleTuple tuple   = tuplePeer.createTuple();
			tuple.setValue(TYPE_IDX,cat);
			tuple.setValue(SENTENCE_ID_IDX, count);
			tuple.setValue(LOCATION_ID_IDX, inLocation);
			tuple.setValue(TEXT_START_IDX,mention.split(":")[1].split("-")[0]);
			tuple.setValue(TEXT_IDX,mention.split(":")[0]);
			output.add(tuple);
			}



//		List<List<CoreLabel>> out = classifier.classify(originalText);
//        for (List<CoreLabel> sentence : out) {
//
//          for (CoreLabel word : sentence) {
//
//
//            String ne = word.get(AnswerAnnotation.class);
//            String type = null;
//            if ("LOCATION".equals(ne) || "PERSON".equals(ne) || "ORGANIZATION".equals(ne)) {
//            	type = ne.toLowerCase();
//            	// consistent with openNLP
//            }
//
//            String text = word.word();
//		   	int indexOfLastWord = originalText.indexOf(text, startIdx);
//
//            if (type != null) {
//
//            	//int idx  = word.index();
//                //int sIdx = word.sentIndex();
//               SimpleTuple tuple   = tuplePeer.createTuple();
//               // console.info(word.word() + '/' +  type);
//               tuple.setValue(TYPE_IDX,        type);
//			   tuple.setValue(SENTENCE_ID_IDX, sentenceId);  // keep this zero based
//			   tuple.setValue(TEXT_START_IDX,  indexOfLastWord);
//			   tuple.setValue(TEXT_IDX,        text);
//			   output.add(tuple);
//
//
//			   if (doPrint) {
//				   System.out.println(tuplePeer.toString());
//				   System.out.println(tuple);
//			   }
//            }
//
//            int len = text.length();
//            if (len > 1 && text.endsWith(".")) {
//
//            	// HACK for how the stanford tokenizer works
//            	// e.g. Ill. ==> tokenized into Ill.  and .
//            	len--;
//
//            }
//            startIdx = indexOfLastWord + len;
//
//          }
//          sentenceId++;
//        }

        return output;

       // return clean(output, strLine);

    }

    public static void main(String[] args) throws FileNotFoundException {

    	FileInputStream fstream = new FileInputStream("lists/reports.txt");
    	DataInputStream in = new DataInputStream(fstream);
    	File f1=new File("lists/OP_reports.txt");
        FileOutputStream fop1=new FileOutputStream(f1);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String str;
        AdvGazetteerWrapper gazHelper;
		gazHelper = new AdvGazetteerWrapper();
		int sid=0;

        try {
			while ((str = br.readLine()) != null)   {
			    String sMessage="file:///Users/kdd-admin/Documents/meandre/Components-Foundry/lists/lists.def";
				// Print the content on the console
				URL loc = StreamUtils.getURLforResource(DataTypeParser.parseAsURI(sMessage));
				if(str.length()!=0)
				{
System.out.println(">>>>>"+sid++);
//					List<SimpleTuple> tuples = gazHelper.toTuples(str,sid,"0","/Users/kdd-admin/Documents/meandre/Components-Foundry/lists/lists.def");
					List<SimpleTuple> tuples = gazHelper.toTuples(str,sid,"0",loc.getPath());

					System.out.println(tuples);

				}
				System.out.println(loc.getPath());

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }




}
