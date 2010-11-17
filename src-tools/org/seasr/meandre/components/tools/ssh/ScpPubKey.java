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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

import com.sshtools.j2ssh.ScpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

@Component(
        creator = "Boris Capitanu",
        description = "Securely copies a file or directory from a local to a remote server or from a remote server to local",
        name = "SCP Public Key",
        tags = "scp, file",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

public class ScpPubKey extends AbstractExecutableComponent {
    
    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The user name to authenticate to the remote server",
            name = "username"
    )
    protected static final String IN_USERNAME = "username";
    
    @ComponentInput(
            description = "The private key for the user",
            name = "private_key"
    )
    protected static final String IN_PRIVKEY = "private_key";

    @ComponentInput(
            description = "The passphrase for the private key",
            name = "passphrase"
    )
    protected static final String IN_PASSPHRASE = "passphrase";
    
    @ComponentInput(
            description = "The source file or directory",
            name = "source"
    )
    protected static final String IN_SOURCE = "source";

    @ComponentInput(
            description = "The destination file or directory",
            name = "destination"
    )
    protected static final String IN_DESTINATION = "destination";
    
    @ComponentInput(
            description = "Recursive?",
            name = "recursive"
    )
    protected static final String IN_RECURSIVE = "recursive";

	//------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "Remote server port number",
	        name = Names.PROP_PORT_NUMBER,
	        defaultValue = "22"
	)
	protected static final String PROP_PORT_NUMBER = Names.PROP_PORT_NUMBER;

	//--------------------------------------------------------------------------------------------
	
	static final Pattern REGEXP = Pattern.compile("(.+):(.+)$");
	private int _port;
	
	//--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    _port = Integer.parseInt(getPropertyOrDieTrying(PROP_PORT_NUMBER, ccp));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String userName = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_USERNAME))[0];
        String passphrase = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_PASSPHRASE))[0];
        String source = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_SOURCE))[0];
        String destination = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_DESTINATION))[0];
        boolean recursive = Boolean.parseBoolean(DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_RECURSIVE))[0]);
        byte[] privKeyBytes = DataTypeParser.parseAsByteArray(cc.getDataComponentFromInput(IN_PRIVKEY));
        
        String hostName = null;
        boolean doGet;
        
        Matcher matcher = REGEXP.matcher(source);
        if (matcher.matches()) {
            hostName = matcher.group(1);
            source = matcher.group(2);
            doGet = true;
        } else {
            matcher = REGEXP.matcher(destination);
            if (matcher.matches()) {
                hostName = matcher.group(1);
                destination = matcher.group(2);
                doGet = false;
            } else
                throw new Exception("Cannot obtain hostname from source or destination.");
        }
        
        // construct connection
        console.fine("* Connecting to " + hostName);
        SshClient ssh = new SshClient();
        ssh.connect(hostName, _port, new IgnoreHostKeyVerification());

        // setting up authentication
        console.fine("* Setting up authentication");
        PublicKeyAuthenticationClient authClient = new PublicKeyAuthenticationClient();
        authClient.setUsername(userName);
        SshPrivateKeyFile privKey = SshPrivateKeyFile.parse(privKeyBytes);
        if (passphrase.length() == 0) passphrase = null;
        authClient.setKey(privKey.toPrivateKey(passphrase));
        
        // authenticating
        console.fine("* Authenticating");
        int result = ssh.authenticate(authClient);
        
        switch (result) {
            case AuthenticationProtocolState.COMPLETE:
                console.fine("* Copying from " + source + " to " + destination + "  (recursive: " + recursive + ")");
                ScpClient scpClient = ssh.openScpClient();
                if (doGet)
                    // source is remote, destination is local
                    scpClient.get(destination, source, recursive);
                else
                    // source is local, destination is remote
                    scpClient.put(source, destination, recursive);
                
                console.fine("* Done");
                break;
                
            default:
                console.warning(String.format("Authentication failed or incomplete (code=%d)", result));
                break;
        }
    }

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
