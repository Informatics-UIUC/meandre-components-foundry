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

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.utils.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import java.awt.*;

/**
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public abstract class PVertexNode extends PNode {

   public static final String BORDERCOLOR      = "bordercolor";
   public static final String BORDERWIDTH      = "borderwidth";
   public static final String COLOR            = "color";
   public static final String HEIGHT           = "height";
   public static final String LABEL            = "label";
   public static final String LABELCOLOR       = "labelcolor";
   public static final String WEIGHT           = "weight";

   /**
    * @deprecated
    */
   public static final String LABELFONT        = "labelfont";

   public static final String LABELFONTNAME    = "labelfontname";
   public static final String LABELMAX         = "labelmax";
   public static final String LABELSIZE        = "labelsize";
   public static final String LABELSTYLE       = "labelstyle";
   public static final String SIZE             = "size";
   public static final String SHAPE            = "shape";
   public static final String STYLE_BOLD       = "bold";
   public static final String STYLE_BOLDITALIC = "bolditalic";
   public static final String STYLE_ITALIC     = "italic";
   public static final String STYLE_PLAIN      = "plain";
   public static final String TYPE_CIRCLE      = "circle";
   public static final String TYPE_DEFAULT     = "default";
   public static final String TYPE_ELLIPSE     = "ellipse";
   public static final String TYPE_RECTANGLE   = "rectangle";
   public static final String TYPE_SQUARE      = "square";
   public static final String WIDTH            = "width";

   public static final int DEFAULT   = 100;
   public static final int ELLIPSE   = 101;
   public static final int CIRCLE    = 102;
   public static final int RECTANGLE = 103;
   public static final int SQUARE    = 104;

   protected Vertex vertex;
   protected PLabel text;

   protected Color baseColor, borderColor;
   protected Stroke borderStroke;

   protected static Stroke defaultStroke = new BasicStroke();

   public PVertexNode(Vertex v) {

      super();

      vertex = v;
      text = new PLabel(Color.BLACK);

      Object datum;

      datum = (String)vertex.getUserDatum(LABEL);
      if (datum != null) {

         Integer maxd = (Integer)vertex.getUserDatum(LABELMAX);
         if (maxd != null) {
            int maxv = maxd.intValue();
            if (((String)datum).length() > maxv) {
               datum = ((String)datum).substring(0, maxv - 1);
            }
         }

         text.setText((String)datum);

      }
      else {
         text.setText(vertex.toString());
      }

      datum = (Font)vertex.removeUserDatum(LABELFONT);
      if (datum != null) {

         // !: ...deprecated...
         Font f = (Font)datum;
         // text.setFont(f);
         vertex.setUserDatum(LABELFONTNAME, f.getFontName(), UserData.SHARED);
         vertex.setUserDatum(LABELSIZE, new Integer(f.getSize()), UserData.SHARED);

         if (f.isBold()) {
            if (f.isItalic()) {
               vertex.setUserDatum(LABELSTYLE, STYLE_BOLDITALIC, UserData.SHARED);
            }
            else {
               vertex.setUserDatum(LABELSTYLE, STYLE_BOLD, UserData.SHARED);
            }
         }
         else if (f.isItalic()) {
            vertex.setUserDatum(LABELSTYLE, STYLE_ITALIC, UserData.SHARED);
         }
         else {
            vertex.setUserDatum(LABELSTYLE, STYLE_PLAIN, UserData.SHARED);
         }

      }

      String nameDatum = (String)vertex.getUserDatum(LABELFONTNAME);
      Integer sizeDatum = (Integer)vertex.getUserDatum(LABELSIZE);
      String styleDatum = (String)vertex.getUserDatum(LABELSTYLE);

      String fontname;
      if (nameDatum != null) {
         fontname = nameDatum;
      }
      else {
         fontname = "Default";
      }

      int fontsize;
      if (sizeDatum != null) {
         fontsize = sizeDatum.intValue();
      }
      else {
         fontsize = 12;
      }

      int fontstyle;
      if (styleDatum != null) {
         if (styleDatum.equals(PVertexNode.STYLE_BOLD)) {
            fontstyle = Font.BOLD;
         }
         else if (styleDatum.equals(PVertexNode.STYLE_ITALIC)) {
            fontstyle = Font.ITALIC;
         }
         else if (styleDatum.equals(PVertexNode.STYLE_BOLDITALIC)) {
            fontstyle = Font.BOLD | Font.ITALIC;
         }
         else {
            fontstyle = Font.PLAIN;
         }
      }
      else {
         fontstyle = Font.PLAIN;
      }

      text.setFont(new Font(fontname, fontstyle, fontsize));

      datum = (Color)vertex.getUserDatum(COLOR);
      if (datum != null) {
         baseColor = (Color)datum;
      }
      else {
         baseColor = Color.DARK_GRAY;
      }

      datum = (Color)vertex.getUserDatum(BORDERCOLOR);
      if (datum != null) {
         borderColor = (Color)datum;
      }

      datum = (Double)vertex.getUserDatum(BORDERWIDTH);
      if (datum != null) {
         borderStroke = new BasicStroke(((Double)datum).floatValue());
      }
      else {
         borderStroke = defaultStroke;
      }

      datum = (Color)vertex.getUserDatum(LABELCOLOR);
      if (datum != null) {
         text.setColor((Color)datum);
      }

      addChild(text);

   }

   public static PVertexNode createNode(Vertex v, int type) {

      switch (type) {

         case ELLIPSE:
            return new PEllipseNode(v);
         case CIRCLE:
            return new PCircleNode(v);
         case RECTANGLE:
            return new PRectangleNode(v);
         case SQUARE:
            return new PSquareNode(v);
         default:
            return new PCircleNode(v);

      }

   }

   public abstract double getHeight();

   public abstract double getWidth();

   public abstract void paint(PPaintContext pContext);

   public void setBorderColor(Color color) {
      borderColor = color;
   }

   public boolean setBounds(double x, double y, double w, double h) {

      if(super.setBounds(x, y, w, h)) {
         text.setBounds(x, y, text.getWidth(), text.getHeight());
         return true;
      }

      return false;

   }

   public void setColor(Color color) {
      baseColor = color;
   }

   public void setLabelColor(Color color) {
      text.setColor(color);
   }

   public void setLabelVisible(boolean visible) {
      text.setVisible(visible);
   }

   public abstract void setSize(double d);

   public abstract void setSize(double w, double h);

   public void setText(String s) {
      text.setText(s);
   }

}
