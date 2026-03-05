package jp.yoshiaki.insuranceapp.dto;

import jp.yoshiaki.insuranceapp.entity.Accident;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccidentDetailResponse {

    private Long id;
    private Long policyId;
    private String policyNumber;
    private String customerName;
    private String occurredAt;
    private String place;
    private String description;
    private String status;
    private String statusCode;
    private String lastContactedAt;
    private String memo;
    private boolean stagnant;
    
    private boolean canStartProgress;
    private boolean canResolve;
    private boolean canContact;

    public static AccidentDetailResponse from(Accident accident) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

        return AccidentDetailResponse.builder()
                .id(accident.getId())
                .policyId(accident.getPolicyId())
                .policyNumber(accident.getPolicy() != null ? 
                        accident.getPolicy().getPolicyNumber() : "")
                .customerName(accident.getPolicy() != null ? 
                        accident.getPolicy().getCustomerName() : "")
                .occurredAt(accident.getOccurredAt().format(dateFormatter))
                .place(accident.getPlace())
                .description(accident.getDescription())
                .status(accident.getStatusLabel())
                .statusCode(accident.getStatus())
                .lastContactedAt(accident.getLastContactedAt() != null ? 
                        accident.getLastContactedAt().format(dateTimeFormatter) : "")
                .memo(accident.getMemo() != null ? accident.getMemo() : "")
                .stagnant(accident.isStagnant())
                .canStartProgress("OPEN".equals(accident.getStatus()))
                .canResolve("IN_PROGRESS".equals(accident.getStatus()))
                .canContact("OPEN".equals(accident.getStatus()) || 
                        "IN_PROGRESS".equals(accident.getStatus()))
                .build();
    }
}
