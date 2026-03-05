package jp.yoshiaki.insuranceapp.dto;

import jp.yoshiaki.insuranceapp.entity.Accident;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

/**
 * 事故詳細レスポンスDTO
 *
 * 事故の詳細情報に加えて、画面上の操作ボタンの有効/無効を制御する
 * フラグ（canStartProgress, canResolve, canContact）を含む。
 *
 * 【操作可否フラグの設計意図】
 * ボタンの有効/無効判定をフロント（テンプレート/JS）に任せると、
 * ルール変更時に画面とServiceの両方を直す必要がある。
 * DTO にフラグを持たせれば、判定ロジックは Java 側に一元化できる。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccidentDetailResponse {

    private Long id;
    private Long policyId;
    private String policyNumber;     // 契約番号
    private String customerName;     // 契約者名
    private String occurredAt;       // 事故受付日（yyyy/MM/dd）
    private String place;            // 事故場所
    private String description;      // 事故概要
    private String status;           // ステータス（日本語ラベル）
    private String statusCode;       // ステータス（内部コード: OPEN等）
    private String lastContactedAt;  // 最終対応日時（yyyy/MM/dd HH:mm）
    private String memo;             // 対応メモ
    private boolean stagnant;        // 滞留フラグ

    // --- 操作可否フラグ ---
    /** 対応開始ボタンが有効か（OPEN の時のみ） */
    private boolean canStartProgress;
    /** 完了ボタンが有効か（IN_PROGRESS の時のみ） */
    private boolean canResolve;
    /** 「対応した」ボタンが有効か（OPEN または IN_PROGRESS の時） */
    private boolean canContact;

    /**
     * Entity → DTO 変換
     *
     * @param accident 事故エンティティ
     * @return 詳細表示用DTO
     */
    public static AccidentDetailResponse from(Accident accident) {
        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

        return AccidentDetailResponse.builder()
                .id(accident.getId())
                .policyId(accident.getPolicyId())
                // Policy が null の場合は空文字（データ不整合時の安全策）
                .policyNumber(accident.getPolicy() != null
                        ? accident.getPolicy().getPolicyNumber() : "")
                .customerName(accident.getPolicy() != null
                        ? accident.getPolicy().getCustomerName() : "")
                .occurredAt(accident.getOccurredAt().format(dateFormatter))
                .place(accident.getPlace())
                .description(accident.getDescription())
                .status(accident.getStatusLabel())
                .statusCode(accident.getStatus())
                .lastContactedAt(accident.getLastContactedAt() != null
                        ? accident.getLastContactedAt().format(dateTimeFormatter) : "")
                .memo(accident.getMemo() != null ? accident.getMemo() : "")
                .stagnant(accident.isStagnant())
                // 操作可否フラグ（ステータスに応じて判定）
                .canStartProgress("OPEN".equals(accident.getStatus()))
                .canResolve("IN_PROGRESS".equals(accident.getStatus()))
                .canContact("OPEN".equals(accident.getStatus())
                        || "IN_PROGRESS".equals(accident.getStatus()))
                .build();
    }
}
