package org.seasr.meandre.support.components.tuples;


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


import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;


public class SimpleTuple {
	
	SimpleTuplePeer peer;
	String[] values; 
	
	
	protected SimpleTuple() {}
	
	public SimpleTuple(SimpleTuplePeer peer)
	{
		this.peer = peer;
		this.values = new String[peer.size()];
	}
	
	public SimpleTuplePeer getPeer() 
	{
		return this.peer;
	}
	
	public void setValues(Strings v) 
	{
		// convert, copy
		String[] va = BasicDataTypesTools.stringsToStringArray (v);
		this.values = va;
	}
	
	 public void setValue(SimpleTuple copyMe) 
	 {
	    int sz = copyMe.peer.size();
	    for (int i = 0; i < sz; i++ ) {
	       setValue(i, copyMe.getValue(i));
	    }
    }
	
	
	public void setValue(int idx, String v) 
	{
		values[idx] = v;
	}
	
	public void setValue(int idx, int v) 
	{
		values[idx] = Integer.toString(v);
	}
	
	public void setValue(int idx, double v) 
	{
		values[idx] = Double.toString(v);
	}
	
	public void setValue(String fieldName, String value)
	{
		int idx = peer.getIndexForFieldName(fieldName);
		if (idx == -1) {
			throw new RuntimeException("Unable to find field " + fieldName);
		}
		this.setValue(idx, value);
	}
	
	public String getValue(int idx) 
	{
		return values[idx];
	}
	
	public String getValue(String fieldName) 
	{
		return this.getValue(peer.getIndexForFieldName(fieldName));
	}
	
	public Strings convert() 
	{
		return BasicDataTypesTools.stringToStrings(values);
	}
	
	
	public String toString() {
		return SimpleTuplePeer.toString(values);
	}
	
}
