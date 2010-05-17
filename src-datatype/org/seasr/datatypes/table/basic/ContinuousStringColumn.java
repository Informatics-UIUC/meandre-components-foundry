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

package org.seasr.datatypes.table.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.MutableTable;


/**
 * <p>A TextualColumn that keeps its data as a continuous array of chars. This
 * minimizes the space requirements to hold the data. The data is kept as a
 * buffer of chars with a secondary array of pointers into the buffer.</p>
 *
 * <p>This column is efficient in storing and retrieving textual data. Insertion
 * and deletion methods may require an expansion or compaction of the internal
 * buffer.</p>
 *
 * <p>The buffer will allocate extra space when an insertion requires more space
 * than the size of the buffer. This extra space can be removed using the trim()
 * method.</p>
 *
 * <p>The buffer will compact itself when a row is removed from this column. The
 * space freed up from the removal will not be freed until trim() is called.</p>
 *
 * @author  $Author: shirk $
 * @version $Revision: 1.4 $, $Date: 2006/08/03 19:42:50 $
 */
public final class ContinuousStringColumn extends ContinuousCharArrayColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 3909060290164419627L;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>ContinuousStringColumn</code> with zero rows and
    * default size for the internal buffer.
    */
   public ContinuousStringColumn() {
      this(0, DEFAULT_INITIAL_SIZE);
      type = ColumnTypes.STRING;
   }

   /**
    * Creates a new <code>ContinuousStringColumn</code> with the specified
    * number of rows and the default initial size for the internal buffer.
    *
    * @param initialLength Initial number of rows
    */
   public ContinuousStringColumn(int initialLength) {
      this(initialLength, DEFAULT_INITIAL_SIZE, false);
      type = ColumnTypes.STRING;
   }

   /**
    * Creates a new <code>ContinuousStringColumn</code> object.
    *
    * @param fill True if each row should be filled with a blank entry
    */
   public ContinuousStringColumn(boolean fill) {
      this(0, DEFAULT_INITIAL_SIZE, fill);
      type = ColumnTypes.STRING;
   }

   /**
    * Creates a new <code>ContinuousStringColumn</code> and insert all items in
    * the internal buffer.
    *
    * @param intern Strings to insert
    */
   public ContinuousStringColumn(String[] intern) {
      this();

      for (int i = 0; i < intern.length; i++) {
         setString(intern[i], i);
      }

      type = ColumnTypes.STRING;
   }

   /**
    * Creates a new <code>ContinuousStringColumn</code>.
    *
    * @param initialLength Initial number of rows
    * @param initialSize   Initial size of the internal buffer
    */
   public ContinuousStringColumn(int initialLength, int initialSize) {
      super(initialLength, initialSize, false);
      type = ColumnTypes.STRING;
   }

   /**
    * Creates a new <code>ContinuousStringColumn</code> with the specified
    * number of rows and the default initial size for the internal buffer.
    *
    * @param initialLength Initial number of rows
    * @param fill          True if each row should be filled with a blank entry
    */
   public ContinuousStringColumn(int initialLength, boolean fill) {
      this(initialLength, DEFAULT_INITIAL_SIZE, fill);
      type = ColumnTypes.STRING;
   }

   /**
    * Creates a new <code>ContinuousStringColumn</code>.
    *
    * @param initialLength Initial number of rows
    * @param initialSize   Initial size of the internal buffer
    * @param fill          True if each row should be filled with a blank entry
    */
   public ContinuousStringColumn(int initialLength, int initialSize,
                                 boolean fill) {
      super(initialLength, initialSize, fill);
      type = ColumnTypes.STRING;
   }

   //~ Methods *****************************************************************

   /**
    * Compares two Strings.
    *
    * @param  s1 First String to compare
    * @param  s2 Second String to compare
    *
    * @return -1, 0, 1
    */
   private int compareStrings(String s1, String s2) { return s1.compareTo(s2); }

   /**
    * Rearranges the subarray A[p..r] in place.
    *
    * @param  p Beginning index
    * @param  r Ending index
    * @param  t <code>MutableTable</code> to swap rows for
    *
    * @return New partition point
    */
   protected int partition(int p, int r, MutableTable t) {
      boolean xMissing = this.isValueMissing(p);
      int i = p - 1;
      int j = r + 1;

      while (true) {

         if (xMissing) {
            j--;

            do {
               i++;
            } while (!this.isValueMissing(i));
         } else {

            do {
               j--;
            } while (this.isValueMissing(j) || compareRows(j, p) > 0);

            do {
               i++;
            } while (!this.isValueMissing(i) && compareRows(i, p) < 0);
         }

         if (i < j) {

            if (t == null) {
               this.swapRows(i, j);
            } else {
               t.swapRows(i, j);
            }
         } else {
            return j;
         }
      } // end while
   } // end method partition

   /**
    * Appends the new entry to the end of the Column.
    *
    * @param newEntry A new entry
    */
   @Override
public void addRow(Object newEntry) {

      if (newEntry instanceof byte[]) {
         appendChars(toCharArray((byte[]) newEntry));
      } else if (newEntry instanceof char[]) {
         appendChars((char[]) newEntry);
      } else {
         appendChars(newEntry.toString().toCharArray());
      }
   }

   /**
    * Compares the values of the element passed in and pos. Return 0 if they are
    * the same, greater than 0 if element is greater, and less than 0 if element
    * is less.
    *
    * @param  element Element to be passed in and compared
    * @param  pos     Position of the element in Column to be compare with
    *
    * @return Value representing the relationship- >, <, or == 0
    */
   @Override
public int compareRows(Object element, int pos) {
      return compareStrings(element.toString(), getString(pos));
   }

   /**
    * Compares pos1 and pos2 positions in the Column. Return 0 if they are the
    * same, greater than 0 if pos1 is greater, and less than 0 if pos1 is less.
    *
    * @param  pos1 Position of the first element to compare
    * @param  pos2 Position of the second element to compare
    *
    * @return Value representing the relationship- >, <, or == 0
    */
   @Override
public int compareRows(int pos1, int pos2) {
      return compareStrings(getString(pos1), getString(pos2));
   }

   /**
    * Creates an exact copy of this column.
    *
    * @return A copy of this column
    */
   @Override
public Column copy() {
      ContinuousStringColumn bac;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         bac = (ContinuousStringColumn) ois.readObject();
         ois.close();

         return bac;
      } catch (Exception e) {
         bac = new ContinuousStringColumn(getNumRows());

         for (int i = 0; i < getNumRows(); i++) {
            String orig = getString(i);
            bac.setString(new String(orig), i);
         }

         bac.setLabel(getLabel());
         bac.setComment(getComment());

         return bac;
      }
   } // end method copy

   /**
    * Gets the value at row <code>i</code> as an <code>Object</code>.
    *
    * @param  i Position to get the value from
    *
    * @return Value at <code>i</code> as an <code>Object</code>
    */
   @Override
public Object getObject(int i) { return getString(i); }

   /**
    * Gets an entry from the <code>Column</code> at the indicated position.
    *
    * @param  i Position to get the entry from
    *
    * @return Entry at the specified position
    */
   @Override
public Object getRow(int i) { return getString(i); }

   /**
    * Removes an entry from the Column, at <code>row</code>. All entries from
    * <code>row</code>+1 will be moved back 1 position
    *
    * @param  row Position to remove
    *
    * @return Removed object
    */
   @Override
public Object removeRow(int row) {
      char[] removed = (char[]) super.removeRow(row);

      return new String(removed);
   }

   /**
    * Sets the entry at <code>pos</code> to be <code>o</code>. If <code>o</code>
    * is a String, set it. If newEntry is a byte[] or char[], converts them to
    * String and insert. Otherwise, calls to toString() method on <code>o</code>
    * and inserts that String.
    *
    * @param o   New item
    * @param row Position to set the entry
    */
   @Override
public void setObject(Object o, int row) {

      if (o instanceof byte[]) {
         setBytes((byte[]) o, row);
      } else if (o instanceof char[]) {
         setChars((char[]) o, row);
      } else {
         setChars(o.toString().toCharArray(), row);
      }
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   @Override
public void setRow(Object newEntry, int pos) { setObject(newEntry, pos); }
} // end class ContinuousStringColumn
