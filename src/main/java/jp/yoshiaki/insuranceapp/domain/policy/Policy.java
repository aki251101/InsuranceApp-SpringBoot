package jp.yoshiaki.insuranceapp.domain.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 証券（契約）
 * - コントローラ/検索で参照される「車両情報」「契約内容」を正式フィールドとして追加
 * - 更新/カレンダー登録など業務支援アプリで必要になる最小項目を保持
 */
@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String policyNumber;

    @Column(nullable = false, length = 80)
    private String customerName;

    /** 車両情報（例：プリウス / 型式 / ナンバー 等の要約） */
    @Column(length = 200)
    private String vehicleInfo;

    /** 契約内容（例：対人/対物/車両、免責、特約などの要約） */
    @Column(length = 400)
    private String contractContent;

    /** 更新満了（期限） */
    @Column(nullable = false)
    private LocalDate renewalDueEndDate;

    /** 更新済みフラグ（UIの「更新」可否などに利用） */
    @Column(nullable = false)
    private boolean renewable;

    /** 失効/有効など（一覧タブやフィルタに利用） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EffectiveStatus effectiveStatus = EffectiveStatus.ACTIVE;

    /** 更新実施日時（更新済み統計・早期更新判定に利用） */
    private LocalDateTime renewedAt;

    /** Googleカレンダー登録済みトグル */
    @Column(nullable = false)
    private boolean calendarRegistered;

    public enum EffectiveStatus {
        ACTIVE,
        LAPSED
    }
}
