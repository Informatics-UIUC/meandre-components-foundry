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

import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

/*
 * <p>Title: JarXMLLoader</p>
 * <p>
 * This class provides services to read in an external xml file with information about jdbc driver jar files, datasource class names, driver class names, and user specified vendor names.
 * This file also writes an xml file containing this data. Finally, it can load a new jar file, moving it from the user specified directory into the meandre-store published_resources folder.
 * This folder is typically found in the meandre-install directory. For example meandre-infrastructure/trunk/dist/published_resources is the typical location of the xml configuation file and the jar file.
 * 
 * This allows a user to specify driver jar files needed at runtime for his or her datasources. 
 * The user also specifies the driver and datasource class within these jar files. 
 * Finally, the user specifies a label, such as 'my vendor' for this configuration
 * This data would be used during runtime. After completion, the JarXMLLoader will write the user's configuration to an xml file
 * in the meandre-store published resources directory. It will also copy the jar file from the user specified location to the published resources file so all jars are in a central location
 * On start up, the JarXMLLoader will read the xml file (if available) and load the correct driver and datasource classes into the classpath so the users can connect again without needing to reload the jar files. 
 *</p>
 *
 *<p>Company: NCSA, Automated Learning Group</p>
 * @author E Johnson
 * @version 1.0
 */

public class JarXMLLoader {

	 //==============
    // Data members
    //==============
	
	private Logger logger= Logger.getAnonymousLogger();
	
	//xml loader to read properties from the xml file
	private XMLLoader xmlLoader;
	
	//xml writer to write properties into xml file
	private XMLWriter xmlWriter;

	//properties object containing properties for all jars currently used
	private Properties allJarProps;
	
	//This static URL tracks the base URL of the meandre server (defaults to localhost port 1714
	private static URL serverURL;
	
	private int jarCount;
	
	//==============
    // Constructors
    //==============
	
	
	
	/** Default constructor, specifies xml file location
	 * @param xmlFileLoc String representing location of xml file (typically published_resources/myxmlname.xml)
	    */
	public JarXMLLoader(String xmlFileLoc){
			logger.log(Level.INFO,"Preparing XML Loader for Jar Files...");
			xmlLoader= new XMLLoader(xmlFileLoc);
			logger.log(Level.INFO,"...Jar XML Loader Ready");
			allJarProps = new Properties();
			jarCount = 0;
	}
	
	 //==============
    // Public Methods
    //==============
	
	/** This method loads the jar xml configuration file, loads the properties and puts the necessary classes in the classpath
	    */
	public void loadJars()
	{
		//attempt to read properties file
		logger.log(Level.INFO,"Attempting to read Jar Props File...");
		if(xmlLoader.readPropertiesFile())
		{
			logger.log(Level.INFO,"...Jar Props File Read");
			String Vendor = "";
			String DSClass = "";
			String DriverClass = "";
			String jarName = "";
			String pooling = "";
			//go through all objects in file (defined by the count)
			for (int i=0; i<xmlLoader.getObjCount(); i++)
			{
					//use xml loader to get data and properties
					Integer countInt = new Integer(i);
					try{
						Vendor = xmlLoader.getObjName(i);
						jarName = xmlLoader.getObjProperty(i, "Jar");
						DSClass = xmlLoader.getObjProperty(i, "DSClass");
						DriverClass = xmlLoader.getObjProperty(i,"DriverClass");
						pooling = xmlLoader.getObjProperty(i,"Pooling");
						logger.log(Level.INFO,"Loading Jar " +jarName);
						
						String jarURL = getPublicResourcesURL()+jarName;
						
						logger.log(Level.INFO,"Jar URL is :"+jarURL);
						
						//set properties- after this for loop, allJarProps will contain all the properties for all jar files loaded
						allJarProps.setProperty(countInt.toString(), Vendor);
						allJarProps.setProperty(Vendor+"_Jar", jarName);
						allJarProps.setProperty(Vendor+"_DSClass", DSClass);
						allJarProps.setProperty(Vendor+"_DriverClass", DriverClass);
						allJarProps.setProperty(Vendor+"_Pooling", pooling);
						//create a new datasource using these properties
	    			
						//use the datasourcefactory to put the driver and datasource classes on the classpath
	    				DataSourceFactory.addJarFile(jarURL);
	    				DataSourceFactory.loadJarClass(DSClass);
	    				DataSourceFactory.loadJarClass(DriverClass);
	    				DataSourceFactory.addNewDatabaseVendor(Vendor, DriverClass, DSClass, pooling);
	    			}
	    			catch (Exception e)
	    			{
	    				logger.log(Level.SEVERE,"There has been an error loading driver or Datasource from Jar "+e);
	    			}
			}
			jarCount = xmlLoader.getObjCount();
		}
	}
	
	/** This method accepts user supplied data on a new vendor driver jar file, saves the properties, then loads the properties and puts the necessary classes in the classpath
	    @param VendorName String VendorName- the user specified name of vendor, such as 'my postgres vendor'
	    @param DatasourceClass String datasourceclass- the name of the datasource class
	    @param DriverClass String DriverClass- the name of the driver class
	    @param JarFilePath String jarFilePath- user specified location of the vendor driver jarfile on the user's file system
	    @param destURL String destURL- the destination url of the jar file. Typically this is in the meandre install directory under the published_resources folder
	    @param JarName String JarName- the name of the vendor jar file
	    */
	public void addJar(String pooling, String VendorName, String DatasourceClass, String DriverClass, String JarFilePath, String destURL, String JarName)
	{
		//use xml loader to get data and properties
		Integer countInt = new Integer(jarCount);
		
		//check formating on destination dirctory, add '/' if necessary
		String fname = destURL;
		if ((!(fname.endsWith("/"))) && (!(fname.endsWith("\\")))) {
			fname += File.separator;
		}
		String jarDir = fname+JarName;
		String jarURL = getPublicResourcesURL()+JarName;
		
		if ((!(JarFilePath.endsWith("/"))) && (!(JarFilePath.endsWith("\\")))) {
			JarFilePath += File.separator;
		}
		String filePath = JarFilePath + JarName; 
		
		
		logger.log(Level.INFO,"Adding Jar " +JarName);
		allJarProps.setProperty(countInt.toString(), VendorName);
		allJarProps.setProperty(VendorName+"_Jar", JarName);
		allJarProps.setProperty(VendorName+"_DSClass", DatasourceClass);
		allJarProps.setProperty(VendorName+"_DriverClass", DriverClass);
		allJarProps.setProperty(VendorName+"_Pooling", pooling);
		//create a new datasource using these properties
		try{
			//copy jar file to published resources folder
			java.io.File origJarFile = new java.io.File (filePath).getAbsoluteFile();
			loadJar (destURL, JarName, origJarFile.toURL());
			//add jar file to datasourcefactory, this will load the class into the JVM
			DataSourceFactory.addJarFile(jarURL);
			DataSourceFactory.loadJarClass(DatasourceClass);
			DataSourceFactory.loadJarClass(DriverClass);
			DataSourceFactory.addNewDatabaseVendor(VendorName, DriverClass, DatasourceClass, pooling);
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE,"There has been an error loading driver or Datasource"+e);
		}
		
		logger.log(Level.INFO,"Jar Loaded");
		jarCount++;
	}
	/** This method formats a java URL and writest the jarfile from the jarURL location to the destination URL location
	 * Typically it will copy a jar from the file system to the meandre store published resources folder 
	@param destURL String destURL- a string version of the URL destination of the jar file (Typically the location of the meandre published resources directory)
    @param jarName String jarName- the full name of the jar to be loaded
    @param jarURL URL jarURL- the URL of the original jar location (typically on a user's file system)
    */
	protected void loadJar (String destURL, String jarName, URL jarURL)
	{
		//formate destination URL
		String fname = destURL;
		if ((!(fname.endsWith("/"))) && (!(fname.endsWith("\\")))) {
			fname += File.separator;
		}
		java.io.File jarFile = new java.io.File(fname + jarName);
		
		// if not, get the jar as a resource and write it to file
		if (!jarFile.exists()) {
			java.io.FileOutputStream fos = null;
			java.io.InputStream in = null;
			try {
				//create the file at the destination
				jarFile.createNewFile();
				//get input stream
				in = jarURL.openStream();
				if (in == null) {
					logger.log(Level.SEVERE, "Could not open input stream");
					return;
				}
				fos = new java.io.FileOutputStream(jarFile);
				byte[] bytes = new byte[4096];
				int n = -1;
				while ((n = in.read(bytes)) != -1) {
					fos.write(bytes, 0, n);
				}
				fos.flush();
			} catch (Exception e) {
				throw new RuntimeException(
						"ERROR: writing resource jar to file." +e, e);
			} finally {
				try {
					if (fos != null) {
						fos.flush();
						fos.close();
					}
					if (in != null) {
						in.close();
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/** This method writes the properties file for all jars to the specified xml location
	 * @param xmlLoc String xmlLoc- the full path to the desired xml file 
    */
	public void writePropsFile(String xmlLoc)
	{
		//Create xml writer
		xmlWriter = new XMLWriter (xmlLoc);
		try{
			//set properties and number of items, then write
			xmlWriter.addProperties(allJarProps);
			xmlWriter.setCount(jarCount);
			xmlWriter.writePropertiesFile();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Problem adding datasource to writer: " + e +":"+ e.getMessage());
		}
	}
	
	/** This static method returns the published resources directory. Typically this will be in the meandre install directory in a folder called published resources.
	 * By returning "public resources", the file object will resolve the path as ./public resources.
	 * TODO: make this more flexible, use a system property so code does not have to be modified to change it.
	 * @return a string containing the public resources directory on the file system. 
    */
	public static String getPublicResourcesDirectory()
	{
		String path = "published_resources"+File.separator+"contexts"+File.separator+"datasources";
		//if file cannot be found, create the directories
		File newFile = new File(path);
		if (!newFile.exists())
		{
			newFile.mkdirs();
		}
		return path;
	}
	
	public static void setServerURL(URL newServerURL)
	{
		serverURL = newServerURL;
	}
	
	public static void setServerURL()
	{
		try{
			serverURL = new URL ("http://localhost:1714");
		}
		catch (MalformedURLException e)
		{}
	}
	
	public static String getPublicResourcesURL()
	{
		String pubURL = serverURL.toString();
		if ((!(pubURL.endsWith("/"))) && (!(pubURL.endsWith("\\")))) {
			pubURL += File.separator;
		}
		return pubURL+"public/resources/contexts/datasources/";
	}
}

