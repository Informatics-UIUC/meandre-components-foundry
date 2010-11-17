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

package org.seasr.meandre.components.tools.ssh;

import java.io.InputStream;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

@Component(
        creator = "Lily Dong",
        description = "Executes a command based on SSH and returns result.",
        name = "SSH",
        tags = "ssh, command",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

public class Ssh extends AbstractExecutableComponent {
	//------------------------------ OUTPUTS -----------------------------------------------------
	@ComponentOutput(
			description = "The return value of command." +
			"<br><br>TYPE: org.seasr.datatypes.BasicDataTypes.String",
	        name = Names.PORT_TEXT
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------
    @ComponentProperty(
	        description = "Hostname.",
            name = Names.PROP_HOSTNAME,
            defaultValue = ""
	)
	protected static final String PROP_HOSTNAME = Names.PROP_HOSTNAME;

    @ComponentProperty(
	        description = "Port number.",
            name = Names.PROP_PORT_NUMBER,
            defaultValue = "22"
	)
	protected static final String PROP_PORT_NUMBER = Names.PROP_PORT_NUMBER;

    @ComponentProperty(
	        description = "Username.",
            name = Names.PROP_USERNAME,
            defaultValue = ""
	)
	protected static final String PROP_USERNAME = Names.PROP_USERNAME;

    @ComponentProperty(
	        description = "Password.",
            name = Names.PROP_PASSWORD,
            defaultValue = ""
	)
	protected static final String PROP_PASSWORD = Names.PROP_PASSWORD;

    @ComponentProperty(
	        description = "Command to execute.",
            name = Names.PROP_COMMAND,
            defaultValue = "ls"
	)
	protected static final String PROP_COMMAND = Names.PROP_COMMAND;

    //--------------------------------------------------------------------------------------------
	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		//initialize
		String hostname   = cc.getProperty(PROP_HOSTNAME);
		String portnumber = cc.getProperty(PROP_PORT_NUMBER);
		String username   = cc.getProperty(PROP_USERNAME);
		String password   = cc.getProperty(PROP_PASSWORD);
		String command    = cc.getProperty(PROP_COMMAND);

		//construct connection
		SshClient ssh = new SshClient();
		ssh.connect(hostname,
					Integer.parseInt(portnumber),
					new IgnoreHostKeyVerification());

		PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
		pwd.setUsername(username);
		pwd.setPassword(password); // Authenticate the user
		int result = ssh.authenticate(pwd);

		if(result == AuthenticationProtocolState.COMPLETE) {
			console.info("The authentication is complete");

			SessionChannelClient session = ssh.openSessionChannel();

			session.executeCommand(command+"\n");

			InputStream in = session.getInputStream();
			byte buffer[] = new byte[255];
			int read;
			StringBuffer buf = new StringBuffer();
			while((read = in.read(buffer)) > 0) {
   				String out = new String(buffer, 0, read);
   				buf.append(out).append("\n");
			}
			session.close();
			console.info(buf.toString());
			cc.pushDataComponentToOutput(
					OUT_TEXT,
					BasicDataTypesTools.stringToStrings(buf.toString()));
		}
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
