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

package org.seasr.datatypes;

/**
 * The <code>Expression</code> interface encapsulates any parsed-string
 * expression that can be evaluated.
 * 
 * <p>
 * Classes that implement this interface should not accept an expression
 * <code>String</code> as an argument to any constructor. Rather, they should
 * rely upon the <code>setExpression</code> method, which should attempt to
 * parse the expression and throw an <code>ExpressionException</code> if there
 * is an error. In this way, a <code>String</code>'s validity as an
 * expression can be determined by simply calling <code>setExpression</code>
 * and catching the exception.
 * </p>
 * 
 * <p>
 * <code>evaluate</code> should return an <code>Object</code> corresponding
 * to an evaluation of the last <code>String</code> specified by <code>
 * setExpression</code>.
 * </p>
 * 
 * @author gpape
 * @version $Revision: 1.3 $, $Date: 2006/07/27 14:35:52 $
 */
public interface Expression {

	// ~ Methods
	// *****************************************************************

	/**
	 * Attempts to evaluate the last <code>String</code> specified by <code>
	 * setExpression</code>.
	 * 
	 * @return an appropriate <code>Object</code>
	 * 
	 * @throws ExpressionException
	 *             If the given expression string is invalid
	 */
	public Object evaluate() throws ExpressionException;

	/**
	 * Sets this <code>Expression</code>'s internal state to represent the
	 * given expression <code>String</code>.
	 * 
	 * @param expression
	 *            some expression
	 * 
	 * @throws ExpressionException
	 *             If the given expression string is invalid
	 */
	public void setExpression(String expression) throws ExpressionException;

}
