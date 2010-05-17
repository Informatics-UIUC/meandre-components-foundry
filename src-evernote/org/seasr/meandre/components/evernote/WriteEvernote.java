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

package org.seasr.meandre.components.evernote;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;

import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.edam.userstore.UserStore;

/**
 *
 * @author Lily Dong
 *
 */

@Component(
        creator = "Lily Dong",
        description = "Demonstrates how to implement " +
           "a interface to write text conforming to Evernote Markup Language" +
           "(ENML) to the owner's default note book under sandbox.evernote.com. " +
           "It should be noted that sandbox.evernote.com does not corporate with the api setting the default note book. " +
           ",so designating the default note book through sandbox.evernote.com before writting.",
        name = "Write Evernote",
        tags = "evernote, note, notebook, write",
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class WriteEvernote extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name= Names.PORT_TEXT,
            description = "The text to be written to server." +
        		 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

	//------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_USERNAME,
            description = "This property sets the username.",
            defaultValue = ""
    )
    protected static final String PROP_USERNAME = Names.PROP_USERNAME;

    @ComponentProperty(
            name = Names.PROP_PASSWORD,
            description = "This property sets the password.",
            defaultValue = ""
    )
    protected static final String PROP_PASSWORD = Names.PROP_PASSWORD;

    @ComponentProperty(
            name = Names.PROP_CONSUMER_KEY,
            description = "This property sets the consumer key.",
            defaultValue = "seasr"
    )
    protected static final String PROP_CONSUMER_KEY = Names.PROP_CONSUMER_KEY;

    @ComponentProperty(
            name = Names.PROP_CONSUMER_SECRET,
            description = "This property sets the consumer secret.",
            defaultValue = "d38423353add320b"
    )
    protected static final String PROP_CONSUMER_SECERT = Names.PROP_CONSUMER_SECRET;

    @ComponentProperty(
            name = Names.PROP_TITLE,
            description = "This property sets the title.",
            defaultValue = ""
    )
    protected static final String PROP_TITLE = Names.PROP_TITLE;

    //--------------------------------------------------------------------------------------------


    static final String userStoreUrl = "https://sandbox.evernote.com/edam/user";
    static final String noteStoreUrlBase = "http://sandbox.evernote.com/edam/note/";

    private String username, password;
    private String consumerKey, consumerSecret;
    private String title;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        username = ccp.getProperty(PROP_USERNAME);
        password = ccp.getProperty(PROP_PASSWORD);

        consumerKey = ccp.getProperty(PROP_CONSUMER_KEY);
        consumerSecret = ccp.getProperty(PROP_CONSUMER_SECERT);

        title = ccp.getProperty(PROP_TITLE);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];

        THttpClient userStoreTrans = new THttpClient(userStoreUrl);
        TBinaryProtocol userStoreProt = new TBinaryProtocol(userStoreTrans);
        UserStore.Client userStore = new UserStore.Client(
                userStoreProt, userStoreProt);

        boolean versionOk = userStore.checkVersion(
                "Evernote's Local Client API Usage",
                com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);

        if (!versionOk)
            throw new Exception("Incompatible EDAM client protocol version");

        AuthenticationResult authResult = userStore.authenticate(
                username, password, consumerKey, consumerSecret);
        User user = authResult.getUser();
        String authToken = authResult.getAuthenticationToken();

        String noteStoreUrl = noteStoreUrlBase + user.getShardId();
        THttpClient noteStoreTrans = new THttpClient(noteStoreUrl);
        TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
        NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt,
                noteStoreProt);

        Note note = new Note();
        note.setContent(text);
        note.setTitle(title);

        noteStore.createNote(authToken, note); //write to the default book
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}

