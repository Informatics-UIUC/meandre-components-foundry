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

package org.seasr.meandre.components.tools.io;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.generic.io.StreamUtils;

/**
 *
 * @author Loretta Auvil
 * @author Boris Capitanu
 *
 */

@Component(
		name = "Read Zip",
		creator = "Loretta Auvil",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "#INPUT, io, read, zip",
		description = "This component reads a zip file and passes each file as output.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class ReadZip extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name to read" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_LOCATION = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "A URL reference for each entry in the archive" +
                "<br>TYPE: java.net.URL"
	)
	protected static final String OUT_LOCATION = Names.PORT_LOCATION;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Wrap output as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	//--------------------------------------------------------------------------------------------


    private boolean wrapStream;


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

	    wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
	    URL location = StreamUtils.getURLforResource(
	            DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_LOCATION)));

		console.fine("Reading ZIP file from: " + location);
		JarInputStream zipStream = new JarInputStream(new BufferedInputStream(location.openStream()));

		try {
		    if (wrapStream) cc.pushDataComponentToOutput(OUT_LOCATION, new StreamInitiator(streamId));

		    ZipEntry entry;
		    while ((entry = zipStream.getNextEntry()) != null) {
		        URL entryUrl = new URL("jar:" + location.toString() + "!/" + entry);
		        console.finer("Pushing " + entryUrl);
		        cc.pushDataComponentToOutput(OUT_LOCATION, entryUrl);
		    }

		    if (wrapStream) cc.pushDataComponentToOutput(OUT_LOCATION, new StreamTerminator(streamId));
		}
		finally {
		    zipStream.close();
		}
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }
}
