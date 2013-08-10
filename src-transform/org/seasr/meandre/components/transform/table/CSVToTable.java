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

package org.seasr.meandre.components.transform.table;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.datatypes.datamining.table.basic.BasicTableFactory;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.transform.csv.ParseByte;
import org.seasr.meandre.support.components.transform.csv.ParseFloat;
import org.seasr.meandre.support.components.transform.csv.ParseShort;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseChar;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "CSV To Table",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, table, convert, csv",
        description = "This component converts a CSV to Table." ,
        dependency = { "protobuf-java-2.2.0.jar", "super-csv-2.1.0.jar" }
)
public class CSVToTable extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The CSV data" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_CSV = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TABLE,
            description = "The table" +
                    "<br>TYPE: org.seasr.datatypes.table.Table"
    )
    protected static final String OUT_TABLE = Names.PORT_TABLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_HEADER,
            description = "Does the CSV data contain a header?",
            defaultValue = "true"
    )
    protected static final String PROP_HEADER = Names.PROP_HEADER;

    @ComponentProperty(
            name = "column_count",
            description = "How many columns are in the CSV data? (optional: only needed if the CSV does not contain a header)",
            defaultValue = ""
    )
    protected static final String PROP_COL_COUNT = "column_count";

    @ComponentProperty(
            name = "column_types",
            description = "Specifies the data type of each column in the CSV data. " +
                    "Comma-separated list of type-code values, one for each column, where each code represents the type according to the following table: " +
                    "<p><table>" +
                    "<tr><th>Type</th><th>Type Code</th></tr>" +
                    "<tr><td>Integer</td><td>0</td></tr>" +
                    "<tr><td>Float</td><td>1</td></tr>" +
                    "<tr><td>Double</td><td>2</td></tr>" +
                    "<tr><td>Short</td><td>3</td></tr>" +
                    "<tr><td>Long</td><td>4</td></tr>" +
                    "<tr><td>String</td><td>5</td></tr>" +
                    "<tr><td>Boolean</td><td>8</td></tr>" +
                    "<tr><td>Byte</td><td>10</td></tr>" +
                    "<tr><td>Char</td><td>11</td></tr>" +
                    "</table></p>" +
                    "<br>Example: 5,0  -- representing CSV data with 2 columns where first column contains string (textual) data, " +
                    "and second column contains integer data." +
                    "<br>(optional: if not specified, all columns are going to be assumed to contain string data)",
            defaultValue = ""
    )
    protected static final String PROP_COL_TYPES = "column_types";

    //--------------------------------------------------------------------------------------------

    protected static final TableFactory TABLE_FACTORY = new BasicTableFactory();
    protected static Integer[] VALID_TYPES = new Integer[] {
        ColumnTypes.INTEGER,	// 0
        ColumnTypes.FLOAT,		// 1
        ColumnTypes.DOUBLE,		// 2
        ColumnTypes.SHORT,		// 3
        ColumnTypes.LONG,		// 4
        ColumnTypes.STRING,		// 5
        ColumnTypes.BOOLEAN,	// 8
        ColumnTypes.BYTE,		// 10
        ColumnTypes.CHAR		// 11
    };

    protected boolean _hasHeader;
    protected int _columnCount;
    protected Integer[] _columnTypes;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _hasHeader = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HEADER, ccp));

        List<Integer> validTypes = Arrays.asList(VALID_TYPES);
        String colTypes = getPropertyOrDieTrying(PROP_COL_TYPES, true, false, ccp);
        if (!colTypes.isEmpty()) {
            String[] types = colTypes.split(",");
            _columnTypes = new Integer[types.length];
            for (int i = 0; i < types.length; i++)
                _columnTypes[i] = Integer.parseInt(types[i]);

            if (!validTypes.containsAll(Arrays.asList(_columnTypes)))
                throw new ComponentContextException("Invalid column type code specified in the " + PROP_COL_TYPES + " property!");
        }

        if (!_hasHeader)
            _columnCount = Integer.parseInt(getPropertyOrDieTrying(PROP_COL_COUNT, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_CSV))[0];
        MutableTable table = (MutableTable) TABLE_FACTORY.createTable();

        final StringReader csvData = new StringReader(text);
        ICsvMapReader csvReader = null;
        try {
            csvReader = new CsvMapReader(csvData, CsvPreference.EXCEL_PREFERENCE);

            // the header columns are used as the keys to the Map
            String[] header;
            if (_hasHeader) {
                header = csvReader.getHeader(true);
                _columnCount = header.length;

                List<String> nonUniqueHeaderLabels = checkUniqueHeaderLabels(header);
                if (nonUniqueHeaderLabels != null)
                    throw new ComponentExecutionException("The supplied CSV contains non-unique header labels! " +
                            "This will cause the data in the non-unique column to overwrite each other! " +
                            "The duplicate labels are: " + nonUniqueHeaderLabels);
            } else {
                header = new String[_columnCount];
                for (int col = 0; col < _columnCount; col++)
                    header[col] = String.format("Col_%d", col+1);
            }

            if (_columnTypes != null && _columnTypes.length != _columnCount) {
                console.warning("Inconsistency between the number of columns in the data and " +
                        "the number of column types specified! Falling back to all textual column types...");
                _columnTypes = null;
            }

            Column[] tableColumns = new Column[_columnCount];
            for (int col = 0; col < _columnCount; col++) {
                int colType = (_columnTypes != null) ? _columnTypes[col] : ColumnTypes.STRING;
                Column column = TABLE_FACTORY.createColumn(colType);
                column.setLabel(header[col]);
                tableColumns[col] = column;
            }

            table.addColumns(tableColumns);

            CellProcessor[] processors = new CellProcessor[_columnCount];
            for (int col = 0; col < _columnCount; col++) {
                CellProcessor processor = null;

                if (_columnTypes != null) {
                    switch (_columnTypes[col]) {
                        case ColumnTypes.INTEGER:
                            processor = new ParseInt();
                            break;

                        case ColumnTypes.FLOAT:
                            processor = new ParseFloat();
                            break;

                        case ColumnTypes.DOUBLE:
                            processor = new ParseDouble();
                            break;

                        case ColumnTypes.SHORT:
                            processor = new ParseShort();
                            break;

                        case ColumnTypes.LONG:
                            processor = new ParseLong();
                            break;

                        case ColumnTypes.BOOLEAN:
                            processor = new ParseBool();
                            break;

                        case ColumnTypes.BYTE:
                            processor = new ParseByte();
                            break;

                        case ColumnTypes.CHAR:
                            processor = new ParseChar();
                            break;

                        default:  // ColumnTypes.STRING
                            processor = null;
                            break;
                    }
                }

                processor = (processor != null) ?
                        new Optional(processor) : new Optional();  // use Optional to allow for possible null values in CSV data

                processors[col] = processor;
            }

            Map<String, Object> csvEntry;
            while ((csvEntry = csvReader.read(header, processors)) != null) {
                int row = table.getNumRows();
                table.addRows(1);

                for (int col = 0; col < _columnCount; col++) {
                    String headerLabel = header[col];
                    Object value = csvEntry.get(headerLabel);
                    table.setObject(value, row, col);
                }
            }
        }
        finally {
            if (csvReader != null)
                csvReader.close();
        }

        console.fine(String.format("The resulting table has %,d row(s) and %,d column(s)", table.getNumRows(), table.getNumColumns()));

        cc.pushDataComponentToOutput(OUT_TABLE, table);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    protected static List<String> checkUniqueHeaderLabels(String[] header) {
        // TODO Auto-generated method stub
        return null;
    }

}
