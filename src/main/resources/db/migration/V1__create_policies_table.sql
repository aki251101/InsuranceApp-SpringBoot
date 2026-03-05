-- 配置：src/main/resources/db/migration/V1__create_policies_table.sql
-- policies テーブル作成（ER図 v1.1 準拠：PostgreSQL想定→MySQLへ置換）
-- 参照：損保アプリ ER図（概念→物理） v1.1（2026-01-14）
-- ※ V2 accidents が policies(id) を参照します。

CREATE TABLE IF NOT EXISTS policies (
    id                 BIGINT       AUTO_INCREMENT PRIMARY KEY,
    policy_number       VARCHAR(32)  NOT NULL,
    customer_name       VARCHAR(100) NOT NULL,
    start_date          DATE         NOT NULL,
    end_date            DATE         NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    renewal_due_end_date DATE        NULL,
    renewed_at          DATETIME     NULL,
    cancelled_at        DATETIME     NULL,
    calendar_registered TINYINT(1)   NOT NULL DEFAULT 0,
    calendar_event_id   VARCHAR(128) NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uq_policies_policy_number (policy_number),
    KEY idx_policies_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- サンプルデータ（V2 の accidents が policy_id=1,2 を参照するため）
INSERT INTO policies (id, policy_number, customer_name, start_date, end_date, status, calendar_registered)
VALUES
  (1, 'P-2026-0001', '山田 太郎', '2025-04-01', '2026-04-01', 'ACTIVE', 0),
  (2, 'P-2026-0002', '佐藤 花子', '2025-05-15', '2026-05-15', 'ACTIVE', 1)
ON DUPLICATE KEY UPDATE
  policy_number=VALUES(policy_number),
  customer_name=VALUES(customer_name),
  start_date=VALUES(start_date),
  end_date=VALUES(end_date),
  status=VALUES(status),
  calendar_registered=VALUES(calendar_registered);
