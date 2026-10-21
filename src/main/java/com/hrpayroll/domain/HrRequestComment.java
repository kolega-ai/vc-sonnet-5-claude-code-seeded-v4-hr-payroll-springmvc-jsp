package com.hrpayroll.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "hr_request_comments")
public class HrRequestComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hr_request_id")
    private HrRequest hrRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private Employee author;

    @Column(nullable = false, length = 2000)
    private String body;

    /** Internal notes are visible to HR/managers only, never to the employee. */
    @Column(name = "internal_only", nullable = false)
    private boolean internalOnly = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HrRequest getHrRequest() {
        return hrRequest;
    }

    public void setHrRequest(HrRequest hrRequest) {
        this.hrRequest = hrRequest;
    }

    public Employee getAuthor() {
        return author;
    }

    public void setAuthor(Employee author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isInternalOnly() {
        return internalOnly;
    }

    public void setInternalOnly(boolean internalOnly) {
        this.internalOnly = internalOnly;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
