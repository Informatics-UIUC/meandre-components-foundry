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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.db.DBUtils;

import com.jolbox.bonecp.BoneCP;


/**
 * @author Boris Capitanu
 */

@Component(
        name = "Execute SQL",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.any,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "tools, database, sql",
        description = "This component executes SQL statements passed in the input",
        dependency = { "protobuf-java-2.2.0.jar", "sqlite-jdbc-3.7.2.jar",
                       "guava-r09.jar", "slf4j-api-1.6.1.jar", "slf4j-log4j12-1.6.1.jar" }
)
public class ExecuteSQL extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "db_conn_pool",
            description = "The DB connection pool used for providing / managing connections to the specified database" +
                "<br>TYPE: com.jolbox.bonecp.BoneCP"
    )
    protected static final String IN_DB_CONN_POOL = "db_conn_pool";

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The SQL statements to be executed" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "num_rows_updated_per_query",
            description = "A sequence of integers representing the number of rows updated by each SQL statement executed." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Integers"
    )
    protected static final String OUT_RESULT = "num_rows_updated_per_query";

    //--------------------------------------------------------------------------------------------


    protected BoneCP connectionPool = null;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        componentInputCache.storeIfAvailable(cc, IN_TEXT);

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

        if (connectionPool == null || !componentInputCache.hasData(IN_TEXT))
            // we're not ready to process yet, return
            return;

        Object input;
        while ((input = componentInputCache.retrieveNext(IN_TEXT)) != null) {
            if (input instanceof StreamDelimiter) {
                // No output to push...
                continue;
            }

            Connection connection = null;
            try {
                connection = connectionPool.getConnection();

                List<String> stmts = new ArrayList<String>();
                String sqlStatements = DataTypeParser.parseAsString(input)[0];
                for (String sql : sqlStatements.split("\n")) {
                	sql = sql.trim();
                	if (sql.startsWith("--") || sql.length() == 0)
                		continue;
                	if (sql.endsWith(";"))
                		stmts.add(sql);
                	else
                		console.warning(String.format("Ignoring malformed SQL statement '%s'", sql));
                }

                if (stmts.isEmpty()) continue;

                Statement stmt = null;
                try {
                    stmt = connection.createStatement();

                    for (String sql : stmts)
                        stmt.addBatch(sql);

                    long startTime = System.currentTimeMillis();
                    int[] results = stmt.executeBatch();
                    double elapsedSec = (System.currentTimeMillis() - startTime) / 1000f;

                    for (int i = 0, iMax = results.length; i < iMax; i++) {
                        if (results[i] == Statement.EXECUTE_FAILED)
                            console.warning("SQL EXECUTE_FAILED: " + stmts.get(i));
                        else {
                            if (results[i] >= 0) {
                                String report = String.format("(%,.2f seconds) %,d rows updated", elapsedSec, results[i]);
                                if (console.isLoggable(Level.FINER) || results[i] == 0)
                                    report += ": " + stmts.get(i);

                                if (console.isLoggable(Level.FINER))
                                    console.finer(report);
                                else
                                    console.fine(report);
                            } else
                                console.fine("SQL SUCCESS_NO_INFO: " + stmts.get(i));
                        }
                    }

                    cc.pushDataComponentToOutput(OUT_RESULT, BasicDataTypesTools.integerArrayToIntegers(results));
                }
                finally {
                    DBUtils.closeStatement(stmt);
                }
            }
            finally {
                DBUtils.releaseConnection(connection);
            }
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        connectionPool = null;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public void handleStreamInitiators() throws Exception {
        executeCallBack(componentContext);
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        executeCallBack(componentContext);
    }
}
