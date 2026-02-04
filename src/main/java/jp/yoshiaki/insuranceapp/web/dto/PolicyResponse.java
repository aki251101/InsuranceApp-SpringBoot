package jp.yoshiaki.insuranceapp.web.dto;

import jp.yoshiaki.insuranceapp.domain.Policy;
import java.time.LocalDate;

public class PolicyResponse {
    private long id;
    private Long customerId;
    private String customerName;
    private String productName;
    private LocalDate startDate;
    private int premium;

    public PolicyResponse() {}

    public PolicyResponse(Policy policy) {
        this.id = policy.getId();
        this.customerId = policy.getCustomerId();
        this.customerName = policy.getCustomerName();
        this.productName = policy.getProductName();
        this.startDate = policy.getStartDate();
        this.premium = policy.getPremium();
    }

    // getter/setter
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public int getPremium() { return premium; }
    public void setPremium(int premium) { this.premium = premium; }
}