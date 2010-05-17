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

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 *
 */

@Component(
        creator = "Lily Dong",
        description = "Inputs token counts or the Map<String, Integer> structure and filters " +
                      "tokens based on their counts. A property can be set to filter for the " +
                      "highest counts or the lowest counts.",
        name = "Top N Filter",
        tags = "token, word, filter",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TopNFilter extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "Token counts." +
                "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>"
    )
    protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "Filtered token counts." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
    )
    protected static final String OUT_FILTERED_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            defaultValue = "100",
            description = "This property sets the maximum number of tokens to be output.",
            name = Names.PROP_N_TOP_TOKENS
    )
    protected static final String PROP_UPPER_LIMIT = Names.PROP_N_TOP_TOKENS;

    @ComponentProperty(
            defaultValue = "false",
            description = "Perform bottom N filtering",
            name = Names.PROP_BOTTOM_N
    )
    protected static final String PROP_BOTTOM_N = Names.PROP_BOTTOM_N;

    //--------------------------------------------------------------------------------------------


    private int _upperLimit;
    private boolean _bottomN;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _upperLimit = Integer.parseInt(ccp.getProperty(PROP_UPPER_LIMIT));
        _bottomN = Boolean.parseBoolean(ccp.getProperty(PROP_BOTTOM_N));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Map<String, Integer> inputMap = DataTypeParser.parseAsStringIntegerMap(cc.getDataComponentFromInput(IN_TOKEN_COUNTS));

        Map<String, Integer> outputMap = inputMap;

        int upperLimit = _upperLimit;
        if (inputMap.size() > upperLimit) {
            byValueComparator bvc = new byValueComparator(inputMap);
            TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer>(bvc);
            sortedMap.putAll(inputMap);
            outputMap = new Hashtable<String, Integer>();
            while (upperLimit > 0) {
                String key = _bottomN ? sortedMap.lastKey() : sortedMap.firstKey();
                Integer value = sortedMap.get(key);
                outputMap.put(key, value);
                sortedMap.remove(key);
                --upperLimit;
            }
        }

        console.fine(String.format("Filter results:%ninput_tokens=%s%noutput_tokens=%s", inputMap.size(), outputMap.size()));
        cc.pushDataComponentToOutput(OUT_FILTERED_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(outputMap, false));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    class byValueComparator implements Comparator<String> {
        Map base_map;

        public byValueComparator(Map base_map) {
            this.base_map = base_map;
        }

        public int compare(String arg0, String arg1) {
            int result = ((Integer)base_map.get(arg1)).compareTo(
                    (Integer)base_map.get(arg0));
            if(result == 0)
                result = arg1.compareTo(arg0);
            return result;
        }
    }
}
