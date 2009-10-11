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

package org.seasr.datatypes.table.transformations;

import org.seasr.datatypes.table.*;
import org.seasr.datatypes.table.basic.*;

public class ScalingTransformation implements ReversibleTransformation {

      private int[] indices;
      private double[] from_min, from_max, to_min, to_max;


	/** Constructor 1

		This allows the from_min and from_max values to be explicitly stated
		for each column to transform
	*/

      public ScalingTransformation(int[] indices, double[] from_min,
	  	double[] from_max, double[] to_min, double[] to_max) {

         this.indices = indices;
         this.from_min = from_min;
         this.from_max = from_max;
         this.to_min = to_min;
         this.to_max = to_max;

      }

	/**Constructor 2
		This will automatically determing the from_min and from_max from the
		data in the table
		*/
	public ScalingTransformation(int[] indices, double[] to_min,
			double[] to_max, Table sourceData) {

         this.indices = indices;
         this.to_min = to_min;
         this.to_max = to_max;

		from_min=new double[to_min.length];
		from_max=new double[to_min.length];

		int numRows=sourceData.getNumRows();
		int numRelevantCols=indices.length;
		int i,j;
		for(i=0;i<numRelevantCols;i++){
			from_max[i]=sourceData.getDouble(0,indices[i]);
			from_min[i]=sourceData.getDouble(0,indices[i]);
		}



		for(i=0;i<numRelevantCols;i++){
			for(j=0;j<numRows;j++){
				if(sourceData.getDouble(j,indices[i])>from_max[i])
					from_max[i]=sourceData.getDouble(j,indices[i]);
				if(sourceData.getDouble(j,indices[i])<from_min[i])
					from_min[i]=sourceData.getDouble(j,indices[i]);
			}
		}
	}


      public boolean transform(MutableTable table) {

         if (indices.length == 0 || table.getNumRows() == 0) {
            // no transformation is necessary
            return true;
         }

         for (int count = 0; count < indices.length; count++) {

            int index = indices[count];

            if (index < 0) // this column wasn't selected for scaling
               continue;

            double[] data = new double[table.getNumRows()];

            double from_range = from_max[count] - from_min[count];
            double to_range = to_max[count] - to_min[count];

            double d;

            if (from_range == 0) { // no variance in data...

               d = table.getDouble(0, index);

               if (d >= to_min[count] && d <= to_max[count]) {
                  // data is in new range; leave it alone
                  for (int j = 0; j < data.length; j++)
                     data[j] = table.getDouble(j, index);
               }
               else {
                  // data is out of new range; set to min
                  for (int j = 0; j < data.length; j++)
                     data[j] = to_min[count];
               }

            }
            else { // ordinary data; scale away!

               for (int j = 0; j < data.length; j++) {
                  d = table.getDouble(j, index);
                  data[j] = (d - from_min[count])*to_range/from_range
                          + to_min[count];
               }

            }

            String columnLabel = table.getColumnLabel(index);
            String columnComment = table.getColumnComment(index);
            table.setColumn(new DoubleColumn(data), index);
            table.setColumnLabel(columnLabel, index);
            table.setColumnComment(columnComment, index);
         }

         return true;

      }

	  public boolean untransform(MutableTable table){

			int[] new_indices=indices;
			double[] newFromMax=to_max;
			double[] newFromMin=to_min;
			double[] newToMax=from_max;
			double[] newToMin=from_min;

		  	//if this is a prediction table, untransform any predictions
			//as if they were outputs by adding the appropriate prediction
			//columns to the 'new_indices' array

			if(table instanceof PredictionTable){
				PredictionTable pt=(PredictionTable) table;
				int[] predSet=pt.getPredictionSet();
				if(predSet!=null){
					int numTransformedOutputs=0;
					int i,j,k;
					for(i=0;i<pt.getNumOutputFeatures();i++){
						for(j=0;j<indices.length;j++){
							if(pt.getOutputFeatures()[i]==indices[j]){
								numTransformedOutputs++;
							}
						}
					}
					i=indices.length+numTransformedOutputs;
					new_indices=new int[i];
					newFromMax=new double[i];
					newFromMin=new double[i];
					newToMax=new double[i];
					newToMin=new double[i];

					for(i=0;i<indices.length;i++){
						new_indices[i]=indices[i];
						newFromMax[i]=to_max[i];
						newFromMin[i]=to_min[i];
						newToMax[i]=from_max[i];
						newToMin[i]=from_min[i];
					}
					for(k=0;k<pt.getNumOutputFeatures();k++){
						for(j=0;j<indices.length;j++){
							if(pt.getOutputFeatures()[k]==indices[j]){
								new_indices[i]=pt.getPredictionSet()[k];
								newFromMax[i]=to_max[j];
								newFromMin[i]=to_min[j];
								newToMax[i]=from_max[j];
								newToMin[i]=from_min[j];
								i++;
							}
						}
					}
				}
			}
			//just make a new transformation where the old 'from' values
			//become the 'to' values
		  ScalingTransformation s=
		  		new ScalingTransformation(new_indices,newFromMin,newFromMax,
					newToMin,newToMax);
		  return s.transform(table);
	  }


   }

