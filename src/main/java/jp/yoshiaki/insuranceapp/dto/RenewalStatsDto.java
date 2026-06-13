package jp.yoshiaki.insuranceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenewalStatsDto {

    private String fiscalYearStart;
    private String fiscalYearRate;
    private String fiscalYearCount;
    private double fiscalYearRateValue;
    private int fiscalYearTotalCount;
    private String monthStart;
    private String monthRate;
    private String monthCount;
    private double monthRateValue;
    private int monthTotalCount;

    public static RenewalStatsDto empty() {
        return RenewalStatsDto.builder()
                .fiscalYearStart("")
                .fiscalYearRate("")
                .fiscalYearCount("")
                .fiscalYearRateValue(0)
                .fiscalYearTotalCount(0)
                .monthStart("")
                .monthRate("")
                .monthCount("")
                .monthRateValue(0)
                .monthTotalCount(0)
                .build();
    }
}
