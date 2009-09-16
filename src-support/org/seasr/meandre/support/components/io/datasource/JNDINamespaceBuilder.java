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
import javax.naming.NamingException;

import javax.sql.DataSource;
import javax.sql.ConnectionPoolDataSource;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.Properties;

/*
 * <p>Title: JNDINamespaceBuilder</p>
 * <p>
 * This class provides a method to populate a JNDI namespace from an JNDIXMLWriter
 * Right now it will only create datasources, and the developer needs to make sure the proper
 * Jars have been loaded onto the classpath before attempting to create datasources
 * TO DO: Expand support for general objects and general JNDI namespaces
 *</p>
 *
 *<p>Company: NCSA, Automated Learning Group</p>
 * @author E Johnson
 * @version 1.0
 */

public class JNDINamespaceBuilder {

	 //==============
    // Data members
    //==============
	
	private Logger logger= Logger.getAnonymousLogger();
	
	private Context ctx;//The context in which Objects are located
	
	private String baseURL;//base URL for JNDI lookup
	
	private XMLLoader xmlLoader;
	
	
	//==============
    // Constructors
    //==============
	
	//base constructor- looks in java:comp/env environment
	//This may need to be changed to match meandre Jetty server settings
	/** Default constructor, specifies xml file location
	 * @param xmlFileLoc String representing location of xml file
	    */
	public JNDINamespaceBuilder(String xmlFileLoc){
		try{
			//create the xml loader
			xmlLoader= new XMLLoader(xmlFileLoc);
			
			//fill the environment variables
			Hashtable env = new Hashtable();
			//Jetty initial context factory
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"org.mortbay.naming.InitialContextFactory");
			
			//default JNDI location
			env.put(Context.PROVIDER_URL,
			  "localhost:1099");
			 ctx = new InitialContext(env);
		}
		catch (Exception e){
			logger.log(Level.SEVERE,"Error configuring initial context "+e);
			
		}
	}
	
	//Constructor accepts base URL argument
	/** Default constructor, accepts xml file location and base url for namespace
	 * 
	 * @param sURL String representing base url
	 * @param xmlFileLoc string represeting xml file location
	    */
	public JNDINamespaceBuilder(String sURL, String xmlFileLoc){
		//base URL doesn't work- Jetty JNDI namespace not correctly configured
		baseURL=sURL;
		try{
			//create xml loader
			xmlLoader= new XMLLoader(xmlFileLoc);
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"org.mortbay.naming.InitialContextFactory");
			
			env.put(Context.PROVIDER_URL,
			  "localhost:1099");
			 ctx = new InitialContext(env);
		}
		catch (NamingException e){
			logger.log(Level.SEVERE,"Error configuring initial context "+e);
			
		}
	}
	
	 //==============
    // Public Methods
    //==============
	
	/** This method builds the namespace using data from the xml loader.
	    */
	public void buildNamespace()
	{
		//attempt to read properties file
		if(xmlLoader.readPropertiesFile())
		{
			String JNDILoc = "";
			String DSClass = "";
			Properties newDSProps = new Properties();
			//go through all objects in file (defined by the count)
			for (int i=0; i<xmlLoader.getObjCount(); i++)
			{
				try{
					//use xml loader to get data and properties
					JNDILoc = xmlLoader.getObjName(i);
					DSClass = xmlLoader.getObjProperty(i, "DSClass");
					newDSProps = xmlLoader.getObjProps(i);
					newDSProps.setProperty("Vendor DataSource", DSClass);
					logger.log(Level.INFO,"Creating Datasource");
					//create a new datasource using these properties
					if (DataSourceFactory.isPooledfromDataSource(DSClass))
					{
						ConnectionPoolDataSource newDS = DataSourceFactory.createPooledDS(newDSProps);
						logger.log(Level.INFO,"Datasource created");
						//pre-processing on the name to get the correct place to bind the object
						if (JNDILoc.lastIndexOf('/')>0)
						{
							JNDILoc = JNDILoc.substring(JNDILoc.lastIndexOf('/')+1);
						}
						else if (JNDILoc.lastIndexOf('\\')>0)
						{
							JNDILoc = JNDILoc.substring(JNDILoc.lastIndexOf('\\')+1);
						}
						//bind the object to the name
						ctx.bind(JNDILoc, newDS);
						logger.log(Level.INFO,"Resource "+newDS.toString()+" bound to "+JNDILoc);
					}
					else
					{
						DataSource newDS = DataSourceFactory.createDS(newDSProps);
						logger.log(Level.INFO,"Datasource created");
						//pre-processing on the name to get the correct place to bind the object
						if (JNDILoc.lastIndexOf('/')>0)
						{
							JNDILoc = JNDILoc.substring(JNDILoc.lastIndexOf('/')+1);
						}
						else if (JNDILoc.lastIndexOf('\\')>0)
						{
							JNDILoc = JNDILoc.substring(JNDILoc.lastIndexOf('\\')+1);
						}
						//bind the object to the name
						ctx.bind(JNDILoc, newDS);
						logger.log(Level.INFO,"Resource "+newDS.toString()+" bound to "+JNDILoc);
					}
				}
				catch (Exception e)
				{
					logger.log(Level.SEVERE,"Error Binding Datasource "+e);
				}
			}
		}
	}
	
}
