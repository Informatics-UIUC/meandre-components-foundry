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
 * simple wrapper class to process text into tuples using GATE's Gazetteer
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
			SimpleTuple tuple   = tuplePeer.createTuple();
			tuple.setValue(TYPE_IDX,cat);
			tuple.setValue(SENTENCE_ID_IDX, count);
			tuple.setValue(LOCATION_ID_IDX, inLocation);
			tuple.setValue(TEXT_START_IDX,mention.split(":")[1].split("-")[0]);
			tuple.setValue(TEXT_IDX,mention.split(":")[0]);
			output.add(tuple);
			}




        return output;


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
				URL loc = StreamUtils.getURLforResource(DataTypeParser.parseAsURI(sMessage));
				if(str.length()!=0)
				{
System.out.println(">>>>>"+sid++);
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
