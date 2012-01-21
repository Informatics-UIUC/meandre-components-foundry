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

package org.seasr.meandre.components.tools.db;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
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
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.components.db.DBUtils;
import org.seasr.meandre.support.generic.io.Serializer;
import org.seasr.meandre.support.generic.util.UUIDUtils;

import com.jolbox.bonecp.BoneCP;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Retrive Persisted Data",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.any,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "tools, database, db, persist, serialize",
        description = "This component retrieves persisted data from a database",
        dependency = { "protobuf-java-2.2.0.jar", "sqlite-jdbc-3.7.2.jar",
                       "guava-r09.jar", "slf4j-api-1.6.1.jar", "slf4j-log4j12-1.6.1.jar" }
)
public class RetrieveFromDB extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
            name = "db_conn_pool",
            description = "The DB connection pool used for providing / managing connections to the specified database" +
                "<br>TYPE: com.jolbox.bonecp.BoneCP"
    )
    protected static final String IN_DB_CONN_POOL = "db_conn_pool";

    @ComponentInput(
            name = "id",
            description = "Identifier for the persisted data" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_ID = "id";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "data",
            description = "The persisted data"
    )
    protected static final String OUT_DATA = "data";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "db_table",
            description = "The table name where the persisted data is stored",
            defaultValue = ""
    )
    protected static final String PROP_TABLE = "db_table";

    //--------------------------------------------------------------------------------------------


    /** This is the table used as a directory of metainformation for persistence "units" */
    public static final String PERSISTENCE_META_TABLE_NAME = "persistence_meta";

    protected BoneCP connectionPool = null;

    protected String _dbTable;

    protected String _sqlQueryMeta;
    protected String _sqlQueryData;

    /** The number of ports on which retrieved data can be output */
    protected final int _portCount = 1;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _dbTable = getPropertyOrDieTrying(PROP_TABLE, ccp);
        _sqlQueryMeta = String.format(
                "SELECT table_name, streaming, port_count FROM %s WHERE uuid = ?", PERSISTENCE_META_TABLE_NAME);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        componentInputCache.storeIfAvailable(cc, IN_ID);

        if (cc.isInputAvailable(IN_DB_CONN_POOL)) {
            Object in_conn_pool = cc.getDataComponentFromInput(IN_DB_CONN_POOL);
            if (!(in_conn_pool instanceof StreamDelimiter)) {
                if (connectionPool == null)
                    connectionPool = (BoneCP) in_conn_pool;
                else
                    console.warning("The connection pool can only be set once! Ignoring input from port '" + IN_DB_CONN_POOL + "'");
            } else
                console.warning("Stream delimiters should not arrive on port '" + IN_DB_CONN_POOL + "'. Ignoring...");
        }

        if (connectionPool == null || !componentInputCache.hasData(IN_ID))
            // we're not ready to process yet, return
            return;

        Connection connection = null;
        PreparedStatement psMeta = null;
        PreparedStatement psData = null;
        try {
            connection = connectionPool.getConnection();
            psMeta = connection.prepareStatement(_sqlQueryMeta);

            Object input;
            while ((input = componentInputCache.retrieveNext(IN_ID)) != null) {
                if (input instanceof StreamDelimiter) {
                    StreamDelimiter sd = (StreamDelimiter) input;
                    if (sd.getStreamId() == streamId)
                        throw new ComponentExecutionException(String.format("Stream id conflict! Incoming stream has the same id (%d) " +
                                "as the one set for this component (%s)!", streamId, getClass().getSimpleName()));

                    cc.pushDataComponentToOutput(OUT_DATA, sd);
                    continue;
                }

                for (String sUUID : DataTypeParser.parseAsString(input)) {
                    BigDecimal uuid = new BigDecimal(UUIDUtils.toBigInteger(UUID.fromString(sUUID)));
                    psMeta.setBigDecimal(1, uuid);
                    ResultSet rs = psMeta.executeQuery();

                    if (rs.next()) {
                        int portCount = rs.getInt("port_count");
                        if (portCount != _portCount) {
                            outputError(String.format("This component cannot be used to retrieve data for id '%s'! " +
                            		"Reason: port number mismatch! (should be: %d, actual: %d) - " +
                            		"Use a component with a matching number of output ports!", input, _portCount, portCount), Level.SEVERE);
                            continue;
                        }
                        String tableName = rs.getString("table_name");
                        boolean isStreaming = rs.getBoolean("streaming");

                        if (isStreaming)
                            cc.pushDataComponentToOutput(OUT_DATA, new StreamInitiator(streamId));

                        String sqlQueryData = String.format(
                                "SELECT data, type, port_name FROM %s WHERE uuid = ? ORDER BY seq_no ASC", tableName);
                        psData = connection.prepareStatement(sqlQueryData);
                        psData.setBigDecimal(1, uuid);
                        rs = psData.executeQuery();

                        while (rs.next()) {
                            InputStream dataStream = null;
                            try {
                                dataStream = rs.getBinaryStream("data");
                                String type = rs.getString("type");

                                console.finer(String.format("from db: type='%s'", type));

                                Object obj = Serializer.deserializeObject(dataStream);
                                cc.pushDataComponentToOutput(OUT_DATA, obj);
                            }
                            finally {
                                if (dataStream != null)
                                    dataStream.close();
                            }
                        }

                        if (isStreaming)
                            cc.pushDataComponentToOutput(OUT_DATA, new StreamTerminator(streamId));
                    } else
                        outputError(String.format("Could not locate data for id '%s'!", input), Level.WARNING);
                }
            }
        }
        finally {
            DBUtils.releaseConnection(connection, psMeta, psData);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        connectionPool = null;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }

    @Override
    public void handleStreamInitiators() throws Exception {
        // Since this component is a FiringPolicy.any, it is possible that when handleStreamInitiators() is
        // called, there could be non-StreamDelimiter data that has arrived on other ports which would be
        // lost if we don't call 'executeCallBack' (when handleStreamInitiators() gets called, executeCallBack is NOT called)

        executeCallBack(componentContext);
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        executeCallBack(componentContext);
    }
}
