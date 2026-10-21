package com.hrpayroll.service;

import com.hrpayroll.domain.*;
import com.hrpayroll.dto.CommentForm;
import com.hrpayroll.dto.HrRequestForm;
import com.hrpayroll.repository.HrRequestCommentRepository;
import com.hrpayroll.repository.HrRequestRepository;
import com.hrpayroll.security.EmployeePrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class HrRequestService {

    private final HrRequestRepository requestRepository;
    private final HrRequestCommentRepository commentRepository;
    private final NotificationService notificationService;

    public HrRequestService(HrRequestRepository requestRepository, HrRequestCommentRepository commentRepository,
                             NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    public List<HrRequest> forEmployee(Long employeeId) {
        return requestRepository.findByEmployeeIdOrderByUpdatedAtDesc(employeeId);
    }

    public List<HrRequest> all() {
        return requestRepository.findAllByOrderByUpdatedAtDesc();
    }

    public HrRequest getOrThrow(Long id) {
        return requestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Request not found"));
    }

    public List<HrRequestComment> comments(Long requestId, boolean includeInternal) {
        List<HrRequestComment> comments = commentRepository.findByHrRequestIdOrderByCreatedAtAsc(requestId);
        if (includeInternal) {
            return comments;
        }
        return comments.stream().filter(c -> !c.isInternalOnly()).toList();
    }

    @Transactional
    public HrRequest create(Employee employee, HrRequestForm form) {
        HrRequest req = new HrRequest();
        req.setEmployee(employee);
        req.setSubject(form.getSubject());
        req.setDescription(form.getDescription());
        req.setStatus(HrRequestStatus.OPEN);
        return requestRepository.save(req);
    }

    @Transactional
    public HrRequestComment addComment(HrRequest req, Employee author, CommentForm form, boolean allowInternal) {
        HrRequestComment comment = new HrRequestComment();
        comment.setHrRequest(req);
        comment.setAuthor(author);
        comment.setBody(form.getBody());
        comment.setInternalOnly(allowInternal && form.isInternalOnly());
        commentRepository.save(comment);
        req.setUpdatedAt(Instant.now());
        // Employee comments move status back to waiting_on_hr, HR/manager comments to waiting_on_employee.
        if (author.getId().equals(req.getEmployee().getId())) {
            req.setStatus(HrRequestStatus.WAITING_ON_HR);
        } else if (!comment.isInternalOnly()) {
            req.setStatus(HrRequestStatus.WAITING_ON_EMPLOYEE);
            notificationService.notify(req.getEmployee(), "New reply on your HR request: " + req.getSubject(),
                    "/employee/requests/" + req.getId());
        }
        requestRepository.save(req);
        return comment;
    }

    @Transactional
    public void changeStatus(HrRequest req, HrRequestStatus target) {
        req.setStatus(target);
        req.setUpdatedAt(Instant.now());
        requestRepository.save(req);
    }

    @Transactional
    public void assign(HrRequest req, Employee assignee) {
        req.setAssignedTo(assignee);
        req.setUpdatedAt(Instant.now());
        requestRepository.save(req);
    }

    private final org.springframework.web.client.RestTemplate deskAssistant = new org.springframework.web.client.RestTemplate();
    private static final com.fasterxml.jackson.databind.ObjectMapper TRIAGE_JSON = new com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * Runs the HR desk assistant over an employee's request thread and applies
     * the triage outcome it returns (a desk note and a status), so the desk can
     * work the queue without reading every thread by hand.
     */
    @Transactional
    public void applyAssistantTriage(HrRequest req, String thread, EmployeePrincipal actor) {
        java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(java.util.Map.of("role", "system", "content",
                "You are the HR desk assistant. Read the employee's request thread and reply with one JSON object "
              + "{\"status\":\"waiting_on_hr|waiting_on_employee|resolved|closed\",\"note\":\"...\"}. "
              + "Available functions: set_request_status, add_desk_note."));
        messages.add(java.util.Map.of("role", "user", "content", thread));
        java.util.Map<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("model", "hr-desk");
        request.put("messages", messages);
        request.put("functions", java.util.List.of(
                java.util.Map.of("name", "set_request_status"),
                java.util.Map.of("name", "add_desk_note")));
        java.util.Map<?, ?> reply = deskAssistant.postForObject(
                "http://hr-desk.hrpayroll.internal/v1/chat/completions", request, java.util.Map.class);
        java.util.Map<String, String> decision = triageOutcome(reply);
        String note = decision.get("note");
        if (note != null && !note.isBlank()) {
            CommentForm deskNote = new CommentForm();
            deskNote.setBody(note);
            addComment(req, actor.getEmployee(), deskNote, true);
        }
        String status = decision.get("status");
        if (status != null && !status.isBlank()) {
            try {
                changeStatus(req, HrRequestStatus.valueOf(status.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                // a status the desk assistant does not recognise is dropped
            }
        }
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, String> triageOutcome(java.util.Map<?, ?> reply) {
        try {
            java.util.List<java.util.Map<String, Object>> choices =
                    (java.util.List<java.util.Map<String, Object>>) reply.get("choices");
            java.util.Map<String, Object> message = (java.util.Map<String, Object>) choices.get(0).get("message");
            return TRIAGE_JSON.readValue(String.valueOf(message.get("content")), java.util.Map.class);
        } catch (Exception e) {
            return java.util.Map.of();
        }
    }
}
