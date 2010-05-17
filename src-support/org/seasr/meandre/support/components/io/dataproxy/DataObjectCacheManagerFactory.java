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

package org.seasr.meandre.support.components.io.dataproxy;

/**
 * The factory class to set up DataObjectCacheManagers
 *
 * @author  $Author: dfleming $
 * @version $Revision: 1.2 $, $Date: 2007/01/23 23:09:36 $
 * 
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public class DataObjectCacheManagerFactory {

   //~ Static fields/initializers **********************************************

   /** The cache manager. */
   static DataObjectCacheManager manager = null;

   //~ Methods *****************************************************************

   /**
    * Get the cache manager, use default resources.
    * <p>
    * Probably should denigrate this method
    *
    * @return Description of return value.
    */
   static public DataObjectCacheManager getCacheManager() throws DataObjectProxyException {

      // need to make a file for each user?
      if (manager == null) {
         manager = new DataObjectCacheManager();
         manager.setConfigFile("resources/cacheState.xml");
         manager.setSchemaFile("resources/cacheManager.xsd");

         /*
          * Initialize the cache manager, may throw exception
          */
         manager.initCacheState();
      }

      return manager;
   }

   /**
    * get the cache manager, specify the schema and data files.
    *
    * @param  xschema Description of parameter xschema.
    * @param  xdata   Description of parameter xdata.
    *
    * @return Description of return value.
    */
   static public DataObjectCacheManager getCacheManager(String xschema,
                                                        String xdata)
   						throws DataObjectProxyException {

      // need to make a file for each user?
      if (manager == null) {
         manager = new DataObjectCacheManager();
         manager.setConfigFile(xdata);
         manager.setSchemaFile(xschema);
         
         /*
          * Initialize the cache manager, may throw exception
          */      
         manager.initCacheState();
      }

      return manager;
   }
} // end class DataObjectCacheManagerFactory
