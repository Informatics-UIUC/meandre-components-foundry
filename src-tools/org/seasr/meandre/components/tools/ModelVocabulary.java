/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.seasr.meandre.components.tools;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This class just provide a convenience place for JSTOR RDF vocabulary
 *
 * @author Xavier Llor&agrave;
 *
 */
public class ModelVocabulary {

	 /** The RDF model that holds the vocabulary terms */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** The namespace of the vocabulary as a string  */
    public static final String NS = "meandre://meandre.org/components/vocabulary#";

    /** The namespace of the vocabulary as a string */
    public static String getURI() {
          return NS;
    }

    /** The namespace of the vocabulary as a resource */
    public static final Resource NAMESPACE = m_model.createResource(NS);

    /** Text property used for having the text. */
    public static final Property text = m_model.createProperty(NS+"text");

    /** Text property used for creating keys. */
    public static final Property key = m_model.createProperty(NS+"key");

    /** Text property used for creating values associated to a key. */
    public static final Property value = m_model.createProperty(NS+"value");

    /** Used to annotate sequences of tokens. */
    public static final Resource tokens = m_model.createResource(NS+"tokens");

    /** Used to annotate sequences of token counts. */
    public static final Resource token_counts = m_model.createResource(NS+"token_counts");

    /** Used to annotate sequences of sentences. */
    public static final Resource sentences = m_model.createResource(NS+"sentences");

}
