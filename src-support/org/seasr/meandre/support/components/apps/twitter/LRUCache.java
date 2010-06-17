package org.seasr.meandre.support.components.apps.twitter;

import java.util.ArrayList;
import java.util.List;

//
// just an array of items with a max capacity
// when you add to it, the oldest element (the one added first) will be replaced with the new element
//

public class LRUCache<T> {
	
	List<T> list;
	
	int oldestIdx;
	int newestIdx;
	int maxSize;
	int currentSize;
	
	
	public LRUCache(int size)
	{
		list = new ArrayList<T>();
		
		oldestIdx   = 0;
	    maxSize     = size;
	    currentSize = 0;
	    newestIdx   = 0;
	}
	
    public T oldest()
    {
    	try {
    		return list.get(oldestIdx);
    	}
    	catch (Exception e) {
    		return null;
    	}
    	
    }
    
	public T add(T element)
	{
		T oldEle = null;
		
		if (list.size() < maxSize) {
			list.add(element);
		}
		else {
			oldEle = list.get(oldestIdx);
			list.set(oldestIdx, element);
		}
		
		newestIdx  = oldestIdx;
		
		oldestIdx   = (oldestIdx + 1)%maxSize;
		currentSize = Math.min(currentSize + 1, maxSize);
		
		return oldEle;
	}
	
	public int size()
	{
		return list.size();
	}
	
	public void toArray(Object[] out) {
		// assert out.length == this.size
		// client can pass in the array
		// prevent the arraycopy ?
		//
	}
	
	@SuppressWarnings("unchecked")
	public Object[] toArray()
	{
		//T[] tmp = new T[list.size()];
		
		if (list.size() < maxSize) {
			//
			// element 0 is the oldest
			//
			// return (T[]) list.toArray();
			return list.toArray();
		}
		else {
			
			Object[] tmp  = list.toArray();
			Object[] dest = new Object[maxSize];
			

			// two copies needed
			// oldestIdx to list.size() 
			// then 0 to oldestIdx
			int s1 = maxSize - oldestIdx;
			int s2 = maxSize - s1;
			System.arraycopy(tmp, oldestIdx,  dest, 0,  s1);
			System.arraycopy(tmp, 0,          dest, s1, s2);
			
			// return (T[]) dest;
			return dest;
		}	
	}

}
