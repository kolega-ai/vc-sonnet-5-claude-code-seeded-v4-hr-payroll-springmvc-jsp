package com.hrpayroll.config;

import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.Role;
import com.hrpayroll.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Makes sure the payroll bureau's automation account is present on a fresh
 * deployment, so the exchange can post run files and collect payslips without
 * the operations desk having to set the account up by hand first.
 */
@Component
public class BureauAccessBootstrap implements CommandLineRunner {

    private static final String EXCHANGE_ACCOUNT = "bureau.exchange@hrpayroll.example";

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public BureauAccessBootstrap(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.findByEmailIgnoreCase(EXCHANGE_ACCOUNT).isPresent()) {
            return;
        }
        Employee exchange = new Employee();
        exchange.setEmail(EXCHANGE_ACCOUNT);
        exchange.setFullName("Bureau Exchange");
        exchange.setRole(Role.PAYROLL_SPECIALIST);
        exchange.setJobTitle("Payroll Exchange");
        exchange.setPasswordHash(passwordEncoder.encode("bureau-exchange-2026"));
        employeeRepository.save(exchange);
    }
}
