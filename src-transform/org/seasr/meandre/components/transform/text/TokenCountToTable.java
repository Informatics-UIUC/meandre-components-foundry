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

import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.datatypes.datamining.table.basic.BasicTableFactory;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Converts token counts to table.",
        name = "Token Count To Table",
        tags = "#TRANSFORM, token, count, table, convert",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TokenCountToTable extends AbstractExecutableComponent {

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
            name = Names.PROP_TABLE,
            description = "The table." +
                "<br>TYPE: org.seasr.datatypes.table.MutableTable"
    )
    protected static final String OUT_TABLE = Names.PROP_TABLE;

    //--------------------------------------------------------------------------------------------


    private static final TableFactory TABLE_FACTORY = new BasicTableFactory();


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object inTokenCounts = cc.getDataComponentFromInput(IN_TOKEN_COUNTS);
        Map<String, Integer> tokenCounts = DataTypeParser.parseAsStringIntegerMap(inTokenCounts);

        MutableTable table = (MutableTable) TABLE_FACTORY.createTable();

        // create the token column
        Column colToken = TABLE_FACTORY.createColumn(ColumnTypes.STRING);
        colToken.setLabel("token");

        // create the count column
        Column colCount = TABLE_FACTORY.createColumn(ColumnTypes.INTEGER);
        colCount.setLabel("count");

        table.addColumns(new Column[] { colToken, colCount });

        if (tokenCounts.size() > 0) {
            table.addRows(tokenCounts.size());

            int row = 0;
            for (Map.Entry<String, Integer> entry : tokenCounts.entrySet()) {
                String token = entry.getKey();
                Integer count = entry.getValue();

                table.setString(token, row, 0);
                table.setInt(count, row, 1);

                row++;
            }
        }

        console.fine(String.format("The resulting table has %,d row(s) and %,d column(s)", table.getNumRows(), table.getNumColumns()));

        cc.pushDataComponentToOutput(OUT_TABLE, table);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
