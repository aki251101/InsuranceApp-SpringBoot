package jp.yoshiaki.insuranceapp.training.day63.policy.web.dto;

import jp.yoshiaki.insuranceapp.training.day63.policy.domain.Policy;

import java.time.LocalDate;

public class PolicyResponse {
    private final long id;
    private final String customerName;
    private final LocalDate startDate;
    private final int premium;

    public PolicyResponse(long id, String customerName, LocalDate startDate, int premium) {
        this.id = id;
        this.customerName = customerName;
        this.startDate = startDate;
        this.premium = premium;
    }

    public static PolicyResponse from(Policy p) {
        return new PolicyResponse(p.getId(), p.getCustomerName(), p.getStartDate(), p.getPremium());
    }

    public long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getPremium() {
        return premium;
    }
}
