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

package org.seasr.meandre.support.components.transform.attribute;

import java.util.HashMap;

import org.seasr.datatypes.datamining.Expression;
import org.seasr.datatypes.datamining.ExpressionException;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.Table;


/**
 * A <code>ColumnExpression</code> object encapsulates a single mathematical
 * expression of arbitrary length in which columns of a <code>Table</code> are
 * combined by various operators. It also encapsulates a single string object
 * which can not be combined with operators at the moment.
 *
 * @author  gpape
 * @version $Revision: 1.8 $, $Date: 2006/09/01 18:53:48 $
 */
public class ColumnExpression implements Expression {

   //~ Static fields/initializers **********************************************

   /******************************************************************************/
   /* The expression string is parsed recursively.
    */
   /******************************************************************************/

   /** constant for addition */
   static private final int ADDITION = 0;
    /** constant for subtraction */
   static private final int SUBTRACTION = 1;
    /** constant for multiplication */
   static private final int MULTIPLICATION = 2;
    /** constant for division */
   static private final int DIVISION = 3;
    /** constant for modulus */
   static private final int MODULUS = 4;
    /** logical and */
   static private final int AND = 5;
    /** logical or */
   static private final int OR = 6;
   // if you add any more with higher precedence than AND, make sure you update
    // best_type in parse()!

   /** to the power of */
   static private final int POW = 7;
   // SQRT = 8,
   // LOG = 9,
   // LN = 10,
   // EXP = 11;

   /** order */
   static private final int[] order = {
      0, 0, 2, 2, 1, 4, 3, 5
   };

   /******************************************************************************/
   /* The traversal tree for the column expression is composed of subexpressions
    */
   /* that can represent either operations or terminals (columns).
    */
   /******************************************************************************/
   /** boolean datatype */
   static public final int TYPE_BOOLEAN = 0;
    /** byte datatype */
   static public final int TYPE_BYTE = 1;
    /** double datatype */
   static public final int TYPE_DOUBLE = 2;
    /** float datatype */
   static public final int TYPE_FLOAT = 3;
    /** int datatype */
   static public final int TYPE_INTEGER = 4;
    /** long datatype */
   static public final int TYPE_LONG = 5;
    /** short datatype */
   static public final int TYPE_SHORT = 6;
    /** string datatype */
   static public final int TYPE_STRING = 7;

   /** pretty names for log. */
   static private final String LOG = "log";
    /** pretty names for exponential */
   static private final String EXP = "exp";
    /** pretty names for natural log. */
   static private final String NAT_LOG = "ln";
    /** pretty names for absolute value. */
   static private final String ABS = "abs";
    /** pretty names for sine */
   static private final String SIN = "sin";
    /** pretty names for arcsine */
   static private final String ASIN = "asin";
    /** pretty names for cosine */
   static private final String COS = "cos";
    /** pretty names for arc cosine */
   static private final String ACOS = "acos";
    /** pretty names for tangent */
   static private final String TAN = "tan";
    /** pretty names for arctangent */
   static private final String ATAN = "atan";
    /** pretty names for square root */
   static private final String SQRT = "sqrt";
    /** pretty names for negation */
   static private final String NEG = "neg";

   /** constants for log operation */
   static private final int LOG_OP = 0;
    /** constants for exponential operation */
   static private final int EXP_OP = 1;
    /** constants for natural log operation */
   static private final int NAT_LOG_OP = 2;
    /** constants for absolute value operation */
   static private final int ABS_OP = 3;
    /** constants for sine operation */
   static private final int SIN_OP = 4;
    /** constants for arcsine operation */
   static private final int ASIN_OP = 5;
    /** constants for cosine operation */
   static private final int COS_OP = 6;
    /** constants for arccosine operation */
   static private final int ACOS_OP = 7;
    /** constants for tangent operation */
   static private final int TAN_OP = 8;
    /** constants for arctangent operation */
   static private final int ATAN_OP = 9;
    /** constants for square root operation */
   static private final int SQRT_OP = 10;
    /** constants for negation operation */
   static private final int NEG_OP = 11;

   //~ Instance fields *********************************************************

   /** Map column label to table column index. */
   private HashMap labelToIndex;
    /** Map newly created column label to what its table column idex will be */
   private HashMap extraColumnIndexToType;

   /** true for lazy evaluation */
   private boolean lazy = false;

   /** missing values in the table. */
   private boolean[] missingValues;

   /** root of evaluation tree. */
   private Node root;

   /** the table. */
   private Table table;

   //~ Constructors ************************************************************

   /**
    * Constructor for a <code>ColumnExpression</code> object that should use the
    * given <code>Table</code> as its context.
    *
    * @param table the <code>Table</code> that this <code>
    *              ColumnExpression</code> object should reference
    */
   public ColumnExpression(Table table) {
      this.table = table;

      labelToIndex = new HashMap();

      for (int i = 0; i < table.getNumColumns(); i++) {
         labelToIndex.put(table.getColumnLabel(i), new Integer(i));
      }

      this.initMissing();
   }

   //~ Methods *****************************************************************

   /**
    * Get the index of the given column label.
    *
    * @param  label column label
    *
    * @return index in table of this column label
    *
    * @throws ExpressionException when label not found
    */
   private int getIndex(String label) throws ExpressionException {

      StringBuffer buffer = new StringBuffer(label.trim());

      for (int i = 0; i < buffer.length(); i++) {

         if (buffer.charAt(i) == '\\') {
            buffer.deleteCharAt(i);
            i--;
         }
      }

      Integer I = (Integer) labelToIndex.get(buffer.toString());

      if (I == null) {
         throw new ExpressionException("ColumnExpression: column " +
                                       buffer.toString() +
                                       " not found.");
      }

      return I.intValue();

   }

   /**
    * create and initialized the missing values array.
    */
   private void initMissing() {
      this.missingValues = new boolean[table.getNumRows()];

      for (int i = 0; i < missingValues.length; i++) {
         this.missingValues[i] = false;
      }
   }

   /**
    * Parse an expression and return the root of the evaluation tree.
    *
    * @param  expression expression
    *
    * @return root of evaluation tree
    *
    * @throws ExpressionException when expression is malformed
    */
   private Node parse(String expression) throws ExpressionException {

      char c;

      // we're interested in the shallowest operator of least precedence
      // (meaning we break ties by going to the right). if we don't find an
      // operator then we're at a terminal subexpression.

      boolean operator_found = false;
      int depth = 0;
      int max_depth = 0;
      int best_depth = Integer.MAX_VALUE;
      int best_type = AND;
      int best_pos = -1;

      for (int i = 0; i < expression.length(); i++) {

         c = expression.charAt(i);

         switch (c) {

            case '(':
               depth++;

               break;

            case ')':
               depth--;

               break;

            case '+':
               operator_found = true;

               if (
                   depth < best_depth ||
                      depth == best_depth &&
                      order[ADDITION] <= order[best_type]) {
                  best_depth = depth;
                  best_type = ADDITION;
                  best_pos = i;
               }

               break;

            case '-':
               operator_found = true;

               if (
                   depth < best_depth ||
                      depth == best_depth &&
                      order[SUBTRACTION] <= order[best_type]) {
                  best_depth = depth;
                  best_type = SUBTRACTION;
                  best_pos = i;
               }

               break;

            case '*':
               operator_found = true;

               if (
                   depth < best_depth ||
                      depth == best_depth &&
                      order[MULTIPLICATION] <= order[best_type]) {
                  best_depth = depth;
                  best_type = MULTIPLICATION;
                  best_pos = i;
               }

               break;

            case '/':
               operator_found = true;

               if (
                   depth < best_depth ||
                      depth == best_depth &&
                      order[DIVISION] <= order[best_type]) {
                  best_depth = depth;
                  best_type = DIVISION;
                  best_pos = i;
               }

               break;

            case '%':
               operator_found = true;

               if (
                   depth < best_depth ||
                      depth == best_depth &&
                      order[MODULUS] <= order[best_type]) {
                  best_depth = depth;
                  best_type = MODULUS;
                  best_pos = i;
               }

               break;

            case '&':
               operator_found = true;

               if (
                   depth < best_depth ||
                      depth == best_depth &&
                      order[AND] <= order[best_type]) {
                  best_depth = depth;
                  best_type = AND;
                  best_pos = i;
               }

               i++;

               break;

            case '|':
               operator_found = true;

               if (
                   depth < best_depth ||
                      depth == best_depth &&
                      order[OR] <= order[best_type]) {
                  best_depth = depth;
                  best_type = OR;
                  best_pos = i;
               }

               i++;

               break;

            case '\\':
               i++;

               break;

            // vered - merging updates form absic3.
            // power
            case '^':

               // System.out.println("FOUND POW.");
               operator_found = true;

               if (
                   depth < best_depth ||
                      depth == best_depth && order[POW] <= order[best_type]) {
                  best_depth = depth;
                  best_type = POW;
                  best_pos = i;
               }

               break;

            default:

               // check and see if it starts with a function
               StringBuffer sb = new StringBuffer(10);

               for (int z = i; z < expression.length(); z++) {
                  sb.append(expression.charAt(z));
               }

               // this is the rest of the expression
               String ex = sb.toString();

               // System.out.println("DEF:"+ex);
               // the amount to increment i
               int increment = 0;

               // count whitespace at the beginning
               // we will want to skip over this
               for (int z = 0; z < ex.length(); z++) {
                  char cc = ex.charAt(z);

                  if (cc == ' ') {
                     increment++;
                  } else {
                     break;
                  }
               }

               // trim it
               ex = ex.trim();

               // if it starts with one of our functions, use it
               if (
                   ex.startsWith(LOG) ||
                      ex.startsWith(EXP) ||
                      ex.startsWith(SIN) ||
                      ex.startsWith(COS) ||
                      ex.startsWith(TAN) ||
                      ex.startsWith(SQRT) ||
                      ex.startsWith(NAT_LOG) ||
                      ex.startsWith(ABS) ||
                      ex.startsWith(NEG) ||
                      ex.startsWith(ASIN) ||
                      ex.startsWith(ACOS) ||
                      ex.startsWith(ATAN)) {
                  // try to move the current location to the end of the function

                  // the index of the first parenthesis
                  int firstParen = expression.indexOf('(', i);

                  // the number of open paren
                  int numOpen = 1;

                  // if it had the parenthesis
                  if (firstParen != -1) {
                     int k = firstParen + 1;

                     // for each character
                     for (; k < expression.length(); k++) {
                        char cc = expression.charAt(k);

                        if (cc == '(') {

                           // System.out.println("OPEN PAREN:"+k);
                           numOpen++;
                        } else if (cc == ')') {

                           // System.out.println("CLOSE PAREN:"+k);
                           numOpen--;
                        }

                        if (numOpen == 0) {

                           // k++;
                           break;
                        }
                     }

                     // skip the rest of the function
                     i = k + increment;
                  } // end if
               } // end if

               break;
               // end of default section
               // vered - end of merging

         }

         if (depth > max_depth) {
            max_depth = depth;
         }

      } // end for

      if (best_depth > max_depth) {

         // if there were no parentheses (important!)
         best_depth = 0;
      }

      if (operator_found) { // we must recurse

         // first, remove extraneous parentheses, which are going to confuse
         // the parser.
         if (best_depth > 0) {

            for (int i = 0; i < best_depth; i++) {
               expression = expression.trim();
               expression = expression.substring(1, expression.length() - 1);
               best_pos--;
            }

         }

         int offset = 1;

         if (best_type == AND || best_type == OR) {
            offset = 2;
         }

         // now we recurse
         return new OperationNode(best_type,
                                  parse(expression.substring(0, best_pos)
                                                  .trim()),
                                  parse(expression.substring(best_pos + offset,
                                                             expression
                                                                .length())
                                                  .trim()));

      }

      // vered - merging updates from basic3 check if it is a function node
      // before checking if it is a terminal node
      else if (
               expression.indexOf(LOG) != -1 ||
                  expression.indexOf(EXP) != -1 ||
                  expression.indexOf(SIN) != -1 ||
                  expression.indexOf(COS) != -1 ||
                  expression.indexOf(TAN) != -1 ||
                  expression.indexOf(SQRT) != -1 ||
                  expression.indexOf(NAT_LOG) != -1 ||
                  expression.indexOf(ABS) != -1 ||
                  expression.indexOf(NEG) != -1 ||
                  expression.indexOf(ASIN) != -1 ||
                  expression.indexOf(ACOS) != -1 ||
                  expression.indexOf(ATAN) != -1) {

         // System.out.println("***New FunctionNode: "+expression);
         return new FunctionNode(expression);
      }
      // vered - end of merging
      else { // this is a terminal

         // we still have to remove extraneous parentheses, but it's a
         // little different this time
         if (max_depth > 0) {

            for (int i = 0; i < max_depth; i++) {
               expression = expression.trim();
               expression = expression.substring(1, expression.length() - 1);
            }

         }

         try {
            return new TerminalNode(1, 1, Float.parseFloat(expression));
         } catch (Exception e) {

            try {
               float tempmyfloat = 0;

               return new TerminalNode(0,
                                       getIndex(expression),
                                       tempmyfloat);
            } catch (Exception f) {

               /**
                * If failed to find string value in table, create new terminal
                * node just for string Use a flag of 2, distinguish from other
                * terminal nodes
                */
               /*
                *               float tempmyfloat = 0;              return new
                * TerminalNode(2, expression, tempmyfloat);
                */
               // the problem with this ^^^ is that it introduces the ability
               // to parse any string, which breaks syntax checking. for now
               // we're going to throw an exception here instead.
               throw new ExpressionException(expression +
                                             " does not appear to be " +
                                             "a scalar value or an attribute name");
            }

         } // end try-catch
      } // end if-else

   } // end method parse

   /******************************************************************************/
   /* Expression interface
    */
   /******************************************************************************/

   /**
    * Evaluates the current expression and returns a new column in the form of a
    * primitive array cast to an <code>Object</code>.
    *
    * @return the appropriate new column
    *
    * @throws ExpressionException when something goes wrong
    */
   public Object evaluate() throws ExpressionException {

      if (lazy || root == null || table == null) {
         return null;
      }

      Object result = root.evaluate();

      // Now repopulate missing values with the default missing value for that
      // type.
      switch (root.returnType) {

         case TYPE_BOOLEAN: {
            boolean[] tmp = (boolean[]) result;
            boolean missing = table.getMissingBoolean();

            for (int i = 0; i < missingValues.length; i++) {

               if (missingValues[i]) {
                  tmp[i] = missing;
               }
            }
         }

         break;

         case TYPE_BYTE: {
            byte[] tmp = (byte[]) result;
            byte missing = table.getMissingByte();

            for (int i = 0; i < missingValues.length; i++) {

               if (missingValues[i]) {
                  tmp[i] = missing;
               }
            }
         }

         break;

         case TYPE_DOUBLE: {
            double[] tmp = (double[]) result;
            double missing = table.getMissingDouble();

            for (int i = 0; i < missingValues.length; i++) {

               if (missingValues[i]) {
                  tmp[i] = missing;
               }
            }
         }

         break;

         case TYPE_FLOAT: {
            float[] tmp = (float[]) result;
            float missing = (float) table.getMissingDouble();

            for (int i = 0; i < missingValues.length; i++) {

               if (missingValues[i]) {
                  tmp[i] = missing;
               }
            }
         }

         break;

         case TYPE_INTEGER: {
            int[] tmp = (int[]) result;
            int missing = table.getMissingInt();

            for (int i = 0; i < missingValues.length; i++) {

               if (missingValues[i]) {
                  tmp[i] = missing;
               }
            }
         }

         break;

         case TYPE_LONG: {
            long[] tmp = (long[]) result;
            long missing = table.getMissingInt();

            for (int i = 0; i < missingValues.length; i++) {

               if (missingValues[i]) {
                  tmp[i] = missing;
               }
            }
         }

         break;

         case TYPE_SHORT: {
            short[] tmp = (short[]) result;
            short missing = (short) table.getMissingInt();

            for (int i = 0; i < missingValues.length; i++) {

               if (missingValues[i]) {
                  tmp[i] = missing;
               }
            }
         }

         break;
      }

      return result;
   } // end method evaluate

   /**
    * Evaluates the type of column that should be returned by this expression.
    *
    * @return column type of the form <code>ColumnExpression.TYPE_???</code>
    */
   public int evaluateType() { return root.returnType; }

   /**
    * Return a reference to the missing values array.
    *
    * @return a reference to the missing values array
    */
   public boolean[] getMissingValues() { return this.missingValues; }

   /**
    * Get the table.
    *
    * @return the table
    */
   public MutableTable getTable() { return (MutableTable) table; }

   /**
    * Sets this <code>ColumnExpression</code>'s internal state to represent the
    * given column construction expression string.
    *
    * <p>The <code>expression</code> string must be composed entirely of the
    * following elements (ignoring whitespace):</p>
    *
    * <ul>
    *   <li>valid column labels from <code>table</code>,</li>
    *   <li>valid symbols for column operations, namely:
    *
    *     <ul>
    *       <li><code>+</code> for addition,</li>
    *       <li><code>-</code> for subtraction,</li>
    *       <li><code>*</code> for multiplication,</li>
    *       <li><code>/</code> for division,</li>
    *       <li><code>%</code> for modulus,</li>
    *       <li><code>&&</code> for AND, and</li>
    *       <li><code>||</code> for OR,</li>
    *     </ul>
    *
    * and</li>
    *   <li>left and right parentheses: <code>(</code> and <code>)</code>.</li>
    * </ul>
    *
    * <p>In the absence of parentheses, the order of operations is as follows:
    * </p>
    *
    * <ul>
    *   <li>AND,</li>
    *   <li>OR,</li>
    *   <li>multiplication and division,</li>
    *   <li>modulus,</li>
    *   <li>addition and subtraction.</li>
    * </ul>
    *
    * Note that AND and OR can only be applied to boolean columns.
    *
    * <p>Should a column label contain whitespace and/or a symbol for a column
    * operator (such as a hyphen, which will be interpreted as a subtraction
    * operator), every such character must be preceded by a backslash in the
    * <code>expression</code> string. For example, a column label <code>p-adic
    * number</code> should appear in the <code>expression</code> string as
    * <code>p\-adic\ number</code>.</p>
    *
    * @param  expression an expression which, if valid, will specify the
    *                    behavior of this <code>ColumnExpression</code> object
    *
    * @throws ExpressionException when something goes wrong
    */
   public void setExpression(String expression) throws ExpressionException {
      root = parse(expression);
   }

   /**
    * Sets this <code>ColumnExpression</code>'s internal state to <b>lazily</b>
    * represent the given construction expression string, meaning that only type
    * evaluation is allowed. This is useful for constructing delayed
    * construction transformations using this <code>ColumnExpression</code>'s
    * <code>Table</code>.
    *
    * <p>Obviously, <code>newColumns</code> and <code>newColumnTypes</code> must
    * have the same length.</p>
    *
    * @param  expression     a construction expression string
    * @param  newColumns     an array of potential new column labels
    * @param  newColumnTypes an array specifying those labels' types
    *
    * @throws ExpressionException when something goes wrong
    */
   public void setLazyExpression(String expression,
                                 String[] newColumns,
                                 int[] newColumnTypes)
      throws ExpressionException {

      lazy = true;
      extraColumnIndexToType = new HashMap();

      if (newColumns == null || newColumnTypes == null) {
         root = parse(expression);

         return;
      }

      int numCols = table.getNumColumns();

      for (int i = 0; i < newColumns.length; i++) {

         // System.out.println("adding " + newColumns[i]);
         labelToIndex.put(newColumns[i], new Integer(i + numCols));
         extraColumnIndexToType.put(new Integer(i + numCols),
                                    new Integer(newColumnTypes[i]));
      }

      root = parse(expression);

   }

   /**
    * Set the table.
    *
    * @param mt the table
    */
   public void setTable(MutableTable mt) {
      table = mt;

      labelToIndex = new HashMap();

      for (int i = 0; i < table.getNumColumns(); i++) {
         labelToIndex.put(table.getColumnLabel(i), new Integer(i));
      }

      this.initMissing();
   }

   /******************************************************************************/
   /* Evaluation traverses the tree recursively starting at the root.
    */
   /******************************************************************************/

   /**
    * Returns a formatted representation of the expression represented by this
    * object.
    *
    * @return the formatted expression, including column labels
    */
   @Override
public String toString() { return root.toString(); }

   //~ Inner Classes ***********************************************************

   /**
    * The evaluation tree is made up of nodes.
    *
    * @author  $Author: clutter $
    * @version $Revision: 1.8 $, $Date: 2006/09/01 18:53:48 $
    */
   private abstract class Node {
      /** datatype of value returned from evaluate */
      protected int returnType = 0;

       /**
        * Evaluate
        * @return result of evaluation
        * @throws ExpressionException
        */
      public abstract Object evaluate() throws ExpressionException;

       /**
        * Get a nicely formatted description of this Node
        * @return nicely formatted description of this Node
        */
      @Override
    public abstract String toString();

   }

   /**
    * A FunctionNode is a mathematical function on another node. Currently log,
    * exp, ln, abs, sin, asin, cos, acos, tan, atan, sqrt, and neg are
    * supported.
    *
    * @author  $Author: clutter $
    * @version $Revision: 1.8 $, $Date: 2006/09/01 18:53:48 $
    */
   private class FunctionNode extends Node {

      /** the argument. */
      private Node argument;

      /** the operation. */
      private int operation;

      /**
       * Creates a new FunctionNode object.
       *
       * @param  expression the expression
       *
       * @throws ExpressionException when something goes wrong
       */
      FunctionNode(String expression) throws ExpressionException {

         // all the functions return doubles
         returnType = TYPE_DOUBLE;

         expression = expression.trim();

         // it is a log
         if (expression.startsWith(LOG)) {
            operation = LOG_OP;

            // remove the log part of the expression
            String tmpExp =
               expression.substring(LOG.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         }
         // it is an exp (e^x)
         else if (expression.startsWith(EXP)) {
            operation = EXP_OP;

            // remove the exp part of the expression
            String tmpExp =
               expression.substring(EXP.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(NAT_LOG)) {
            operation = NAT_LOG_OP;

            String tmpExp =
               expression.substring(NAT_LOG.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(ABS)) {
            operation = ABS_OP;

            String tmpExp =
               expression.substring(ABS.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(SIN)) {
            operation = SIN_OP;

            String tmpExp =
               expression.substring(SIN.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(ASIN)) {
            operation = ASIN_OP;

            String tmpExp =
               expression.substring(ASIN.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(COS)) {
            operation = COS_OP;

            String tmpExp =
               expression.substring(COS.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(ACOS)) {
            operation = ACOS_OP;

            String tmpExp =
               expression.substring(ACOS.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(TAN)) {
            operation = TAN_OP;

            String tmpExp =
               expression.substring(TAN.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(ATAN)) {
            operation = ATAN_OP;

            String tmpExp =
               expression.substring(ATAN.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(SQRT)) {
            operation = SQRT_OP;

            String tmpExp =
               expression.substring(SQRT.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         } else if (expression.startsWith(NEG)) {
            operation = NEG_OP;

            String tmpExp =
               expression.substring(NEG.length() + 1,
                                    expression.length() - 1);

            // parse the argument
            argument = parse(tmpExp);
         }
         /*      else if (expression.startsWith(POW)) {
          *      operation = POW_OP;     String tmpExp =
          * expression.substring(POW.length() + 1,
          *            expression.length() - 1);     // parse the argument
          * argument = parse(tmpExp);   }*/
         else {
            throw new ExpressionException("FunctionNode: not an expression " +
                                          expression);
         }
      }

       /**
        * Evaluate
        *
        * @return result of evaluation
        * @throws ncsa.d2k.modules.core.datatype.ExpressionException when
        * something goes wrong
        *
        */
       @Override
    public Object evaluate() throws ExpressionException {
           double[] retVal = new double[table.getNumRows()];
           double[] arg;

           // evaluate the argument and copy all of its results into a double
           // array for simplicity later
           switch (argument.returnType) {

               case TYPE_BOOLEAN:
                   throw new ExpressionException("FunctionNode: Functions do not evaluate to boolean values.");

               case TYPE_DOUBLE:
                   arg = (double[]) argument.evaluate();

                   break;

               case TYPE_FLOAT:

                   float[] ar = (float[]) argument.evaluate();
                   arg = new double[ar.length];

                   for (int i = 0; i < ar.length; i++) {
                       arg[i] = ar[i];
                   }

                   break;

               case TYPE_LONG:

                   long[] l = (long[]) argument.evaluate();
                   arg = new double[l.length];

                   for (int i = 0; i < l.length; i++) {
                       arg[i] = l[i];
                   }

                   break;

               case TYPE_BYTE:

                   byte[] b = (byte[]) argument.evaluate();
                   arg = new double[b.length];

                   for (int i = 0; i < b.length; i++) {
                       arg[i] = b[i];
                   }

                   break;

               case TYPE_INTEGER:

                   int[] ia = (int[]) argument.evaluate();
                   arg = new double[ia.length];

                   for (int i = 0; i < ia.length; i++) {
                       arg[i] = ia[i];
                   }

                   break;

               case TYPE_SHORT:

                   short[] s = (short[]) argument.evaluate();
                   arg = new double[s.length];

                   for (int i = 0; i < s.length; i++) {
                       arg[i] = s[i];
                   }

                   break;

               default:
                   throw new ExpressionException("FunctionNode: Cannot use return type.");
           }

           switch (operation) {

               case LOG_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.log(arg[i]) / Math.log(10);
                   }

                   break;

               case SIN_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.sin(arg[i]);
                   }

                   break;

               case COS_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.cos(arg[i]);
                   }

                   break;

               case TAN_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.tan(arg[i]);
                   }

                   break;

               case SQRT_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.sqrt(arg[i]);
                   }

                   break;

               case ABS_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.abs(arg[i]);
                   }

                   break;

               case EXP_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.exp(arg[i]);
                   }

                   break;

               case ASIN_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.asin(arg[i]);
                   }

                   break;

               case ACOS_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.acos(arg[i]);
                   }

                   break;

               case ATAN_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = Math.atan(arg[i]);
                   }

                   break;

               case NEG_OP:

                   for (int i = 0; i < arg.length; i++) {
                       retVal[i] = arg[i] * -1;
                   }

                   break;

               default:
                   throw new ExpressionException("FunctionNode: Function not recognized.");

           }

           return retVal;
       } // end method evaluate

       /**
        * Get a nicely formatted description of this Node
        *
        * @return nicely formatted description of this Node
        */
       @Override
    public String toString() {
           StringBuffer sb = new StringBuffer();

           switch (operation) {

               case LOG_OP:
                   sb.append(LOG);

                   break;

               case SIN_OP:
                   sb.append(SIN);

                   break;

               case COS_OP:
                   sb.append(COS);

                   break;

               case TAN_OP:
                   sb.append(TAN);

                   break;

               case SQRT_OP:
                   sb.append(SQRT);

                   break;

               case ABS_OP:
                   sb.append(ABS);

                   break;

               case EXP_OP:
                   sb.append(EXP);

                   break;

               case ASIN_OP:
                   sb.append(ASIN);

                   break;

               case ACOS_OP:
                   sb.append(ACOS);

                   break;

               case ATAN_OP:
                   sb.append(ATAN);

                   break;

               case NEG_OP:
                   sb.append(NEG);

                   break;
                   /*        case POW_OP:
                   *        sb.append(POW);       break;
                   */
           }

           sb.append('(');
           sb.append(argument.toString());
           sb.append(')');

           return sb.toString();
       } // end method toString
   } // FunctionNode

   /**
    * Node that impelments a mathematical operation.
    *
    * @author  $Author: clutter $
    * @version $Revision: 1.8 $, $Date: 2006/09/01 18:53:48 $
    */
   private class OperationNode extends Node {

      /** left child */
      private final Node left;
       /** right child */
      private final Node right;

      /** type of operation. */
      protected int type;

      /**
       * Constructor.
       *
       * @param  type  operation type
       * @param  left  left hand side
       * @param  right right hand side
       *
       * @throws ExpressionException when something goes wrong
       */
      public OperationNode(int type, Node left, Node right)
         throws ExpressionException {
         this.type = type;
         this.left = left;
         this.right = right;

         switch (left.returnType) {

            case TYPE_BOOLEAN:

               if (type != AND && type != OR) {
                  throw new ExpressionException("ColumnExpression: Illegal expression.");
               }

               switch (right.returnType) {

                  case TYPE_BOOLEAN:
                     returnType = TYPE_BOOLEAN;

                     break;

                  default:
                     throw new ExpressionException("No operator can combine a numeric and a boolean column.");

               }

               break;

            case TYPE_BYTE:

               if (type == AND || type == OR) {
                  throw new ExpressionException("ColumnExpression: illegal expression.");
               }

               switch (right.returnType) {

                  case TYPE_BOOLEAN:
                     throw new ExpressionException("No operator can combine a numeric and a boolean column.");

                  case TYPE_BYTE:
                     returnType = TYPE_BYTE;

                     break;

                  case TYPE_DOUBLE:
                     returnType = TYPE_DOUBLE;

                     break;

                  case TYPE_FLOAT:
                     returnType = TYPE_FLOAT;

                     break;

                  case TYPE_INTEGER:
                     returnType = TYPE_INTEGER;

                     break;

                  case TYPE_LONG:
                     returnType = TYPE_LONG;

                     break;

                  case TYPE_SHORT:
                     returnType = TYPE_SHORT;

                     break;

               }

               break;

            case TYPE_DOUBLE:

               if (type == AND || type == OR) {
                  throw new ExpressionException("ColumnExpression: illegal expression.");
               }

               switch (right.returnType) {

                  case TYPE_BOOLEAN:
                     throw new ExpressionException("No operator can combine a numeric and a boolean column.");

                  case TYPE_BYTE:
                  case TYPE_DOUBLE:
                  case TYPE_FLOAT:
                  case TYPE_INTEGER:
                  case TYPE_LONG:
                  case TYPE_SHORT:
                     returnType = TYPE_DOUBLE;

                     break;

               }

               break;

            case TYPE_FLOAT:

               if (type == AND || type == OR) {
                  throw new ExpressionException("ColumnExpression: illegal expression.");
               }

               switch (right.returnType) {

                  case TYPE_BOOLEAN:
                     throw new ExpressionException("No operator can combine a numeric and a boolean column.");

                  case TYPE_BYTE:
                  case TYPE_FLOAT:
                  case TYPE_INTEGER:
                  case TYPE_SHORT:
                     returnType = TYPE_FLOAT;

                     break;

                  case TYPE_DOUBLE:
                  case TYPE_LONG:
                     returnType = TYPE_DOUBLE;

                     break;

               }

               break;

            case TYPE_INTEGER:

               if (type == AND || type == OR) {
                  throw new ExpressionException("ColumnExpression: illegal expression.");
               }

               switch (right.returnType) {

                  case TYPE_BOOLEAN:
                     throw new ExpressionException("No operator can combine a numeric and a boolean column.");

                  case TYPE_BYTE:
                  case TYPE_INTEGER:
                  case TYPE_SHORT:
                     returnType = TYPE_INTEGER;

                     break;

                  case TYPE_FLOAT:
                     returnType = TYPE_FLOAT;

                     break;

                  case TYPE_DOUBLE:
                     returnType = TYPE_DOUBLE;

                     break;

                  case TYPE_LONG:
                     returnType = TYPE_LONG;

                     break;

               }

               break;

            case TYPE_LONG:

               if (type == AND || type == OR) {
                  throw new ExpressionException("ColumnExpression: illegal expression.");
               }

               switch (right.returnType) {

                  case TYPE_BOOLEAN:
                     throw new ExpressionException("No operator can combine a numeric and a boolean column.");

                  case TYPE_BYTE:
                  case TYPE_INTEGER:
                  case TYPE_LONG:
                  case TYPE_SHORT:
                     returnType = TYPE_LONG;

                     break;

                  case TYPE_FLOAT:
                  case TYPE_DOUBLE:
                     returnType = TYPE_DOUBLE;

                     break;

               }

               break;

            case TYPE_SHORT:

               if (type == AND || type == OR) {
                  throw new ExpressionException("ColumnExpression: illegal expression.");
               }

               switch (right.returnType) {

                  case TYPE_BOOLEAN:
                     throw new ExpressionException("No operator can combine a numeric and a boolean column.");

                  case TYPE_BYTE:
                  case TYPE_SHORT:
                     returnType = TYPE_SHORT;

                     break;

                  case TYPE_INTEGER:
                     returnType = TYPE_INTEGER;

                     break;

                  case TYPE_LONG:
                     returnType = TYPE_LONG;

                     break;

                  case TYPE_FLOAT:
                     returnType = TYPE_FLOAT;

                     break;

                  case TYPE_DOUBLE:
                     returnType = TYPE_DOUBLE;

                     break;

               }

               break;

         }

      }


       /**
        * Evaluate
        *
        * @return result of evaluation
        * @throws ncsa.d2k.modules.core.datatype.ExpressionException when
        * something goes wrong
        */
       @Override
    public Object evaluate() throws ExpressionException {

           int numRows = table.getNumRows();
           Object[] output = new Object[numRows];

           switch (left.returnType) {

               case TYPE_BOOLEAN:

                   switch (right.returnType) {

                       case TYPE_BOOLEAN:

                           boolean[] b = new boolean[table.getNumRows()];
                           boolean[] bL = (boolean[]) left.evaluate();
                           boolean[] bR = (boolean[]) right.evaluate();

                           switch (type) {

                               case AND:

                                   for (int i = 0; i < b.length; i++) {
                                       b[i] = bL[i] && bR[i];
                                   }

                                   break;

                               case OR:

                                   for (int i = 0; i < b.length; i++) {
                                       b[i] = bL[i] || bR[i];
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return b;

                       default:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                   }

               case TYPE_BYTE:

                   switch (right.returnType) {

                       case TYPE_BOOLEAN:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                       case TYPE_BYTE:

                           byte[] b = new byte[table.getNumRows()];
                           byte[] bL = (byte[]) left.evaluate();
                           byte[] bR = (byte[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < b.length; i++) {
                                       b[i] = (byte) (bL[i] + bR[i]);
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < b.length; i++) {
                                       b[i] = (byte) (bL[i] - bR[i]);
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < b.length; i++) {
                                       b[i] = (byte) (bL[i] * bR[i]);
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < b.length; i++) {
                                       b[i] = (byte) (bL[i] / bR[i]);
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < b.length; i++) {
                                       b[i] = (byte) (bL[i] % bR[i]);
                                   }

                                   break;

                               case POW:

                                   for (int i = 0; i < b.length; i++) {
                                       b[i] = (byte) Math.pow(bL[i], bR[i]);
                                   }

                                   break;


                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return b;

                       case TYPE_DOUBLE:

                           double[] d = new double[table.getNumRows()];
                           bL = (byte[]) left.evaluate();

                           double[] dR = (double[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = bL[i] + dR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = bL[i] - dR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = bL[i] * dR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = bL[i] / dR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = bL[i] % dR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(bL[i], dR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return d;

                       case TYPE_FLOAT:

                           float[] f = new float[table.getNumRows()];
                           bL = (byte[]) left.evaluate();

                           float[] fR = (float[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = bL[i] + fR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = bL[i] - fR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = bL[i] * fR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = bL[i] / fR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = bL[i] % fR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) Math.pow(bL[i], fR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return f;

                       case TYPE_INTEGER:

                           int[] I = new int[table.getNumRows()];
                           bL = (byte[]) left.evaluate();

                           int[] iR = (int[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = bL[i] + iR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = bL[i] - iR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = bL[i] * iR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = bL[i] / iR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = bL[i] % iR[i];
                                   }

                                   break;

                               case POW:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) Math.pow(bL[i], iR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return I;

                       case TYPE_LONG:

                           long[] l = new long[table.getNumRows()];
                           bL = (byte[]) left.evaluate();

                           long[] lR = (long[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = bL[i] + lR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = bL[i] - lR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = bL[i] * lR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = bL[i] / lR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = bL[i] % lR[i];
                                   }

                                   break;

                               case POW:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) Math.pow(bL[i], lR[i]);
                                   }

                                   break;


                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return l;

                       case TYPE_SHORT:

                           short[] s = new short[table.getNumRows()];
                           bL = (byte[]) left.evaluate();

                           short[] sR = (short[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (bL[i] + sR[i]);
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (bL[i] - sR[i]);
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (bL[i] * sR[i]);
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (bL[i] / sR[i]);
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (bL[i] % sR[i]);
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) Math.pow(bL[i], sR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return s;

                       default:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                   }

               case TYPE_DOUBLE:

                   switch (right.returnType) {

                       case TYPE_BOOLEAN:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                       case TYPE_BYTE:

                           double[] d = new double[table.getNumRows()];
                           double[] dL = (double[]) left.evaluate();
                           byte[] bR = (byte[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] + bR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] - bR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] * bR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] / bR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] % bR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(dL[i], bR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal Expression.");
                           }

                           return d;

                       case TYPE_DOUBLE:
                           d = new double[table.getNumRows()];
                           dL = (double[]) left.evaluate();

                           double[] dR = (double[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] + dR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] - dR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] * dR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] / dR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] % dR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(dL[i], dR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return d;

                       case TYPE_FLOAT:
                           d = new double[table.getNumRows()];
                           dL = (double[]) left.evaluate();

                           float[] fR = (float[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] + fR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] - fR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] * fR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] / fR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] % fR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(dL[i], fR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return d;

                       case TYPE_INTEGER:
                           d = new double[table.getNumRows()];
                           dL = (double[]) left.evaluate();

                           int[] iR = (int[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] + iR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] - iR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] * iR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] / iR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] % iR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(dL[i], iR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return d;

                       case TYPE_LONG:
                           d = new double[table.getNumRows()];
                           dL = (double[]) left.evaluate();

                           long[] lR = (long[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] + lR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] - lR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] * lR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] / lR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] % lR[i];
                                   }

                                   break;

                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(dL[i], lR[i]);
                                   }

                                   break;


                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return d;

                       case TYPE_SHORT:
                           d = new double[table.getNumRows()];
                           dL = (double[]) left.evaluate();

                           short[] sR = (short[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] + sR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] - sR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] * sR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] / sR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = dL[i] % sR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(dL[i], sR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return d;

                       default:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                   }

               case TYPE_FLOAT:

                   switch (right.returnType) {

                       case TYPE_BOOLEAN:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                       case TYPE_BYTE:

                           float[] f = new float[table.getNumRows()];
                           float[] fL = (float[]) left.evaluate();
                           byte[] bR = (byte[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] + bR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] - bR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] * bR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] / bR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] % bR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) Math.pow(fL[i], bR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal Expression.");
                           }

                           return f;

                       case TYPE_DOUBLE:

                           double[] d = new double[table.getNumRows()];
                           fL = (float[]) left.evaluate();

                           double[] dR = (double[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = fL[i] + dR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = fL[i] - dR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = fL[i] * dR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = fL[i] / dR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = fL[i] % dR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(fL[i], dR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return d;

                       case TYPE_FLOAT:
                           f = new float[table.getNumRows()];
                           fL = (float[]) left.evaluate();

                           float[] fR = (float[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] + fR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] - fR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] * fR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] / fR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] % fR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) Math.pow(fL[i], fR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return f;

                       case TYPE_INTEGER:
                           f = new float[table.getNumRows()];
                           fL = (float[]) left.evaluate();

                           int[] iR = (int[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] + iR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] - iR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] * iR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] / iR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] % iR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) Math.pow(fL[i], iR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return f;

                       case TYPE_LONG:
                           d = new double[table.getNumRows()];
                           fL = (float[]) left.evaluate();

                           long[] lR = (long[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) fL[i] + (double) lR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) fL[i] - (double) lR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) fL[i] * (double) lR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) fL[i] / (double) lR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) fL[i] % (double) lR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(fL[i], lR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return d;

                       case TYPE_SHORT:
                           f = new float[table.getNumRows()];
                           fL = (float[]) left.evaluate();

                           short[] sR = (short[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] + sR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] - sR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] * sR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] / sR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = fL[i] % sR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) Math.pow(fL[i], sR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return f;

                       default:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                   }

               case TYPE_INTEGER:

                   switch (right.returnType) {

                       case TYPE_BOOLEAN:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                       case TYPE_BYTE:

                           int[] I = new int[table.getNumRows()];
                           int[] iL = (int[]) left.evaluate();
                           byte[] bR = (byte[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] + bR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] - bR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] * bR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] / bR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] % bR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) Math.pow(iL[i], bR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal Expression.");
                           }

                           return I;

                       case TYPE_DOUBLE:

                           double[] d = new double[table.getNumRows()];
                           iL = (int[]) left.evaluate();

                           double[] dR = (double[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) iL[i] + dR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) iL[i] - dR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) iL[i] * dR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) iL[i] / dR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) iL[i] % dR[i];
                                   }

                                   break;

                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = Math.pow(iL[i], dR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) d;

                       case TYPE_FLOAT:

                           float[] f = new float[table.getNumRows()];
                           iL = (int[]) left.evaluate();

                           float[] fR = (float[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) iL[i] + fR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) iL[i] - fR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) iL[i] * fR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) iL[i] / fR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) iL[i] % fR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) Math.pow(iL[i], fR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) f;

                       case TYPE_INTEGER:
                           I = new int[table.getNumRows()];
                           iL = (int[]) left.evaluate();

                           int[] iR = (int[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] + iR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] - iR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] * iR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] / iR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] % iR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) Math.pow(iL[i], iR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) I;

                       case TYPE_LONG:

                           long[] l = new long[table.getNumRows()];
                           iL = (int[]) left.evaluate();

                           long[] lR = (long[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) iL[i] + lR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) iL[i] - lR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) iL[i] * lR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) iL[i] / lR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) iL[i] % lR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) Math.pow(iL[i], lR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) l;

                       case TYPE_SHORT:
                           I = new int[table.getNumRows()];
                           iL = (int[]) left.evaluate();

                           short[] sR = (short[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] + (int) sR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] - (int) sR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] * (int) sR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] / (int) sR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = iL[i] % (int) sR[i];
                                   }

                                   break;

                               case POW:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) Math.pow(iL[i], sR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) I;

                       default:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                   }

               case TYPE_SHORT:

                   switch (right.returnType) {

                       case TYPE_BOOLEAN:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                       case TYPE_BYTE:

                           short[] s = new short[table.getNumRows()];
                           short[] sL = (short[]) left.evaluate();
                           byte[] bR = (byte[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] + (short) bR[i]);
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] - (short) bR[i]);
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] * (short) bR[i]);
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] / (short) bR[i]);
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] % (short) bR[i]);
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) Math.pow(sL[i], bR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) s;

                       case TYPE_DOUBLE:

                           double[] d = new double[table.getNumRows()];
                           sL = (short[]) left.evaluate();

                           double[] dR = (double[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) sL[i] + dR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) sL[i] - dR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) sL[i] * dR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) sL[i] / dR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) sL[i] % dR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < d.length; i++) {
                                       d[i] = (double) Math.pow(sL[i], dR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) d;

                       case TYPE_FLOAT:

                           float[] f = new float[table.getNumRows()];
                           sL = (short[]) left.evaluate();

                           float[] fR = (float[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) sL[i] + fR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) sL[i] - fR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) sL[i] * fR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) sL[i] / fR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) sL[i] % fR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < f.length; i++) {
                                       f[i] = (float) Math.pow(sL[i], fR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) f;

                       case TYPE_INTEGER:

                           int[] I = new int[table.getNumRows()];
                           sL = (short[]) left.evaluate();

                           int[] iR = (int[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) sL[i] + iR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) sL[i] - iR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) sL[i] * iR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) sL[i] / iR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) sL[i] % iR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < I.length; i++) {
                                       I[i] = (int) Math.pow(sL[i], iR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) I;

                       case TYPE_LONG:

                           long[] l = new long[table.getNumRows()];
                           sL = (short[]) left.evaluate();

                           long[] lR = (long[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) sL[i] + lR[i];
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) sL[i] - lR[i];
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) sL[i] * lR[i];
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) sL[i] / lR[i];
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) sL[i] % lR[i];
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < l.length; i++) {
                                       l[i] = (long) Math.pow(sL[i], lR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) l;

                       case TYPE_SHORT:
                           s = new short[table.getNumRows()];
                           sL = (short[]) left.evaluate();

                           short[] sR = (short[]) right.evaluate();

                           switch (type) {

                               case ADDITION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] + sR[i]);
                                   }

                                   break;

                               case SUBTRACTION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] - sR[i]);
                                   }

                                   break;

                               case MULTIPLICATION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] * sR[i]);
                                   }

                                   break;

                               case DIVISION:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] / sR[i]);
                                   }

                                   break;

                               case MODULUS:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) (sL[i] % sR[i]);
                                   }

                                   break;


                               case POW:

                                   for (int i = 0; i < s.length; i++) {
                                       s[i] = (short) Math.pow(sL[i], sR[i]);
                                   }

                                   break;

                               default:
                                   throw new ExpressionException("ColumnExpression: Illegal expression.");
                           }

                           return (Object) s;

                       default:
                           throw new ExpressionException("ColumnExpression: Illegal expression.");

                   } // switch(right return type)

           } // switch(left return type)

           throw new ExpressionException("ColumnExpression: apparently malformed expression.");
           // return null;

       } // evaluate

      /**
       * Format contents nicely.
       *
       * @return String description of this
       */
      public String toString() {

         StringBuffer buffer = new StringBuffer();

         if (left instanceof OperationNode) {
            buffer.append('(');
            buffer.append(left);
            buffer.append(')');
         } else {
            buffer.append(left);
         }

         buffer.append(' ');

         switch (type) {

            case ADDITION:
               buffer.append('+');

               break;

            case SUBTRACTION:
               buffer.append('-');

               break;

            case MULTIPLICATION:
               buffer.append('*');

               break;

            case DIVISION:
               buffer.append('/');

               break;

            case MODULUS:
               buffer.append('%');

               break;

            case AND:
               buffer.append("&&");

               break;

            case OR:
               buffer.append("||");

               break;

            // vered - merging updates form basic3
            case POW:
               buffer.append("^");

               break;
               // vered - end merging

         }

         buffer.append(' ');

         if (right instanceof OperationNode) {
            buffer.append('(');
            buffer.append(right);
            buffer.append(')');
         } else {
            buffer.append(right);
         }

         return buffer.toString();

      } // end method toString

      ////////////////////////////////////////////////////////////////////////////////
      // end operation evaluation
      // //
      ////////////////////////////////////////////////////////////////////////////////

   } // end class OperationNode

   /**
    * A node that is not an operation.
    *
    * @author  $Author: clutter $
    * @version $Revision: 1.8 $, $Date: 2006/09/01 18:53:48 $
    */
   private class TerminalNode extends Node {
      /** column index */
      private int column;
      private final int myownflag;
       /** scalar value */
      private float myownscalarvalue;
       /** non-scalar value */
      private String myValue;

      /**
       * Constructor.
       *
       * @param  myownflag        flag
       * @param  column           column index
       * @param  myownscalarvalue scalar value
       *
       * @throws ExpressionException when something goes wrong
       */
      public TerminalNode(int myownflag, int column, float myownscalarvalue)
         throws ExpressionException {

         this.myownflag = myownflag;

         if (myownflag == 0) {
            this.column = column;

            if (column < table.getNumColumns()) { // in table

               switch (table.getColumnType(column)) {

                  case ColumnTypes.BOOLEAN:
                     returnType = TYPE_BOOLEAN;

                     break;

                  case ColumnTypes.BYTE:
                     returnType = TYPE_BYTE;

                     break;

                  case ColumnTypes.DOUBLE:
                     returnType = TYPE_DOUBLE;

                     break;

                  case ColumnTypes.FLOAT:
                     returnType = TYPE_FLOAT;

                     break;

                  case ColumnTypes.INTEGER:
                     returnType = TYPE_INTEGER;

                     break;

                  case ColumnTypes.LONG:
                     returnType = TYPE_LONG;

                     break;

                  case ColumnTypes.SHORT:
                     returnType = TYPE_SHORT;

                     break;

                  default:
                     throw new ExpressionException("ColumnExpression supports only numeric and boolean columns.");
               }

            } else { // in array passed to setLazyExpression

               returnType =
                  ((Integer) extraColumnIndexToType.get(new Integer(column)))
                     .intValue();

            }
         } else {
            returnType = TYPE_FLOAT;
            this.myownscalarvalue = myownscalarvalue;
         }
      }

      /**
       * Creates a new TerminalNode object.
       *
       * @param  myownflag  flag
       * @param  expression expression
       * @param  tempFloat  float (not used)
       *
       * @throws ExpressionException TerminalNode constructor for solo String
       *                             values
       */
      public TerminalNode(int myownflag, String expression, float tempFloat)
         throws ExpressionException {

         this.myownflag = myownflag;
         myValue = expression;
         returnType = TYPE_STRING;

      }

      /**
       * evaluate this node.
       *
       * @return array containing evaluation results
       *
       * @throws ExpressionException when something goes wrong
       */
      public Object evaluate() throws ExpressionException {

         if (myownflag == 0) {

            switch (returnType) {

               case TYPE_BOOLEAN:

                  boolean[] b = new boolean[table.getNumRows()];

                  for (int i = 0; i < b.length; i++) {

                     if (table.isValueMissing(i, column)) {
                        missingValues[i] = true;
                     }

                     b[i] = table.getBoolean(i, column);
                  }

                  return (Object) b;

               case TYPE_BYTE:

                  byte[] bb = new byte[table.getNumRows()];

                  for (int i = 0; i < bb.length; i++) {

                     if (table.isValueMissing(i, column)) {
                        missingValues[i] = true;
                     }

                     bb[i] = table.getByte(i, column);
                  }

                  return (Object) bb;

               case TYPE_DOUBLE:

                  double[] d = new double[table.getNumRows()];

                  for (int i = 0; i < d.length; i++) {

                     if (table.isValueMissing(i, column)) {
                        missingValues[i] = true;
                     }

                     d[i] = table.getDouble(i, column);
                  }

                  return (Object) d;

               case TYPE_FLOAT:

                  float[] f = new float[table.getNumRows()];

                  for (int i = 0; i < f.length; i++) {

                     if (table.isValueMissing(i, column)) {
                        missingValues[i] = true;
                     }

                     f[i] = table.getFloat(i, column);
                  }

                  return (Object) f;

               case TYPE_INTEGER:

                  int[] I = new int[table.getNumRows()];

                  for (int i = 0; i < I.length; i++) {

                     if (table.isValueMissing(i, column)) {
                        missingValues[i] = true;
                     }

                     I[i] = table.getInt(i, column);
                  }

                  return (Object) I;

               case TYPE_LONG:

                  long[] l = new long[table.getNumRows()];

                  for (int i = 0; i < l.length; i++) {

                     if (table.isValueMissing(i, column)) {
                        missingValues[i] = true;
                     }

                     l[i] = table.getLong(i, column);
                  }

                  return (Object) l;

               case TYPE_SHORT:

                  short[] s = new short[table.getNumRows()];

                  for (int i = 0; i < s.length; i++) {

                     if (table.isValueMissing(i, column)) {
                        missingValues[i] = true;
                     }

                     s[i] = table.getShort(i, column);
                  }

                  return (Object) s;

               default:
                  throw new ExpressionException("There has been an error in ColumnExpression. Double-check your expression.");

            }

         } else if (myownflag == 1) {
            float[] myf = new float[table.getNumRows()];

            for (int i = 0; i < myf.length; i++) {
               myf[i] = myownscalarvalue;
            }

            return (Object) myf;
         } else {
            String[] p = new String[table.getNumRows()];

            for (int i = 0; i < p.length; i++) {
               p[i] = myValue;
            }

            return (Object) p;

         }

      } // end method evaluate

      /**
       * Format description nicely.
       *
       * @return description
       */
      public String toString() {

         if (myownflag == 0) {

            if (column >= table.getNumColumns()) {
               return "NEW";
            }

            return table.getColumnLabel(column);
         } else {
            return ((String) Float.toString(myownscalarvalue));
         }
      }
   } // end class TerminalNode
} // ColumnExpression
