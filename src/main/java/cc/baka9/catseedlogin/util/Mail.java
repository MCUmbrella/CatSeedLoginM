package cc.baka9.catseedlogin.util;


import cc.baka9.catseedlogin.bukkit.Config;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class Mail {

    public static void sendMail(String receiveMailAccount, String subject, String content) throws Exception{

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", Config.EmailVerify.smtpHost);
        props.setProperty("mail.smtp.auth", "true");

        final String smtpPort = Config.EmailVerify.smtpPort;
        props.setProperty("mail.smtp.port", smtpPort);

        if (Config.EmailVerify.ssl) {
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.socketFactory.port", smtpPort);
        }

        String emailAccount = Config.EmailVerify.account;
        String emailPassword = Config.EmailVerify.password;

        Session session = Session.getInstance(props);

        session.setDebug(Config.EmailVerify.debug);

        // 创建邮件
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailAccount, Config.EmailVerify.from, "UTF-8"));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMailAccount, "", "UTF-8"));
        message.setSubject(subject, "UTF-8");
        message.setContent(content, Util.isOSLinux() ? "text/html; charset=UTF-8" : "text/html; charset=GBK");
        message.setSentDate(new Date());
        message.saveChanges();

        // 发送
        Transport transport = session.getTransport();
        transport.connect(emailAccount, emailPassword);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }
}
