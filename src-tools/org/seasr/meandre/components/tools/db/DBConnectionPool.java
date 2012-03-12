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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

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
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.db.DBUtils;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "DB Connection Pool",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#INPUT, tools, database, db",
        description = "This component persists (serializes) the input into a database",
        dependency = { "protobuf-java-2.2.0.jar", "sqlite-jdbc-3.7.2.jar",
                       "guava-r09.jar", "slf4j-api-1.6.1.jar", "slf4j-log4j12-1.6.1.jar" }
)
public class DBConnectionPool extends AbstractExecutableComponent {

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
            name = "username",
            description = "The user name to authenticate to the DB server" +
                          "<br>TYPE: java.lang.String" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                          "<br>TYPE: byte[]" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_USERNAME = "username";

    @ComponentInput(
            name = "password",
            description = "The password" +
                          "<br>TYPE: java.lang.String" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                          "<br>TYPE: byte[]" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_PASSWORD = "password";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "db_conn_pool",
            description = "The DB connection pool used for providing / managing connections to the specified database" +
                "<br>TYPE: com.jolbox.bonecp.BoneCP"
    )
    protected static final String OUT_DB_CONN_POOL = "db_conn_pool";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "db_url",
            description = "The url of the database connection. (example: jdbc:mysql://localhost/myDB)",
            defaultValue = "jdbc:mysql://"
    )
    protected static final String PROP_DB_URL = "db_url";

    @ComponentProperty(
            name = "db_driver",
            description = "jdbc driver to be loaded, jar must be in classpath",
            defaultValue = "com.mysql.jdbc.Driver"
    )
    protected static final String PROP_DB_DRIVER = "db_driver";

    @ComponentProperty(
            name = "partition_count",
            description = "In order to reduce lock contention and thus improve performance, " +
            		"each incoming connection request picks off a connection from a pool that has thread-affinity, " +
            		"i.e. pool[threadId % partition_count]. The higher this number, the better your performance " +
            		"will be for the case when you have plenty of short-lived threads. Beyond a certain threshold, " +
            		"maintenence of these pools will start to have a negative effect on performance (and only for " +
            		"the case when connections on a partition start running out)",
            defaultValue = "2"
    )
    protected static final String PROP_PARTITION_COUNT = "partition_count";

    @ComponentProperty(
            name = "min_conn_per_partition",
            description = "The number of connections to start off with per partition",
            defaultValue = "2"
    )
    protected static final String PROP_MIN_CONN_PER_PART = "min_conn_per_partition";

    @ComponentProperty(
            name = "max_conn_per_partition",
            description = "The number of connections to create per partition. Setting this to 5 with 3 partitions " +
            		"means you will have 15 unique connections to the database. Note that BoneCP will not create " +
            		"all these connections in one go but rather start off with minConnectionsPerPartition and " +
            		"gradually increase connections as required",
            defaultValue = "10"
    )
    protected static final String PROP_MAX_CONN_PER_PART = "max_conn_per_partition";

    //--------------------------------------------------------------------------------------------


    protected BoneCP connectionPool = null;
    private BoneCPConfig config = null;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        String dbDriver = getPropertyOrDieTrying(PROP_DB_DRIVER, ccp);
        String dbURL = getPropertyOrDieTrying(PROP_DB_URL, ccp);
        if (dbURL.equals("jdbc:mysql://"))
            throw new ComponentContextException("The value of the property db_url is invalid. Forgot to set?");

        console.fine(String.format("Using %s to connect to %s ", dbDriver, dbURL));

        // This will load the DB driver, each DB has its own driver
        try {
            Class.forName(dbDriver);
        }
        catch (Exception e) {
            console.log(Level.SEVERE, "Could not load the JDBC driver: " + dbDriver, e);
            throw e;
        }

        connectionPool = null;

        int partitionCount = Integer.parseInt(getPropertyOrDieTrying(PROP_PARTITION_COUNT, ccp));
        int minConnPerPart = Integer.parseInt(getPropertyOrDieTrying(PROP_MIN_CONN_PER_PART, ccp));
        int maxConnPerPart = Integer.parseInt(getPropertyOrDieTrying(PROP_MAX_CONN_PER_PART, ccp));

        // partially setup the connection pool (still need username/pw)
        config = new BoneCPConfig();
        config.setJdbcUrl(dbURL); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
        config.setMinConnectionsPerPartition(minConnPerPart);
        config.setMaxConnectionsPerPartition(maxConnPerPart);
        config.setPartitionCount(partitionCount);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (connectionPool != null) {
            console.warning("Connection pool already created. Cannot re-use this component instance to create a new connection pool!");
            return;
        }

        config.setUsername(DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_USERNAME))[0]);
        config.setPassword(DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_PASSWORD))[0]);

        connectionPool = new BoneCP(config);

        // test the connection to make sure it works before proceeding
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
        }
        catch (SQLException e) {
            console.log(Level.SEVERE, "Database connection error", e);
            throw e;
        }
        finally {
            DBUtils.releaseConnection(conn);
        }

        cc.pushDataComponentToOutput(OUT_DB_CONN_POOL, connectionPool);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        shutdownConnectionPool();

        connectionPool = null;
        config = null;
    }

    //--------------------------------------------------------------------------------------------

    protected void shutdownConnectionPool() {
        if (connectionPool != null)
            connectionPool.shutdown();
    }
}
