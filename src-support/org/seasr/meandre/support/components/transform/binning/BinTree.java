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

package org.seasr.meandre.support.components.transform.binning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;


/**
 * BinTree is a data structure that allows for the efficient classification and
 * counting of items. This is a three-level tree. The first level is indexed by
 * class name. The second level is indexed by attribute name. The third level
 * contains bins---these bins have conditions, and if an item satisfies these
 * conditions, that bin's tally will be incremented. If an item does not fit
 * into any bins, a default tally is incremented. The number of unknown class
 * names and attribute names are both kept as well.
 *
 * @author  $Author: mcgrath $
 * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
 */
public class BinTree extends HashMap implements Serializable, Cloneable {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = -6170858308587899888L;

   /** constant for = */
   static public final String EQUAL_TO = "=";

   /** constant for < */
   static public final String LESS_THAN = "<";

   /** constant for > */
   static public final String GREATER_THAN = ">";

   /** constant for <=. */
   static public final String LESS_THAN_EQUAL_TO = "<=";

   /** constant for >= */
   static public final String GREATER_THAN_EQUAL_TO = ">=";

   /** constant for missing */
   static public final String MISSING = "missing";

   /** constant for && */
   static private final String AND = "&&";

   /** constant for || */
   static private final String OR = "||";

   /** constant for ( */
   static private final char open = '(';

   /** constant for ) */
   static private final char close = ')';

   /** constant for empty string */
   static private final String EMPTY = "";

   /** constant for space character */
   static private final String SPACE = " ";

   private static Logger _logger = Logger.getLogger("BinTree");

   //~ Instance fields *********************************************************

   /**
    * A quick lookup table for attributes, just contains each item in
    * attributeNames.
    */
   private HashMap attributeList;

   /** The attribute names. */
   private String[] attributeNames;

   /** The class names. */
   private String[] classNames;

   /**
    * the class totals are incremented each time classify() is called. Since it
    * is called once for each attribute, we must divide the total by the number
    * of attributes to get the real class total
    */
   private HashMap classTotals;

   /** A lookup table for DefaultTrees. */
   private HashMap defaultTree;

   /**
    * the total classified is incremented each time classify() is called. since
    * it is called once for each attribute, it must be divided by the number of
    * attributes to get the real totalClassified
    */
   private int totalClassified;

   /** The number of unknown classes. */
   private int unknownClasses;

   //~ Constructors ************************************************************

   /**
    * Creates a new BinTree object.
    */
   public BinTree() { }

   /**
    * Create a new BinTree by passing in class and attribute names.
    *
    * @param cn the class names
    * @param an the attribute names
    */
   public BinTree(String[] cn, String[] an) {
      super(cn.length);
      classNames = cn;
      attributeNames = an;

      defaultTree = new HashMap(classNames.length);
      classTotals = new HashMap(classNames.length);

      for (int i = 0; i < classNames.length; i++) {
         put(classNames[i], new ClassTree(attributeNames));
         defaultTree.put(cn[i], new DefaultTree(attributeNames));
         classTotals.put(cn[i], new Integer(0));
      }

      unknownClasses = 0;

      attributeList = new HashMap(attributeNames.length);

      for (int i = 0; i < attributeNames.length; i++) {
         attributeList.put(attributeNames[i], attributeNames[i]);
      }

      totalClassified = 0;
   }

   //~ Methods *****************************************************************

   /**
    * < Add bins from an equation typed in by the user.
    *
    * @param an  the attribute name
    * @param bn  the name to give the new bin
    * @param eqn the equation itself
    * @param num true if the bin will be numeric, false if it will contain text
    *
    * @throws DuplicateBinNameException  when a bin with this name exists
    * @throws MalformedEquationException when the equation cannot be parsed
    * @throws AttributeNotFoundException when the attribute does not exist
    */
   public void addBinFromEquation(String an, String bn, String eqn, boolean num)
      throws DuplicateBinNameException, MalformedEquationException,
             AttributeNotFoundException {

      Iterator i = values().iterator();

      while (i.hasNext()) {
         ClassTree ct = (ClassTree) i.next();

         if (ct != null) {
            ct.addBinFromEquation(an, bn, eqn, num);
         }
      }
   }

   /**
    * Add a default bin for a class and attribute pair.
    *
    * @param cn class name
    * @param an attribute name
    */
   public void addDefault(String cn, String an) {
      DefaultTree dt = (DefaultTree) defaultTree.get(cn);

      if (dt == null) {
         return;
      }

      dt.addDefault(an);
   }

   /**
    * Add a bin for missing values
    *
    * @param  cn class name
    * @param  an attribute name
    *
    * @throws DuplicateBinNameException  when a bin already exists with this name
    * @throws AttributeNotFoundException when attribute is not found
    */
   public void addMissingValuesBin(String cn, String an)
      throws DuplicateBinNameException, AttributeNotFoundException {

      ClassTree ct = (ClassTree) get(cn);
      ct.addStringBin(an, MISSING, " ");

   }

   /**
    * Add a numeric bin.
    *
    * @param an   the attribute name
    * @param bn   the name to give the bin
    * @param op   the operator to use in the bin
    * @param item the number that goes in the bin
    *
    * @throws DuplicateBinNameException  when a bin with this name exists
    * @throws AttributeNotFoundException when the attribute does not exist
    */
   public void addNumericBin(String an, String bn, String op, double item)
      throws DuplicateBinNameException, AttributeNotFoundException {

      Iterator i = values().iterator();

      while (i.hasNext()) {
         ClassTree ct = (ClassTree) i.next();

         if (ct != null) {
            ct.addNumericBin(an, bn, op, item);
         }
      }
   }

   /**
    * Add a numeric bin
    *
    * @param  an        attribute name
    * @param  bn        bin name
    * @param  lower     low bound
    * @param  includeLo true if low bound includes lower
    * @param  upper     high bound
    * @param  includeHi true if high bound includes upper
    *
    * @throws DuplicateBinNameException  when a bin with this name exists
    * @throws AttributeNotFoundException when attribute is not found
    */
   public void addNumericBin(String an, String bn, double lower,
                             boolean includeLo,
                             double upper, boolean includeHi)
      throws DuplicateBinNameException, AttributeNotFoundException {

      Iterator i = values().iterator();

      while (i.hasNext()) {
         ClassTree ct = (ClassTree) i.next();

         if (ct != null) {
            ct.addNumericBin(an, bn, lower, includeLo, upper, includeHi);
         }
      }
   }

   /**
    * Add a StringBin. These currently only support equality between Strings.
    *
    * @param an   the attribute name
    * @param bn   the name to give the new bin
    * @param item the item to put in the bin
    *
    * @throws DuplicateBinNameException  when a bin with this name exists
    * @throws AttributeNotFoundException when the attribute does not exist
    */
   public void addStringBin(String an, String bn, String item)
      throws DuplicateBinNameException, AttributeNotFoundException {

      Iterator i = values().iterator();

      while (i.hasNext()) {
         ClassTree ct = (ClassTree) i.next();

         if (ct != null) {
            ct.addStringBin(an, bn, item);
         }
      }
   }

   /**
    * Add a bin for string values
    *
    * @param  an    attribute name
    * @param  bn    bin name
    * @param  items the strings that fall in this bin
    *
    * @throws DuplicateBinNameException  when a bin exists with this name
    * @throws AttributeNotFoundException when attribute is not found
    */
   public void addStringBin(String an, String bn, String[] items)
      throws DuplicateBinNameException, AttributeNotFoundException {

      Iterator i = values().iterator();

      while (i.hasNext()) {
         ClassTree ct = (ClassTree) i.next();

         if (ct != null) {
            ct.addStringBin(an, bn, items);
         }
      }
   }

   /**
    * Classify a double value by looking up the class and attribute and finding
    * an appropriate bin. If no bin is found, the default tally for this
    * class-attribute combination is incremented.
    *
    * @param cn    the class name
    * @param an    the attribute name
    * @param value the item to find a bin for
    */
   public void classify(String cn, String an, double value) {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         unknownClasses++;

         return;
      }

      try {

         if (!ct.classify(an, value)) {
            addDefault(cn, an);
         } else {

            // System.out.println("classified : cn an value " + cn + " " + an +
            // " " + value );
            totalClassified++;

            Integer t = (Integer) classTotals.get(cn);
            classTotals.put(cn, new Integer(t.intValue() + 1));
         }
      } catch (AttributeNotFoundException e) { }
   }

   /**
    * Classify a String value by looking up the class and attribute and finding
    * an appropriate bin. If no bin is found, the default tally for this
    * class-attribute combination is incremented.
    *
    * @param cn    the class name
    * @param an    the attribute name
    * @param value the item to find a bin for
    */
   public void classify(String cn, String an, String value) {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         unknownClasses++;

         return;
      }

      try {

         if (!ct.classify(an, value)) {
            addDefault(cn, an);
         } else {
            totalClassified++;

            Integer t = (Integer) classTotals.get(cn);
            classTotals.put(cn, new Integer(t.intValue() + 1));
         }
      } catch (AttributeNotFoundException e) { }
   }

    /**
     * Compares the specified object with this map for equality.  Returns
     * <tt>true</tt> if the given object is also a map and the two maps
     * represent the same mappings.  More formally, two maps <tt>t1</tt> and
     * <tt>t2</tt> represent the same mappings if
     * <tt>t1.keySet().equals(t2.keySet())</tt> and for every key <tt>k</tt>
     * in <tt>t1.keySet()</tt>, <tt> (t1.get(k)==null ? t2.get(k)==null :
     * t1.get(k).equals(t2.get(k))) </tt>.  This ensures that the
     * <tt>equals</tt> method works properly across different implementations
     * of the map interface.<p>
     * <p/>
     * This implementation first checks if the specified object is this map;
     * if so it returns <tt>true</tt>.  Then, it checks if the specified
     * object is a map whose size is identical to the size of this set; if
     * not, it it returns <tt>false</tt>.  If so, it iterates over this map's
     * <tt>entrySet</tt> collection, and checks that the specified map
     * contains each mapping that this map contains.  If the specified map
     * fails to contain such a mapping, <tt>false</tt> is returned.  If the
     * iteration completes, <tt>true</tt> is returned.
     *
     * @param obj object to be compared for equality with this map.
     * @return <tt>true</tt> if the specified object is equal to this map.
     */
    public boolean equals(Object obj) {

        if (obj instanceof BinTree) {
            return equals((BinTree) obj);
        } else {
        	_logger.severe("the second object is not of type BinTree");

            return false;

        }
    }


   /**
    * Return true if bt is equal to this
    *
    * @param  bt a bin tree
    *
    * @return true iff bt is equal to this
    */
   public boolean equals(BinTree bt) {

      boolean retVal = true;

      if (!attributeList.equals(bt.attributeList)) {
         retVal = false;
         _logger.severe("attribute List are not equal!");
      }

      Arrays.sort(attributeNames);
      Arrays.sort(bt.attributeNames);

      if (!Arrays.equals(attributeNames, bt.attributeNames)) {
         retVal = false;
         _logger.severe("attribute Names are not equal!");
      }

      Arrays.sort(classNames);
      Arrays.sort(bt.classNames);

      if (!Arrays.equals(classNames, bt.classNames)) {
         retVal = false;
         _logger.severe("class Names are not equal!");
      }

      if (!classTotals.equals(bt.classTotals)) {
         retVal = false;
         _logger.severe("class totals are not equal!");
      }

      if (!defaultTree.equals(bt.defaultTree)) {
         retVal = false;
         _logger.severe("default trees are not equal!");
      }

      if (this.totalClassified != bt.totalClassified) {
         retVal = false;
         _logger.severe("total classified are not equal!");
      }

      if (this.unknownClasses != bt.unknownClasses) {
         retVal = false;
         _logger.severe("unknown classes are not equal!");
      }


      return retVal;
   } // end method equals

   /**
    * Get the names of the attributes.
    *
    * @return the names of the attributes.
    */
   public String[] getAttributeNames() { return attributeNames; }

   /**
    * Find the bin that value belongs in, knowing only the attribute name. This
    * will probe all bins until a suitable one is found.
    *
    * @param  an    the attribute name
    * @param  value the value
    *
    * @return the name of the bin that value belongs in
    */
   public String getBinNameForValue(String an, double value) {
      Iterator i = keySet().iterator();

      while (i.hasNext()) {
         String cl = (String) i.next();
         ClassTree ct = (ClassTree) get(cl);

         if (ct == null) {
            return null;
         }

         String retVal = ct.getBinNameForValue(an, value);

         if (retVal != null) {
            return retVal;
         }
      }

      return null;
   }

   /**
    * Find the bin that value belongs in, knowing only the attribute name. This
    * will probe all bins until a suitable one is found.
    *
    * @param  an    the attribute name
    * @param  value the value
    *
    * @return the name of the bin that value belongs in
    */
   public String getBinNameForValue(String an, String value) {
      Iterator i = keySet().iterator();

      while (i.hasNext()) {
         String cl = (String) i.next();
         ClassTree ct = (ClassTree) get(cl);

         if (ct == null) {
            return null;
         }

         String retVal = ct.getBinNameForValue(an, value);

         if (retVal != null) {
            return retVal;
         }
      }

      return null;
   }

   /**
    * Given a class name, attribute name, and a value, find the bin that this
    * value falls in.
    *
    * @param  cn    the class name
    * @param  an    the attribute name
    * @param  value the value
    *
    * @return the name of the bin value belongs in
    */
   public String getBinNameForValue(String cn, String an, double value) {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         return null;
      }

      return ct.getBinNameForValue(an, value);
   }

   /**
    * Given a class name, attribute name, and a value, find the bin that this
    * value falls in.
    *
    * @param  cn    the class name
    * @param  an    the attribute name
    * @param  value the value
    *
    * @return the name of the bin value belongs in
    */
   public String getBinNameForValue(String cn, String an, String value) {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         return null;
      }

      return ct.getBinNameForValue(an, value);
   }

   /**
    * Get the names of the bins for an attribute.
    *
    * @param  an the attribute name
    *
    * @return the names of the bins for this attribute
    */
   public String[] getBinNames(String an) {
      ClassTree ct = (ClassTree) get(classNames[0]);

      if (ct == null) {
         return null;
      }

      return ct.getBinNames(an);
   }

   /**
    * Get the names of the bins for a specific class and attribute.
    *
    * @param  cn the class name
    * @param  an the attribute name
    *
    * @return the names of the bins for this class-attribute pair
    */
   public String[] getBinNames(String cn, String an) {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         return null;
      }

      return ct.getBinNames(an);
   }

   /**
    * Get the names of the classes.
    *
    * @return the names of the classes
    */
   public String[] getClassNames() { return classNames; }

   /**
    * Get the total number of items in a certain class.
    *
    * @param  cn the class of interest
    *
    * @return the number of items in class cn
    */
   public int getClassTotal(String cn) {
      Integer i = (Integer) classTotals.get(cn);

      if (i == null) {
         return 0;
      }

      int tot = i.intValue();

      return (int) Math.ceil((tot / attributeNames.length));
   }

   /**
    * Get the lower bound of a bin
    *
    * @param  cn class name
    * @param  an attribute name
    * @param  bn bin name
    *
    * @return lower bound for the bin
    *
    * @throws Exception when the bin could not be found
    */
   public double getLowerBound(String cn, String an, String bn)
      throws Exception {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         throw new Exception("The bin could not be found.");
      }

      return ct.getLowerBound(an, bn);
   }

   /**
    * Get the number of default items. Default items are items that have been
    * attempted to be classified, but do not fit into any of the bins.
    *
    * @param  cn the class name
    * @param  an the attribute name
    *
    * @return the number of default items
    */
   public int getNumDefault(String cn, String an) {
      DefaultTree dt = (DefaultTree) defaultTree.get(cn);

      if (dt == null) {
         return -1;
      }

      return dt.getNumDefault(an);
   }

   /**
    * Get the number of unknown attributes for a class.
    *
    * @param  cn the class name
    *
    * @return the number of unknown attributes for this class
    */
   public int getNumUnknownAttributes(String cn) {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         return -1;
      }

      return ct.getNumUnknownAttributes();
   }

   /**
    * Get the number of unknown classes.
    *
    * @return the number of unknown classes
    */
   public int getNumUnknownClasses() { return unknownClasses; }

   /**
    * Get the tally for a specific bin. Returns -1 if it was not found.
    *
    * @param  cn the class name
    * @param  an the attribute name
    * @param  bn the bin name
    *
    * @return the tally in the specified bin
    */
   public int getTally(String cn, String an, String bn) {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         return -1;
      }

      return ct.getTally(an, bn);
   }

   /**
    * Get the total for a specific class and attribute. Returns -1 if it was not
    * found.
    *
    * @param  cn the class name
    * @param  an the attribute name
    *
    * @return the total number of items that are of this class and attribute
    */
   public int getTotal(String cn, String an) {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         return -1;
      }

      return ct.getTotal(an);
   }

   /**
    * Get the total number of items classified.
    *
    * @return the total number of items classified
    */
   public int getTotalClassified() {
      return (int) (totalClassified / attributeNames.length);
   }

   /**
    * Get the upper bound of bin
    *
    * @param  cn class name
    * @param  an attribute name
    * @param  bn bin name
    *
    * @return the upper bound for the bin
    *
    * @throws Exception when the bin could not be found
    */
   public double getUpperBound(String cn, String an, String bn)
      throws Exception {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         throw new Exception("The bin could not be found.");
      }

      return ct.getUpperBound(an, bn);
   }


   // ADDED DC 3.12.2003

   /**
    * Return true if the lower bound of the specified bin is included, false
    * otherwise.
    *
    * @param  cn class name
    * @param  an attribute name
    * @param  bn bin name
    *
    * @return return true if the lower bound of the specified bin is included,
    *         false otherwise.
    *
    * @throws Exception when the bin could not be found
    */
   public boolean includeLowerBound(String cn, String an, String bn)
      throws Exception {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         throw new Exception("The bin could not be found.");
      }

      return ct.includeLowerBound(an, bn);
   }

   /**
    * Return true if the bin includes the upper bound
    *
    * @param  cn class name
    * @param  an attribute name
    * @param  bn bin name
    *
    * @return true if the bin includes the upper bound
    *
    * @throws Exception when the bin could not be found
    */
   public boolean includeUpperBound(String cn, String an, String bn)
      throws Exception {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         throw new Exception("The bin could not be found.");
      }

      return ct.includeUpperBound(an, bn);
   }

   /**
    * Print the contents
    */
   public void printAll() {
	   _logger.severe("UNKNOWN CLASSES: " + unknownClasses);

      Iterator i = keySet().iterator();

      while (i.hasNext()) {
         String key = (String) i.next();
         _logger.severe("CLASS: " + key);

         ClassTree cb = (ClassTree) get(key);
         _logger.severe("UNKNOWN ATTR: " + getNumUnknownAttributes(key));
         cb.printAll();
      }

      _logger.severe("CLASS TOTALS");
      i = classTotals.keySet().iterator();

      while (i.hasNext()) {
         String key = (String) i.next();
         _logger.severe("CLASS: " + key);
         _logger.severe(""+getClassTotal(key));
      }

      _logger.severe("TOTAL CLASSIFIED: " + getTotalClassified());
   }

   /**
    * Remove a bin.
    *
    * @param  an the attribute name
    * @param  bn the bin name
    *
    * @throws AttributeNotFoundException when attribute was not found
    * @throws BinNotFoundException       when bin was not found
    */
   public void removeBin(String an, String bn)
      throws AttributeNotFoundException, BinNotFoundException {

      Iterator i = values().iterator();

      while (i.hasNext()) {
         ClassTree ct = (ClassTree) i.next();

         if (ct != null) {
            ct.removeBin(an, bn);
         }
      }
   }


   /**
    * Set the tally for a bin
    *
    * @param  cn    class name
    * @param  an    attribute name
    * @param  bn    bin name
    * @param  tally tally
    *
    * @throws Exception when the bin could not be found
    */
   public void setBinTally(String cn, String an, String bn, int tally)
      throws Exception {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         throw new Exception("The bin could not be found.");
      }

      ct.setBinTally(an, bn, tally);
   }

   /**
    * Set the total number of items in a certain class.
    *
    * @param cn    the class of interest
    * @param total the number of items in class cn
    */
   public void setClassTotal(String cn, int total) {
      classTotals.put(cn, new Integer(total));
   }

   /**
    * Set the total for a class-attribute combination
    *
    * @param cn    class name
    * @param an    attribute name
    * @param total new total
    */
   public void setTotal(String cn, String an, int total) {
      ClassTree ct = (ClassTree) get(cn);

      if (ct == null) {
         ;
      }

      ct.setTotal(an, total);
   }

   /**
    * Set the total number of items classified.
    *
    * @param t total number of items classified
    */
   public void setTotalClassified(int t) { totalClassified = t; }

   //~ Inner Classes ***********************************************************

    /**
     * Tree to hold default values that do not fall in any other bin.
     */
   private class DefaultTree extends HashMap implements Serializable {

      /** Use serialVersionUID for interoperability. */
      static private final long serialVersionUID = -3313456949363139481L;

        /**
         * Constructor
         */
      DefaultTree() { }

        /**
         * Constructor
         * @param an attribute names
         */
      DefaultTree(String[] an) {
         super(an.length);

         for (int i = 0; i < an.length; i++) {
            put(an[i], new Integer(0));
         }
      }

        /**
         * Add a default for an
         * @param an attribute name
         */
      void addDefault(String an) {
         Integer i = (Integer) get(an);
         int num = i.intValue();
         num++;
         put(an, new Integer(num));
      }

        /**
         * Get the number of default values for the attribute
         * @param an attribute name
         * @return number of default for this attribute
         */
      int getNumDefault(String an) {
         Integer i = (Integer) get(an);

         if (i == null) {
            return -1;
         } else {
            return i.intValue();
         }
      }
   } // end class DefaultTree

    /**
     * thrown when an attribute is not found
     */
   public class AttributeNotFoundException extends Exception { }

    /**
     * thrown when a bin is not found
     */
   public class BinNotFoundException extends Exception { }

   /**
    * Each class has a class tree
    *
    * @author  $Author: mcgrath $
    * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
    */
   public class ClassTree extends HashMap implements Serializable {

      /** Use serialVersionUID for interoperability. */
      static private final long serialVersionUID = -5844274250647344072L;

      private String[] operators =
      {
         LESS_THAN_EQUAL_TO, "=<",
         GREATER_THAN_EQUAL_TO, "=>",
         "==", EQUAL_TO, LESS_THAN, GREATER_THAN
      };
      private int unknownAttributes;

       /**
        * Constructor
        */
      ClassTree() { }

       /**
        * Constructor
        * @param an attribute names
        */
      ClassTree(String[] an) {
         super(an.length);

         for (int i = 0; i < an.length; i++) {
            put(an[i], new BinList());
         }

         unknownAttributes = 0;
      }

       /**
        * break s up into 3 parts
        * @param s string
        * @param loc
        * @param len
        * @return
        */
      private String[] breakUp(String s, int loc, int len) {
         String[] retVal = new String[3];
         retVal[0] = s.substring(0, loc).trim();
         retVal[1] = s.substring(loc, loc + len).trim();
         retVal[2] = s.substring(loc + len).trim();

         return retVal;
      }

       /**
        * count the number of occurences of c in s
        * @param s string
        * @param c character
        * @return the number of occurences of c in s
        */
      private int countCharacter(String s, char c) {
         char[] str = s.toCharArray();
         int retVal = 0;

         for (int i = 0; i < str.length; i++) {

            if (str[i] == c) {
               retVal++;
            }
         }

         return retVal;
      }

       /**
        *
        * @param s
        * @return
        * @throws MalformedEquationException
        */
      private PhraseList getPhraseList(String s)
         throws MalformedEquationException {

         // System.out.println("getPhraseList: "+s);
         if ((s.indexOf(AND) == -1) && (s.indexOf(OR) == -1)) {
            return null;
         }

         int numOpen = countCharacter(s, open);
         int numClose = countCharacter(s, close);

         if (numOpen != numClose) {
            throw new MalformedEquationException();
         }

         // loop through string
         // when #open == #close, break it up
         char[] str = s.toCharArray();
         numOpen = 0;
         numClose = 0;

         for (int i = 0; i < str.length; i++) {

            if (str[i] == open) {
               numOpen++;
            } else if (str[i] == close) {
               numClose++;
            }

            if ((numOpen == numClose) && (i != 0)) {
               String s1 = s.substring(1, i);

               int j = i + 1;

               for (; j < str.length; j++) {

                  if (str[j] == open) {
                     break;
                  }
               }

               String con = s.substring(i + 1, j).trim();
               String s2 = s.substring(j + 1, str.length - 1);
               // System.out.println("1: "+s1);
               // System.out.println("2: "+s2);
               // System.out.println("C: "+con);

               return new PhraseList(s1, con, s2);
            }
         } // end for

         return null;
      } // end method getPhraseList

       /**
        * Print the contents for debugging
        */
      private void printAll() {
         Iterator i = keySet().iterator();

         while (i.hasNext()) {
            String key = (String) i.next();
            _logger.severe("ATTR: " + key);

            BinList bl = (BinList) get(key);
            Iterator it = bl.keySet().iterator();

            while (it.hasNext()) {
               String bname = (String) it.next();
               _logger.severe("BIN: " + bname);

               Bin b = (Bin) bl.get(bname);
               _logger.severe(" COUNT: " + b.tally);
            }
         }
      }

       /**
        * Add a bin from an equation
        * @param an attribute name
        * @param bn bin name
        * @param eq equation
        * @param nu true if numeric bin
        * @throws MalformedEquationException when equation cannot be parsed
        * @throws DuplicateBinNameException  when bin already exists with this name
        * @throws AttributeNotFoundException when attribute is not found
        */
      void addBinFromEquation(String an, String bn, String eq, boolean nu)
         throws MalformedEquationException, DuplicateBinNameException,
                AttributeNotFoundException {

         BinList bl = (BinList) get(an);

         if (bl == null) {
            throw new AttributeNotFoundException();
         }

         if (bl.containsKey(bn)) {
            throw new DuplicateBinNameException();
         }

         bl.put(bn, new Bin(parse(eq, nu)));
      }

       /**
        * Add a numeric bin
         * @param an  attribute name
        * @param bn  bin name
        * @param op  operation
        * @param item
        * @throws DuplicateBinNameException  when bin already exists with this name
        * @throws AttributeNotFoundException when attribute not found
        */
      void addNumericBin(String an, String bn, String op, double item)
         throws DuplicateBinNameException, AttributeNotFoundException {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            throw new AttributeNotFoundException();
         }

         if (bl.containsKey(bn)) {
            throw new DuplicateBinNameException();
         }

         bl.put(bn, new Bin(new NumericEvaluate(op, item)));
      }

       /**
        * Add a numeric bin
        * @param an attribute name
        * @param bn bin name
        * @param lower lower bound
        * @param includeLo true if inclusive of lower bound
        * @param upper upper bound
        * @param includeHi true if inclusive of upper bound
        * @throws DuplicateBinNameException when bin already exists with this name
        * @throws AttributeNotFoundException when attribute not found
        */
      void addNumericBin(String an, String bn, double lower, boolean includeLo,
                         double upper, boolean includeHi)
         throws DuplicateBinNameException, AttributeNotFoundException {

         BinList bl = (BinList) get(an);

         if (bl == null) {
            throw new AttributeNotFoundException();
         }

         if (bl.containsKey(bn)) {
            throw new DuplicateBinNameException();
         }

         bl.put(bn,
                new Bin(new BoundedNumericEvaluate(lower, includeLo,
                                                   upper, includeHi)));
      }

       /**
        * Add a string bin
        * @param an attribute name
        * @param bn bin name
        * @param item string value for this bin
        * @throws DuplicateBinNameException when bin already exists with this name
        * @throws AttributeNotFoundException when attribute is not found
        */
      void addStringBin(String an, String bn, String item)
         throws DuplicateBinNameException, AttributeNotFoundException {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            throw new AttributeNotFoundException();
         }

         if (bl.containsKey(bn)) {
            throw new DuplicateBinNameException();
         }

         bl.put(bn, new Bin(new StringEvaluate(item)));

         // System.out.println("addStringBin called");
      }

       /**
        * Add a string bin
        * @param an attribute name
        * @param bn bin name
        * @param items string values for this bin
        * @throws DuplicateBinNameException when bin alredy exists with this name
        * @throws AttributeNotFoundException when attribute is not found
        */
      void addStringBin(String an, String bn, String[] items)
         throws DuplicateBinNameException, AttributeNotFoundException {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            throw new AttributeNotFoundException();
         }

         if (bl.containsKey(bn)) {
            throw new DuplicateBinNameException();
         }

         bl.put(bn, new Bin(new MultiStringEvaluate(items)));
         // System.out.println("addMultiStringBin called for binName " +bn + "
         // for attribute  " + an + " with items " + items[0]  + " "  + items[1]);
      }

      /**
       * Find a suitable bin for a double item.
       *
       * @param  an  attribute name
       * @param  val value
       *
       * @return a suitable bin for a double item.
       *
       * @throws AttributeNotFoundException when attribute is not found
       */
      boolean classify(String an, double val)
         throws AttributeNotFoundException {
         BinList bl = (BinList) get(an);

         if (bl == null) {

            // this is an unknown attribute.
            // we cannot classify it further
            unknownAttributes++;
            throw new AttributeNotFoundException();
         }

         Iterator i = bl.values().iterator();
         boolean binFound = false;

         while (!binFound && i.hasNext()) {
            Bin b = (Bin) i.next();

            if (b.eval(val)) {

               // System.out.println("classifiyng : an value "  + an + " " +
               // val );
               binFound = true;
               bl.incrementTotal();
            }
         }

         return binFound;
      } // end method classify


      /**
       * Find a suitable bin for a String item.
       *
       * @param  an  attribute name
       * @param  val value
       *
       * @return find a suitable bin for a String item.
       *
       * @throws AttributeNotFoundException when attribute is not found
       */
      boolean classify(String an, String val)
         throws AttributeNotFoundException {
         BinList bl = (BinList) get(an);

         if (bl == null) {

            // this is an unknown attribute.
            // we cannot classify it further
            unknownAttributes++;
            throw new AttributeNotFoundException();
         }

         Iterator i = bl.values().iterator();
         boolean binFound = false;

         while (!binFound && i.hasNext()) {
            Bin b = (Bin) i.next();

            if (b.eval(val)) {
               binFound = true;
               bl.incrementTotal();
            }
         }

         return binFound;
      }

      NumericEvaluate createNumericEvaluate(String s)
         throws MalformedEquationException {
         int loc;
         double d;

         if (
             ((loc = s.indexOf(operators[0])) != -1) ||
                ((loc = s.indexOf(operators[1])) != -1)) {
            String[] t = breakUp(s, loc, operators[0].length());

            if (attributeList.containsKey(t[0])) {

               try {
                  d = Double.parseDouble(t[2]);
               } catch (Exception e) {
                  throw new MalformedEquationException();
               }

               return new NumericEvaluate(LESS_THAN_EQUAL_TO, d);
            }

            try {
               d = Double.parseDouble(t[0]);
            } catch (Exception e) {
               throw new MalformedEquationException();
            }

            return new NumericEvaluate(LESS_THAN_EQUAL_TO, d);
         } else if (
                    ((loc = s.indexOf(operators[2])) != -1) ||
                       ((loc = s.indexOf(operators[3])) != -1)) {
            String[] t = breakUp(s, loc, operators[2].length());

            if (attributeList.containsKey(t[0])) {

               try {
                  d = Double.parseDouble(t[2]);
               } catch (Exception e) {
                  throw new MalformedEquationException();
               }

               return new NumericEvaluate(GREATER_THAN_EQUAL_TO, d);
            }

            try {
               d = Double.parseDouble(t[0]);
            } catch (Exception e) {
               throw new MalformedEquationException();
            }

            return new NumericEvaluate(GREATER_THAN_EQUAL_TO, d);
         } else if ((loc = s.indexOf(operators[4])) != -1) {
            String[] t = breakUp(s, loc, operators[4].length());

            if (attributeList.containsKey(t[0])) {

               try {
                  d = Double.parseDouble(t[2]);
               } catch (Exception e) {
                  throw new MalformedEquationException();
               }

               return new NumericEvaluate(EQUAL_TO, d);
            }

            try {
               d = Double.parseDouble(t[0]);
            } catch (Exception e) {
               throw new MalformedEquationException();
            }

            return new NumericEvaluate(EQUAL_TO, d);
         } else if ((loc = s.indexOf(operators[5])) != -1) {
            String[] t = breakUp(s, loc, operators[5].length());

            if (attributeList.containsKey(t[0])) {

               try {
                  d = Double.parseDouble(t[2]);
               } catch (Exception e) {
                  throw new MalformedEquationException();
               }

               return new NumericEvaluate(EQUAL_TO, d);
            }

            try {
               d = Double.parseDouble(t[0]);
            } catch (Exception e) {
               throw new MalformedEquationException();
            }

            return new NumericEvaluate(EQUAL_TO, d);
         } else if ((loc = s.indexOf(operators[6])) != -1) {
            String[] t = breakUp(s, loc, operators[6].length());

            if (attributeList.containsKey(t[0])) {

               try {
                  d = Double.parseDouble(t[2]);
               } catch (Exception e) {
                  throw new MalformedEquationException();
               }

               return new NumericEvaluate(LESS_THAN, d);
            }

            try {
               d = Double.parseDouble(t[0]);
            } catch (Exception e) {
               throw new MalformedEquationException();
            }

            return new NumericEvaluate(LESS_THAN, d);
         } else if ((loc = s.indexOf(operators[7])) != -1) {
            String[] t = breakUp(s, loc, operators[7].length());

            if (attributeList.containsKey(t[0])) {

               try {
                  d = Double.parseDouble(t[2]);
               } catch (Exception e) {
                  throw new MalformedEquationException();
               }

               return new NumericEvaluate(GREATER_THAN, d);
            }

            try {
               d = Double.parseDouble(t[0]);
            } catch (Exception e) {
               throw new MalformedEquationException();
            }

            return new NumericEvaluate(GREATER_THAN, d);
         }

         throw new MalformedEquationException();
      } // end method createNumericEvaluate

      StringEvaluate createStringEvaluate(String s)
         throws MalformedEquationException {
         int loc;
         // System.out.println("createStringEvaluate called"); perhaps check for
         // > or < and throw malfomed eq exception first?

         if ((loc = s.indexOf(operators[4])) != -1) {
            String[] t = breakUp(s, loc, operators[4].length());

            if (attributeList.containsKey(t[0])) {
               return new StringEvaluate(t[2]);
            }

            return new StringEvaluate(t[0]);
         } else if ((loc = s.indexOf(operators[5])) != -1) {
            String[] t = breakUp(s, loc, operators[5].length());

            if (attributeList.containsKey(t[0])) {
               return new StringEvaluate(t[2]);
            }

            return new StringEvaluate(t[0]);
         }

         throw new MalformedEquationException();
      }


      /**
       * Find a suitable bin for a double item.
       *
       * @param  an  attribute name
       * @param  val value
       *
       * @return find a suitable bin for a double item.
       */
      String getBinNameForValue(String an, double val) {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            return null;
         }

         Iterator i = bl.keySet().iterator();
         boolean binFound = false;

         while (!binFound && i.hasNext()) {
            String bName = (String) i.next();
            Bin b = (Bin) bl.get(bName);

            if (b.eval(val, false)) {
               return bName;
            }
         }

         return null;
      }

      /**
       * Find a suitable bin for a string item.
       *
       * @param  an  attribute name
       * @param  val value
       *
       * @return find a suitable bin for a string item.
       */
      String getBinNameForValue(String an, String val) {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            return null;
         }

         Iterator i = bl.keySet().iterator();
         boolean binFound = false;

         while (!binFound && i.hasNext()) {
            String bName = (String) i.next();
            Bin b = (Bin) bl.get(bName);

            if (b.eval(val, false)) {
               return bName;
            }
         }

         return null;
      }

      /**
       * Get the names of the bins for this attribute.
       *
       * @param  an attribute name
       *
       * @return get the names of the bins for this attribute.
       */
      String[] getBinNames(String an) {
         BinList bl = (BinList) get(an);
         String[] retVal;

         if (bl != null) {
            retVal = new String[bl.size()];

            Iterator i = bl.keySet().iterator();
            int idx = 0;

            while (i.hasNext()) {
               retVal[idx] = (String) i.next();
               idx++;
            }
         } else {
            retVal = new String[0];
         }

         return retVal;
      }

       /**
        * Get the lower bound of the specified bin
        * @param an attribute name
        * @param bn bin name
        * @return lower bound
        * @throws BinNotFoundException when bin is not found
        */
      double getLowerBound(String an, String bn) throws BinNotFoundException {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            throw new BinNotFoundException();
         }

         Bin b = (Bin) bl.get(bn);
         Evaluate e = b.item;

         if (e instanceof BoundedNumericEvaluate) {
            return ((BoundedNumericEvaluate) e).lower;
         } else if (e instanceof NumericEvaluate) {

            // what to do?
            return 0;
         } else {
            throw new BinNotFoundException();
         }
      }

       /**
        * Get the number of unknown attributes
        * @return number of unknown attributes
        */
      int getNumUnknownAttributes() { return unknownAttributes; }

       /**
        * Get the tally for a bin
        * @param an attribute name
        * @param bn bin name
        * @return tally for the specified bin
        */
      int getTally(String an, String bn) {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            return -1;
         }

         Bin b = (Bin) bl.get(bn);

         if (b == null) {
            return -1;
         }

         return b.tally;
      }

       /**
        * Get the total for an attribute
        * @param an attribute name
        * @return total
        */
      int getTotal(String an) {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            return -1;
         }

         return bl.getTotal();
      }

       /**
        * Get the upper bound
        * @param an attribute name
        * @param bn bin name
        * @return upper bound for bin
        * @throws BinNotFoundException when bin not found
        */
      double getUpperBound(String an, String bn) throws BinNotFoundException {
// return 0;
         BinList bl = (BinList) get(an);

         if (bl == null) {
            throw new BinNotFoundException();
         }

         Bin b = (Bin) bl.get(bn);
         Evaluate e = b.item;

         if (e instanceof BoundedNumericEvaluate) {
            return ((BoundedNumericEvaluate) e).upper;
         } else if (e instanceof NumericEvaluate) {

            // what to do?
            return 0;
         } else {
            throw new BinNotFoundException();
         }
      }

      /**
       * Return true if the lower bound of the specified bin is included, false
       * otherwise.
       *
       * @param  an attribute name
       * @param  bn bin name
       *
       * @return return true if the lower bound of the specified bin is
       *         included, false otherwise.
       *
       * @throws BinNotFoundException when bin is not found
       */
      boolean includeLowerBound(String an, String bn)
         throws BinNotFoundException {
         BinList bl = (BinList) get(an);

         // System.out.println("an: " + an + "bn: " + bn);
         if (bl == null) {
            throw new BinNotFoundException();
         }

         Bin b = (Bin) bl.get(bn);
         Evaluate e = b.item;

         if (e instanceof BoundedNumericEvaluate) {
            return ((BoundedNumericEvaluate) e).includeLower;
         } else if (e instanceof NumericEvaluate) {

            // what to do?
            return false;
         } else {
            throw new BinNotFoundException();
         }
      }

      /**
       * Return true if the upper bound of the specified bin is included, false
       * otherwise.
       *
       * @param  an attribute name
       * @param  bn bin name
       *
       * @return return true if the upper bound of the specified bin is
       *         included, false otherwise.
       *
       * @throws BinNotFoundException when bin is not found
       */
      boolean includeUpperBound(String an, String bn)
         throws BinNotFoundException {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            throw new BinNotFoundException();
         }

         Bin b = (Bin) bl.get(bn);
         Evaluate e = b.item;

         if (e instanceof BoundedNumericEvaluate) {
            return ((BoundedNumericEvaluate) e).includeUpper;
         } else if (e instanceof NumericEvaluate) {

            // what to do?
            return false;
         } else {
            throw new BinNotFoundException();
         }
      }

      Evaluate parse(String s, boolean numeric)
         throws MalformedEquationException {
         // System.out.println("PARSE: "+s);

         // base case: we make a Numeric or StringEval
         // we hit the base case if there are no && or || in S
         if ((s.indexOf(AND) == -1) && (s.indexOf(OR) == -1)) {

            // return a Numeric or StringEval
            if (numeric) {

               // System.out.println("create Numeric Eval object: "+s);
               return createNumericEvaluate(s);
            } else {

               // System.out.println("create String Eval object: "+s);
               return createStringEvaluate(s);
            }
         }

         // otherwise strip the conjunctions and recurse
         // find the outermost conjunction
         // System.out.println("getPhraseList: "+s);
         PhraseList pl = getPhraseList(s);
         // System.out.println("PL: "+pl.part1); System.out.println("PL:
         // "+pl.conjunction); System.out.println("PL: "+pl.part2);

         if (pl == null) {
            throw new MalformedEquationException();
         }

         if (pl.conjunction.trim().equals(AND)) {

            // System.out.println(pl.conjunction+": "+s);
            return new AndList(pl.part1, pl.part2, numeric);
         } else if (pl.conjunction.trim().equals(OR)) {

            // System.out.println(pl.conjunction+": "+s);
            return new OrList(pl.part1, pl.part2, numeric);
         } else {
            throw new MalformedEquationException();
         }
      } // end method parse

      /**
       * Remove a bin.
       *
       * @param  an attribute name
       * @param  bn bin name
       *
       * @throws BinNotFoundException when bin is not found
       */
      void removeBin(String an, String bn) throws BinNotFoundException {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            throw new BinNotFoundException();
         }

         Bin b = (Bin) bl.remove(bn);
         bl.decrementTotal(b.tally);
      }

       /**
        * Set the tally for a bin
        * @param an attribute name
        * @param bn bin name
        * @param tally new tally
        * @throws BinNotFoundException when bin is not found
        */
      void setBinTally(String an, String bn, int tally)
         throws BinNotFoundException {
         BinList bl = (BinList) get(an);
         bl.total += tally;

         if (bl == null) {
            throw new BinNotFoundException();
         }

         Bin b = (Bin) bl.get(bn);

         if (b == null) {
            throw new BinNotFoundException();
         } else {
            b.tally = tally;
         }
      }

       /**
        * Set the total for a bin
        * @param an attribute name
        * @param total total count
        */
      void setTotal(String an, int total) {
         BinList bl = (BinList) get(an);

         if (bl == null) {
            ;
         }

         bl.setTotal(total);
      }

      /**
       * Loops through the list and only returns true when all items in the list
       * are true.
       *
       * @author  $Author: mcgrath $
       * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
       */
      private class AndList extends Evaluate implements Serializable {

         /** Use serialVersionUID for interoperability. */
         static private final long serialVersionUID = 7469737154321493465L;
         private LinkedList items;

          /**
           * Constructor
           */
         AndList() { items = new LinkedList(); }

          /**
           * Constructor
           * @param s1 expression 1
           * @param s2 expression 2
           * @param numeric true if numeric
           * @throws MalformedEquationException when s1 or s2 could not be parsed
           */
         AndList(String s1, String s2, boolean numeric)
            throws MalformedEquationException {
            this();
            items.add(parse(s1, numeric));
            items.add(parse(s2, numeric));
         }

          /**
           * Add an item to be ANDed
           * @param e new item
           */
         void addItem(Evaluate e) { items.add(e); }

          /**
           * Evaluate the contents of this list for v.  Only return true if all
           * items return true
           * @param v value
           * @return  true if all items evaluate to true
           */
         boolean eval(String v) {
            Iterator i = items.listIterator();

            while (i.hasNext()) {
               Evaluate ev = (Evaluate) i.next();
               boolean retVal = ev.eval(v);

               if (!retVal) {
                  return false;
               }
            }

            return true;
         }

          /**
           * Evaluate the contents of this list for v.  Only return true if all
           * items return true
           * @param v value
           * @return  true if all items evaluate to true
           */
         boolean eval(double v) {
            Iterator i = items.listIterator();

            while (i.hasNext()) {
               Evaluate ev = (Evaluate) i.next();
               boolean retVal = ev.eval(v);

               if (!retVal) {
                  return false;
               }
            }

            return true;
         }

          /**
           * Evaluate the contents of this list for v.  Only return true if all
           * items return true
           * @param v value
           * @return  true if all items evaluate to true
           */
         boolean eval(int v) {
            Iterator i = items.listIterator();

            while (i.hasNext()) {
               Evaluate ev = (Evaluate) i.next();
               boolean retVal = ev.eval(v);

               if (!retVal) {
                  return false;
               }
            }

            return true;
         }

          /**
           * Get a list of the attribute value pairs for an attribute name
           * @param an attribute name
           * @return list of attirbute value pairs
           */
         ArrayList getAttrValuePair(String an) {
            Iterator i = items.listIterator();
            ArrayList curr;
            ArrayList prev = new ArrayList();

            while (i.hasNext()) {
               Evaluate ev = (Evaluate) i.next();

               if (ev.getAttrValuePair(an) != null) {
                  curr = ev.getAttrValuePair(an);

                  Iterator it1 = curr.iterator();
                  Iterator it2 = prev.iterator();
                  ArrayList cond = new ArrayList();

                  while (it1.hasNext()) {

                     while (it2.hasNext()) {
                        HashMap hm1 = (HashMap) it1.next();
                        HashMap hm2 = (HashMap) it2.next();
                        hm1.putAll(hm2);
                        cond.add(hm1);
                        prev = cond;
                     }
                  }
               }
            }

            return prev;
         } // end method getAttrValuePair

          /**
           * Get the condition for the attribute name
           * @param an attribute name
           * @return condition
           */
         String getCondition(String an) {
            Iterator i = items.listIterator();
            String cond = "";

            while (i.hasNext()) {
               cond = cond + " ( ";

               Evaluate ev = (Evaluate) i.next();
               cond = cond + ev.getCondition(an) + " )";

               if (i.hasNext()) {
                  cond = cond + " AND ";
               }
            }

            return cond;
         }

          /**
           * Print contents for debugging
           */
         void print() {
            Iterator i = items.listIterator();

            while (i.hasNext()) {
            	_logger.fine("(");

               Evaluate ev = (Evaluate) i.next();
               ev.print();
               _logger.fine(")");

               if (i.hasNext()) {
            	   _logger.fine(" && ");
               }
            }


         }


      } // end class AndList


       /**
        * Used for a bin with numeric boundaries
        */
      private class BoundedNumericEvaluate extends Evaluate {
         boolean includeLower;
         boolean includeUpper;
         double lower;
         double upper;

           /**
            * Constructor
            */
         BoundedNumericEvaluate() { }

           /**
            * Constructor
            * @param low lower bound
            * @param includeLow true if inclusive of lower bound
            * @param high upper bound
            * @param includeHi true if inclusive of upper bound
            */
         BoundedNumericEvaluate(double low, boolean includeLow,
                                double high, boolean includeHi) {

            // System.out.println("created BoundedNumEval low " + low + " high
            // " + high);
            lower = low;
            includeLower = includeLow;
            upper = high;
            includeUpper = includeHi;
         }

           /**
            * eval
            * @param s
            * @return false
            */
         boolean eval(String s) { return false; }

           /**
            * Return true if d falls in this bin
            * @param d value
            * @return true if d falls in this bin
            */
         boolean eval(double d) {

            // System.out.println("evaluate d " +d + " upper " + upper + "
            // lower " + lower);
            if (includeLower) {

               if (includeUpper) {
                  return (d >= lower) && (d <= upper);
               } else {
                  return (d >= lower) && (d < upper);
               }
            }

            if (includeUpper) {
               return (d > lower) && (d <= upper);
            } else {
               return (d > lower) && (d < upper);
            }
         }

           /**
            * Return true if d falls in this bin
            * @param i value
            * @return true if d falls in this bin
            */
         boolean eval(int i) { return eval((double) i); }


         // FIX ME by David - Cannot be implemented for ADTrees by Anca
         ArrayList getAttrValuePair(String an) { return null; }

         // FIX ME by David - FIXED by Anca
         String getCondition(String an) {
            boolean eliminateLower = false;
            boolean eliminateUpper = false;

            // Double upperValue = new Double(upper);
            if (Double.isInfinite(lower)) {
               eliminateLower = true;
            }

            if (Double.isInfinite(upper)) {
               eliminateUpper = true;
            }

            //      System.out.println("upper " + upper + " lower " + lower);
            //   System.out.println("eliminateUpper " + eliminateUpper + "
            // eliminateLower " + eliminateLower);
            if (eliminateLower && eliminateUpper) {
               return an + " is not null";
            }

            if (eliminateLower) {

               if (includeUpper) {
                  return an + " <= " + upper;
               } else { // do not includeUpper
                  return an + " < " + upper;
               }
            }

            if (eliminateUpper) {

               if (includeLower) {
                  return an + " >= " + lower;
               } else {
                  return an + " > " + lower;
               }
            }

            if (includeLower) {

               if (includeUpper) {
                  return an + " >= " + lower + " AND " + an + " <= " + upper;
               } else {
                  return an + " >= " + lower + " AND " + an + " < " + upper;
               }
            } else {

               if (includeUpper) {
                  return an + " > " + lower + " AND " + an + " <= " + upper;
               } else {
                  return an + " > " + lower + " AND " + an + " < " + upper;
               }
            }
         } // end method getCondition

           /**
            *
            */
         void print() {
            //
         }
      } // end class BoundedNumericEvaluate

      /**
       * Base class for bins.  Defines eval() methods.
       *
       * @author  $Author: mcgrath $
       * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
       */
      private class Evaluate implements Serializable {

         /** Use serialVersionUID for interoperability. */
         static private final long serialVersionUID = 2788421328116623398L;

          /**
           * Constructor
           */
         Evaluate() { }

          /**
           * Evaluate for a string value
           * @param val value
           * @return true if val falls in this bin
           */
         boolean eval(String val) { return false; }

          /**
           * Evaluate for a double value
           * @param val value
           * @return true if val falls in this bin
           */
         boolean eval(double val) { return false; }

          /**
           * Evaluate for a int value
           * @param val value
           * @return true if val falls in this bin
           */
         boolean eval(int val) { return false; }

          /**
           * Get attribute value pairs for specified attribute
           * @param an attribute name
           * @return  attribute value pairs for specified attribute
           */
         ArrayList getAttrValuePair(String an) { return new ArrayList(); }

          /**
           * Get condition
           * @param an attirbute name
           * @return condition
           */
         String getCondition(String an) { return an; }

         void print() { }
      }

       /**
        * Holds a set of strings
        */
      private class MultiStringEvaluate extends Evaluate
         implements Serializable {

         /** Use serialVersionUID for interoperability. */
         static private final long serialVersionUID = -5245658788684673688L;
         HashSet values;

           /**
            * Constructor
            */
         MultiStringEvaluate() { values = new HashSet(); }

           /**
            * Constructor
            * @param vals the values of interest
            */
         MultiStringEvaluate(String[] vals) {
            values = new HashSet();

            for (int i = 0; i < vals.length; i++) {
               values.add(vals[i]);
            }
         }

          /**
           * Evaluate for a string value
           * @param s value
           * @return true if val falls in this bin
           */
         boolean eval(String s) { return values.contains(s); }

          /**
           * Evaluate for a string value
           * @param d value
           * @return true if val falls in this bin
           */
         boolean eval(double d) { return eval(Double.toString(d)); }

          /**
           * Evaluate for a string value
           * @param i value
           * @return true if val falls in this bin
           */
         boolean eval(int i) { return eval(Integer.toString(i)); }

           /**
            *
            * @param an
            * @return
            */
         ArrayList getAttrValuePair(String an) {
            Iterator i = values.iterator();
            ArrayList ar = new ArrayList(values.size());

            while (i.hasNext()) {
               HashMap hm = new HashMap();
               hm.put(an, i.next());
               ar.add(hm);
            }

            return ar;
         }

         // FIX ME by David - FIXED by Anca
         String getCondition(String an) {
            Iterator i = values.iterator();
            // System.out.println("values iterator for attr " + an + " is " +
            // values.size());
            String condition = "";

            while (i.hasNext()) {
               condition = condition + an + " = '" + i.next() + "'";

               if (i.hasNext()) {
                  condition = condition + " OR ";
                  // System.out.println("condition is " + condition);
               }
            }

            condition = " ( " + condition + " )";

            return condition;
         }

         // FIX ME
         void print() {
            //
         }
      } // end class MultiStringEvaluate

      /**
       * Bin for less than or equal to a certain value.
       *
       * @author  $Author: mcgrath $
       * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
       */
      private class NumericEvaluate extends Evaluate implements Serializable {

         /** Use serialVersionUID for interoperability. */
         static private final long serialVersionUID = -4298139193855256667L;
         private String operator;
         private double value;

          /**
           * Constructor
           */
         NumericEvaluate() { }

          /**
           * Constructor
           * @param op operator
           * @param v value
           */
         NumericEvaluate(String op, double v) {
            operator = op;
            value = v;
            // System.out.println("new NE: "+op+" "+v);
         }

          /**
           * Evaluate for a string value
           * @param s value
           * @return true if val falls in this bin
           */
         boolean eval(String s) {

            try {
               Double d = Double.valueOf(s);

               return eval(d.doubleValue());
            } catch (Throwable t) {
               return false;
            }
         }

          /**
           * Evaluate for a double value
           * @param d value
           * @return true if val falls in this bin
           */
         boolean eval(double d) {

            if (operator.equals(EQUAL_TO)) {
               return (value == d);
            }

            if (operator.equals(LESS_THAN)) {
               return (value > d);
            }

            if (operator.equals(GREATER_THAN)) {
               return (value < d);
            }

            if (operator.equals(LESS_THAN_EQUAL_TO)) {
               return (value >= d);
            }

            if (operator.equals(GREATER_THAN_EQUAL_TO)) {
               return (value <= d);
            }

            return false;
         }

          /**
           * Evaluate for a int value
           * @param i value
           * @return true if val falls in this bin
           */
         boolean eval(int i) { return eval((double) i); }


          /**
           * Get attribute value pairs for specified attribute
           *
           * @param an attribute name
           * @return attribute value pairs for specified attribute
           */
          ArrayList getAttrValuePair(String an) {

              if (operator.equals(EQUAL_TO)) {
                  HashMap hm = new HashMap();
                  hm.put(an, String.valueOf(value));

                  ArrayList ar = new ArrayList(1);
                  ar.add(hm);

                  return ar;
              } else {
                  return null;
              }
          }


          /**
           * Get condition
           *
           * @param an attirbute name
           * @return condition
           */
          String getCondition(String an) {
              return an + SPACE + operator + SPACE + value;
          }

          /**
           * print contents for debuggin
           */
         void print() { System.out.print(" " + operator + " " + value); }

      } // end class NumericEvaluate

      /**
       * OR all the contents of the lists together.
       *
       * @author  $Author: mcgrath $
       * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
       */
      private class OrList extends Evaluate implements Serializable {

         /** Use serialVersionUID for interoperability. */
         static private final long serialVersionUID = 6425809430231111674L;
         LinkedList items;

          /**
           * Constructor
           */
         OrList() { items = new LinkedList(); }

          /**
           * Constructor
           * @param s1 equation 1
           * @param s2 equation 2
           * @param numeric true if numeric
           * @throws MalformedEquationException when equations could not be parsed
           */
         OrList(String s1, String s2, boolean numeric)
            throws MalformedEquationException {
            this();
            items.add(parse(s1, numeric));
            items.add(parse(s2, numeric));
         }

          /**
           * Add an item to be ORed
           * @param e new item
           */
         void addItem(Evaluate e) { items.add(e); }

          /**
           * Evaluate for a string value
           *
           * @param v value
           * @return true if val falls in this bin
           */
          boolean eval(String v) {
              Iterator i = items.listIterator();

              while (i.hasNext()) {
                  Evaluate ev = (Evaluate) i.next();
                  boolean retVal = ev.eval(v);

                  if (retVal) {
                      return true;
                  }
              }

              return false;
          }

          /**
           * Evaluate for a double value
           *
           * @param v value
           * @return true if val falls in this bin
           */
          boolean eval(double v) {
              Iterator i = items.listIterator();

              while (i.hasNext()) {
                  Evaluate ev = (Evaluate) i.next();
                  boolean retVal = ev.eval(v);

                  if (retVal) {
                      return true;
                  }
              }

              return false;
          }

          /**
           * Evaluate for a int value
           *
           * @param v value
           * @return true if val falls in this bin
           */
          boolean eval(int v) {
              Iterator i = items.listIterator();

              while (i.hasNext()) {
                  Evaluate ev = (Evaluate) i.next();
                  boolean retVal = ev.eval(v);

                  if (retVal) {
                      return true;
                  }
              }

              return false;
          }

          /**
           * Get attribute value pairs for specified attribute
           *
           * @param an attribute name
           * @return attribute value pairs for specified attribute
           */
          ArrayList getAttrValuePair(String an) {
              Iterator i = items.listIterator();
              ArrayList cond = new ArrayList();

              while (i.hasNext()) {
                  Evaluate ev = (Evaluate) i.next();

                  if (ev.getAttrValuePair(an) != null) {
                      cond.add(ev.getAttrValuePair(an));
                  }
              }

              return cond;
          }

          /**
           * Get condition
           *
           * @param an attirbute name
           * @return condition
           */
          String getCondition(String an) {
              Iterator i = items.listIterator();
              String cond = "";

              while (i.hasNext()) {
                  cond = cond + "(";

                  Evaluate ev = (Evaluate) i.next();
                  cond = cond + ev.getCondition(an);
                  cond = cond + ")";

                  if (i.hasNext()) {
                      cond = cond + " OR ";
                  }
              }

              return cond;
          }

          /**
           * Print contents for debugging
           */
         void print() {
            Iterator i = items.listIterator();

            while (i.hasNext()) {
            	_logger.fine("(");

               Evaluate ev = (Evaluate) i.next();
               ev.print();
               _logger.fine(")");

               if (i.hasNext()) {
            	   _logger.fine(" || ");
               }
            }
         }


      } // end class OrList

      /**
       * Simple structure to hold two strings and a conjunction, like "A and B"
       *
       * @author  $Author: mcgrath $
       * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
       */
      private class PhraseList implements Serializable {

         /** Use serialVersionUID for interoperability. */
         static private final long serialVersionUID = -5919882128070984583L;
          /** the conjunction */
         private String conjunction;
          /** before the conjunction */
         private String part1;
          /** after the conjunction */
         private String part2;

          /**
           * Constructor
           */
         PhraseList() { }

          /**
           * Constructor
           * @param s first part
           * @param c conjucntion
           * @param s2 second part
           */
         PhraseList(String s, String c, String s2) {
            part1 = s;
            conjunction = c;
            part2 = s2;
         }
      }

      /**
       * Bin for a single string value
       *
       * @author  $Author: mcgrath $
       * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
       */
      private class StringEvaluate extends Evaluate implements Serializable {

         /** Use serialVersionUID for interoperability. */
         static private final long serialVersionUID = -5022787361753397713L;
          /** the value */
         String value;

          /**
           * Constructor
           */
         StringEvaluate() { }

          /**
           * Constructor
           * @param v the value
           */
         StringEvaluate(String v) { value = v; }

          /**
           * Evaluate for a string value
           *
           * @param s value
           * @return true if val falls in this bin
           */
          boolean eval(String s) {
              return value.trim().equals(s.trim());
          }

          /**
           * Evaluate for a double value
           *
           * @param d value
           * @return true if val falls in this bin
           */
          boolean eval(double d) {
              return eval(Double.toString(d));
          }

          /**
           * Evaluate for a int value
           *
           * @param i value
           * @return true if val falls in this bin
           */
          boolean eval(int i) {
              return eval(Integer.toString(i));
          }


          /**
           * Get attribute value pairs for specified attribute
           *
           * @param an attribute name
           * @return attribute value pairs for specified attribute
           */
          ArrayList getAttrValuePair(String an) {
              HashMap hm = new HashMap();
              hm.put(an, value);

              ArrayList ar = new ArrayList(1);
              ar.add(hm);

              return ar;
          }


          /**
           * Get condition
           *
           * @param an attirbute name
           * @return condition
           */
          String getCondition(String an) {
              return value + EQUAL_TO + an;
          }

          /**
           * print contents for debugging
           */
         void print() { System.out.print(" == " + value); }
      } // end class StringEvaluate

      /**
       * Class to represent a bin
       *
       * @author  $Author: mcgrath $
       * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
       */
      public class Bin extends Evaluate implements Serializable {

         /** Use serialVersionUID for interoperability. */
         static private final long serialVersionUID = -7252419429746159100L;
          /** evaluator */
         private Evaluate item;
          /** tally of items that fall in this bin */
         private int tally;

          /**
           * Constructor
           */
         Bin() { }

          /**
           * Constructor
           * @param e evaluator
           */
         Bin(Evaluate e) {
            item = e;
            tally = 0;
         }

          /**
           * Evaluate for a string value
           *
           * @param v value
           * @return true if val falls in this bin
           */
          boolean eval(String v) {
              boolean retVal = item.eval(v);

              if (retVal) {
                  tally++;
              }

              return retVal;
          }

          /**
           * Evaluate for a double value
           *
           * @param v value
           * @return true if val falls in this bin
           */
          boolean eval(double v) {
              boolean retVal = item.eval(v);

              if (retVal) {
                  tally++;
              }

              return retVal;
          }

          /**
           * Evaluate for a int value
           *
           * @param v value
           * @return true if val falls in this bin
           */
          boolean eval(int v) {
              boolean retVal = item.eval(v);

              if (retVal) {
                  tally++;
              }

              return retVal;
          }

          /**
           * Evaluate for a string value
           *
           * @param v value
           * @param incTally true if tally should be incremented
           * @return true if val falls in this bin
           */
         boolean eval(String v, boolean incTally) {
            boolean retVal = item.eval(v);

            if (retVal && incTally) {
               tally++;
            }

            return retVal;
         }

          /**
           * Evaluate for a double value
           *
           * @param v value
           * @param incTally true if tally should be incremented
           * @return true if val falls in this bin
           */
         boolean eval(double v, boolean incTally) {
            boolean retVal = item.eval(v);

            if (retVal && incTally) {
               tally++;
            }

            return retVal;
         }

          /**
           * Evaluate for a int value
           *
           * @param v value
           * @param incTally true if tally should be incremented
           * @return true if val falls in this bin
           */
         boolean eval(int v, boolean incTally) {
            boolean retVal = item.eval(v);

            if (retVal && incTally) {
               tally++;
            }

            return retVal;
         }

          /**
           * print debugging info
           */
         void print() {
            item.print();
            System.out.println(EMPTY);
         }

          /**
           * Get attribute value pairs for specified attribute
           *
           * @param an attribute name
           * @return attribute value pairs for specified attribute
           */
          public ArrayList getAttrValuePair(String an) {
              return item.getAttrValuePair(an);
          }

          /**
           * Get condition
           *
           * @param an attirbute name
           * @return condition
           */
          public String getCondition(String an) {
              return item.getCondition(an);
          }

          /**
           * increment the tally
           */
         public void incrementTally() { tally = tally + 1; }

          /**
           * Set the tally
           * @param n new ttaly
           */
         public void setTally(int n) { tally = n; }


      } // end class Bin

      /**
       * List of bins
       *
       * @author  $Author: mcgrath $
       * @version $Revision: 3021 $, $Date: 2007-05-18 16:43:49 -0500 (Fri, 18 May 2007) $
       */
      public class BinList extends HashMap implements Serializable {

         /** Use serialVersionUID for interoperability. */
         static private final long serialVersionUID = 8455630864511724017L;
          /** total number of tallies in this list */
         int total;

          /**
           * Constructor
           */
         BinList() {
            super();
            total = 0;
         }

          /**
           * Decrement total by d
           * @param d decrement amount
           */
         void decrementTotal(int d) { total = total - d; }

          /**
           * Get the total
           * @return total
           */
         int getTotal() { return total; }

          /**
           * Increment the total
           */
         void incrementTotal() { total++; }

          /**
           * Set the total
           * @param t new total
           */
         public void setTotal(int t) { total = t; }
      }
   } // end class ClassTree

    /**
     * thrown when bins have the same name
     */
   public class DuplicateBinNameException extends Exception { }

    /**
     * thrown when an equation cannot be parsed
     */
   public class MalformedEquationException extends Exception { }
} // end class BinTree
