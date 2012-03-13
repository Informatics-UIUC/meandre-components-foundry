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

package org.seasr.meandre.components.tools.tuples;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 *
 * @author Loretta Auvil
 *
 */

@Component(
        name = "Tuple To Table",
        creator = "Loretta Auvil",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tuple, table",
        description = "This component writes the incoming set of tuples to a Table." ,
        dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleToTable extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The set of tuples" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    @ComponentInput(
            name = Names.PROP_TABLE_FACTORY,
            description = "The TableFactory object" +
            "<br>TYPE: org.seasr.datatypes.table.TableFactory"
    )
    protected static final String IN_TABLE_FACTORY = Names.PROP_TABLE_FACTORY;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The set of tuples (same as input)" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuples (same as input)" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    @ComponentOutput(
            name = Names.PROP_TABLE,
            description = "Output Table object." +
            "<br>TYPE: org.seasr.datatypes.table.MutableTable"
    )
    protected static final String OUT_TABLE = Names.PROP_TABLE;

    //----------------------------- PROPERTIES ---------------------------------------------------


    //--------------------------------------------------------------------------------------------

    private boolean _gotInitiator;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _gotInitiator = false;
        // _fact = null;
        // _table = null;
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {

        TableFactory _fact;
        MutableTable _table;
        Strings[] in = null;
        SimpleTuplePeer tuplePeer = null;
        SimpleTuple tuple = null;

        Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        tuplePeer = new SimpleTuplePeer(inputMeta);
        tuple = tuplePeer.createTuple();

        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
        in = BasicDataTypesTools.stringsArrayToJavaArray(input);

        _fact = (TableFactory)cc.getDataComponentFromInput(IN_TABLE_FACTORY);
        _table = (MutableTable)_fact.createTable();

        if ((_table != null) && (in != null) && (tuple != null)) {
            int size = tuplePeer.size();
            console.fine("Number of rows: "+in.length+" Number of columns: "+size);

            for (int j = 0; j < size; j++) {
                int col=0;
                col = _table.getNumColumns();
                //if (i == 0) { // set columns up the first time

                Column column = _fact.createColumn(ColumnTypes.STRING);
                column.setLabel(tuplePeer.getFieldNameForIndex(j));

                _table.addColumn(column);
            }
            _table.addRows(in.length);

            for (int j = 0; j < size; j++) {
                for (int i = 0; i < in.length; i++) {

                    tuple.setValues(in[i]);
                    //int row = _table.getNumRows();

                    String value = tuple.getValue(j);
                    if (value.length()<100)
                        console.finest("Setting table: ("+i+","+j+")="+value);
                    else
                        console.finest("Setting table: ("+i+","+j+")="+value.substring(0, 100)+"...");
                    _table.setString(value, i, j);
                }
            }
            //if (!_gotInitiator) {
            console.fine(String.format("The resulting table has %,d row(s) and %,d column(s)", _table.getNumRows(), _table.getNumColumns()));

            cc.pushDataComponentToOutput(OUT_TABLE, _table);
            //    _table = null;
            //}
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
