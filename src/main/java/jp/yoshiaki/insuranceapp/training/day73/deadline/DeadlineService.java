package jp.yoshiaki.insuranceapp.training.day73.deadline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 締め切りタスクの業務ロジック
 *
 * Controller（REST API）と Scheduler（定期実行）の
 * どちらからも呼ばれる「処理の中心」。
 * データの取得・判定・登録はすべてここを経由する。
 */
@Service("day73DeadlineService")
public class DeadlineService {

    // ① ログ出力用（SLF4J）
    //    クラスごとに Logger を作るのが定番
    private static final Logger log = LoggerFactory.getLogger(DeadlineService.class);

    // ② Repository（データ保存窓口）
    private final DeadlineRepository repository;

    // ③ コンストラクタインジェクション（DIの推奨パターン）
    //    Springが InMemoryDeadlineRepository を自動で渡してくれる
    public DeadlineService(DeadlineRepository repository) {
        this.repository = repository;
    }

    /**
     * タスクを新規登録する
     *
     * @param title   タスクのタイトル
     * @param dueDate 締め切り日
     * @return 登録されたタスク（ID付き）
     */
    public Deadline create(String title, LocalDate dueDate) {
        Deadline deadline = new Deadline(title, dueDate);
        Deadline saved = repository.save(deadline);
        log.info("[登録] {} （期限: {}）", saved.getTitle(), saved.getDueDate());
        return saved;
    }

    /**
     * 全タスクを取得する
     *
     * @return タスクの全件リスト
     */
    public List<Deadline> findAll() {
        return repository.findAll();
    }

    /**
     * タスクを完了にする
     *
     * @param id タスクID
     * @return 完了にしたタスク
     * @throws NotFoundException IDが見つからない場合
     */
    public Deadline complete(Long id) {
        Deadline deadline = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "タスクが見つかりません（ID: " + id + "）"));

        deadline.complete();
        repository.save(deadline);
        log.info("[完了] {} （ID: {}）", deadline.getTitle(), deadline.getId());
        return deadline;
    }

    /**
     * 期限切れのタスクを検索する（定期チェック用）
     *
     * 未完了かつ締め切り日が今日より前のタスクを返す
     *
     * @return 期限切れタスクのリスト
     */
    public List<Deadline> checkOverdue() {
        return repository.findAll().stream()
                .filter(Deadline::isOverdue)
                .toList();
    }

    /**
     * 期限間近のタスクを検索する（定期チェック用）
     *
     * 未完了かつ締め切り日が今日〜3日後以内のタスクを返す
     *
     * @return 期限間近タスクのリスト
     */
    public List<Deadline> checkDueSoon() {
        return repository.findAll().stream()
                .filter(Deadline::isDueSoon)
                .toList();
    }
}
