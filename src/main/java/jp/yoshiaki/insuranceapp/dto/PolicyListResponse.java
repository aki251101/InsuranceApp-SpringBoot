package jp.yoshiaki.insuranceapp.dto;

import jp.yoshiaki.insuranceapp.entity.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyListResponse {

    private List<PolicyItem> items;
    private RenewalStatsDto stats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PolicyItem {
        private Long id;
        private String policyNumber;
        private String customerName;
        private String startDate;
        private String endDate;
        private String status;
        private boolean attentionRequired;
        private boolean calendarRegistered;

        public static PolicyItem from(Policy policy) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            
            return PolicyItem.builder()
                    .id(policy.getId())
                    .policyNumber(policy.getPolicyNumber())
                    .customerName(policy.getCustomerName())
                    .startDate(policy.getStartDate().format(formatter))
                    .endDate(policy.getEndDate().format(formatter))
                    .status(policy.getEffectiveStatus())
                    .attentionRequired(policy.isAttentionRequired())
                    .calendarRegistered(policy.getCalendarRegistered())
                    .build();
        }
    }

    public static PolicyListResponse from(List<Policy> policies, RenewalStatsDto stats) {
        List<PolicyItem> items = policies.stream()
                .map(PolicyItem::from)
                .collect(Collectors.toList());

        return PolicyListResponse.builder()
                .items(items)
                .stats(stats)
                .build();
    }
}
