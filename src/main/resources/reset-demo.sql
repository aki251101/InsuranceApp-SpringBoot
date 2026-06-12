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
-- 前提:
-- - 自動車保険は start_date からちょうど1年後を end_date とする
-- - 2026-05-25 に更新した想定で:
--   - 早期更改（満期21日以上前）になる契約: 2件
--   - 早期更改にならず要注意（満期20日前〜）になる契約: 3件
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
  ('P-2026-0001', '山田 太郎', '2025-04-01', '2027-04-01', 'ACTIVE',
   '2026-04-01', '2026-03-15 10:00:00', NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0002', '佐藤 花子', '2025-07-15', '2026-07-15', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0003', '田中 一郎', '2026-01-01', '2027-01-01', 'CANCELLED',
   NULL, NULL, CURRENT_TIMESTAMP,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0101', '青木 翔太', '2025-06-20', '2027-06-20', 'ACTIVE',
   '2026-06-20', '2026-05-20 10:00:00', NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0102', '石田 美咲', '2025-06-30', '2026-06-30', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0103', '上田 恒一', '2025-05-30', '2027-05-30', 'ACTIVE',
   '2026-05-30', '2026-05-05 15:00:00', NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0104', '遠藤 里奈', '2025-09-26', '2026-09-26', 'CANCELLED',
   NULL, NULL, CURRENT_TIMESTAMP,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0105', '小川 健太', '2025-05-22', '2027-05-22', 'ACTIVE',
   '2026-05-22', '2026-04-27 10:00:00', NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0106', '加藤 優子', '2025-05-30', '2026-05-30', 'ACTIVE',
   NULL, NULL, NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0107', '木村 大輔', '2024-06-22', '2026-06-22', 'ACTIVE',
   '2025-06-22', '2025-06-15 13:00:00', NULL,
   NULL, FALSE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('P-2026-0108', '斎藤 直樹', '2025-08-12', '2026-08-12', 'ACTIVE',
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
SELECT p.id, '2026-06-03', '大阪市北区梅田1丁目', '追突事故', 'IN_PROGRESS',
       '2026-06-03 13:00:00',
       CONCAT('26.06.03 13:00 相手方へ連絡し、今後の補償の流れを説明。',
                CHAR(10),
                '26.06.03 17:00 修理工場へ車を渡し、見積依頼。'),
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0001';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, '2026-05-15', '京都市中京区', '自損事故', 'IN_PROGRESS',
       '2026-06-11 09:00:00',
       CONCAT('26.05.15 16:00 修理工場へ車を渡し、見積依頼。',
              CHAR(10),
              '2026-06-11 09:00:00 修理工場に修理状況確認連絡。来週納車予定。'),
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0002';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, '2026-01-20', '大阪市天王寺区', '車両破損', 'RESOLVED',
       '2026-02-25 13:00:00', '26.02.25 13:00 示談成立。保険金支払い完了。',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0003';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, '2026-06-11', '福岡市博多区', '追突事故', 'IN_PROGRESS',
       '2026-06-12 10:00:00', '26.06.12 10:00 相手方保険会社へ連絡済み。',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0101';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, '2026-06-12', '熊本市中央区', '物損事故', 'OPEN',
       NULL, '',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0102';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, '2026-05-17', '千葉市中央区', '車庫内接触', 'IN_PROGRESS',
       '2026-06-01 13:00:00',
       CONCAT('26.05.16 10:00 修理工場へ車を渡し、見積依頼。',
              CHAR(10),
              '26.06.01 13:00 修理見積もり待ち。送付依頼再度連絡済'),
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0104';

INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
SELECT p.id, '2026-04-01', '札幌市中央区', '人身事故', 'RESOLVED',
       '2026-06-06 14:00:00',
       CONCAT('26.04.01 11:00 相手方と病院へ連絡し、今後の補償の流れを説明。',
              CHAR(10),
              '26.06.06 14:00 保険金支払い完了。'),
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM policies p WHERE p.policy_number = 'P-2026-0107';
