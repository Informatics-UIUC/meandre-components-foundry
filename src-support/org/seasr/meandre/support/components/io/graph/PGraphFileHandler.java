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

package org.seasr.meandre.support.components.io.graph;

//==============
// Java Imports
//==============

import java.awt.*;
import java.util.*;

//===============
// Other Imports
//===============

import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.utils.*;

/**
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
import org.seasr.meandre.support.components.io.graph.*;

public class PGraphFileHandler extends GraphMLFileHandler {

   @SuppressWarnings("unchecked")
public Graph graph() {

      Graph graph = getGraph();
      Iterator<Vertex> vIter = graph.getVertices().iterator(), keyIter;
      Vertex v;
      Object key, datum;
      while (vIter.hasNext()) {

         v = (Vertex)vIter.next();
         keyIter = v.getUserDatumKeyIterator();
         while (keyIter.hasNext()) {

            key = keyIter.next();
            datum = v.getUserDatum(key);

            if (key.equals(PVertexNode.SHAPE)) {

               if (datum.equals("ellipse")) {
                  v.setUserDatum(key,
                                 new Integer(PVertexNode.ELLIPSE),
                                 UserData.SHARED);
               }
               else if (datum.equals("circle")) {
                  v.setUserDatum(key,
                                 new Integer(PVertexNode.CIRCLE),
                                 UserData.SHARED);
               }
               else if (datum.equals("rectangle")) {
                  v.setUserDatum(key,
                                 new Integer(PVertexNode.RECTANGLE),
                                 UserData.SHARED);
               }
               else if (datum.equals("square")) {
                  v.setUserDatum(key,
                                 new Integer(PVertexNode.SQUARE),
                                 UserData.SHARED);
               }

            }
            else if (key.equals(PVertexNode.SIZE)         ||
                     key.equals(PVertexNode.WIDTH)        ||
                     key.equals(PVertexNode.HEIGHT)       ||
                     key.equals(PVertexNode.BORDERWIDTH))    {

               v.setUserDatum(key,
                              new Double(Double.parseDouble((String)datum)),
                              UserData.SHARED);

            }
            else if (key.equals(PVertexNode.COLOR)       ||
                     key.equals(PVertexNode.BORDERCOLOR) ||
                     key.equals(PVertexNode.LABELCOLOR))    {

               v.setUserDatum(key,
                              Color.decode((String)datum),
                              UserData.SHARED);

            }
            else if (key.equals(PVertexNode.LABELMAX)   ||
                     key.equals(PVertexNode.LABELSIZE))    {

               v.setUserDatum(key,
                              new Integer(Integer.parseInt((String)datum)),
                              UserData.SHARED);

            }

         }

      }

      Iterator eIter = graph.getEdges().iterator();
      Edge e;
      while (eIter.hasNext()) {

         e = (Edge)eIter.next();
         keyIter = e.getUserDatumKeyIterator();
         while (keyIter.hasNext()) {

            key = keyIter.next();
            datum = e.getUserDatum(key);

            if (key.equals(PEdgeNode.WIDTH)) {

               e.setUserDatum(key,
                              new Double(Double.parseDouble((String)datum)),
                              UserData.SHARED);

            }
            else if (key.equals(PEdgeNode.COLOR)       ||
                     key.equals(PEdgeNode.LABELCOLOR))    {

               e.setUserDatum(key,
                              Color.decode((String)datum),
                              UserData.SHARED);

            }
            else if (key.equals(PEdgeNode.LABELMAX)   ||
                     key.equals(PEdgeNode.LABELSIZE))    {

               e.setUserDatum(key,
                              new Integer(Integer.parseInt((String)datum)),
                              UserData.SHARED);

            }

         }

      }

      return graph;

   }

}
