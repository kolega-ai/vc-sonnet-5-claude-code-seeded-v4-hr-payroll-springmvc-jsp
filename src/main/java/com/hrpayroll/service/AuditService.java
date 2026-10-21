package com.hrpayroll.service;

import com.hrpayroll.domain.AuditEvent;
import com.hrpayroll.repository.AuditEventRepository;
import com.hrpayroll.security.EmployeePrincipal;
import org.springframework.stereotype.Service;

/** Append-only audit writer. No update/delete method exists for AuditEvent anywhere. */
@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void record(EmployeePrincipal actor, String action, String resourceType, String resourceId,
                        String before, String after, String authorizationBasis) {
        AuditEvent event = new AuditEvent();
        if (actor != null) {
            event.setActorId(actor.getId());
            event.setActorEmail(actor.getUsername());
        }
        event.setAction(action);
        event.setResourceType(resourceType);
        event.setResourceId(resourceId);
        event.setBeforeValue(before);
        event.setAfterValue(after);
        event.setAuthorizationBasis(authorizationBasis);
        auditEventRepository.save(event);
    }

    /** One audit event per list/export request, naming the resource class and count returned. */
    public void recordRead(EmployeePrincipal actor, String resourceType, int count, String authorizationBasis) {
        record(actor, "READ_LIST", resourceType, null, null, "count=" + count, authorizationBasis);
    }
}
