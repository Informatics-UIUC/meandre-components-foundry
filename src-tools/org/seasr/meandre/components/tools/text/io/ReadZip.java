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

package org.seasr.meandre.components.tools.text.io;

import java.io.BufferedInputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.utils.ComponentUtils;

/**
 *
 * @author Loretta Auvil
 *
 */

@Component(
		name = "Read Zip",
		creator = "Loretta Auvil",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "io, read, zip",
		description = "This component reads a zip file and passes each file as output.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class ReadZip extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model to read" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_LOCATION = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model read" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
			name = Names.PORT_RAW_DATA,
			description = "The raw data of the objects containing in the zip file." +
                "<br>TYPE: byte[]"
	)
	private final static String OUT_BYTES = Names.PORT_RAW_DATA;

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		try {

		}
		catch (Throwable t) {

		}
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {

		URI location = DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_LOCATION));
		console.fine("Parsing location: " + location);

		try {

		ZipFile zipfile = new ZipFile("location");
		Enumeration e = zipfile.entries();
		ZipEntry entry;
		BufferedInputStream is;
		byte bytes[] = null;

		while(e.hasMoreElements()) {
		    entry = (ZipEntry) e.nextElement();

		    console.info("Extracting: " +entry+":"+entry.getName());
		    is = new BufferedInputStream(zipfile.getInputStream(entry));
		    int count = (int)entry.getSize();
		    is.read(bytes,0,count);
            is.close();

	        cc.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(entry.getName()));
	        cc.pushDataComponentToOutput(OUT_BYTES, bytes);
	        bytes = null;
		 }



		}
		catch (Throwable t) {
			console.warning("Could not read Zipfile from location " + location.toString());

			if ( !ignoreErrors )
				throw new ComponentExecutionException(t);
		}
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {

    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        pushDelimiters((StreamDelimiter)componentContext.getDataComponentFromInput(IN_LOCATION));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        pushDelimiters((StreamDelimiter)componentContext.getDataComponentFromInput(IN_LOCATION));
    }

    //--------------------------------------------------------------------------------------------

	/**
	 * Push the delimiters
	 *
	 * @param sdLoc The delimiter object
	 * @throws Exception
	 */
	private void pushDelimiters(StreamDelimiter sdLoc) throws Exception {
	    componentContext.pushDataComponentToOutput(OUT_LOCATION, sdLoc);

		try {
		    componentContext.pushDataComponentToOutput(OUT_BYTES, ComponentUtils.cloneStreamDelimiter(sdLoc));
		} catch (Exception e) {
			console.warning("Failed to create a new delimiter - reusing existing one");
			componentContext.pushDataComponentToOutput(OUT_BYTES, sdLoc);
		}
	}
}
