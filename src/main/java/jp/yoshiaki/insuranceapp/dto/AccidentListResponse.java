package jp.yoshiaki.insuranceapp.dto;

import jp.yoshiaki.insuranceapp.entity.Accident;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 事故一覧レスポンスDTO
 *
 * Entity（Accident）を直接返さず、画面表示に必要な情報だけを
 * フラットな形に変換して返す。
 *
 * 【なぜDTOを使うのか】
 * - Entity を直接返すと、不要な内部フィールド（createdAt, updatedAt 等）まで露出する
 * - @ManyToOne の Policy が LAZY のため、JSON変換時に LazyInitializationException が起きる
 * - 日付フォーマット等の「表示都合の変換」を Entity に書かない（Entity はDBの構造だけ知ればよい）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccidentListResponse {

    /** 事故一覧の項目リスト */
    private List<AccidentItem> items;

    /**
     * 一覧の1行分のデータ
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccidentItem {
        private Long id;
        private String occurredAt;      // 事故受付日（yyyy/MM/dd形式）
        private String policyNumber;    // 契約番号（Policy経由）
        private String customerName;    // 契約者名（Policy経由）
        private String status;          // ステータス（日本語ラベル）
        private boolean stagnant;       // 滞留フラグ

        /**
         * Entity → DTO 変換
         *
         * Policy が null の場合（データ不整合時）は空文字で安全に処理する
         */
        public static AccidentItem from(Accident accident) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            return AccidentItem.builder()
                    .id(accident.getId())
                    .occurredAt(accident.getOccurredAt().format(formatter))
                    .policyNumber(accident.getPolicy() != null
                            ? accident.getPolicy().getPolicyNumber() : "")
                    .customerName(accident.getPolicy() != null
                            ? accident.getPolicy().getCustomerName() : "")
                    .status(accident.getStatusLabel())
                    .stagnant(accident.isStagnant())
                    .build();
        }
    }

    /**
     * Entity リスト → DTO 変換（一覧全体）
     *
     * @param accidents 事故エンティティのリスト
     * @return 一覧表示用DTO
     */
    public static AccidentListResponse from(List<Accident> accidents) {
        List<AccidentItem> items = accidents.stream()
                .map(AccidentItem::from)
                .collect(Collectors.toList());

        return AccidentListResponse.builder()
                .items(items)
                .build();
    }
}
