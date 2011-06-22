package org.seasr.meandre.components.tools.io;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.seasr.meandre.components.abstracts.python.AbstractPythonExecutableComponent;

@Component(
        name = "Send Email",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "smtp, email",
        description = "This component can send an email to an address or a list of addresses specified as an input.",
        dependency = { "protobuf-java-2.2.0.jar", "sendemail.py.jar" }
)
public class SendEmail extends AbstractPythonExecutableComponent {

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

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "smtp_server",
            description = "The SMTP server to use",
            defaultValue = ""
    )
    public static final String PROP_SMTP_SERVER = "smtp_server";
}
