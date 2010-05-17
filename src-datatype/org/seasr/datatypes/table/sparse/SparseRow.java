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

package org.seasr.datatypes.table.sparse;

//===============
// Other Imports
//===============
import java.io.Serializable;

import org.seasr.datatypes.table.Row;
import org.seasr.datatypes.table.Table;
/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 *

 */
public class SparseRow implements Row, Serializable {

        /** this is the index of the row to access. */
        protected int index;

        /** this is the example table we are accessing. */
        private SparseTable table;

        /** columns in total. */
//        private Column [] columns;

        public SparseRow () {
        }

        public SparseRow (SparseTable et) {
                table = et;
//                columns = table.getColumns();
        }

        /**
         * Return the table this row is in.
         * @return the table this row is in.
         */
        public Table getTable() {
                return table;
        }

        /**
         * This could potentially be subindexed.
         * @param i
         */
        public void setIndex(int i) {
                this.index = i;
        }
        /**
         * Get the ith input as a double.
         * @param i the input index
         * @return the ith input as a double
         */
        final public double getDouble(int i) {
                return table.getColumn(i).getDouble(index);
        }

        /**
         * Get the ith input as a String.
         * @param i the input index
         * @return the ith input as a String
         *
         *
         */
        final public String getString(int i) {
                return table.getColumn(i).getString(index);
                //return table.getString(i,index);

        }

        /**
         * Get the ith input as an int.
         * @param i the input index
         * @return the ith input as an int
         */
        final public int getInt(int i) {
                return table.getColumn(i).getInt(index);
        }

        /**
         * Get the ith input as a float.
         * @param i the input index
         * @return the ith input as a float
         */
        final public float getFloat(int i) {
                return table.getColumn(i).getFloat(index);
        }

        /**
         * Get the ith input as a short.
         * @param i the input index
         * @return the ith input as a short
         */
        final public short getShort(int i) {
                return table.getColumn(i).getShort(index);
        }

        /**
         * Get the ith input as a long.
         * @param i the input index
         * @return the ith input as a long
         */
        final public long getLong(int i) {
                return table.getColumn(i).getLong(index);
        }

        /**
         * Get the ith input as a byte.
         * @param i the input index
         * @return the ith input as a byte
         */
        final public byte getByte(int i) {
                return table.getColumn(i).getByte(index);
        }

        /**
         * Get the ith input as an Object.
         * @param i the input index
         * @return the ith input as an Object.
         */
        final public Object getObject(int i) {
                return table.getColumn(i).getObject(index);
        }

        /**
         * Get the ith input as a char.
         * @param i the input index
         * @return the ith input as a char
         */
        final public char getChar(int i) {
                return table.getColumn(i).getChar(index);
        }

        /**
         * Get the ith input as chars.
         * @param i the input index
         * @return the ith input as chars
         */
        final public char[] getChars(int i) {
                return table.getColumn(i).getChars(index);
        }

        /**
         * Get the ith input as bytes.
         * @param i the input index
         * @return the ith input as bytes.
         */
        final public byte[] getBytes(int i) {
                return table.getColumn(i).getBytes(index);
        }

        /**
         * Get the ith input as a boolean.
         * @param i the input index
         * @return the ith input as a boolean
         */
        final public boolean getBoolean(int i) {
                return table.getColumn(i).getBoolean(index);
        }
}
