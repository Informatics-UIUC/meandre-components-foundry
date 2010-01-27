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

package org.seasr.meandre.support.component.opennlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.StringTokenizer;


public class StaticLocationFinder implements StaticTextSpanFinder {

	Map<String,String> locationMap;
	
	private String normalize(String in) 
	{
		return in.toLowerCase().trim();
	}
	
	public StaticLocationFinder(Map<String,String> map) 
	{
		this.locationMap = new HashMap<String,String>();
		
		for (Map.Entry<String, String> e : map.entrySet()) {
			
			String k = e.getKey();
			String v = e.getValue();
			
			String key = normalize(k);
			
			locationMap.put(key, v);
			
		}
	}
	
	public List<TextSpan> labelSentence(String sentence) 
	{

		List<TextSpan> list = new ArrayList<TextSpan>();

		StringTokenizer tokens = new StringTokenizer(sentence);
		int sIdx = 0;
		int eIdx = 0;

		while(tokens.hasMoreTokens()) {
			
			String sub = tokens.nextToken();

			String key = normalize(sub);
			String location = locationMap.get(key);
			
			//
			// the sentence contains a value that is in our map
			//
			
			if ( location != null) {
				
				sIdx = sentence.indexOf(sub);
				eIdx = sIdx + sub.length();

				TextSpan span = new TextSpan();
				span.setStart(sIdx);
				span.setEnd(eIdx);
				span.setText(location);

				list.add(span);

			}

		}
	    return list;

	}

}
