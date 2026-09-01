package org.tbee.dancewithme.infrastructure.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/// JavaMailSender that captures sent mails instead of actually sending them, so tests can assert on them.
/// Registered as a bean in InfraTestBase, so the whole test suite shares a single Spring context.
public class MailSenderStub implements JavaMailSender {

    public static final List<MimeMessage> sentMimeMessages = new CopyOnWriteArrayList<>();
    public static final List<SimpleMailMessage> sentSimpleMessages = new CopyOnWriteArrayList<>();

    @Override
    public MimeMessage createMimeMessage() {
        return new MimeMessage((Session) null);
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) {
        try {
            return new MimeMessage(null, contentStream);
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(MimeMessage mimeMessage) {
        sentMimeMessages.add(mimeMessage);
    }

    @Override
    public void send(MimeMessage... mimeMessages) {
        sentMimeMessages.addAll(List.of(mimeMessages));
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) {
        try {
            MimeMessage mimeMessage = createMimeMessage();
            mimeMessagePreparator.prepare(mimeMessage);
            send(mimeMessage);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) {
        for (MimeMessagePreparator mimeMessagePreparator : mimeMessagePreparators) {
            send(mimeMessagePreparator);
        }
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) {
        sentSimpleMessages.add(simpleMessage);
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) {
        sentSimpleMessages.addAll(List.of(simpleMessages));
    }
}
