package com.hrpayroll.config;

import com.hrpayroll.domain.OnboardingTask;
import com.hrpayroll.domain.OnboardingTaskStatus;
import com.hrpayroll.repository.EmployeeRepository;
import com.hrpayroll.repository.OnboardingTaskRepository;
import com.hrpayroll.service.NotificationService;
import com.hrpayroll.service.PolicyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * In-process scheduled job (no external broker/queue): daily onboarding
 * due-date reminder sweep. Runs once at startup (small initial delay) and
 * then every 24h; also exposed via runNow() so it is exercised meaningfully
 * even in a short-lived demo/CI environment.
 */
@Component
public class ScheduledJobs {

    private final OnboardingTaskRepository onboardingTaskRepository;
    private final NotificationService notificationService;
    private final PolicyService policyService;
    private final EmployeeRepository employeeRepository;

    public ScheduledJobs(OnboardingTaskRepository onboardingTaskRepository, NotificationService notificationService,
                          PolicyService policyService, EmployeeRepository employeeRepository) {
        this.onboardingTaskRepository = onboardingTaskRepository;
        this.notificationService = notificationService;
        this.policyService = policyService;
        this.employeeRepository = employeeRepository;
    }

    @Scheduled(initialDelay = 5000, fixedRate = 24 * 60 * 60 * 1000)
    public void remindOnDueTasks() {
        runNow();
    }

    public int runNow() {
        LocalDate soon = LocalDate.now().plusDays(2);
        List<OnboardingTask> dueSoon = onboardingTaskRepository.findByStatusOrderByDueDateAsc(OnboardingTaskStatus.NOT_STARTED);
        int notified = 0;
        for (OnboardingTask task : dueSoon) {
            if (task.getDueDate() != null && !task.getDueDate().isAfter(soon)) {
                notificationService.notify(task.getAssignee(),
                        "Reminder: onboarding task \"" + task.getTitle() + "\" is due " + task.getDueDate() + ".",
                        "/employee/onboarding");
                notified++;
            }
        }
        // Outstanding policy acknowledgements: the reminder links straight at the
        // acknowledgement so staff can clear it from the message itself.
        for (com.hrpayroll.domain.Policy policy : policyService.activePolicies()) {
            for (com.hrpayroll.domain.Employee employee : employeeRepository.findAll()) {
                boolean acked = policyService.acknowledgementsFor(employee.getId()).stream()
                        .anyMatch(a -> a.getPolicy().getId().equals(policy.getId())
                                && a.getPolicyVersion() >= policy.getVersion());
                if (!acked) {
                    notificationService.notify(employee,
                            "Policy \"" + policy.getTitle() + "\" still needs your acknowledgement.",
                            "/employee/policies/acknowledge?policyId=" + policy.getId());
                    notified++;
                }
            }
        }
        return notified;
    }
}
