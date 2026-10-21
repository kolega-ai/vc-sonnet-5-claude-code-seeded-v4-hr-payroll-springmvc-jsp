package com.hrpayroll.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Append-only audit trail. No update/delete path exists anywhere in the app
 * for this entity - it is written by AuditService.record(...) only.
 */
@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_email", length = 190)
    private String actorEmail;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "resource_type", length = 80)
    private String resourceType;

    @Column(name = "resource_id", length = 80)
    private String resourceId;

    @Column(name = "before_value", length = 2000)
    private String beforeValue;

    @Column(name = "after_value", length = 2000)
    private String afterValue;

    @Column(name = "authorization_basis", length = 300)
    private String authorizationBasis;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }

    public void setAfterValue(String afterValue) {
        this.afterValue = afterValue;
    }

    public String getAuthorizationBasis() {
        return authorizationBasis;
    }

    public void setAuthorizationBasis(String authorizationBasis) {
        this.authorizationBasis = authorizationBasis;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
