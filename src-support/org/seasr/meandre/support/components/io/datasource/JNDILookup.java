/**
 * University of Illinois/NCSA
 * Open Source License
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.  
 * All rights reserved.
 * 
 * Developed by: 
 * 
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 * 
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions: 
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers. 
 * 
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the 
 *    documentation and/or other materials provided with the distribution. 
 * 
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */ 

package org.seasr.meandre.support.components.io.datasource;

//java imports
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;

public class JNDILookup {
	
    //==============
    // Data Members
    //==============

	private Context ctx;//The context in which Objects are located
	
	private String baseURL;//base URL for JNDI lookup
	
	private Logger logger= Logger.getAnonymousLogger();
	
//	private ExternalJarLoader objectClassLoader;
	 //==============
    // Constructors
    //==============
	
	//base constructor- looks in general runtime namespace
	//This may need to be changed to match meandre Jetty server settings
    /** Default constructor, base url is null
    */
	public JNDILookup(){
		//use no base url
		baseURL="";
		new JNDILookup(baseURL.toString());
//		try{
//			//set up environment variables
//			Hashtable env = new Hashtable();
//			//use Jetty JNDI ICfactory at default JNDI location
//			env.put(Context.INITIAL_CONTEXT_FACTORY,
//					"org.mortbay.naming.InitialContextFactory");
//			//Class.forName("org.mortbay.naming.InitialContextFactory");
//			env.put(Context.PROVIDER_URL,
//			  "localhost:1099");
//			//use new context
//			 ctx = new InitialContext(env);
//		}
//		catch (Exception e){
//			logger.log(Level.SEVERE, "Error configuring initial context "+e);
//		}
	}
	
    /** Constructor with base url specified (typically something like java:comp/env
     * 
     * @param sURL String of base url
	*/
	//Constructor accepts base URL argument
	public JNDILookup(String sURL){
		//base URL not used- Jetty environment is not configured correctly, need to work on that
		baseURL=sURL;
		try{
			//set up environment variables
			Hashtable<String,String> env = new Hashtable<String,String>();
			//use Jetty JNDI ICfactory at default JNDI location
			env.put(Context.INITIAL_CONTEXT_FACTORY,"org.mortbay.naming.InitialContextFactory");
			//Class.forName("org.mortbay.naming.InitialContextFactory");
			env.put(Context.PROVIDER_URL, "localhost:1099");
			//create same context as initial constructor- Jetty namespace is not configured properly
			 ctx = new InitialContext(env);
		}
		catch (NamingException e){
			logger.log(Level.SEVERE,"Error configuring initial context "+e);
		}
	}
	
    //================
    // Public Methods
    //================
	
	//This method lists objects in a given context from the base URL, String sContext is the relative path from the base url
	 /** This method lists objects in a given context from the base URL, String sContext is the relative path from the base url
     * 
     * @param sContext the context to look in
     * @return	Vector containing string list of object names in specified context
	*/
	public Vector<String> listObjects(String sContext){
		//name vector
		Vector<String> names = new Vector<String>();
		try{
			//get naming enumeration of classes in this context
			NamingEnumeration<NameClassPair> list = ctx.list(sContext);

			//turn enumeration into vector
			while (list.hasMore()) {
				NameClassPair nc = (NameClassPair)list.next();
				names.add(nc.getName());
			}
		} catch (NamingException e) {
			logger.log(Level.SEVERE,"Problem looking up Objects in the "+baseURL+sContext+" namespace. Is the server namespace configured?: " + e +":"+ e.getMessage());
			return new Vector<String>(0);
		}
		//return vector
		return names;
	}
	
	//binds object into namespace at URL name given in vJNDILoc. For example jdbc/mydb would bind a datasource named mydb into the java://comp/env/jdbc namespace
	 /** This method binds object into namespace at URL name given in vJNDILoc.
     * 
     * @param vJNDILoc the location to bind object
     * @param obj the object to bind
	*/
	public void bindObject(String vJNDILoc, Object obj)
	{	
		try{
			//InitialContext ic = new InitialContext();
			//ic.bind("java:comp/env/"+vJNDILoc, obj);
			logger.log(Level.INFO,"Object class: "+obj.getClass().getName());
		ctx.bind(vJNDILoc, obj);
		}
		
		catch(NamingException e){
			logger.log(Level.SEVERE,"Error occured with JNDI namespace. "+e +":"+ e.getMessage());
		}
	}
	
	
	//rebinds existing object into JNDI namespace at vJNDILoc
	 /** This method rebinds object that already exists in the namespace at the URL name given in vJNDILoc.
     * 
     * @param vJNDILoc the location to rebind object
     * @param obj the object to rebind
	*/
	public void rebindObject(String vJNDILoc, Object obj)
	{
		try{
			//use rebind to bind object that already exists in namespace
			ctx.rebind(vJNDILoc, obj);
			}
			
			catch(NamingException e){
				logger.log(Level.SEVERE,"Error occured with JNDI namespace. "+e +":"+ e.getMessage());
			}
	}
	
	//remove object at given JNDI location
	 /** This method removes the object at the specified location from the JNDI namespace
     * 
     * @param vJNDILoc the location of the object to remove
	*/
	public void removeObject (String vJNDILoc)
	{
		try{
			//InitialContext ic = new InitialContext();
			//ic.unbind(baseURL+"/"+vJNDILoc);
			ctx.unbind(vJNDILoc);
		} catch (NamingException e) {
			logger.log(Level.SEVERE,"Problem removing database in the "+baseURL+"/"+vJNDILoc+" namespace. Is the server namespace configured?: " + e +":"+ e.getMessage());
		}
	}
	
	
	//returns object found at sObject, for example would return the datasource at jdbc/mydb if sObject="jdbc/mydb"
	 /** returns object found at sObject, for example would return the datasource at jdbc/mydb if sObject="jdbc/mydb"
     * 
     * @param sObject the location to retrieve
     * @return the object bound to that name
	*/
	public Object getExistingObject (String sObject)
	{
		logger.log(Level.INFO, "Attempting to load Object");
		Object obj;//object to return
		try{
			//InitialContext ic = new InitialContext();
			obj = ctx.lookup(sObject); //preform lookup
			logger.log(Level.INFO, "Object loaded "+obj.getClass().getName());
			
			//if the object is a reference, then that means the object's factory could not be found.
			if (obj.getClass().getName().equalsIgnoreCase("javax.naming.Reference"))
			{
				
				//URLClassLoader testLoader;
				
				javax.naming.Reference objRef = (javax.naming.Reference) obj;
				
				Class<?> factoryClass = DataSourceFactory.getClassForName(objRef.getFactoryClassName());
				Class<?> objClass = DataSourceFactory.getClassForName(objRef.getClassName());
				
				
				
				URL [] jarLocURLs = DataSourceFactory.jdbcLoader.getURLs();
				
				String factoryURL = "";
				
				for (int i=0; i<jarLocURLs.length; i++)
				{
					logger.log(Level.INFO, jarLocURLs[i].toString());
					//testLoader = new URLClassLoader (jarLocURLs[i]);
					//try
					//{
						//Class cl = testLoader.loadClass(factoryClass.getClass().getName());
						//if (cl.getClass().getName().equalsIgnoreCase(factoryClass.getClass().getName()))
							
					factoryURL += jarLocURLs[i].toString() + " ";

					
				}
				
					
				javax.naming.Reference newObjRef = new Reference (objClass.getName(), factoryClass.getName(), factoryURL);
				
				Enumeration<?> addrEnum = objRef.getAll();
			
				
				while(addrEnum.hasMoreElements())
				{
					RefAddr newaddr = (RefAddr)addrEnum.nextElement();
					newObjRef.add(newaddr);
				}
				//logger.log(Level.INFO, "Rebinding");
				ctx.rebind(sObject, newObjRef);
				
				
				
				Object dsObj = ctx.lookup(sObject);
				
				return dsObj;
			}
			else
				return obj;
		}
		catch (NamingException e) {
			logger.log(Level.WARNING,"Problem looking up Object"+sObject+" in the java:comp/env/jdbc JNDI namespase. Is the server namespace configured?: " + e +":"+ e.getMessage()+e.getExplanation()+e.getClass().getName());
			
			return null;
		}
		catch (ClassNotFoundException e) {
			logger.log(Level.WARNING,"Problem looking up Object"+sObject+" in the java:comp/env/jdbc JNDI namespase. Is the server namespace configured?: " + e +":"+ e.getMessage());
			
			return null;
		}
	}

}
