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

package org.seasr.meandre.components.tools.xml.transform;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 */

@Component(
        creator = "Lily Dong",
        description = "Transforms the XML input for Simile Timeline.",
        name = "Date-entity To Simile",
        tags = "date entity, simile, xml, convert",
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/tools/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class DateEntityToSimile extends AbstractExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        description = "The source XML document",
	        name = Names.PORT_XML
	)
    protected static final String IN_XML = Names.PORT_XML;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        description = "The output XML document",
	        name = Names.PORT_XML
	)
	protected static final String OUT_XML = Names.PORT_XML;

	//--------------------------------------------------------------------------------------------


	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		Document doc_in = DataTypeParser.parseAsDomDocument(cc.getDataComponentFromInput(IN_XML));

    	int minYear = Integer.MAX_VALUE;
    	int maxYear = Integer.MIN_VALUE;

    	Document doc_out = DOMUtils.createNewDocument();

    	Element rootElement = doc_out.createElement("data");
    	doc_out.appendChild(rootElement);

		doc_in.getDocumentElement().normalize();

		NodeList dateNodes = doc_in.getElementsByTagName("date");

		for (int i = 0, iMax = dateNodes.getLength(); i < iMax; i++) {
			Element elEntity = (Element)dateNodes.item(i);
			String aDate = elEntity.getAttribute("value");

			console.finest("aDate: '" + aDate + "'");

			//standardize date

			String month = null,
				   day   = null,
				   year  = null;

			String startMonth = null,
					 endMonth = null;

			Pattern datePattern = Pattern.compile("(january|jan|february|feb|march|mar|" + //look for month
					"april|apr|may|june|jun|july|jul|august|aug|september|sept|october|oct|"+
					"november|nov|december|dec)");
			Matcher dateMatcher = datePattern.matcher(aDate);

			if(dateMatcher.find()) { //look for month
				month = dateMatcher.group(1);
			} else { //look for season
				datePattern = Pattern.compile("(spring|summer|fall|winter)");
				dateMatcher = datePattern.matcher(aDate);

				if(dateMatcher.find()) {
					String season = dateMatcher.group(1);
					if(season.equalsIgnoreCase("spring")) {
						startMonth = "Mar 21";
						endMonth = "June 20";
					} else if(season.equalsIgnoreCase("summer")) {
						startMonth = "June 21";
						endMonth = "Sept 20";
					} else if(season.equalsIgnoreCase("fall")) {
						startMonth = "Sept 21";
						endMonth = "Dec 20";
					} else { //winter
						startMonth = "Dec 21";
						endMonth = "Mar 20";
					}
				}
			}

			datePattern = Pattern.compile("(\\b\\d{1,2}((st)?|(nd)?|(rd)?|(th)?)\\b)");
			dateMatcher = datePattern.matcher(aDate); //look for 1 or 1st or 22 or 22nd or 3rd or 4th
			if(dateMatcher.find()) {
				datePattern = Pattern.compile("(\\d{1,2})"); //look for 1 or 22 only
				dateMatcher = datePattern.matcher(dateMatcher.group(1));
				if(dateMatcher.find());
					day = dateMatcher.group(1);
			}

			datePattern = Pattern.compile("(\\d{3,4})"); //look for year with 3 or 4 digits
			dateMatcher = datePattern.matcher(aDate);
			if(dateMatcher.find()) { //look for year
	            StringBuffer sbHtml = new StringBuffer();
	            int nr = 0;

			    NodeList sentenceNodes = elEntity.getElementsByTagName("sentence");
			    for (int idx = 0, idxMax = sentenceNodes.getLength(); idx < idxMax; idx++) {
			        Element elSentence = (Element)sentenceNodes.item(idx);
			        String docTitle = elSentence.getAttribute("docTitle");
			        String theSentence = elSentence.getTextContent();

			        String normalizedSentence = theSentence.replaceAll("\r|\n", " ").toLowerCase();
			        String normalizedDate = aDate.replaceAll("\r|\n", " ").toLowerCase();

			        int datePos = normalizedSentence.indexOf(normalizedDate);
			        if (datePos < 0) {
			            console.warning("Could not find the position of the date in the sentence! This should not happen!");
			            console.warning("   sentence: '" + normalizedSentence + "'");
			            console.warning("   date: '" + normalizedDate + "'");
			        }

			        String sentBefore = theSentence.substring(0, datePos);
			        String sentAfter = theSentence.substring(datePos + aDate.length());
			        theSentence = StringEscapeUtils.escapeHtml(sentBefore) +
			                      "<font color='red'>" + StringEscapeUtils.escapeHtml(aDate) + "</font>" +
			                      StringEscapeUtils.escapeHtml(sentAfter);
                    sbHtml.append("<div onclick='toggleVisibility(this)' style='position:relative' align='left'><b>Sentence ").append(++nr);
                    if (docTitle != null && docTitle.length() > 0)
                        sbHtml.append(" from '" + StringEscapeUtils.escapeHtml(docTitle) + "'");
                    sbHtml.append("</b><span style='display: ' align='left'><table><tr><td>").append(theSentence).append("</td></tr></table></span></div>");
			    }

			    String sentence = sbHtml.toString();

				year = dateMatcher.group(1);
				minYear = Math.min(minYear, Integer.parseInt(year));
				maxYear = Math.max(maxYear, Integer.parseInt(year));

				//year or month year or month day year
				if(day == null)
					if(month == null) { //season year
						if(startMonth != null) {//spring or summer or fall or winter year
							Element em = doc_out.createElement("event");
							em.setAttribute("start", startMonth + " " + year);
							em.setAttribute("end", endMonth + " " + year);
							em.setAttribute("title", aDate+"("+nr+")");
							em.appendChild(doc_out.createTextNode(sentence));
							rootElement.appendChild(em);
						} else { //year
							Element em = doc_out.createElement("event");
							em.setAttribute("start", year);
							em.setAttribute("title", aDate+"("+nr+")");
							em.appendChild(doc_out.createTextNode(sentence));
							rootElement.appendChild(em);
						}
					} else { //month year
						String startDay = month + " 01";
						int m = 1;
						if(month.startsWith("feb"))
							m = 2;
						else if(month.startsWith("mar"))
							m = 3;
						else if(month.startsWith("apr"))
							m = 4;
						else if(month.startsWith("may"))
							m = 5;
						else if(month.startsWith("jun"))
							m = 6;
						else if(month.startsWith("jul"))
							m = 7;
						else if(month.startsWith("aug"))
							m = 8;
						else if(month.startsWith("sept"))
							m = 9;
						else if(month.startsWith("oct"))
							m = 10;
						else if(month.startsWith("nov"))
							m = 11;
						else if(month.startsWith("dec"))
							m = 12;
						int y = Integer.parseInt(year);
						int numberOfDays = 31;
						if (m == 4 || m == 6 || m == 9 || m == 11)
							  numberOfDays = 30;
						else if (m == 2) {
							boolean isLeapYear = (y % 4 == 0 && y % 100 != 0 || (y % 400 == 0));
							if (isLeapYear)
								numberOfDays = 29;
							else
								numberOfDays = 28;
						}
						String endDay = month + " " + Integer.toString(numberOfDays);
						Element em = doc_out.createElement("event");
						em.setAttribute("start", startDay + " " + year);
						em.setAttribute("end", endDay + " " + year);
						em.setAttribute("title", aDate+"("+nr+")");
						em.appendChild(doc_out.createTextNode(sentence));
						rootElement.appendChild(em);
					}
				else {
					if(month == null) {//year
						Element em = doc_out.createElement("event");
						em.setAttribute("start", year);
						em.setAttribute("title", aDate+"("+nr+")");
						em.appendChild(doc_out.createTextNode(sentence));
						rootElement.appendChild(em);
					} else { //month day year
						Element em = doc_out.createElement("event");
						em.setAttribute("start", month + " " + day + " " + year);
						em.setAttribute("title", aDate+"("+nr+")");
						em.appendChild(doc_out.createTextNode(sentence));
						rootElement.appendChild(em);
					}
				}
			}
		}

		StringWriter writer = new StringWriter();
		DOMUtils.writeXML(doc_out, writer, null);

	    cc.pushDataComponentToOutput(OUT_XML,
	            BasicDataTypesTools.stringToStrings(writer.toString()));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
