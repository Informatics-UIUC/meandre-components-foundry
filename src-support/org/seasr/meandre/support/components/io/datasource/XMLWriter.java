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

import java.util.Properties;
import java.util.Enumeration;

import java.io.File;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.net.URL;
import java.net.URLConnection;

/*
 * <p>Title: JNDIXMLWriter</p>
 * <p>
 * This class writes datasource objects out to a xml file. 
 * This can be used generally to write any object to a file. 
 * The developer will need to write a wrapper class to discover that particular objects properties then add them to the writer.
 * This should be used together with an xml Loader to provide persistance capabilities.
 * This class used the properties class method storeToXML to store properties in the file
 * 
 * If the developer uses the addObject method, the class will automatically format the properties as below, and add the appropriate indexing.
 * Alternatively, the developer can populate their own properties element and count and use the set functions- this will not, however, format or count the objects.
 * 
 * To be used properly with an xml loader, the writer maintains a count of objects added
 * This count is added as a count property, representing the total number of objects in the file
 * Each index has an entry containing the logical name of the object
 * this name is then used to tag the rest of the properties, for example, the 'user' element for 'my datasource' becomes 'my datasource_user'.
 * 
 * Here's an example:
 * Note that the 'Count' property is 2- this is the number of objects
 * index '0' is assigned to 'mydb2' and index '1' to 'mydb'
 *   <?xml version="1.0" encoding="UTF-8" standalone="no" ?> 
  <!DOCTYPE properties (View Source for full doctype...)> 
 <properties version="1.0">
  <comment /> 
  <entry key="mydb_logintimeout">0</entry> 
  <entry key="mydb_description">a test db</entry> 
  <entry key="mydb_databasename">C:/db/test</entry> 
  <entry key="mydb_datasourcename">My test</entry> 
  <entry key="mydb2_DSClass">org.apache.derby.jdbc.EmbeddedDataSource40</entry> 
  <entry key="Count">2</entry> 
  <entry key="mydb2_datasourcename">my second test</entry> 
  <entry key="mydb_createdatabase">create</entry> 
  <entry key="mydb2_logintimeout">0</entry> 
  <entry key="mydb2_databasename">C:/db/test2</entry> 
  <entry key="mydb2_attributesaspassword">false</entry> 
  <entry key="mydb2_createdatabase">create</entry> 
  <entry key="mydb_attributesaspassword">false</entry> 
  <entry key="mydb_DSClass">org.apache.derby.jdbc.EmbeddedDataSource40</entry> 
  <entry key="1">mydb</entry> 
  <entry key="0">mydb2</entry> 
  <entry key="mydb2_description">another db</entry> 
  </properties>
 *</p>
 *
 *<p>Company: NCSA, Automated Learning Group</p>
 * @author E Johnson
 * @version 1.0
 */

public class XMLWriter {

	 //==============
    // Data members
    //==============
	
	//location of xml file
	private String xmlFile;
	
	//properties object representing all the datasource objects to be written
	private Properties allProps;
	
	//object count
	private int objCount;
	
	private Logger logger= Logger.getAnonymousLogger();
	
	 //==============
    // Constructors
    //==============
	
	/** Constructor with String xmlFileLoc specifies the file path to the xml file. 
	 * If the file is not there, the program will attempt to create it
	 * @param xmlFileLoc String representing the location of the xml file on the user's machine
	    */
	public XMLWriter (String xmlFileLoc)
	{
		xmlFile = xmlFileLoc;
		objCount =0;
		allProps = new Properties();
	}
	
	 //==============
    // public methods
    //==============
	
	/** This method adds an object to the writer to be written 
	 * @param objName String representing JNDILoc
	 * @param objProps the properties of the object to be written
	    */
	public void addObject (String objName, Properties objProps)
	{
		Integer objInt = new Integer(objCount);
		allProps.setProperty(objInt.toString(), objName);
		Enumeration keyEnum = objProps.keys();
		String key;
		while (keyEnum.hasMoreElements())
		{
			key = (String)keyEnum.nextElement();
			if (objProps.getProperty(key)!= null && !objProps.getProperty(key).toString().equalsIgnoreCase("null") && !objProps.getProperty(key).toString().equalsIgnoreCase(""))
				allProps.setProperty(objName+"_"+key, objProps.getProperty(key));
		}
		objCount++;
	}
	
	/** This method sets the properties object to be written. 
	 * It must be properly formatted by the developer. 
	 * The developer does not have to add a 'count' entry to the property object, 
	 * but should use the setCount method 
	 * @param allObjProps Properties object to be written
	    */
	public void addProperties (Properties allObjProps)
	{
		allProps = allObjProps;
	}
	/** This method sets the object count 
	 * @param count integer Count of objects
	    */
	public void setCount (int count)
	{
		objCount = count;
	}
	
	/** This method writes the datasources that have been added to the xml file
	 * @return Boolean: true if write is sucessful, otherwise false. 
	    */
	public boolean writePropertiesFile()	{
		//add one last property- total number of props added
		Integer dbInt = new Integer(objCount);
		allProps.setProperty("Count", dbInt.toString());
				
		try {
			//java.io.OutputStream os;
			//logger.log(Level.INFO,"Writing to :"+ xmlFile);
			//os =new java.io.FileOutputStream(new File (xmlFile));				
			//URL outURL = new URL (xmlFile);
			//URLConnection urlConn = outURL.openConnection();
			//urlConn.setDoOutput(true);
			//os = urlConn.getOutputStream();
			//allProps.storeToXML(os, ""); //write no comments
			//os.close();
			//os = null;
			java.io.OutputStream os;
			logger.log(Level.INFO,"Writing to :"+ xmlFile);
			os =new java.io.FileOutputStream(new File (xmlFile));				
			allProps.storeToXML(os, "");
			os.close();
			os = null;	
		} catch (Exception e){
			logger.log(Level.SEVERE,"Warning: could not write to "+xmlFile+" data will not be persisted "+e);			
			return false;
		}
		logger.log(Level.INFO,"Successfully wrote to "+xmlFile);
		return true;
	}
	
	/** This method writes the datasources that have been added to the xml file
	 * @param comments String containing comments
	 * @return Boolean: true if write is sucessful, otherwise false. 
	    */
	public boolean writePropertiesFile(String comments)	{
		//add one last property- total number of props added
		Integer dbInt = new Integer(objCount);
		allProps.setProperty("Count", dbInt.toString());
				
		try {
			//java.io.OutputStream os;
			//logger.log(Level.INFO,"Writing to :"+ xmlFile);
			//os =new java.io.FileOutputStream(new File (xmlFile));				
			//URL outURL = new URL (xmlFile);
			//URLConnection urlConn = outURL.openConnection();
			//urlConn.setDoOutput(true);
			//os = urlConn.getOutputStream();
			//allProps.storeToXML(os, ""); //write no comments
			//os.close();
			//os = null;	
			java.io.OutputStream os;
			logger.log(Level.INFO,"Writing to :"+ xmlFile);
			os =new java.io.FileOutputStream(new File (xmlFile));				
			allProps.storeToXML(os, comments);
			os.close();
			os = null;	
		} catch (Exception e){
			logger.log(Level.SEVERE,"Warning: could not write to "+xmlFile+" data will not be persisted "+e.getMessage());		
			return false;
		}
		logger.log(Level.INFO,"Successfully wrote to "+xmlFile);
		return true;
	}
	
}