from org.seasr.meandre.components.tools.io.SendEmail import IN_BODY_TEXT, IN_SUBJECT, IN_TO, IN_FROM, PROP_SMTP_SERVER
from org.seasr.meandre.components.abstracts import AbstractExecutableComponent
from org.seasr.datatypes.core import DataTypeParser
from org.seasr.datatypes.core import BasicDataTypesTools
from email.mime.text import MIMEText
import smtplib

class SendEmail(AbstractExecutableComponent):

    def initializeCallBack(self, ccp):
        self.smtp_server = self.getPropertyOrDieTrying(PROP_SMTP_SERVER, ccp)

    def executeCallBack(self, cc):
        in_body_text = cc.getDataComponentFromInput(IN_BODY_TEXT)
        in_subject = cc.getDataComponentFromInput(IN_SUBJECT)
        in_to = cc.getDataComponentFromInput(IN_TO)
        in_from = cc.getDataComponentFromInput(IN_FROM)

        email_body_text = str(DataTypeParser.parseAsString(in_body_text)[0])
        email_subject = str(DataTypeParser.parseAsString(in_subject)[0])
        email_to_list = str(DataTypeParser.parseAsString(in_to)[0]).replace(' ', '').split(',')
        email_from = str(DataTypeParser.parseAsString(in_from)[0])

        self.console.fine('Sending email to %s with subject %s' % (email_to_list, email_subject))

        msg = MIMEText(email_body_text)
        msg['Subject'] = email_subject
        msg['From'] = email_from
        msg['To'] = ', '.join(email_to_list)

        smtp = smtplib.SMTP(self.smtp_server)
        smtp.sendmail(email_from, email_to_list, msg.as_string())
        smtp.quit()

    def disposeCallBack(self, cc):
        pass