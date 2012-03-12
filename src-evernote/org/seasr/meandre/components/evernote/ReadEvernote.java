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

import java.util.List;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
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
        description = "Demomstrates how to write a interface " +
            "to read all of the notes under owner's account at sandbox.evernote.com, " +
            "and outputs note's title and note's content as XML text. " +
            "It should be pointed out that sandbox.evernote.com is only a test-only server.",
        name = "Read Evernote",
        tags = "#INPUT, evernote, note, notebook, read",
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class ReadEvernote extends AbstractStreamingExecutableComponent {

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TITLE,
            description = "Outputs note's title." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TITLE = Names.PORT_TITLE;

    @ComponentOutput(
            name = Names.PORT_XML,
            description = "Output note's content as XML text." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_XML = Names.PORT_XML;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            defaultValue = "",
            description = "This property sets the username.",
            name = Names.PROP_USERNAME
    )
    protected static final String PROP_USERNAME = Names.PROP_USERNAME;

    @ComponentProperty(
            defaultValue = "",
            description = "This property sets the password.",
            name = Names.PROP_PASSWORD
    )
    protected static final String PROP_PASSWORD = Names.PROP_PASSWORD;

    @ComponentProperty(
            defaultValue = "seasr",
            description = "This property sets the consumer key.",
            name = Names.PROP_CONSUMER_KEY
    )
    protected static final String PROP_CONSUMER_KEY = Names.PROP_CONSUMER_KEY;

    @ComponentProperty(
            defaultValue = "d38423353add320b",
            description = "This property sets the consumer secret.",
            name = Names.PROP_CONSUMER_SECRET
    )
    protected static final String PROP_CONSUMER_SECRET = Names.PROP_CONSUMER_SECRET;

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Wrap output as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

    //--------------------------------------------------------------------------------------------


    protected static final String userStoreUrl = "https://sandbox.evernote.com/edam/user";
    protected static final String noteStoreUrlBase = "http://sandbox.evernote.com/edam/note/";

    private String username, password;
    private String consumerKey, consumerSecret;
    private boolean wrapStream;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        username = getPropertyOrDieTrying(PROP_USERNAME, ccp);
        password = getPropertyOrDieTrying(PROP_PASSWORD, false, true, ccp);

        consumerKey = getPropertyOrDieTrying(PROP_CONSUMER_KEY, ccp);
        consumerSecret = getPropertyOrDieTrying(PROP_CONSUMER_SECRET, ccp);

        wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	THttpClient userStoreTrans = new THttpClient(userStoreUrl);
    	TBinaryProtocol userStoreProt = new TBinaryProtocol(userStoreTrans);
    	UserStore.Client userStore = new UserStore.Client(userStoreProt, userStoreProt);

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
    	NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);

    	List<Notebook> notebooks = noteStore.listNotebooks(authToken);

    	if (wrapStream) {
    	    StreamInitiator si = new StreamInitiator(streamId);
    	    cc.pushDataComponentToOutput(OUT_TITLE, si);
    	    cc.pushDataComponentToOutput(OUT_XML, si);
    	}

    	for (Notebook notebook : notebooks) {
    	    console.info("Notebook: " + notebook.getName());
    	    NoteFilter filter = new NoteFilter();
    	    filter.setNotebookGuid(notebook.getGuid());
    	    NoteList noteList = noteStore.findNotes(authToken, filter, 0, 100);
    	    List<Note> notes = noteList.getNotes();

    	    for (Note note : notes) {
    	        cc.pushDataComponentToOutput(OUT_TITLE, BasicDataTypesTools.stringToStrings(note.getTitle()));
    	        cc.pushDataComponentToOutput(OUT_XML, BasicDataTypesTools.stringToStrings(noteStore.getNoteContent(authToken, note.getGuid())));
    	    }
    	}

        if (wrapStream) {
            StreamTerminator st = new StreamTerminator(streamId);
            cc.pushDataComponentToOutput(OUT_TITLE, st);
            cc.pushDataComponentToOutput(OUT_XML, st);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }
}

