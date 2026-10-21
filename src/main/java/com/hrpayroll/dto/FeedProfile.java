package com.hrpayroll.dto;

/**
 * Connection profile for the outbound HRIS feed, as issued by the payroll
 * bureau. Historically the vendor exported this as an XML document; the
 * import screen accepts that legacy document so bureaus that have not moved
 * to the newer .properties profile can still be onboarded.
 */
public class FeedProfile {

    private String feedEndpoint;
    private String interchangeKey;
    private String partnerAccount;

    public String getFeedEndpoint() {
        return feedEndpoint;
    }

    public void setFeedEndpoint(String feedEndpoint) {
        this.feedEndpoint = feedEndpoint;
    }

    public String getInterchangeKey() {
        return interchangeKey;
    }

    public void setInterchangeKey(String interchangeKey) {
        this.interchangeKey = interchangeKey;
    }

    public String getPartnerAccount() {
        return partnerAccount;
    }

    public void setPartnerAccount(String partnerAccount) {
        this.partnerAccount = partnerAccount;
    }
}
