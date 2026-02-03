package jp.yoshiaki.insuranceapp.training.day64.policy.web.dto;

public class PolicyResponse {
    public long id;
    public long customerId;
    public String productName;
    public String startDate;

    public PolicyResponse(long id, long customerId, String productName, String startDate) {
        this.id = id;
        this.customerId = customerId;
        this.productName = productName;
        this.startDate = startDate;
    }
}
