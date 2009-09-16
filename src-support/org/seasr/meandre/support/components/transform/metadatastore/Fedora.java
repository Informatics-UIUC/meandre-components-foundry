/**
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
 */

package org.seasr.meandre.support.components.transform.metadatastore;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.ProfileRegistry;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Jena vocabulary for Fedora collection
 *
 * @author Boris Capitanu
 * @jira COMPONENTS-10 Create XmlToTriples component
 */
public class Fedora {
    /**
     * Fedora header section
     */
    public class Header {
        /**
         * <p>The ontology model that holds the vocabulary terms</p>
         */
        private OntModel m_model = ModelFactory.createOntologyModel(ProfileRegistry.OWL_LANG);

        /**
         * <p>The namespace of the vocabalary as a string </p>
         */
        public static final String NS = "http://fedora.org/docHeaders/set/";

        /**
         * <p>The namespace of the vocabalary as a resource </p>
         */
        public final Resource NAMESPACE = m_model.createResource(NS);

        /**
         * <p>The fedora docHeaders document resource</p>
         */
        public final Property item = m_model.createProperty(NS + "document");

        /**
         * <p>The fedora docHeader root </p>
         */
        public final Resource root = m_model.createResource(NS + "root");
    }

    /**
     * Fedora body section
     */
    public class Document {
        /**
         * <p>The ontology model that holds the vocabulary terms</p>
         */
        private OntModel m_model = ModelFactory.createOntologyModel(ProfileRegistry.OWL_LANG);

        /**
         * <p>The namespace of the vocabalary as a string </p>
         */
        public static final String NS = "http://fedora.org/document/set/";

        /**
         * <p>The namespace of the vocabalary as a resource </p>
         */
        public final Resource NAMESPACE = m_model.createResource(NS);

        /**
         * <p>The fedora document items resource</p>
         */
        public final Property item = m_model.createProperty(NS + "item");

        /**
         * <p>The fedora document root </p>
         */
        public final Resource root = m_model.createResource(NS + "root");
    }
}