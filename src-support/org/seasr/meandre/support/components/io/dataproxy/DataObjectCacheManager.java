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
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
import org.xml.sax.SAXException;


/**
 * The DataObjectCacheManager manages a read cache for objects from remote
 * servers.
 * <p>
 * The objects are stored in a local directory or directories (selected by calling
 * code).
 * <p>
 * The state of the cache is maintained in a table in XML. The stored table is
 * located at 'cfile'.
 * <p>
 * The Cache info is defined in the XML schema, which is defined as 'schema'
 * <p>
 * The cache state info is loaded and retrieved using JAXB.
 * <p>
 * JAXB automatically generates the classes ObjectFactory, DataObjectCacheRecord
 * and DataObjectCacheRecords from the XML schema.  Do not modify these classes
 * by hand!  If the schema changes, regenerate them using 'xjc'.
 *
 * @author  $Author: mcgrath $
 * @version $Revision: 1.3 $, $Date: 2007/05/18 17:12:23 $
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public class DataObjectCacheManager {

   //~ Instance fields *********************************************************

   /** Configuration File.* */
   private String cfile = null;

   /** A HashTable to store cached records.* */
   private final Hashtable<String, DataObjectCacheRecord> CRTable;

   /** An integer indicating adding a record.* */
   private final int OP_ADDARECORD = 1;

   /** An integer indicating removing a record.* */
   private final int OP_RMARECORD = -1;

   /** Select update. */
   private final int OP_UPDARECORD = 2;

   /** Package Name for Jaxb */
   private final String packageName = "ncsa.d2k.modules.core.io.proxy";


   /** File name of the schema.* */
   private String schema = "resources/cacheSchema.xsd";

   /**
    * An indicator for searching value in the creation time of locally cached
    * files.*
    */
   private final int TARGET_CALENDAR = 05;

   /** An indicator for searching value in ETAGS.* */
   private final int TARGET_ETAG = 06;

   /** An indicator for searching value in cached Local Files.* */
   private final int TARGET_LOCALFILE = 03;

   /** An indicator for searching value in RIDs.* */
   private final int TARGET_RID = 01;

   /** An indicator for searching value in URLs.* */
   private final int TARGET_URL = 02;

   /** An indicator for searching value in User names.* */
   private final int TARGET_USER = 04;

   private final DataObjectCachePolicy myPolicy;

	private static Logger _logger = Logger.getLogger("DataObjectCacheManager");

   //~ Constructors ************************************************************

   /**
    * Creates a new DataObjectCacheManager object.
    */
   public DataObjectCacheManager() {
	   myPolicy = new DataObjectCacheDefaultPolicy();
      CRTable = new Hashtable<String, DataObjectCacheRecord>();
   }

   /**
    * Creates a new DataObjectCacheManager object.
    *
    * @param configFile The stored cache information.
    */
   public DataObjectCacheManager(String configFile) {
	   myPolicy = new DataObjectCacheDefaultPolicy();
	      CRTable = new Hashtable<String, DataObjectCacheRecord>();
      cfile = configFile;
   }

   /**
    * Creates a new DataObjectCacheManager object.
    *
    * @param configFile The stored cache information.
    * @param schemaFile The XML schema for the cache records.
    */
   public DataObjectCacheManager(String configFile, String schemaFile) {
	   myPolicy = new DataObjectCacheDefaultPolicy();
	      schema = schemaFile;
      CRTable = new Hashtable<String, DataObjectCacheRecord>();
      cfile = configFile;
   }

   //~ Methods *****************************************************************

   /**
    * Clear all of the DataObjectCacheRecords from the hashtable.
    *
    * @throws DataObjectProxyException
    */
   private void CLRDataObjectCacheRecords() throws DataObjectProxyException {
      CRTable.clear();
   }

   /**
    * Create a DataOjectCacheRecord using the information provided by user.
    *
    * @param  id               - Record ID
    * @param  filename         - Local File Name
    * @param  url              - URL cached to the local file
    * @param  user             - who write the contents of url to the local file
    * @param  etag             Description of parameter etag.
    * @param  modificationdate - last modified date of URL
    *
    * @return a DataOjectCacheRecord
    *
    * @throws DataObjectProxyException
    */
   private DataObjectCacheRecord createADataObjectCacheRecord(String id,
                                                              String filename,
                                                              String url,
                                                              String user,
                                                              String etag,
                                                              String modificationdate)
      throws DataObjectProxyException {
      ObjectFactory objFactory = new ObjectFactory();
      DataObjectCacheRecord record = objFactory.createDataObjectCacheRecord();
      record.setID(id);
      record.setLocalFile(filename);
      record.setURL(url);
      record.setUsername(user);
      record.setRid(filename);
      record.setETag(etag);
      record.setCalendar(modificationdate);

      return record;
   }


   /**
    * Search different fields based on the second parameter, get all of the
    * matched keys in the Hashtable.
    *
    * @param  thing - String to be searched
    * @param  f     - indicator of which field (RID, URL, LocalFile, Username,
    *               Calendar or Etag) the search should be carried out
    *
    * @return search different fields based on the second parameter, get all of
    *         the matched keys in the Hashtable.
    */
   private Vector getResultKeys(String thing, int f) {
      Vector keys = new Vector();
      Enumeration en = CRTable.elements();

      String recordItem = null;

      while (en.hasMoreElements()) {
         DataObjectCacheRecord curRecord =
            (DataObjectCacheRecord) en.nextElement();

         switch (f) {

            case TARGET_RID:
               recordItem = curRecord.getRid();

               break;

            case TARGET_URL:
               recordItem = curRecord.getURL();

               break;

            case TARGET_LOCALFILE:
               recordItem = curRecord.getLocalFile();

               break;

            case TARGET_USER:
               recordItem = curRecord.getUsername();

               break;

            case TARGET_CALENDAR:
               recordItem = curRecord.getCalendar();

               break;

            case TARGET_ETAG:
               recordItem = curRecord.getETag();

               break;
         }

         if (recordItem.contains((thing))) {
            keys.add(curRecord.getRid());
         }
      } // end while

      return keys;
   } // end method getResultKeys


   /**
    * Handle DataObjectCacheManager Exceptions and throw new DataObjectProxy
    * Exceptions.
    *
    * @param  ex an Exception
    *
    * @throws DataObjectProxyException
    */

   private void handleExceptions(Exception ex) throws DataObjectProxyException {

      if (ex instanceof JAXBException) {
         throw new DataObjectProxyException("*****JAXBException " + ex);
      } else if (ex instanceof FileNotFoundException) {
         throw new DataObjectProxyException("*****FileNotFoundException " + ex);
      } else if (ex instanceof MalformedURLException) {
         throw new DataObjectProxyException("****MalformedURLException " + ex);
      } else {
         ex.printStackTrace();
         throw new DataObjectProxyException("******Unknown Exception " +
                                            ex.getLocalizedMessage());
      }
   }


   /**
    * Put a DataObjectCacheRecord List from JAXB into a hashtable.
    *
    * @param cachelist a list type of DataObjectCacheRecord (read by JAXB
    */
   private void loadTable(List<DataObjectCacheRecord> cachelist) {
      List<DataObjectCacheRecord> records = cachelist;

      for (Iterator iter = records.iterator(); iter.hasNext();) {
         DataObjectCacheRecord curRecord = (DataObjectCacheRecord) iter.next();
         CRTable.put(curRecord.getRid(), curRecord);
      }
   }


   /**
    * Print a record for debugging.
    *
    * @param record Teh record to print.
    */
   private void printDataObejctCacheRecord(DataObjectCacheRecord record) {
	   _logger.severe("***************************************");
	   _logger.severe("Record ID : " + record.getRid());
	   _logger.severe("Local File: " + record.getLocalFile());
	   _logger.severe("URL       : " + record.getURL());
	   _logger.severe("User Name : " + record.getUsername());
	   _logger.severe("Etag      : " + record.getETag());
	   _logger.severe("Created   : " + record.getCalendar());
	   _logger.severe("\n");
   }

   /**
    * Print DataObjectCacheRecord Hashtable.
    *
    * @param table a Hashtable type of DataObjectCacheRecord
    */
   private void printHashtable(Hashtable<String, DataObjectCacheRecord> table) {
      Enumeration en = table.elements();

      while (en.hasMoreElements()) {
         DataObjectCacheRecord record =
            (DataObjectCacheRecord) en.nextElement();
         this.printDataObejctCacheRecord(record);
      }
   }


   /**
    * Add a record, need input for r, RID and ADDARECORD Remove a record, need
    * input for null, RID and RMARECORD.
    * <p>
    * This method uses JAXB.
    *
    * @param  r      - DataObjectCacheRecord to be added to removed.
    * @param  xRID   - Record ID of DataObjectCacheRecode to be added or
    *                removed.
    * @param  action - An integer indicating adding or removing a record.
    *
    * @throws DataObjectProxyException
    */
   private void updateConfigFile(DataObjectCacheRecord r, String xRID,
                                 int action) throws DataObjectProxyException {
      /* Update the DataObjectCacheRecord into the xml file */

      /* two parts of this operation:
       *    1.  update the stored XML file   2.  update the in core table
       */

      try {

         /*Create unmarshaller */
    	 /*  Note:  supply the class loader to assure that JAXB is using
    	  *    the same class loader as D2K.
    	  */
         JAXBContext jc =
            JAXBContext.newInstance(packageName,
                                    this.getClass().getClassLoader());
         Unmarshaller um = jc.createUnmarshaller();

         try {
            File cf = new File(cfile);
         } catch (Exception fnf) {
            throw new DataObjectProxyException("Can't find cfile " + cfile,fnf);
         }

         /*Get records from the configuration file.*/
         JAXBElement crelem =
            (JAXBElement) um.unmarshal(new FileInputStream(cfile));
         DataObjectCacheRecords records =
            (DataObjectCacheRecords) crelem.getValue();

         /* Create the marshaller and set the output format*/
         Marshaller m = jc.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));


         SchemaFactory sf = new XMLSchemaFactory();
         Schema s = null;

         try {
            File schemaFile = new File(schema);
            s = sf.newSchema(schemaFile);
         } catch (SAXException se) {
            throw new DataObjectProxyException("Schema Not Found.");
         }

         m.setSchema(s);

         List<DataObjectCacheRecord> recordsList = records.getCacheList();
         Iterator it = recordsList.iterator();
         DataObjectCacheRecord curRecord;
         DataObjectCacheRecord matchedRecord = null;

         switch (action) {

            case OP_ADDARECORD:

               /* Check if the record to be added is already
                * in the cfile by looking up in the records with the same URL.
                *
                * If the RID, URL and Etag of the record to be added match one of
                * the records in the recordsList, we think they are exactly
                * matched. So we don't need to add it again.
                *
                * Otherwise, if only RID and URL match and Etag doesn't match, we
                * think the record need to be replaced by the record to be
                * added.
                */
               boolean ExactMatch = false;

               /*Keys of the records with the same URL as the
                * record to be added
                */
               Vector matchedKeys = this.lookupURL(r.getURL());

               Vector keysToRM = new Vector();
               int count = 0;

               while (count < matchedKeys.size()) {
                  curRecord = CRTable.get(matchedKeys.elementAt(count));

                  if (curRecord.getUsername().equals(r.getUsername())) {

                     if ((curRecord.getETag().equals(r.getETag()))) {
                        ExactMatch = true;
                     } else {

                        /*Record the outdated records*/
                        keysToRM.add(matchedKeys.elementAt(count));
                     }

                  }

                  count++;
               }

               /*If there are any outdated records, remove them from
                * the recordsList.
                */
               if (keysToRM != null && keysToRM.size() != 0) {

                  for (int i = 0; i < keysToRM.size(); i++) {
                     recordsList.remove(CRTable.get(keysToRM.elementAt(i)));
                  }
               }

               /*If there are no exactly matched record, add the record.*/
               if (!ExactMatch) {
                  recordsList.add(r);
               }

               break;


            case OP_RMARECORD:

               matchedRecord = this.CRTable.get(xRID);

               if (matchedRecord == null) {
            	   // nothind to delete--just continue
                  break; //throw new DataObjectProxyException("\nNo record match for the given RID: " +                                                    xRID);
               }

               count = 0;

               int RecordPositionToRM = -1;
               it = recordsList.iterator();

               while (it.hasNext()) {
                  DataObjectCacheRecord curr =
                     (DataObjectCacheRecord) it.next();

                  if (xRID.equals(curr.getRid())) {
                     RecordPositionToRM = count;

                     /* In our records there should be only one record whose RID
                      * will match
                      * the given one. Once we find it, no need to proceed.
                      */
                     break;
                  }

                  count++;
               }
               recordsList.remove(count);

               break;

            case OP_UPDARECORD:

               /* find the record, delete old, add the new one*/

               /*Keys of the records with the same URL as the
                * record to be added
                */
               matchedKeys = this.lookupURL(r.getURL());

               /*If there are any outdated records, remove them from
                * the recordsList.
                */
               if (matchedKeys != null && matchedKeys.size() > 0) {

                  for (int i = 0; i < matchedKeys.size(); i++) {
                     recordsList.remove(CRTable.get(matchedKeys.elementAt(i)));
                  }
               }

               recordsList.add(r);

               break;
         }
         /* rewrite the stored file to reflect the new contents. */

         m.marshal(crelem, new FileOutputStream(cfile));

         /* Update CRTable after updating Cfile
          * because we need to look up if a record exist or not in the table
          * before we remove it from the cfile.
          */
         /* Update the DataObjectCacheRecord into the hashtable*/
         switch (action) {

            case OP_ADDARECORD:
               CRTable.put(r.getRid(), r);

               break;

            case OP_RMARECORD:

               DataObjectCacheRecord record = this.CRTable.get(xRID);

               if (record == null) {
            	   // Nothing to delete--just continue
                  break; // throw new DataObjectProxyException("The record doesn't exist!");
               }

               CRTable.remove(record.getRid());

               break;

            case OP_UPDARECORD:

               DataObjectCacheRecord delrecord = this.CRTable.get(xRID);

               if (delrecord == null) {
                  throw new DataObjectProxyException("The record doesn't exist!");
               }

               CRTable.remove(delrecord.getRid());
               CRTable.put(r.getRid(), r);

               break;
         }
      } catch (Exception e) {
         this.handleExceptions(e);
         e.printStackTrace();
      }

   } // end method updateConfigFile

   /**
    * Add a DataObjectCacheRecord into the hashtable and the configuration file.
    *
    * @param  r - A dataObjectCacheRecord
    *
    * @throws DataObjectProxyException
    */
   private void AddADataObjectCacheRecord(DataObjectCacheRecord r)
      throws DataObjectProxyException {
      this.updateConfigFile(r, r.getRid(), OP_ADDARECORD);
   }

   /**
    * Clean out a cache directory.
    *
    * <p>Users the policy set to 'myPolicy'.</p>
    *
    * @param tempDataDir Description of parameter $param.name$.
    */
   public void cleanup(File tempDataDir) {
      // apply cache retention policy here e.g., delete files older than 1 day,
      // etc.

      if (tempDataDir != null) {
         File[] children = tempDataDir.listFiles();
         long now = System.currentTimeMillis();

         for (int i = 0; i < children.length; i++) {

            if (myPolicy.shouldFlush(children[i])) {
            	children[i].delete();

               try {
                  this.RMADataObjectCacheRecord(children[i].getAbsolutePath());
               } catch (DataObjectProxyException e) {
                  // use logger here
            	   String msg = "delete failed: " + children[i].getName();
            	   _logger.info((Object)msg + " " + e.getMessage());
               }
            }
         }
      }
   }


   /**
    * Description of method getCachedCopy.
    *
    * @param  url    - url whose local copy being requested  username - username
    *                to access to the server password - passward to access to
    *                the server saveto - If no locally cached copy matches the
    *                current server's version, where to save the file to be
    *                downloaded from url;
    * @param  saveto Description of parameter saveto.
    *
    * @return Description of return value.
    *
    * @throws DataObjectProxyException
    *
    * @see    getCachedCopy(String url,String username, String password,String
    *         saveto,boolean overwrite) Username in a DataObjectCacheRecord can
    *         not be blank Therefore we manually set the username to the system
    *         username and password to "" if the user doesn't provide the user
    *         name and password to access the server.
    *
    *         <p>If "saveto" exists, it will be overwritten.</p>
    */
   public File getCachedCopy(String url, String saveto)
      throws DataObjectProxyException {
      /*
       * Username in a DataObjectCacheRecord can not be blank Therefore we
       * manually set the username and password to the system username if the
       * user doesn't provide the user name and password to access the server.
       *
       * if one of username and password is blank, we will get a dop with username
       * and password not set.
       *
       * If there is no cached file, the url will be saved to "saveto". If
       * "saveto" exists, it will be overwritten. If you don't want "saveto" to
       * be overwritten, use the other getCachedCopy method and set the boolean
       * OverWrite to false;
       *
       */

      String user = System.getProperty("user.name");

      return this.getCachedCopy(url, user, "", saveto, true);
   }

   /**
    * * if there are any locally cached copies matching the server's current
    * version, return the file name of one of those.
    *
    * <p>If there is no locally cached copy matching the server's current
    * version, download one from the server and save it to where the user
    * specified.</p>
    *
    * <p>For WebDAV resources, Etags are used to compare two or more entities
    * from the same source. If a current ETag matches an earlier ETag from the
    * same resource, then the resource has the same content it did before. For a
    * given URL, check if there is a cached version is same as the server's
    * latest version of that file using ETag.</p>
    *
    * <p>For local resources, the last modified date was used as Etag for webdav
    * resources. If the last modified date is not changed, we think the file is
    * same as before.</p>
    *
    * @param  dop       Description of parameter dop.
    * @param  saveto    Description of parameter saveto.
    * @param  overwrite Description of parameter overwrite.
    *
    * @return if there are any locally cached copies matching the server's
    *         current version, return the file name of one of those.
    *
    *         <p>If there is no locally cached copy matching the server's
    *         current version, download one from the server and save it to where
    *         the user specified.</p>
    *
    *         <p>For WebDAV resources, Etags are used to compare two or more
    *         entities from the same source. If a current ETag matches an
    *         earlier ETag from the same resource, then the resource has the
    *         same content it did before. For a given URL, check if there is a
    *         cached version is same as the server's latest version of that file
    *         using ETag.</p>
    *
    *         <p>For local resources, the last modified date was used as Etag
    *         for webdav resources. If the last modified date is not changed, we
    *         think the file is same as before.</p>
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   public File getCachedCopy(DataObjectProxy dop, String saveto,
                             boolean overwrite)
      throws DataObjectProxyException {
      String localfile = null;
      File lf = null;
      String currentEtag;

      if (dop == null) {
         throw new DataObjectProxyException("Internal error: proxy not set");
      }

      currentEtag = dop.getTag();

      /* Find the keys in CRTable which have the URL equal to url*/
      Vector results = this.lookupURL(dop.getURL().toString());
      boolean notFindACopy = true;
      Vector RectoDel = new Vector();

      if (results.size() > 0) {

         /*
          * Find the first cached copy matching the server's current version if
          * there are any.
          */
         int count = 0;

         while (count < results.size() && notFindACopy) {
            DataObjectCacheRecord currRecord =
               CRTable.get(results.elementAt(count));

            if (currRecord.getETag().equals(currentEtag)) {
               localfile = CRTable.get(results.elementAt(count)).getLocalFile();
               lf = new File(localfile);

               if (!lf.exists()) {
            	  _logger.info("Data File is missing in cache "+lf.getAbsolutePath());
                  // hit but data is missing--same as miss
                  notFindACopy = true;
                  RectoDel.add(lf.getAbsolutePath());
               } else {
            	   _logger.info("Hit in cache "+lf.getAbsolutePath());
                  // cache hit
                  notFindACopy = false;
               }
            } else {
            	_logger.info("Data File is out of date in cache "+lf.getAbsolutePath());

               // hit, but out of date in the cached--flush it
               notFindACopy = true;
               RectoDel.add(currRecord.getLocalFile());
            }

            count++;
         } // end while
      } // end if

      /*
       * Flush any out of date or partial records
       */
      if (RectoDel.size() > 0) {
         Enumeration en = RectoDel.elements();

         while (en.hasMoreElements()) {
            String it = (String) en.nextElement();
            DataObjectCacheRecord recordx = this.CRTable.get(it);

            if (recordx != null) {
               File f = new File(recordx.getLocalFile());
               this.RMADataObjectCacheRecord(recordx.getRid());

               if (f.exists()) {
                  f.delete();
               }
            }
         }
      }

      /*
       * Load cache if necessary
       */
      if (notFindACopy) {

         /*
          * 1. read the file into the cache This can fail if the source has
          * moved....
          */
         if (!overwrite) {
            throw new DataObjectProxyException("File " + dop.getLocalFile() +
                                               " Exists!");
         }

         if (saveto != null) {
           // the local file name was specified
        	lf = new File(saveto);
        	_logger.info("Load "+dop.getURL()+" into "+saveto);
        	dop.readFile(lf);

         } else {
        	 _logger.info("Load "+dop.getURL()+" into temp");
            // create temporary name for local copy
        	 lf = dop.readFile(null);

         }

         // ASSERT: the file is loaded in the cache, 'lf' is open

         /*
          * 2. create a cache record for the new file, update the cache manager
          */
         DataObjectCacheRecord record =
            this.createADataObjectCacheRecord(lf.getName(),
                                              lf.getAbsolutePath(),
                                              dop.getURL().toString(),
                                              dop.getUsername(), dop.getTag(),
                                              dop.getURLLastModified());

         this.AddADataObjectCacheRecord(record);
      } // end if

      // ASSERT: the cache manager has a correct record for the local object

      /*
       * Touch the local copy to mark the last usage
       */
      lf.setLastModified(System.currentTimeMillis());

      return lf;
   } // end method getCachedCopy

   /**
    * Description of method getCachedCopy.
    *
    * @param  url       - url whose local copy being requested  username -
    *                   username to access to the server password - passward to
    *                   access to the server saveto - If no locally cached copy
    *                   matches the current server's version, where to save the
    *                   file to be downloaded from url; overwrite - Specify
    *                   whether or not saveto should be overwritten if it
    *                   already exists.
    * @param  username  Description of parameter username.
    * @param  password  Description of parameter password.
    * @param  saveto    Description of parameter saveto.
    * @param  overwrite Description of parameter overwrite.
    *
    * @return Description of return value.
    *
    * @throws DataObjectProxyException
    */


   public File getCachedCopy(String url, String username, String password,
                             String saveto, boolean overwrite)
      throws DataObjectProxyException {

      DataObjectProxy dop = null;

      try {
         dop =
            DataObjectProxyFactory.getDataObjectProxy(new URL(url), username,
                                                      password);
      } catch (MalformedURLException e) {
         this.handleExceptions(e);
      }

      return getCachedCopy(dop, saveto, overwrite);
   }

   /**
    * Get the cache infor.
    *
    * @return The name of the cache infor file.
    */
   public String getConfigFile() { return cfile; }

   /**
    * Get the XML schema for the cache info..
    *
    * @return The name of the schema.
    */
   public String getSchemaFile() { return schema; }

   /**
    * Look up the given String in Calendar.
    *
    * @param  cal - calendar in String format
    *
    * @return a Vector contains all of the keys whose calendar matched the given
    *         parameter
    *
    *         <p>Notes</p>
    *
    *         <p>The calendar to be searched should be in the following format:
    *         2006-06-29T17:49:11Z</p>
    *
    *         <p>If you want to find the records whose calendar only match
    *         year,month and day, put in a string in format "2006-06-29"</p>
    */
   private Vector lookupCalendar(String cal) {
      Vector resultKeys = this.getResultKeys(cal, TARGET_CALENDAR);

      return resultKeys;
   }

   /**
    * Look up if Etag of the cached copies matches the given ETag.
    *
    * @param  etag - A tag which identify if the content of url has been
    *              modified For Webdav, etag is the etag specified by http For
    *              local file, the lastmodified date is used as the etag
    *
    * @return a Vector contains all of the keys whose Etag matched the given
    *         parameter*
    */
   private Vector lookupEtag(String etag) {
      Vector resultKeys = this.getResultKeys(etag, TARGET_ETAG);

      return resultKeys;
   }


   /**
    * Look up if the file name of any cached local files matched the given
    * localfile.
    *
    * @param  localfile - local file name to be searched
    *
    * @return a Vector contains all of the keys whose local file name matched
    *         the given parameter*
    */
   private Vector lookupLocalFile(String localfile) {
      Vector resultKeys = this.getResultKeys(localfile, TARGET_LOCALFILE);

      return resultKeys;
   }

   /**
    * Look up th given String in RIDs.
    *
    * @param  rid - Record ID to be searched
    *
    * @return a Vector contains all of the keys whose RID matched the given
    *         parameter
    */
   private Vector lookupRID(String rid) {
      Vector resultKeys = this.getResultKeys(rid, TARGET_RID);

      return resultKeys;
   }

   /**
    * Look up th given String in URLs.
    *
    * @param  url - url as a String to be searched
    *
    * @return a Vector contains all of the keys in the Hashtable whose URL
    *         matched the given parameter
    */
   private Vector lookupURL(String url) {
      Vector resultKeys = this.getResultKeys(url, TARGET_URL);

      return resultKeys;
   }

   /**
    * Look up th given String in user names.
    *
    * @param  user - user name to be searched
    *
    * @return a Vector contains all of the keys whose user name matched the
    *         given parameter
    */
   private Vector lookupUSER(String user) {
      Vector resultKeys = this.getResultKeys(user, TARGET_USER);

      return resultKeys;
   }

   /**
    * Read the configuration file and put all of the records in the
    * configuration into a hashtable.
    * <p>
    * This method uses JAXB.
    *
    * @throws DataObjectProxyException
    */
   public void initCacheState() throws DataObjectProxyException {

      if (cfile == null) {
         throw new DataObjectProxyException("Configuration File Not Set!");
      }

      try {
    	 /* Note:
    	  *   Supply the class loader to assure that JAXB is using the correct
    	  *   environment.
    	  */
         JAXBContext jc =
            JAXBContext.newInstance(packageName,
                                    this.getClass().getClassLoader());
         Unmarshaller um = jc.createUnmarshaller();
         SchemaFactory sf = new XMLSchemaFactory();
         Schema s = null;

         try {
            s = sf.newSchema(new File(schema));
         } catch (SAXException se) {
            throw new DataObjectProxyException("Schema Not Found.");
         }

         um.setSchema(s);

         JAXBElement jelement =
            (JAXBElement) um.unmarshal(new FileInputStream(cfile));

         DataObjectCacheRecords records =
            (DataObjectCacheRecords) jelement.getValue();

         List<DataObjectCacheRecord> recordList = records.getCacheList();

         /**Put the configuration info into a hash table**/
         loadTable(recordList);

      } catch (JAXBException je) {
         throw new DataObjectProxyException("JAXBException", je);
      } catch (FileNotFoundException fe) {
         throw new DataObjectProxyException("FileNotFoundException", fe);
      }
   } // end method readConfigFile

   /**
    * Remove a DataObjectCacheRecord from the hashtable and the configuration
    * file.
    *
    * @param  xRID - A dataObjectCacheRecord
    *
    * @throws DataObjectProxyException
    */

   private void RMADataObjectCacheRecord(String xRID)
      throws DataObjectProxyException {
      this.updateConfigFile(null, xRID, OP_RMARECORD);
   }

   /**
    * Set the name of the cache info file.
    *
    * @param configFile The name oft he cache info file.
    */
   public void setConfigFile(String configFile) { cfile = configFile; }

   /**
    * Set the XML schema file.
    *
    * @param schemaFile The name of the XML schema file.
    */
   public void setSchemaFile(String schemaFile) { schema = schemaFile; }

   /**
    * Update a record: delete the old, add the new.
    * <p>
    * Note: this method will throw an exception if the record does not already
    * exist.
    *
    * @param  r The record to add.
    *
    * @throws DataObjectProxyException Description of exception
    *                                  DataObjectProxyException.
    */
   private void UpdateADataObjectCacheRecord(DataObjectCacheRecord r)
      throws DataObjectProxyException {
      this.updateConfigFile(r, r.getRid(), OP_UPDARECORD);
   }

} // end class DataObjectCacheManager
