/**
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
