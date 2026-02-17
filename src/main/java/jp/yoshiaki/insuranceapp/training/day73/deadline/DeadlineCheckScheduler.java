package jp.yoshiaki.insuranceapp.training.day73.deadline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 期限チェックの定期実行クラス（Scheduler）
 *
 * このクラスの役割は「いつ実行するか」を決めること。
 * 「何をチェックするか」はServiceに委譲する。
 *
 * たとえ話：
 * - Scheduler = 「毎朝9時に見回りに行く警備員」
 * - Service   = 「実際にドアの施錠を確認する作業」
 * 警備員（Scheduler）は時間になったら作業（Service）を呼ぶだけ。
 */
@Profile("training")
@Component("day73DeadlineCheckScheduler")
public class DeadlineCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeadlineCheckScheduler.class);

    // ① 表示用の日時フォーマット
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ② Serviceへの依存（コンストラクタインジェクション）
    private final DeadlineService service;

    public DeadlineCheckScheduler(DeadlineService service) {
        this.service = service;
    }

    /**
     * 期限チェックを定期実行するメソッド
     *
     * @Scheduled(fixedRate = 10000)
     *   → アプリ起動後、10秒（10,000ミリ秒）ごとに自動実行される
     *   → ユーザーが何もしなくても勝手に動く
     *
     * initialDelay = 5000
     *   → 起動直後は5秒待ってから最初の実行を開始する
     *   → 起動直後にデータが無い状態で動くのを防ぐ
     */
    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    public void checkDeadlines() {
        String now = LocalDateTime.now().format(FORMATTER);
        log.info("===== 期限チェック開始 [{}] =====", now);

        // ① 期限切れタスクのチェック
        List<Deadline> overdueList = service.checkOverdue();
        if (overdueList.isEmpty()) {
            log.info("[OK] 期限切れタスクはありません");
        } else {
            for (Deadline d : overdueList) {
                log.warn("[期限切れ] ID={} タスク「{}」期限: {}（{}日超過）",
                        d.getId(),
                        d.getTitle(),
                        d.getDueDate(),
                        Math.abs(d.daysUntilDue()));
            }
        }

        // ② 期限間近タスクのチェック
        List<Deadline> dueSoonList = service.checkDueSoon();
        if (dueSoonList.isEmpty()) {
            log.info("[OK] 期限間近タスクはありません");
        } else {
            for (Deadline d : dueSoonList) {
                log.info("[期限間近] ID={} タスク「{}」期限: {}（残り{}日）",
                        d.getId(),
                        d.getTitle(),
                        d.getDueDate(),
                        d.daysUntilDue());
            }
        }

        log.info("===== 期限チェック完了 =====");
    }
}
