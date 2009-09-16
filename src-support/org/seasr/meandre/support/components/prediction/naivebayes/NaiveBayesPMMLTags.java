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

package org.seasr.meandre.support.components.prediction.naivebayes;

import org.seasr.meandre.support.components.prediction.PMMLTags;


/**
 * Constants for use in PMML markup for NaiveBayes models
 *
 * @author  $author$
 * @version $Revision: 1.2 $, $Date: 2006/08/02 15:07:17 $
 */
public interface NaiveBayesPMMLTags extends PMMLTags {

   //~ Static fields/initializers **********************************************

   /** NaiveBayesModel */
   static public final String NBM = "NaiveBayesModel";

   /** threshold */
   static public final String THRESHOLD = "threshold";

   /** BayesInputs */
   static public final String BAYES_INPUTS = "BayesInputs";

   /** BayesInput */
   static public final String BAYES_INPUT = "BayesInput";

   /** fieldName */
   static public final String FIELD_NAME = "fieldName";

   /** PairCounts */
   static public final String PAIR_COUNTS = "PairCounts";

   /** TargetValueCounts */
   static public final String TARGET_VALUE_COUNTS = "TargetValueCounts";

   /** TargetValueCount */
   static public final String TARGET_VALUE_COUNT = "TargetValueCount";

   /** count */
   static public final String COUNT = "count";

   /** DerivedField */
   static public final String DERIVED_FIELD = "DerivedField";

   /** Discretize */
   static public final String DISCRETIZE = "Discretize";

   /** DiscretizeBin */
   static public final String DISCRETIZE_BIN = "DiscretizeBin";

   /** binValue */
   static public final String BIN_VALUE = "binValue";

   /** Interval */
   static public final String INTERVAL = "Interval";

   /** closure */
   static public final String CLOSURE = "closure";

   /** leftMargin */
   static public final String LEFT_MARGIN = "leftMargin";

   /** rightMargin */
   static public final String RIGHT_MARGIN = "rightMargin";

   /** closedOpen */
   static public final String CLOSED_OPEN = "closedOpen";

   /** closedClosed */
   static public final String CLOSED_CLOSED = "closedClosed";

   /** openClosed */
   static public final String OPEN_CLOSED = "openClosed";

   /** openOpen */
   static public final String OPEN_OPEN = "openOpen";

   /** BayesOutput */
   static public final String BAYES_OUTPUT = "BayesOutput";

   /** numberOfFields */
   static public final String NO_FIELDS = "numberOfFields";
} // end interface NaiveBayesPMMLTags
