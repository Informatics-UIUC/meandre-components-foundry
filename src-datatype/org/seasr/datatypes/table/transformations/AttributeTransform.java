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

import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.Transformation;
import org.seasr.datatypes.table.basic.BooleanColumn;
import org.seasr.datatypes.table.basic.ByteColumn;
import org.seasr.datatypes.table.basic.DoubleColumn;
import org.seasr.datatypes.table.basic.FloatColumn;
import org.seasr.datatypes.table.basic.IntColumn;
import org.seasr.datatypes.table.basic.LongColumn;
import org.seasr.datatypes.table.basic.ShortColumn;
import org.seasr.datatypes.table.basic.StringColumn;
import org.seasr.meandre.support.components.transform.attribute.ColumnExpression;

/**
 * <code>AttributeTransform</code> is a <code>Transformation</code> which uses
 * <code>ColumnExpression</code>s to construct new attributes from existing
 * attributes in a <code>Table</code>.
 */
@SuppressWarnings("serial")
public class AttributeTransform implements Transformation {

   private final Object[] constructions;

   public AttributeTransform(Object[] constructions) {
      this.constructions = constructions;
   }

   public boolean transform(MutableTable table) {

      if (constructions == null || constructions.length == 0)
         return true;

      for (int i = 0; i < constructions.length; i++) {

         ColumnExpression exp = new ColumnExpression(table);
         Construction current = (Construction)constructions[i];
		 boolean [] missing;
         Object evaluation = null;
         try {
            exp.setExpression(current.expression);
            evaluation = exp.evaluate();
            missing = exp.getMissingValues();
         }
         catch(Exception e) {
            e.printStackTrace();
            return false;
         }

         switch (exp.evaluateType()) {

            case ColumnExpression.TYPE_BOOLEAN: {
               boolean[] data = (boolean[])evaluation;
			   BooleanColumn col = new BooleanColumn(data);
               col.setMissingValues(missing);
               table.addColumn(col);
               } break;

            case ColumnExpression.TYPE_BYTE:{
               byte[] data = (byte[])evaluation;
			   ByteColumn col = new ByteColumn(data);
			   col.setMissingValues(missing);
               table.addColumn(col);
               } break;

            case ColumnExpression.TYPE_DOUBLE:{
               double[] data = (double[])evaluation;
			   DoubleColumn col = new DoubleColumn(data);
			   col.setMissingValues(missing);
			   table.addColumn(col);
              } break;

            case ColumnExpression.TYPE_FLOAT:{
               float[] data = (float[])evaluation;
			   FloatColumn col = new FloatColumn(data);
			   col.setMissingValues(missing);
			   table.addColumn(col);
               } break;

            case ColumnExpression.TYPE_INTEGER:{
               int[] data = (int[])evaluation;
			   IntColumn col = new IntColumn(data);
			   col.setMissingValues(missing);
			   table.addColumn(col);
               } break;

            case ColumnExpression.TYPE_LONG:{
               long[] data = (long[])evaluation;
			   LongColumn col = new LongColumn(data);
			   col.setMissingValues(missing);
			   table.addColumn(col);
               } break;

            case ColumnExpression.TYPE_SHORT:{
               short[] data = (short[])evaluation;
			   ShortColumn col = new ShortColumn(data);
			   col.setMissingValues(missing);
			   table.addColumn(col);
               } break;

            case ColumnExpression.TYPE_STRING:{
               String[] data = (String[]) evaluation;
			   StringColumn col = new StringColumn(data);
			   col.setMissingValues(missing);
			   table.addColumn(col);
           	   } break;

            default:
               return false;
         }

         table.setColumnLabel(current.label, table.getNumColumns() - 1);

      }

      return true;
   }
}
