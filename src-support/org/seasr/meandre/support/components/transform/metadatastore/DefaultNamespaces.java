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

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Supporting class for the XmlToTriples component
 *
 * @author Boris Capitanu
 * @jira COMPONENTS-10 Create XmlToTriples component
 */
public class DefaultNamespaces {
    public final static String XSD = "http://www.w3.org/2001/XMLSchema#";
    public final static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public final static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    public final static String OWL = "http://www.w3.org/2002/07/owl#";
    public final static String DC = "http://purl.org/dc/elements/1.1/";
    public final static String TEI = "http://www.tei-c.org/ns/1.0";

    public static void setDefaultModelNsPrefix(Model model, String fedoraNS) {
        model.setNsPrefix("xsd", XSD);
        model.setNsPrefix("rdf", RDF);
        model.setNsPrefix("rdfs", RDFS);
        model.setNsPrefix("owl", OWL);
        model.setNsPrefix("dc", DC);
        model.setNsPrefix("fedora", fedoraNS);
    }
}