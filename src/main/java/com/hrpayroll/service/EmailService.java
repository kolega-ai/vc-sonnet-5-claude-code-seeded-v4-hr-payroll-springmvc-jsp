package com.hrpayroll.service;

import com.hrpayroll.domain.OutboxEmail;
import com.hrpayroll.repository.OutboxEmailRepository;
import org.springframework.stereotype.Service;

/** Local mock email provider: outbound mail is persisted to the outbox table instead of sent over SMTP. */
@Service
public class EmailService {

    private final OutboxEmailRepository outboxEmailRepository;

    public EmailService(OutboxEmailRepository outboxEmailRepository) {
        this.outboxEmailRepository = outboxEmailRepository;
    }

    public void send(String recipient, String subject, String body) {
        OutboxEmail email = new OutboxEmail();
        email.setRecipient(recipient);
        email.setSubject(subject);
        email.setBody(body);
        outboxEmailRepository.save(email);
    }
}
