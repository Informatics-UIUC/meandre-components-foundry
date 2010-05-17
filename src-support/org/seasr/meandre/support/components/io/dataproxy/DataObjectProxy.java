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
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * DataObjectProxy is a common interface for every data object proxy.
 *
 * <p>The DataObjectProxy manages access to data objects, local or remote, using
 * alternative access protocols.</p>
 *
 * <p>To access an object, the general procedure is to construct a URL, and call
 * the DataObjectProxyFactory to get an instance of a proxy:</p>
 *
 * <pre>
 *      DataObjectProxy dataobj =
 *      DataObjectProxyFactory.getDataObjectProxy(
 *          url, username, password);
 *     pushOutput(dataobj, 0);
 * </pre>
 *
 * <p>To read from the object, the proxy is called to get an input stream:</p>
 *
 * <pre>
 *      BufferedReader reader = new BufferedReader(
 *      new InputStreamReader(DataObjProx.getInputStream()));
 *    </pre>
 *
 * <p>To copy the file to a specific local file and read it there:</p>
 *
 * <pre>
 *        DataObjectProxy dataobj =
 *        DataObjectProxyFactory.getDataObjectProxy(
 *          url, username, password);
 *
          dataobj.readFile(new File (destinationPath));

          </pre>
 *
 * <p>To write the object, a file or input stream is passed to the
 * DataObjectProxy. After the put, the DataObjectProxy should be closed to
 * complete the store.</p>
 *
 * <p>To write a temporary file and then push:</p>
 *
 * <pre>
 *      FileWriter fw = new
 *           FileWriter( dataobj.initLocalFile(null));
 *
 *      fw.write(...); fw.flush(); fw.close();
 *
 *      dataobj.putFromFile(dataobj.getLocalFile());
 *      dataobj.close();
 *    </pre>
 *
 * <p>To write from a stream,</p>
 *
 * <pre>
 *        InputStream is == ...
 *        dataobj.putFromFile(is);
 *        dataobj.close();
 *     </pre>
 *
 * <p>To read all the objects in a given directory/colletion:</p>
 *
 * <pre>
 *      DataObjectProxy srcdop =
 *      int depth = DEPTH_INFINITY;
 *      URL desurl = new URL(wheretoWrite);
 *      srcdop.downloadDir(desurl,depth);
 *      srcdop.close();
 *              </pre>
 *
 * <p>To write the contents of a directory is similar:</p>
 *
 * <pre>
 *      DataObjectProxy destdop =
 *      int depth = DEPTH_INFINITY;
 *      URL srcurl = new URL(wheretoread);
 *      destdop.uploadloadDir(srcurl,depth);
 *      destdop.close();
 *              </pre>
 *
 * <p>All Known Implementing Classes:</p>
 *
 * <ul>
 *   <li>LocalDataObjectProxyImpl,WebDataObjectProxyImpl</li>
 * </ul>
 *
 * @author  $Author: dfleming $
 * @version $Revision: 1.2 $, $Date: 2007/01/23 23:09:36 $
 * @see     DataObjectProxyFactory
 * 
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public abstract class DataObjectProxy {

   //~ Static fields/initializers **********************************************

   /** Traverse all the descendents of a directory. */
   static public final int DEPTH_INFINITY = 2147483647;

   /** Traverse only the immediate children. */
   static public final int DEPTH_1 = 1;

   /** Traverse on the object itself. */
   static public final int DEPTH_0 = 0;

   //~ Instance fields *********************************************************

   /** The logger. */
   protected Logger mLogger = null;

   /** Password to access the current DataObjectProxy if needed. */
   protected String mPassword = null;

   /** url of the current DataObjectProxy. */
   protected URL mURL = null;

   /** Username to access the current DataObjectProxy if needed. */
   protected String mUsername = null;

   /** A temporary directory to store locally cached files. */
   protected File tempDataDir = null;

   /**
    * The paths of all of the temporary files created in the process. This is
    * used to clean up the temporary files. Not used when the DataObjectProxy
    * Cache is used.
    */
   protected Vector tempFilesCreated;

   //~ Methods *****************************************************************

   /**
    * Get the password being used to access the current DataObjectProxy.
    *
    * @return the password
    */
   protected abstract String getPassword();

   /**
    * Close the connection and clean up the temp files created.
    *
    * <p>The specific action depends on the soruce of the object.</p>
    */
   public abstract void close();

   /**
    * Create a directory at path.
    *
    * @param  relativePath Description of parameter path.
    *
    * @return a DOP for the new object.
    *
    * @throws DataObjectProxyException
    */
   public abstract DataObjectProxy createCollection(String relativePath)
      throws DataObjectProxyException;


   /**
    * Download the object and its descendants specified by this
    * DataObjectProxy to the location specified by the URL.
    *
    * <p>The destination is a URL for a local or remote directory.</p>
    *
    * <p>The destination is the current DataObjectProxy, either a local or
    * remote directory.</p>
    *
    * <p>This is the same as dest.uploadDir(src).</p>
    *
    * <p><b>Note:</b> This method does not take authintication for the
    * destination. Use downLoadDir(dop) below.</p>
    *
    * @param  wheretosave : A local directory to store the downloaded files
    *                     depth:
    * @param  depth       An integer to indicate how you would like to download
    *                     the directory. There are only two valid values for
    *                     depth: DataObjectProxy.DEPTH_1: Only download the
    *                     files under the collection, no sub directories
    *                     DataObjectProxy.DEPTH_INFINITY: download all of the
    *                     files and subdirectories.
    *
    * @throws DataObjectProxyException An error ocurred.
    */
   public abstract void downloadDir(URL wheretosave, int depth)
      throws DataObjectProxyException;

   /**
    * Download the object and its descendants specified by this
    * DataObjectProxy to the location specified by the destination
    * DataObjectProxy.
    *
    * <p>The destination is DataObjectProxy for a local or remote directory.</p>
    *
    * <p>The destination is the current DataObjectProxy, either a local or
    * remote directory.</p>
    *
    * <p>This is the same as dest.uploadDir(src).</p>
    *
    * @param  wheretosave : A local directory to store the downloaded files
    * @param  depth       An integer to indicate how you would like to download
    *                     the directory. There are only two valid values for
    *                     depth: DataObjectProxy.DEPTH_1: Only download the
    *                     files under the collection, no sub directories
    *                     DataObjectProxy.DEPTH_INFINITY: download all of the
    *                     files and subdirectories
    *
    * @throws DataObjectProxyException An error occurred.
    */
   public abstract void downloadDir(DataObjectProxy wheretosave, int depth)
      throws DataObjectProxyException;

   /**
    * Return whether the object pointed to exists.
    *
    * @return true if the object exists, false otherwise.
    */
   public abstract boolean exists();

   /**
    * Get a list of the descendents of the URL.
    *
    * <p>This is empty for a file or non-collection.</p>
    * <p>This is the same as getChildrenURLs(int i, false) </p>
    *
    * @param  i The depth to traverse.
    *
    * @return A list of the relative paths.
    *
    * @throws DataObjectProxyException Exception occured.
    */
   public abstract Vector getChildrenURLs(int i)
      throws DataObjectProxyException;

   /**
    * Get a list of the descendents of the URL, optinally return only
    * file names (i.e., omit diretory names).
    *
    * <p>This is empty for a file or non-collection.</p>
    *
    * @param  i The depth to traverse.
    * @param  noDirs  If true, returns only the file names, else return all.
    *
    * @return A list of the relative paths.
    *
    * @throws DataObjectProxyException Exception occured.
    */
   public abstract Vector getChildrenURLs(int i, boolean noDirs)
   throws DataObjectProxyException;
   /**
    * Get the InputStream from the URL being pointed to by the DataObjectProxy.
    *
    * <p>The bahavior depends on the source.</p>
    *
    * <p>When the object is remote, this will be the input stream from the
    * remote service.</p>
    *
    * <p>When the object is local, this will be a FileInputStream.</p>
    *
    * @return InputStream opened from the URL.
    *
    * @throws DataObjectProxyException Exception occured.
    *
    * @see    getLocalInputStream, getLocalFile
    */
   public abstract InputStream getInputStream() throws DataObjectProxyException;

   /**
    * Get the local copy of the file, if set.
    *
    * @return File the file or null if not set;
    */
   public abstract File getLocalFile();

   /**
    * Get InputStream from a locally cached file.
    *
    * <p>The behavior depends on the source.</p>
    *
    * <p>When the object is remote, this force a copy to be cached from the
    * remote service.</p>
    *
    * <p>When the object is local, this will be a local File.*</p>
    *
    * @return InputStream from a locally cached file.
    *
    * @throws DataObjectProxyException Exception occurred.
    *
    * @see    getLocalFile, getInputStream
    */
   public abstract InputStream getLocalInputStream()
      throws DataObjectProxyException;

   /**
    * Get all properties on the current DataObjectProxy.
    *
    * <p>The behavior depends on the source.</p>
    *
    * @return A Hashtable of property objects.
    *
    * @throws Exception                Exception in read.
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public abstract Object getMeta() throws DataObjectProxyException;

   /**
    * Get the value of the specified property on the current DataObjectProxy.
    *
    * <p>The behavior depends on the source.</p>
    *
    * @param  prop - property including namespace and key.
    *
    * @return a single propery.
    *
    * @throws DataObjectProxyException Excetion in read.
    */
   public abstract Object getMeta(Object prop) throws DataObjectProxyException;


   /**
    * Give the resource name not including the path.
    *
    * <p>For a file system, this is the last component of the path.</p>
    *
    * @return give the resource name not including the path.
    *
    * @throws DataObjectProxyException
    */
   public abstract String getResourceName() throws DataObjectProxyException;

   /**
    * Get a unique tag for the object. This is used to detect changes to
    * objects--when the tag is the same, the object is unchanged.
    *
    * <p>The behavior depends on the source.</p>
    *
    * <p>For local objects, the tag is the modification date.</p>
    *
    * <p>For remote objects, the tag is the "ETAG", defined by the HTTP
    * standard.</p>
    *
    * @return String The unique tag, rendered as a string.
    *
    * @throws DataObjectProxyException
    */
   public abstract String getTag() throws DataObjectProxyException;

   /**
    * Get the modification data of the object.
    *
    * @return String the modification data in a string.
    *
    * @throws DataObjectProxyException
    */
   public abstract String getURLLastModified() throws DataObjectProxyException;

   /**
    * Get the username being used to access the current DataObjectProxy.
    *
    * @return the username in use.
    */
   public abstract String getUsername();

   /**
    * Set up the local file to be the source or destination.
    *
    * <p>The primary use is to create a local file to be uploaded to a remote
    * destination.</p>
    *
    * <p>The behavior depends on the source.</p>
    *
    * <p>When the object is remote, this will be a local copy from the remote
    * service.</p>
    *
    * <p>When the object is local, this will be a local File.*</p>
    *
    * @param  dest The local file. If <b>null</b>, a temporary file is allocated
    *              in the local cache.
    *
    * @return a loca file.
    *
    * @throws DataObjectProxyException
    *
    * @see    getLocalInputStream, getInputStream
    */
   public abstract File initLocalFile(File dest)
      throws DataObjectProxyException;

   /**
    * Does the URL point to a collection or a directory?
    *
    * @return true if the current URL is a collection, false otherwise.
    *
    * @throws DataObjectProxyException
    */
   public abstract boolean isCollection() throws DataObjectProxyException;

   /**
    * Put the file to the current URL being pointed to by the current
    * DataObjectProxy.
    *
    * <p>The behavior depends on the destination.</p>
    *
    * <p>When the URL is remote, this will push the file to the remote server.
    * </p>
    *
    * <p>When the URL is local, this will copy the file to the destination if
    * necessary.</p>
    *
    * @param  file - the file to be put to the current URL.
    *
    * @throws DataObjectProxyException
    */

   public abstract void putFromFile(File file) throws DataObjectProxyException;

   /*
    * Put the file and the given properties to the current URL @param  file  -
    * the file to be put to the current URL. @param  nsp   - the property to be
    * put to the current URL including namespace and key. @param  value -  the
    * property value to be put to the current URL. @throws
    * DataObjectProxyException.
    */
// public abstract void putFromFileWithProp(File file, NSProperty nsp,String
// value) throws Exception;

   /**
    * Put InputStream to the current URL.
    *
    * <p>When the object is remote, this will copy from the input stream to the
    * destination URL.</p>
    *
    * <p>The behavior depends on the destination.</p>
    *
    * <p>When the object is remote, the inptu strema will be copied to a local
    * file.</p>
    *
    * @param  is - InputStream to be put to the current URL.
    *
    * @throws Exception                Description of exception Exception.
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public abstract void putFromStream(InputStream is)
      throws DataObjectProxyException;

   /**
    * Read the file into a local file.
    *
    * <p>If a destination is specified, copy the file there.</p>
    *
    * <p>If the input is <b>null<b>, create a temporary file and copy
    * there.</b></b></p>
    *
    * <p>The behavior depends on the source.</p>
    *
    * <p>For a remote object, the file is downloaded.</p>
    *
    * <p>For a local object, the file is copied.</p>
    *
    * @param  dest The destination, a local file, or<b>null</b>.
    *
    * @return read the file into a local file.
    *
    *         <p>If a destination is specified, copy the file there.</p>
    *
    *         <p>If the input is <b>null<b>, create a temporary file and copy
    *         there.</b></b></p>
    *
    *         <p>The behavior depends on the source.</p>
    *
    *         <p>For a remote object, the file is downloaded.</p>
    *
    *         <p>For a local object, the file is copied.</p>
    *
    * @throws DataObjectProxyException
    */
   public abstract File readFile(File dest) throws DataObjectProxyException;

   /**
    * Delete a directory and all its children.
    *
    * <p><b>Warning:</b>This method is very dangerous.</p>
    *
    * @throws DataObjectProxyException
    */
   public abstract void removeDirectory() throws DataObjectProxyException;

   /**
    * Get a new DataObjectProxy represents the new URL.
    *
    * <p>This method is used to change the target of a proxy.</p>
    *
    * @param  newURL URL to be used to create a DataObjectProxy.
    *
    * @return a DataObjectProxy.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public abstract DataObjectProxy resetDataObjectProxy(URL newURL)
      throws DataObjectProxyException;

   /**
    * Get a new DataObjectProxy based on the new URL, username and password.
    *
    * @param  newURL  - url to be used to create a DataObjectProxy.
    * @param  newUser - username to access the URL.
    * @param  newPass - password to access the URL.
    *
    * @return DataObjectProxy.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public abstract DataObjectProxy resetDataObjectProxy(URL newURL,
                                                        String newUser,
                                                        String newPass)
      throws DataObjectProxyException;

   /**
    * Set up the password.
    *
    * @param pass - password to be used.
    */
   public abstract void setPassword(String pass);

   /**
    * Description of method setUserInfo.
    *
    * @param user User name.
    * @param pass Password.
    */
   public abstract void setUserInfo(String user, String pass);

   /**
    * Set up the username.
    *
    * @param user - username to be used.
    */
   public abstract void setUsername(String user);

   /**
    * Put the source and its descendents to the destination, recreating the
    * hierarchy.
    *
    * <p>The source is a directory and the destination is the directory to place
    * the files in.</p>
    *
    * <p><b>Note:</b> This is the same as src.uploadDir(dest)</p>
    *
    * <p>The implemenation depends on the destination.</p>
    *
    * @param  srcdop a DataObjectProxy pointing to the collection in the server
    *                where the directory to be uploaded will be stored
    * @param  depth  a DataObjectProxy pointing to the directory to be uploaded
    *
    * @throws DataObjectProxyException
    */
   public abstract void uploadDir(DataObjectProxy srcdop, int depth)
      throws DataObjectProxyException;

   /**
    * Get the full URL currently being pointed to by the DataObjectProxy.
    *
    * @return The current URL the DataObjectProxy points to.
    */
   public URL getURL() { return mURL; }

   /**
    * Put a set of properties on the current DataObjectProxy.
    *
    * <p><b>Note:</b> Metadata is not implemented yet.</p>
    *
    * @param  proptable - a Hashtable of key/value pairs.
    *
    * @throws DataObjectProxyException
    */
   public void putMeta(Object proptable) throws DataObjectProxyException { }

   /*
    * Put a single property on the current DataObjectProxy. @param  prop  - the
    * property with namespace and key. @param  value - the value of the
    * property. @throws DataObjectProxyException.
    */
   // public void putMeta(NSProperty prop, String value) throws
   // DataObjectProxyException { }

   /**
    * Search metadata. Not implmented yet.
    *
    * @return null.
    *
    * @throws Exception Description of exception Exception.
    */
   public Object searchMeta() throws Exception { return null; }

} // end class DataObjectProxy
