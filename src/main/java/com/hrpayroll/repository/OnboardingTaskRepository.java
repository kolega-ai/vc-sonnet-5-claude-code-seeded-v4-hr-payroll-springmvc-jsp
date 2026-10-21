package com.hrpayroll.repository;

import com.hrpayroll.domain.OnboardingTask;
import com.hrpayroll.domain.OnboardingTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    List<OnboardingTask> findByAssigneeIdOrderByDueDateAsc(Long assigneeId);

    List<OnboardingTask> findByStatusOrderByDueDateAsc(OnboardingTaskStatus status);

    List<OnboardingTask> findAllByOrderByDueDateAsc();
}
