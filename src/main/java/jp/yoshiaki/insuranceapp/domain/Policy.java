package jp.yoshiaki.insuranceapp.domain;

import java.time.LocalDate;

public class Policy {
    private final long id;
    private final Long customerId;
    private final String customerName;
    private final String productName;
    private final LocalDate startDate;
    private final int premium;
    private final String policyNumber;
    private final LocalDate endDate;
    private final String effectiveStatus;

    public Policy(long id,
                  Long customerId,
                  String customerName,
                  String productName,
                  LocalDate startDate,
                  int premium,
                  String policyNumber,
                  LocalDate endDate,
                  String effectiveStatus) {

        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.productName = productName;
        this.startDate = startDate;
        this.premium = premium;
        this.policyNumber = policyNumber;
        this.endDate = endDate;
        this.effectiveStatus = effectiveStatus;
    }

    public long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getProductName() { return productName; }
    public LocalDate getStartDate() { return startDate; }
    public int getPremium() { return premium; }
    public String getPolicyNumber() { return policyNumber; }
    public LocalDate getEndDate() { return endDate; }
    public String getEffectiveStatus() { return effectiveStatus; }
}