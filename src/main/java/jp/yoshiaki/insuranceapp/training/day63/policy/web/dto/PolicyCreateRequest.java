package jp.yoshiaki.insuranceapp.training.day63.policy.web.dto;

import java.time.LocalDate;

// Boot3: jakarta.validation.constraints.*
// Boot2: javax.validation.constraints.*
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PolicyCreateRequest {

    @NotBlank(message = "customerName は必須です")
    private String customerName;

    @NotNull(message = "startDate は必須です")
    private LocalDate startDate;

    @Positive(message = "premium は正の数である必要があります")
    private Integer premium;

    public PolicyCreateRequest() {
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getPremium() {
        return premium;
    }

    public void setPremium(Integer premium) {
        this.premium = premium;
    }
}
