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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import javax.sql.DataSource;
import javax.sql.ConnectionPoolDataSource;

import java.util.Properties;

import java.util.logging.Logger;
import java.util.logging.Level;

/*
 * <p>Title: JNDINamespaceWriter</p>
 * <p>
 * This class provides a method to write out the contents of a JNDI environment (specifically just datasource objects) to a JNDIXMLWriter object.
 * TO DO: Expand support for general objects and general JNDI namespaces
 *</p>
 *
 *<p>Company: NCSA, Automated Learning Group</p>
 * @author E Johnson
 * @version 1.0
 */

public class JNDINamespaceWriter {

	 //==============
    // Data members
    //==============
	
	private Logger logger= Logger.getAnonymousLogger();
	
	private Context ctx;//The context in which Objects are located
	
	private String baseURL;//base URL for JNDI lookup
	
	private XMLWriter xmlWriter;
	
	//==============
    // Constructors
    //==============
	
	//base constructor- looks in java:comp/env environment
	//This may need to be changed to match meandre Jetty server settings
    /** Default constructor, base url is null
	    */
	public JNDINamespaceWriter(){
		//base URL not used
		baseURL="java:comp/env/";
		try{
			//fill environment properties
			Hashtable env = new Hashtable();
			//Add Jetty initial context factory
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"org.mortbay.naming.InitialContextFactory");
			//Class.forName("org.mortbay.naming.InitialContextFactory");
			//Add default JNDI location
			env.put(Context.PROVIDER_URL,
			  "localhost:1099");
			 ctx = new InitialContext(env);
		}
		catch (Exception e){
			logger.log(Level.SEVERE,"Error configuring initial context "+e);
		}
	}
	
	//Constructor accepts base URL argument
    /** Constructor with base url specified (typically something like java:comp/env
     * 
     * @param sURL String of base url
	*/
	public JNDINamespaceWriter(String sURL){
		//base url not used
		baseURL=sURL;
		try{
			//Create hashtable with environment variables
			Hashtable env = new Hashtable();
			//the initial context factory for the Jetty server
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"org.mortbay.naming.InitialContextFactory");
			//JNDI default location
			env.put(Context.PROVIDER_URL,
			  "localhost:1099");
			//create a new context to look up objects
			 ctx = new InitialContext(env);
		}
		catch (NamingException e){
			logger.log(Level.SEVERE,"Error configuring initial context "+e);
		}
	}
	
	 //==============
    // Public Methods
    //==============
	
    /** This method moves through the JNDI namespace and outputs datasource objects found there
     * 
     * @param xmlLoc String of xml file location
	*/
	public void writeNamespace(String xmlLoc)
	{
		//create xml writer
		xmlWriter = new XMLWriter (xmlLoc);
		Properties dsProps; 
		try{
			//get a list of all objects- they should be datasources
			NamingEnumeration<NameClassPair> list = ctx.list("");
			
			//add datasources
				while (list.hasMore()) {
					NameClassPair nc = (NameClassPair)list.next();
					Object ds = getExistingObject(nc.getName());
					logger.log(Level.INFO, "Attempting to write class "+ds.getClass().getName());
					if (DataSourceFactory.isPooledfromDataSource(ds.getClass().getName())){
						ConnectionPoolDataSource cpds = (ConnectionPoolDataSource)getExistingObject(nc.getName());
						//use datasourcefacotry to get the properties
						dsProps = DataSourceFactory.getDatasourceProps(cpds);
						//add a class property
						dsProps.setProperty("DSClass", cpds.getClass().getName());
						//add an object to the writer
						xmlWriter.addObject(nc.getName(), dsProps);
						logger.log(Level.INFO,"Resource " +nc+ "Has been added to writer");
					}
					else
					{
						DataSource newds = (DataSource)getExistingObject(nc.getName());
						//use datasourcefacotry to get the properties
						dsProps = DataSourceFactory.getDatasourceProps(newds);
						//add a class property
						dsProps.setProperty("DSClass", newds.getClass().getName());
						//add an object to the writer
						xmlWriter.addObject(nc.getName(), dsProps);
						logger.log(Level.INFO,"Resource " +nc+ "Has been added to writer");
					}
				}
			//write the objects
			xmlWriter.writePropertiesFile();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Problem adding datasource to writer: " + e +":"+ e.getMessage());		
		}
	}
	
    /** This private method retrieves and object from the JNDI namespace
     * 
     * @param sObject String of object location
     * @return Object bound to that location
	*/
	//returns object found at sObject, for example would return the datasource at jdbc/mydb if sObject="jdbc/mydb"
	private Object getExistingObject (String sObject)
	{
		Object obj;//object to return
		try{
			//InitialContext ic = new InitialContext();
			obj = ctx.lookup(sObject); //preform lookup
		}
		catch (NamingException e) {
			logger.log(Level.SEVERE,"Problem looking up Object "+sObject+" in the java:comp/env/jdbc JNDI namespase. Is the server namespace configured?: " + e +":"+ e.getMessage());
			return null;
		}
		return obj;
	}
	
}
