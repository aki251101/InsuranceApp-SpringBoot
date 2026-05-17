-- ============================================================
-- デモ環境リセットSQL
-- ファイル: deliverables/reset-demo.sql
-- 用途: デモ前に契約・事故データを初期状態へ戻す
-- ============================================================

USE insuranceapp;

-- 参照制約のため、事故 -> 契約の順に削除
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE accidents;
TRUNCATE TABLE policies;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- policies 初期データ
-- ------------------------------------------------------------
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
  ('P-2026-0001', '山田 太郎', DATE '2026-01-01', DATE '2026-04-15', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0002', '佐藤 花子', DATE '2025-01-01', DATE '2025-02-01', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0003', '田中 一郎', DATE '2026-02-01', DATE '2026-12-31', 'CANCELLED',
   NULL, NULL, CURRENT_TIMESTAMP,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0101', '青木 翔太', DATE '2025-07-21', DATE '2026-06-01', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0102', '石田 美咲', DATE '2025-06-21', DATE '2026-06-26', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0103', '上田 恒一', DATE '2025-05-02', DATE '2026-05-07', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0104', '遠藤 里奈', DATE '2025-10-29', DATE '2026-09-26', 'CANCELLED',
   NULL, NULL, CURRENT_TIMESTAMP,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0105', '小川 健太', DATE '2025-05-17', DATE '2026-05-22', 'ACTIVE',
   DATE '2026-05-22', TIMESTAMP '2026-04-27 10:00:00', NULL,
   NULL, TRUE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0106', '加藤 優子', DATE '2025-08-31', DATE '2026-07-31', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0107', '木村 大輔', DATE '2025-05-27', DATE '2026-05-18', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0108', '斎藤 直樹', DATE '2025-11-18', DATE '2026-11-14', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ------------------------------------------------------------
-- accidents 初期データ
-- ------------------------------------------------------------
INSERT INTO accidents (
  policy_id,
  occurred_at,
  place,
  description,
  status,
  last_contacted_at,
  memo,
  created_at,
  updated_at
)
SELECT p.id, DATE '2026-03-10', '大阪市北区梅田1丁目', '追突事故', 'IN_PROGRESS', TIMESTAMP '2026-03-12 10:00:00', '初回連絡完了。保険会社へ報告済み。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0001';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, DATE '2026-03-05', '京都市中京区', '自損事故', 'IN_PROGRESS', TIMESTAMP '2026-03-09 09:00:00', '修理工場と連絡調整中。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0002';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, DATE '2026-02-18', '神戸市中央区三宮町', '接触事故', 'OPEN', NULL, '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0002';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, DATE '2026-01-20', '大阪市天王寺区', '車両破損', 'RESOLVED', TIMESTAMP '2026-01-25 13:00:00', '示談成立。保険金支払い完了。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0003';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, DATE '2026-04-15', '福岡市博多区', '追突事故', 'IN_PROGRESS', TIMESTAMP '2026-04-25 09:00:00', '相手方保険会社へ連絡済み。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0101';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, DATE '2026-05-14', '熊本市中央区', '物損事故', 'OPEN', NULL, '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0102';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, DATE '2026-04-17', '千葉市中央区', '車庫内接触', 'IN_PROGRESS', TIMESTAMP '2026-05-03 12:00:00', '修理見積もり待ち。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0104';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, DATE '2026-04-01', '札幌市中央区', '人身事故', 'RESOLVED', TIMESTAMP '2026-04-06 14:00:00', '保険金支払い完了。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0107';
