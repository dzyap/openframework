package eu.ibagroup.easyrpa.openframework.email.service;

public class EmailConfigParam {

    public static final String OUTBOUND_EMAIL_SERVER = "outbound.email.server";

    public static final String OUTBOUND_EMAIL_PROTOCOL = "outbound.email.protocol";

    public static final String OUTBOUND_EMAIL_SECRET = "outbound.email.secret";

    public static final String INBOUND_EMAIL_SERVER = "inbound.email.server";

    public static final String INBOUND_EMAIL_PROTOCOL = "inbound.email.protocol";

    public static final String INBOUND_EMAIL_SECRET = "inbound.email.secret";

    public static final String MAILBOX_DEFAULT_FOLDER = "mailbox.default.folder";

    public static final String SENDER_TPL = "%s.sender";

    public static final String SENDER_NAME_TPL = "%s.sender.name";

    public static final String FROM_TPL = "%s.from";

    public static final String RECIPIENTS_TPL = "%s.recipients";

    public static final String CC_RECIPIENTS_TPL = "%s.cc.recipients";

    public static final String BCC_RECIPIENTS_TPL = "%s.bcc.recipients";

    public static final String REPLY_TO_TPL = "%s.reply.to";

    public static final String SUBJECT_TPL = "%s.subject";

    public static final String BODY_TEMPLATE_TPL = "%s.body.tpl";

    public static final String CHARSET_TPL = "%s.charset";

    private EmailConfigParam() {
    }
}