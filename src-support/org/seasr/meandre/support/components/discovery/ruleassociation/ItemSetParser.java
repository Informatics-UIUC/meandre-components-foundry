package org.seasr.meandre.support.components.discovery.ruleassociation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


public class ItemSetParser {
	
    
    public ItemSetInterface getItemSets(String filename)
       throws FileNotFoundException, IOException
    {
       File file = new File(filename);
       FileInputStream fis = new FileInputStream(file);
       return getItemSets(new InputStreamReader(fis));
    }
    
    public ItemSetInterface getItemSets(Reader r)
    throws IOException
    {
    	return getItemSets(r,1);
    }
    
    public ItemSetInterface getItemSets(Reader r, int linesPerSet)
       throws IOException
    {
       BufferedReader reader = new BufferedReader(r);
       int lineNumber = 0;
          
     
       String line = null;
       SimpleItemSet itemSet = new SimpleItemSet();
       Set<String> set = new HashSet<String>();
       
       while ( (line = reader.readLine()) != null) {
    	   
    	  lineNumber++;
          
          line = line.replaceAll("[{}]", "");
          line = line.trim();
          StringTokenizer tokens = new StringTokenizer(line, ",");
          
          while(tokens.hasMoreTokens()){
             String item = tokens.nextToken();
             set.add(item);
          }
          
          if (lineNumber%linesPerSet == 0) {
        	  
             itemSet.addSet(set); 
             set = new HashSet<String>();
          }
       }
       
       itemSet.compute();
       
       return itemSet;
       
    }
}