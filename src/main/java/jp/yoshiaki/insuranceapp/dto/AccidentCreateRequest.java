package jp.yoshiaki.insuranceapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccidentCreateRequest {

    @NotNull(message = "契約を選択してください")
    private Long policyId;

    @NotNull(message = "受付日を入力してください")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate occurredAt;

    @Size(max = 200, message = "事故場所は200文字以内で入力してください")
    private String place;

    @Size(max = 2000, message = "事故概要は2000文字以内で入力してください")
    private String description;
}
