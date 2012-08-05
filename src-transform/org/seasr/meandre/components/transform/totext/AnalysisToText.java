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

package org.seasr.meandre.components.transform.totext;

import java.io.PrintStream;

import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.core.BasicDataTypes.DoublesMap;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * Basic analysis tools to text.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 * @author Lily Dong
 * @author Ian Wood
 *
 */
public abstract class AnalysisToText extends AbstractExecutableComponent {

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "Text containing the human readable text of the analysis results" +
			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

	//------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_SEPARATOR,
			description = "Used to separate field values",
			defaultValue = ","
	)
	protected static final String PROP_TEXT_SEPARATOR = Names.PROP_SEPARATOR;

	@ComponentProperty(
			name = "header",
			description = "The comma-separated list of attribute names. The commas will be replaced " +
			"by the separator specified in the " + PROP_TEXT_SEPARATOR + " property. If this property is empty, " +
			"no header will be used.",
			defaultValue = ""
	)
	protected static final String PROP_HEADER = "header";

	@ComponentProperty(
			name = Names.PROP_OFFSET,
			description = "The offset of the first element to print ",
			defaultValue = "0"
	)
	protected static final String PROP_OFFSET = Names.PROP_OFFSET;

	@ComponentProperty(
			name = Names.PROP_COUNT,
			description = "The number of elements to print. A negative number " +
			"forces to print all the available ",
			defaultValue = "-1"
	)
	protected static final String PROP_COUNT = Names.PROP_COUNT;

	//--------------------------------------------------------------------------------------------


	/** The header message */
	String sHeader;

	/** Should the header be added */
	boolean bHeaderAdded;

	/** The offset of the first element */
	int iOffset;

	/** The number of elements to print */
	int iCount;

	/** The printing separator */
	String textSep;


	//--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.sHeader = getPropertyOrDieTrying(PROP_HEADER, true, false, ccp);
		this.bHeaderAdded = this.sHeader.length() > 0;

		this.iOffset = Integer.parseInt(getPropertyOrDieTrying(PROP_OFFSET, ccp));
		this.iCount = Integer.parseInt(getPropertyOrDieTrying(PROP_COUNT, ccp));

		this.textSep = getPropertyOrDieTrying(PROP_TEXT_SEPARATOR, false, true, ccp).replaceAll("\\\\t", "\t");
		this.sHeader = this.sHeader.replaceAll(",", this.textSep);
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		this.bHeaderAdded = false;
		this.sHeader = null;
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Print strings.
	 *
	 * @param ps The print stream to use
	 * @param str The tokens to print
	 */
	void printStrings(PrintStream ps, String[] str) {
		if ( bHeaderAdded )
			ps.println(sHeader);
		for ( String s : str )
			ps.println(s);
		ps.println();
	}


	/**
	 * Print an integer map.
	 *
	 * @param ps The print stream to use
	 * @param im The tokens to print
	 */
	void printIntegerMap(PrintStream ps, IntegersMap im) {
		// Get the container of the counts
		if ( bHeaderAdded )
			ps.println(sHeader);
		for ( int i=0, iMax=im.getKeyCount() ; i<iMax ; i++ ) {
			String sToken = im.getKey(i);
			ps.print(sToken+textSep);
			for ( int iCounts:im.getValue(i).getValueList() )
				ps.println(" "+iCounts);
		}
		ps.println();
	}


	/**
	 * Print strings.
	 *
	 * @param ps The print stream to use
	 * @param str The tokens to print
	 * @print count The number of entries to print
	 * @print offset The position of the first element
	 */
	void printStrings(PrintStream ps, String[] str, int count, int offset) {
		if ( bHeaderAdded )
			ps.println(sHeader);
		if ( offset<0 ) offset = 0;
		if ( count<0 ) count = str.length-offset-1;
		for ( ; count>=0 ; offset++, count-- )
			ps.println(str[offset]);
		ps.println();
	}


	/**
	 * Print an integer map.
	 *
	 * @param ps The print stream to use
	 * @param im The tokens to print
	 * @print count The number of entries to print
	 * @print offset The position of the first element
	 */
	void printIntegerMap(PrintStream ps, IntegersMap im, int count, int offset) {
		// Get the container of the counts
		if ( bHeaderAdded )
			ps.println(sHeader);
		if ( offset<0 ) offset = 0;
		if ( count<0 ) count = im.getKeyCount()-offset;
		for ( count-- ; count>=0 ; offset++, count-- ) {
			String sToken = im.getKey(offset);
			ps.print(sToken+textSep);
			for ( int iCounts:im.getValue(offset).getValueList() )
				ps.print(iCounts);
			ps.println();
		}
		ps.println();
	}

	/**
	 * Print an doubles map.
	 *
	 * @param ps The print stream to use
	 * @param dm The tokens to print
	 * @print count The number of entries to print
	 * @print offset The position of the first element
	 */
	void printDoublesMap(PrintStream ps, DoublesMap dm, int count, int offset) {
		// Get the container of the counts
		if ( bHeaderAdded )
			ps.println(sHeader);
		if ( offset<0 ) offset = 0;
		if ( count<0 ) count = dm.getKeyCount()-offset;
		for ( count-- ; count>=0 ; offset++, count-- ) {
			String sToken = dm.getKey(offset);
			ps.print(sToken+textSep);
			for ( Double dValues:dm.getValue(offset).getValueList() )
				ps.print(dValues+textSep);
			ps.println();
		}
		ps.println();
	}
}