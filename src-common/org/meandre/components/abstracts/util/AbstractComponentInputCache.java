/**
 *
 */
package org.meandre.components.abstracts.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;

/**
 * @author bernie acs
 *
 * Basic cache abstract that can be used to collect, store, and retrieve objects
 * made available to during the execute() of org.meandre.component.ExecutableComponent.
 * A general purpose set of mechanics to aid in handle logic where the a compute
 * component uses FiringPolicy.any and some inputs are required, some may be optional,
 * OR where some set of inputs are required to handle a logical processing cycle. This
 * Object is intended to be couple with a single ExecutableComponent.
 *
 */
public abstract class AbstractComponentInputCache {

	/**
	 * Internal storage container for a Collection of java.util.Queue Objects keyed by Name.
	 */
	private Hashtable<String,Queue<Object>> cacheCollection =
			new Hashtable<String,Queue<Object>>();
	/**
	 * Internal storage container initialized on first use of putCacheComponentInput()
	 * that will hold the names of connected ComponentInput ports seen at runtime.
	 */
	private Set<String>componentInputPortNames = null;
	private Map<String,Integer>componentInputQueueNames = null;
	/**
	 *
	 */
/*	private boolean outputToConsole = false;
	private boolean outputToConsoleVerbose = false;
	private PrintStream consoleOut = null;
*/
	private Logger consoleLogger = null;
	/**
	 *
	 * @param cc
	 * @param dataComponentInputPort
	 * @throws ComponentContextException
	 */
	synchronized public void putCacheComponentInput (
			ComponentContext cc,
			String dataComponentInputPort
	) throws ComponentContextException {
		//
		Queue<Object> queue = null;

		//
		if( componentInputPortNames == null ){
			String s[] = cc.getInputNames();
			componentInputPortNames = new HashSet<String>(s.length);
			componentInputQueueNames= new HashMap<String,Integer>(s.length);
			for(int i=0; i< s.length; i++){
				componentInputPortNames.add(s[i]);
			}

/*			if(isOutputToConsoleVerbose())
				getConsoleOut().println(this.getClass().getName()+
						" : Initialized ComponentInput Names references");
*/
		}

		consoleLogger.logp(Level.INFO, getClass().getName(), "putCacheComponentInput",
		        "Received Data Event on "+ dataComponentInputPort +" (Cache/Return)");
/*
		if(isOutputToConsoleVerbose())
			getConsoleOut().println(this.getClass().getName()+
					".put() : Recieved Data Event on "+ dataComponentInputPort +" (Cache/Return)");

*/		//
		if( ! componentInputPortNames.contains(dataComponentInputPort)){
		      consoleLogger.logp(Level.INFO, getClass().getName(), "putCacheComponentInput",
		                "Received Data Event on "+ dataComponentInputPort + " which appears to be not connected. Simply returning.");

/*
			if(isOutputToConsoleVerbose())
				getConsoleOut().println(this.getClass().getName()+
						".put() : Data Event on "+ dataComponentInputPort +
						" which appears to be not connected. Simply returning.");
*/
			return;
		}

		//
		if(! cc.isInputAvailable(dataComponentInputPort)){
/*
			if(isOutputToConsoleVerbose())
				getConsoleOut().println(this.getClass().getName()+
						".put() : Data Event on "+ dataComponentInputPort +
						" which returned false for isInputAvailable(). Simply returning.");
*/
		    consoleLogger.logp(Level.INFO, getClass().getName(), "putCacheComponentInput",
		            "Data Event on " + dataComponentInputPort + " which returned false for isInputAvailable(). Simply returning.");
		    return;
		}

		//
		if( ! cacheCollection.containsKey(dataComponentInputPort) ){
			queue = new LinkedList<Object>();
		} else {
			queue = (Queue<Object>) cacheCollection.get(dataComponentInputPort);
		}

		//
		queue.add( (Object)cc.getDataComponentFromInput(dataComponentInputPort) );
		cacheCollection.put(dataComponentInputPort, queue);

		//
		componentInputQueueNames.put(dataComponentInputPort,queue.size());
        consoleLogger.logp(Level.INFO, getClass().getName(), "putCacheComponentInput",
                "Data Event on " + dataComponentInputPort + " new Queue size = "+ queue.size() );
/*
				message);
		if(isOutputToConsole())
			getConsoleOut().println(this.getClass().getName()+
					".put() : Data Event on "+ dataComponentInputPort +
					" new Queue size = "+ queue.size() );
		//
		//
*/	}

	/**
	 *
	 * @param cc
	 * @param dataComponentInputPort
	 * @return
	 * @throws ComponentContextException
	 */
	synchronized public Object getCacheComponentInput (
			String dataComponentInputPort
	) {
		//
		Object returnValue = null;
		Queue<Object> queue = null;

		//
        consoleLogger.logp(Level.INFO, getClass().getName(), "getCacheComponentInput",
                "Fetching Cached Data Event on " + dataComponentInputPort + " (deQueue/Return)");
/*		//
		if(isOutputToConsoleVerbose())
			getConsoleOut().println(this.getClass().getName()+ " : Fetching Cached Data Event on "+ dataComponentInputPort +" (deQueue/Return)");

*/		//
		if( ! componentInputPortNames.contains(dataComponentInputPort)){
/*			if(isOutputToConsoleVerbose())
				getConsoleOut().println(this.getClass().getName()+
						".get() : Data Event on "+ dataComponentInputPort +
						" which appears to be not connected. Simply returning.");
*/
	        consoleLogger.logp(Level.INFO, getClass().getName(), "getCacheComponentInput",
	                "Data Event on " + dataComponentInputPort + " which appears to be not connected. Simply returning.");
			return null;
		}

		//
		if( ! cacheCollection.containsKey(dataComponentInputPort) ){
/*			if(isOutputToConsoleVerbose())
				getConsoleOut().println(this.getClass().getName()+
						".get() : Data Event on "+ dataComponentInputPort +
						" which has not cached any values. Simply returning null.");
			*/
	        consoleLogger.logp(Level.INFO, getClass().getName(), "getCacheComponentInput",
	                "Data Event on " + dataComponentInputPort + " which has not cached any values. Simply returning null.");

			return null;
		} else {
			queue = (Queue<Object>) cacheCollection.get(dataComponentInputPort);
		}
		//
		returnValue = queue.poll();
		//
		cacheCollection.put(dataComponentInputPort, queue);
		componentInputQueueNames.put(dataComponentInputPort,queue.size());
/*		if(isOutputToConsole())
			getConsoleOut().println(this.getClass().getName()+
					".get() : Data Event on "+ dataComponentInputPort +
					" new Queue size = "+ queue.size() );
*/
        consoleLogger.logp(Level.INFO, getClass().getName(), "getCacheComponentInput",
                "Data Event on " + dataComponentInputPort + " new Queue size = " + queue.size());

		//
		return returnValue;
	}

	/**
	 *
	 * @param dataComponentInputs -- Set<String> of names
	 * @return
	 */
	synchronized public int cacheSetsAvailable(Set<String> dataComponentInputs){
		int setSize = dataComponentInputs.size();
		int minValue = 0;
		boolean initialValueSet = false;

		for(String s: dataComponentInputs){
			if(cacheCollection.containsKey(s)){
				setSize-- ;
				Queue<Object> q = cacheCollection.get(s);
				if(q.size()==0)
					return 0;
				else
				if( q.size()> 0 ){
					if((initialValueSet) && (q.size()< minValue)){
						minValue = q.size();
					} else if(!initialValueSet){
						minValue = q.size();
						initialValueSet = true;
					}
				}
			}
		}
		// expecting this value to be decremented to zero
		if(setSize>0)
			return 0;

		// expecting this value to be >= one
		if(minValue>0)
			return minValue;
		//
		return 0;
	}

	/**
	 *
	 * @param dataComponentInputs -- String[] of names
	 * @return
	 */
	synchronized public int cacheSetsAvailable(String[] dataComponentInputs){
		return cacheSetsAvailable( convertStringArrayToSet(dataComponentInputs) );
	}

	/**
	 *
	 * @param dataComponentInputs -- comma delimited String of names
	 * @return
	 */
	synchronized public int cacheSetsAvailable(String dataComponentInputs){
		String s[] = dataComponentInputs.split(",");
		return cacheSetsAvailable( s );
	}

	/**
	 *
	 * @param dataComponentInputs
	 * @return
	 */
	synchronized public boolean isCacheSetAvailable(Set<String> dataComponentInputs){
		if(cacheSetsAvailable(dataComponentInputs)>0)
			return true;
		return false;
	}

	/**
	 *
	 * @param dataComponentInputs -- String[] of names
	 * @return
	 */
	synchronized public boolean isCacheSetAvailable(String[] dataComponentInputs){
		return isCacheSetAvailable( convertStringArrayToSet(dataComponentInputs) );
	}

	/**
	 *
	 * @param dataComponentInputs -- comma delimited String of names
	 * @return
	 */
	synchronized public boolean isCacheSetAvailable(String dataComponentInputs){
		String s[] = dataComponentInputs.split(",");
		return isCacheSetAvailable( s );
	}


	/**
	 *
	 * @param dataComponentInputs -- Set<String> of names
	 * @return
	 */
	synchronized public Map<String,Object> getCacheSet(Set<String> dataComponentInputs){
		Map<String,Object>returnValue = new HashMap<String,Object>(dataComponentInputs.size());

		for(String s: dataComponentInputs){
			//
			if(cacheCollection.containsKey(s)){
				returnValue.put(s,getCacheComponentInput(s));
			}
			//
		}
		return returnValue;
	}

	/**
	 *
	 * @param dataComponentInputs -- String[] of names
	 * @return
	 */
	synchronized public Map<String,Object> getCacheSet(String[] dataComponentInputs){
		return getCacheSet( convertStringArrayToSet(dataComponentInputs) );
	}

	/**
	 *
	 * @param dataComponentInputs -- comma delimited String of names
	 * @return
	 */
	synchronized public Map<String,Object> getCacheSet(String dataComponentInputs){
		String s[] = dataComponentInputs.split(",");
		return getCacheSet( s );
	}

	/**
	 *
	 * @param dataComponentInputs
	 * @return
	 */
	private Set<String> convertStringArrayToSet(String[] dataComponentInputs){
		Set<String> s = new HashSet<String>(dataComponentInputs.length);
		for(int i=0; i< dataComponentInputs.length; i++){
			String t = dataComponentInputs[i].trim();
			if( t != "" )
				s.add(t);
		}
		return s;
	}

	public void setLogger(Logger logger) {
	    consoleLogger = logger;
	}
}
