package org.seasr.meandre.support.utils;

import java.io.File;
import java.util.logging.Logger;

public class PublishedResourcesUtility {
	
	protected PublishedResourcesUtility()
	{
		
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
