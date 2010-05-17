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
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;

/**
 * Basic analysis tools to text.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
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
			name = Names.PROP_HEADER,
			description = "Should the header be added? ",
		    defaultValue = "true"
	)
	protected static final String PROP_HEADER = Names.PROP_HEADER;

	@ComponentProperty(
			name = Names.PROP_MESSAGE,
			description = "The header to use. ",
		    defaultValue = "The header message"
	)
	protected static final String PROP_MESSAGE = Names.PROP_MESSAGE;

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


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.bHeaderAdded = Boolean.parseBoolean(ccp.getProperty(PROP_HEADER));
		this.sHeader = ccp.getProperty(PROP_MESSAGE);
		this.iOffset = Integer.parseInt(ccp.getProperty(PROP_OFFSET));
		this.iCount = Integer.parseInt(ccp.getProperty(PROP_COUNT));
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
			ps.print(sToken+":");
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
			ps.print(sToken+":");
			for ( int iCounts:im.getValue(offset).getValueList() )
				ps.print(" "+iCounts);
			ps.println();
		}
		ps.println();
	}
}
