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

package org.seasr.meandre.components.tools.io;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Send Email",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#OUTPUT, smtp, email",
        description = "This component can send an email to an address or a list of addresses specified as an input.",
        dependency = { "protobuf-java-2.2.0.jar", "mail.jar" }
)
public class JavaSendEmail extends AbstractExecutableComponent {

    // ------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "body_text",
            description = "The body of the email" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    public static final String IN_BODY_TEXT = "body_text";

    @ComponentInput(
            name = "subject",
            description = "The subject of the email" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    public static final String IN_SUBJECT = "subject";

    @ComponentInput(
            name = "email_to",
            description = "The (comma separated) list of email addresses to send the email to" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    public static final String IN_TO = "email_to";

    @ComponentInput(
            name = "email_from",
            description = "The email address to use as FROM address" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    public static final String IN_FROM = "email_from";

    // ------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The message body" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "smtp_server",
            description = "The SMTP server to use",
            defaultValue = ""
    )
    public static final String PROP_SMTP_SERVER = "smtp_server";

    @ComponentProperty(
            name = "format",
            description = "The message format. One of text/html or text/plain",
            defaultValue = "text/html"
    )
    public static final String PROP_FORMAT = "format";

    //--------------------------------------------------------------------------------------------


    protected String _smtpServer;
    protected String _format;


    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		_smtpServer = getPropertyOrDieTrying(PROP_SMTP_SERVER, ccp);
		_format = getPropertyOrDieTrying(PROP_FORMAT, ccp);
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		String inFrom = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_FROM))[0];
		String inTo = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TO))[0];
		String inSubject = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_SUBJECT))[0];
		String inBody = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_BODY_TEXT))[0];

		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", _smtpServer);

		Session session = Session.getDefaultInstance(properties);

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(inFrom));

		for (String to : inTo.split(","))
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));

		message.setSubject(inSubject);
		message.setContent(inBody, _format);

		// Send message
		Transport.send(message);

		cc.pushDataComponentToOutput(OUT_TEXT, inBody);
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
