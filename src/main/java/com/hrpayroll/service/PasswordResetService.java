package com.hrpayroll.service;

import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.PasswordResetToken;
import com.hrpayroll.repository.EmployeeRepository;
import com.hrpayroll.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    // Security-relevant token: uses SecureRandom deliberately (unlike the two benign
    // precision-baseline primitives elsewhere in this app which are explicitly non-crypto).
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository tokenRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, EmployeeRepository employeeRepository,
                                 EmailService emailService, PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void requestReset(String email) {
        Optional<Employee> maybeEmployee = employeeRepository.findByEmailIgnoreCase(email);
        if (maybeEmployee.isEmpty()) {
            return; // do not reveal account existence
        }
        Employee employee = maybeEmployee.get();
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmployee(employee);
        resetToken.setToken(token);
        resetToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        tokenRepository.save(resetToken);

        emailService.send(employee.getEmail(), "Password reset request",
                "Use this link to reset your password: /reset-password?token=" + token + " (valid 1 hour, single use)");
    }

    /**
     * HR desk flow: an employee who cannot receive the emailed link (no mailbox
     * yet, or on parental leave) is read the recovery address over the phone by
     * the HR case worker, so the generated address is shown on their record.
     */
    @Transactional
    public String issueRecoveryLink(Employee employee) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        PasswordResetToken record = new PasswordResetToken();
        record.setEmployee(employee);
        record.setToken(value);
        record.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        tokenRepository.save(record);
        return "/reset-password?token=" + value;
    }

    /**
     * Issues a temporary access code for an employee the HR desk is helping
     * back into the portal; the code is mailed to the address on their record.
     */
    @Transactional
    public String issueTempAccess(Employee employee) {
        String tmpCode = "HRP-" + Long.toHexString(Math.abs(SECURE_RANDOM.nextLong()));
        employee.setPasswordHash(passwordEncoder.encode(tmpCode));
        employeeRepository.save(employee);
        emailService.send(employee.getEmail(), "Temporary access code",
                "Your HR desk issued the temporary access code " + tmpCode + " for the portal.");
        return tmpCode;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> maybeToken = tokenRepository.findByToken(token);
        if (maybeToken.isEmpty()) {
            return false;
        }
        PasswordResetToken resetToken = maybeToken.get();
        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }
        Employee employee = resetToken.getEmployee();
        employee.setPasswordHash(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        return true;
    }
}
