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
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.tools.Names;

import com.sshtools.j2ssh.ScpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

@Component(
        creator = "Lily Dong",
        description = "Executes SCP get or put command. " +
        "Get or put command needs local file name and remote file name as input parameters, " +
        "If any parameter is null, the relevand command is unexecutable.",
        name = "SCP Wrapper",
        tags = "SCP, get, put",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

public class ScpWrapper extends AbstractExecutableComponent {
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
	protected static final String PROP_FILENAME = Names.PROP_FILENAME;

	@ComponentProperty(
	        description = "Lcal file for get command.",
	        name = Names.PROP_LOCAL_FILENAME_GET,
	        defaultValue = ""
	)
	protected static final String PROP_LOCAL_FILENAME_GET = Names.PROP_LOCAL_FILENAME_GET;

	@ComponentProperty(
	        description = "Remote file for get command.",
	        name = Names.PROP_REMOTE_FILENAME_GET,
	        defaultValue = ""
	)
	protected static final String PROP_REMOTE_FILENAME_GET = Names.PROP_REMOTE_FILENAME_GET;

	@ComponentProperty(
	        description = "Lcal file for put command.",
	        name = Names.PROP_LOCAL_FILENAME_PUT,
	        defaultValue = ""
	)
	protected static final String PROP_LOCAL_FILENAME_PUT = Names.PROP_LOCAL_FILENAME_PUT;

	@ComponentProperty(
	        description = "Remote file for PUT command.",
	        name = Names.PROP_REMOTE_FILENAME_PUT,
	        defaultValue = ""
	)
	protected static final String PROP_REMOTE_FILENAME_PUT = Names.PROP_REMOTE_FILENAME_PUT;
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
		String filename   = cc.getProperty(PROP_FILENAME);

		String localFileForGet = cc.getProperty(PROP_LOCAL_FILENAME_GET);
		String remoteFileForGet = cc.getProperty(PROP_REMOTE_FILENAME_GET);

		String localFileForPut = cc.getProperty(PROP_LOCAL_FILENAME_PUT);
		String remoteFileForPut = cc.getProperty(PROP_REMOTE_FILENAME_PUT);


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
			ScpClient scpClient = ssh.openScpClient();

			//local, remote(local is copied into remote)
			if((localFileForPut!=null&&localFileForPut.length()!=0) &&
			   (remoteFileForPut!=null&&remoteFileForPut.length()!=0))
				scpClient.put(localFileForPut, remoteFileForPut, false);
				/*scpClient.put(
					"E:/Limin/code/j2ssh/file1.txt",
					"/home/demo/.ssh/file1.txt",
					false);*/

			//local, remote(remote is copied into local)
			if((localFileForGet!=null&&localFileForGet.length()!=0) &&
			   (remoteFileForGet!=null&&remoteFileForGet.length()!=0))
				scpClient.get(localFileForGet, remoteFileForGet, false);
				/*scpClient.get(
					"E:/Limin/code/j2ssh/authorized_keys",
					"/home/demo/.ssh/authorized_keys",
					false);*/
        }
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
