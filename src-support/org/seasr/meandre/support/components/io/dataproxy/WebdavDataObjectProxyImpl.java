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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.scidac.cmcs.dsmgmt.dsi.DSI;
import org.scidac.cmcs.dsmgmt.dsi.DSIProperty;
import org.scidac.cmcs.dsmgmt.dsi.StatusException;
import org.scidac.cmcs.dsmgmt.util.ResourceList;
import org.scidac.cmcs.util.NSProperty;


/**
 * WebdavDataObjectProxyImpl manages access to files through the http/https
 * protocol. Users can read file or InputStream from, write file or InputStream
 * to the url.
 *
 * <p>This class is built on the Data Storage Interface (DSI) from the SCIDAC
 * CMCS project.</p>
 *
 * <p>See <u>http://collaboratory.emsl.pnl.gov/</u></p>
 *
 * @author  $Author: mcgrath $
 * @version $Revision: 1.6 $, $Date: 2007/05/18 17:12:23 $
 *
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public class WebdavDataObjectProxyImpl extends DataObjectProxy {

   //~ Instance fields *********************************************************

   /** The read cache manager. */
   private DataObjectCacheManager cache = null;

   /** Internal flag to mark attempt to access non-enabled server. */
   private final boolean isnotwebdav = false;

   /** The locally cached file. */
   private File localCopy = null;

   /** Internal variable: false will disable the read cache. */
   private final boolean usingCache = true;

   /** The DSI object, implements access to WebDAV. */
   protected DSI mDSI = null;

   private static Logger _logger = Logger.getLogger("WebdavDataObjectProxyImpl");

   //~ Constructors ************************************************************

   /**
    * WebdavDataObjectProxyImpl.
    *
    * <p>Creates a new WebdavDataObjectProxyImpl object based on the given
    * URL.</p>
    *
    * @param  url - the URI to a WebDAV server.
    *
    * @throws DataObjectProxyException Error creating proxy.
    */
   public WebdavDataObjectProxyImpl(URL url) throws DataObjectProxyException {
      this(url, "", "");
   }

   /**
    * WebdavDataObjectProxyImpl.
    *
    * <p>Creates a new WebdavDataObjectProxyImpl object.</p>
    *
    * <p>This method sets log levels for the underlying packages.</p>
    *
    * @param  url      - the URI to a WebDAV server.
    * @param  username - username to access the server.
    * @param  password - password to access the server.
    *
    * @throws DataObjectProxyException Error creating proxy, possibly because
    *                                  server is not webdav enabled.
    */
   public WebdavDataObjectProxyImpl(URL url, String username, String password)
      throws DataObjectProxyException {

      try {
         cache = DataObjectCacheManagerFactory.getCacheManager();
      } catch (DataObjectProxyException dopex) {
         String msg = "Error initializing Cache Manager";
         _logger.severe(msg + " " + dopex);
      }

      mURL = url;

      try {

         if (isnotwebdav) {
            throw new DataObjectProxyNotSupportedException("resource does not support access method");
         }
      } catch (Exception e) {
         e.printStackTrace();
         throw new DataObjectProxyException(e);
      }

      if (!username.equals("")) {
         this.setUsername(username);
      }

      if (!password.equals("")) {
         this.setPassword(password);
      }

      if (!this.getUsername().equals("")) {
         mDSI = new DSI(url.toString(), this.getUsername(), this.getPassword());
      } else {
         mDSI = new DSI(url.toString());
      }

   }

   //~ Methods *****************************************************************

   /**
    * Check if the URL ends in a '/'
    *
    * <p>If so, the Web server will be confused if you try to put a file to the
    * URL.</p>
    *
    * @return Description of return value.
    */
   private boolean checkLastComp() {

      try {
         URL pu = new URL(mDSI.getURL());
         String pt = pu.getPath();

         if (pt.contains("/")) {
            pt = pt.substring(pt.lastIndexOf('/') + 1);

            if (pt.length() == 0) {
               return false;
            }
         }
      } catch (MalformedURLException mfe) {
         return false;
      }

      return true;
   }

   /*
    * Check if userID and password are necessary to access the url if yes,
    * return true; if no, return no; How ?
    */
/*
 * public boolean checkAuth() {   boolean ret = false;
 *
 * return ret; }
 */
   /**
    * Delete all of the temp files created during the process.
    *
    * <p>When</p>
    */
   private void cleanUp() {

      if (usingCache) {

         /*
          * When using the cache, call the cache manager to clean out the cache
          * areas.
          */
         if (tempDataDir == null) {

            try {
               setTempDataDir();
            } catch (DataObjectProxyException d) { }
         }

         cache.cleanup(tempDataDir);

         return;
      }

      /* Non cache version called only if 'useCache' is off */
      /*
       * Delete the files inside temp data directory. For all of the files which
       * can not be deleted, delete then on exit.
       */
      if (tempDataDir != null) {
         boolean isTempDataDel = deleteDir(tempDataDir);

         if (!isTempDataDel) {
            File[] children = tempDataDir.listFiles();

            for (int i = 0; i < children.length; i++) {
               children[i].delete();
            }
         }

      }
   } // end method cleanUp


   /**
    * This is a mapping of D2K log4j log levels to java.util.logging levels.
    *
    * @param  lin Description of parameter lin.
    *
    * @return Description of return value.
    */
//   private java.util.logging.Level convertLevel(org.apache.log4j.Level lin) {
//
//      if (lin == null) {
//
//         // Fatal
//         return null;
//      }
//
//      if (lin == org.apache.log4j.Level.DEBUG) {
//         return java.util.logging.Level.FINEST;
//      } else if (lin == org.apache.log4j.Level.WARN) {
//         return java.util.logging.Level.WARNING;
//      } else if (lin == org.apache.log4j.Level.INFO) {
//         return java.util.logging.Level.INFO;
//      } else if (lin == org.apache.log4j.Level.ERROR) {
//         return java.util.logging.Level.SEVERE;
//      } else if (lin == org.apache.log4j.Level.FATAL) {
//         return java.util.logging.Level.SEVERE;
//      } else if (lin == org.apache.log4j.Level.OFF) {
//         return java.util.logging.Level.OFF;
//      } else if (lin == org.apache.log4j.Level.ALL) {
//         return java.util.logging.Level.ALL;
//      } else {
//         return java.util.logging.Level.INFO;
//      }
//   }


   /**
    * Create all the components of a path pu/rest.
    *
    * <p>Given a source URL, create all the components of the path at the
    * destination.</p>
    *
    * @param  pu          The base URL formwhich to create.
    * @param  rest        A relative path.
    * @param  includelast If true, create the whole path, if false, create all
    *                     but the last component.
    *
    * @throws DataObjectProxyException
    */

   private void createPath(URL pu, String rest, boolean includelast)
      throws DataObjectProxyException {

      // get the path to the file
      try {

         String pt = pu.getPath();

         if (!includelast) {
            pt = pt.substring(0, pt.lastIndexOf('/'));
         }

         if (!rest.equals("")) {
            pt = new String(pt + "/" + rest);
         }

         URL pcheck =
            new URL(pu.getProtocol(), pu.getHost(), pu.getPort(), "/" +
                    pt);
         DSI dcheck = null;

         if (this.getUsername().equals("")) {
            dcheck = new DSI(pcheck.toString());
         } else {
            dcheck =
               new DSI(pcheck.toString(), this.getUsername(),
                       this.getPassword());
         }

         if (dcheck.exists()) {
            return;
         }

         String[] comps = pt.split("/");
         String path = new String("");
         String ppath = new String("");
         String newpath;

         for (int i = 0; i < comps.length; i++) {

            if (comps[i] == null || comps[i].equals("")) {
               continue;
            }

            path = ppath + comps[i];

            URL pp =
               new URL(pu.getProtocol(), pu.getHost(), pu.getPort(), "/" +
                       path);

            DSI d2 = null;

            if (this.getUsername().equals("")) {
               d2 = new DSI(pp.toString());
            } else {
               d2 =
                  new DSI(pp.toString(), this.getUsername(),
                          this.getPassword());
            }

            if (!d2.exists()) {

               URL root =
                  new URL(pu.getProtocol(), pu.getHost(), pu.getPort(), "/" +
                          ppath);

               DSI rootdsi = null;

               if (this.getUsername().equals("")) {
                  rootdsi = new DSI(root.toString());
               } else {
                  rootdsi =
                     new DSI(root.toString(), this.getUsername(),
                             this.getPassword());
               }

               try {
                  newpath = rootdsi.makeCollection(comps[i]);
               } catch (StatusException se) {

                  if (se.getStatusCode() == 405 || se.getStatusCode() == 409) {

                     /* already exists? */
                	  _logger.warning("405 or 409 " + se.getStatusCode());
                  }

                  se.printStackTrace();
                  handleExceptions(se);
               }

            } // end if

            ppath = path + "/";
         } // end for
      } catch (MalformedURLException mfe) {
         this.handleExceptions(mfe);
      } catch (StatusException se) {
         this.handleExceptions(se);
      }
   } // end method createPath

   /**
    * <p>Create a temporary local file in the specified directory and save the
    * path of this temporary file into a Vector.</p>
    *
    * @throws IOException              If a file could not be created.
    * @throws DataObjectProxyException
    *
    * @see    setTempDataDir.
    */
   private void createTempFile() throws DataObjectProxyException {

      if (tempDataDir == null) {
         setTempDataDir();
      }

      try {
         localCopy = File.createTempFile("d2k", ".out", tempDataDir);
      } catch (IOException ioe) {
         handleExceptions(ioe);
      }

      if (tempFilesCreated == null) {
         tempFilesCreated = new Vector();
      }

      /*
       * For future use, save the path of the newly created files into a Vector.
       * Not needed when using the cache.
       */
      tempFilesCreated.add(localCopy.getAbsolutePath());
   }


   /**
    * <p>Delete all of its children resources of a directory. Only used to clean
    * up temporary files.</p>
    *
    * @param  dir - the directory to be cleaned.
    *
    * @return true if all of the children resources are deleted successfully,
    *         false otherwise.
    */
   private boolean deleteDir(File dir) {
      boolean ret = true;

      if (dir.isDirectory()) {
         String[] children = dir.list();

         for (int i = 0; i < children.length; i++) {
            boolean success = deleteDir(new File(dir, children[i]));

            if (!success) {
               ret = false;
            }
         }
      } else {
         ret = false;
      }

      return ret;
   }


   /**
    * Format a Date in to a string.
    *
    * @param  when The date.
    *
    * @return String
    *
    * @throws DataObjectProxyException
    */
   private String formatModificationDate(Date when)
      throws DataObjectProxyException {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      sdf.setTimeZone(TimeZone.getDefault());

      return sdf.format(when);
   }


   /**
    * Recursively list all the children of the URL as URLs.
    *
    * <p>This method repeately calls the server to list the directories, and
    * descends into the children to construct a depth first traversal.</p>
    *
    * @param  depth depth to list, DEPTH_1 or DEPTH_INFINITY
    * @param  url   The URL to list.
    *
    * @return A vector with alist of URLs.
    *
    * @throws DataObjectProxyException Encapsulates errors found in the process.
    */
   private Vector getChildrenURLs(int depth, URL url)
      throws DataObjectProxyException {
      Vector childrenURLs = new Vector();
      Vector retVecs = new Vector();
      Vector noDirs = new Vector();
      Vector childrenPaths = new Vector();
      Vector properties = new Vector();

      DataObjectProxy tempdop = this.resetDataObjectProxy(url);

      DSI lDSI = null;

      if (this.getUsername().equals("")) {
         lDSI = new DSI(url.toString());
      } else {

         /* assume uses the same credentials... */
         lDSI = new DSI(url.toString(), this.getUsername(), this.getPassword());
      }

      /*
       * Get the children urls of input url with depth 1
       */
      properties.add(new NSProperty("DAV::displayname"));

      ResourceList rl = null;

      try {

         /*  the boolean is for setting separateParent
          * But it seems not working
          */
         rl = lDSI.getResources(properties, DataObjectProxy.DEPTH_1, false);
      } catch (StatusException e) {
         this.handleExceptions(e);
      }

      rl.selectAll();
      childrenPaths = rl.getSelectedFilePaths();

      String server = rl.getServer();

      if (childrenPaths.size() == 0) {
    	  retVecs.add(childrenURLs);
    	  retVecs.add(noDirs);
         /* some servers return empty list when no children */
         return retVecs; //childrenURLs;
      }

      /* check for alternative behavior on servers */
      String first = (String) childrenPaths.firstElement();
      boolean hasServer = first.startsWith(server);

      /*
       * If depth is 1, DSI getResources method works fine
       */
      if (depth == DataObjectProxy.DEPTH_1) {

         for (int i = 0; i < childrenPaths.size(); i++) {
            URL utmp = null;

            try {

               if (hasServer) {
                  utmp = new URL((String) childrenPaths.elementAt(i));
               } else {
                  utmp = new URL(server + childrenPaths.elementAt(i));
               }
            } catch (Exception ex) {
               this.handleExceptions(ex);
            }

            childrenURLs.add(utmp);
            if (!(utmp.toString().equals(url.toString()))) {
                tempdop = tempdop.resetDataObjectProxy(utmp);
                if (!tempdop.isCollection()) {
                    noDirs.add(tempdop.getURL());
                 }
             }
         }
      }

      /*
       * If depth is infinity, DSI getResources method does not work for url
       * with more than 3 levels.
       *
       * So, recurse to implementa a depth first list.
       */
      if (depth == DataObjectProxy.DEPTH_INFINITY) {

         if (tempdop.isCollection()) {
            childrenURLs.add(url);

            /*
             * Loop through all of the children at this level.
             */
            URL walker = null;

            for (int i = 0; i < childrenPaths.size(); i++) {

               try {

                  if (hasServer) {
                     walker = new URL((String) childrenPaths.elementAt(i));
                  } else {
                     walker = new URL(server + childrenPaths.elementAt(i));
                  }

                  /*
                   * If the path is not same as the input url
                   */
                  if (!(walker.toString().equals(url.toString()))) {
                     tempdop = tempdop.resetDataObjectProxy(walker);
                  }
               } catch (Exception ex) {
                  this.handleExceptions(ex);
               }

               /*
                * If the walker is not a collection (directory), just add url in
                * childrenURLs
                */
               if (!tempdop.isCollection()) {
                  childrenURLs.add(tempdop.getURL());
                  noDirs.add(tempdop.getURL());
               }

               /*
                * If the walker is a collection and not same as input url
                * recursively call this method with walker as input url
                */
               else {

                  if (!(walker.toString().equals(url.toString()))) {
                     Vector vsubdir =
                        this.getChildrenURLs(DataObjectProxy.DEPTH_INFINITY,
                                             walker);
                     Vector subdir = (Vector) vsubdir.get(0);
                     for (int k = 0; k < subdir.size(); k++) {
                        if (!childrenURLs.contains(subdir.elementAt(k))) {
                           childrenURLs.add(subdir.elementAt(k));
                        }
                     }
                     Vector sd1 = (Vector)vsubdir.get(1);
                     for (int k = 0; k < sd1.size(); k++) {
                         noDirs.add(sd1.elementAt(k));
                      }
                  }
               }
            } // end for
         } // end if
      } // end if
      retVecs.add(childrenURLs);
	  retVecs.add(noDirs);
     return retVecs; //childrenURLs;
   //   return childrenURLs;
   } // end method getChildrenURLs

   /**
    * Get the value of the specified property on the current url.
    *
    * @param  prop - the property of interest including namespace
    *
    * @return a possibly null DSIProperty object
    *
    * @throws DataObjectProxyException
    */
   private Object getMeta(NSProperty prop) throws DataObjectProxyException {
      Object ret = null;

      try {
         ret = mDSI.getMetaData(prop);
      } catch (StatusException se) {
         handleExceptions(se);
      }

      return ret;
   }

   /**
    * Handles exceptions.
    *
    * @param  ex Description of parameter ex.
    *
    * @throws DataObjectProxyException
    */
   private void handleExceptions(Exception ex) throws DataObjectProxyException {

      /*
       * HttpException method getReasonCode() is deprecated. DSI
       * handleExceptions() method is using this deprecated method. The
       * following se.getStatusCode() might not give correct status code because
       * the usage of this deprecated method.
       */
      if (ex instanceof StatusException) {
         StatusException se = (StatusException) ex;
         Throwable theCause = se.getCause();

         if (se.getStatusCode() == 200) {
            throw new DataObjectProxyException("Unknown error (200), possibly authorization failed");
         } else if (theCause != null && theCause instanceof StatusException) {

            // extract the encased exception
            StatusException se2 = (StatusException) theCause;

            if (se2.getStatusCode() == 401) {
               throw new DataObjectProxyException("Unauthorized (401)");
            } else if (se2.getStatusCode() == 200) {
               throw new DataObjectProxyException("Unknown error (200), possibly authorization failed");
            }
         }

         throw new DataObjectProxyException("*****StatusException " +
                                            se.getStatusCode() + " " +
                                            se.getImprovedMessage());
      } else if (ex instanceof FileNotFoundException) {
         FileNotFoundException fe = (FileNotFoundException) ex;
         throw new DataObjectProxyException("*****FileNotFoundException " +
                                            fe.getLocalizedMessage());
      } else if (ex instanceof MalformedURLException) {
         MalformedURLException me = (MalformedURLException) ex;
         throw new DataObjectProxyException("******MalformedURLException " +
                                            me.getLocalizedMessage());
      } else {
         throw new DataObjectProxyException("******Unknown Exception " +
                                            ex.getLocalizedMessage());
      }
   } // end method handleExceptions

   /**
    * try to deduce that this URL can be accessed via WEBDAV.
    *
    * @return Description of return value.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   private boolean isWebDav() throws DataObjectProxyException {
      boolean iswebdavsupport = false;
      String protocol = mURL.getProtocol();

      if (
          !protocol.equalsIgnoreCase("http") &&
             !protocol.equalsIgnoreCase("https")) {
         return false;
      }

      HttpClient client = new HttpClient();

      try {

         // need to construct an escaped URL here....
         HttpURL u = null;

         /* This section is correct for httpclient-3.0.1, but fails with older
          * servers if (!this.getUsername().equals("")) {
          * UsernamePasswordCredentials credentials = new
          * UsernamePasswordCredentials(mUsername,mPassword);
          * client.getState().setCredentials(new
          * AuthScope(mURL.getHost(),mURL.getPort()),credentials); }
          */
         /* the folloing works for earlier */
         if (mURL.getProtocol().equals("http")) {
            u = new HttpURL(mURL.toString());
         } else if (mURL.getProtocol().equals("https")) {
            u = new HttpsURL(mURL.toString());
         }

         if (!this.getUsername().equals("")) {
            u.setUserinfo(mUsername, mPassword);
         }

         OptionsMethod optionsmethod = new OptionsMethod(u.toString());
         int returncode = client.executeMethod(optionsmethod);
         Header[] headers = optionsmethod.getResponseHeaders();

         for (int i = 0; i < headers.length; i++) {

            if (headers[i].toString().contains("DAV")) {
               iswebdavsupport = true;

               break;
            }
         }
      } catch (HttpException he) {

         // he.printStackTrace();
         handleExceptions(he);
      } catch (Exception ex) {

         // ex.printStackTrace();
         return false;
      }

      return iswebdavsupport;
   } // end method isWebDav

   /**
    * Get the file from the remote server.
    *
    * @throws
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   private void loadFile() throws DataObjectProxyException {

      try {

         if (localCopy == null) {
            createTempFile();
         }

         if (localCopy != null /* and not read yet */) {
            localCopy = mDSI.getDataSet(localCopy.getAbsolutePath());
         }

      } catch (Exception e) {
         this.handleExceptions(e);
      }

   }

   /**
    * Delete a directory and all its contents.
    *
    * @throws DataObjectProxyException
    */
   private void removeResource() throws DataObjectProxyException {

      try {
         mDSI.removeResource();
      } catch (Exception e) {
         this.handleExceptions(e);
      }
   }

   /**
    * Set the java.util.loggin level.
    *
    * @param l Description of parameter l.
    */
   private void setJavaUtilLogger(java.util.logging.Level l) {
      LogManager lm = LogManager.getLogManager();

      java.util.Enumeration e = lm.getLoggerNames();

      while (e.hasMoreElements()) {
         Logger jlogger = lm.getLogger((String) e.nextElement());
         jlogger.setLevel(l);
      }


   }

   /**
    * <p>Try to create a directory to store locally cached files in the users
    * working directory or users home directory. If users don't have writing
    * access to either of these two directories, throw a
    * DataObjectProxyException.</p>
    *
    * @throws DataObjectProxyException
    */
   private void setTempDataDir() throws DataObjectProxyException {
      File parentDir = new File(System.getProperty("user.dir"));

      if (parentDir.canWrite()) {
         tempDataDir = new File("tempData");
      } else {
         parentDir = new File(System.getProperty("user.home"));

         if (parentDir.canWrite()) {
            tempDataDir = new File(parentDir + File.separator + "tempData");
         } else {
            throw new DataObjectProxyException("Need write access to \n" +
                                               System.getProperty("user.dir") +
                                               "\n or \n" +
                                               System.getProperty("user.home"));
         }

      }

      tempDataDir.mkdirs();
   }

   /**
    * Get the password to access the server.
    *
    * @return the current password to access the server.
    */
   @Override
protected String getPassword() {

      if (mPassword == null) {
         mPassword = "";
      }

      return mPassword;
   }

   /**
    * <p>Close DSI object, clean up the tempory files created, username and
    * password</p>
    */
   @Override
public void close() {

      if (mDSI != null) {
         mDSI.close();
      }

      mUsername = null;
      mPassword = null;
      cleanUp();
   }

   /**
    * Create a directory at path.
    *
    * @param  relativePath a relative path (relative to current URL).
    *
    * @return DataObjectProxy for the new object.
    *
    * @throws DataObjectProxyException Encapsulate errors in the process.
    */

   @Override
public DataObjectProxy createCollection(String relativePath)
      throws DataObjectProxyException {


      createPath(this.getURL(), relativePath, true);

      try {
         URL newU = new URL(mDSI.getURL());

         newU = new URL(mDSI.getURL() + "/" + relativePath);

         return this.resetDataObjectProxy(newU,
                                          this.getUsername(),
                                          this.getPassword());
      } catch (MalformedURLException mfe) {
         handleExceptions(mfe);
      }

      return this;
   }

   /**
    * Download the current collection refered by this proxy to destination.
    *
    * <p>This calls uploadDir</p>
    *
    * @param  wheretosave : A local or remote directory to store the files
    * @param  depth       An integer to indicate how you would like to download
    *                     the directory. There are only two valid values for
    *                     depth: DataObjectProxy.DEPTH_1: Only download the
    *                     files under the collection, no sub directories
    *                     DataObjectProxy.DEPTH_INFINITY: download all of the
    *                     files and subdirectories.
    *
    * @throws DataObjectProxyException
    */

   @Override
public void downloadDir(DataObjectProxy wheretosave, int depth)
      throws DataObjectProxyException {
      DataObjectProxy srcdop = this.resetDataObjectProxy(this.getURL());
      wheretosave.uploadDir(srcdop, depth);
   } // end method downloadDir

   /**
    * Download the current collection referred by this proxy to destination URL.
    *
    * <p><b>Warning:</b> This method does not implement authentication. To do
    * so, use downloadDir(dop).</p>
    *
    * <p>This calls uploadDir</p>
    *
    * @param  wheretosave : A local or remote directory to store the files
    * @param  depth       An integer to indicate how you would like to download
    *                     the directory. There are only two valid values for
    *                     depth: DataObjectProxy.DEPTH_1: Only download the
    *                     files under the collection, no sub directories
    *                     DataObjectProxy.DEPTH_INFINITY: download all of the
    *                     files and subdirectories.
    *
    * @throws DataObjectProxyException
    */
   @Override
public void downloadDir(URL wheretosave, int depth)
      throws DataObjectProxyException {

      /* this method will fail if the destination requires user/password */
      DataObjectProxy destdop =
         DataObjectProxyFactory.getDataObjectProxy(wheretosave);

      this.downloadDir(destdop, depth);
   } // end method downloadDir

   /**
    * Return whether the object pointed to exists.
    *
    * @return true if the object exists, false otherwise.
    */
   @Override
public boolean exists() {
      boolean isexist = false;

      try {
         isexist = mDSI.exists();
      } catch (StatusException se) {
         /* pretty much any error means it doesn't exist... */
      }

      return isexist;
   }

   /**
    * Return the collection url and all of its children.
    *
    * @param  depth --- indicator of DSI.DEPTH i can be one of DSI.DEPTH_0: only
    *               the current url will be included
    *
    *               <p>DSI.DEPTH_1: The children files and subdirectories will
    *               be included, not the chidren of those subdirectories
    *               DSI.DEPTH_INFINITY: All of files including direct children
    *               and children of subdirectories</p>
    *
    * @return the collection url and all of its children.
    *
    * @throws DataObjectProxyException
    */
   @Override
public Vector getChildrenURLs(int depth, boolean noDirs) throws DataObjectProxyException {

	   Vector tv =  getChildrenURLs(depth, mURL);
	   Vector v0 = (Vector) tv.get(0);
	   Vector v1 = (Vector) tv.get(1);
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
   @Override
public Vector getChildrenURLs(int depth) throws DataObjectProxyException {
	   return getChildrenURLs(depth,false);
   }

   /**
    * Get a input stream to the current DSI object.
    *
    * <p>Note: a collection (directory) is not allowed.</p>
    *
    * @return An input stream to read the remote object.
    *
    * @throws DataObjectProxyException Object is not a file or encapsulates
    *                                  other errors.
    */
   @Override
public InputStream getInputStream() throws DataObjectProxyException {
      InputStream ret = null;

      if (this.isCollection()) {

         /* can't download this! */
         throw new DataObjectProxyException(this.getURL() +
                                            " is a collection ");
      }

      try {
         ret = mDSI.getDataSetAsFIS();
      } catch (StatusException e) {
         handleExceptions(e);
      }

      return ret;
   }

   /**
    * Get the local file, e.g., in the cache.
    *
    * @return The File object for the local file.
    */
   @Override
public File getLocalFile() { return localCopy; }

   /**
    * Get a InputStream from locally cached copy.*
    *
    * <p>Note: a collection (directory) is not allowed.</p>
    *
    * @return a InputStream from locally cached copy
    *
    * @throws DataObjectProxyException
    */
   @Override
public InputStream getLocalInputStream() throws DataObjectProxyException {

      if (this.isCollection()) {

         /* can't download this! */
         throw new DataObjectProxyException(this.getURL() +
                                            " is a collection ");
      }

      InputStream is = null;
      String copyTo = null; // in this case, allocate a local temp file
      localCopy = cache.getCachedCopy(this, copyTo, true);

      try {
         is = new FileInputStream(localCopy);
      } catch (Exception e) {
         this.handleExceptions(e);
      }

      return is;
   }

   /**
    * Get the metadata of the current DSI object.
    *
    * @return a meatdata Hashtable
    *
    * @throws DataObjectProxyException
    */
   @Override
public Hashtable getMeta() throws DataObjectProxyException {
      Hashtable ret = null;

      try {
         ret = mDSI.getMetaData();
      } catch (StatusException se) {
         handleExceptions(se);
      }

      return ret;
   }

   /**
    * Get metadata.
    *
    * @param  prop The NSProp
    *
    * @return the values.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   @Override
public Object getMeta(Object prop) throws DataObjectProxyException {
      return getMeta((NSProperty) prop);
   }

   /**
    * Give the resource name not including the path, i.e. the last component of
    * the path.
    *
    * @return the resource name not including the path.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   @Override
public String getResourceName() throws DataObjectProxyException {
      Object rsrname = null;
      NSProperty prop = new NSProperty("DAV::displayname");


      DSIProperty val = (DSIProperty) this.getMeta(prop);
      String rn = "";
      rn = val.getPropertyAsString();

      return rn;
   }

   /**
    * Get the Etag from the server.
    *
    * <p>The Etag will change if the file is updated on the server.</p>
    *
    * @return The tag.
    *
    * @throws DataObjectProxyException
    */
   @Override
public String getTag() throws DataObjectProxyException {

      /* If URL refers to a local file, we think
       * the content is changed if the modification date is changed, not changed
       * if the modification date is same as before.
       */
      NSProperty propNamex = new NSProperty("DAV:", "getetag");
      DSIProperty val = (DSIProperty) this.getMeta(propNamex);
      String tag = val.getPropertyAsString();

      return tag;
   }

   /**
    * Get the modification date.
    *
    * <p>The data is not returned correctly from some servers.</p>
    *
    * @return The modification date.
    *
    * @throws DataObjectProxyException
    */
   @Override
public String getURLLastModified() throws DataObjectProxyException {
      NSProperty propNamex = new NSProperty("DAV:", "getlastmodified");
      DSIProperty val = (DSIProperty) this.getMeta(propNamex);
      String modified = val.getPropertyAsString();

      return modified;

   }

   /**
    * Get the current user to access the current WebDataObjectProxy.
    *
    * @return username.
    */
   @Override
public String getUsername() {

      if (mUsername == null) {
         mUsername = "";
      }

      return mUsername;
   }

   /**
    * Get locally cached file, creating it if necesary.
    *
    * @param  dest Description of parameter dest.
    *
    * @return locally cached file
    *
    * @throws DataObjectProxyException
    */
   @Override
public File initLocalFile(File dest) throws DataObjectProxyException {

      if (dest != null) {
         localCopy = dest;

         if (!localCopy.exists()) {

            try {
               File p = localCopy.getParentFile();

               if (!p.exists()) {

                  try {
                     p.mkdirs();
                  } catch (Exception io) {
                     // nothing
                  }
               }

               localCopy.createNewFile();
            } catch (IOException ioe) {
               handleExceptions(ioe);
            }
         }

         return localCopy;
      }

      if (localCopy == null || (!localCopy.exists())) {
         createTempFile();
      }

      return localCopy;
   } // end method initLocalFile

   /**
    * Test if the current DSI represents a collection or not.
    *
    * @return true if DSI represents a collection, false otherwise;
    *
    * @throws DataObjectProxyException
    */
   @Override
public boolean isCollection() throws DataObjectProxyException {
      boolean ret = false;

      try {
         ret = mDSI.isCollection();
      } catch (StatusException se) {
         handleExceptions(se);
      }

      return ret;
   }

   /**
    * Put a local file to the url represented by the current DSI object.
    *
    * <p>This method will try to create the path necessary to store the file at
    * the given URL.</p>
    *
    * @param  file - local file to be put to url.
    *
    * @throws DataObjectProxyException
    */
   @Override
public void putFromFile(File file) throws DataObjectProxyException {
      boolean doCreate = true;

      try {

         if (!mDSI.exists()) {

            if (doCreate) {
               createPath(this.getURL(), "", false);
            }
         }

         if (checkLastComp()) {
            mDSI.putDataSet(file);
         } else {
            /* All done--everything should be OK */
            // throw new DataObjectProxyException(mDSI.getURL() +
         }
      } catch (StatusException se) {
         this.handleExceptions(se);
      }
   }


   /*
    * <p>Put a local file to the url represented by the current DSI object Put
    * the specified properties to the current url and its parent url.</p>
    *
    * @param  is - local file to be put to url
    *
    * @throws DataObjectProxyException
    */
   // public void putFromFileWithProp(File file, NSProperty nsp,String value)
   // throws DataObjectProxyException {   try { //       mDSI.putDataSet(file);
   //      this.putMeta(nsp,value); mDSI.setURL(DSI.splitUrl(mDSI.getURL())[0]);
   // this.putMeta(nsp,value);    mDSI.setURL(mURL.toString());   }   catch
   // (StatusException se){ this.handleExceptions(se);   }

   // }

   /**
    * <p>Put InputStream to the url being pointed to by the current
    * WebDataObjectProxyImpl.</p>
    *
    * <p>This method will try to create the path necessary to store the file at
    * the given URL.</p>
    *
    * @param  is - InputStream to be put to the current URL.
    *
    * @throws DataObjectProxyException
    */
   @Override
public void putFromStream(InputStream is) throws DataObjectProxyException {
      boolean doCreate = true;
      int verysmall = 1024;

      try {

         if (!mDSI.exists()) {

            if (doCreate) {
               createPath(this.getURL(), "", false);
            }
         }

         if (checkLastComp()) {

            try {

               if (is.available() < verysmall) {

                  /* this is a workaround for a strange bug seen when uploading
                   * to oracle's version of webdav.
                   */
                  File ftmp = File.createTempFile("xxx", ".tmp");
                  java.io.BufferedInputStream isr =
                     new java.io.BufferedInputStream(is);
                  java.io.BufferedOutputStream osr =
                     new java.io.BufferedOutputStream(new java.io.FileOutputStream(ftmp));
                  byte[] b = new byte[2 * verysmall];

                  while (isr.available() > 0) {
                     int howmany = isr.read(b);
                     osr.write(b, 0, howmany);
                  }

                  osr.close();

                  mDSI.putDataSet(ftmp);

                  ftmp.delete();

                  return;
               }
            } catch (Exception e) {
               this.handleExceptions(e);
            }

            mDSI.putDataSet(is);
         } else {

            // not sure what to do in this case
            throw new DataObjectProxyException(mDSI.getURL() +
                                               ": target is a directory?");
         }
      } catch (StatusException se) {
         this.handleExceptions(se);
      }
   } // end method putFromStream

   /**
    * Put a set of properties on the current WebDataObjectProxyImpl.
    *
    * @param  proptable - a Hashtable of key/value pairs.
    *
    * @throws DataObjectProxyException.
    * @throws DataObjectProxyException  Description of exception
    *                                   DataObjectProxyException.
    */
   public void putMeta(Hashtable proptable) throws DataObjectProxyException {

      try {
         mDSI.putMetaData(proptable);
      } catch (StatusException se) {
         this.handleExceptions(se);
      }
   }

   /**
    * Put a single property on the current WebDataObjectProxyImpl.
    *
    * @param  prop  - the property with namespace and key.
    * @param  value - the value of the property.
    *
    * @throws DataObjectProxyException.
    * @throws DataObjectProxyException  Description of exception
    *                                   DataObjectProxyException.
    */
   public void putMeta(NSProperty prop, String value)
      throws DataObjectProxyException {

      try {

         // Put the property on the current url
         mDSI.putMetaData(prop, value);

         // reset the dsi point to the parent url of the current one
         mDSI.setURL(DSI.splitUrl(mDSI.getURL())[0]);
         mDSI.putMetaData(prop, value);

         // set the url back to the original one
         mDSI.setURL(mURL.toString());
      } catch (StatusException se) {
         this.handleExceptions(se);
      }

   }


   /**
    * Copy the contents of the object to the destination file.
    *
    * <p>Note: colletions are not allowed</p>
    *
    * @param  destination Description of parameter localpath.
    *
    * @return add more DSI methods wrapper later.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   @Override
public File readFile(File destination) throws DataObjectProxyException {

      if (this.isCollection()) {

         /* can't download this! */
         throw new DataObjectProxyException(this.getURL() +
                                            " is a collection ");
      }

      File lf = initLocalFile(destination);
      loadFile();

      return localCopy;
   }


   /**
    * Remove a collection.
    *
    * <p><b>Warning:</b> This method is extremely dangerous to use!</p>
    *
    * @throws DataObjectProxyException
    */
   @Override
public void removeDirectory() throws DataObjectProxyException {
      this.removeResource();
   }

   /**
    * Reset WebDataObjectProxyImpl using the new url and current user and
    * password.
    *
    * @param  newURL - new url to be accessed.
    *
    * @return a new DataObjectProxy representing the new url.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   @Override
public DataObjectProxy resetDataObjectProxy(URL newURL)
      throws DataObjectProxyException {
      return resetDataObjectProxy(newURL, this.getUsername(),
                                  this.getPassword());
   }

   /**
    * <p>Reset the WebDataObjectProxyImpl to represent the new url,user and
    * password. If no user and password provided,use the current user and
    * password.</p>
    *
    * @param  newURL  - new url to be accessed.
    * @param  newUser - new user name to access the WebDataObjectProxyImpl.
    * @param  newPass - new password to access the WebDataObjectProxyImpl.
    *
    * @return a new DataObjectProxy representing the new url.
    *
    * @throws DataObjectProxyException Could not create the new proxy.
    */
   @Override
public DataObjectProxy resetDataObjectProxy(URL newURL, String newUser,
                                               String newPass)
      throws DataObjectProxyException {
      return new WebdavDataObjectProxyImpl(newURL, newUser, newPass);
   }

   /**
    * Search for metadata. Not implemented yet.
    *
    * @return Description of return value.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   @Override
public Object searchMeta() throws DataObjectProxyException {
      throw new DataObjectProxyException("Search metadata not implemented yet");
      // return null;
   }

   /**
    * Set the password to access the WebDataObjectProxyImpl.
    *
    * @param pass - password to access the server
    */
   @Override
public void setPassword(String pass) { mPassword = pass; }

   /**
    * This method should replace the old setUsername and setPassword Reason: If
    * we generate a dop without username and password after that we do
    * dop.setUserName and dop.setPassword the set will not set the credentials
    * to access DSI.
    *
    * @param user The user name to access the server.
    * @param pass password to access the server.
    */
   @Override
public void setUserInfo(String user, String pass) {
      this.mUsername = user;
      this.mPassword = pass;
      mDSI = new DSI(mURL.toString(), mUsername, mPassword);
   }

   /**
    * Set the username to access the server.
    *
    * @param user - username to access the server
    */
   @Override
public void setUsername(String user) { mUsername = user; }

   /**
    * Copy all the files of a sorue directory to a destination collections,
    * creating all the directories and files as needed.
    *
    * <p>Note: this method id called to implement downloadDir.</p>
    *
    * @param  srcdop a DataObjectProxy pointing to a local or remote collcetion
    * @param  depth  how far to traverse the sub-tree: currently, 1 or infinite.
    *
    * @throws DataObjectProxyException
    */
   @Override
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
               tempdop.close();
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

} // end class WebdavDataObjectProxyImpl
