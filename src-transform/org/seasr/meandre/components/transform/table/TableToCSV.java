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

import java.io.StringWriter;

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
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Table To CSV",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, table, convert, csv",
        description = "This component converts a Table to CSV." ,
        dependency = { "protobuf-java-2.2.0.jar", "super-csv-2.1.0.jar" }
)
public class TableToCSV extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TABLE,
            description = "The table" +
                    "<br>TYPE: org.seasr.datatypes.table.Table"
    )
    protected static final String IN_TABLE = Names.PORT_TABLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The CSV representation of the table" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_HEADER,
            description = "Should the header be added to the CSV output? ",
            defaultValue = "true"
    )
    protected static final String PROP_HEADER = Names.PROP_HEADER;

    //--------------------------------------------------------------------------------------------


    protected boolean _addHeader;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _addHeader = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HEADER, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Table table = (Table) cc.getDataComponentFromInput(IN_TABLE);

        int numRows = table.getNumRows();
        int numCols = table.getNumColumns();

        StringWriter csvData = new StringWriter();
        ICsvListWriter csvWriter = null;
        try {
            csvWriter = new CsvListWriter(csvData, CsvPreference.EXCEL_PREFERENCE);

            String[] header = new String[numCols];
            for (int i = 0; i < numCols; i++) {
                String columnLabel = table.getColumnLabel(i);
                if (columnLabel == null || columnLabel.isEmpty())
                    columnLabel = String.format("Col_%d", i + 1);
                header[i] = columnLabel;
            }

            if (_addHeader)
                csvWriter.writeHeader(header);

            Object[] rowData = new Object[numCols];
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++)
                    rowData[col] = table.getObject(row, col);

                csvWriter.write(rowData);
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
}
