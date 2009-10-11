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

import org.seasr.datatypes.table.*;
import org.seasr.datatypes.table.sparse.columns.*;
import org.seasr.datatypes.table.sparse.primitivehash.*;


/**
 * Useful methods that are used on <code>Columns</code>.
 *
 * @author  suvalala
 * @author  redman
 * @author  goren
 * @author  $Author: shirk $
 * @version $Revision: 1.12 $, $Date: 2006/08/01 19:24:41 $
 */
public final class ColumnUtilities {

   //~ Methods *****************************************************************

   /**
    * Copy the missing values from one <code>Column</code> to another.
    *
    * @param from <code>Column</code> to copy missing values from.
    * @param to   <code>Column</code> to copy the missing values to.
    */
   static private void copyMissingValues(Column from, Column to) {

      if (from instanceof MissingValuesColumn) {
         boolean[] orig = ((MissingValuesColumn) from).getMissingValues();
         boolean[] copy = new boolean[orig.length];
         System.arraycopy(orig, 0, copy, 0, copy.length);
         ((MissingValuesColumn) to).setMissingValues(copy);
      }

      if (from instanceof AbstractSparseColumn) {
         VIntHashSet missing = ((AbstractSparseColumn) from).getMissing();
         ((AbstractSparseColumn) to).setMissing(missing.toArray());
      }
   }

   /**
    * Copies the <code>Column</code> data in a <code>Table</code> and returns it
    * as a <code>Column</code>.
    *
    * @param  sourceTable <code>Table</code> to copy the <code>Column</code> out
    *                     of
    * @param  colIndex    Which <code>Column</code> in the table to copy
    *
    * @return Copied column
    */
   static public Column copyColumn(Table sourceTable, int colIndex) {
      int type = sourceTable.getColumnType(colIndex);
      int numRows = sourceTable.getNumRows();
      Column c;

      switch (type) {

         case (ColumnTypes.INTEGER): {
            c = sourceTable.getColumn(colIndex);
            c = new IntColumn((int[]) c.getInternal());

            break;
         }

         case (ColumnTypes.FLOAT): {
            c = sourceTable.getColumn(colIndex);
            c = new FloatColumn((float[]) c.getInternal());

            break;
         }

         case (ColumnTypes.SHORT): {
            c = sourceTable.getColumn(colIndex);
            c = new ShortColumn((short[]) c.getInternal());

            break;
         }

         case (ColumnTypes.LONG): {
            c = sourceTable.getColumn(colIndex);
            c = new LongColumn((long[]) c.getInternal());

            break;
         }

         case (ColumnTypes.STRING): {
            c = sourceTable.getColumn(colIndex);
            c = new StringColumn((String[]) c.getInternal());

            break;
         }

         case (ColumnTypes.CHAR_ARRAY): {
            c = sourceTable.getColumn(colIndex);
            c = new CharArrayColumn((char[][]) c.getInternal());

            break;
         }

         case (ColumnTypes.BYTE_ARRAY): {
            c = sourceTable.getColumn(colIndex);
            c = new ByteArrayColumn((byte[][]) c.getInternal());

            break;
         }

         case (ColumnTypes.BOOLEAN): {
            c = sourceTable.getColumn(colIndex);
            c = new BooleanColumn((boolean[]) c.getInternal());

            break;
         }

         case (ColumnTypes.OBJECT): {
            c = sourceTable.getColumn(colIndex);
            c = new ObjectColumn((Object[]) c.getInternal());

            break;
         }

         case (ColumnTypes.BYTE): {
            c = sourceTable.getColumn(colIndex);
            c = new ByteColumn((byte[]) c.getInternal());

            break;
         }

         case (ColumnTypes.CHAR): {
            c = sourceTable.getColumn(colIndex);
            c = new CharColumn((char[]) c.getInternal());

            break;
         }

         case (ColumnTypes.DOUBLE): {
            c = sourceTable.getColumn(colIndex);
            c = new DoubleColumn((double[]) c.getInternal());

            break;

         }

         default: {
            System.err.println("ColumnUtilities:CopyColumn: Invalid Column Type");
            c = new ObjectColumn(numRows);
         }
      }

      c.setLabel(sourceTable.getColumnLabel(colIndex));
      c.setComment(sourceTable.getColumnComment(colIndex));

      return c;
   } // end method copyColumn

   /**
    * Creates a <code>Column</code> given the type and size.
    *
    * @param  type Type of <code>Column</code> to create
    * @param  size Initial size of the <code>Column</code>
    *
    * @return A new, empty <code>Column</code>
    */
   static public Column createColumn(String type, int size) {

      if (type.compareToIgnoreCase("String") == 0) {
         return new StringColumn(size);
      } else if (type.compareToIgnoreCase("float") == 0) {
         return new FloatColumn(size);
      } else if (type.compareToIgnoreCase("double") == 0) {
         return new DoubleColumn(size);
      } else if (type.compareToIgnoreCase("int") == 0) {
         return new IntColumn(size);
      } else if (type.compareToIgnoreCase("boolean") == 0) {
         return new BooleanColumn(size);
      } else if (type.compareToIgnoreCase("char[]") == 0) {
         return new ContinuousCharArrayColumn(size);
      } else if (type.compareToIgnoreCase("byte[]") == 0) {
         return new ContinuousByteArrayColumn(size);
      } else if (type.compareToIgnoreCase("long") == 0) {
         return new LongColumn(size);
      } else if (type.compareToIgnoreCase("short") == 0) {
         return new ShortColumn(size);
      } else {
         return new StringColumn(size);
      }
   }

   /**
    * Creates a <code>Column</code> given the type and size.
    *
    * @param  type Type of <code>Column</code> to create. Use one of the types
    *              available in the <code>ColummTypes</code> class
    * @param  size Initial size of the <code>Column</code>
    *
    * @return A new, empty <code>Column</code>
    */
   static public Column createColumn(int type, int size) {
      Column c;

      switch (type) {

         case (ColumnTypes.INTEGER): {
            c = new IntColumn(size);

            break;
         }

         case (ColumnTypes.FLOAT): {
            c = new FloatColumn(size);

            break;
         }

         case (ColumnTypes.SHORT): {
            c = new ShortColumn(size);

            break;
         }

         case (ColumnTypes.LONG): {
            c = new LongColumn(size);

            break;
         }

         case (ColumnTypes.STRING): {
            c = new StringColumn(size);

            break;
         }

         case (ColumnTypes.CHAR_ARRAY): {
            c = new CharArrayColumn(size);

            break;
         }

         case (ColumnTypes.BYTE_ARRAY): {
            c = new ByteArrayColumn(size);

            break;
         }

         case (ColumnTypes.BOOLEAN): {
            c = new BooleanColumn(size);

            break;
         }

         case (ColumnTypes.OBJECT): {
            c = new ObjectColumn(size);

            break;
         }

         case (ColumnTypes.BYTE): {
            c = new ByteColumn(size);

            break;
         }

         case (ColumnTypes.CHAR): {
            c = new CharColumn(size);

            break;
         }

         case (ColumnTypes.DOUBLE): {
            c = new DoubleColumn(size);

            break;

         }

         case (ColumnTypes.NOMINAL): {
            c = new StringColumn(size);

            break;

         }

         default: {

            c = new StringObjectColumn(size);
         }
      }

      c.setLabel("");

      return c;
   } // end method createColumn

   /**
    * Creates a subset from a <code>Table</code> instance and puts it into a new
    * <code>Column</code>.
    *
    * @param  tbl      The original table
    * @param  colIndex Which <code>Column</code> to make a subset of
    * @param  subset   Indices of the rows from the original <code>Column</code>
    *                  to put in the new <code>Column</code>
    *
    * @return New <code>Column</code> object of the same datatype as the
    *         original <code>Column</code> of <code>tbl</code>
    */
   static public Column createColumnSubset(Table tbl,
                                           int colIndex,
                                           int[] subset) {
      int type = tbl.getColumnType(colIndex);
      int size = subset.length;

      Column col = ColumnUtilities.createColumn(type, size);
      col.setLabel(tbl.getColumnLabel(colIndex));
      col.setComment(tbl.getColumnLabel(colIndex));

      switch (type) {

         case (ColumnTypes.DOUBLE): {

            for (int i = 0; i < size; i++) {
               col.setDouble(tbl.getDouble(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.INTEGER): {

            for (int i = 0; i < size; i++) {
               col.setInt(tbl.getInt(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.FLOAT): {

            for (int i = 0; i < size; i++) {
               col.setFloat(tbl.getFloat(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.SHORT): {

            for (int i = 0; i < size; i++) {
               col.setShort(tbl.getShort(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.LONG): {

            for (int i = 0; i < size; i++) {
               col.setLong(tbl.getLong(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.STRING): {

            for (int i = 0; i < size; i++) {
               col.setString(tbl.getString(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.CHAR_ARRAY): {

            for (int i = 0; i < size; i++) {
               col.setChars(tbl.getChars(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.BYTE_ARRAY): {

            for (int i = 0; i < size; i++) {
               col.setBytes(tbl.getBytes(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.BOOLEAN): {

            for (int i = 0; i < size; i++) {
               col.setBoolean(tbl.getBoolean(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.OBJECT): {

            for (int i = 0; i < size; i++) {
               col.setObject(tbl.getObject(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.BYTE): {

            for (int i = 0; i < size; i++) {
               col.setByte(tbl.getByte(subset[i], colIndex), i);
            }

            break;
         }

         case (ColumnTypes.CHAR): {

            for (int i = 0; i < size; i++) {
               col.setChar(tbl.getChar(subset[i], colIndex), i);
            }

            break;
         }

         default: { }
      }

      return col;

   } // end method createColumnSubset

   /* DONT DELETE THIS! every function needs to cut and
    *   paste this switch
    *
    * switch(type){          case (ColumnTypes.DOUBLE) : { break;          } case
    * (ColumnTypes.INTEGER) : { break;          }          case
    * (ColumnTypes.FLOAT) : { break;          }          case
    * (ColumnTypes.SHORT) : { break;          }          case (ColumnTypes.LONG)
    * : { break;          }          case (ColumnTypes.STRING) : { break;  }
    * case (ColumnTypes.CHAR_ARRAY) : {   break;          } case
    * (ColumnTypes.BYTE_ARRAY) : {      break;          }          case
    * (ColumnTypes.BOOLEAN) : {      break;          }          case
    * (ColumnTypes.OBJECT) : {     break;          }          case
    * (ColumnTypes.BYTE) : {  break;          }          case (ColumnTypes.CHAR)
    * : { break;          }          default : {          }  }
    */

   /**
    * Tests if all the items in the specified <code>Column</code> can be
    * represented numerically.
    *
    * @param  column The <code>Column</code> to test
    *
    * @return True if <code>Column</code> contains only numeric data, false
    *         otherwise
    */
   static public boolean isNumericColumn(Column column) {
      int numRows = column.getNumRows();

      for (int row = 0; row < numRows; row++) {

         try {
            Double d = Double.valueOf(column.getString(row));
         } catch (Exception e) {
            return false;
         }
      }

      return true;

   }

   /**
    * Creates a new <code>BooleanColumn</code> with a copy of the data from
    * <code>sc</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from <code>sc</code>
    */
   static public Column toBooleanColumn(Column sc) {
      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseBooleanColumn(sc.getNumEntries());
      } else {
         dc = new BooleanColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         if (!sc.isValueMissing(i)) {
            dc.setBoolean(sc.getBoolean(i), i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   }

   /**
    * Creates a new <code>ByteArrayColumn</code> with the data from <code>
    * sc</code>. The Objects in the new <code>Column</code> are references to
    * the Objects in the original <code>Column</code>.
    *
    * @param  sc The original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from <code>sc</code>
    */
   static public Column toByteArrayColumn(Column sc) {
      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseByteArrayColumn(sc.getNumEntries());
      } else {
         dc = new ByteArrayColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         if (!sc.isValueMissing(i)) {
            dc.setBytes(sc.getBytes(i), i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   }

   /**
    * Creates a new <code>ByteColumn</code> with the data from <code>sc</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return a <code>Column</code> initialized with the data from <code>
    *         sc</code>
    */
   static public Column toByteColumn(Column sc) {
      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseByteColumn(sc.getNumEntries());
      } else {
         dc = new ByteColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         if (!sc.isValueMissing(i)) {
            dc.setByte(sc.getByte(i), i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   }

   /**
    * Creates a new <code>CharArrayColumn</code> with the data from <code>
    * sc</code>. The Objects in the new <code>Column</code> are references to
    * the Objects in the original <code>Column</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from sc
    */
   static public Column toCharArrayColumn(Column sc) {

      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseCharArrayColumn(sc.getNumEntries());
      } else {
         dc = new CharArrayColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         if (!sc.isValueMissing(i)) {
            dc.setChars(sc.getChars(i), i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   }

   /**
    * Creates a new <code>CharColumn</code> with the data from <code>sc</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from <code>sc</code>
    */
   static public Column toCharColumn(Column sc) {

      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseCharColumn(sc.getNumEntries());
      } else {
         dc = new CharColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         if (!sc.isValueMissing(i)) {
            dc.setChar(sc.getChar(i), i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   }

   /**
    * Creates a new <code>DoubleColumn</code> with a copy of the data from
    * <code>sc</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from <code>sc</code>
    */
   static public Column toDoubleColumn(Column sc) {
      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseDoubleColumn(sc.getNumEntries());
      } else {
         dc = new DoubleColumn(sc.getNumRows());
      }

      try {

         for (int i = 0; i < sc.getNumRows(); i++) {

            if (!sc.isValueMissing(i)) {
               dc.setDouble(sc.getDouble(i), i);
            }
         }
      } catch (NumberFormatException nfe) {

         // This column is not numberic.
         for (int i = 0; i < sc.getNumRows(); i++) {
            dc.setDouble(0.0, i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   } // end method toDoubleColumn

   /**
    * Creates a new <code>FloatColumn</code> with a copy of the data from <code>
    * sc</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from <code>sc</code>
    */
   static public Column toFloatColumn(Column sc) {
      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseFloatColumn(sc.getNumEntries());
      } else {
         dc = new FloatColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         try {

            if (!sc.isValueMissing(i)) {
               dc.setFloat(sc.getFloat(i), i);
            }
         } catch (NumberFormatException nfe) {

            // This column is not numberic.
            dc.setFloat((float) 0.0, i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   } // end method toFloatColumn

   /**
    * Creates a new <code>IntColumn</code> with a copy of the data from <code>
    * sc</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return a <code>Column</code> initialized with the data from <code>
    *         sc</code>
    */
   static public Column toIntColumn(Column sc) {
      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseIntColumn(sc.getNumEntries());
      } else {
         dc = new IntColumn(sc.getNumRows());
      }


      for (int i = 0; i < sc.getNumRows(); i++) {

         try {

            if (!sc.isValueMissing(i)) {
               dc.setInt(sc.getInt(i), i);
            }
         } catch (NumberFormatException nfe) {
            dc.setInt(0, i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   } // end method toIntColumn

   /**
    * Creates a new <code>LongColumn</code> with a copy of the data from <code>
    * sc</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from sc
    */
   static public Column toLongColumn(Column sc) {
      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseLongColumn(sc.getNumEntries());
      } else {
         dc = new LongColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         try {

            if (!sc.isValueMissing(i)) {
               dc.setLong(sc.getLong(i), i);
            }
         } catch (NumberFormatException nfe) {

            // This column is not numberic.
            dc.setLong(0, i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   } // end method toLongColumn

   /**
    * Creates a new <code>ObjectColumn</code> with the data from <code>
    * sc</code>. The Objects in the new <code>Column</code> are references to
    * the Objects in the original <code>Column</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from <code>sc</code>
    */
   static public Column toObjectColumn(Column sc) {

      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseObjectColumn(sc.getNumEntries());
      } else {
         dc = new ObjectColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         if (!sc.isValueMissing(i)) {
            dc.setObject(sc.getObject(i), i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   }

   /**
    * Creates a new <code>ShortColumn</code> with a copy of the data from <code>
    * sc</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from <code>sc</code>
    */
   static public Column toShortColumn(Column sc) {

      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseShortColumn(sc.getNumEntries());
      } else {
         dc = new ShortColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         try {

            if (!sc.isValueMissing(i)) {
               dc.setShort(sc.getShort(i), i);
            }
         } catch (NumberFormatException nfe) {

            // This column is not numberic.
            dc.setShort((short) 0, i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   } // end method toShortColumn

   /**
    * Creates a new <code>StringColumn</code> with the data from <code>sc</code>.
    * The Objects in the new <code>Column</code> are references to the Objects
    * in the original <code>Column</code>.
    *
    * @param  sc Original <code>Column</code>
    *
    * @return <code>Column</code> initialized with the data from <code>
    *         sc</code>
    */
   static public Column toStringColumn(Column sc) {
      Column dc = null;

      if (sc instanceof AbstractSparseColumn) {
         dc = new SparseStringColumn(sc.getNumEntries());
      } else {
         dc = new StringColumn(sc.getNumRows());
      }

      for (int i = 0; i < sc.getNumRows(); i++) {

         if (!sc.isValueMissing(i)) {
            dc.setString(sc.getString(i), i);
         }
      }

      dc.setLabel(sc.getLabel());
      dc.setComment(sc.getComment());
      ColumnUtilities.copyMissingValues(sc, dc);

      return dc;
   }
} // end class ColumnUtilities
