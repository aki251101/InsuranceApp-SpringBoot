package jp.yoshiaki.insuranceapp.training.day63.policy.domain;

import java.time.LocalDate;

public class Policy {
    private final long id;
    private final String customerName;
    private final LocalDate startDate;
    private final int premium;

    public Policy(long id, String customerName, LocalDate startDate, int premium) {
        this.id = id;
        this.customerName = customerName;
        this.startDate = startDate;
        this.premium = premium;
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
