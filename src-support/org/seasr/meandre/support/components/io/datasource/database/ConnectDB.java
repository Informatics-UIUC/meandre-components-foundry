/* University of Illinois/NCSA
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

package org.seasr.meandre.support.components.io.datasource.database;

//import statements

import java.util.logging.Level;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.sql.Connection;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;

@Component(creator="Erik Johnson",
        description="<p>Overview:<br>"
        +"This component opens a connection object to a database."
        +" This component assumes that a JNDI context with datasources is already configured."
        +" It will open a connection to the database specified by the JNDI object at JNDIName.</p>",
        name="Connect to Database",
        tags="database, connect, JNDI, datasource, db",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

/** A component to close the DB connection passed to it.
 *
 * @author Erik Johnson
 * @author Lily Dong
 */
public class ConnectDB extends AbstractExecutableComponent {
	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			description = "Connection to Database",
	 		name = Names.PORT_CONNECTION)
	final static String OUT_CONNECTION = Names.PORT_CONNECTION;

	//------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(description = "JNDI Datasource URL",
			defaultValue = "",
			name = Names.PROP_JNDI_NAME)
	public final static String PROP_JNDI_NAME = Names.PROP_JNDI_NAME;

	//--------------------------------------------------------------------------------------------

	private Connection dbConn = null;
	private String jndiName="";
	private Context ctx;
	private DirContext dctx;

	//--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		jndiName = cc.getProperty(PROP_JNDI_NAME);
     	console.log(Level.INFO, "Attempting to connect to db "+jndiName);

     	ctx = null;
       	try {
			Hashtable<String,String> env = new Hashtable<String,String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
					//"org.mortbay.naming.InitialContextFactory");
			env.put(Context.PROVIDER_URL,
					"ldap://localhost:389");
					//"localhost:1099");

			env.put(Context.SECURITY_AUTHENTICATION,"simple");
			env.put(Context.SECURITY_PRINCIPAL,"cn=Directory Manager"); // specify the username
			env.put(Context.SECURITY_CREDENTIALS,"91234567");           // specify the password

			dctx = new InitialDirContext(env);
			DataSource ds = (DataSource)dctx.lookup("cn=myconnection,cn=anconnection");
			dbConn = (Connection)ds.getConnection("root", "91234567");

			//ctx = new InitialContext(env);
			//Assume Context already configured
			NamingEnumeration<NameClassPair> list = dctx.list( "" );//ctx.list( "" );

			while (list.hasMore()) {
				NameClassPair nc = (NameClassPair)list.next();
				console.info("jndiName = " + jndiName + " nc.getName() = " + nc.getName());
				/*if(nc.getName().equalsIgnoreCase(jndiName)){
					try{
						dbConn = (Connection)( (DataSource)( ctx.lookup(jndiName))).getConnection() ;
						dbConn.isReadOnly(); //.setAutoCommit(false);
					}
					catch (Exception exc)
					{
						console.log(Level.WARNING, "Could not open "+jndiName+" as Datasource, trying Pooled Datasource");
						try{
							dbConn = ((ConnectionPoolDataSource) ctx.lookup(jndiName)).getPooledConnection().getConnection();
							dbConn.isReadOnly(); //.setAutoCommit(false);
						}
						catch (Exception exc2)
						{
							console.log(Level.SEVERE, "Could not open "+jndiName+": "+exc);
						}
					}

				}*/
			}
       	} catch (Exception e){
       		e.printStackTrace();
       	}

       	//cc.pushDataComponentToOutput(OUT_CONNECTION, dbConn);
     }

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}

