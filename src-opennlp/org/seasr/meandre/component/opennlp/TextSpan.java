package org.seasr.meandre.component.opennlp;

public class TextSpan {
	
	int startIdx;
	int endIdx;
	String span;
	
	public TextSpan()
	{
		reset();
	}

	public void reset()
	{
		startIdx = endIdx = 0;
	}
	public void setStart(int s) {
		startIdx = s;
	}
	public void setEnd(int e) {
		endIdx = e;
	}
	
	public void setSpan(String s) 
	{
		String textSpan = s.substring(startIdx, endIdx);
		this.span = textSpan.replace("\n", " ").trim();
	}
	
	public int getStart() {return startIdx;}
	public int getEnd()   {return endIdx;}
	public String getText() { return span;}

}
