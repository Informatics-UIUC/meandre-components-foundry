package org.seasr.meandre.components.nlp.stanford;


import java.util.ArrayList;
import java.util.List;

import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;


/* 
 * simple wrapper class to process text into tuples using Stanford's named entity classifier
 */


public class StanfordNEWrapper {

	SimpleTuplePeer tuplePeer;

    public static final String TYPE_FIELD        = "type";
    public static final String SENTENCE_ID_FIELD = "sentenceId";
    public static final String TEXT_START_FIELD  = "textStart";
    public static final String TEXT_FIELD        = "text";

    int TYPE_IDX        ;
    int SENTENCE_ID_IDX ;
    int TEXT_START_IDX  ;
    int TEXT_IDX        ;

    AbstractSequenceClassifier classifier;
	int sentenceId   = 0;
	int startIdx     = 0;
	
	public StanfordNEWrapper(AbstractSequenceClassifier c)
    {
    	String[] fields = {};
    	this.init(c, fields);
    }
	
	public StanfordNEWrapper(AbstractSequenceClassifier c, String[] addFields)
	{
		this.init(c, addFields);
	}
	
    public void init(AbstractSequenceClassifier c, String[] addFields)
	{
		List<String> fields = new ArrayList<String>();
		fields.add(SENTENCE_ID_FIELD);
		fields.add(TYPE_FIELD);
		fields.add(TEXT_START_FIELD);
		fields.add(TEXT_FIELD);
		
		for (String f : addFields) {
			fields.add(f);
		}
		String[] f = fields.toArray(new String[0]);
		
		
        this.classifier = c;
    	
    	//
    	// build the tuple (output) data
    	//
    	

    	tuplePeer = new SimpleTuplePeer(f);

    	TYPE_IDX        = tuplePeer.getIndexForFieldName(TYPE_FIELD);
    	SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(SENTENCE_ID_FIELD);
    	TEXT_START_IDX  = tuplePeer.getIndexForFieldName(TEXT_START_FIELD);
    	TEXT_IDX        = tuplePeer.getIndexForFieldName(TEXT_FIELD);
		
		
	}
    
    public SimpleTuplePeer getTuplePeer()
    {
    	return tuplePeer;
    }
    
    public List<SimpleTuple> toTuples(String stringToParse) {
    	
    	List<SimpleTuple> output = new ArrayList<SimpleTuple>();

    	String originalText = stringToParse;

		List<List<CoreLabel>> out = classifier.classify(originalText);
        for (List<CoreLabel> sentence : out) {

          for (CoreLabel word : sentence) {


            String ne = word.get(AnswerAnnotation.class);
            String type = null;
            if ("LOCATION".equals(ne) || "PERSON".equals(ne) || "ORGANIZATION".equals(ne)) {
            	type = ne.toLowerCase();
            	// consistent with openNLP
            }

            String text = word.word();
		   	int indexOfLastWord = originalText.indexOf(text, startIdx);

            if (type != null) {

            	//int idx  = word.index();
                //int sIdx = word.sentIndex();
               SimpleTuple tuple   = tuplePeer.createTuple();
               // console.info(word.word() + '/' +  type);
               tuple.setValue(TYPE_IDX,        type);
			   tuple.setValue(SENTENCE_ID_IDX, sentenceId);  // keep this zero based
			   tuple.setValue(TEXT_START_IDX,  indexOfLastWord);
			   tuple.setValue(TEXT_IDX,        text);
			   output.add(tuple);
            }

            int len = text.length();
            if (len > 1 && text.endsWith(".")) {

            	// HACK for how the stanford tokenizer works
            	// e.g. Ill. ==> tokenized into Ill.  and .
            	len--;

            }
            startIdx = indexOfLastWord + len;

          }
          sentenceId++;
        }
        
        return output;

    }

    
}
