CREATE TABLE accident_memos (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    accident_id BIGINT       NOT NULL,
    handled_at  DATETIME     NOT NULL COMMENT '対応を行った日時',
    content     TEXT         NOT NULL COMMENT '対応内容',
    created_by  VARCHAR(100) NOT NULL COMMENT '登録者',
    updates_last_contacted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '最終対応日時の更新対象',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    KEY idx_accident_memos_accident_handled (accident_id, handled_at),

    CONSTRAINT fk_accident_memos_accident_id
        FOREIGN KEY (accident_id) REFERENCES accidents(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 既存の長文メモは失わず、移行時点で1件の履歴として取り込む。
INSERT INTO accident_memos (
    accident_id, handled_at, content, created_by, updates_last_contacted
)
SELECT id, COALESCE(last_contacted_at, updated_at), memo, '移行データ',
       last_contacted_at IS NOT NULL
FROM accidents
WHERE memo IS NOT NULL AND TRIM(memo) <> '';
