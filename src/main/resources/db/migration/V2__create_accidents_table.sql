-- 配置：src/main/resources/db/migration/V2__create_accidents_table.sql
-- accidents テーブル作成（ER図 v1.1 準拠：PostgreSQL想定→MySQLへ置換）
-- ※ policies テーブルが先に存在していること（V1で作成済み）

CREATE TABLE IF NOT EXISTS accidents (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    policy_id         BIGINT       NOT NULL,
    occurred_at       DATE         NOT NULL          COMMENT '事故受付日（事故発生日として扱う）',
    place             VARCHAR(200)                  COMMENT '事故場所（任意）',
    description       TEXT                          COMMENT '事故内容（任意）',
    status            VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT '状態: OPEN/IN_PROGRESS/RESOLVED',
    last_contacted_at DATETIME                       COMMENT '最終対応日時（滞留判定用）',
    memo              TEXT         NOT NULL          COMMENT '対応履歴メモ（追記式）',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    KEY idx_accidents_occurred_at (occurred_at),

    CONSTRAINT fk_accidents_policy_id
        FOREIGN KEY (policy_id) REFERENCES policies(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- サンプルデータ投入（動作確認用）
-- ※ policies テーブルに id=1, id=2 のデータが存在する前提（V1で投入済み）
INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo)
VALUES
    (1, CURDATE(), '熊本市中央区 国道3号線', '信号待ちで追突された', 'OPEN', NULL, ''),
    (1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '熊本市東区 県道沿い', '駐車場内で接触', 'IN_PROGRESS', NOW(), '修理工場に連絡済み'),
    (2, DATE_SUB(CURDATE(), INTERVAL 20 DAY), '熊本市西区 商業施設駐車場', '落下物による損傷', 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 10 DAY), 'お客様に連絡中'),
    (2, DATE_SUB(CURDATE(), INTERVAL 30 DAY), '熊本市南区 住宅街', '自損事故（電柱）', 'RESOLVED', DATE_SUB(NOW(), INTERVAL 25 DAY), '保険金支払い完了');
