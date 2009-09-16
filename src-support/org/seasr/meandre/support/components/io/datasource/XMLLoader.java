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

import java.util.logging.Logger;
import java.util.logging.Level;

import java.net.URL;

import java.io.File;
import java.io.FileInputStream;
/*
 * <p>Title: JNDIXMLLoader</p>
 * <p>
 * This class provides functionality to connect to an xml file containing configuration data for user created datasources. The xml file should be in the user's file system.
 * This class can be used for any object in a xml file, as long as it follows the following format:
 * There is a 'count' element with the total number of objects in the file.
 * Each index has an entry where the value is the logical name of the object 
 * Each property is then labeled with the logical name (for examle, MyDatasource_PortNumber or postgres datasource_Password)
 * These property names should match the get and set names for the class as reflection will be used to locate these methods to instantiate the classes.
 * The best way to format these objects is using an xml loader and xml writer together. A wrapper class will probably be necesary to accomodate the instantiation of specific objects
 * 
 * This way a user can use an index to move through the objects in a file, get their names and properties. These properties can then be used by the developer to instantiate new objects
 * 
 * Here is an example XML file:
 * notice that count contains the total number of datasource objects
 * also not that entry '0' and '1' are assigned to the logical names of the objects
 * These logical names then serve as tags to discover the rest of the properties.
 * 
 *<?xml version="1.0" encoding="UTF-8" standalone="no" ?> 
  <!DOCTYPE properties (View Source for full doctype...)> 
- <properties version="1.0">
 *<entry key="mydb_logintimeout">0</entry> 
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

 *
 *</p>
 *
 *<p>Company: NCSA, Automated Learning Group</p>
 * @author E Johnson
 * @version 1.0
 */

public class XMLLoader {

	 //==============
    // Data members
    //==============
	
	private Logger logger= Logger.getAnonymousLogger();
	
	//string path to xml file on user's file system
	private String xmlFile;
	
	//the properties of all the datasource elements in the xml file
	private Properties allProps;
	
	//the number of datasource elements in the xml file
	private int objCount;
	
	//a flag if the file has been loaded or not
	private boolean loadedFile;

	//==============
    // Constructors
    //==============
	/** Constructor with String xmlFileLoc specifies the file path to the xml file. 
	 * If the file is not there, the program will continue with a blank JNDI namespace
	 * @param xmlFileLoc String representing the location of the xml file on the user's machine
	    */
	public XMLLoader (String xmlFileLoc)
	{
		xmlFile = xmlFileLoc;
		objCount =0;
		allProps = new Properties();
		loadedFile = false;
	}
	
	/** This method returns a count of the number of datasource objects in the xml file
	 * @return Integer number of datasources
	    */
	public int getObjCount()
	{
		//if the file has been loaded
		if (loadedFile)
		{
			return objCount;
		}
		else 
			return 0;
	}
	
	/** This method returns the jndi name for a datasource at a given index
	 * @param index the index of the datasource
	 * @return the jndi location
	    */
	public String getObjName(int index)
	{
		//if the file is loaded and the index is valid
		if (loadedFile && index<=objCount)
		{
			Integer indexInt = new Integer (index);
			return allProps.getProperty(indexInt.toString());
		}
		else
			return "";
	}
	
	/** This method returns the selected property for the object at index
	 * @param index the index of the datasource
	 * @param propName the name of the property
	 * @return the datasource class
	    */
	
	public String getObjProperty(int index, String propName)
	{
		//if the file is loaded and the index is valid
		if (loadedFile && index<=objCount)
		{
			//return a specific property
			return allProps.getProperty(getObjName(index)+"_"+propName);
		}
		else
			return "";
	}
	
	/** This method returns the datasource properties for an object in the xml file at a given index
	 * @param index the index of the datasource
	 * @return the datasource properties
	    */
	public Properties getObjProps(int index)
	{
		Properties currentProps = new Properties();
		String currentObj = getObjName(index);
		//if file is loaded and index valid
		if (loadedFile && index<=objCount)
		{
			Enumeration keyEnum = allProps.keys();
			String key="";
			//move through all elements
			while (keyEnum.hasMoreElements())
			{
				//get the name of the current element
				key = (String) keyEnum.nextElement();
				//if the key starts with the object name, then it is a property for that object
				if (key.startsWith(currentObj))
				{
					//check for null then add to properties
					if (allProps.getProperty(key)!= null){
						currentProps.setProperty(key.substring(currentObj.length()+1), allProps.getProperty(key));
					}
				}
			}
			//return the properties
			return currentProps;
		}
		else{
			logger.log(Level.WARNING,"Could not get datasource properties");
			return null;
		}
	}
	
	/**
	 * Blindly attempts to read properties from file at xml location
	 * 
	 * @return boolean: true if read, false if error occured
	 */
	public boolean readPropertiesFile(){
		logger.log(Level.INFO,"Attempting to read Properties File from "+xmlFile);
		java.io.InputStream is;
		try {
			//
				//URL xmlURL = new URL (xmlFile);
				//is = xmlURL.openStream();				
				is =new java.io.FileInputStream(new File (xmlFile));
				//use properties object load from xml file to get the properties
				allProps.loadFromXML(is);
				is.close();
				is = null;
			//
		} catch (Exception e){
			logger.log(Level.WARNING," XML File "+xmlFile+" could not be read, will attempt to create on close");
			e.printStackTrace();
			return false;
		}
		logger.log(Level.INFO, "Datasource properties file read");
		//set count
		objCount = Integer.parseInt(allProps.getProperty("Count"));
		//set loaded file flag
		loadedFile = true;
		return true;
	}
}
