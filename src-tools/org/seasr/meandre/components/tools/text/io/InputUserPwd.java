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

package org.seasr.meandre.components.tools.text.io;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This component allows a user to enter credentials that can be passed to other components",
        name = "Input User Password",
        tags = "#INPUT, credentials, username, password",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.7-dep.jar" },
        resources = { "InputUserPwd.vm" }
)
public class InputUserPwd extends GenericTemplate {

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "username",
            description = "The username" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_USERNAME = "username";

    @ComponentOutput(
            name = "password",
            description = "The password" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_PASSWORD = "password";

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "The template name",
	        name = GenericTemplate.PROP_TEMPLATE,
	        defaultValue = "org/seasr/meandre/components/tools/text/io/InputUserPwd.vm"
	)
    protected static final String PROP_TEMPLATE = GenericTemplate.PROP_TEMPLATE;

	@ComponentProperty(
            description = "Username prompt",
            name = "username_prompt",
            defaultValue = "Username"
    )
    protected static final String PROP_USERNAME_PROMPT = "username_prompt";

	@ComponentProperty(
            description = "Default username",
            name = "default_username",
            defaultValue = ""
    )
    protected static final String PROP_DEFAULT_USERNAME = "default_username";

	@ComponentProperty(
            description = "Password prompt",
            name = "password_prompt",
            defaultValue = "Password"
    )
    protected static final String PROP_PASSWORD_PROMPT = "password_prompt";

	@ComponentProperty(
            description = "Default password",
            name = "default_password",
            defaultValue = ""
    )
    protected static final String PROP_DEFAULT_PASSWORD = "default_password";

	@ComponentProperty(
            description = "Ask for username?",
            name = "ask_username",
            defaultValue = "true"
    )
    protected static final String PROP_ASK_USERNAME = "ask_username";

    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

	    context.put("username_prompt", getPropertyOrDieTrying(PROP_USERNAME_PROMPT, ccp));
	    context.put("default_username", getPropertyOrDieTrying(PROP_DEFAULT_USERNAME, false, false, ccp));
	    context.put("password_prompt", getPropertyOrDieTrying(PROP_PASSWORD_PROMPT, ccp));
	    context.put("default_password", getPropertyOrDieTrying(PROP_DEFAULT_PASSWORD, false, false, ccp));
	    context.put("ask_username", Boolean.parseBoolean(getPropertyOrDieTrying(PROP_ASK_USERNAME, ccp)));
	}

	@Override
	protected boolean processRequest(HttpServletRequest request) throws IOException {
	    try {
            String username = request.getParameter("username");
            if (username != null)
                componentContext.pushDataComponentToOutput(OUT_USERNAME, BasicDataTypesTools.stringToStrings(username));

            String password = request.getParameter("password");
            if (password != null)
                componentContext.pushDataComponentToOutput(OUT_PASSWORD, BasicDataTypesTools.stringToStrings(password));
        }
        catch (ComponentContextException e) {
            throw new IOException(e.toString());
        }

	    return true;
	}
}
