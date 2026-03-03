-- ============================================================
-- Stub環境: 初期データ投入（H2メモリDB用）
-- ============================================================
-- 目的:
--   stub起動時に /policies と /policies/{id} を動作確認できるようにする
--
-- 注意:
--   created_at / updated_at は NOT NULL のため、SQL直投入では必ず値を入れる。
--   （JPAの @PrePersist/@PreUpdate は SQL 直投入では動かない）
-- ============================================================

INSERT INTO policies (
  policy_number,
  customer_name,
  start_date,
  end_date,
  status,
  renewal_due_end_date,
  renewed_at,
  cancelled_at,
  calendar_event_id,
  calendar_registered,
  created_at,
  updated_at
) VALUES
  -- 1) ACTIVE（操作確認用：満期日を近めに設定）
  ('P-2026-0001', '山田 太郎', DATE '2026-01-01', DATE '2026-04-15', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  -- 2) ACTIVEだが満期が過去 → getEffectiveStatus() 等の表示確認用
  ('P-2026-0002', '佐藤 花子', DATE '2025-01-01', DATE '2025-02-01', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  -- 3) CANCELLED（解約表示の確認用）
  ('P-2026-0003', '田中 一郎', DATE '2026-02-01', DATE '2026-12-31', 'CANCELLED',
   NULL, NULL, CURRENT_TIMESTAMP,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
