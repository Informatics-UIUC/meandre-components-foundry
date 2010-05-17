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

package org.seasr.meandre.support.components.discovery.cluster;

/**
 * <p> Title: ClusterModel </p> <p> Description: The state in this module
 * represents model information for a clustering of a dataset. </p> <p>
 * Copyright: Copyright (c) 2003 </p> <p> Company: NCSA Automated Learning Group
 * </p>
 *
 * @author D. Searmith
 * @version 1.0
 */

//==============
// Java Imports
//==============

import java.util.logging.Logger;

// ===============
// Other Imports
// ===============

import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.Row;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.datatypes.datamining.table.basic.MutableTableImpl;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Description of class ClusterModel.
 *
 * @author  $Author: mcgrath $
 * @version $Revision: 1.10 $, $Date: 2007/05/21 17:59:27 $
 */
public class ClusterModel implements Table, Serializable {

    //~ Static fields/initializers **********************************************

    //~ Instance fields *********************************************************

    /** Holds TableCluster objects. */
    private ArrayList _clusters = null;

    /** The root of the bottom-up clustering. an object in _clusters */
    private TableCluster _root = null;

    /** Holds the clustered data of this model. */
    private Table _table = null;

    /** Default missing string. */
    protected boolean defaultMissingBoolean = false;

    /** Default missing string. */
    protected byte defaultMissingByte = (byte) '\000';

    /** Default missing string. */
    protected byte[] defaultMissingByteArray = {(byte) '\000'};

    /** Default missing string. */
    protected char defaultMissingChar = '\000';

    /** Default missing string. */
    protected char[] defaultMissingCharArray = {'\000'};

    /** Default for float double and extended. */
    protected double defaultMissingDouble = 0.0;

    /** The missing value for longs, ints, and shorts. */
    protected int defaultMissingInt = 0;

    /** Default missing string. */
    protected String defaultMissingString = "?";

    //~ Constructors ************************************************************

    /**
     *
     * @param clusters Description of parameter clusters.
     */
    public ClusterModel(ArrayList clusters) {
        _clusters = clusters;
    }

    /**
     * Creates a new ClusterModel object.
     *
     * @param clusters Description of parameter clusters.
     * @param root     Description of parameter root.
     */
    public ClusterModel(ArrayList clusters, TableCluster root) {
        _clusters = clusters;
        _root = root;
    }

    /**
     * Creates a new ClusterModel object.
     *
     * @param tab      Description of parameter tab.
     * @param clusters Description of parameter clusters.
     * @param root     Description of parameter root.
     */
    public ClusterModel(Table tab, ArrayList clusters, TableCluster root) {
        _table = tab;
        _clusters = clusters;
        _root = root;
    }

    //~ Methods *****************************************************************
    private Logger myLogger = Logger.getLogger("ClusterModel");

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public Table copy() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.copy() -- Model does not " +
                            "contain table data -- will return null");

            return null;
        } else {
            return _table.copy();
        }
    }

    /**
     * Create a copy of this Table. This is a deep copy, and it contains a copy
     * of all the data.
     *
     * @param  rows Description of parameter rows.
     *
     * @return a copy of this Table
     */
    public Table copy(int[] rows) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.copy(int [] rows) -- Model " +
                            "does not contain table data -- will return null");

            return null;
        } else {
            return _table.copy(rows);
        }
    }

    /**
     * Create a copy of this Table. This is a deep copy, and it contains a copy
     * of all the data.
     *
     * @param  start Description of parameter start.
     * @param  len   Description of parameter len.
     *
     * @return a copy of this Table
     */
    public Table copy(int start, int len) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.copy(int start, int len) --" +
                            "Model does not contain table data -- will return null");

            return null;
        } else {
            return _table.copy(start, len);
        }
    }

    /**
     * Creates an empty MutableTable and returns it.
     *
     * @return MutableTable and empty Mutable Table
     */
    public MutableTable createTable() {
        return new MutableTableImpl().createTable();
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public boolean getBoolean(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getBoolean(...) -- Model does not contain " +
                           "table data and will return false");

            return false;
        } else {
            return _table.getBoolean(row, column);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public byte getByte(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getByte(...) -- Model does not contain " +
                           "table data and will return Byte.MIN_VALUE");

            return Byte.MIN_VALUE;
        } else {
            return _table.getByte(row, column);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public byte[] getBytes(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getBytes(...) -- Model does not contain " +
                           "table data and will return null");

            return null;
        } else {
            return _table.getBytes(row, column);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public char getChar(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getChar(...) -- Model does not contain " +
                           "table data and will return Character.MIN_VALUE");

            return Character.MIN_VALUE;
        } else {
            return _table.getChar(row, column);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public char[] getChars(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getChars(...) -- Model does not contain " +
                           "table data and will return null");

            return null;
        } else {
            return _table.getChars(row, column);
        }
    }

    /**
     * Get an ArrayList of the clusters in this model.
     *
     * @return ArrayList
     */
    public ArrayList getClusters() {
        return _clusters;
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  position Description of parameter position.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */

    public Column getColumn(int position) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getColumn(...) -- Model does not contain"
                           + " table data.");

            return null;
        } else {
            return _table.getColumn(position);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  position Description of parameter position.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public String getColumnComment(int position) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getColumnComment(...) -- Model does not " +
                           "contain table data -- will return null");

            return null;
        } else {
            return _table.getColumnComment(position);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  position Description of parameter position.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public String getColumnLabel(int position) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getColumnLabel(...) -- Model does not " +
                           "contain table data -- will return null");

            return null;
        } else {
            return _table.getColumnLabel(position);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  position Description of parameter position.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public int getColumnType(int position) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getColumnType(...) -- Model does not contain " +
                           " table data and will return Integer.MIN_VALUE");

            return Integer.MIN_VALUE;
        } else {
            return _table.getColumnType(position);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public String getComment() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getComment() -- Model does not contain table" +
                           " data -- will return null");

            return null;
        } else {
            return _table.getComment();
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public double getDouble(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getDouble(...) -- Model does not contain " +
                           "table data and will return Double.MIN_VALUE");

            return Double.MIN_VALUE;
        } else {
            return _table.getDouble(row, column);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public float getFloat(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getFloat(...) -- Model does not contain " +
                           "table data and will return Float.MIN_VALUE");

            return Float.MIN_VALUE;
        } else {
            return _table.getFloat(row, column);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public int getInt(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getInt(...) -- Model does not contain " +
                           "table data and will return Integer.MIN_VALUE");

            return Integer.MIN_VALUE;
        } else {
            return _table.getInt(row, column);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public String getLabel() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getLabel() -- Model does not contain table" +
                           " data -- will return null");

            return null;
        } else {
            return _table.getLabel();
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public long getLong(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getLong(...) -- Model does not contain table " +
                           "data and will return Long.MIN_VALUE");

            return Long.MIN_VALUE;
        } else {
            return _table.getLong(row, column);
        }
    }

    /**
     * Return the default missing value for doubles, floats and extendeds.
     *
     * @return the double for missing value.
     */
    public boolean getMissingBoolean() {
        return defaultMissingBoolean;
    }

    /**
     * Return the default missing value for doubles, floats and extendeds.
     *
     * @return the double for missing value.
     */
    public byte getMissingByte() {
        return defaultMissingByte;
    }

    /**
     * Return the default missing value for doubles, floats and extendeds.
     *
     * @return the double for missing value.
     */
    public byte[] getMissingBytes() {
        return this.defaultMissingByteArray;
    }

    /**
     * Return the default missing value for doubles, floats and extendeds.
     *
     * @return the double for missing value.
     */
    public char getMissingChar() {
        return this.defaultMissingChar;
    }

    /**
     * Return the default missing value for doubles, floats and extendeds.
     *
     * @return the double for missing value.
     */
    public char[] getMissingChars() {
        return this.defaultMissingCharArray;
    }

    /**
     * Return the default missing value for doubles, floats and extendeds.
     *
     * @return the double for missing value.
     */
    public double getMissingDouble() {
        return this.defaultMissingDouble;
    }

    /**
     * Return the default missing value for integers, both short, int and long.
     *
     * @return the integer for missing value.
     */
    public int getMissingInt() {
        return defaultMissingInt;
    }

    /**
     * Return the default missing value for doubles, floats and extendeds.
     *
     * @return the double for missing value.
     */
    public String getMissingString() {
        return this.defaultMissingString;
    }

    /**
     * Describes the purpose of the module.
     *
     * @return <code>String</code> describing the purpose of the module.
     */
    public String getModuleInfo() {
        String s = "<p>Overview: ";
        s +=
                "This module encapsulates a model for a hierarchical, bottom-up " +
                "clustering.";
        s += "</p>";

        s += "<p>Detailed Description: ";
        s +=
                "The <i>Cluster Model</i> in this module contains the original " +
                "table of examples that were clustered, ";
        s +=
                "a tree of <i>Table Cluster</i> objects that was built during" +
                " the bottom-up ";
        s +=
                "clustering process, and a list of the clusters that were " +
                "created. The list of created ";
        s +=
                "clusters is a subset of the clusters from the cluster tree, " +
                "and represents a \"cut\" in ";
        s += "the tree.";
        s += "</p>";
        s +=
                "<p>The <i>Cluster Model</i> implements the Table interface.  It" +
                " delegates all table interface ";
        s +=
                "functions to the contained table.  As a result, it can be " +
                " passed to modules that take ";
        s += "a table as input, such as <i>TableViewer</i>.";
        s += "</p>";

        s += "<p>Scalability: ";
        s +=
                "The model scalability is limited principally by the size of the" +
                " example table it contains ";
        s += "and the memory limitations that follow from that.";
        s += "</p>";

        return s;
    } // end method getModuleInfo

    // ========================
    // D2K Abstract Overrides

    /**
     * Returns the name of the module that is appropriate for end user
     * consumption.
     *
     * @return The name of the module.
     */
    public String getModuleName() {
        return "Cluster Model";
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public int getNumColumns() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getNumColumns() -- Model does not contain " +
                           " table data -- will return Integer.MIN_VALUE");

            return Integer.MIN_VALUE;
        } else {
            return _table.getNumColumns();
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public int getNumEntries() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getNumEntries() -- Model does not contain " +
                           " table data -- will return Integer.MIN_VALUE");

            return Integer.MIN_VALUE;
        } else {
            return _table.getNumRows();
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public int getNumRows() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getNumRows() -- Model does not contain table" +
                           " data -- will return Integer.MIN_VALUE");

            return Integer.MIN_VALUE;
        } else {
            return _table.getNumRows();
        }
    }

    // =================================
    // Interface Implementation: Table
    // =================================

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public Object getObject(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getObject(...) --  " +
                           "Model does not contain table data and will " +
                           "return null");

            return null;
        } else {
            return _table.getObject(row, column);
        }
    }

    /**
     * Get the root node of the cluster tree.
     *
     * @return TableCluster
     */
    public TableCluster getRoot() {
        return _root;
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public Row getRow() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getRow(...) -- Model does not contain "
                           + " table data.");

            return null;
        } else {
            return (Row) _table.getRow();
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public short getShort(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getShort(...) -- Model does not contain " +
                           "table data and will return Short.MIN_VALUE");

            return Short.MIN_VALUE;
        } else {
            return _table.getShort(row, column);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row    Description of parameter row.
     * @param  column Description of parameter column.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public String getString(int row, int column) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getString(...) -- Model does not contain " +
                           "table data and will return \"\"");

            return "";
        } else {
            return _table.getString(row, column);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  rows Description of parameter rows.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public Table getSubset(int[] rows) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getSubset(...) -- Model does not contain table" +
                           " data and will return null");

            return null;
        } else {
            return _table.getSubset(rows);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  start Description of parameter start.
     * @param  len   Description of parameter len.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public Table getSubset(int start, int len) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getSubset(...) -- Model does not contain" +
                           " table data -- will return null");

            return null;
        } else {
            return _table.getSubset(start, len);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  rows Description of parameter rows.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public Table getSubsetByReference(int[] rows) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getSubsetByReference(...) -- Model does not " +
                           "contain table data and will return null");

            return null;
        } else {

            return _table.getSubset(rows);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  start Description of parameter start.
     * @param  len   Description of parameter len.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public Table getSubsetByReference(int start, int len) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getSubsetByReference(...) -- Model does not " +
                           "contain table data and will return null");

            return null;
        } else {

            return _table.getSubset(start, len);
        }
    }

    // ================
    // Public Methods
    // ================

    /**
     * Returns the table of cluster entities for this model.
     *
     * @return Table of cluster entities for this model.
     */
    public Table getTable() {
        return _table;
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public TableFactory getTableFactory() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.getTableFactory() -- Model does not contain" +
                           " table data -- will return null");

            return null;
        } else {
            return _table.getTableFactory();
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public boolean hasMissingValues() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.hasMissingValues() -- Model does not contain " +
                           "table data and will return false");

            return false;
        } else {
            return _table.hasMissingValues();
        }
    }

    /**
     * Description of return value.
     *
     * @param  columnIndex Description of parameter columnIndex.
     *
     * @return Description of return value.
     *
     * @see    ncsa.d2k.modules.core.datatype.table.Table#hasMissingValues(int)
     */
    public boolean hasMissingValues(int columnIndex) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.isValueMissing(...) -- Model does not contain" +
                           " table data and will return false");

            return false;
        } else {
            return _table.hasMissingValues(columnIndex);
        }
    }

    /**
     * Does this model have a root node (cluster tree).
     *
     * @return boolean
     */
    public boolean hasRoot() {
        return _root != null;
    }

    /**
     * Does this model contain a table.
     *
     * @return boolean
     */
    public boolean hasTable() {
        return _table != null;
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  position Description of parameter position.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public boolean isColumnNominal(int position) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.isColumnNominal(...) -- Model does not " +
                           " contain table data -- will return false");

            return false;
        } else {
            return _table.isColumnNominal(position);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  position Description of parameter position.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public boolean isColumnNumeric(int position) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.isColumnNumeric(...) -- Model does not contain " +
                           " table data and will return false");

            return false;
        } else {
            return _table.isColumnNumeric(position);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  position Description of parameter position.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public boolean isColumnScalar(int position) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.isColumnScalar(...) -- Model does not " +
                           " contain table data -- will return false");

            return false;
        } else {
            return _table.isColumnScalar(position);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row Description of parameter row.
     * @param  col Description of parameter col.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public boolean isValueEmpty(int row, int col) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.isValueEmpty(...) -- Model does not contain " +
                           "table data and will return false");

            return false;
        } else {
            return _table.isValueEmpty(row, col);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param  row Description of parameter row.
     * @param  col Description of parameter col.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public boolean isValueMissing(int row, int col) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.isValueMissing(...) -- Model does not contain" +
                           " table data and will return false");

            return false;
        } else {
            return _table.isValueMissing(row, col);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param value    Description of parameter value.
     * @param position Description of parameter position.
     */
    public void setColumnIsNominal(boolean value, int position) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.setColumnIsNominal(...) -- Model does " +
                           " not contain table data.");
        } else {
            _table.setColumnIsNominal(value, position);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param value    Description of parameter value.
     * @param position Description of parameter position.
     */
    public void setColumnIsScalar(boolean value, int position) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.setColumnIsScalar(...) -- Model does not" +
                           " contain table data.");
        } else {
            _table.setColumnIsScalar(value, position);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param comment Description of parameter comment.
     */
    public void setComment(String comment) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.setComment(...) -- Model does not contain table "
                           + " data.");
        } else {
            _table.setComment(comment);
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @param labl Description of parameter labl.
     */
    public void setLabel(String labl) {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.setLabel(...) -- Model does not contain "
                           + " table data.");
        } else {
            _table.setLabel(labl);
        }
    }

    /**
     * Return the default missing value for integers, both short, int and long.
     *
     * @param newMissingBoolean Description of parameter $param.name$. integer
     *                          for missing values.
     */
    public void setMissingBoolean(boolean newMissingBoolean) {
        this.defaultMissingBoolean = newMissingBoolean;
    }

    /**
     * Return the default missing value for integers, both short, int and long.
     *
     * @param newMissingByte Description of parameter $param.name$. integer for
     *                       missing values.
     */
    public void setMissingByte(byte newMissingByte) {
        this.defaultMissingByte = newMissingByte;
    }

    /**
     * Return the default missing value for integers, both short, int and long.
     *
     * @param newMissingBytes Description of parameter $param.name$. integer for
     *                        missing values.
     */
    public void setMissingBytes(byte[] newMissingBytes) {
        this.defaultMissingByteArray = newMissingBytes;
    }

    /**
     * Return the default missing value for integers, both short, int and long.
     *
     * @param newMissingChar Description of parameter $param.name$. integer for
     *                       missing values.
     */
    public void setMissingChar(char newMissingChar) {
        this.defaultMissingChar = newMissingChar;
    }

    /**
     * Return the default missing value for integers, both short, int and long.
     *
     * @param newMissingChars Description of parameter $param.name$. integer for
     *                        missing values.
     */
    public void setMissingChars(char[] newMissingChars) {
        this.defaultMissingCharArray = newMissingChars;
    }

    /**
     * Return the default missing value for integers, both short, int and long.
     *
     * @param newMissingDouble Description of parameter $param.name$. integer for
     *                         missing values.
     */
    public void setMissingDouble(double newMissingDouble) {
        this.defaultMissingDouble = newMissingDouble;
    }

    /**
     * Return the default missing value for integers, both short, int and long.
     *
     * @param newMissingInt Description of parameter $param.name$. integer for
     *                      missing values.
     */
    public void setMissingInt(int newMissingInt) {
        this.defaultMissingInt = newMissingInt;
    }

    /**
     * Return the default missing value for integers, both short, int and long.
     *
     * @param newMissingString Description of parameter $param.name$. integer for
     *                         missing values.
     */
    public void setMissingString(String newMissingString) {
        this.defaultMissingString = newMissingString;
    }

    /**
     * Returns a shallow copy of this model's Table. returns null if the table is
     * null.
     *
     * @return Table a shallow copy of this model's Table or null if the Table is
     *         null.
     */
    public Table shallowCopy() {

        if (_table == null) {
            myLogger.severe("ERROR: .shallowCopy() -- Model does not contain table " +
                           "data and will return false");

            return null;
        } else {
            return _table.shallowCopy();
        }
    }

    /**
     * See Table in ncsa.d2k.modules.core.datatype.table.
     *
     * @return see Table in ncsa.d2k.modules.core.datatype.table.
     */
    public ExampleTable toExampleTable() {

        if (_table == null) {
            myLogger.severe("ERROR: ClusterModel.toExampleTable() -- Model does not contain table"
                           + " data and will return null");

            return null;
        } else {
            return _table.toExampleTable();
        }
    }

} // end class ClusterModel

