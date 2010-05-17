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

import java.io.File;


/**
 * This is the default implementation of a cache policy.  It implements 
 * the 'shouldFlush' method with a simple default rule.
 *
 * @author  $Author: dfleming $
 * @version $Revision: 1.2 $, $Date: 2007/01/23 23:09:36 $
 * 
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public class DataObjectCacheDefaultPolicy implements DataObjectCachePolicy {

   //~ Instance fields *********************************************************

   /** The default timeOut -- one day. */
   private long timeOut = 24 * 60 * 60 * 1000; // ms/day

   //~ Methods *****************************************************************

   /**
    * Set the timeout to another value
    *
    * @param msecs Description of parameter msecs.
    */
   public void setTimeOut(long msecs) { timeOut = msecs; }

   /**
    * The required policy.  In this case, true if the file is older than one day.
    *
    * @param  f The file.
    *
    * @return true if older than the timeout.
    */
   public boolean shouldFlush(File f) {
	  if (f == null || !f.exists()) return false; // no such file?
	  
      long now = System.currentTimeMillis();
      long mod = f.lastModified();

      // testing with simple default policy
      long d = now - mod;

      return (d > timeOut);
   }

} // end class DataObjectCacheDefaultPolicy
