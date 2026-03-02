// 配置：src/main/java/jp/yoshiaki/insuranceapp/dto/policy/PolicyResponse.java
package jp.yoshiaki.insuranceapp.dto.policy;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 契約レスポンス DTO。
 *
 * Entity（Policy）をそのままJSONで返さず、DTO に変換する理由は：
 *   ① クライアントに見せたいフィールドだけに絞れる
 *   ② 日付の表示形式を「yyyy/MM/dd」に変換できる
 *   ③ effectiveStatus のように「計算した値」を追加できる
 *   ④ Entity の内部構造（@PrePersist 等）がAPI仕様に漏れない
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyResponse {

    private Long id;
    private String policyNumber;
    private String customerName;
    private String startDate;
    private String endDate;
    private String status;
    private String effectiveStatus;
    private boolean attentionRequired;
    private boolean renewable;
    private boolean calendarRegistered;

    /** 日付の表示フォーマット（yyyy/MM/dd） */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * Entity → DTO 変換メソッド。
     * Controller が Entity を受け取った後、レスポンスとして返す前にこのメソッドで変換する。
     *
     * @param policy Policy エンティティ
     * @return PolicyResponse DTO
     */
    public static PolicyResponse from(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .customerName(policy.getCustomerName())
                .startDate(formatDate(policy.getStartDate()))
                .endDate(formatDate(policy.getEndDate()))
                .status(policy.getStatus())
                .effectiveStatus(policy.getEffectiveStatus())
                .attentionRequired(policy.isAttentionRequired())
                .renewable(policy.isRenewable())
                .calendarRegistered(
                        policy.getCalendarRegistered() != null
                                && policy.getCalendarRegistered())
                .build();
    }

    /**
     * LocalDate を "yyyy/MM/dd" 形式の文字列に変換する。
     * null の場合は空文字を返す。
     */
    private static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(FORMATTER);
    }
}
