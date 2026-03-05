package jp.yoshiaki.insuranceapp.dto;

import jp.yoshiaki.insuranceapp.entity.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 契約詳細画面の表示用DTO
 *
 * 【Day92で追加】
 * - canRenew / canUnrenew / canCancel / canUncancel の4フラグ
 * - status は effectiveStatus（契約中/解約/失効）を使用
 *
 * なぜDTOに「操作可否フラグ」を持たせるのか：
 *   - 画面（Thymeleaf）側で「このボタンを表示するか」を判定する必要がある
 *   - 判定ロジックをテンプレートに書くと複雑になるため、
 *     DTO変換時にService/Entity側で計算し、booleanフラグとして渡す
 *   - 画面テンプレートは「trueなら表示」とだけ書けばよい（責務分離）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyDetailResponse {

    private Long id;
    private String policyNumber;
    private String customerName;
    private String startDate;
    private String endDate;

    /** 表示用ステータス（契約中 / 解約 / 失効） */
    private String status;

    /** 要注意フラグ（満期20日前〜満期日） */
    private boolean attentionRequired;

    /** カレンダー登録済みフラグ */
    private boolean calendarRegistered;

    // ─── Day92で追加：操作可否フラグ ───

    /** 更新ボタンを表示するか（更新可能期間内 かつ ACTIVE） */
    private boolean canRenew;

    /** 更新取消ボタンを表示するか（更新済み かつ 当日） */
    private boolean canUnrenew;

    /** 解約ボタンを表示するか（effectiveStatus が 契約中） */
    private boolean canCancel;

    /** 解約取消ボタンを表示するか（解約済み かつ 当日） */
    private boolean canUncancel;

    /**
     * Entity → DTO 変換
     *
     * ここで各操作の可否を計算してフラグに設定する。
     * 画面テンプレートは「canRenew が true なら更新ボタンを表示」
     * とだけ書けばよく、判定ロジックを知る必要がない。
     *
     * @param policy 契約Entity
     * @return 画面表示用DTO
     */
    public static PolicyDetailResponse from(Policy policy) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate today = LocalDate.now();

        // 更新取消の可否：renewedAtが記録されていて、当日であり、旧満期日が退避されている
        boolean canUnrenew = policy.getRenewedAt() != null
                && policy.getRenewedAt().toLocalDate().equals(today)
                && policy.getRenewalDueEndDate() != null;

        // 解約取消の可否：cancelledAtが記録されていて、当日である
        boolean canUncancel = policy.getCancelledAt() != null
                && policy.getCancelledAt().toLocalDate().equals(today);

        return PolicyDetailResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .customerName(policy.getCustomerName())
                .startDate(policy.getStartDate().format(formatter))
                .endDate(policy.getEndDate().format(formatter))
                // effectiveStatus を使用（DB値ではなく表示用の3値）
                .status(policy.getEffectiveStatus())
                .attentionRequired(policy.isAttentionRequired())
                .calendarRegistered(policy.getCalendarRegistered())
                // 操作可否フラグ
                .canRenew(policy.isRenewable())
                .canUnrenew(canUnrenew)
                .canCancel("契約中".equals(policy.getEffectiveStatus()))
                .canUncancel(canUncancel)
                .build();
    }
}
