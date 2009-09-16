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

package org.seasr.meandre.support.generic.text.analytics;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import edu.uci.ics.jung.algorithms.importance.AbstractRanker;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.UndirectedSparseVertex;
import edu.uci.ics.jung.utils.UserData;

/**
 * @author capitanu
 *
 */
public abstract class AnalyticsUtils {

    public static Graph buildGraph(List<Vector<String>> listAuthors, String userDatum) {
        UndirectedSparseGraph g = new UndirectedSparseGraph();
        Hashtable<String,UndirectedSparseVertex> htVertex = new Hashtable<String,UndirectedSparseVertex>();

        for ( Vector<String> vec:listAuthors ) {
            for ( String sAuthor:vec )
                if ( !htVertex.containsKey(sAuthor) ) {
                    UndirectedSparseVertex v = new UndirectedSparseVertex();
                    v.addUserDatum(userDatum, sAuthor, UserData.SHARED);
                    g.addVertex(v);
                    htVertex.put(sAuthor, v);
                }
        }

        for ( Vector<String> vec:listAuthors ) {
            Object[] oa = vec.toArray();
            for ( int i=0,iMax=oa.length ; i<iMax ; i++ )
                for ( int j=i+1,jMax=oa.length ; j<jMax ; j++ ) {
                    UndirectedSparseEdge e = new UndirectedSparseEdge(htVertex.get(oa[i].toString()), htVertex.get(oa[j].toString()));
                    try {
                        g.addEdge(e);
                    }
                    catch (Exception exception) {
                        // The edge was already added
                    }
                }
        }

        return g;
    }

    public static AbstractRanker computeBetweenness(Graph g) {
        BetweennessCentrality rBC = new BetweennessCentrality(g,true,false);
        rBC.evaluate();
        return rBC;
    }
}
