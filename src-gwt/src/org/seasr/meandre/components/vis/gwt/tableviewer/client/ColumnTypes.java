/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.seasr.meandre.components.vis.gwt.tableviewer.client;

/**
 * Taken from <code>org.seasr.datatypes.table.ColumnTypes</code>
 *
 * @author Boris Capitanu
 *
 */
public final class ColumnTypes {

    /** A column of integer values. */
    static public final int INTEGER = 0;

    /** A column of float values. */
    static public final int FLOAT = 1;

    /** A column of double values. */
    static public final int DOUBLE = 2;

    /** A column of short values. */
    static public final int SHORT = 3;

    /** A column of long values. */
    static public final int LONG = 4;

    /** A column of String values. */
    static public final int STRING = 5;

    /** A column of char[] values. */
    static public final int CHAR_ARRAY = 6;

    /** A column of byte[] values. */
    static public final int BYTE_ARRAY = 7;

    /** A column of boolean values. */
    static public final int BOOLEAN = 8;

    /** A column of Object values. */
    static public final int OBJECT = 9;

    /** A column of byte values. */
    static public final int BYTE = 10;

    /** A column of char values. */
    static public final int CHAR = 11;

    /** A column of char values. */
    static public final int NOMINAL = 12;

    /** A column of unspecified values -- used for sparse tables. */
    static public final int UNSPECIFIED = 13;

}
