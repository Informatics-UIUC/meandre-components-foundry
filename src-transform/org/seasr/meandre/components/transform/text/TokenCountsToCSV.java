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

package org.seasr.meandre.components.transform.text;

import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Token Counts To CSV",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, token count, text, convert, csv",
        description = "This component converts token counts to CSV." ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TokenCountsToCSV extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The token counts" +
                "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
    )
    protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The CSV representation of the token counts" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //----------------------------- PROPERTIES ------------------------------

    @ComponentProperty(
            name = Names.PROP_SEPARATOR,
            description = "The delimiter to use to separate the data columns",
            defaultValue = ","
    )
    protected static final String PROP_SEPARATOR = Names.PROP_SEPARATOR;

    @ComponentProperty(
            name = Names.PROP_HEADER,
            description = "The header to use (leave empty if you don't want a header). <br>" +
                    "Use comma to separate the header names. Example: tokens,counts <br>" +
                    "The comma will be replaced with the actual separator defined in the " + PROP_SEPARATOR + " property",
            defaultValue = "tokens,counts"
    )
    protected static final String PROP_HEADER = Names.PROP_HEADER;

    @ComponentProperty(
            name = "delimiter_replacement",
            description = "If the token contains a delimiter (separator), what should the delimiter be replaced with? <br>" +
                    "If not replaced or escaped the delimiter will cause the CSV to be malformed.",
            defaultValue = "\\\\,"
    )
    protected static final String PROP_DELIM_REPLACE = "delimiter_replacement";


    //--------------------------------------------------------------------------------------------


    private String _separator;
    private String _header;
    private String _delimReplacement;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _separator = getPropertyOrDieTrying(PROP_SEPARATOR, false, true, ccp).replaceAll("\\\\t", "\t");
        _header = getPropertyOrDieTrying(PROP_HEADER, true, false, ccp).replaceAll(",", _separator);
        _delimReplacement = getPropertyOrDieTrying(PROP_DELIM_REPLACE, false, true, ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object input = cc.getDataComponentFromInput(IN_TOKEN_COUNTS);
        Map<String, Integer> tokenCountsMap = DataTypeParser.parseAsStringIntegerMap(input);

        StringBuilder sb = new StringBuilder();
        sb.append(_header).append("\n");

        for (Map.Entry<String, Integer> entry : tokenCountsMap.entrySet()) {
            String token = entry.getKey();
            Integer count = entry.getValue();

            if (token.contains(_separator))
                token = token.replaceAll(_separator, _delimReplacement);

            sb.append(token).append(_separator).append(count).append("\n");
        }

        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(sb.toString()));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

}
