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

package org.seasr.datatypes.table.sparse.primitivehash;

import java.util.logging.Logger;

import org.seasr.datatypes.table.sparse.ObjectComparator;


/**
 * Wrapper for an item in a sparse column. Used in sorting methods.
 *
 * @author  goren
 * @version $Revision: 1.4 $, $Date: 2007/05/18 21:27:14 $
 */

public class Element implements Comparable {

   //~ Instance fields *********************************************************

   /** Whether or not this element is default. */
   private boolean _default;

   /** Whether or not this element is empty. */
   private boolean empty;

   /** Whether or not this elelemt exists. */
   private boolean exist;

   /** Index of this element. */
   private int index;

   /** Whether or not this element is missing. */
   private boolean missing;

   /** Element contents. */
   private Object obj;

   private static Logger _logger = Logger.getLogger("Element");

   //~ Constructors ************************************************************

   /**
    * Creates a new Element object.
    *
    * @param _obj   Contents of this element
    * @param _index Index of this element
    */
   public Element(Object _obj, int _index) {
      obj = _obj;
      index = _index;
      missing = false;
      empty = false;
      _default = false;
      exist = true;
   }


   /**
    * Creates a new Element object.
    *
    * @param _obj     Contents of this element
    * @param _index   Index of this element
    * @param _missing Whether or not this element is missing
    * @param _empty   Whether or not this element is empty
    * @param _def     Whether or not this element is default
    * @param _exist   Whether or not this element exists
    */
   public Element(Object _obj, int _index, boolean _missing, boolean _empty,
                  boolean _def, boolean _exist) {
      obj = _obj;
      index = _index;
      exist = _exist;
      missing = _missing;
      empty = _empty;
      _default = _def;
   }

   //~ Methods *****************************************************************

   /**
    * Compares for equality with another element.
    *
    * @param  obj Object to compare
    *
    * @return Result of comparison (-1,0, or 1)
    */
   public int compareTo(Object obj) {

      if (obj instanceof Element) {
    	  _logger.warning("WARNING: Cannot compare object " +
                            "of type Element to object of type " +
                            obj.getClass().getName());
         return -1;
      }

      Element other = (Element) obj;
      // checking for missing values of any kind;

      // if this object is not valid
      if (missing || empty || _default || !exist) {

         // and the other object is not valid too
         if (
             other.getMissing() ||
                other.getEmpty() ||
                other.getDefault() ||
                !other.getExist()) {
            return 0; // then they are equal
         }
         // if the other object is valid, then this object is "greater" than
         // other. (missing values are sorted to the end)
         else {
            return 1;
         }
      }
      // this object is valid but the other is not valid
      else if (
               other.getMissing() ||
                  other.getEmpty() ||
                  other.getDefault() ||
                  !other.getExist()) {
         return -1; // then this object is smaller.
      }


      // comparing the class of obj
      Object otherObj = other.getObj();
      String myObjectClass = this.obj.getClass().getName();
      String otherObjectClass = otherObj.getClass().getName();

      if (!myObjectClass.equals(otherObjectClass)) {
    	  _logger.warning("WARNING: Cannot compare object " +
                            "of type " + myObjectClass + " to object of type " +
                            otherObjectClass);
         return -1;
      }


      // both objects are valid objects. compare them using ObjectComparator
      return new ObjectComparator().compare(obj, otherObj);
   } // end method compareTo

   /**
    * Returns whether or not this element is default.
    *
    * @return Whether or not this element is default
    */
   public boolean getDefault() { return _default; }

   /**
    * Returns whether or not this element is empty.
    *
    * @return Whether or not this element is empty
    */
   public boolean getEmpty() { return empty; }

   /**
    * Returns whether or not this element exists.
    *
    * @return Whether or not this element exists
    */
   public boolean getExist() { return exist; }

   /**
    * Returns the index of this element
    *
    * @return Index of this element
    */
   public int getIndex() { return index; }

   /**
    * Returns whether or not this element is missing.
    *
    * @return Whether or not this element is missing
    */
   public boolean getMissing() { return missing; }


   /**
    * Returns the contents of the element.
    *
    * @return Contents of this element
    */
   public Object getObj() { return obj; }

   /**
    * Sets whether or not this element is default.
    *
    * @param bl Whether or not this element is default
    */
   public void setDefault(boolean bl) { _default = bl; }

   /**
    * Sets whether or not this element is empty.
    *
    * @param bl Whether or not this element is empty
    */
   public void setEmpty(boolean bl) { empty = bl; }

   /**
    * Sets whether or not this element exists.
    *
    * @param bl Whether or not this element exists
    */
   public void setExist(boolean bl) { exist = bl; }

   /**
    * Sets whether or not this element is missing.
    *
    * @param bl Whether or not this element is mising
    */
   public void setMissing(boolean bl) { missing = bl; }

   /**
    * Returns the String representation of this element.
    *
    * @return String representation of this element
    */
   public String toString() {

      if (missing) {
         return "Missing";
      }

      if (empty) {
         return "Empty";
      }

      if (!exist) {
         return "Does not exist";
      }

      if (_default) {
         return "Default";
      }

      if (obj instanceof char[]) {
         return new String((char[]) obj);
      }

      if (obj instanceof byte[]) {
         return new String((byte[]) obj);
      }

      return obj.toString();
   } // end method toString


} // end class Element
