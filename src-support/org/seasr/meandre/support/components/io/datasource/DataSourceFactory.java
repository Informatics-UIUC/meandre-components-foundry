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
import javax.sql.*;

import java.sql.*;

import java.util.Properties;
import java.util.Vector; 
import java.util.Enumeration;
import java.lang.reflect.*;
import java.net.URL;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.net.MalformedURLException;

import java.io.InputStream;

import java.util.Hashtable;

/*
 * <p>Title: DataSourceFactory</p>
 * 
 * This is a critical part of the code for meandre database lookups in webapps.
 * This class provides static services to load and store vendor names, datasource classes 
 * and driver classes.
 * 
 * From these names, it can create general datasources, discover the properties of a datasource,
 * get connections to datasources, get connection properties, and create new connections using 
 * new properties.
 * 
 * Using the ExternalJarLoader, functionality is provided to look for and load driver and 
 * datasource classes
 * 
 * All the properties are found dynamically using reflection. 
 * This should allow it to work for any datasource class.
 * 
 *</p>
 *
 *<p>Company: NCSA, Automated Learning Group</p>
 * @author E Johnson
 * @version 1.0
 */

public class DataSourceFactory {
	
	//==============
    // Data Members
    //==============
	
	private static Logger logger= Logger.getAnonymousLogger();
	
	//vector of known vendors, vendor drivers, and vendor datasource classes
	private static Vector<String> knownDatabaseVendors;
	private static Vector<String> commonDatabaseVendors;
	private static Hashtable<String,Vector<String>> knownDatabaseProperties;
	
	//class loader for external jars, allows users to add unknown vendors at runtime
	public static ExternalJarLoader jdbcLoader = new ExternalJarLoader (new URL [] {});
	
    //================
    // Public Methods
    //================
	//Initialize vendor list with known vendors and classes
	//They may not be installed- components must check for installation at run time and allow the user to specify a jar file if they are not installed
    /** This method initializes the private knownDatabaseVendor vector, 
     * recording some known vendor names, driver classes, and datasource classes.
     * This method does not load these classes and does not guarantee that they are available.
     * They may have to be loaded from an external jar. 
     *     */
	public static void initDatabases()
	{
	knownDatabaseVendors=new Vector<String>();
	commonDatabaseVendors=new Vector<String>();
	
	knownDatabaseVendors.add("Apache Derby Client");
	knownDatabaseVendors.add("org.apache.derby.jdbc.ClientDriver");
	knownDatabaseVendors.add("org.apache.derby.jdbc.ClientDataSource40");
	knownDatabaseVendors.add("false");

	knownDatabaseVendors.add("Apache Derby Client- With Pooling");
	knownDatabaseVendors.add("org.apache.derby.jdbc.ClientDriver");
	knownDatabaseVendors.add("org.apache.derby.jdbc.ClientConnectionPoolDataSource40");
	knownDatabaseVendors.add("true");//vendor uses pooling
	
	knownDatabaseVendors.add("Apache Derby Embedded");
	knownDatabaseVendors.add("org.apache.derby.jdbc.EmbeddedDriver");
	knownDatabaseVendors.add("org.apache.derby.jdbc.EmbeddedDataSource40");
	knownDatabaseVendors.add("false");
	
	knownDatabaseVendors.add("Apache Derby Embedded- With Pooling");
	knownDatabaseVendors.add("org.apache.derby.jdbc.EmbeddedDriver");
	knownDatabaseVendors.add("org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40");
	knownDatabaseVendors.add("true");//vendor uses pooling
	
	commonDatabaseVendors.add("Apache Derby Client");
	commonDatabaseVendors.add("org.apache.derby.jdbc.ClientDriver");
	commonDatabaseVendors.add("org.apache.derby.jdbc.ClientDataSource40");
	commonDatabaseVendors.add("false");

	commonDatabaseVendors.add("Apache Derby Client- With Pooling");
	commonDatabaseVendors.add("org.apache.derby.jdbc.ClientDriver");
	commonDatabaseVendors.add("org.apache.derby.jdbc.ClientConnectionPoolDataSource40");
	commonDatabaseVendors.add("true");//vendor uses pooling
	
	commonDatabaseVendors.add("Apache Derby Embedded");
	commonDatabaseVendors.add("org.apache.derby.jdbc.EmbeddedDriver");
	commonDatabaseVendors.add("org.apache.derby.jdbc.EmbeddedDataSource40");
	commonDatabaseVendors.add("false");
	
	commonDatabaseVendors.add("Apache Derby Embedded- With Pooling");
	commonDatabaseVendors.add("org.apache.derby.jdbc.EmbeddedDriver");
	commonDatabaseVendors.add("org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40");
	commonDatabaseVendors.add("true");//vendor uses pooling
	
	commonDatabaseVendors.add("Oracle DB");
	commonDatabaseVendors.add("oracle.jdbc.driver.OracleDriver");
	commonDatabaseVendors.add("oracle.jdbc.pool.OracleDataSource");
	commonDatabaseVendors.add("false");
	
	commonDatabaseVendors.add("MySQL DB");
	commonDatabaseVendors.add("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
	commonDatabaseVendors.add("com.mysql.jdbc.Driver");
	commonDatabaseVendors.add("false");
	
	commonDatabaseVendors.add("Postgresql DB- no pooling");
	commonDatabaseVendors.add("org.postgresql.jdbc2.optional.SimpleDataSource");
	commonDatabaseVendors.add("org.postgresql.driver");
	commonDatabaseVendors.add("false");
	
	commonDatabaseVendors.add("Postgresql DB- pooling");
	commonDatabaseVendors.add("org.postgresql.jdbc2.optional.PoolingDataSource");
	commonDatabaseVendors.add("org.postgresql.driver");
	commonDatabaseVendors.add("true");//vendor uses pooling
	
	knownDatabaseProperties=new Hashtable<String,Vector<String>>();
	
	Vector<String> postGresProps=new Vector<String>() ;
	postGresProps.add("ServerName");
	postGresProps.add("Server Name");
	postGresProps.add("String");
	postGresProps.add("The hostname of the database server");
	postGresProps.add("DatabaseName");
	postGresProps.add("Database Name");
	postGresProps.add("String");
	postGresProps.add("The name of the database on database server");
	postGresProps.add("PortNumber");
	postGresProps.add("Port Number");
	postGresProps.add("int");
	postGresProps.add("The port on the database server");
	postGresProps.add("User");
	postGresProps.add("User");
	postGresProps.add("String");
	postGresProps.add("The user name for database");
	postGresProps.add("Password");
	postGresProps.add("Password");
	postGresProps.add("String");
	postGresProps.add("The password for this user");
	postGresProps.add("DataSourceName");
	postGresProps.add("Data Source Name");
	postGresProps.add("String");
	postGresProps.add("A unique identifier for this datasource object (i.e. \"My Database\"");
	
	knownDatabaseProperties.put("Postgresql DB- no pooling",postGresProps);
	
	Vector<String> postGresPooling=new Vector<String>() ;
	postGresPooling.add("ServerName");
	postGresPooling.add("Server Name");
	postGresPooling.add("String");
	postGresPooling.add("The hostname of the database server");
	postGresPooling.add("DatabaseName");
	postGresPooling.add("Database Name");
	postGresPooling.add("String");
	postGresPooling.add("The name of the database on database server");
	postGresPooling.add("PortNumber");
	postGresPooling.add("Port Number");
	postGresPooling.add("int");
	postGresPooling.add("The port on the database server");
	postGresPooling.add("User");
	postGresPooling.add("User");
	postGresPooling.add("String");
	postGresPooling.add("The user name for database");
	postGresPooling.add("Password");
	postGresPooling.add("Password");
	postGresPooling.add("String");
	postGresPooling.add("The password for this user");
	postGresPooling.add("DataSourceName");
	postGresPooling.add("Data Source Name");
	postGresPooling.add("String");
	postGresPooling.add("A unique identifier for this datasource object (i.e. \"My Database\"");
	postGresPooling.add("MaxConnections");
	postGresPooling.add("Maximum Number of Connections");
	postGresPooling.add("int");
	postGresPooling.add("Maximum number of connections for this pooling datasource");
	
	knownDatabaseProperties.put("Postgresql DB- pooling",postGresPooling);
	
	Vector<String> oracle=new Vector<String>() ;
	oracle.add("ServerName");
	oracle.add("Server Name");
	oracle.add("String");
	oracle.add("The hostname of the database server");
	oracle.add("DatabaseName");
	oracle.add("Database Name");
	oracle.add("String");
	oracle.add("The name of the database on database server");
	oracle.add("PortNumber");
	oracle.add("Port Number");
	oracle.add("int");
	oracle.add("The port on the database server");
	oracle.add("User");
	oracle.add("User");
	oracle.add("String");
	oracle.add("The user name for database");
	oracle.add("Password");
	oracle.add("Password");
	oracle.add("String");
	oracle.add("The password for this user");
	oracle.add("DataSourceName");
	oracle.add("Data Source Name");
	oracle.add("String");
	oracle.add("A unique identifier for this datasource object (i.e. \"My Database\"");
	
	knownDatabaseProperties.put("Oracle DB",oracle);
	
	Vector<String> mysql=new Vector<String>() ;
	mysql.add("ServerName");
	mysql.add("Server Name");
	mysql.add("String");
	mysql.add("The hostname of the database server");
	mysql.add("DatabaseName");
	mysql.add("Database Name");
	mysql.add("String");
	mysql.add("The name of the database on database server");
	mysql.add("PortNumber");
	mysql.add("Port Number");
	mysql.add("int");
	mysql.add("The port on the database server");
	mysql.add("User");
	mysql.add("User");
	mysql.add("String");
	mysql.add("The user name for database");
	mysql.add("Password");
	mysql.add("Password");
	mysql.add("String");
	mysql.add("The password for this user");
	mysql.add("DataSourceName");
	mysql.add("Data Source Name");
	mysql.add("String");
	mysql.add("A unique identifier for this datasource object (i.e. \"My Database\"");
	
	knownDatabaseProperties.put("MySQL DB",mysql);
	
	
	Vector<String> derbyclient=new Vector<String>() ;
	derbyclient.add("DatabaseName");
	derbyclient.add("Database Name");
	derbyclient.add("String");
	derbyclient.add("The name of the database on database server");
	derbyclient.add("User");
	derbyclient.add("User");
	derbyclient.add("String");
	derbyclient.add("The user name for database");
	derbyclient.add("Password");
	derbyclient.add("Password");
	derbyclient.add("String");
	derbyclient.add("The password for this user");
	derbyclient.add("DataSourceName");
	derbyclient.add("Data Source Name");
	derbyclient.add("String");
	derbyclient.add("A unique identifier for this datasource object (i.e. \"My Database\"");
	derbyclient.add("CreateDatabase");
	derbyclient.add("Create Database");
	derbyclient.add("String");
	derbyclient.add("Create a New Derby Database, type \'create\' to create the database");
	derbyclient.add("ShutdownDatabase");
	derbyclient.add("Shutdown Database");
	derbyclient.add("String");
	derbyclient.add("Shutdown the Derby Database, type \'shutdown\' to shutdown the database");
	
	knownDatabaseProperties.put("Apache Derby Client",derbyclient);
	knownDatabaseProperties.put("Apache Derby Embedded",derbyclient);
	knownDatabaseProperties.put("Apache Derby Embedded- With Pooling",derbyclient);
	knownDatabaseProperties.put("Apache Derby Client- With Pooling",derbyclient);
	}

	//
	// Wrapper for jdbcLoader, adds path to jarFile, the path should be a url, 
	// such as http://localhost:1714/published/resources/contexts/mycontext.jar
	//
    /** This method adds a external jar file path to the class loader
     *
     * @param jarFile string representation of jar file location
     * @throws MalformedURLException URL has been incorrectly formatted
     */
	public static void addJarFile (String jarFile) throws MalformedURLException
	{
		jdbcLoader.addFile (jarFile);
	}
	
    /** This method returns an input stream for the resource given by filename
    *
    * @param fileName file name of resource, such as jar file, to find in class path
    * @return Input stream for resource
    */
	public static InputStream getInputStreamfromJarLoader(String fileName)
	{
		return jdbcLoader.getResourceAsStream(fileName);
	}
	
    /** This finds an external resource on the jdbcLoader's classpath
    *
    * @param fileName file name for resource, such as jar file, to find in class path
    * @return URL for the given resource
    */
	public static URL findResource(String fileName)
	{
		return jdbcLoader.findResource(fileName);
	}
	
	//Loads jarFile using jdbcLoader, linking it for use. After sucessful completion, the class can be looked up using Class.forName
    /** This method loads a class from a jar file that has been added
     *
     * @param className class name for classLoader to load
     */
	public static void loadJarClass (String className)
	{
		try{
			logger.log(Level.INFO,"Attempting to load class "+className);
			
			//ask jdbc loader to load class into JVM
			Class<?> c = jdbcLoader.loadClass (className);
			
			logger.log(Level.INFO,"Class "+c.getName() +"has been loaded.");
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE,"There has been an error loading class "+e);
		}
	}
	
	//wrapper to load drivers and datasources from jdbcloader using Class.forName
    /** This method uses external jar classloader to load a specified class
     *
     * @param className string representing full class name 
     * @throws ClassNotFoundException
     * @return specified class object
     */
	public static Class<?> getClassForName (String className) throws ClassNotFoundException
	{
		return Class.forName(className, true, jdbcLoader);//specify classLoader as jdbcLoader
	}
	
	//Return known drivers
    /** This method fills a vector with known drivers (drivers registered with driver manager)
     *
     * @return vector of strings containing known driver classes
     */
	public static Vector<String> getKnownDrivers()
	{
		Vector<String> driverNames= new Vector<String>();
		//get registered drivers from driver manager
		Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements())
		{
			driverNames.add(drivers.nextElement().getClass().getName());
		}
		return driverNames;
	}

	//return known vendors from list
    /** This method fills a vector with known vendor names
    *
    * @return vector of strings containing known vendor names
    */
	public static Vector<String> getCommonVendors()
	{

		Vector<String> commonNames= new Vector<String>();
		for (int i=0; i<commonDatabaseVendors.size(); i+=4)
		{
			commonNames.add(commonDatabaseVendors.elementAt(i));
		}
		return commonNames;
	}
	
	public static boolean isCommonVendor(String vendorName)
	{
		for (int i=0; i<commonDatabaseVendors.size(); i+=4)
		{
			if (commonDatabaseVendors.elementAt(i).equalsIgnoreCase(vendorName))
				return true;
		}
		return false;
	}
	
	public static String getCommonDriver(String vendorName)
	{
		for (int i=0; i<commonDatabaseVendors.size(); i+=4)
		{
			if(commonDatabaseVendors.elementAt(i).equalsIgnoreCase(vendorName)){
				return commonDatabaseVendors.elementAt(i+1);
			}
		}
		return null;
	}

	public static String getCommonDatasource(String vendorName)
	{
		for (int i=0; i<commonDatabaseVendors.size(); i+=4)
		{
			if(commonDatabaseVendors.elementAt(i).equalsIgnoreCase(vendorName)){
				return commonDatabaseVendors.elementAt(i+2);
			}
		}
		return null;
	}
	
	public static String getCommonPooling(String vendorName)
	{
		for (int i=0; i<commonDatabaseVendors.size(); i+=4)
		{
			if(commonDatabaseVendors.elementAt(i).equalsIgnoreCase(vendorName)){
				return commonDatabaseVendors.elementAt(i+3);
			}
		}
		return null;
	}
	
	public static Vector<?> getCommonProps(String vendorName)
	{
		Vector <?> tempVector = (Vector<?>)(knownDatabaseProperties.get(vendorName));
		return (Vector<?>) tempVector;
	}
	//return known vendors from list
    /** This method fills a vector with known vendor names
    *
    * @return vector of strings containing known vendor names
    */
	public static Vector<String> getKnownVendors()
	{
		Vector<String> vendorNames= new Vector<String>();
		for (int i=0; i<knownDatabaseVendors.size(); i+=4)
		{
			vendorNames.add(knownDatabaseVendors.elementAt(i));
		}
		return vendorNames;
	}
	
	//return the current driver class name from known vendors list given vendor name
    /** This method returns a string with the vendor driver class for the given name
    *
    * @param vName string of vendor name
    * @return string with driver class for that vendor name
    */
	public static String getCurrentDriver(String vName)
	{
		
		for (int i=0; i<knownDatabaseVendors.size(); i+=4)
		{
			if(knownDatabaseVendors.elementAt(i).equalsIgnoreCase(vName)){
				//make sure driver is loaded first
				addNewDriver(knownDatabaseVendors.elementAt(i+1));
				return knownDatabaseVendors.elementAt(i+1);
			}
		}
		return null;
	}
	
	//return the current driver class name given datasource name
    /** This method finds the current driver class name given the datasource class name
    *
    * @param vDatasource string of datasource class name
    * @return string of driver class name
    */
	public static String getCurrentDriverFromDatasource(String vDatasource)
	{
		
		for (int i=2; i<knownDatabaseVendors.size(); i+=4)
		{
			if(knownDatabaseVendors.elementAt(i).equalsIgnoreCase(vDatasource)){
				return knownDatabaseVendors.elementAt(i-1);
			}
		}
		return null;
	}
	
	//return the current datasource class name given vendor name
    /** This method returns a string with the datasource class name given a vendor name
    *
    * @param vName string of logical vendor name
    * @return string containing datasource class
    */
	public static String getCurrentDatasource(String vName)
	{
		for (int i=0; i<knownDatabaseVendors.size(); i+=4)
		{
			if(knownDatabaseVendors.elementAt(i).equalsIgnoreCase(vName))
				return knownDatabaseVendors.elementAt(i+2);
		}
		return null;
	}
	
	//return current datasource class name from given driver class name
    /** This method returns a string with the current datasource class name for a given vendor driver class 
    *
    * @param vDriver string of vendor driver class name
    * @return string containing datasource class
    */
	public static String getCurrentDatasourceFromDriver(String vDriver)
	{
		for (int i=1; i<knownDatabaseVendors.size(); i+=4)
		{
			if(knownDatabaseVendors.elementAt(i).equalsIgnoreCase(vDriver))
				return knownDatabaseVendors.elementAt(i+1);
		}
		return null;
	}
	
	public static boolean isPooled(String vName)
	{
		for (int i=0; i<knownDatabaseVendors.size(); i+=4)
		{
			if(knownDatabaseVendors.elementAt(i).equalsIgnoreCase(vName))
			{
				if (knownDatabaseVendors.elementAt(i+3).equalsIgnoreCase("true"))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}
	
	public static boolean isPooledfromDriver(String vDriver)
	{
		for (int i=1; i<knownDatabaseVendors.size(); i+=4)
		{
			if(knownDatabaseVendors.elementAt(i).equalsIgnoreCase(vDriver))
			{
				if (knownDatabaseVendors.elementAt(i+2).equalsIgnoreCase("true"))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}
	
	public static boolean isPooledfromDataSource(String vDataSource)
	{
		for (int i=2; i<knownDatabaseVendors.size(); i+=4)
		{
			if(knownDatabaseVendors.elementAt(i).equalsIgnoreCase(vDataSource))
			{
				if (knownDatabaseVendors.elementAt(i+1).equalsIgnoreCase("true"))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}
	
	//checks to see if datasource is known- can it be loaded by Class.forName
    /** This method checks to see if a datasource class can be loaded by the class loader
     * If it can, it returns true.
     * If it cannot, it returns false.
    *
    * @param vDataSource string of datasource class name
    * @return boolean true if datasource can be loaded by class loader, false if it cannot 
    */
	public static boolean isKnownDataSource(String vDataSource)
	{
		try{
			Class.forName (vDataSource, true, jdbcLoader);
		}
		catch (ClassNotFoundException e)
		{
			logger.log(Level.WARNING,"Class could not be found "+e);
			return false;
		}
		return true;
	}
	
	//checks to see if driver is known- is it registered with driver manager
	//Changed- use class.forName function rather than querying driver manager
    /** This method checks to see if a driver class can be loaded by the class loader
     * If it can, it returns true.
     * If it cannot, it returns false.
    *
    * @param sDriver string of driver class name
    * @return boolean true if driver can be loaded by class loader, false if it cannot 
    */
	public static boolean isKnownDriver(String sDriver)
	{
		try{
			Class.forName (sDriver, true, jdbcLoader);
		}
		catch (ClassNotFoundException e)
		{
			logger.log(Level.WARNING,"Dirver could not be found "+ e);
			return false;
		}
		return true;
	}


	
	//Add in new vendor from user's specified information
    /** This method adds a user specified vendor name, driver, and datasource to the list of known database vendors
    *
    *@param vName string of logical vendor name
    *@param vDriver string of full driver class name
    * @param vDataSource string of full datasource class name
    */
	public static void addNewDatabaseVendor(String vName, String vDriver, String vDataSource, String pooling)
	{
		knownDatabaseVendors.add(vName);
		knownDatabaseVendors.add(vDriver);
		knownDatabaseVendors.add(vDataSource);
		knownDatabaseVendors.add(pooling);
		addNewDriver(vDriver);//register driver
	}
	
	
	//Need to dynamically discover datasource properties given vendor name
	//Datasource implementations do NOT use a common properties interface like drivers do
	//Therefore, properties must be discovered dynamically using reflection
	//This function finds 'set' methods for a given class and infers them to be user definable properties
	//Not perfect- there is no way to provide formatting information, descriptions, or examples to user
	//returned properties contain property name and property argument- string, int, etc
    /** This method takes a vendor name and looks up its datasource class.
     * It uses reflection to find the properties that can be set.
     * It returns a properties file with the name of the set-able variable as the key and the parameter type of the variable as the value
     * Example:
     * key: DatabaseName value: java.lang.Sting
    *
    *@param vName string of vendor name
    *@return properties object of discovered properties
    */
	public static Properties discoverProps(String vName)
	{
		//Set basic properties
		Properties basicDBProps = new Properties();
		basicDBProps.setProperty("Vendor Name", "java.lang.String");
		basicDBProps.setProperty("Vendor Driver", "java.lang.String");
		basicDBProps.setProperty("Vendor DataSource", "java.lang.String");
		basicDBProps.setProperty("Connection Pooling", "java.lang.Boolean");
		
		//discover available datasource properties
		try{
			//use reflection to get class and available methods
			Class<?> c = Class.forName((String)getCurrentDatasource(vName), true, jdbcLoader);
			Method[] methods = c.getMethods();
			Class<?> [] params;
			//serch for 'set' methods- such as setUser or setPort
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().startsWith("set"))
				{
					params= methods[i].getParameterTypes();//look up parameters for set method
					basicDBProps.setProperty(methods[i].getName().substring(3), params[0].getName());//bind property name and parameter 
				}
			}
		}
		catch (ClassNotFoundException e) {
			logger.log(Level.WARNING,"Could not find class, check your installation. " + e);
			return basicDBProps;
		}
		return basicDBProps;
	}
	
	//for an existing datasource, this method returns properties that have been set
	//It uses reflection to find 'get' methods and returns the 'get' value of those methods
	//returned properties object has property names as keys and the return of the get function as the value
    /** This method 
    *For an existing datasource, this method returns properties that have been set
	*It uses reflection to find 'get' methods and returns the 'get' value of those methods
	*returned properties object has property names as keys and the return of the get function as the value
    *By using reflection, it can work for any vendor's datasources.
    *
    *@param newds the Datasource object to get the properties of
    */
	public static Properties getDatasourceProps(DataSource newds)
	{
		Properties dsProps = new Properties();
		try{
			//look up class methods for datasource
			Class<?> c = newds.getClass();
			Method[] methods = c.getMethods();
			Class<?> [] params;
			Class<?> returns;
			//sort through methods for set functions
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().startsWith("get"))
				{
					params= methods[i].getParameterTypes();
					returns = methods[i].getReturnType();
					//ignore get methods with input values, ignore get functions that do not return primitive or strings
					if (params.length == 0 && (returns.isPrimitive() || returns.getName().equalsIgnoreCase("java.lang.String")) )
					{
						//invoke method to get value
						Object value = methods[i].invoke(newds, new Object [0]);
						if (value != null)
							dsProps.setProperty(methods[i].getName().substring(3).toLowerCase(), value.toString()  );
						else
							dsProps.setProperty(methods[i].getName().substring(3).toLowerCase(), "");
					}
				}
			}
		}
		catch (Exception e) {
			logger.log(Level.WARNING,"Exception finding Datasource Properties " + e);
			return dsProps;
		}
		return dsProps;
	}
	
	public static Properties getDatasourceProps(ConnectionPoolDataSource newds)
	{
		Properties dsProps = new Properties();
		try{
			//look up class methods for datasource
			Class<?> c = newds.getClass();
			Method[] methods = c.getMethods();
			Class<?> [] params;
			Class<?> returns;
			//sort through methods for set functions
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().startsWith("get"))
				{
					params= methods[i].getParameterTypes();
					returns = methods[i].getReturnType();
					//ignore get methods with input values, ignore get functions that do not return primitive or strings
					if (params.length == 0 && (returns.isPrimitive() || returns.getName().equalsIgnoreCase("java.lang.String")) )
					{
						//invoke method to get value
						Object value = methods[i].invoke(newds, new Object [0]);
						if (value != null)
							dsProps.setProperty(methods[i].getName().substring(3).toLowerCase(), value.toString()  );
						else
							dsProps.setProperty(methods[i].getName().substring(3).toLowerCase(), "");
					}
				}
			}
		}
		catch (Exception e) {
			logger.log(Level.WARNING,"Exception finding Datasource Properties " + e);
			return dsProps;
		}
		return dsProps;
	}
	
	//Add a new driver to known drivers by using Class.forName- this will ensure it can be loaded later
    /** This method loads a class into the java virtual machine using the Class.forName command
     *    
    *@param sDriver string of full driver class name
    */
	public static void addNewDriver(String sDriver)
	{
		try{
			Class.forName (sDriver, true, jdbcLoader);
		}
		catch (ClassNotFoundException e)
		{
			logger.log(Level.WARNING,"Class could not be found"+e);
		}
	}

	//method to ceate a Datasource given a set of database properties. These properties should  be a completed version of discoveredProps from above
    /** This method creates and returns a datasource from a properties file. This file should include one entry "Vendor Datasource" specifying the datasource class to create. 
     * The other properties should be values for available properties (see discoverProps).
    *
    *@param DBprops Properties object with database properties and vendor name
    *@return Datasource 
    */
	public static DataSource createDS (Properties DBprops)
	{
		DataSource ds = null;
		try{
			//get class object for datasource class
			logger.log(Level.INFO,"Creating Datasource.....");
			Class<?> cl = getClassForName(DBprops.getProperty("Vendor DataSource"));

			//	create datasource object using class constructor
			ds = (DataSource) cl.newInstance();

			//look up datasource methods
			Method[] methods = cl.getMethods();
			String value;
			for (int i = 0; i < methods.length; i++) {
				//attempt to invoke setProperty methods
				if (methods[i].getName().startsWith("set"))
				{
					//get parameters for a given class
					Class<?> [] params = methods[i].getParameterTypes();
					
					//the key names should match the method names after the 'set' prefix- use .substring(3)
					value = DBprops.getProperty(methods[i].getName().substring(3));//The string after set should be used for properly configured DBprops object. See discoverProps above
					
					//if the value is still null, check for a lower case version of the method name
					if (value == null || value =="")
					{
						value = DBprops.getProperty(methods[i].getName().substring(3).toLowerCase());
					}
					
					//if we have a value for this property, set the value
					if (value != null && value !="")
					{
							//this only supports setting primitives and strings
							//the primitive classes are used to parse string entries
							if (params.length == 1)
							{
								//this only supports setting primitives and strings
								//the primitive classes are used to parse string entries
								if (params[0].getName().equalsIgnoreCase("boolean"))
								{
									
									methods[i].invoke(ds, Boolean.getBoolean(value));
								}
								else if (params[0].getName().equalsIgnoreCase("int"))
								{
									
									methods[i].invoke(ds, Integer.parseInt(value));
								}
								else if (params[0].getName().equalsIgnoreCase("char"))
								{
									
									methods[i].invoke(ds, value);
								}
								else if (params[0].getName().equalsIgnoreCase("byte"))
								{
									
									methods[i].invoke(ds, Byte.parseByte(value));
								}
								else if (params[0].getName().equalsIgnoreCase("short"))
								{
									
									methods[i].invoke(ds, Short.parseShort(value));
								}
								else if (params[0].getName().equalsIgnoreCase("long"))
								{
									
									methods[i].invoke(ds, Long.parseLong(value));
								}
								else if (params[0].getName().equalsIgnoreCase("float"))
								{
									
									methods[i].invoke(ds, Float.parseFloat(value));
								}
								else if (params[0].getName().equalsIgnoreCase("double"))
								{
									
									methods[i].invoke(ds, Double.parseDouble(value));
								}
								else if (params[0].getName().equalsIgnoreCase("java.lang.String"))
								{			
									
									methods[i].invoke(ds, value);
								}
							}
						}
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			logger.log(Level.SEVERE,"Datasource class could not be found for binding. Check Installtion and path. "+e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (SecurityException e)
		{
			logger.log(Level.SEVERE,"Member function access for datasource denied" +e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (IllegalAccessException e)
		{
			logger.log(Level.SEVERE,"Member function access for datasource denied" +e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (IllegalArgumentException e)
		{
			logger.log(Level.SEVERE,"Illegal Argument passed to Datasource or Illegal format for Port Number" +e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (InvocationTargetException e)
		{
			logger.log(Level.SEVERE,"Invocation target exception. " +e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch(Exception e){
			logger.log(Level.SEVERE,"Error occured during binding"+e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		logger.log(Level.INFO,"....Datasource Created "+ds.getClass().getName());
		return ds;
	}
	
	public static ConnectionPoolDataSource createPooledDS (Properties DBprops)
	{
		ConnectionPoolDataSource ds = null;
		try{
			//get class object for datasource class
			logger.log(Level.INFO,"Creating Pooled Datasource.....");
			Class<?> cl = getClassForName(DBprops.getProperty("Vendor DataSource"));

			//	create datasource object using class constructor
			ds = (ConnectionPoolDataSource) cl.newInstance();

			//look up datasource methods
			Method[] methods = cl.getMethods();
			String value;
			for (int i = 0; i < methods.length; i++) {
				//attempt to invoke setProperty methods
				if (methods[i].getName().startsWith("set"))
				{
					//get parameters for a given class
					Class<?> [] params = methods[i].getParameterTypes();
					
					logger.log(Level.INFO, "Looking for value for "+methods[i].getName());
					
					//the key names should match the method names after the 'set' prefix- use .substring(3)
					value = DBprops.getProperty(methods[i].getName().substring(3));//The string after set should be used for properly configured DBprops object. See discoverProps above
					
					//if the value is still null, check for a lower case version of the method name
					if (value == null || value =="")
					{
						value = DBprops.getProperty(methods[i].getName().substring(3).toLowerCase());
					}
					
					//if we have a value for this property, set the value
					if (value != null && value !="")
					{
						//we can only set methods that require one parameter
						if (params.length == 1)
						{
							//this only supports setting primitives and strings
							//the primitive classes are used to parse string entries
							if (params[0].getName().equalsIgnoreCase("boolean"))
							{
								
								methods[i].invoke(ds, Boolean.getBoolean(value));
							}
							else if (params[0].getName().equalsIgnoreCase("int"))
							{
								
								methods[i].invoke(ds, Integer.parseInt(value));
							}
							else if (params[0].getName().equalsIgnoreCase("char"))
							{
								
								methods[i].invoke(ds, value);
							}
							else if (params[0].getName().equalsIgnoreCase("byte"))
							{
								
								methods[i].invoke(ds, Byte.parseByte(value));
							}
							else if (params[0].getName().equalsIgnoreCase("short"))
							{
								
								methods[i].invoke(ds, Short.parseShort(value));
							}
							else if (params[0].getName().equalsIgnoreCase("long"))
							{
								
								methods[i].invoke(ds, Long.parseLong(value));
							}
							else if (params[0].getName().equalsIgnoreCase("float"))
							{
								
								methods[i].invoke(ds, Float.parseFloat(value));
							}
							else if (params[0].getName().equalsIgnoreCase("double"))
							{
								
								methods[i].invoke(ds, Double.parseDouble(value));
							}
							else if (params[0].getName().equalsIgnoreCase("java.lang.String"))
							{			
								
								methods[i].invoke(ds, value);
							}
						}
					}
				
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			logger.log(Level.SEVERE,"Datasource class could not be found for binding. Check Installtion and path. "+e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (SecurityException e)
		{
			logger.log(Level.SEVERE,"Member function access for datasource denied" +e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (IllegalAccessException e)
		{
			logger.log(Level.SEVERE,"Member function access for datasource denied" +e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (IllegalArgumentException e)
		{
			logger.log(Level.SEVERE,"Illegal Argument passed to Datasource or Illegal format for Port Number" +e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (InvocationTargetException e)
		{
			logger.log(Level.SEVERE,"Invocation target exception. " +e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch(Exception e){
			logger.log(Level.SEVERE,"Error occured during binding"+e +":"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		logger.log(Level.INFO,"....Datasource Created "+ds.getClass().getName());
		return ds;
	}
	
	//Finds DatabaseMetaData object for given datasource
    /** This method takes an existing datasource and returns the DatabaseMetaData object for that datasource
    *
    *@param DS Datasource object to find DatabaseMetaData of 
    *@return DatabaseMetaData object
    */
	public static DatabaseMetaData getMetaData(DataSource DS)
	{
		//connection to database
		Connection dbConnection;
		try{
			//InitialContext ic = new InitialContext();
			dbConnection = DS.getConnection();//get connection from datasource
		}
		catch (SQLException e)
		{
			logger.log(Level.WARNING,"Problem connecting Datasource "+DS.toString()+e.getMessage());
			return null;
		}
		try{
			return dbConnection.getMetaData();//return metadata
		}
		catch (SQLException e)
		{
			logger.log(Level.WARNING,"Problem connecting to database "+DS.toString()+" :"+e +":"+ e.getMessage());
			return null;
		}
	}

	public static DatabaseMetaData getMetaData(ConnectionPoolDataSource DS)
	{
		//connection to database
		PooledConnection dbConnection;
		Connection dbConn;
		try{
			//InitialContext ic = new InitialContext();
			dbConnection = DS.getPooledConnection();//get connection from datasource
			dbConn = dbConnection.getConnection();
		}
		catch (SQLException e)
		{
			logger.log(Level.WARNING,"Problem connecting Datasource "+DS.toString()+e.getMessage());
			return null;
		}
		try{
			return dbConn.getMetaData();//return metadata
		}
		catch (SQLException e)
		{
			logger.log(Level.WARNING,"Problem connecting to database "+DS.toString()+" :"+e +":"+ e.getMessage());
			return null;
		}
	}
	
	//Finds DatabaseMetaData object for given connection
    /** This method gets the DatabaseMetaData from a connection to that database
    *
    *@param conn Connection to a database
    *@return DatabaseMetaData object
    */
	public static DatabaseMetaData getMetaData(Connection conn)
	{
		//connection to database
		try{
			return conn.getMetaData();//return metadata
		}
		catch (SQLException e)
		{
			logger.log(Level.WARNING,"Problem connecting to database "+conn.toString()+" :"+e +":"+ e.getMessage());
			return null;
		}
	}

	//gets driver properties from connection
    /** This method fills a DriverPropertyInfo array from a connection and corresponding datasource object.
    *
    *@param conn Connection object to database
    *@param DS Datasource object that created that connection
    *@return DriverPropertyInfo[] with driver property elements
    */
	public static DriverPropertyInfo[] getConnectionProperties(Connection conn, DataSource DS)
	{
		logger.log(Level.INFO,"Datasource- Attempting to get properties");
		//Connection cannot return driver class name, only string name
		//need to use datasource to look it up
		 Driver d = null; 
		 String dsClassName = DS.getClass().getName();
		 String driverClassName = getCurrentDriverFromDatasource(dsClassName);
		 try {
			 //get driver class
			 d = (Driver)getClassForName(driverClassName).newInstance();
		 } catch ( Exception e ){
			 logger.log(Level.SEVERE,"There has been a problem connecting to driver: "+e);
		 }
		 try{
			 //return the properties
			 logger.log(Level.INFO, "Attempting to return properties");
			 return d.getPropertyInfo(conn.getMetaData().getURL(), getDatasourceProps(DS));
		 }
		 catch (SQLException e)
		 {
			 logger.log(Level.SEVERE,"There has been a problem getting driver properties: "+e); 
			return null;
		 }
	}

	public static DriverPropertyInfo[] getConnectionProperties(Connection conn, ConnectionPoolDataSource DS)
	{
		logger.log(Level.INFO,"Datasource- Attempting to get properties");
		//Connection cannot return driver class name, only string name
		//need to use datasource to look it up
		 Driver d = null; 
		 String dsClassName = DS.getClass().getName();
		 String driverClassName = getCurrentDriverFromDatasource(dsClassName);
		 try {
			 //get driver class
			 d = (Driver)getClassForName(driverClassName).newInstance();
		 } catch ( Exception e ){
			 logger.log(Level.SEVERE,"There has been a problem connecting to driver: "+e);
		 }
		 try{
			 //return the properties
			 logger.log(Level.INFO, "Attempting to return properties");
			 return d.getPropertyInfo(conn.getMetaData().getURL(), getDatasourceProps(DS));
		 }
		 catch (SQLException e)
		 {
			 logger.log(Level.SEVERE,"There has been a problem getting driver properties: "+e); 
			return null;
		 }
	}
	
    /** This method opens a new connection to the database using a set of properties provided. It closes the old connection.
    *
    *@param oldConn Connection object of old connection
    *@param connProps Properties object representing connection properties
    *@param	DS Datasource object representing datasource for these connections
    *@return New connection
    */
	public static Connection reConnect(Connection oldConn, Properties connProps, DataSource DS)
	{
		//connection cannot tell us the driver class name
		//need to use datasource to get driver class name
		 Driver d = null;
		 String connURL = ""; 
		 String dsClassName = DS.getClass().getName();
		 String driverName = getCurrentDriverFromDatasource(dsClassName);
		 try{
			 connURL = oldConn.getMetaData().getURL();
			 oldConn.close();//close off old connection before returning new one
		 }
		 catch (SQLException e)
		 {
			 logger.log(Level.SEVERE,"There has been a problem getting old connection information: "+e);
		 }
		 try {
			 d = (Driver)getClassForName(driverName).newInstance();
		 } catch ( Exception e ){
			 logger.log(Level.SEVERE,"There has been a problem connecting to driver: "+e);
		 }
		 try{
		 	return d.connect(connURL, connProps);//get new connection with new properties
		 }
		 catch (SQLException e)
		 {
			 logger.log(Level.SEVERE,"There has been a problem getting new connection: "+e);
			 return null;
		 }
	}
	
	public static Connection reConnect(Connection oldConn, Properties connProps, ConnectionPoolDataSource DS)
	{
		//connection cannot tell us the driver class name
		//need to use datasource to get driver class name
		 Driver d = null;
		 String connURL = ""; 
		 String dsClassName = DS.getClass().getName();
		 String driverName = getCurrentDriverFromDatasource(dsClassName);
		 try{
			 connURL = oldConn.getMetaData().getURL();
			 oldConn.close();//close off old connection before returning new one
		 }
		 catch (SQLException e)
		 {
			 logger.log(Level.SEVERE,"There has been a problem getting old connection information: "+e);
		 }
		 try {
			 d = (Driver)getClassForName(driverName).newInstance();
		 } catch ( Exception e ){
			 logger.log(Level.SEVERE,"There has been a problem connecting to driver: "+e);
		 }
		 try{
		 	return d.connect(connURL, connProps);//get new connection with new properties
		 }
		 catch (SQLException e)
		 {
			 logger.log(Level.SEVERE,"There has been a problem getting new connection: "+e);
			 return null;
		 }
	}

	//get a connection from a datasource
    /** This method returns a connection to an existing datasource, using previously configured properties
    *
    *@param DS Datasource to connect 
    *@return Connection to datasource
    */
	public static Connection getExistingConnection(DataSource DS) throws SQLException
	{
		logger.log(Level.INFO, "Datasource- attempting connection");
		Connection dbConnection = null;;
		// TODO: Why was it necessay to include the hardcoded credentals in the getConnection() method below.  Normally, this should be parameterless invocation.		
		// dbConnection = DS.getConnection("tgridreader","tgr1dr3ad3r");
		dbConnection = DS.getConnection();
		logger.log(Level.INFO, "Datasource- connected");
		return dbConnection;
		
	}

	public static Connection getExistingConnection(ConnectionPoolDataSource DS) throws SQLException
	{
		logger.log(Level.INFO, "Datasource- attempting connection");
		PooledConnection dbConnection = null;
		// TODO: Why was it necessay to include the hardcoded credentals in the getConnection() method below.  Normally, this should be parameterless invocation.
		// dbConnection = DS.getPooledConnection("tgridreader","tgr1dr3ad3r");
		dbConnection = DS.getPooledConnection();
		Connection dbConn = dbConnection.getConnection();
		logger.log(Level.INFO, "Datasource- connected");
		return dbConn;
		
	}
	
	
}
