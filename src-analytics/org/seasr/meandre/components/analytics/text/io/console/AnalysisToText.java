/**
 * 
 */
package org.seasr.meandre.components.analytics.text.io.console;

import java.io.PrintStream;

import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.datatypes.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.Names;


/** Basic analysis tools to text.
 * 
 * @author Xavier Llorˆ
 *
 */
public abstract class AnalysisToText 
implements ExecutableComponent {
	
	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_ERROR_HANDLING,
			description = "If set to true errors will be handled and they will be reported to the screen ." +
					      "Otherwise, the component will throw an exception an force the flow to abort. ",
		    defaultValue = "true" 
		)
	final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;

	@ComponentProperty(
			name = Names.PROP_HEADER,
			description = "Should the header be added? ",
		    defaultValue = "true" 
		)
	final static String PROP_HEADER = Names.PROP_HEADER;

	@ComponentProperty(
			name = Names.PROP_MESSAGE,
			description = "The header to use. ",
		    defaultValue = "The header message" 
		)
	final static String PROP_MESSAGE = Names.PROP_MESSAGE;

	@ComponentProperty(
			name = Names.PROP_OFFSET,
			description = "The offset of the first element to print ",
		    defaultValue = "0" 
		)
	final static String PROP_OFFSET = Names.PROP_OFFSET;
	
	@ComponentProperty(
			name = Names.PROP_COUNT,
			description = "The number of elements to print. A negative number " +
					      "forces to print all the available ",
		    defaultValue = "-1" 
		)
	final static String PROP_COUNT = Names.PROP_COUNT;

	//--------------------------------------------------------------------------------------------
		
	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "Text containing the human readable text of the analysis results"
		)
	final static String OUTPUT_TEXT = Names.PORT_TEXT;
	
	//--------------------------------------------------------------------------------------------
	
	/** The error handling flag */
	boolean bErrorHandling;
	
	/** The header message */
	String sHeader;
	
	/** Should the header be added */
	boolean bHeaderAdded;
	
	/** The offset of the first element */
	int iOffset;
	
	/** The number of elements to print */
	int iCount;
	
	//--------------------------------------------------------------------------------------------
	
	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
		this.bHeaderAdded = Boolean.parseBoolean(ccp.getProperty(PROP_HEADER));
		this.sHeader = ccp.getProperty(PROP_MESSAGE);
		this.iOffset = Integer.parseInt(ccp.getProperty(PROP_OFFSET));
		this.iCount = Integer.parseInt(ccp.getProperty(PROP_COUNT));
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.bErrorHandling = this.bHeaderAdded = false;
		this.sHeader = null;
	}
	
	
	//--------------------------------------------------------------------------------------------

	/** Print strings.
	 * 
	 * @param ps The print stream to use
	 * @param str The tokens to print
	 */
	void printStrings(PrintStream ps, Strings str) {
		if ( bHeaderAdded )
			ps.println(sHeader);
		for ( String s:str.getValueList() )
			ps.println(s);
		ps.println();		
	}


	/** Print an integer map.
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


	/** Print strings.
	 * 
	 * @param ps The print stream to use
	 * @param str The tokens to print
	 * @print count The number of entries to print
	 * @print offset The position of the first element
	 */
	void printStrings(PrintStream ps, Strings str, int count, int offset) {
		if ( bHeaderAdded )
			ps.println(sHeader);
		if ( offset<0 ) offset = 0;
		if ( count<0 ) count = str.getValueCount()-offset-1;
		for ( ; count>=0 ; offset++, count-- )
			ps.println(str.getValue(offset));
		ps.println();		
	}


	/** Print an integer map.
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
