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

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;

/*
 * <p>Title: External Jar Loader</p>
 * <p>
 * This class is used to access classes stored in external jars not loaded when the java virtual machine is initialized.
 * URLs or file paths can be added to the loader's path, and it will attempt to load new classes from this path during runtime.
 * For example, this can be used for a database web application where the JDBC driver classes are not loaded by the server at runtime.
 * This allows a user to specify an external jar file with the drivers and load them into the virtual machine for use by the web application.
 *</p>
 *
 *<p>Company: NCSA, Automated Learning Group</p>
 * @author E Johnson
 * @version 1.0
 */

public class ExternalJarLoader extends URLClassLoader
{
	Logger logger = Logger.getAnonymousLogger();
    //================
    // Constructors
    //================
	//public ExternalJarLoader (URL[] urls, ClassLoader cl)
    /** Class constructor
    *
    * @param urls URLs to add to path to load files
    */
	public ExternalJarLoader (URL[] urls)
    {
        super (urls);
    }

	
    //================
    // Public Methods
    //================
    /** This method adds a file path to the class loader. This should be a well formated URL or an exception will be thrown
    *
    * @param path String path to add to class loader
    * @throws MalformedURLException
    */
    public void addFile (String path) throws MalformedURLException
    {
    	
        //String urlPath = "jar:file:" + path + "!/";
    	this.addURL (new URL (path));
    }

    /** This method adds a file path to the class loader
    *
    * @param name String path to add to class loader
    */
    public Class<?> loadClass(String name)
    	throws ClassNotFoundException 
    {	
    	//attempt to find class locally first
    	Class loadedClass = findLoadedClass(name);
    	if (loadedClass == null) 
    	{
    		try {
    			loadedClass = findClass(name);
    		} 
    		catch (ClassNotFoundException e) {
    			//does not exist locally, try to load externally
    		}
    		if (loadedClass == null) 
    		{
    			//use URLClassLoader's loadClass function to search file path for jar and resource files
    			loadedClass = super.loadClass(name);
    			super.resolveClass(loadedClass);
    		}
    	}
    	return loadedClass;
    }
}
    

