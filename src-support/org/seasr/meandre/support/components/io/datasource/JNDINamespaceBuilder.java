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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;
import javax.naming.NamingException;

import javax.sql.DataSource;
import javax.sql.ConnectionPoolDataSource;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;


/*
 * <p>Title: JNDINamespaceBuilder</p>
 * <p>
 * This class provides a method to populate a JNDI namespace.
 * Right now it will only create datasources, and the developer needs to make sure the proper
 * jars have been loaded onto the classpath before attempting to create datasources.
 *</p>
 *
 *<p>Company: NCSA, Automated Learning Group</p>
 * @author E Johnson
 * @author Lily Dong
 * @version 1.0
 */

public class JNDINamespaceBuilder {

	//==============
    // Data members
    //==============

	private Logger logger= Logger.getAnonymousLogger();

	private DirContext dctx;//The context in which objects are located.
	private XMLLoader xmlLoader;
	private String baseDN;


	//==============
    // Constructors
    //==============

	//base constructor- looks in java:comp/env environment
	//This may need to be changed to match meandre Jetty server settings
	/** Default constructor, specifies xml file location
	 * @param xmlFileLoc String representing location of xml file
	 */
	public JNDINamespaceBuilder(
			String xmlFileLoc, String username, String password, String dn) {
		try{
			baseDN = dn;

			//create the xml loader
			xmlLoader= new XMLLoader(xmlFileLoc);

			//fill the environment variables
			Hashtable env = new Hashtable();

			//Jetty initial context factory
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			//default JNDI location
			env.put(Context.PROVIDER_URL, "ldap://localhost:389");
			//specifies the authentication mechanism to use
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			//user name
			env.put(Context.SECURITY_PRINCIPAL, username);
			// passward
			env.put(Context.SECURITY_CREDENTIALS, password);

			dctx = new InitialDirContext(env);
		}
		catch (Exception e){
			logger.log(Level.SEVERE,"Error configuring initial context "+e);

		}
	}

	//==============
    // Public Methods
    //==============

	/**
	 * This method builds the namespace using data from the xml loader.
	 */
	public void buildNamespace()
	{
		//attempt to read properties file
		if(xmlLoader.readPropertiesFile())
		{
			String DSClass = "";
			Properties newDSProps = new Properties();

			//supposedly the entry exists, so rebinding it to a new datasouce object
			try{
				//use xml loader to get data and properties
				DSClass = xmlLoader.getObjProperty(0, "DSClass");

				newDSProps = xmlLoader.getObjProps(0);
				newDSProps.setProperty("Vendor DataSource", DSClass);

				logger.log(Level.INFO,"Creating Datasource");

				Attributes attrs = new BasicAttributes(true);

		    	Attribute oc = new BasicAttribute("objectclass"); // required by 'top'
		    	oc.add("top");
		    	oc.add("untypedObject");
		    	attrs.put(oc);

				//create a new datasource using these properties
				if (DataSourceFactory.isPooledfromDataSource(DSClass)) {
					ConnectionPoolDataSource newDS = DataSourceFactory.createPooledDS(newDSProps);
					logger.log(Level.INFO,"Datasource created");
					dctx.rebind(baseDN, newDS, attrs);
					//dctx.unbind(baseDN);
					logger.log(Level.INFO,"Resource "+newDS.toString()+" bound to "+baseDN);
				} else {
					DataSource newDS = DataSourceFactory.createDS(newDSProps);
					logger.log(Level.INFO,"Datasource created");
					dctx.rebind(baseDN, newDS, attrs);
					//dctx.unbind(baseDN);
				}

				dctx.close();

				logger.log(Level.INFO,"Resource "+" bound to "+baseDN);
			} catch (NamingException e) {
				logger.log(Level.SEVERE,"Error Binding Datasource "+e);
			}
		}
	}

}
