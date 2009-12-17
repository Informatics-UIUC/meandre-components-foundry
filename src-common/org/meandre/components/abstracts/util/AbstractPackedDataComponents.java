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

package org.meandre.components.abstracts.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author bernie acs
 *
 */
public class AbstractPackedDataComponents extends HashMap<String,Object> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public AbstractPackedDataComponents() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param initialCapacity
	 */
	public AbstractPackedDataComponents(int initialCapacity) {
		super(initialCapacity);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param m
	 */
	public AbstractPackedDataComponents(Map<String,Object> m) {
		super(m);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public AbstractPackedDataComponents(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		// TODO Auto-generated constructor stub
	}
	/**
	 *
	 * @param rParameters
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static Map<String, String> convertObjectToMapStringString(Object packedMap){
		Map<String, String> requestParameters = new HashMap<String, String>();
		if( packedMap instanceof  java.util.Map  ){
			for( Entry<?, ?> e :((Map<?, ?>)(packedMap)).entrySet() ){
				requestParameters.put( (String)e.getKey(), e.getValue().toString() );
			}
		} else {
			return null;
		}
		return requestParameters;
	}

	/**
	 *
	 * @param rParameters
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static Map<String, Object> convertObjectToMapStringObject(Object packedMap){
		Map<String, Object> packedInMap = new HashMap<String, Object>();
		if( packedMap instanceof  java.util.Map  ){
			for( Entry<?, ?> e :((Map<?, ?>)(packedMap)).entrySet() ){
				packedInMap.put( (String)e.getKey(), (Object)e.getValue() );
			}
		} else {
			return null;
		}
		return packedInMap;
	}

}
