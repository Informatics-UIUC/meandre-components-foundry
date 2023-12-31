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
import java.util.Map.Entry;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.datatypes.datamining.table.sparse.SparseTableFactory;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This component accumulates a set of token counts representing documents and constructs a sparse table <br>" +
                "where each row represents a document, and each column represents a word in the document. The value stored <br>" +
                "at a particular x,y coordinate in the table represents the frequency (count) of word 'y' from document 'x'. <br>" +
                "This type of table is necessary for being able to run a number of data analysis algorithms available in Meandre.",
        name = "Token Count To Feature Table",
        tags = "#TRANSFORM, token, count, table, convert",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TokenCountsToFeatureTable extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The token counts" +
                "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
    )
    protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    @ComponentInput(
            name = "label",
            description = "The label (class) to associate with this document." +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_LABEL = "label";

    @ComponentInput(
            name = "id",
            description = "The id to associate with this entry in the Feature Table. The id column will not be added as an input feature." +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_ID = "id";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PROP_TABLE,
            description = "The feature table." +
                "<br>TYPE: org.seasr.datatypes.table.ExampleTable"
    )
    protected static final String OUT_TABLE = Names.PROP_TABLE;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "The name of the table column holding the class label information",
            name = "class_column_label",
            defaultValue = "_class"
    )
    protected static final String PROP_CLASS_COLUMN_LABEL = "class_column_label";

    //--------------------------------------------------------------------------------------------


    protected static final TableFactory TABLE_FACTORY = new SparseTableFactory();
    protected static final String LABEL_ID = "_id";  // Note: By convention the ID will be stored in column 1

    // Note: By convention the ID is stored in column 1 while the label (possibly empty for unlabeled data) in column 0
    //       You can't change the values below because the order in which the columns get created in code matters!
    protected static final int LABEL_COL_IDX = 0;
    protected static final int ID_COL_IDX = 1;

    protected boolean _isStreaming;
    protected MutableTable _table;
    protected Map<String, Integer> _tokenColumnMap;
    protected String _classColumnLabel;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _classColumnLabel = getPropertyOrDieTrying(PROP_CLASS_COLUMN_LABEL, ccp);
        _isStreaming = false;
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (_table == null)
            throw new ComponentExecutionException("Start stream marker not received!");

        String label = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LABEL))[0];
        String id = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_ID))[0];

        Object inTokenCounts = cc.getDataComponentFromInput(IN_TOKEN_COUNTS);
        Map<String, Integer> tokenCounts = DataTypeParser.parseAsStringIntegerMap(inTokenCounts);

        int row = _table.getNumRows();
        _table.addRows(1);
        _table.setString(label, row, LABEL_COL_IDX);  // the label/class always goes in column 0
        _table.setString(id, row, ID_COL_IDX);        // the id goes in column 1  (by convention)

        for (Entry<String, Integer> entry : tokenCounts.entrySet()) {
            String token = entry.getKey();
            int count = entry.getValue();

            int col;

            if (!_tokenColumnMap.containsKey(token)) {
                col = _table.getNumColumns();
                Column column = TABLE_FACTORY.createColumn(ColumnTypes.INTEGER);
                column.setLabel(token);
                _table.addColumn(column);
                _tokenColumnMap.put(token, col);
            } else
                col = _tokenColumnMap.get(token);

            _table.setInt(count, row, col);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _table = null;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return true;
    }

    @Override
    public void startStream() throws Exception {
        if (_isStreaming)
            console.severe("Stream error - start stream marker already received!");

        _isStreaming = true;
        _table = (MutableTable) TABLE_FACTORY.createTable();

        // create the label/class column
        Column labelCol = TABLE_FACTORY.createColumn(ColumnTypes.STRING);
        labelCol.setLabel(_classColumnLabel);
        _table.addColumn(labelCol);

        // create the id column
        Column idCol = TABLE_FACTORY.createColumn(ColumnTypes.STRING);
        idCol.setLabel(LABEL_ID);
        _table.addColumn(idCol);

        _tokenColumnMap = new HashMap<String, Integer>();
    }

    @Override
    public void endStream() throws Exception {
        if (!_isStreaming)
            console.severe("Stream error - received end stream marker without start stream!");

        ExampleTable featureTable = _table.toExampleTable();
        int numCols = featureTable.getNumColumns();
        int[] inputFeatures = new int[numCols - 2];  // two of the columns are not input features: label/class and id
        for (int i = 0; i < numCols-2; i++)
            inputFeatures[i] = i + 2;
        int[] outputFeatures = new int[] { 0 };  // label/class is always in column 0
        featureTable.setInputFeatures(inputFeatures);
        featureTable.setOutputFeatures(outputFeatures);

        console.fine(String.format("The resulting table has %,d row(s) and %,d column(s) with %,d input features and %,d output features",
                featureTable.getNumRows(), featureTable.getNumColumns(), inputFeatures.length, outputFeatures.length));
        componentContext.pushDataComponentToOutput(OUT_TABLE, featureTable);

        _isStreaming = false;
        _table = null;
        _tokenColumnMap.clear();
        _tokenColumnMap = null;
    }

}
