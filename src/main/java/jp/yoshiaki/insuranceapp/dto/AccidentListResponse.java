package jp.yoshiaki.insuranceapp.dto;

import jp.yoshiaki.insuranceapp.entity.Accident;
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
public class AccidentListResponse {

    private List<AccidentItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccidentItem {
        private Long id;
        private String occurredAt;
        private String policyNumber;
        private String customerName;
        private String status;
        private boolean stagnant;

        public static AccidentItem from(Accident accident) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            
            return AccidentItem.builder()
                    .id(accident.getId())
                    .occurredAt(accident.getOccurredAt().format(formatter))
                    .policyNumber(accident.getPolicy() != null ? 
                            accident.getPolicy().getPolicyNumber() : "")
                    .customerName(accident.getPolicy() != null ? 
                            accident.getPolicy().getCustomerName() : "")
                    .status(accident.getStatusLabel())
                    .stagnant(accident.isStagnant())
                    .build();
        }
    }

    public static AccidentListResponse from(List<Accident> accidents) {
        List<AccidentItem> items = accidents.stream()
                .map(AccidentItem::from)
                .collect(Collectors.toList());

        return AccidentListResponse.builder()
                .items(items)
                .build();
    }
}
