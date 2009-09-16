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

package org.seasr.meandre.support.components.prediction;

/**
 * Defines some constants for PMML tags
 *
 * @author  $author$
 * @version $Revision: 1.2 $, $Date: 2006/08/02 15:08:17 $
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 */
public interface PMMLTags {

   //~ Static fields/initializers **********************************************

   /** PMML */
   static public final String PMML = "PMML";

   /** DataDictionary */
   static public final String DATA_DICT = "DataDictionary";

   /** modelName */
   static public final String MODEL_NAME = "modelName";

   /** functionName */
   static public final String FUNCTION_NAME = "functionName";

   /** MiningSchema */
   static public final String MINING_SCHEMA = "MiningSchema";

   /** MiningField */
   static public final String MINING_FIELD = "MiningField";

   /** usageType */
   static public final String USAGE_TYPE = "usageType";

   /** name */
   static public final String NAME = "name";

   /** predicted */
   static public final String PREDICTED = "predicted";

   /** DataField */
   static public final String DATA_FIELD = "DataField";

   /** optype */
   static public final String OPTYPE = "optype";

   /** categorical */
   static public final String CATEGORICAL = "categorical";

   /** continuous */
   static public final String CONTINUOUS = "continuous";

   /** Value */
   static public final String VALUE_ELEMENT = "Value";

   /** value */
   static public final String VALUE = "value";

   /** active */
   static public final String ACTIVE = "active";
} // end interface PMMLTags
