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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.meandre.annotations.Component;
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
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.encoder.CsvEncoder;
import org.supercsv.encoder.DefaultCsvEncoder;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author Boris Capitanu
 *
 */

@Component(
        creator = "Boris Capitanu",
        description = "Converts CSV text to token counts structure.",
        name = "CSV To Token Counts",
        tags = "#TRANSFORM, CSV, text, token count",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "protobuf-java-2.2.0.jar", "super-csv-2.1.0.jar" }
)
public class CSVToTokenCounts extends AbstractExecutableComponent{

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The CSV token count data" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The token counts" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
    )
    protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_HEADER,
            description = "Does the CSV data contain a header?",
            defaultValue = "true"
    )
    protected static final String PROP_HEADER = Names.PROP_HEADER;

    @ComponentProperty(
            name = "enable_constraint_checking",
            description = "Should checks be made against the CSV data to ensure that tokens " +
                    "are unique and counts are integers and not null? There is a performance penalty when turning this on.",
            defaultValue = "false"
    )
    protected static final String PROP_CONSTRAINT_CHECK = "enable_constraint_checking";

    @ComponentProperty(
            name = "token_pos",
            description = "The position of the token (the 'token' column) in the CSV (0=first, 1=second, etc.)",
            defaultValue = "0"
    )
    protected static final String PROP_TOKEN_POS = "token_pos";

    @ComponentProperty(
            name = "count_pos",
            description = "The position of the count (the 'count' column) in the CSV (0=first, 1=second, etc.)",
            defaultValue = "1"
    )
    protected static final String PROP_COUNT_POS = "count_pos";

    @ComponentProperty(
            name = "column_count",
            description = "The number of columns in the CSV data (required if the CSV data has no header)",
            defaultValue = ""
    )
    protected static final String PROP_COLUMN_COUNT = "column_count";

    @ComponentProperty(
            name = Names.PROP_ORDERED,
            description = "Should the resulting token counts be ordered?",
            defaultValue = "true"
    )
    protected static final String PROP_ORDERED = Names.PROP_ORDERED;

    //--------------------------------------------------------------------------------------------


    protected boolean _hasHeader, _ordered, _enableValidation;
    protected int _tokenPos, _countPos;
    protected Integer _columnCount;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _hasHeader = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HEADER, ccp));
        _ordered = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_ORDERED, ccp));
        if (!_hasHeader)
            _columnCount = Integer.parseInt(getPropertyOrDieTrying(PROP_COLUMN_COUNT, ccp));
        _enableValidation = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_CONSTRAINT_CHECK, ccp));
        _tokenPos = Integer.parseInt(getPropertyOrDieTrying(PROP_TOKEN_POS, ccp));
        _countPos = Integer.parseInt(getPropertyOrDieTrying(PROP_COUNT_POS, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        final Map<String,Integer> tokenCountMap = new HashMap<String,Integer>();
        final String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];

        final StringReader csvData = new StringReader(text);
        ICsvMapReader csvReader = null;
        try {
            final CsvEncoder csvEncoder = new DefaultCsvEncoder();
            final CsvPreference csvPreference =
                    new CsvPreference.Builder(CsvPreference.EXCEL_PREFERENCE)
                            .useEncoder(csvEncoder).build();
            csvReader = new CsvMapReader(csvData, csvPreference);

            // the header columns are used as the keys to the Map
            String[] header;
            if (_hasHeader) {
                header = csvReader.getHeader(true);
                _columnCount = header.length;

                // setting header to ignore all columns except the ones containing the token and count
                for (int i = 0; i < _columnCount; i++)
                    if (i != _tokenPos && i != _countPos)
                        header[i] = null;
            } else {
                header = new String[_columnCount];
                header[_tokenPos] = "token";
                header[_countPos] = "count";
            }

            final CellProcessor[] processors = new CellProcessor[_columnCount];
            if (_enableValidation)
                processors[_tokenPos] = new StrNotNullOrEmpty(new Unique());

            // enable processor to convert counts from string to integer
            processors[_countPos] = new NotNull(new ParseInt());

            Map<String, Object> tokenCountEntry;
            while ((tokenCountEntry = csvReader.read(header, processors)) != null) {
                String token = tokenCountEntry.get(header[_tokenPos]).toString();
                Integer count = (Integer) tokenCountEntry.get(header[_countPos]);

                tokenCountMap.put(token, count);
            }
        }
        finally {
            if (csvReader != null)
                csvReader.close();
        }

        cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(tokenCountMap, _ordered));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
