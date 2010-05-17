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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;


/**
 * LocalDataObjectProxyImpl handles access to a local file. Users can read file
 * or InputStream from, write file or InputStream to the URL with <code>
 * file</code> as protocol.
 *
 * @author  $Author: dfleming $
 * @version $Revision: 1.2 $, $Date: 2007/01/23 23:09:36 $
 * 
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public class LocalDataObjectProxyImpl extends DataObjectProxy {

   //~ Constructors ************************************************************

   /**
    * Creates a new LocalDataObjectProxyImpl object.
    *
    * @param url      - The URL the LocalDataObjectProxyImpl to point to.
    * @param username - The username to be used to access the
    *                 LocalDataObjectProxyImpl.
    * @param password - The password to be used to access the
    *                 LocalDataObjectProxyImpl.
    */
   public LocalDataObjectProxyImpl(URL url, String username, String password) {
      mURL = url;
      setUsername(username);
   }

   //~ Methods *****************************************************************

   /**
    * Create whatever directories are needed to create the location pu/res.
    *
    * <p>If 'includelast' is set, then create the whole path, else create just
    * the parent of the last component.</p>
    *
    * <p>This method is used to prepare a directory to copy into.</p>
    *
    * @param  pu          A URL which is the base of the new path.
    * @param  rest        A relative path from the URL.
    * @param  includelast If true, then will create the whole path. If false,
    *                     will create all but ht elast component..
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   private void createPath(URL pu, String rest, boolean includelast)
      throws DataObjectProxyException {

      String pt = pu.getPath();
      pt = new String(pt + "/" + rest);

      if (!includelast) {
         pt = pt.substring(0, pt.lastIndexOf('/'));
      }

      File ff = new File(pt);

      if (!ff.exists()) {

         boolean success = ff.mkdirs();

         if (!success) {
            throw new DataObjectProxyException("Failed to create directory " +
                                               pt);
         }
      }
   }

   /**
    * Format a date in to a standard string.
    *
    * @param  when The Data object.
    *
    * @return The data in a string.
    */
   private String formatModificationDate(Date when) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      sdf.setTimeZone(TimeZone.getDefault());

      return sdf.format(when);
   }

   /**
    * Get a list of all the descendants of a directory.
    *
    * @param  depth The depth to descend, currently 1 or infinite.
    * @param  url   The root of the descent.
    *
    * @return A list of all the children.
    *
    * @throws DataObjectProxyException
    */
   private Vector getChildrenURLs(int depth, URL url)
      throws DataObjectProxyException {
      Vector ret = new Vector();
      Vector v = new Vector();
      Vector noDirs = new Vector();

      if (isCollection()) {

         ret.add(url);

         File dir = new File(url.getPath());
         File[] relFileNames = dir.listFiles();

         if (relFileNames == null) {
        	 v.add(ret);
        	 v.add(noDirs);
            return v; //ret;
         }

         // For depth_1
         if (depth == DataObjectProxy.DEPTH_1) {

            for (int j = 0; j < relFileNames.length; j++) {

               try {
                  URL turl = (URL) relFileNames[j].toURL();
                  ret.add(turl);
                  if (!relFileNames[j].isDirectory()) {
                	  noDirs.add(turl);
                  }
               } catch (Exception e) {
                  this.handleExceptions(e);
               }
            }
         }

         // For depth_infinity
         if (depth == DataObjectProxy.DEPTH_INFINITY) {

            for (int j = 0; j < relFileNames.length; j++) {

               if (relFileNames[j].isDirectory()) {

                  try {
                     Vector vsubdir =
                        this.getChildrenURLs(DataObjectProxy.DEPTH_INFINITY,
                                             relFileNames[j].toURL());
                     Vector subdir = (Vector)vsubdir.get(0);
                     for (int k = 0; k < subdir.size(); k++) {
                        URL turl = (URL) subdir.elementAt(k);
                        ret.add(turl);
                     }
                     Vector sd1 = (Vector)vsubdir.get(1);
                     for (int k = 0; k < sd1.size(); k++) {
                         URL turl = (URL) sd1.elementAt(k);
                         noDirs.add(turl);
                      }
                  } catch (Exception e) {
                     this.handleExceptions(e);
                  }
               } else {

                  try {
                     URL turl = (URL) relFileNames[j].toURL();
                     ret.add(turl);
                     noDirs.add(turl);
                  } catch (Exception e) {
                     this.handleExceptions(e);
                  }
               }
            }
         } // end if
      } else {

         // Depth 0?
         ret.add(mURL);
      }
      v.add(ret);
 	 v.add(noDirs);
     return v; //ret;
   //   return ret;

   } // end method getChildrenURLs

   /**
    * <p>Catch exceptions and throw new corresponding DataObjectProxyExceptions
    * with the root cause.</p>
    *
    * @param  e - A type of Exception to be passed in.
    *
    * @throws DataObjectProxyException
    */
   private void handleExceptions(Exception e) throws DataObjectProxyException {

      // Handle IOException
      if (e instanceof IOException) {
         throw new DataObjectProxyException("*****IOException " +
                                            e.getLocalizedMessage());
      }

      // Handle URISyntaxException
      else if (e instanceof URISyntaxException) {
         throw new DataObjectProxyException("*****URISyntaxException " +
                                            e.getLocalizedMessage());
      }

      // Handle MalformedURLException
      else if (e instanceof MalformedURLException) {
         throw new DataObjectProxyException("*****MalformedURLException " +
                                            e.getLocalizedMessage());
      } else {
         throw new DataObjectProxyException("*****Unknown Exception " +
                                            e.getLocalizedMessage());
      }


   }


   /**
    * Copy the contents of the file 'localpath' to the current file.
    *
    * <p><b>Warning:</b> this method does not do sophisticated error checking,
    * e.g., it does not detect if the soruce and destination are the same, it
    * doesn't check permissions, etc.</p>
    *
    * @param  localpath Description of parameter localpath.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   private void readTheFile(String localpath) throws DataObjectProxyException {

      try {
         String rpath = mURL.getPath();
         BufferedInputStream isr =
            new BufferedInputStream(new FileInputStream(rpath));
         BufferedOutputStream osr =
            new BufferedOutputStream(new FileOutputStream(localpath));
         byte[] b = new byte[4096];

         while (isr.available() > 0) {
            int howmany = isr.read(b);
            osr.write(b, 0, howmany);
         }

         osr.close();
         isr.close();
      } catch (Exception e) {
         this.handleExceptions(e);
      }

   }

   /**
    * Null because the password is probably not necessary to access local files.
    *
    * @return null because the password is probably not necessary to access
    *         local files.
    */
   protected String getPassword() { return null; }

   /**
    * No action required to clean up a local file.
    */
   public void close() { }

   /**
    * Create a directory at path.
    *
    * @param  path a relative path (relative to current URL).
    *
    * @return DataObjectProxy for the new object.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public DataObjectProxy createCollection(String path)
      throws DataObjectProxyException {

      if (!mURL.getProtocol().equals("file")) {
         throw new DataObjectProxyException("createCollection: not local url");
      }

      File file =
         new File(File.separator +
                  mURL.getHost() + File.separator +
                  mURL.getPath() + File.separator + path);

      try {

         if (file.exists()) {
            return this.resetDataObjectProxy(file.toURL());
         }

         file.mkdirs();

         if (file.exists()) {
            return this.resetDataObjectProxy(file.toURL());
         }
      } catch (MalformedURLException mfu) {
         throw new DataObjectProxyException(mfu);
      }

      return this;
   } // end method createCollection

   /**
    * Download the directory and it's descendants specified by this
    * DataObjectProxy to the location specified by the destination proxy.
    *
    * <p>This is the same as dest.uploadDir(src).</p>
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
   public void downloadDir(DataObjectProxy wheretosave, int depth)
      throws DataObjectProxyException {
      DataObjectProxy srcdop = this.resetDataObjectProxy(this.getURL());
      wheretosave.uploadDir(srcdop, depth);
   }

   /**
    * Download the directory and it's descendants specified by this
    * DataObjectProxy to the location specified by the destination proxy.
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
   public void downloadDir(URL wheretosave, int depth)
      throws DataObjectProxyException {
      DataObjectProxy destdop =
         DataObjectProxyFactory.getDataObjectProxy(wheretosave);

      this.downloadDir(destdop, depth);
   }

   /**
    * Return whether the object pointed to exists.
    *
    * @return true if the object exists, false otherwise.
    */
   public boolean exists() {
      File file = new File(mURL.getPath());

      return file.exists();
   }

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
   public Vector getChildrenURLs(int depth, boolean noDirs) throws DataObjectProxyException {
      Vector v = this.getChildrenURLs(depth, mURL);
      Vector v0 = (Vector) v.get(0);
      Vector v1 = (Vector) v.get(1);
      if (noDirs) {
    	  return v1;
      } else {
    	  return v0;
      }
   }

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
   public Vector getChildrenURLs(int depth) throws DataObjectProxyException {
	   return getChildrenURLs(depth,false);
   }
   /**
    * Get InputStream from URL being pointed to by the current
    * LocalDataObjectProxy.
    *
    * @return InputStream from the url.
    *
    * @throws DataObjectProxyException
    */
   public InputStream getInputStream() throws DataObjectProxyException {
      InputStream is = null;

      try {
         is = mURL.openStream();
      } catch (IOException ioe) {
         handleExceptions(ioe);
      }

      return is;
   }

   /**
    * Open the local file.
    *
    * @return Description of return value.
    */
   public File getLocalFile() {
      File f = null;

      try {

         /* uses 'toURI' to deal with spaces and other issues in URLs */
         f = new File(mURL.toURI().getPath());
      } catch (URISyntaxException ue) { }

      return f;
   }

   /**
    * Same as getInputStream() for a local file.
    *
    * @return InputStream from locally cached copy.
    *
    * @throws DataObjectProxyException
    */
   public InputStream getLocalInputStream() throws DataObjectProxyException {
      return getInputStream();
   }

   /**
    * getMeta.
    *
    * <p>Metadata is not defined for local files.</p>
    *
    * @return null
    *
    * @throws DataObjectProxyException Always throws.
    */
   public Object getMeta() throws DataObjectProxyException {
      throw new DataObjectProxyException("metadata not supported for local files");
   }

   /**
    * getMeta.
    *
    * <p>Metadata is not defined for local files.</p>
    *
    * @param  props properties to get.
    *
    * @return null.
    *
    * @throws DataObjectProxyException always throws.
    */
   public Object getMeta(Object props) throws DataObjectProxyException {
      throw new DataObjectProxyException("metadata not supported for local files");
   }


   /**
    * Returen the resource name not including the path, i.e., the file name.
    *
    * @return the resource name not including the path.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public String getResourceName() throws DataObjectProxyException {
      File file = null;

      /* uses 'toURI' to deal with spaces and other issues in URLs */
      file = new File(this.getURL().getPath());

      return file.getName();
   }

   /**
    * Get the value of the Etag.
    *
    * <p>For a local file, this is the modification time.</p>
    *
    * @return The etag value
    *
    * @throws DataObjectProxyException
    */
   public String getTag() throws DataObjectProxyException {

      /* If URL refers to a local file, we think
       * the content is changed if the modification date is changed, not changed
       * if the modification date is same as before.
       */
      return this.getURLLastModified();
   }

   /**
    * Get the tiem the file was last modified.
    *
    * @return The modified time (in a formated string).
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public String getURLLastModified() throws DataObjectProxyException {
      File f = null;

      try {

         /* uses 'toURI' to deal with spaces and other issues in URLs */
         f = new File(mURL.toURI().getPath());
      } catch (Exception e) {
         this.handleExceptions(e);
      }

      Date when = new Date(f.lastModified());

      return this.formatModificationDate(when);
   }


   /**
    * Get the username.
    *
    * @return username.
    */
   public String getUsername() { return mUsername; }

   /**
    * Get the file the LocalDataObjectProxyImpl points to, if it doesn't exist,
    * create a new file using the path and file name given by the url being
    * pointed to by the current LocalDataObjectImpl.
    *
    * <p>In this version, when this method is called for a file does not exist,
    * an empty file will be created.</p>
    *
    * <p>In future versions, this behavior may be changed.</p>
    *
    * @param  dest Description of parameter dest.
    *
    * @return File The file being pointed to by the current LocalDataObjectImpl
    *
    * @throws DataObjectProxyException
    */
   public File initLocalFile(File dest) throws DataObjectProxyException {
      boolean doCreate = true; // This may become a parameter.
      File file = dest;

      if (file == null) {

         try {
            file = new File(mURL.toURI().getPath());
         } catch (URISyntaxException ue) { }
      }

      if (file != null) {

         try {

            // If file doesn't exist, create a empty file based on url.
            if (!file.exists()) {

               if (doCreate) {
                  File p = file.getParentFile();
                  p.mkdirs();
                  file.createNewFile();
               } else {
                  throw new DataObjectProxyException(file + ": not found");
               }
            }
         } catch (Exception e) {
            handleExceptions(e);
         }

      } else {
         throw new DataObjectProxyException("No local path specified.");
      }

      return file;
   } // end method initLocalFile

   /**
    * Does the LocalDataObjectProxy point to a directory?
    *
    * @return true - if the URL points to a directory, false - otherwise.
    */
   public boolean isCollection() {
      File file = new File(mURL.getPath());

      return file.isDirectory();
   }


   /**
    * <p>Copy the the file being pointed to by the current URL to destination
    * file.</p>
    *
    * @param  dest - store the contents of URL to this destination file.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public void putFromFile(File dest) throws DataObjectProxyException {
      // Do nothing ?
   }


   // FUTURE:  what to do with metadata?  This method was commented for now.
   /*
    * Same as putFromFile. @param  dest  - store the contents of URL to this
    * destination file. @param  nsp   - property including namespace and
    * property key. @param  value - property value.
    */
   //  public void putFromFileWithProp(File dest, NSProperty nsp, String value)
   // throws Exception {              this.putFromFile(dest);               }


   /**
    * <p>Copy the given InputStream into the URL being pointed to by the current
    * LocalDataObjectProxy.</p>
    *
    * @param  is - InputStream to be put into current URL.
    *
    * @throws DataObjectProxyException
    */
   public void putFromStream(InputStream is) throws DataObjectProxyException {

      try {
         File file = new File(mURL.getPath());
         BufferedInputStream isr = new BufferedInputStream(is);
         BufferedOutputStream osr =
            new BufferedOutputStream(new FileOutputStream(file));
         byte[] b = new byte[4096];

         while (isr.available() > 0) {
            int howmany = isr.read(b);
            osr.write(b, 0, howmany);
         }

         osr.close();
         isr.close();

      } catch (IOException ioe) {
         handleExceptions(ioe);
      }
   }

   /**
    * Actually the method is copying the file refered by mURL to the given
    * localpath.
    *
    * @param  dest the destination to copy to.
    *
    * @return the destination file.
    *
    * @throws DataObjectProxyException
    */
   public File readFile(File dest) throws DataObjectProxyException {

      if (this.isCollection()) {

         /* can't download this! */
         throw new DataObjectProxyException(this.getURL() +
                                            " is a collection ");
      }

      if (dest == null) {
         File f = null;

         try {
            f = new File(mURL.toURI().getPath());
         } catch (URISyntaxException ue) { }

         return f;
         // throw new DataObjectProxyException("Destination is null");
      }

      File lf = initLocalFile(dest);
      this.readTheFile(lf.getAbsolutePath());

      return lf;
   }

   /**
    * Remove directory refered by this DOP.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public void removeDirectory() throws DataObjectProxyException {
      DataObjectProxy tempdop = this;

      if (!(this.isCollection())) {
         boolean success = new File(this.getURL().getFile()).delete();

         if (!success) {
            throw new DataObjectProxyException("Delete File " +
                                               this.getURL().getFile() +
                                               " Failed.");
         }
      } else {
         Vector childurls = this.getChildrenURLs(DataObjectProxy.DEPTH_1, false);

         for (int i = 0; i < childurls.size(); i++) {

            try {
               tempdop =
                  tempdop.resetDataObjectProxy(new URL(childurls.elementAt(i)
                                                                .toString()));
            } catch (MalformedURLException me) {
               this.handleExceptions(me);
            }

            if (!tempdop.isCollection()) {
               boolean success = new File(tempdop.getURL().getFile()).delete();

               if (!success) {
                  throw new DataObjectProxyException("Delete File " +
                                                     tempdop.getURL().getFile() +
                                                     " Failed.");
               }
            } else {

               if (!tempdop.getURL().equals(this.getURL())) {
                  tempdop.removeDirectory();
               }
            }
         }

         boolean success = new File(this.getURL().getFile()).delete();

         if (!success) {
            throw new DataObjectProxyException("Delete File " +
                                               tempdop.getURL().getFile() +
                                               " Failed.");
         }
      } // end if

   } // end method removeDirectory


   /**
    * Get a new DataObjectProxy represents the new URL.
    *
    * @param  newURL URL to be used to create a DataObjectProxy.
    *
    * @return a DataObjectProxy.
    *
    * @throws DataObjectProxyException Unknown problem.
    */
   public DataObjectProxy resetDataObjectProxy(URL newURL)
      throws DataObjectProxyException {
      return resetDataObjectProxy(newURL, mUsername, mPassword);
   }

   /**
    * Get a new DataObjectProxy based on the new URL, username and password.
    *
    * @param  newURL  - URL to be used to create a DataObjectProxy.
    * @param  newUser - username to access the URL.
    * @param  newPass - password to access the URL.
    *
    * @return DataObjectProxy.
    *
    * @throws DataObjectProxyException Unknown problem.
    */
   public DataObjectProxy resetDataObjectProxy(URL newURL, String newUser,
                                               String newPass)
      throws DataObjectProxyException {
      return DataObjectProxyFactory.getDataObjectProxy(newURL, newUser,
                                                       newPass);
   }

   /**
    * Search metadata. Not defined for local files (yet?)
    *
    * @return null.
    *
    * @throws DataObjectProxyException Always throws DataObjectException
    */
   public Object searchMeta() throws DataObjectProxyException {
      throw new DataObjectProxyException("metadata not supported for local files");
   }

   /**
    * <p>Resetting password for localDataObjectProxyImpl is probably not
    * necessary. This method would not do anything.</p>
    *
    * @param pass - password to be used.
    */
   public void setPassword(String pass) { }

   /**
    * See reason in webdavObjectProxyImpl.
    *
    * @param user User name.
    * @param pass Password.
    */
   public void setUserInfo(String user, String pass) {
      this.mUsername = user;
      this.mPassword = pass;
   }

   /**
    * Reset the username.
    *
    * @param s - username to be used.
    */
   public void setUsername(String s) { mUsername = s; }

   /**
    * Copy the source directory to the destination, along with children.
    *
    * <p>This recreates a file hierarchy at the destination</p>
    *
    * <p>This is called by 'downloadDir'.</p>
    *
    * <p><b>Warning</b> This method does not have sophisticated error checking
    * or handling, and may beahave differently on different file systems. For
    * example, if there are links or loops in the directory structure, the
    * result is uncertain.</p>
    *
    * @param  srcdop Source to copy
    * @param  depth  Depth of the copy, currently 1 or infinite.
    *
    * @throws DataObjectProxyException
    */
   public void uploadDir(DataObjectProxy srcdop, int depth)
      throws DataObjectProxyException {

      if (
          depth != DataObjectProxy.DEPTH_1 &&
             depth != DataObjectProxy.DEPTH_INFINITY) {
         throw new DataObjectProxyException("Depth value is not valid");
      }

      // 1. create the first directory, if needed
      String firstdir = srcdop.getResourceName();

      DataObjectProxy dop2 =
         DataObjectProxyFactory.getDataObjectProxy(this.getURL(),
                                                   this.getUsername(),
                                                   this.getPassword());

      DataObjectProxy dop3 = dop2.createCollection(firstdir);

      String parenturl = dop3.getURL().toString();

      // get the children in the source area
      Vector childrenURLs = srcdop.getChildrenURLs(DataObjectProxy.DEPTH_1, false);

      DataObjectProxy childdop;
      String tempurl;

      DataObjectProxy tempdop = this.resetDataObjectProxy(this.getURL());
      URL childurl = null;

      try {

         for (int i = 0; i < childrenURLs.size(); i++) {
            childurl = new URL(childrenURLs.elementAt(i).toString());

            childdop = srcdop.resetDataObjectProxy(childurl);

            if (!(childdop.isCollection())) {
               String childname = childurl.getPath();

               if (childname.contains("/")) {
                  childname = childname.substring(childname.lastIndexOf('/'));
               }

               tempurl = parenturl + "/" + childname;

               tempdop = this.resetDataObjectProxy(new URL(tempurl));

               InputStream is = childdop.getInputStream();
               tempdop.putFromStream(is);
            } else {

               if (depth == DataObjectProxy.DEPTH_1) {
                  continue;
               }

               if (srcdop.getURL().sameFile(childdop.getURL())) {
                  continue;
               }

               tempdop = this.resetDataObjectProxy(new URL(parenturl));

               tempdop.uploadDir(childdop, depth);
            }

         } // end for

      } catch (Exception e) {
         this.handleExceptions(e);
      }

   } // end method uploadDir


} // end class LocalDataObjectProxyImpl
