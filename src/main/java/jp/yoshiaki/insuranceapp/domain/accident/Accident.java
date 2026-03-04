package jp.yoshiaki.insuranceapp.domain.accident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 事故（受付）情報
 * - AI次アクション提案、対応履歴（最終連絡日時）、メモなど業務支援に必要な最小項目を保持
 */
@Entity
@Table(name = "accidents")
@Getter
@Setter
@NoArgsConstructor
public class Accident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 事故発生日/受付日（運用に合わせて使い分け） */
    private LocalDateTime occurredAt;

    /** 発生場所 */
    @Column(length = 120)
    private String place;

    /** 事故概要 */
    @Column(length = 1000)
    private String description;

    /** 進捗ステータス（例：REPORTED/CONTACTING/WAITING/CLOSED 等） */
    @Column(nullable = false, length = 30)
    private String status = "REPORTED";

    /** 最終連絡日時 */
    private LocalDateTime lastContactedAt;

    /** 社内メモ */
    @Column(length = 2000)
    private String memo;

    public String getStatusLabel() {
        // UI表示用（必要に応じてEnum化/マスタ化推奨）
        return switch (status) {
            case "REPORTED" -> "受付";
            case "CONTACTING" -> "連絡中";
            case "WAITING" -> "保留";
            case "CLOSED" -> "完了";
            default -> status;
        };
    }
}
