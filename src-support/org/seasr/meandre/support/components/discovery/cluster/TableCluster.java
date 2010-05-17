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

import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.Sparse;
import org.seasr.datatypes.datamining.table.Table;

import gnu.trove.TIntHashSet;

import java.io.Serializable;


/**
 * <p>Title: TableCluster</p>
 *
 * <p>Description: Holds information about a cluster of Table rows.</p>
 *
 * <p>TODO: Make it Sparse Table Friendly</p>
 *
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * <p>Company: NCSA</p>
 *
 * @author  D. Searsmith
 * @version 1.0
 */

public class TableCluster implements Serializable {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = 1947581215538725270L;


   /**
    * The assigned ID of the cluster. Since this is static and shared by all
    * TableCluster objects it assures each one has a unique ID. The ID of this
    * cluster is kept as the label of the cluster.
    */
   static private int s_id = 0;

   //~ Instance fields *********************************************************

   /** The centroid of all the rows in <code>_members.</code> */
   private double[] _centroid = null;

   /** square root of (the sum of (<code>_centroid</codE>[i] squared)). */
   private double _centroid_norm = -1;

   /** A flag - whether the centroid was computed already or not. */
   private boolean _centroidComputed = false;

   /**
    * Distance between the clusters <code>_cluster1</code> and <code>_cluster2.
    * </code>
    */
   private double _childDistance = -1;

   /** _cind[i] is the column index for _spcentroid[i]. */
   private int[] _cind = null;

   /**
    * Sub cluster of this table cluster. It is null if this table cluster is a
    * leaf inthe clustering tree
    */
   private TableCluster _cluster1 = null;

   /**
    * Sub cluster of this table cluster. It is null if this table cluster is a
    * leaf inthe clustering tree
    */
   private TableCluster _cluster2 = null;

   /** The ID of this cluster. unique. */
   private int _label = assignID();

   /**
    * rows indices into the columns of the <code>_table</code> that its data
    * belongs to this cluster.
    */
   private int[] _members = null;

   /** Whether _table is a sparse table or not. */
   private boolean _sparse = false;

   /** Special array for computing the centroid incase of a sparse table. */
   private double[] _spcentroid = null;

   /** Holds the data that is clustered. */
   private Table _table = null;


   /** The textual representation of this lcuster. */
   private String _txtLabel = null;

   //~ Constructors ************************************************************


   /**
    * Creates a new TableCluster object.
    */
   public TableCluster() { }

   /**
    * Construct a single row table cluster form row number <code>row</code> in
    * Table <code>table.</code>
    *
    * @param table Table The table this table cluster refers to
    * @param row   int The row number that belongs to this cluster.
    */
   public TableCluster(Table table, int row) {
      _table = table;
      _members = new int[1];
      _members[0] = row;

      if (_table instanceof Sparse) {
         setSparse(true);
         // Thread.currentThread().dumpStack();
      } else if (_table instanceof ExampleTable) {
         _centroid =
            new double[((ExampleTable) _table).getInputFeatures().length];
      } else {
         _centroid = new double[_table.getNumColumns()];
      }
   }


   /**
    * Construct a multiple rows table cluster form rows indicated by <code>
    * rows</code> in Table <code>table.</code>
    *
    * @param table Table The table this table cluster refers to
    * @param rows  int[] The rows indices that belong to this cluster.
    */
   public TableCluster(Table table, int[] rows) {
      _table = table;
      _members = rows;

      if (_table instanceof Sparse) {
         setSparse(true);
         // Thread.currentThread().dumpStack();
      } else if (_table instanceof ExampleTable) {
         _centroid =
            new double[((ExampleTable) _table).getInputFeatures().length];
      } else {
         _centroid = new double[_table.getNumColumns()];
      }
   }

   /**
    * Constructs a Table Cluster that is defined by 2 sub clusters. <code>
    * c1</code> and <code>c2</codE>
    *
    * @param table Table The table this table cluster refers to.
    * @param c1    TableCluster A sub table cluster
    * @param c2    TableCluster A sub table cluster
    */
   public TableCluster(Table table, TableCluster c1, TableCluster c2) {
      _table = table;
      _cluster1 = c1;
      _cluster2 = c2;

      if (_table instanceof Sparse) {
         setSparse(true);
         // Thread.currentThread().dumpStack();
      } else if (_table instanceof ExampleTable) {
         _centroid =
            new double[((ExampleTable) _table).getInputFeatures().length];
      } else {
         _centroid = new double[_table.getNumColumns()];
      }
   }

   //~ Methods *****************************************************************

   /**
    * Returns an integer that serves as a unique ID for the formed TableCluster
    * objects.
    *
    * @return int A unique ID for the formed TableCluster objects
    */
   static public synchronized int assignID() { return s_id++; }

   /**
    * Merges 2 TableCluster objects into one. Requires that <code>tc1</code> and
    * <code>tc2</code> both refer to the very same Table object in memory.
    *
    * @param  tc1 TableCluster First TableCluster to be merged
    * @param  tc2 TableCluster Second TableCluster to be merged
    *
    * @return TableCluster A Table Cluster that contains rows from <code>
    *         tc1</codE> and <codE>tc2</code> Returns null if one of the input
    *         parameters is null or if the input TableCluster objects do not
    *         refer to the same table.
    */
   static public TableCluster merge(TableCluster tc1, TableCluster tc2) {

      if (tc1 == null) {
         System.out.println("TableCluster.merge -- input param tc1 is null");

         return null;
      }

      if (tc2 == null) {
         System.out.println("TableCluster.merge -- input param tc2 is null");

         return null;
      }

      if (tc1.getTable() != tc2.getTable()) {
         System.out.println("TableCluster.merge -- tc1 and tc2 reference different tables and cannot be merged.");

         return null;
      }

      TableCluster newtc = new TableCluster(tc1.getTable(), tc1, tc2);

      return newtc;
   }

   /**
    * Computes the centroids values of this cluster TODO: Fix for sparse tables.
    */

   public void computeCentroid() {

      if (_centroidComputed) {
         return;
      }

      int[] members = this.getMemberIndices();

      if (!getSparse()) {

         if (_table instanceof ExampleTable) {
            double sum = 0;
            int[] feats = ((ExampleTable) _table).getInputFeatures();

            for (int i = 0, n = feats.length; i < n; i++) {
               sum = 0;

               for (int j = 0, m = members.length; j < m; j++) {
                  sum += _table.getDouble(members[j], feats[i]);
               }

               _centroid[i] = sum;
            }
         } else {
            double sum = 0;

            for (int i = 0, n = _table.getNumColumns(); i < n; i++) {
               sum = 0;

               for (int j = 0, m = members.length; j < m; j++) {
                  sum += _table.getDouble(members[j], i);
               }

               _centroid[i] = sum;
            }
         }

         int cnt = members.length;

         for (int i = 0, n = _centroid.length; i < n; i++) {
            _centroid[i] = _centroid[i] / cnt;
         }
      } else {
         double sum = 0;
         double[] temp = null;
         double[] tempv = null;
         int[] tempi = null;

         // java.util.HashSet ofeats = new java.util.HashSet();
         TIntHashSet ofeats = new TIntHashSet();

         if (_table instanceof ExampleTable) {

            // ofeats = new java.util.HashSet();
            int[] xfeats = ((ExampleTable) _table).getInputFeatures();
            ofeats = new TIntHashSet(xfeats.length);

            for (int x = 0, y = xfeats.length; x < y; x++) {
               ofeats.add(xfeats[x]);
            }

            int numcols = ((ExampleTable) _table).getInputFeatures().length;
            temp = new double[numcols];
            tempv = new double[numcols];
            tempi = new int[numcols];
         } else {
            int numcols = _table.getNumColumns();
            temp = new double[numcols];
            tempv = new double[numcols];
            tempi = new int[numcols];
         }

         // System.out.println("Numcols: " + ( (ExampleTable)
         // _table).getInputFeatures().length);
         for (int j = 0, m = members.length; j < m; j++) {
            int[] feats = ((Sparse) _table).getRowIndices(members[j]);

            for (int i = 0, n = feats.length; i < n; i++) {

               // if (ofeats.contains(new Integer(feats[i]))){
               if (ofeats.contains(feats[i])) {
                  temp[feats[i]] += _table.getDouble(members[j], feats[i]);
               }
            }
         }

         int cnt = 0;

         for (int i = 0, n = temp.length; i < n; i++) {

            if (temp[i] != 0) {
               tempi[cnt] = i;
               tempv[cnt] = temp[i];
               cnt++;
            }
         }

         _cind = new int[cnt];
         _spcentroid = new double[cnt];
         System.arraycopy(tempi, 0, this._cind, 0, cnt);
         System.arraycopy(tempv, 0, this._spcentroid, 0, cnt);
         cnt = members.length;

         for (int i = 0, n = _spcentroid.length; i < n; i++) {
            _spcentroid[i] = _spcentroid[i] / cnt;
         }
      } // end if

      _centroidComputed = true;
   } // end method computeCentroid


   /**
    * Computes the centroids values and nulls the 2 sub clusters.
    */
   public void cut() {
      computeCentroid();
      _cluster1 = null;
      _cluster2 = null;
   }


   /**
    * Generates a textual representation of this cluster.
    *
    * @return String The textual representation of this cluster.
    */
   public String generateTextLabel() {
      String out = null;
      double[] centroid = this.getCentroid();
      out = "[ " + centroid[0] + " ... " + centroid[centroid.length - 1] + " ]";
      _txtLabel = out;

      return out;
   }

   /**
    * If the centroids are not coputed - computes the centroids and returns the
    * centroids array. Handles Sparse Tables as well as regular ones
    *
    * @return double[] The centroids of this cluster.
    */
   public double[] getCentroid() {
      computeCentroid();

      if (this.getSparse()) {
         int sz = 0;

         if (this.getTable() instanceof ExampleTable) {
            sz = ((ExampleTable) this.getTable()).getInputFeatures().length;
         } else {
            sz = this.getTable().getNumColumns();
         }

         double[] centroid = new double[sz];

         for (int i = 0, n = sz; i < n; i++) {
            centroid[0] = 0;
         }

         for (int i = 0, n = _cind.length; i < n; i++) {
            centroid[_cind[i]] = this._spcentroid[i];
         }

         return centroid;
      }

      return this._centroid;
   } // end method getCentroid

   /**
    * If called for the first time - computes the square root of (the sum of
    * (<code>_centroid</codE>[i] squared)) and returns this value. If the
    * returned value is zero returns 0.0000001 This value is saved and returned
    * the next time this method is called
    *
    * @return double the square root of (the sum of (<code>_centroid</codE>[i]
    *         squared)) . If the computed value is zero returns 0.0000001
    */
   public double getCentroidNorm() {
      computeCentroid();

      if (_centroid_norm < 0) {
         double temp = 0;

         if (!getSparse()) {

            for (int i = 0, n = _centroid.length; i < n; i++) {
               temp += Math.pow(_centroid[i], 2);
            }
         } else {

            for (int i = 0, n = _spcentroid.length; i < n; i++) {
               temp += Math.pow(_spcentroid[i], 2);
            }
         }

         _centroid_norm = Math.sqrt(temp);
      }

      if (_centroid_norm == 0) {
         _centroid_norm = .0000001;
      }

      return this._centroid_norm;
   } // end method getCentroidNorm

   /**
    * Returns the value for the Distance between the clusters <code>
    * _cluster1</code> and <code>_cluster2.</code>
    *
    * @return double The value for the Distance between the clusters <code>
    *         _cluster1</code> and <code>_cluster2</code>
    */
   public double getChildDistance() { return _childDistance; }

   /**
    * Returns the unique ID that was assigned to this cluster.
    *
    * @return int The unique ID that was assigned to this cluster.
    */
   public int getClusterLabel() { return _label; }

   /**
    * Return the left child of this node or null if it doesn't exist.
    *
    * @return left child as TableCluster
    */
   public TableCluster getLC() { return _cluster1; }

   /**
    * Returns an array of indices of rows in <code>_table</code> that are
    * clustered in this TableCLuster.
    *
    * @return int[] Indices of rows in <code>_table</code> that are clustered in
    *         this TableCLuster.
    */
   public int[] getMemberIndices() {
      int[] temparr = null;

      if (_members == null) {

         if ((_cluster1 == null) || (_cluster2 == null)) {
            System.out.println("ERROR: TableCluster.getMemberIndices() -- no clusters or indices defined.");

            return null;
         } else {
            int[] arr1 = _cluster1.getMemberIndices();
            int[] arr2 = _cluster2.getMemberIndices();
            _members = new int[arr1.length + arr2.length];
            System.arraycopy(arr1, 0, _members, 0, arr1.length);
            System.arraycopy(arr2, 0, _members, arr1.length, arr2.length);
         }
      }

      temparr = new int[_members.length];
      System.arraycopy(_members, 0, temparr, 0, _members.length);

      return temparr;
   }

   /**
    * Returns the centroid value of the column indexed <codE>z</code> in the
    * table this cluster refers to (<code>_table</code>. Returns zero in the
    * case that there isn't such column in the table.
    *
    * @param  z int A column index into the columns array of the table this
    *           cluster refers to.
    *
    * @return double the centroid value of the column indexed <codE>z</code> in
    *         the table this cluster refers to (<code>_table</code>. Returns
    *         zero in the case that there isn't such column in the table.
    */
   public double getNthCentroidValue(int z) {
      computeCentroid();

      if (!getSparse()) {
         return _centroid[z];
      } else {

         for (int i = 0, n = _cind.length; i < n; i++) {

            if (z == _cind[i]) {
               return _spcentroid[i];
            } else if (z > _cind[i]) {
               return 0;
            }
         }

         return 0;
      }
   }

   /**
    * Return the right child of this node or null if it doesn't exist.
    *
    * @return right child as TableCluster
    */
   public TableCluster getRC() { return _cluster2; }

   /**
    * Returns the number of rows included in this cluster.
    *
    * @return int The number of rows included in this cluster.
    */
   public int getSize() { return getMemberIndices().length; }

   /**
    * Returns true if the input table is a sparse table. Otherwise returns
    * false.
    *
    * @return boolean true if the input table is a sparse table. Otherwise
    *         returns false.
    */
   public boolean getSparse() { return _sparse; }


   /**
    * Returns the indices of the columns (fromt he input table <code>
    * _Table</code>) that are valid for this cluster (that have values in them,
    * as far as regarding the rows indicated in <code>_members.</codE>
    *
    * @return int[] indices of the columns (fromt he input table <code>
    *         _Table</code>) that are valid for this cluster.
    */
   public int[] getSparseCentroidInd() {
      computeCentroid();

      return this._cind;
   }

   /**
    * Special method to return the centroids values in case the input table is a
    * sparse table.
    *
    * @return double[] The centroids of the input sparse table. contains values
    *         only from columns that are valid (has values in them for the rows
    *         that are part of this cluster).
    */
   public double[] getSparseCentroidValues() {
      computeCentroid();

      return this._spcentroid;
   }

   /**
    * Returns an array of Object references, containing the 2 sub clusters of
    * this cluster.
    *
    * @return Object[] An Object references array, containing the 2 sub clusters
    *         of this cluster
    */
   public Object[] getSubClusters() {
      Object[] ret = new Object[2];
      ret[0] = _cluster1;
      ret[1] = _cluster2;

      return ret;
   }


   /**
    * Returns the table this table cluster refers to.
    *
    * @return Table The table this table cluster refers to.
    */
   public Table getTable() { return _table; }

   /**
    * Returns the value of the textual representation of this cluster.
    *
    * @return String The value of the textual representation of this cluster
    */
   public String getTextClusterLabel() { return _txtLabel; }


   /**
    * Returns true if the left sub cluster is null. otherwise returns false.
    *
    * @return boolean true if the left sub cluster is null. otherwise returns
    *         false.
    */
   public boolean isLeaf() { return (_cluster1 == null); }

   /**
    * Sets the value for the Distance between the clusters <code>
    * _cluster1</code> and <code>_cluster2.</code>
    *
    * @param i double The value for the Distance between the clusters <code>
    *          _cluster1</code> and <code>_cluster2</code>
    */
   public void setChildDistance(double i) { _childDistance = i; }

   /**
    * Sets the value of the <codE>_sparse</code> flag.
    *
    * @param b boolean true if the input table is a sparse table. Otherwise
    *          shoudl be false.
    */
   public void setSparse(boolean b) { _sparse = b; }

   /**
    * Sets the value of the textual representation of this cluster.
    *
    * @param i String The value for the textual representation of this cluster
    */
   public void setTextClusterLabel(String i) { _txtLabel = i; }


   /**
    * Returns the textual representation of this cluster.
    *
    * @return String The textual representation of this cluster.
    */
    @Override
   public String toString() {

      if (_txtLabel == null) {
         generateTextLabel();
      }

      return _txtLabel;
   }


} // end class TableCluster


