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

import java.io.BufferedReader;
import java.io.FileReader;
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
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

@Component(
        creator = "Lily Dong",
        description = "Executes a command based on SSH and returns result.",
        name = "SSH Public Key",
        tags = "ssh, command",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

public class SshPubKey extends AbstractExecutableComponent {
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
	        description = "Passphrase.",
	        name = Names.PROP_PASSPHRASE,
	        defaultValue = ""
	)
	protected static final String PROP_PASSPHRASE = Names.PROP_PASSPHRASE;

	@ComponentProperty(
	        description = "Private key file.",
	        name = Names.PROP_FILENAME,
	        defaultValue = ""
	)
	protected static final String PROP_FILE_NAME = Names.PROP_FILENAME;

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
		console.info("initialize");
		String hostname   = cc.getProperty(PROP_HOSTNAME);
		String portnumber = cc.getProperty(PROP_PORT_NUMBER);
		String username   = cc.getProperty(PROP_USERNAME);
		String passphrase = cc.getProperty(PROP_PASSPHRASE);
		String filename   = cc.getProperty(PROP_FILE_NAME);
		String command    = cc.getProperty(PROP_COMMAND);

		//construct connection
		console.info("construct connection");
		SshClient ssh = new SshClient();
		ssh.connect(hostname,
					Integer.parseInt(portnumber),
					new IgnoreHostKeyVerification());

		//authentication
		console.info("authenticate");
		PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();
		pk.setUsername(username);

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuffer buf = new StringBuffer();
		String line;
		while((line=reader.readLine())!=null)
			buf.append(line).append("\n");
		String privateKey = buf.toString();

		SshPrivateKeyFile spkf = SshPrivateKeyFile.parse(
				privateKey.getBytes());
		SshPrivateKey key = spkf.toPrivateKey(passphrase);
        pk.setKey(key);

        int result = ssh.authenticate(pk);
        if(result == AuthenticationProtocolState.COMPLETE) { // Authentication complete
			console.info("The authentication is complete");

			if(command!=null && command.length()!=0) {
				SessionChannelClient session = ssh.openSessionChannel();
				session.executeCommand(command+"\n");
				InputStream in = session.getInputStream();
				byte buffer[] = new byte[255];
				int read;
				buf.delete(0, buf.length());
				while((read = in.read(buffer)) > 0) {
					String out = new String(buffer, 0, read);
					buf.append(out).append("\n");
					//console.info(out);
				}
				session.close();
				console.info(buf.toString());
				cc.pushDataComponentToOutput(
						OUT_TEXT, 
						BasicDataTypesTools.stringToStrings(buf.toString()));
			}
        }
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
