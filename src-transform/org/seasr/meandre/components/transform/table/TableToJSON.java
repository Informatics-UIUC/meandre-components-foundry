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

package org.seasr.meandre.components.transform.table;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Loretta Auvil
 */

@Component(
        creator = "Loretta Auvil",
        description = "Converts table to JSON format.",
        name = "Table To JSON",
        tags = "#TRANSFORM, table, JSON, convert",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TableToJSON extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------

    @ComponentInput(
            name = Names.PORT_TABLE,
            description = "The table" +
            "<br>TYPE: org.seasr.datatypes.table.Table"
    )
    protected static final String IN_TABLE = Names.PORT_TABLE;

    //------------------------------ OUTPUTS ------------------------------

    @ComponentOutput(
            name = Names.PORT_JSON,
            description = "text output as JSON" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_JSON = Names.PORT_JSON;

    //--------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Table table = (Table) cc.getDataComponentFromInput(IN_TABLE);

        int numRows = table.getNumRows();
        int numCols = table.getNumColumns();

        int[] columnTypes = new int[numCols];
        String[] columnLabels = new String[numCols];

        for (int col = 0; col < numCols; col++) {
            columnTypes[col] = table.getColumnType(col);
            String columnLabel = table.getColumnLabel(col);
            if (columnLabel == null)
                columnLabel = String.format("Col_%d", col + 1);
            columnLabels[col] = columnLabel;
        }

        JSONArray jsonData = new JSONArray();
        for (int row = 0; row < numRows; row++) {
            JSONObject jsonRow = new JSONObject();

            for (int col = 0; col < numCols; col++) {
                switch (columnTypes[col]) {
                    case ColumnTypes.INTEGER:
                        jsonRow.put(columnLabels[col], table.getInt(row,col));
                        break;

                    case ColumnTypes.FLOAT:
                        jsonRow.put(columnLabels[col], table.getFloat(row,col));
                        break;

                    case ColumnTypes.DOUBLE:
                        jsonRow.put(columnLabels[col], table.getDouble(row,col));
                        break;

                    case ColumnTypes.SHORT:
                        jsonRow.put(columnLabels[col], table.getShort(row,col));
                        break;

                    case ColumnTypes.LONG:
                        jsonRow.put(columnLabels[col], table.getLong(row,col));
                        break;

                    case ColumnTypes.STRING:
                        jsonRow.put(columnLabels[col], table.getString(row,col));
                        break;

                    case ColumnTypes.CHAR_ARRAY:
                        jsonRow.put(columnLabels[col], table.getChars(row,col));
                        break;

                    case ColumnTypes.BYTE_ARRAY:
                        jsonRow.put(columnLabels[col], table.getBytes(row,col)); // cannot display
                        break;

                    case ColumnTypes.BOOLEAN:
                        jsonRow.put(columnLabels[col], table.getBoolean(row,col));
                        break;

                    case ColumnTypes.OBJECT:
                        jsonRow.put(columnLabels[col], table.getObject(row,col));
                        break;

                    case ColumnTypes.BYTE:
                        jsonRow.put(columnLabels[col], table.getByte(row,col));
                        break;

                    case ColumnTypes.CHAR:
                        jsonRow.put(columnLabels[col], table.getChar(row,col));
                        break;

                    case ColumnTypes.NOMINAL:
                        jsonRow.put(columnLabels[col], table.getString(row,col));
                        break;

                    default:
                        jsonRow.put(columnLabels[col], table.getObject(row,col));
                        break;
                }
            }

            jsonData.put(jsonRow);
        }

        cc.pushDataComponentToOutput(OUT_JSON, BasicDataTypesTools.stringToStrings(jsonData.toString(4)));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}