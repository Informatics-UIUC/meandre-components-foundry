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

package org.seasr.meandre.components.tools.basic;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.core.BasicDataTypes.Integers;
import org.seasr.datatypes.core.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.w3c.dom.Document;

/**
 * Prints an object to the console
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 * @author Lily Dong
 */

@Component(
		name = "Print To Console",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "#OUTPUT, print, console",
		description = "This component takes the input and prints it to the console. ",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class PrintToConsole extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_OBJECT,
			description = "The object to print" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_OBJECT = Names.PORT_OBJECT;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_OBJECT,
			description = "The object printed"
	)
	protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the stream markers be printed to the console along with the object? ",
            defaultValue = "false"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	//--------------------------------------------------------------------------------------------


	/** Should be wrapped */
	private boolean bWrapped;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		bWrapped = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		Object data = cc.getDataComponentFromInput(IN_OBJECT);
        PrintStream outputConsole = cc.getOutputConsole();

		if (data instanceof StringsMap) {
		    StringsMap sm = (StringsMap)data;
            for (int i = 0; i < sm.getValueCount(); i++) {
                String key = sm.getKey(i);
                String[] values = BasicDataTypesTools.stringsToStringArray(sm.getValue(i));

                StringBuffer buf = new StringBuffer();
                for (int j = 0; j < values.length; j++)
                	buf.append(values[j]).append("\n");

                outputConsole.println(String.format("key:%n%s%nvalues:%n%s%n", key, buf.toString()));
            }
		}

		else

		if (data instanceof IntegersMap) {
		    IntegersMap im = (IntegersMap)data;

		    int maxLength = 0;
			for (int i = 0; i < im.getValueCount(); i++) {
				String key = im.getKey(i);
				maxLength = (key.length() > maxLength) ? key.length() : maxLength;
		    }

			String pattern = "%-" + maxLength + "s %s%n";
		    outputConsole.println(String.format(pattern, "key", "value"));

		    pattern = "%-" + maxLength + "s %d";
		    for (int i = 0; i < im.getValueCount(); i++) {
		        String key = im.getKey(i);
		        Integers values = im.getValue(i);
                outputConsole.println(String.format(pattern, key, values.getValue(0)));
		    }
		}

		else

		if (data instanceof Document) {
	        Properties outputProperties = new Properties();
	        outputProperties.setProperty(OutputKeys.INDENT, "yes");
            outputProperties.setProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	        outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
		    DOMUtils.writeXML((Document) data, outputConsole, outputProperties);
		}

		else

		if (data instanceof Collection<?>) {
		    Collection<?> collection = (Collection<?>) data;
		    StringBuilder sb = new StringBuilder();
		    for (Object o : collection)
		        sb.append(", ").append(o.toString());
		    if (sb.length() > 0)
		        outputConsole.println("collection(" + data.getClass().getName() + "): [" + sb.substring(2) + "]");
		}

		else
            for (String s : DataTypeParser.parseAsString(data))
                outputConsole.println(s);

		cc.pushDataComponentToOutput(OUT_OBJECT, data);
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	    bWrapped = false;
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public void handleStreamInitiators() throws Exception {
	    super.handleStreamInitiators();

	    if (bWrapped)
	        printStreamDelimiter(componentContext.getDataComponentFromInput(IN_OBJECT));
	}

	@Override
	public void handleStreamTerminators() throws Exception {
	    super.handleStreamTerminators();

	    if (bWrapped)
	       printStreamDelimiter(componentContext.getDataComponentFromInput(IN_OBJECT));
	}

	/**
	 * Prints a stream delimiter
	 *
	 * @param cc The component context
	 * @param obj The delimiter to print
	 */
	private void printStreamDelimiter(Object obj) {
	    StreamDelimiter sd = (StreamDelimiter) obj;
		componentContext.getOutputConsole().println(String.format("%s (id: %d)", sd.getClass().getSimpleName(), sd.getStreamId()));
	}
}
