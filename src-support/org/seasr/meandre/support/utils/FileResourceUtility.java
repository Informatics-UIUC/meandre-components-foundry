/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/


package org.seasr.meandre.support.utils;

import java.io.File;
import java.util.logging.Logger;

import org.meandre.core.ComponentContextProperties;




public class FileResourceUtility {
	
	protected FileResourceUtility()
	{
		
	}
	
	public static String buildResourcePath(ComponentContextProperties ccp, String filename)
	{
		if (filename.startsWith(File.separator) || filename.startsWith(".")) {
			// it's an absolute path
			return filename;
		}
		
		return ccp.getPublicResourcesDirectory() + File.separator + filename;
	}
	//
	// convenience method for components to create directories inside published resources
	//
	public static String createPathToPublishedResources(ComponentContextProperties ccp, 
			                                            String filename,
			                                            Logger console)
	{
		String destination = ccp.getPublicResourcesDirectory();
		return createPathToResource(destination, filename, console);
	}
	
	
	
	public static String createPathToResource(String dir, String filename, Logger console)
	{
		if (filename.startsWith(File.separator) || filename.startsWith(".")) {
			return filename;
		}

		//
		// user did not specify an absolute path e.g. /tmp/file.stuff
		// if user does "data/data.csv" 
		// need to create that path
		//


		String path = dir + File.separator + filename;
		FileResourceUtility.createPathToResource(path, console);

		return path;

	}
	
	
	public static String createPathToResource(String path) 
	{
		return createPathToResource(path,null);
	}
	
	public static String createPathToResource(String path, Logger console) 
	{
		String resource = path;
		int idx = path.lastIndexOf(File.separator);
		
		if (idx > 0) {
			resource = path.substring(idx+1);
			path     = path.substring(0, idx);
			
			if (console != null) console.info("Discover: " + path + "-->" + resource);
			boolean success = (new File(path)).mkdirs();
		    if (success && console != null) {
		      console.info("Created directories: " + path + " created");
		    }
		}
		
	    return resource;
	}

}
