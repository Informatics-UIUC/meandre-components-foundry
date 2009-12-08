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

package org.evernote.meandre.components;

import java.util.Iterator;
import java.util.List;

import org.meandre.components.abstracts.AbstractExecutableComponent;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;

import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.User;
import com.evernote.edam.type.Note;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.edam.userstore.UserStore;

@Component(creator="Lily Dong",
           description="Demonstrates how to implement " +
           "a interface to write text conforming to Evernote Markup Language" +
           "(ENML) to the owner's default note book under sandbox.evernote.com. " +
           "It should be noted that sandbox.evernote.com does not corporate with the api setting the default note book. " +
           ",so designating the default note book through sandbox.evernote.com before writting.",
           name="Write Evernote",
           tags="evernote, note, notebook, write",
           baseURL="meandre://seasr.org/components/foundry/",
           dependency = {"protobuf-java-2.2.0.jar"}
)

public class WriteEvernote extends AbstractExecutableComponent
{
	 //------------------------------ INPUTS ------------------------------------------------------
    @ComponentInput(description="The text to be written to server.",
    				name= Names.PORT_TEXT)
    public final static String IN_TEXT = Names.PORT_TEXT;


	//------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(defaultValue="",
                       description="This property sets the username.",
                       name=Names.PROP_USERNAME)
    final static String PROP_USERNAME = Names.PROP_USERNAME;

    @ComponentProperty(defaultValue="",
                       description="This property sets the password.",
                       name=Names.PROP_PASSWORD)
    final static String PROP_PASSWORD = Names.PROP_PASSWORD;

    @ComponentProperty(defaultValue="seasr",
                       description="This property sets the consumer key.",
                       name=Names.PROP_CONSUMER_KEY)
    final static String PROP_CONSUMER_KEY = Names.PROP_CONSUMER_KEY;

    @ComponentProperty(defaultValue="d38423353add320b",
                       description="This property sets the consumer secret.",
                       name=Names.PROP_CONSUMER_SECRET)
    final static String PROP_CONSUMER_SECERT = Names.PROP_CONSUMER_SECRET;

    @ComponentProperty(defaultValue="",
            		   description="This property sets the title.",
            		   name=Names.PROP_TITLE)
    final static String PROP_TITLE = Names.PROP_TITLE;

    static final String userStoreUrl = "https://sandbox.evernote.com/edam/user";
    static final String noteStoreUrlBase = "http://sandbox.evernote.com/edam/note/";

    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
        String username = cc.getProperty(PROP_USERNAME),
               password = cc.getProperty(PROP_PASSWORD);

        String consumerKey = cc.getProperty(PROP_CONSUMER_KEY),
        	   consumerSecret = cc.getProperty(PROP_CONSUMER_SECERT);

        String title = cc.getProperty(PROP_TITLE);

        String text = DataTypeParser.parseAsString(
    			cc.getDataComponentFromInput(IN_TEXT))[0];

        try {
        	THttpClient userStoreTrans = new THttpClient(userStoreUrl);
        	TBinaryProtocol userStoreProt = new TBinaryProtocol(userStoreTrans);
        	UserStore.Client userStore = new UserStore.Client(
        			userStoreProt, userStoreProt);
        	boolean versionOk = userStore.checkVersion(
        			"Evernote's Local Client API Usage",
        			com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
        	        com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
        	if (!versionOk) {
        	      throw new Exception("Incomatible EDAM client protocol version");
        	}

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
        } catch(Exception e) {
            throw new ComponentExecutionException(e);
        }
    }

    /**
     * Call at the end of an execution flow.
     */
    public void initializeCallBack(ComponentContextProperties ccp)
    throws Exception {
    }

    /**
     * Called when a flow is started.
     */
    public void disposeCallBack(ComponentContextProperties ccp)
    throws Exception {
    }
}

