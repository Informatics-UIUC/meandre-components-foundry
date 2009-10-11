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

import java.util.*;
import org.seasr.datatypes.table.*;
import org.seasr.datatypes.table.basic.*;

/**
 * Encapsulates a (reversible) transformation on a <code>MutableTable</code>
 * that replaces unique nominal column values with unique integers.
 */
public class ReplaceNominalsWithIntsTransform
   implements ReversibleTransformation {

   static final long serialVersionUID = 2772230274372026572L;

   protected int[] indirection;
   protected HashMap[] nominalToInteger, integerToNominal;

   public ReplaceNominalsWithIntsTransform(MutableTable mt) {

	  // how many nominal columns do we have?
	  int numNominalColumns = 0, totalColumns = mt.getNumColumns();

	  for (int i = 0; i < totalColumns; i++) {
		 if (mt.isColumnNominal(i)) {
			numNominalColumns++;
		 }
	  }

	  nominalToInteger = new HashMap[numNominalColumns];
	  integerToNominal = new HashMap[numNominalColumns];

	  // create the indirection lookup for the nominal columns
	  indirection = new int[numNominalColumns];

	  int index = 0;
	  for (int i = 0; i < totalColumns; i++)
		 if (mt.isColumnNominal(i))
			indirection[index++] = i;

	  // replace the columns
	  int numRows = mt.getNumRows(), numItems;
	  String item;
	  for (int i = 0; i < indirection.length; i++) {

		 nominalToInteger[i] = new HashMap();
		 integerToNominal[i] = new HashMap();

		 int col = indirection[i];

		 numItems = 0;
		 for (int j = 0; j < numRows; j++) {
			if (mt.isValueMissing(j, col))
				continue;

			item = mt.getString(j, col);
			if (!nominalToInteger[i].containsKey(item)) {
			   nominalToInteger[i].put(item, new Integer(numItems));
			   integerToNominal[i].put(new Integer(numItems), item);
			   numItems++;
			}

		 }

	  }

	  // transform(mt);

   }

   public String toMappingString(MutableTable mt) {

	  if (nominalToInteger.length == 0 || integerToNominal.length == 0)
		 return "empty transformation: " + super.toString();

	  StringBuffer sb = new StringBuffer();

	  for (int i = 0; i < indirection.length; i++) {

		 sb.append("column '");
		 sb.append(mt.getColumnLabel(indirection[i]));
		 sb.append("':\n");

		 int size = integerToNominal[i].size();
		 for (int j = 0; j < size; j++) {
			sb.append(integerToNominal[i].get(new Integer(j)));
			sb.append(" -> ");
			sb.append(j);
			sb.append('\n');
		 }

	  }

	  return sb.toString();

   }

   public boolean transform(MutableTable mt) {
	  int numRows = mt.getNumRows();
	  String item, label;
	  for (int i = 0; i < indirection.length; i++) {
		 int[] intColumn = new int[numRows];
		 int col = indirection[i];
		 boolean [] missing = new boolean [numRows];
		 for (int j = 0; j < numRows; j++) {
		     if (!mt.isValueMissing(j,col)) {
			 	item = (String)mt.getString(j, col);
			 	intColumn[j] = ((Integer)nominalToInteger[i].get(item)).intValue();
			 	missing[j] = false;
			 } else {
			 	intColumn[j] = mt.getMissingInt();
			 	missing[j] = true;
			 }
		 }

         Column newColumn = ColumnUtilities.toIntColumn(mt.getColumn(col));
         mt.setColumn(newColumn, col);
         for (int pp = 0; pp < numRows ; pp++) {
             mt.setInt (intColumn[pp], pp, col);
         }
         mt.setColumnIsNominal(false,col);
        // System.out.println("set col " + col + " to Nominal");
         mt.setColumnIsScalar(true,col);
	  }

	  return true;
   }

   public boolean untransform(MutableTable mt) {
	  int numRows = mt.getNumRows();
	  Integer item;
	  String label;
	  for (int i = 0; i < indirection.length; i++) {
		 String[] stringColumn = new String[numRows];
		 int col = indirection[i];
		 boolean missing [] = new boolean [numRows];
		 for (int j = 0; j < numRows; j++) {
		    if (!mt.isValueMissing(j,col)) {
				item = new Integer(mt.getInt(j, col));
				stringColumn[j] = (String)integerToNominal[i].get(item);
				missing[j] = false;
			} else {
				stringColumn[j] = mt.getMissingString();
				missing[j] = true;
			}
		 }
		 label = mt.getColumnLabel(col);
		 StringColumn sc = new StringColumn(stringColumn);
		 sc.setMissingValues (missing);
		 mt.setColumn(sc, col);
		 mt.setColumnLabel(label, col);
	  }

	  return true;

   }

}

// QA Anca - added setColumnIsNominal in transform to keep the column nominal
// QA Tom - added support for missing values when needed
