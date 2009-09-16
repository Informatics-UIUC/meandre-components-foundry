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

package org.seasr.meandre.support.generic.zotero;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Boris Capitanu
 *
 */
public abstract class ZoteroUtils {

    private static final String HTTP_WWW_GUTENBERG_ORG_FILES = "http://www.gutenberg.org/files/";
    private static final String HTTP_WWW_GUTENBERG_ORG_ETEXT = "http://www.gutenberg.org/etext/";


    /**
     * Extracts the authors from a Zotero RDF model
     *
     * @param model The Zotero model
     * @return The list of sets of authors
     */
    public static List<Vector<String>> extractAuthors(Model model) {
        final String QUERY_AUTHORS =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"+
            "SELECT DISTINCT ?doc ?first ?last " +
            "WHERE { " +
            "      ?doc rdf:type rdf:Seq . " +
            "      ?doc ?pred ?author ." +
            "      ?author rdf:type foaf:Person ." +
            "      ?author foaf:givenname ?first . " +
            "      ?author foaf:surname ?last " +
            "} " +
            "ORDER BY ?doc" ;

       // Query the basic properties
       //QuerySolutionMap qsmBindings = new QuerySolutionMap();
       //qsmBindings.add("component", res);

       Query query = QueryFactory.create(QUERY_AUTHORS) ;
       QueryExecution exec = QueryExecutionFactory.create(query, model, null);//qsmBindings);
       ResultSet results = exec.execSelect();

       String sLastDocID = "";
       List<Vector<String>> vecRes = new LinkedList<Vector<String>>();
       Vector<String> vec = null;
       while ( results.hasNext() ) {
           QuerySolution resProps = results.nextSolution();
           String sDoc   = resProps.getResource("doc").toString();
           String sFirst = resProps.getLiteral("first").getString();
           String sLast  = resProps.getLiteral("last").getString();
           if ( sDoc.equals(sLastDocID)) {
               vec.add(sLast+", "+sFirst);
           }
           else {
               if ( vec!=null )
                   vecRes.add(vec);
               vec = new Vector<String>();
               vec.add(sLast+", "+sFirst);
               sLastDocID = sDoc;
           }
       }
       if ( vec==null ) vec = new Vector<String>();
       if ( vec.size()>0 ) vecRes.add(vec);

       return vecRes;
    }

    /**
     * Extracts the URLs from a Zotero RDF model
     *
     * @param model The Zotero model
     * @return The URLs
     */
    public static Map<String, String> extractURLs(Model model) {
        // Query to extract the item type, uri and title from the zotero rdf
        final String QUERY_TYPE_URI_TITLE =
              "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX bib: <http://purl.org/net/biblio#>\n"
            + "PREFIX dcterms:  <http://purl.org/dc/terms/>\n"
            + "PREFIX z:       <http://www.zotero.org/namespaces/export#> \n"
            + "SELECT ?type ?uri ?title ?a ?n \n"
            + "WHERE { "
            + "      ?n rdf:value ?uri . "
            + "      ?n rdf:type dcterms:URI . "
            + "      ?a z:itemType ?type . "
            + "      ?a dc:title ?title . "
            + "      ?a dc:identifier ?n . "
            + "} order by ?type ?uri ?title ?a ?n ";

        Query query = QueryFactory.create(QUERY_TYPE_URI_TITLE) ;
        QueryExecution exec = QueryExecutionFactory.create(query, model, null);//qsmBindings);
        ResultSet results = exec.execSelect();

        Map<String, String> mapURLs = new HashMap<String, String>();

        while ( results.hasNext() ) {
            QuerySolution resProps = results.nextSolution();
            String typeValue = resProps.getLiteral("type").toString();

            if (typeValue.equalsIgnoreCase("attachment")){
//              System.out.println("skipping ... { type= attachment } { uri= " +
//                      resProps.getLiteral("uri").toString() + " } { title= " +
//                      resProps.getLiteral("title").toString() + " }"
//              );
                continue;
            }

            String sURI = resProps.getLiteral("uri").toString();
            String sTitle = resProps.getLiteral("title").toString();
            sURI = adjustSpecialCaseURL(sURI);
            mapURLs.put(sURI, sTitle);
        }

        return mapURLs;
    }

    /**
     * Makes adjustments to the URL for special cases where adjustments are needed
     *
     * @param sUrl The URL
     * @return The adjusted URL
     */
    private static String adjustSpecialCaseURL(String sUrl) {
        if ( sUrl.startsWith(HTTP_WWW_GUTENBERG_ORG_ETEXT) ) {
            // URL adjustment for Gutenberg items
            String sTmp = sUrl.substring(HTTP_WWW_GUTENBERG_ORG_ETEXT.length());
            sUrl = HTTP_WWW_GUTENBERG_ORG_FILES+sTmp+"/"+sTmp+".txt";
        }
        return sUrl;
    }
}
