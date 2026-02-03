package jp.yoshiaki.insuranceapp.training.day64.policy.domain;

import java.time.LocalDate;

public class Policy {
    private final long id;
    private final long customerId;
    private final String productName;
    private final LocalDate startDate;

    public Policy(long id, long customerId, String productName, LocalDate startDate) {
        this.id = id;
        this.customerId = customerId;
        this.productName = productName;
        this.startDate = startDate;
    }

    public long getId() { return id; }
    public long getCustomerId() { return customerId; }
    public String getProductName() { return productName; }
    public LocalDate getStartDate() { return startDate; }
}
