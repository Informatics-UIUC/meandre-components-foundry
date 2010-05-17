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

import java.net.URL;


/**
 * <p>Factory to create different DataObjectProxy implementations according to
 * the different protocols.</p>
 *
 * <p>This version only handles local files and files accessed via WebDAV (via
 * HTTP).</p>
 *
 * @author  $Author: dfleming $
 * @version $Revision: 1.2 $, $Date: 2007/01/23 23:09:36 $
 * @see     DataObjectProxy
 * @see     Input1FileURL
 * 
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public class DataObjectProxyFactory {

   //~ Methods *****************************************************************

   /**
    * The main method: create a DataObjectProxy for the given object.
    *
    * @param  url - The URL the DataObjectProxy to point to.
    *
    * @return A DataObjectProxy.
    *
    * @throws DataObjectProxyException Error create the proxy. Could be a bad
    *                                  URL or the access methond might not be
    *                                  supported.
    */
   static public DataObjectProxy getDataObjectProxy(URL url)
      throws DataObjectProxyException {
      return getDataObjectProxy(url, "", "");
   }

   /**
    * Description of method getDataObjectProxy.
    *
    * <p>In the current implementation, the URL is examined to determine what
    * implementation class to select. In future versions, this selection may
    * well require a different method.</p>
    *
    * @param  url      - the URL the DataObjectProxy to point to.
    * @param  username - the username to be used to access DataObjectProxy.
    * @param  password - the password to be used to access DataObjectProxy.
    *
    * @return The new Data Object Proxy.
    *
    * @throws DataObjectProxyException Error create the proxy. Could be a bad
    *                                  URL or the access methond might not be
    *                                  supported.
    */
   static public DataObjectProxy getDataObjectProxy(URL url, String username,
                                                    String password)
      throws DataObjectProxyException {
      String protocol = url.getProtocol();

      if (protocol.equals("file")) {
         username = System.getProperty("user.name");

         return new LocalDataObjectProxyImpl(url, username, password);
      }

      if (protocol.equals("http") || protocol.equals("https")) {
         return new WebdavDataObjectProxyImpl(url, username, password);
      } else {
         return null;
      }
   }

} // end class DataObjectProxyFactory
