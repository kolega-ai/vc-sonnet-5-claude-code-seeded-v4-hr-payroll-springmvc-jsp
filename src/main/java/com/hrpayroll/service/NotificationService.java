package com.hrpayroll.service;

import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.Notification;
import com.hrpayroll.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notify(Employee recipient, String message, String link) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setMessage(message);
        n.setLink(link);
        notificationRepository.save(n);
    }

    public List<Notification> forEmployee(Long employeeId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(employeeId);
    }

    public long unreadCount(Long employeeId) {
        return notificationRepository.countByRecipientIdAndReadFalse(employeeId);
    }

    public void markRead(Notification n) {
        n.setRead(true);
        notificationRepository.save(n);
    }
}
