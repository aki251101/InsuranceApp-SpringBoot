package jp.yoshiaki.insuranceapp.dto.policy;

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
    private String monthStart;
    private String monthRate;
    private String monthCount;

    public static RenewalStatsDto empty() {
        return RenewalStatsDto.builder()
                .fiscalYearStart("")
                .fiscalYearRate("— (0/0)")
                .fiscalYearCount("")
                .monthStart("")
                .monthRate("— (0/0)")
                .monthCount("")
                .build();
    }
}
