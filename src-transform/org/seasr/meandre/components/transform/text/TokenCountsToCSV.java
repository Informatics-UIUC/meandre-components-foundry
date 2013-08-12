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

import java.io.StringWriter;
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
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.encoder.CsvEncoder;
import org.supercsv.encoder.DefaultCsvEncoder;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

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
        dependency = { "protobuf-java-2.2.0.jar", "super-csv-2.1.0.jar" }
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

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_HEADER,
            description = "The header to use (leave empty if you don't want a header). <br>" +
                    "Use comma to separate the header names. Example: tokens,counts",
            defaultValue = "tokens,counts"
    )
    protected static final String PROP_HEADER = Names.PROP_HEADER;

    //--------------------------------------------------------------------------------------------


    private String[] _header;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _header = getPropertyOrDieTrying(PROP_HEADER, true, false, ccp).split(",");
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object input = cc.getDataComponentFromInput(IN_TOKEN_COUNTS);
        Map<String, Integer> tokenCountsMap = DataTypeParser.parseAsStringIntegerMap(input);

        StringWriter csvData = new StringWriter();
        ICsvBeanWriter csvWriter = null;
        try {
            final CsvEncoder csvEncoder = new DefaultCsvEncoder();
            final CsvPreference csvPreference =
                    new CsvPreference.Builder(CsvPreference.EXCEL_PREFERENCE)
                            .useEncoder(csvEncoder).build();
            csvWriter = new CsvBeanWriter(csvData, csvPreference);

            // the header elements are used to map the bean values to each column (names must match)
            final String[] beanFieldMapping = new String[] { "token", "count" };
            final CellProcessor[] processors = getProcessors();

            if (_header.length > 0)
                // write the header
                csvWriter.writeHeader(_header);

            for (Map.Entry<String, Integer> entry : tokenCountsMap.entrySet()) {
                String token = entry.getKey();
                Integer count = entry.getValue();

                // write the token counts
                csvWriter.write(new TokenCountBean(token, count), beanFieldMapping, processors);
            }
        }
        finally {
            if (csvWriter != null)
                csvWriter.close();
        }

        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(csvData.toString()));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    private static CellProcessor[] getProcessors() {

        final CellProcessor[] processors = new CellProcessor[] {
                new StrNotNullOrEmpty(), // token
                new NotNull()            // count
        };

        return processors;
    }

    public static class TokenCountBean {
        private final String _token;
        private final Integer _count;

        public TokenCountBean(String token, Integer count) {
            _token = token;
            _count = count;
        }

        public String getToken() {
            return _token;
        }

        public Integer getCount() {
            return _count;
        }
    }
}
