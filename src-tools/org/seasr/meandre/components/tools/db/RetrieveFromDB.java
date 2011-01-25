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
import java.util.LinkedList;
import java.util.Queue;
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
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.support.generic.io.Serializer;
import org.seasr.meandre.support.generic.io.Serializer.SerializationFormat;
import org.seasr.meandre.support.generic.util.UUIDUtils;

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
        tags = "tools, database, db, persistence",
        description = "This component retrieves persisted data from a database",
        dependency = {"protobuf-java-2.2.0.jar", "guava-r06.jar", "slf4j-api-1.6.1.jar", "slf4j-log4j12-1.6.1.jar"}
)
public class RetrieveFromDB extends AbstractDBComponent {

    //------------------------------ INPUTS -----------------------------------------------------

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

    protected String _dbTable;
    protected Queue<UUID> _inputQueue = new LinkedList<UUID>();

    protected String _sqlQueryMeta;
    protected String _sqlQueryData;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _dbTable = getPropertyOrDieTrying(PROP_TABLE, ccp);
        _sqlQueryMeta = String.format(
                "SELECT table_name, streaming FROM %s WHERE uuid = ?", PERSISTENCE_META_TABLE_NAME);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        super.executeCallBack(cc);

        if (cc.isInputAvailable(IN_ID)) {
            for (String input : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_ID)))
                _inputQueue.offer(UUID.fromString(input));
        }

        if (connectionPool == null || _inputQueue.isEmpty())
            // we're not ready to process yet, return
            return;

        Connection connection = null;
        PreparedStatement psMeta = null;
        PreparedStatement psData = null;
        try {
            connection = connectionPool.getConnection();
            psMeta = connection.prepareStatement(_sqlQueryMeta);

            UUID input;
            while ((input = _inputQueue.poll()) != null) {
                BigDecimal uuid = new BigDecimal(UUIDUtils.toBigInteger(input));
                psMeta.setBigDecimal(1, uuid);
                ResultSet rs = psMeta.executeQuery();

                if (rs.next()) {
                    String tableName = rs.getString("table_name");
                    boolean isStreaming = rs.getBoolean("streaming");

                    if (isStreaming)
                        cc.pushDataComponentToOutput(OUT_DATA, new StreamInitiator());

                    String sqlQueryData = String.format(
                            "SELECT data, type, serializer FROM %s WHERE uuid = ? ORDER BY seq_no ASC", tableName);
                    psData = connection.prepareStatement(sqlQueryData);
                    psData.setBigDecimal(1, uuid);
                    rs = psData.executeQuery();

                    while (rs.next()) {
                        InputStream dataStream = null;
                        try {
                            dataStream = rs.getBinaryStream("data");
                            String type = rs.getString("type");
                            String serializer = rs.getString("serializer");

                            console.finer(String.format("from db: type='%s', serializer='%s'", type, serializer));

                            Class<?> clazz = Class.forName(type);
                            SerializationFormat format = SerializationFormat.valueOf(serializer);
                            if (format == null)
                                outputError(String.format("Unsupported serializer format '%s' for id '%s'!", serializer, input), Level.WARNING);
                            else {
                                Object obj = Serializer.deserializeObject(dataStream, clazz, format);
                                cc.pushDataComponentToOutput(OUT_DATA, obj);
                            }
                        }
                        finally {
                            if (dataStream != null)
                                dataStream.close();
                        }
                    }

                    if (isStreaming)
                        cc.pushDataComponentToOutput(OUT_DATA, new StreamTerminator());
                } else
                    outputError(String.format("Could not locate data for id '%s'!", input), Level.WARNING);
            }
        }
        finally {
            releaseConnection(connection, psMeta, psData);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        super.disposeCallBack(ccp);
    }
}
