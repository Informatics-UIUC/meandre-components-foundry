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

package org.seasr.meandre.support.components.io.datasource.database;

//Import statements
import java.io.File;
import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;

import org.seasr.datatypes.core.Names;
import org.seasr.meandre.support.components.io.datasource.DataSourceFactory;
import org.seasr.meandre.support.components.io.datasource.JNDINamespaceBuilder;
import org.seasr.meandre.support.components.io.datasource.JarXMLLoader;

import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

@Component(creator="Lily Dong",
        description="<p>Overview:<br>"
        +"This Component initializes a JNDI context for datasource object."
        +"This datasource object can then be used to connect to a database using the connect DB component."
        +"OpenDS Directory Server is recommended to use for JNDI. "
        +"Before running this component, starting up the server and creating a new entry for saving datasource object. "
        +"Then providing this compoennt with the username and password of server, and "
        +"the entry's distinguished name(DN).",
        name="Initialize JNDI Datsources",
        tags="JNDI, datasource, database, connect, db",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

/** This component will load information from two xml files and construct a JNDI context with datasource objects.
 * To do this, it
 *
 * @author Erik Johnson
 * @author Lily Dong
 */
public class InitJNDI extends AbstractExecutableComponent {
	//------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
			description="File of datasource xml file in published resources directory. If one does not exist, it will be created there on close.",
		    name=Names.PROP_XML_LOCATION,
		    defaultValue="myxml.xml"
	)
	final static String PROP_XML_LOCATION = Names.PROP_XML_LOCATION;

	@ComponentProperty(
			description="Username of OpenDS Directory Server.",
		    name=Names.PROP_USERNAME,
		    defaultValue="cn=Directory Manager"
	)
	final static String PROP_USERNAME = Names.PROP_USERNAME;

	@ComponentProperty(
			description="Passward of OpenDS Directory Server.",
		    name=Names.PROP_PASSWORD,
		    defaultValue=""
	)
	final static String PROP_PASSWORD = Names.PROP_PASSWORD;

	@ComponentProperty(
			description="Entry's unique identifier or distinguished name " +
			"in OpenDS Directory Server. The datasource object is bound to this entry.",
		    name=Names.PROP_BASE_DN,
		    defaultValue=""
	)
	final static String PROP_BASE_DN = Names.PROP_BASE_DN;

	//--------------------------------------------------------------------------------------------

    //object to populate JNDI namespace with data read from xmlLocation property file
    protected JNDINamespaceBuilder nB;

    //Jar loader to load an xml file with jar location and jar class data
    protected org.seasr.meandre.support.components.io.datasource.JarXMLLoader jarLoader;

    //default xml file location for jar location data. If it does not exist, it will be written at the end of use
    protected String jarXMLFile = "JarProps.xml";

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

    @Override
    public void executeCallBack(ComponentContext ccp) throws Exception {
    	//start up logger
     	console.log(Level.INFO, "Initializing JNDI Namespace...");

     	//initialize the vendor database information in the Datasource factory BEFORE attempting to access it while loading jar files
     	DataSourceFactory.initDatabases();

     	//set the server URL
     	try{
     		JarXMLLoader.setServerURL(ccp.getWebUIUrl ( true ));
     	}
     	catch (ComponentContextException e)
     	{
     		console.log(Level.WARNING, "Could not get WebUIURL, using default server localhost at port 1714");
     		JarXMLLoader.setServerURL();
     	}

     	//Load jar file from meandre-store public resources directory located in the meandre-install-directory/published_resources
     	//do this before attempting to create datsource classes with these files
     	String fdir = JarXMLLoader.getPublicResourcesDirectory();

     	//String fname = JarXMLLoader.getPublicResourcesURL();
		//append a '/' to the path
     	if ((!(fdir.endsWith("/"))) && (!(fdir.endsWith("\\")))) {
     		fdir += File.separator;
		}

     	//filepath for xml file containing jar configuration and location details
		String xmldir = fdir+jarXMLFile;

		console.log(Level.INFO, "Loading Jars....");
		jarLoader = new JarXMLLoader (xmldir);
		jarLoader.loadJars();
		console.log(Level.INFO, "...Jars Loaded");

		//initialize vendor list
		//get user defined xml configuration file for datasource objects from property
		String xmlLoc = ccp.getProperty(PROP_XML_LOCATION);

		//create path- xml datasource file is in same directory as xml jar file
		String dsPath = fdir + xmlLoc;
		File dsfile = new File(dsPath).getAbsoluteFile();

		//Use JNDINamespaceBuilder to load xml and populate namespace
		console.log(Level.INFO,"Preparing to load xml file at "+xmlLoc);

		String username = ccp.getProperty(PROP_USERNAME);
		String password = ccp.getProperty(PROP_PASSWORD);
		String dn = ccp.getProperty(PROP_BASE_DN);

		nB = new JNDINamespaceBuilder(
				dsfile.toString(), username, password, dn);
		console.log(Level.INFO,"Building Namespace");
		nB.buildNamespace();

		console.log(Level.INFO, "...JNDI Datasources Initialized");
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
