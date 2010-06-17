package org.seasr.meandre.support.components.apps.twitter;

import java.util.HashMap;
import java.util.Map;



class LRUCacheEntry {
	
	String id;
	String element;
	
	public LRUCacheEntry(String k, String v) {
		this.id      = k;
		this.element = v;
	}
}


public class LRUCacheWithMap extends LRUCache<LRUCacheEntry> {
	
	
	Map<String,LRUCacheEntry> map;
    
	
	public LRUCacheWithMap(int size)
	{
		
		super(size);
		
		map  = new HashMap<String,LRUCacheEntry>();
		
	}
	

	public String add(String id, String value)
	{
		LRUCacheEntry entry = oldest();
		if (entry != null) {
			map.remove(entry.id);
			entry.id      = id;
			entry.element = value;
		}
		else {
			entry = new LRUCacheEntry(id, value);
		}
		
		
		LRUCacheEntry old = super.add(entry);
		map.put(id, entry);
		if (old == null) return null;
		
		return old.element;
	}
	
	
	public String find(String id) 
	{
		LRUCacheEntry e = map.get(id);
		if (e == null) return null;
		
		return e.element;
	}

	
}