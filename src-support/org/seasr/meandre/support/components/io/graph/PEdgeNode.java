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
import java.awt.geom.*;

/**
 * TODO: testing
 * 
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public class PEdgeNode extends PNode {

	private static final long serialVersionUID = 1L;

	public static final String COLOR = PVertexNode.COLOR;
	public static final String LABEL = PVertexNode.LABEL;
	public static final String LABELCOLOR = PVertexNode.LABELCOLOR;
	public static final String WEIGHT = PVertexNode.WEIGHT;

	/**
	 * @deprecated
	 */
	public static final String LABELFONT = PVertexNode.LABELFONT;

	public static final String LABELFONTNAME = PVertexNode.LABELFONTNAME;
	public static final String LABELMAX = PVertexNode.LABELMAX;
	public static final String LABELSIZE = PVertexNode.LABELSIZE;
	public static final String LABELSTYLE = PVertexNode.LABELSTYLE;
	public static final String STYLE_BOLD = PVertexNode.STYLE_BOLD;
	public static final String STYLE_BOLDITALIC = PVertexNode.STYLE_BOLDITALIC;
	public static final String STYLE_ITALIC = PVertexNode.STYLE_ITALIC;
	public static final String STYLE_PLAIN = PVertexNode.STYLE_PLAIN;
	public static final String WIDTH = PVertexNode.WIDTH;

	protected Edge edge;
	protected PLabel text;

	protected Color baseColor, labelColor;
	protected Line2D line;
	protected Stroke stroke;

	protected static Stroke defaultStroke = new BasicStroke();

	public PEdgeNode(Edge e) {

		super();

		edge = e;
		line = new Line2D.Double();

		Object datum;

		datum = (String) edge.getUserDatum(LABEL);
		if (datum != null) {

			Integer maxd = (Integer) edge.getUserDatum(LABELMAX);
			if (maxd != null) {
				int maxv = maxd.intValue();
				if (((String) datum).length() > maxv) {
					datum = ((String) datum).substring(0, maxv - 1);
				}
			}

			text = new PLabel((String) datum, Color.BLACK);

			Font fontd = (Font) edge.removeUserDatum(LABELFONT);
			if (fontd != null) {

				// !: ...deprecated...
				Font f = (Font) datum;
				// text.setFont(f);
				edge.setUserDatum(LABELFONTNAME, f.getFontName(),
						UserData.SHARED);
				edge.setUserDatum(LABELSIZE, new Integer(f.getSize()),
						UserData.SHARED);

				if (f.isBold()) {
					if (f.isItalic()) {
						edge.setUserDatum(LABELSTYLE, STYLE_BOLDITALIC,
								UserData.SHARED);
					} else {
						edge.setUserDatum(LABELSTYLE, STYLE_BOLD,
								UserData.SHARED);
					}
				} else if (f.isItalic()) {
					edge
							.setUserDatum(LABELSTYLE, STYLE_ITALIC,
									UserData.SHARED);
				} else {
					edge.setUserDatum(LABELSTYLE, STYLE_PLAIN, UserData.SHARED);
				}

			}

			String nameDatum = (String) edge.getUserDatum(LABELFONTNAME);
			Integer sizeDatum = (Integer) edge.getUserDatum(LABELSIZE);
			String styleDatum = (String) edge.getUserDatum(LABELSTYLE);

			String fontname;
			if (nameDatum != null) {
				fontname = nameDatum;
			} else {
				fontname = "Default";
			}

			int fontsize;
			if (sizeDatum != null) {
				fontsize = sizeDatum.intValue();
			} else {
				fontsize = 12;
			}

			int fontstyle;
			if (styleDatum != null) {
				if (styleDatum.equals(STYLE_BOLD)) {
					fontstyle = Font.BOLD;
				} else if (styleDatum.equals(STYLE_ITALIC)) {
					fontstyle = Font.ITALIC;
				} else if (styleDatum.equals(STYLE_BOLDITALIC)) {
					fontstyle = Font.BOLD | Font.ITALIC;
				} else {
					fontstyle = Font.PLAIN;
				}
			} else {
				fontstyle = Font.PLAIN;
			}

			text.setFont(new Font(fontname, fontstyle, fontsize));

			addChild(text);

		}

		datum = (Color) edge.getUserDatum(COLOR);
		if (datum != null) {
			baseColor = (Color) datum;
		} else {
			baseColor = Color.DARK_GRAY;
		}

		datum = (Double) edge.getUserDatum(WIDTH);
		if (datum != null) {
			stroke = new BasicStroke(((Double) datum).floatValue());
		} else {
			stroke = defaultStroke;
		}

	}

	public boolean fullIntersects(java.awt.geom.Rectangle2D parentBounds) {
		return line.intersects(parentBounds);
	}

	public void paint(PPaintContext pContext) {

		Graphics2D g2 = pContext.getGraphics();

		g2.setPaint(baseColor);
		g2.setStroke(stroke);
		g2.draw(line);

	}

	public boolean setBounds(double x, double y, double w, double h) {

		if (super.setBounds(x, y, w, h)) {
			line.setLine(x, y, w, h);
			if (text != null) {
				text.setOffset(x + 0.5 * (w - x), y + 0.5 * (h - y));
			}
			return true;
		}

		return false;

	}

	public void setColor(Color color) {
		baseColor = color;
	}

	public void setLabelColor(Color color) {
		if (text == null) {
			text = new PLabel(color);
			addChild(text);
		} else {
			text.setColor(color);
		}
	}

	public void setLabelVisible(boolean visible) {
		if (text != null) {
			text.setVisible(visible);
		}
	}

	public void setText(String s) {
		if (text == null) {
			text = new PLabel(s, Color.BLACK);
			addChild(text);
		} else {
			text.setText(s);
		}
	}

	public void setWidth(double w) {
		stroke = new BasicStroke((float) w);
	}

}
