package org.seasr.meandre.component.opennlp;

import java.util.ArrayList;
import java.util.List;


public class TextChunk {

	String pos;

	List<String> tokens    = new ArrayList<String>();
	List<String> tokensPos = new ArrayList<String>();

	public TextChunk(String pos) 
	{
		this.pos = pos;
	}

	public void add(String text, String pos) {
		tokens.add(text);
		tokensPos.add(pos);
	}

	public int size()
	{
		return tokens.size();
	}

}
