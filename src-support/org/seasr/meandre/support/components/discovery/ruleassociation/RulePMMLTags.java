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

package org.seasr.meandre.support.components.discovery.ruleassociation;

import org.seasr.meandre.support.components.prediction.*;

public interface RulePMMLTags extends PMMLTags {
    public static final String CONSEQUENT = "consequent";
    public static final String ANTECEDENT = "antecedent";
    public static final String SUPPORT = "support";
    public static final String CONFIDENCE = "confidence";
    public static final String ID = "id";
    public static final String VALUE = "value";
    public static final String ASSOC_RULE = "AssociationRule";
    public static final String ITEM_REF = "itemRef";
    public static final String ITEM = "Item";
    public static final String ITEMSET = "Itemset";
    public static final String ITEMREF = "ItemRef";
    public static final String ASSOC_MODEL = "AssociationModel";
    public static final String NUM_TRANS = "numberOfTransactions";
    public static final String MIN_SUP = "minimumSupport";
    public static final String MIN_CON = "minimumConfidence";
    public static final String NUM_ITEM = "numberOfItems";
    public static final String NUM_ITEMSETS = "numberOfItemsets";
    public static final String NUM_RULE = "numberOfRules";
}
