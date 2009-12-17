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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.components.utils.ComponentUtils;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.generic.io.IOUtils;
import org.seasr.meandre.support.utils.FileResourceUtility;

/**
 * Writes a text to a location
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 * @author Lily Dong
 */

@Component(
		name = "Write Text",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, io, read, text",
		description = "This component write text into a file. The component outputs the text. " +
				      "A property allows to control " +
				      "the behaviour of the component in front of an IO error, allowing to continue " +
				      "pushing and empty model or throwing and exception forcing the finalization of " +
				      "the flow execution.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class WriteText extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model to write" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_LOCATION = Names.PORT_LOCATION;

	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "The text to write" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the written XML" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The text written" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_TEXT= Names.PORT_TEXT;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		
		String[] inputs = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));

		if (inputs.length > 1)
		    throw new ComponentExecutionException("Cannot process multiple inputs per execute()");
		String sText = inputs[0];
		
		//
        // build the location to which to write:
		//
		String[] locations = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LOCATION));
		String sLocation = locations[0];
		
		// determine where the file should be written
		sLocation = FileResourceUtility.buildResourcePath(cc.getPublicResourcesDirectory(), sLocation);
		// NOTE, if the path contains directories that do NOT exist, the following will fail
		// TODO: build the directories if they do not exist see FileResourceUtility
		
		//
		// append the date .. TODO: this should be a property to this component
		// could be: append date, use date as a subdirectory, etc
		//
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		int index = sLocation.toString().lastIndexOf(".");
		if (index == -1) {
			// append the date
			sLocation += formatter.format(new Date());
		}
		else {
			// insert the date
			String prefix = sLocation.substring(0,index);
			String suffix = sLocation.substring(index);
			sLocation = prefix + formatter.format(new Date()) + suffix;
		}
		
		
		/* OLD CODE, removed M.E.H
		String sLocation = 
		URI objLoc = DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_LOCATION));
		Date now = new Date();
		
		int index = objLoc.toString().lastIndexOf(".");
		String sLocation;
		if (index == -1)
			 sLocation = objLoc.toString()+formatter.format(now);
		else
			 sLocation = objLoc.toString().substring(0, index)+formatter.format(now)+objLoc.toString().substring(index);
		Writer wrtr = IOUtils.getWriterForResource(new URI(cc.getPublicResourcesDirectory()+File.separator+sLocation)); 
	    
		wrtr.write(sText);
		wrtr.close();
		URL outputURL = new URL(cc.getProxyWebUIUrl(true), "/public/resources/" + sLocation);
		console.info("File written! Accessible at "+ outputURL);
			 
		*/
		
		BufferedWriter out = new BufferedWriter(new FileWriter(sLocation));
		out.write(sText);
		out.close();
		
		console.info("File written to " + sLocation);
		
		cc.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(sLocation));
		cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(sText));
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        pushDelimiters(
                componentContext.getDataComponentFromInput(IN_LOCATION),
                componentContext.getDataComponentFromInput(IN_TEXT));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        pushDelimiters(
                componentContext.getDataComponentFromInput(IN_LOCATION),
                componentContext.getDataComponentFromInput(IN_TEXT));
    }

    //--------------------------------------------------------------------------------------------

	/**
	 * Pushes the obtained delimiters
	 *
	 * @param objLoc The location delimiter
	 * @param objDoc The document delimiter
	 * @throws Exception Push failed
	 */
	private void pushDelimiters(Object objLoc, Object objDoc) throws Exception {
		if ( objLoc instanceof StreamDelimiter &&  objDoc instanceof StreamDelimiter)  {
			componentContext.pushDataComponentToOutput(OUT_LOCATION, objLoc);
			componentContext.pushDataComponentToOutput(OUT_TEXT, objDoc);
		}
		else
			pushMissalignedDelimiters(objLoc, objDoc);
	}

	/**
	 * Push the delimiters to the outputs as needed.
	 *
	 * @param objLoc The location delimiter
	 * @param objDoc The document delimiter
	 * @throws ComponentContextException Push failed
	 */
	private void pushMissalignedDelimiters(Object objLoc, Object objDoc) throws ComponentExecutionException {
	    console.warning("Missaligned delimiters received");

		if ( objLoc instanceof StreamDelimiter ) {
		    try {
                StreamDelimiter clone = ComponentUtils.cloneStreamDelimiter((StreamDelimiter) objLoc);
                componentContext.pushDataComponentToOutput(OUT_LOCATION, objLoc);
                componentContext.pushDataComponentToOutput(OUT_TEXT, clone);
            }
            catch (Exception e) {
                throw new ComponentExecutionException(e);
            }
		}
		else {
		    try {
                StreamDelimiter clone = ComponentUtils.cloneStreamDelimiter((StreamDelimiter) objDoc);
                componentContext.pushDataComponentToOutput(OUT_LOCATION, clone);
                componentContext.pushDataComponentToOutput(OUT_TEXT, objDoc);
            }
            catch (Exception e) {
                throw new ComponentExecutionException(e);
            }
		}
	}
}
