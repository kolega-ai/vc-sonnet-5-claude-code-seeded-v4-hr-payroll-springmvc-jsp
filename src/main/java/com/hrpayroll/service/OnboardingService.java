package com.hrpayroll.service;

import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.OnboardingTask;
import com.hrpayroll.domain.OnboardingTaskStatus;
import com.hrpayroll.dto.OnboardingTaskForm;
import com.hrpayroll.repository.OnboardingTaskRepository;
import com.hrpayroll.security.EmployeePrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OnboardingService {

    private final OnboardingTaskRepository taskRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    private static final Map<OnboardingTaskStatus, Set<OnboardingTaskStatus>> TRANSITIONS = new EnumMap<>(OnboardingTaskStatus.class);
    static {
        TRANSITIONS.put(OnboardingTaskStatus.NOT_STARTED, EnumSet.of(OnboardingTaskStatus.IN_PROGRESS, OnboardingTaskStatus.WAIVED));
        TRANSITIONS.put(OnboardingTaskStatus.IN_PROGRESS, EnumSet.of(OnboardingTaskStatus.SUBMITTED, OnboardingTaskStatus.WAIVED));
        TRANSITIONS.put(OnboardingTaskStatus.SUBMITTED, EnumSet.of(OnboardingTaskStatus.APPROVED, OnboardingTaskStatus.REJECTED));
        TRANSITIONS.put(OnboardingTaskStatus.REJECTED, EnumSet.of(OnboardingTaskStatus.IN_PROGRESS, OnboardingTaskStatus.WAIVED));
        TRANSITIONS.put(OnboardingTaskStatus.APPROVED, EnumSet.noneOf(OnboardingTaskStatus.class));
        TRANSITIONS.put(OnboardingTaskStatus.WAIVED, EnumSet.noneOf(OnboardingTaskStatus.class));
    }

    public OnboardingService(OnboardingTaskRepository taskRepository, AuditService auditService,
                              NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public List<OnboardingTask> forAssignee(Long assigneeId) {
        return taskRepository.findByAssigneeIdOrderByDueDateAsc(assigneeId);
    }

    public List<OnboardingTask> all() {
        return taskRepository.findAllByOrderByDueDateAsc();
    }

    public List<OnboardingTask> pendingReview() {
        return taskRepository.findByStatusOrderByDueDateAsc(OnboardingTaskStatus.SUBMITTED);
    }

    public OnboardingTask getOrThrow(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    @Transactional
    public OnboardingTask create(OnboardingTaskForm form, Employee assignee) {
        OnboardingTask task = new OnboardingTask();
        task.setAssignee(assignee);
        task.setTitle(form.getTitle());
        task.setDescription(form.getDescription());
        task.setDueDate(form.getDueDate());
        return taskRepository.save(task);
    }

    @Transactional
    public void transition(OnboardingTask task, OnboardingTaskStatus target, EmployeePrincipal actor, String notes) {
        Set<OnboardingTaskStatus> allowed = TRANSITIONS.getOrDefault(task.getStatus(), Set.of());
        if (!allowed.contains(target)) {
            throw new IllegalStateException("Cannot move task from " + task.getStatus() + " to " + target);
        }
        String before = task.getStatus().name();
        task.setStatus(target);
        if (notes != null) {
            task.setReviewNotes(notes);
        }
        if (target == OnboardingTaskStatus.APPROVED || target == OnboardingTaskStatus.REJECTED) {
            task.setReviewedBy(actor.getEmployee());
        }
        task.setUpdatedAt(java.time.Instant.now());
        taskRepository.save(task);
        auditService.record(actor, "ONBOARDING_TASK_TRANSITION", "OnboardingTask", task.getId().toString(),
                before, target.name(), "reviewer=" + actor.getId());
        notificationService.notify(task.getAssignee(), "Onboarding task \"" + task.getTitle() + "\" is now " + target.name() + ".",
                "/employee/onboarding");
    }

    @Transactional
    public void attachEvidence(OnboardingTask task, Long documentId) {
        task.setEvidenceDocumentId(documentId);
        taskRepository.save(task);
    }

    private final org.springframework.web.client.RestTemplate deskAssistant = new org.springframework.web.client.RestTemplate();
    private static final com.fasterxml.jackson.databind.ObjectMapper REVIEW_JSON = new com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * Runs the onboarding desk assistant over the evidence an employee attached
     * to a submitted task and applies the decision it returns, so the review
     * queue can be worked without opening each attachment by hand.
     */
    @Transactional
    public void applyAssistantReview(OnboardingTask task, String evidence, EmployeePrincipal reviewer) {
        java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(java.util.Map.of("role", "system", "content",
                "You are the onboarding desk assistant. Compare the attached evidence with the task checklist and "
              + "reply with one JSON object {\"action\":\"approve|reject\",\"notes\":\"...\"}. Available functions: "
              + "approve_task, reject_task."));
        messages.add(java.util.Map.of("role", "user", "content",
                "Task: " + task.getTitle() + " - " + task.getDescription() + "\nEvidence:\n" + evidence));
        java.util.Map<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("model", "onboarding-desk");
        request.put("messages", messages);
        request.put("functions", java.util.List.of(
                java.util.Map.of("name", "approve_task"),
                java.util.Map.of("name", "reject_task")));
        java.util.Map<?, ?> reply = deskAssistant.postForObject(
                "http://onboarding-assistant.hrpayroll.internal/v1/chat/completions", request, java.util.Map.class);
        String action = reviewAction(reply);
        if ("approve".equals(action)) {
            transition(task, OnboardingTaskStatus.APPROVED, reviewer, "Desk assistant review");
        } else if ("reject".equals(action)) {
            transition(task, OnboardingTaskStatus.REJECTED, reviewer, "Desk assistant review");
        }
    }

    @SuppressWarnings("unchecked")
    private String reviewAction(java.util.Map<?, ?> reply) {
        try {
            java.util.List<java.util.Map<String, Object>> choices =
                    (java.util.List<java.util.Map<String, Object>>) reply.get("choices");
            java.util.Map<String, Object> message = (java.util.Map<String, Object>) choices.get(0).get("message");
            java.util.Map<String, Object> parsed =
                    REVIEW_JSON.readValue(String.valueOf(message.get("content")), java.util.Map.class);
            Object action = parsed.get("action");
            return action == null ? "" : action.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
