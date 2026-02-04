package jp.yoshiaki.insuranceapp.domain;

import java.time.LocalDate;

public class Policy {
    private final long id;
    private final Long customerId;
    private final String customerName;
    private final String productName;
    private final LocalDate startDate;
    private final int premium;

    public Policy(long id, Long customerId, String customerName,
                  String productName, LocalDate startDate, int premium) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.productName = productName;
        this.startDate = startDate;
        this.premium = premium;
    }

    public long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getProductName() { return productName; }
    public LocalDate getStartDate() { return startDate; }
    public int getPremium() { return premium; }
}