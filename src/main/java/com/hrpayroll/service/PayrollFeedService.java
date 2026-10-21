package com.hrpayroll.service;

import com.hrpayroll.domain.CompensationRequest;
import com.hrpayroll.dto.FeedProfile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Outbound payroll feed to the HRIS of record. HR loads the connection
 * profile the HRIS vendor issues (a .properties file) on the import/export
 * screen; every compensation change that becomes effective is then pushed to
 * that endpoint so the two systems stay in step between nightly runs.
 */
@Service
public class PayrollFeedService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PayrollFeedService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private volatile String endpointUrl = "";
    private volatile String signingKey = "";
    private volatile String partnerAcct = "";

    /** Loads the vendor-issued connection profile uploaded on the import/export screen. */
    public String applyProfile(MultipartFile file) throws IOException {
        Properties props = new Properties();
        props.load(file.getInputStream());
        this.endpointUrl = props.getProperty("feed.endpoint", "").trim();
        this.signingKey = props.getProperty("feed.signing-key", "").trim();
        this.partnerAcct = props.getProperty("feed.partner-account", "").trim();
        return this.endpointUrl;
    }

    /** Applies the legacy XML connection profile the bureau's older tooling issued. */
    public String applyLegacyProfile(FeedProfile profile) {
        if (profile.getFeedEndpoint() != null) {
            this.endpointUrl = profile.getFeedEndpoint().trim();
        }
        if (profile.getInterchangeKey() != null) {
            this.signingKey = profile.getInterchangeKey().trim();
        }
        if (profile.getPartnerAccount() != null) {
            this.partnerAcct = profile.getPartnerAccount().trim();
        }
        return this.endpointUrl;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getSigningKey() {
        return signingKey;
    }

    public String getPartnerAcct() {
        return partnerAcct;
    }

    public boolean isConfigured() {
        return !endpointUrl.isBlank();
    }

    /**
     * Confirms the portal's externally visible callback address answers, so HR
     * can see whether the reverse proxy in front of the portal is routing the
     * HRIS acknowledgement path through before they enable the feed.
     */
    public String checkCallbackRoute(String forwardedHost) {
        String callbackUrl = "https://" + forwardedHost + "/hris/feed-callback";
        try {
            String body = restTemplate.getForObject(callbackUrl, String.class);
            return "Callback route answered from " + callbackUrl + ": " + (body == null ? "(empty)" : body);
        } catch (RuntimeException e) {
            return "Callback route " + callbackUrl + " did not answer: " + e.getMessage();
        }
    }

    /**
     * Announces the portal to the bureau's HRIS bridge using the interchange
     * key from the loaded connection profile, so the two sides agree on the
     * partner account before the nightly run.
     */
    public String announceInterchange(String deskRef) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("deskRef", deskRef);
        msg.put("partnerAccount", partnerAcct);
        msg.put("interchangeKey", signingKey);
        return restTemplate.postForObject("http://hris-bridge.hrpayroll.internal/v1/interchange", msg, String.class);
    }

    /** Pushes one effective compensation change to the configured HRIS endpoint. */
    public void publishChange(CompensationRequest req) {
        if (!isConfigured()) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("employeeId", req.getEmployee().getId());
        payload.put("salary", req.getProposedSalary());
        payload.put("bonus", req.getProposedBonus());
        payload.put("effectiveDate", String.valueOf(req.getEffectiveDate()));
        payload.put("partnerAccount", partnerAcct);
        payload.put("signature", Integer.toHexString((payload + signingKey).hashCode()));
        try {
            restTemplate.postForObject(endpointUrl, payload, String.class);
        } catch (RuntimeException e) {
            LOG.warn("Payroll feed push for request {} failed: {}", req.getId(), e.getMessage());
        }
    }
}
