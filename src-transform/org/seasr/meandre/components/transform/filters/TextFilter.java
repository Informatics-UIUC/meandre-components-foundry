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

package org.seasr.meandre.components.transform.filters;

import java.util.regex.Pattern;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Text Filter",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tools, text, filter",
        description = "This component filters (in or out) the incoming text based on a regular expression" ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TextFilter extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The text to filter" +
                          "<br>TYPE: java.lang.String" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                          "<br>TYPE: byte[]" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                          "<br>TYPE: java.lang.Object",
            name = Names.PORT_TEXT
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The filtered text" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings",
            name = Names.PORT_TEXT
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_FILTER_REGEX,
            description = "The regular expression to match against the text",
            defaultValue = ""
    )
    protected static final String PROP_FILTER_REGEX = Names.PROP_FILTER_REGEX;

    @ComponentProperty(
            name = "filter_out",
            description = "This setting controls how the regular expression is applied. " +
                    "When set to true, the regular expression is used to match  text that " +
                    "should be filtered out (discarded) from the output. When set to false, the regular " +
                    "expression acts as an enforcer, and only texts that match it will be included in the output " +
                    "(everything else will be discarded)",
            defaultValue = "true"
    )
    protected static final String PROP_FILTER_OUT = "filter_out";

    //--------------------------------------------------------------------------------------------


    protected Pattern _regexp = null;
    protected boolean _filterOut = false;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _regexp = Pattern.compile(getPropertyOrDieTrying(PROP_FILTER_REGEX, false, false, ccp));
        _filterOut = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_FILTER_OUT, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object inText = cc.getDataComponentFromInput(IN_TEXT);
        String text = DataTypeParser.parseAsString(inText)[0];

        boolean match = _regexp.matcher(text).matches();
        if ((_filterOut && !match) || (!_filterOut && match))
            cc.pushDataComponentToOutput(OUT_TEXT, inText);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

}
