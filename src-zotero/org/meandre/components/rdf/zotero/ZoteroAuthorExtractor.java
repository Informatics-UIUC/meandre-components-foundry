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

package org.meandre.components.rdf.zotero;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.io.ModelUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 *  This class extracts the list of authors per entry from a Zotero RDF
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 */

@Component(
		creator = "Xavier Llor&agrave",
		description = "Extract the authors for each entry of a Zotero RDF",
		name = "Zotero Author Extractor",
		tags = "zotero, authors, information extraction",
		mode = Mode.compute,
		firingPolicy = FiringPolicy.all,
		baseURL = "meandre://seasr.org/components/zotero/"
)
public class ZoteroAuthorExtractor extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			description = "A map object containing the key elements of the request and the associated values",
			name = Names.PORT_REQUEST_DATA
	)
	protected static final String IN_REQUEST = Names.PORT_REQUEST_DATA;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			description = "A list of vectors containing the names of the authors. There is one vector for" +
					      "Zotero entry",
			name = Names.PORT_AUTHOR_LIST
	)
	protected static final String OUT_AUTHOR_LIST = Names.PORT_AUTHOR_LIST;


    //--------------------------------------------------------------------------------------------

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@SuppressWarnings("unchecked")
	public void executeCallBack(ComponentContext cc) throws Exception {
		Map<String,byte[]> map = (Map<String, byte[]>) cc.getDataComponentFromInput(IN_REQUEST);

		List<Vector<String>> list = new LinkedList<Vector<String>>();

		for ( String sKey:map.keySet() ) {
			Model model = ModelUtils.getModel(map.get(sKey), null);
			list.addAll(pullGraph(model));
		}

		cc.pushDataComponentToOutput(OUT_AUTHOR_LIST, list);
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

	private List<Vector<String>> pullGraph(Model model) {
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
}
