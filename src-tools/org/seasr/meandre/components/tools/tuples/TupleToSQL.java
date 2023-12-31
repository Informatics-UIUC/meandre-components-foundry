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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.components.db.DBUtils;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

import com.jolbox.bonecp.BoneCP;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Tuple To SQL",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.any,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tuple, tools, database, db",
        description = "This component writes tuples to a db table",
        dependency = { "protobuf-java-2.2.0.jar", "sqlite-jdbc-3.7.2.jar",
                       "guava-14.0.1.jar" }
)
public class TupleToSQL extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
            name = "db_conn_pool",
            description = "The DB connection pool used for providing / managing connections to the specified database" +
                "<br>TYPE: com.jolbox.bonecp.BoneCP"
    )
    protected static final String IN_DB_CONN_POOL = "db_conn_pool";

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    @ComponentInput(
            name = "table_name",
            description = "The table name to use" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_TABLE_NAME = "table_name";


    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "table_name",
            description = "The table name where the tuples have been written to" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TABLE_NAME = "table_name";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The column definitions (example: name VARCHAR(30) NOT NULL, birthday DATETIME, isRetired BOOLEAN). " +
                    "Note: The names of the columns must match the meta tuple field names, but order is arbitrary. " +
                    "You can also specify just a subset of the tuple's fields, so that only those fields are saved in the database.",
            name = "column_definitions",
            defaultValue = ""
    )
    protected static final String PROP_COLUMNDEFS = "column_definitions";

    @ComponentProperty(
            description = "Any table options usually specified after the column definitions " +
                    "(example: ENGINE=InnoDB DEFAULT CHARACTER SET utf8)",
            name = "table_options",
            defaultValue = ""
    )
    protected static final String PROP_TABLE_OPTIONS = "table_options";

    @ComponentProperty(
            description = "Set to true if you want the table to be removed when the flow concludes",
            name = "drop_table",
            defaultValue = "true"
    )
    protected static final String PROP_DROP_TABLE = "drop_table";

    //--------------------------------------------------------------------------------------------


    protected static final int MAX_INSERTS_PER_BATCH = 100;

    protected BoneCP connectionPool = null;

    protected String _columnDefs;
    protected String _tableOptions;

    protected boolean _dropTable;
    protected boolean _isStreaming = false;
    protected List<String> _currentTableColumns;

    protected Set<String> _tableNames = new HashSet<String>();


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _columnDefs = getPropertyOrDieTrying(PROP_COLUMNDEFS, ccp);
        _tableOptions = getPropertyOrDieTrying(PROP_TABLE_OPTIONS, true, false, ccp);
        _dropTable = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_DROP_TABLE, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        componentInputCache.storeIfAvailable(cc, IN_TABLE_NAME);
        componentInputCache.storeIfAvailable(cc, IN_META_TUPLE);
        componentInputCache.storeIfAvailable(cc, IN_TUPLES);

        if (cc.isInputAvailable(IN_DB_CONN_POOL)) {
            Object in_conn_pool = cc.getDataComponentFromInput(IN_DB_CONN_POOL);
            if (!(in_conn_pool instanceof StreamDelimiter)) {
                if (connectionPool == null)
                    connectionPool = (BoneCP) in_conn_pool;
//                else
//                    console.warning("The connection pool can only be set once! Ignoring input from port '" + IN_DB_CONN_POOL + "'");
            } else
                console.warning("Stream delimiters should not arrive on port '" + IN_DB_CONN_POOL + "'. Ignoring...");
        }

        if (connectionPool == null || !componentInputCache.hasDataAll(new String[] { IN_TABLE_NAME, IN_META_TUPLE, IN_TUPLES }))
            // we're not ready to process yet, return
            return;

        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            PreparedStatement ps = null;

            do {
                Object inTableName = componentInputCache.peek(IN_TABLE_NAME);
                if (inTableName instanceof StreamDelimiter) {
                    console.warning("Stream delimiters should not arrive on port '" + IN_TABLE_NAME + "'. Ignoring...");
                    componentInputCache.retrieveNext(IN_TABLE_NAME);
                    continue;
                }

                Object inMeta = componentInputCache.retrieveNext(IN_META_TUPLE);
                Object inTuple = componentInputCache.retrieveNext(IN_TUPLES);
                String tableName = DataTypeParser.parseAsString(inTableName)[0];

                if (inMeta instanceof StreamInitiator || inTuple instanceof StreamInitiator) {
                    if (inMeta instanceof StreamInitiator && inTuple instanceof StreamInitiator) {
                        StreamInitiator siMeta = (StreamInitiator) inMeta;
                        StreamInitiator siTuple = (StreamInitiator) inTuple;
                        if (siMeta.getStreamId() != siTuple.getStreamId())
                            throw new ComponentExecutionException("Streaming error - received different stream ids on different ports!");

                        if (siMeta.getStreamId() != streamId) {
                            // Forward the stream delimiter along
                            cc.pushDataComponentToOutput(OUT_TABLE_NAME, siMeta);
                            continue;
                        }

                        console.finer("Received StreamInitiator");
                        if (_isStreaming)
                            console.severe("Stream error - start stream marker already received!");

                        createNewTable(connection, tableName);
                        _isStreaming = true;

                        continue;
                    } else
                        throw new ComponentExecutionException("Unbalanced stream delimiter received!");
                }

                if (inMeta instanceof StreamTerminator || inTuple instanceof StreamTerminator) {
                    if (inMeta instanceof StreamTerminator && inTuple instanceof StreamTerminator) {
                        StreamTerminator stMeta = (StreamTerminator) inMeta;
                        StreamTerminator stTuple = (StreamTerminator) inTuple;
                        if (stMeta.getStreamId() != stTuple.getStreamId())
                            throw new ComponentExecutionException("Streaming error - received different stream ids on different ports!");

                        if (stMeta.getStreamId() != streamId) {
                            // Forward the stream delimiter along
                            cc.pushDataComponentToOutput(OUT_TABLE_NAME, stMeta);
                            continue;
                        }

                        console.finer("Received StreamTerminator");
                        if (!_isStreaming)
                            console.severe("Stream error - end stream marker received without a start stream marker!");

                        cc.pushDataComponentToOutput(OUT_TABLE_NAME, new StreamInitiator(streamId));
                        cc.pushDataComponentToOutput(OUT_TABLE_NAME, tableName);
                        cc.pushDataComponentToOutput(OUT_TABLE_NAME, new StreamTerminator(streamId));

                        _isStreaming = false;
                        componentInputCache.retrieveNext(IN_TABLE_NAME); // remove from queue the current table name

                        continue;
                    } else
                        throw new ComponentExecutionException("Unbalanced stream delimiter received!");
                }

                if (!_isStreaming)  {
                    createNewTable(connection, tableName);
                    componentInputCache.retrieveNext(IN_TABLE_NAME); // remove from queue the current table name
                }

                SimpleTuplePeer metaPeer  = new SimpleTuplePeer((Strings) inMeta);
                Strings[] tuples = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) inTuple);
                SimpleTuple tuple = metaPeer.createTuple();

                if (console.isLoggable(Level.FINER)) {
                    StringBuilder sb = new StringBuilder();
                    for (String fieldName : metaPeer.getFieldNames())
                        sb.append(", '").append(fieldName).append("'");
                    console.finer("Tuple field names (single quotes added): " + sb.substring(2));
                }

                try {
                    ps = connection.prepareStatement(
                            String.format("INSERT INTO %s VALUES (%s);",
                                    tableName, getSQLInsertParams(_currentTableColumns)));

                    int count = 0;

                    for (Strings t : tuples) {
                        tuple.setValues(t);

                        for (int i = 0, iMax = _currentTableColumns.size(); i < iMax; i++) {
                            console.finer("Retrieving tuple value for column (single quotes added): '" + _currentTableColumns.get(i) + "'");
                            String tupleValue = tuple.getValue(_currentTableColumns.get(i));
                            ps.setObject(i + 1, tupleValue.length() > 0 ? tupleValue : null);
                        }

                        ps.addBatch();

                        // only batch MAX_INSERTS_PER_BATCH inserts at a time for better efficiency
                        if (++count > MAX_INSERTS_PER_BATCH - 1) {
                            ps.executeBatch();
                            count = 0;
                        }
                    }

                    if (count > 0)
                        ps.executeBatch();
                }
                catch (SQLException e) {
                    String newLine = System.getProperty("line.separator");
                    StringBuilder sb = new StringBuilder();
                    sb.append(newLine);
                    for (Strings t : tuples)
                        sb.append(SimpleTuplePeer.toString(BasicDataTypesTools.stringsToStringArray(t))).append(newLine);

                    console.log(Level.SEVERE, sb.toString(), e);
                    throw e;
                }
                finally {
                    DBUtils.closeStatement(ps);
                    ps = null;
                }

                if (!_isStreaming)
                    cc.pushDataComponentToOutput(OUT_TABLE_NAME, tableName);

            } while (componentInputCache.hasDataAll(new String[] { IN_TABLE_NAME, IN_META_TUPLE, IN_TUPLES }));
        }
        finally {
            DBUtils.releaseConnection(connection);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        // Do cleanup here (delete created tables)
        if (_dropTable && connectionPool != null && _tableNames.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String tableName : _tableNames)
                sb.append(",").append(tableName);
            String tables = sb.substring(1);

            Connection conn = null;
            Statement stmt = null;
            try {
                conn = connectionPool.getConnection();
                stmt = conn.createStatement();
                stmt.execute(String.format("DROP TABLE IF EXISTS %s;", tables));
            }
            finally {
                DBUtils.releaseConnection(conn, stmt);
            }
        }

        connectionPool = null;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return true;
    }

    @Override
    public void handleStreamInitiators() throws Exception {
        executeCallBack(componentContext);
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        executeCallBack(componentContext);
    }

    //--------------------------------------------------------------------------------------------

    protected void createNewTable(Connection connection, String tableName) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(String.format("CREATE TABLE IF NOT EXISTS %s (%s) %s;", tableName, _columnDefs, _tableOptions));
            _tableNames.add(tableName);

            ResultSet rs = stmt.executeQuery(
                    String.format("SHOW COLUMNS FROM %s;", tableName));
            _currentTableColumns = new ArrayList<String>();
            while (rs.next())
                _currentTableColumns.add(rs.getString(1));
        }
        finally {
            DBUtils.closeStatement(stmt);
        }
    }

    protected String getSQLInsertParams(List<String> tableColumns) {
        String params = "";

        for (int i = 0, iMax = tableColumns.size(); i < iMax; i++)
            params += ",?";

        return params.substring(1);
    }
}
