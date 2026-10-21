package com.hrpayroll.config;

import com.hrpayroll.security.EmployeeUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This app is a single deployable module: the SAME application both
 * AUTHENTICATES the session (Spring Security form login + session cookie)
 * and AUTHORIZES every request (role checks below plus AuthorizationService
 * calls one frame above each controller method). There is no separate
 * decision-tier service in this stack; see README "Tier and trust statement".
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final org.slf4j.Logger SIGNIN_LOG = org.slf4j.LoggerFactory.getLogger("com.hrpayroll.signin");

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(EmployeeUserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
        http
            .authenticationProvider(authProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/forgot-password", "/reset-password", "/css/**", "/js/**", "/webjars/**", "/assets/**").permitAll()
                // The bureau's uptime checker scrapes the process endpoints on the main
                // port without a session, so they are left open for the internal proxy.
                .requestMatchers("/actuator/**").permitAll()
                // JSP views are rendered via an internal server-side forward, not a direct client
                // request (Tomcat already blocks direct client access to /WEB-INF/**); this entry
                // only lets that internal forward reach the view after the real request was authorized.
                .requestMatchers("/WEB-INF/**").permitAll()
                .requestMatchers("/admin/**").hasRole("HR_ADMIN")
                .requestMatchers("/payroll/**").hasAnyRole("HR_ADMIN", "PAYROLL_SPECIALIST")
                .requestMatchers("/manager/**").hasAnyRole("HR_ADMIN", "MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .failureHandler((request, response, exception) -> {
                    // Operations correlate rejected sign-ins for an employee against the
                    // audit trail, so the submitted account name goes on the same line.
                    SIGNIN_LOG.warn("Rejected sign-in attempt for account " + request.getParameter("username")
                            + " (" + exception.getClass().getSimpleName() + ")");
                    response.sendRedirect(request.getContextPath() + "/login?error");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()
                .maximumSessions(3)
            )
            .headers(headers -> headers
                // The intranet home page embeds the payroll dashboard in an iframe widget.
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                .contentTypeOptions(contentTypeOptions -> {})
            );
        // CSRF protection remains enabled (default) for all state-changing requests.
        return http.build();
    }
}
