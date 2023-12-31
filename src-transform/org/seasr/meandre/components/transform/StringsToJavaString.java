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

package org.seasr.meandre.components.transform;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;


/**
 * @author Boris Capitanu
 */
@Component(
        name = "Strings To Java String",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tools, convert, transform",
        description = "Converts a Google string into an equivalent Java string",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class StringsToJavaString extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The Google string to convert" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_JAVA_STRING,
            description = "The converted string" +
                "<br>TYPE: java.lang.String"
    )
    protected static final String OUT_JAVA_STRING = Names.PORT_JAVA_STRING;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_SEPARATOR,
            description = "If there are multiple strings packed in the Google Strings structure, how should they be combined? " +
                    "The separator is used to create a single Java String where the values in " +
                    "the Google structure are added, separated by the separator.",
            defaultValue = "\\n"
    )
    protected static final String PROP_SEPARATOR = Names.PROP_SEPARATOR;

    //--------------------------------------------------------------------------------------------


    protected String _separator;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        String separator = getPropertyOrDieTrying(PROP_SEPARATOR, false, true, ccp);
        _separator = separator.replaceAll("\\\\t", "\t").replaceAll("\\\\n", "\n").replaceAll("\\\\r", "\r");
        console.fine(String.format("Separator set to '%s' (surrounding single quotes added for readability)", _separator));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String[] sa = BasicDataTypesTools.stringsToStringArray((Strings)cc.getDataComponentFromInput(IN_TEXT));
        StringBuilder sb = new StringBuilder();
        for (String s : sa)
            sb.append(_separator).append(s);

        String text = (sb.length() > 0) ? sb.substring(_separator.length()) : "";
        console.fine(String.format("text (quotes added for readability): '%s'", text));

        cc.pushDataComponentToOutput(OUT_JAVA_STRING, text);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
