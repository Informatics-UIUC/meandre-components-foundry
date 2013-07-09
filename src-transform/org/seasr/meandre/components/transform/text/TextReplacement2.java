/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.seasr.meandre.components.transform.text;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

//
// FORMAT:  newText = {old1, old2, old3}; newText2 = {old4,old5}; newText3=old6"
// newValue = {oldValueA, oldValueB} OR newValue=oldValue
//
// lines are separated with ';'
// {} are optional
// ',' separate the values

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Performs simple text replacement based on input rules",
        name = "Text Replacement2",
        tags = "#TRANSFORM, text, remove, replace",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TextReplacement2 extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The text to be cleaned" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object",
            name = Names.PORT_TEXT
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    @ComponentInput(
            description = "mapData format: newText = {old1, old2, old3}; newText2 = {old4,old5}; newText3=old6; = deleteText" +
                " If you need to use an equals sign, use := to separate values (e.g.  newtext=blah := {old1=A,old2} )" +
                "Note this replacement does NOT use regular expressions and is token based.  Hence it will attemp to do " +
                "matching based on whole tokens (not prefixes, suffix, parts)" +
                "see Text Cleaner for a component that uses regular expressions." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings",
            name = "mapData"
    )
    protected static final String IN_MAP_DATA = "mapData";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The cleaned text" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings",
            name = Names.PORT_TEXT
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "ignoreCase",
            description = "ignore letter case of the matched text",
            defaultValue = "true"
        )
    protected static final String PROP_IGNORE_CASE = "ignoreCase";

    //--------------------------------------------------------------------------------------------


    protected boolean _ignoreCase;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _ignoreCase = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_IGNORE_CASE, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String inRules = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_MAP_DATA))[0];
        String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];

        text = applyRules(inRules, text);

        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    protected String applyRules(String rules, String text) throws Exception {
        Map<String,String> replacementRules = parseReplacementRules(rules);
        for (Map.Entry<String, String> entry : replacementRules.entrySet()) {
            String oldText = entry.getKey();
            String newText = entry.getValue();

            oldText = Pattern.quote(oldText);  // escape any regexp-specific characters
            oldText = "\\b" + oldText + "\\b";

            if (_ignoreCase)
                oldText = "(?i)" + oldText;

            text = text.replaceAll(oldText, newText);
        }

        return text;
    }

    protected Map<String, String> parseReplacementRules(String replacementRules) throws Exception {
        Map<String, String> rulesMap = new HashMap<String, String>();
        replacementRules = replacementRules.replaceAll("\r\n", "\n").replaceAll("\n", "");

        StringTokenizer ruleTokenizer = new StringTokenizer(replacementRules, ";");
        while (ruleTokenizer.hasMoreTokens()) {
            String rule = ruleTokenizer.nextToken();
            String[] parts = rule.split(":=");

            if (parts.length != 2)
                parts = rule.split("=");

            if (parts.length != 2)
                throw new Exception("Cannot parse replacement rule: " + rule);

            String key = parts[0];
            String values = parts[1];

            values = values.replace("{", "");
            values = values.replace("}", "");

            StringTokenizer valTokenizer = new StringTokenizer(values, ",");
            while (valTokenizer.hasMoreTokens()) {
                String value = valTokenizer.nextToken();
                rulesMap.put(value, key);
                console.finest(String.format("adding rule: '%s' -> '%s'", value, key));
            }
        }

        return rulesMap;
    }
}
