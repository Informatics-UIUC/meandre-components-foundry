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

package org.seasr.meandre.support.components.discovery.cluster.hac;


//==============
// Java Imports
//==============

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Logger;


//===============
// Other Imports
//===============

import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.Sparse;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.meandre.support.components.discovery.cluster.ClusterModel;
import org.seasr.meandre.support.components.discovery.cluster.TableCluster;
import org.seasr.meandre.support.components.discovery.cluster.TableColumnTypeException;
import org.seasr.meandre.support.components.discovery.cluster.TableMissingValuesException;


/**
 * <p>Title: HAC</p>
 *
 * <p>Description:</p>
 *
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * <p>Company: NCSA Automated Learning Group</p>
 *
 * @author  D. Searsmith
 * @version 1.0
 */

public class HACWork {

   //~ Static fields/initializers **********************************************

   /** The label for the cluster column */
   static public final String _CLUSTER_COLUMN_LABEL = "Cluster_Column";

   /** An index into  <codE>s_ClusterMethodLabels</code> and <codE>s_ClusterMethodDesc</code>.
    * The ID for the Wards clustering method. */
   static public final int s_WardsMethod_CLUSTER = 0;

   /** An index into  <codE>s_ClusterMethodLabels</code> and <codE>s_ClusterMethodDesc</code>.
    * The ID for the Single Link clustering method. */
   static public final int s_SingleLink_CLUSTER = 1;

   /** An index into  <codE>s_ClusterMethodLabels</code> and <codE>s_ClusterMethodDesc</code>.
    * The ID for the Complete Link clustering method. */
   static public final int s_CompleteLink_CLUSTER = 2;

   /** Labels of clustering methods, this object may performs. */
   static public final String[] s_ClusterMethodLabels =
   {
      "Ward's Method",
      "Single Link", "Complete Link", "UPGMA", "WPGMA", "UPGMC", "WPGMC"
   };

      /** Descriptions of clustering methods, this object may performs. */
   static public final String[] s_ClusterMethodDesc =
   {
      "Minimize the square of the distance",
      "Distance of closest pair (one from each cluster)",
      "Distance of furthest pair (one from each cluster)",
      "Unweighted pair group method using arithmetic averages",
      "Weighted pair group method using arithmetic averages",
      "Unweighted pair group method using centroids",
      "Weighted pair group method using centroids"
   };

   /**
    * In the following, unweighted means that the contribution that each cluster
    * element plays is adjusted for the size of clusters.
    *
    * <p>The weighted case ignores clusters sizes and thus smaller cluster
    * elements are given equal weight as large cluster elements.</p>
    */
   /** An index into <codE>s_ClusterMethodLabels</code> and <code>
    * s_ClusterMethodDesc</codE>. The ID for the
    * Unweighted Pair Group clustering Method using Arithmetic averages. */
   static public final int s_UPGMA_CLUSTER = 3;

   /** An index into <codE>s_ClusterMethodLabels</code> and <code>
    * s_ClusterMethodDesc</codE>. The ID for the
    * Weighted Pair Group clustering Method using Arithmetic averages. */
   static public final int s_WPGMA_CLUSTER = 4;

   /** An index into <codE>s_ClusterMethodLabels</code> and <code>
    * s_ClusterMethodDesc</codE>. The ID for the
    * Unweighted Pair Group clustering Method using centroids. */
   static public final int s_UPGMC_CLUSTER = 5;

   /** An index into <codE>s_ClusterMethodLabels</code> and <code>
    * s_ClusterMethodDesc</codE>. The ID for the
    * Weighted Pair Group clustering method using Centroids. */
   static public final int s_WPGMC_CLUSTER = 6;

   /** Labels of Distance Metrics used by this object to perform clustering */
   static public final String[] s_DistanceMetricLabels =
   {
      "Euclidean",
      "Manhattan", "Cosine"
   };

   /** Descirption of Distance Metrics used by this object to perform clustering */
   static public final String[] s_DistanceMetricDesc =
   {
      "\"Straight\" line distance between points",
      "Distance between two points measured along axes at right angles",
      "1 minus the cosine of the angle between the norms of the vectors "
      + " denoted by two points"
   };

   /** An index into <codE>s_DistanceMetricDesc</code> and <code>
       * s_DistanceMetricLabels</codE>. The ID for the
    * Euclidean distance metric. */
   static public final int s_Euclidean_DISTANCE = 0;

   /** An index into <codE>s_DistanceMetricDesc</code> and <code>
       * s_DistanceMetricLabels</codE>. The ID for the
    * Manhattan distance metric. */
   static public final int s_Manhattan_DISTANCE = 1;

   /** An index into <codE>s_DistanceMetricDesc</code> and <code>
      * s_DistanceMetricLabels</codE>. The ID for the
   * Cosine distance metric. */
   static public final int s_Cosine_DISTANCE = 2;

   //~ Instance fields *********************************************************

   /** Common name for this object. For purposes of Logging and verbose output. */
   private String _alias = "HACWork";

   /** The ID of the cluster method to be used. */
   private int _clusterMethod = -1;

   /** The ID of the distance metric to be used. */
   private int _distanceMetric = -1;

   /** Indices into the data table that are the input feature columns.*/
   private int[] _ifeatures = null;

   /** Number Of Clusters to be formed by this object. */
   private int _numberOfClusters = -1;

   /** The threshold property, determines when to halt the algorithm.. */
   private int _thresh = 0;

   /** The verbose flag. If true this object outputs verbose information to stdout. */
   private boolean _verbose = false;

   /** Time when clustering process starts. */
   private long m_start = -1;

/**
   * Check missing values flag. If set to true, this module verifies prior  to computation, that there are no missing values in the input table. (In the presence of missing values the module throws an Exception.)
*/
   protected boolean _mvCheck = true;

   //~ Constructors ************************************************************

   /**
    * Creates a new HAC object.
    *
    * @param cm          The clustering method to be used. Must be one of the
    * constants IDs of clustering methods defined by this object.
    * @param dm          The distance metric to be used. Must be one of the
    * constants IDs of distance metrics defined by this object.
    * @param num         Number of clusters to be formed
    * @param thresh      The halt threshold.
    * @param ver         The value for the verbose flag
    * @param check       The value for the check missing values flag
    * @param moduleAlias The common name to call this object.
    */
   public HACWork(int cm, int dm, int num, int thresh, boolean ver, boolean check,
              String moduleAlias) {
      this.setClusterMethod(cm);
      this.setDistanceMetric(dm);
      this.setNumberOfClusters(num);
      this.setDistanceThreshold(thresh);
      this.setVerbose(ver);
      this.setCheckMissingValues(check);
      this.setAlias(moduleAlias);
   }

   //~ Methods *****************************************************************

   /**
    * Find an approximate max distance for the input table using only the
    * specified input features and distance metric. Return the thresh (% value)
    * times this distance.
    *
    * @param  itable    Table of examples
    * @param  ifeatures Features of interest
    * @param  dm        Distance Metric
    * @param  thresh    The percent value to multiply times max distance.
    *
    * @return a percent (thresh) of the maxdist
    */
   static public double calculateMaxDist(Table itable, int[] ifeatures,
                                         int dm, int thresh) {
      double maxdist = 0;

      // find distance threshold
      double[] max = new double[ifeatures.length];
      double[] min = new double[ifeatures.length];

      for (int i = 0, n = ifeatures.length; i < n; i++) {
         min[i] = Double.MAX_VALUE;
         max[i] = Double.MIN_VALUE;
      }

      int bcnts = 0;

      for (int i = 0, n = ifeatures.length; i < n; i++) {

         if (itable.getColumnType(ifeatures[i]) == ColumnTypes.BOOLEAN) {
            max[i] = 1;
            min[i] = 0;
            bcnts++;
         }
      }

      if (bcnts != ifeatures.length) {

         if (!(itable instanceof Sparse)) {

            for (int i = 0, n = itable.getNumRows(); i < n; i++) {

               for (int j = 0, m = ifeatures.length; j < m; j++) {

                  if (
                      !(itable.getColumnType(ifeatures[j]) ==
                           ColumnTypes.BOOLEAN)) {
                     double compval = itable.getDouble(i, ifeatures[j]);

                     if (max[j] < compval) {
                        max[j] = compval;
                     }

                     if (min[j] > compval) {
                        min[j] = compval;
                     }
                  }
               }
            }

         } else {
            gnu.trove.TIntHashSet ihash = new gnu.trove.TIntHashSet(ifeatures);

            for (int i = 0, n = itable.getNumRows(); i < n; i++) {
               int[] cols = ((Sparse) itable).getRowIndices(i);

               for (int j = 0, m = cols.length; j < m; j++) {

                  if (ihash.contains(cols[j])) {

                     if (
                         !(itable.getColumnType(cols[j]) ==
                              ColumnTypes.BOOLEAN)) {
                        double compval = itable.getDouble(i, cols[j]);

                        if (max[cols[j]] < compval) {
                           max[cols[j]] = compval;
                        }

                        if (min[cols[j]] > compval) {
                           min[cols[j]] = compval;
                        }
                     }
                  }
               }
            }

         } // end if
      } // end if

      return maxdist = ((double) thresh / 100) * distance(max, min, dm);
   } // end method calculateMaxDist

   /**
    * TODO: Need to accommodate sparse tables.
    *
    * @param  tc1 Cluster to calculate distance from
    * @param  tc2 Cluster to calculate distance from
    * @param  dm  Description of parameter $param.name$. Table for these
    *             clusters
    *
    * @return Distance value as double
    */
   static public double distance(TableCluster tc1, TableCluster tc2, int dm) {

      if (!tc1.getSparse()) {

         double[] centroid1 = tc1.getCentroid();
         double[] centroid2 = tc2.getCentroid();

         if (dm == HACWork.s_Euclidean_DISTANCE) {
            double diffs = 0;

            for (int i = 0, n = centroid1.length; i < n; i++) {
               double diff = centroid1[i] - centroid2[i];
               diffs += Math.pow(diff, 2);
            }

            return Math.sqrt(diffs);
         } else if (dm == HACWork.s_Manhattan_DISTANCE) {
            double diffs = 0;

            for (int i = 0, n = centroid1.length; i < n; i++) {
               double diff = centroid1[i] - centroid2[i];
               diffs += Math.abs(diff);
            }

            return diffs;
         } else if (dm == HACWork.s_Cosine_DISTANCE) {
            double prods = 0;

            for (int i = 0, n = centroid1.length; i < n; i++) {
               double prod = centroid1[i] * centroid2[i];
               prods += prod;
            }

            return Math.abs(1 -
                            (prods /
                                (tc1.getCentroidNorm() *
                                    tc2.getCentroidNorm())));
         } else {

            // this should never happen
            System.out.println("Unknown distance metric ... ");

            return -1;
         }
      } else {

         int[] ind1 = tc1.getSparseCentroidInd();
         double[] val1 = tc1.getSparseCentroidValues();

         int[] ind2 = tc2.getSparseCentroidInd();
         double[] val2 = tc2.getSparseCentroidValues();

         if (dm == HACWork.s_Euclidean_DISTANCE) {

            double dist = 0;
            int x = 0;
            int y = 0;

            while ((x < ind1.length) && (y < ind2.length)) {

               if (ind1[x] == ind2[y]) {

                  dist += Math.pow(val1[x] - val2[y], 2);
                  x++;
                  y++;
               } else if (ind1[x] < ind2[y]) {
                  dist += Math.pow(val1[x], 2);
                  x++;
               } else {
                  dist += Math.pow(val2[y], 2);
                  y++;
               }
            }

            while (x < ind1.length) {
               dist += Math.pow(val1[x], 2);
               x++;
            }

            while (y < ind2.length) {
               dist += Math.pow(val2[y], 2);
               y++;
            }

            return Math.sqrt(dist);
         } else if (dm == HACWork.s_Manhattan_DISTANCE) {
            double dist = 0;
            int x = 0;
            int y = 0;

            while ((x < ind1.length) && (y < ind2.length)) {

               if (ind1[x] == ind2[y]) {

                  dist += Math.abs(val1[x] - val2[y]);
                  x++;
                  y++;
               } else if (ind1[x] < ind2[y]) {
                  dist += Math.abs(val1[x]);
                  x++;
               } else {
                  dist += Math.abs(val2[y]);
                  y++;
               }
            }

            while (x < ind1.length) {
               dist += Math.abs(val1[x]);
               x++;
            }

            while (y < ind2.length) {
               dist += Math.abs(val2[y]);
               y++;
            }

            return dist;
         } else if (dm == HACWork.s_Cosine_DISTANCE) {
            double prods = 0;
            int x = 0;
            int y = 0;

            while ((x < ind1.length) && (y < ind2.length)) {

               if (ind1[x] == ind2[y]) {

                  prods += val1[x] * val2[y];
                  x++;
                  y++;
               } else if (ind1[x] < ind2[y]) {
                  x++;
               } else {
                  y++;
               }
            }

            return Math.abs(1 -
                            (prods /
                                (tc1.getCentroidNorm() *
                                    tc2.getCentroidNorm())));
         } else {

            // this should never happen
            System.out.println("Unknown distance metric ... ");

            return -1;
         }

      } // end if
   } // end method distance

   /**
    * Find distance between two examples, clusters of the input table, (double
    * arrays) using specified distance metric.
    *
    * @param  centroid1 Centroid of a given cluster
    * @param  centroid2 Centroid of a given cluster
    * @param  dm        Distance metric ID.
    *
    * @return Distance value as double
    */
   static public double distance(double[] centroid1, double[] centroid2,
                                 int dm) {

      if (dm == HACWork.s_Euclidean_DISTANCE) {
         double diffs = 0;

         for (int i = 0, n = centroid1.length; i < n; i++) {
            double diff = centroid1[i] - centroid2[i];
            diffs += Math.pow(diff, 2);
         }

         return Math.sqrt(diffs);
      } else if (dm == HACWork.s_Manhattan_DISTANCE) {
         double diffs = 0;

         for (int i = 0, n = centroid1.length; i < n; i++) {
            double diff = centroid1[i] - centroid2[i];
            diffs += Math.abs(diff);
         }

         return diffs;
      } else if (dm == HACWork.s_Cosine_DISTANCE) {
         double prods = 0;

         for (int i = 0, n = centroid1.length; i < n; i++) {
            double prod = centroid1[i] * centroid2[i];
            prods += prod;
         }

         double norm1 = 0;
         double norm2 = 0;

         for (int i = 0, n = centroid1.length; i < n; i++) {
            norm1 += Math.pow(centroid1[i], 2);
            norm2 += Math.pow(centroid2[i], 2);
         }

         norm1 = Math.sqrt(norm1);
         norm2 = Math.sqrt(norm2);

         if (norm1 == 0) {
            norm1 = .000001;
         }

         if (norm2 == 0) {
            norm2 = .000001;
         }

         return Math.abs(1 - (prods / (norm1 * norm2)));
      } else {

         // this should never happen
         System.out.println("Unknown distance metric ... ");

         return -1;
      }
   } // end method distance

   /**
    * Check if any columns are non numeric (excluding boolean).
    *
    * @param  itable    A table to cluster its data.
    * @param  ifeatures An integers array with indices into <codE>itable</code>
    *                   that are its input features.
    *
    * @throws Exception throw TableColumnTypeException if non-numeric column(s)
    *                   are detected
    */
   static public void validateNonTextColumns(Table itable, int[] ifeatures)
      throws Exception {

      // Validate the column types -- can only operate on numeric or boolean
      // types.
      for (int i = 0, n = ifeatures.length; i < n; i++) {
         int ctype = itable.getColumnType(ifeatures[i]);

         if (
             !((ctype == ColumnTypes.BYTE) || (ctype == ColumnTypes.BOOLEAN) ||
                  (ctype == ColumnTypes.DOUBLE) ||
                  (ctype == ColumnTypes.FLOAT) ||
                  (ctype == ColumnTypes.LONG) ||
                  (ctype == ColumnTypes.INTEGER) ||
                  (ctype == ColumnTypes.SHORT))) {

            // can't use getAlias() in next exception since this is static
            // method
            Exception ex1 =
               new TableColumnTypeException(ctype,
                                            " (HAC): " +
                                            "Only boolean and numeric types are permitted." +
                                            " For nominal input types use a scalarization transformation or remove" +
                                            " them from the input set.");

            throw ex1;
         }
      }
   } // end method validateNonTextColumns

   //private Logger myLogger =  Logger.getLogger("HACWork");

   /**
    * Does the work of clustering the input table values and returns a
    * ClusterModel.
    *
    * @param  inittable The data to be clustered.
    *
    * @return ClusterModel Data structure that encapsulates <code>
    *         inittable</code> and the TableCluster objects related to its data.
    *
    * @throws Exception                   If <code>inittable</code> has no data
    *                                     in it or if the data contains missing
    *                                     values
    * @throws TableMissingValuesException If <codE>inittable</code> has missing
    * values or has no data in it.
    */
   public ClusterModel buildModel(Table inittable) throws Exception {

      if (this.getCheckMissingValues()) {

         if (inittable.hasMissingValues()) {
            throw new TableMissingValuesException(getAlias() +
                                                  " (HAC): Please replace or filter out missing values in your data.");
         }
      }

      if (inittable.getNumRows() < 1) {
         throw new Exception(getAlias() + " (HAC): Input table has no rows.");
      }

      ClusterModel model = null;

      m_start = System.currentTimeMillis();

      ArrayList resultClusters = null;

      Table itable = null;

      if (inittable instanceof ClusterModel) {
         itable = ((ClusterModel) inittable).getTable();
      } else {
         itable = inittable;
      }

      if (itable instanceof ExampleTable) {
         _ifeatures = ((ExampleTable) itable).getInputFeatures();
      } else {
         _ifeatures = new int[itable.getNumColumns()];

         for (int i = 0, n = itable.getNumColumns(); i < n; i++) {
            _ifeatures[i] = i;
         }
      }

      validateNonTextColumns(itable, _ifeatures);

      // indexlist maps the active indices for the proximity matrix
      int[] indexlist = null;

      // hash map for keeping track of cluster to proxm index
      HashMap indexmap = null;

      ArrayList clusters = null;

      if (inittable instanceof ClusterModel) {
         clusters = ((ClusterModel) inittable).getClusters();
         indexlist = new int[clusters.size()];
         indexmap = new HashMap(clusters.size());

         for (int i = 0, n = clusters.size(); i < n; i++) {
            TableCluster tc = (TableCluster) clusters.get(i);
            indexlist[i] = i;
            indexmap.put(tc, new Integer(i));
         }
      } else {

         // Create a cluster for each row
         indexlist = new int[itable.getNumRows()];
         indexmap = new HashMap(itable.getNumRows());
         clusters = new ArrayList();

         for (int i = 0, n = itable.getNumRows(); i < n; i++) {
            TableCluster tc = new TableCluster(itable, i);
            clusters.add(tc);
            indexlist[i] = i;
            indexmap.put(tc, new Integer(i));
         }
      }

      // proximity matrix for storing the dval object values
      Object[][] proxm = new Object[clusters.size()][clusters.size()];

      int dvalcnt = 0;
      double distval = 0;

      double maxdist = -1;

      if (_thresh != 0) {
         maxdist =
            calculateMaxDist(itable, _ifeatures, this.getDistanceMetric(),
                             this.getDistanceThreshold());
      }

      try {

         TreeSet cRank = null;
         cRank = new TreeSet(new cRank_Comparator());

         boolean firstTime = true;
         // double shortestDist = Double.MAX_VALUE;

         // while the number of clusters is still greater than
         // m_numberOfClusters
         while (clusters.size() > 1) {

            // System.out.println(clusters.size() + " clusters remain ...
            // beginning similarity search ...");
            // find the two most similar clusters
            double dist = 0;
            TableCluster tc1 = null;
            TableCluster tc2 = null;

            if (firstTime) {
               firstTime = false;

               for (int x = 0, xn = clusters.size(); x < xn; x++) {

                  for (int y = x + 1, yn = clusters.size(); y < yn; y++) {

                     if (y > x) {
                        double simval = 0;
                        TableCluster clustX = (TableCluster) clusters.get(x);
                        TableCluster clustY = (TableCluster) clusters.get(y);

                        distval =
                           distance(clustX, clustY,
                                    this._distanceMetric);

                        if (distval < 0) {
                           distval = 0;
                        }

                        Object[] dval = new Object[4];
                        dval[0] = clustX;
                        dval[1] = clustY;
                        dval[2] = new Double(distval);
                        dval[3] = new Integer(dvalcnt++);

                        if (!cRank.add(dval)) {
                                System.out.println(getAlias() +
                                              " (HAC): " +
                                              ">>>>>>>>>>>>>>>>>>>>> UNABLE TO ADD TO cRANK " +
                                              dval.hashCode() + " " +
                                              simval);
                        }

                        proxm[x][y] = dval;
                     } // end if
                  } // end for
               } // end for

               if (getVerbose()) {
                  System.out.println(getAlias() +
                                     " (HAC): INITIAL PROXIMITY MATRIX COMPLETE");

               }

            } // end if

            Object[] o = (Object[]) cRank.first();

            tc1 = (TableCluster) o[0];
            tc2 = (TableCluster) o[1];
            
            if (getVerbose()){
            	System.out.println("Next closest clusters' distance: " + o[2]);
      		}
      
            TableCluster newc = TableCluster.merge(tc1, tc2);

            if (
                (clusters.size() <= _numberOfClusters) &&
                   (_thresh == 0) &&
                   (resultClusters == null)) {
               resultClusters = new ArrayList(clusters);
            }

            if (resultClusters == null) {

               if (
                   (_thresh != 0) &&
                      (((Double) ((Object[]) cRank.first())[2]).doubleValue() >
                          maxdist)) {
                  resultClusters = new ArrayList(clusters);
               }
            }

            // primarily for the cluster vis
            newc.setChildDistance(((Double) o[2]).doubleValue());

            clusters.remove(tc1);
            clusters.remove(tc2);
            clusters.add(newc);

            Object[] c1c2arr = null;
            ArrayList alist = new ArrayList();

            int row1 = ((Integer) indexmap.get(tc1)).intValue();
            int row2 = ((Integer) indexmap.get(tc2)).intValue();
            int newrow = Math.min(row1, row2);

            for (int i = 0, n = indexlist.length; i < n; i++) {

               // use the indexlist to know where the active indices are
               int ind = indexlist[i];

               // and if the position row1, row2 then exclude it here
               // because we don't
               // want it added to alist twice
               if ((ind < row1) && (ind != row2)) {
                  alist.add(proxm[ind][row1]);
               } else if ((ind > row1) && (ind != row2)) {
                  alist.add(proxm[row1][ind]);
               }

               if (ind < row2) {
                  alist.add(proxm[ind][row2]);
               } else if (ind > row2) {
                  alist.add(proxm[row2][ind]);
               }
            }

            if (row1 < row2) {
               c1c2arr = (Object[]) proxm[row1][row2];
               proxm[row1][row2] = null;
            } else {
               c1c2arr = (Object[]) proxm[row2][row1];
               proxm[row2][row1] = null;
            }

            // Remove old sim values for the two clusters being merged from
            // the sorted list
            for (int i = 0, n = alist.size(); i < n; i++) {

               // System.out.println("Its there?: " +
               // cRank.contains(alist.get(i)));
               if (!cRank.remove(alist.get(i))) {
                   System.out.println("We have already removed it once?: " +
                           cRank.contains(alist.get(i)));
                   System.out.println("Object hash:" + alist.get(i));
                   System.out.println(">>>>>>>>>>>>>>>>>>>>> " +
                                     ((Double) ((Object[]) alist.get(i))[2])
                                        .doubleValue() +
                                     " " +
                                     ((Integer) ((Object[]) alist.get(i))[3])
                                        .intValue());
                   System.out.println("\n");
               }
            }

            // row/column to be removed from proxm
            int remove = Math.max(row1, row2);

            // find sims between the new cluster and all other clusters and
            // add them to the
            // sorted list
            for (int i = 0, n = clusters.size(); i < (n - 1); i++) {
               double simval = 0;

               int clustind =
                  ((Integer) indexmap.get(clusters.get(i))).intValue();
               Double d1 = null;
               Double d2 = null;

               if (clustind < row1) {
                  d1 = (Double) ((Object[]) proxm[clustind][row1])[2];
               } else {
                  d1 = (Double) ((Object[]) proxm[row1][clustind])[2];
               }

               if (clustind < row2) {
                  d2 = (Double) ((Object[]) proxm[clustind][row2])[2];
               } else {
                  d2 = (Double) ((Object[]) proxm[row2][clustind])[2];
               }

               double sim1 = d1.doubleValue();
               double sim2 = d2.doubleValue();
               double sim3 = ((Double) c1c2arr[2]).doubleValue();
               int sz1 = tc1.getSize();
               int sz2 = tc2.getSize();
               int sz3 = ((TableCluster) clusters.get(i)).getSize();
               int sznew = newc.getSize();

               int method = getClusterMethod();

               if (method == s_UPGMA_CLUSTER) {

                  // ** UPGMA **
                  simval = (sz1 * sim1) / (sznew) + (sz2 * sim2) / sznew;
               } else if (method == s_WardsMethod_CLUSTER) {

                  // ** Ward's **
                  simval =
                     (((sz1 + sz3) * sim1) / (sznew + sz3)) +
                     (((sz2 + sz3) * sim2) / (sznew + sz3)) -
                     ((sz3 * sim3) / (sznew + sz3));
               } else if (method == s_SingleLink_CLUSTER) {

                  // ** Single Link **
                  simval = sim1 / 2 + sim2 / 2 - Math.abs(sim1 - sim2) /
                              2;
               } else if (method == s_CompleteLink_CLUSTER) {

                  // ** Complete Link **
                  simval = sim1 / 2 + sim2 / 2 + Math.abs(sim1 - sim2) /
                              2;
               } else if (method == s_WPGMA_CLUSTER) {

                  // ** WPGMA **
                  simval = sim1 / 2 + sim2 / 2;
               } else if (method == s_UPGMC_CLUSTER) {

                  // ** UPGMC **
                  simval = sim1 / 2 + sim2 / 2 - sim3 / 4;
               } else if (method == s_WPGMC_CLUSTER) {

                  // ** WPGMC **
                  simval =
                     (sz1 * sim1) / (sznew) + (sz2 * sim2) / sznew -
                     (((sz1) * (sz2)) / Math.pow((sz1 + sz2), 2)) *
                        sim3;
               } else {

                  throw new Exception(getAlias() +
                  " (HAC): unknown cluster method specified.");
               }

               if (simval < 0) {
                  simval = 0;
               }

               Object[] dval = new Object[4];
               dval[0] = newc;
               dval[1] = (TableCluster) clusters.get(i);
               dval[2] = new Double(simval);
               dval[3] = new Integer(dvalcnt++);

               if (!cRank.add(dval)) {
                  System.out.println(getAlias() +
                  " (HAC): >>>>>>>>>>>>>>>>>>>>> UNABLE TO ADD TO cRANK ");
               }

               // update the matrix
               if (clustind < newrow) {
                  proxm[clustind][newrow] = dval;
               } else {
                  proxm[newrow][clustind] = dval;
               }

               if (clustind < remove) {
                  proxm[clustind][remove] = null;
               } else {
                  proxm[remove][clustind] = null;
               }
            } // end for

            // adjust the index list
            int[] temparr = new int[indexlist.length - 1];
            int cnter = 0;

            for (int i = 0, n = indexlist.length; i < n; i++) {

               if (indexlist[i] != remove) {
                  temparr[cnter] = indexlist[i];
                  cnter++;
               }
            }

            indexlist = temparr;

            // fixup indexmap
            if (indexmap.remove(tc1) == null) {
               System.out.println(">>>>>>>>>>>>>>>>>>>>> UNABLE TO REMOVE TC1 from IndexMap");
            }

            if (indexmap.remove(tc2) == null) {
               System.out.println(">>>>>>>>>>>>>>>>>>>>> UNABLE TO REMOVE TC2 from IndexMap");
            }

            indexmap.put(newc, new Integer(newrow));
         } // end while

         // Outout a Cluster Model which is an object containing that table
         // (optional), clusters, and
         // cluster tree.

         // set classes (ints) in each cluster

         if (resultClusters == null) {
            resultClusters = clusters;
         }

         if (getVerbose()) {

            for (int i = 0, n = resultClusters.size(); i < n; i++) {
               TableCluster tc = (TableCluster) resultClusters.get(i);
                  System.out.println("Cluster " + tc.getClusterLabel() +
                                  " containing " + tc.getSize() + " elements.");
            }
         }
         //
         // //set prediction to class value for each row in table
         // //output table

         model =
            new ClusterModel(itable, resultClusters,
                             (TableCluster) clusters.get(0));

      } catch (Exception ex) {
         ex.printStackTrace();
         System.out.println(ex.getMessage());
         throw ex;
      } finally {
         long end = System.currentTimeMillis();

         if (resultClusters != null) {

            if (getVerbose()) {
                  System.out.println("\n" + getAlias() +
                                  " (HAC): END EXEC -- clusters built: " +
                                  resultClusters.size() + " in " +
                                  (end - m_start) /
                                     1000 + " seconds\n");
            }
         }
      }

      return model;
   } // end method buildModel

   /**
    * Returns the common name for the instance of this object
    *
    * @return The common name for the instance of this object
    */
   public String getAlias() { return _alias; }

   /**
    * Returns the value of the Check Missing Values flag.
    *
    * @return The value of the Check Missing Values flag.
    */
   public boolean getCheckMissingValues() { return _mvCheck; }

   /**
    * Returns the ID of the selected clustering method
    *
    * @return The ID of the selected clustering method.
    */
   public int getClusterMethod() { return _clusterMethod; }

   /**
    * Returns the ID of the selected distance metric.
    *
    * @return The ID of the selected distance metric.
    */
   public int getDistanceMetric() { return _distanceMetric; }

   /**
    * Return the threshold value.
    *
    * @return The threshold value.
    */
   public int getDistanceThreshold() { return _thresh; }

   /**
    * Returns the number of clusters to be formed
    *
    * @return the number of clusters to be formed
    */
   public int getNumberOfClusters() { return _numberOfClusters; }

   /**
    * Returns the value of the verbose flag.
    *
    * @return The value of the verbose flag.
    */
   public boolean getVerbose() { return _verbose; }

   /**
    * Sets the common name for this instance of HAC
    *
    * @param moduleAlias The common name for this instance of HAC
    */
   public void setAlias(String moduleAlias) { _alias = moduleAlias; }

   /**
    * Sets the value of the Check Missing values flag
    *
    * @param b The value for the Check Missing values flag
    */
   public void setCheckMissingValues(boolean b) { _mvCheck = b; }

   /**
    * Sets the cluster method ID
    *
    * @param noc The cluster method ID to be used. Must be in the range of
    * the boundaries of <code>s_ClusterMethodDesc</code> array.
    *
    */
   public void setClusterMethod(int noc) { _clusterMethod = noc; }

   /**
    * Sets the distanc metric ID
    *
    * @param noc The distance metric ID to be used. Must be in the range of
    * the boundaries of <code>s_DistanceMetricDesc</code> array.
    *
    */
   public void setDistanceMetric(int dm) { _distanceMetric = dm; }

   /**
    * Sets the threshold property.
    *
    * @param noc An integer in the range [1,100].
    */
   public void setDistanceThreshold(int noc) { _thresh = noc; }

   /**
    * Sets the number of clusters to be formed
    *
    * @param noc An integer.
    */
   public void setNumberOfClusters(int noc) { _numberOfClusters = noc; }

   /**
    * Sets the verbose flag
    *
    * @param b A value for the verbose flag.
    */
   public void setVerbose(boolean b) { _verbose = b; }

   //~ Inner Classes ***********************************************************

   /**
    * <p>Title: cRank_Comparator</p>
    *
    * <p>Description: Comparator object for 2 <code>Object[]</codE>. The 2
    * arrays should be at least of length 4 and have a <codE>Double</code>
    * object in index 2 and a <code>Integer</code> object in index 3.</p>
    *
    * <p>Copyright: Copyright (c) 2006</p>
    *
    * <p>Company:</p>
    *
    * @author  D. Searsmith
    * @version 1.0
    */
   private class cRank_Comparator implements java.util.Comparator {

      /**
       * The small deviation allowed in double comparisons.
       */
      public cRank_Comparator() { }

      /**
       * Compares <code>o1</code> amd <code>o2</code>. Both <code>o1</code> amd
       * <code>o2</code> are expected to be arrays of Objects, at least of
       * length 4. The object at index 2 is expected to be a <code>Double</code>
       * and the object at index 3 is expected to be an <code.Integer .
       *
       * @param  o1 Object An Object array of at least 4 Objects. The object at
       *            index 2 is expected to be a <code>Double</code> and the
       *            object at index 3 is expected to be an <code.Integer .
       * @param  o2 Object An Object array of at least 4 Objects. The object at
       *            index 2 is expected to be a <code>Double</code> and the
       *            object at index 3 is expected to be an <code.Integer .
       *
       * @return int 0 if both the Double objects and the Integer objects are
       *         equal. Returns 1 if the Double objects are equal and the
       *         Integer object of <code>o1</code> is greater than the Integer
       *         object of <code>o2</code>. Returns 1 if the Double object of
       *         <codE>o1</code> is greater than the Double object of <code>
       *         o2</codE> Returns -1 if the Double objects are equal and the
       *         Integer object of <code>o1</code> is less than the Integer
       *         object of <code>o2</code>. Returns -1 if the Double object of
       *         <code>o1</code> is less than the Double object of <code>
       *         o2</code>.
       */
      public int compare(Object o1, Object o2) {
         Object[] objarr1 = (Object[]) o1;
         Object[] objarr2 = (Object[]) o2;

         if (
             eq(((Double) objarr1[2]).doubleValue(),
                   ((Double) objarr2[2]).doubleValue())) {

            if (
                ((Integer) objarr1[3]).intValue() >
                   ((Integer) objarr2[3]).intValue()) {
               return 1;
            } else if (
                       ((Integer) objarr1[3]).intValue() <
                          ((Integer) objarr2[3]).intValue()) {
               return -1;
            } else {
               return 0;
            }
         } else if (
                    ((Double) objarr1[2]).doubleValue() >
                       ((Double) objarr2[2]).doubleValue()) {
            return 1;
         } else {
            return -1;
         }
      } // end method compare

      public boolean eq(double a, double b) { return a == b; }

      /**
       * Verifies whether <codE>o</code> equals to this comparator.
       *
       * @param  o An object to be compared to this comparator
       *
       * @return true if <codE>o</code> is an instance of cRank_Comparator
       */
      public boolean equals(Object o) { return o instanceof cRank_Comparator; }
   } // end class cRank_Comparator

} // end class HAC

