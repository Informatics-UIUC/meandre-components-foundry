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

import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * @author Boris Capitanu
 */

public abstract class AbstractDBComponent extends AbstractExecutableComponent {

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

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "hostDB",
            description = "host/database eg. localhost/testDB",
            defaultValue = ""
    )
    protected static final String PROP_DB = "hostDB";

    @ComponentProperty(
            name = "protocol",
            description = "jdbc protocol for hostURL",
            defaultValue = "jdbc:mysql://"
    )
    protected static final String PROP_PROTOCOL = "protocol";

    @ComponentProperty(
            name = "JDBCDriver",
            description = "jdbc driver to be loaded, jar must be in classpath",
            defaultValue = "com.mysql.jdbc.Driver"
    )
    protected static final String PROP_JDBC = "JDBCDriver";

    //--------------------------------------------------------------------------------------------


    protected BoneCP connectionPool = null;
    private BoneCPConfig config = null;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        String protocol = getPropertyOrDieTrying(PROP_PROTOCOL, ccp);
        String hostDB = getPropertyOrDieTrying(PROP_DB, ccp);
        String JDBC_DRIVER = getPropertyOrDieTrying(PROP_JDBC, ccp);
        String fullURL = protocol + hostDB;

        console.fine("Connecting using " + fullURL);

        // This will load the DB driver, each DB has its own driver
        try {
            Class.forName(JDBC_DRIVER);
        }
        catch (Exception e) {
            console.log(Level.SEVERE, "Could not load the JDBC driver: " + JDBC_DRIVER, e);
            throw e;
        }

        // partially setup the connection pool (still need username/pw)
        config = new BoneCPConfig();
        config.setJdbcUrl(fullURL); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
        config.setMinConnectionsPerPartition(5);
        config.setMaxConnectionsPerPartition(10);
        config.setPartitionCount(1);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (connectionPool == null) {
            if (cc.isInputAvailable(IN_USERNAME))
                config.setUsername(DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_USERNAME))[0]);

            if (cc.isInputAvailable(IN_PASSWORD))
                config.setPassword(DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_PASSWORD))[0]);

            if (config.getUsername() != null && config.getPassword() != null) {
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
                    if (conn != null)
                        try {
                            conn.close();
                        }
                    catch (SQLException e) { }
                }
            }
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        try {
            if (connectionPool != null) {
                connectionPool.shutdown(); // shutdown connection pool.
                connectionPool = null;
            }
        }
        catch (Exception e) { }
    }
}
