package jp.yoshiaki.insuranceapp.training.day88.deploy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * デプロイチェックの業務ロジックを担当するサービスクラス。
 * 一覧取得、状態更新、デプロイ判定を行う。
 */
public class DeployCheckService {

    private final DeployCheckRepository repository;

    public DeployCheckService(DeployCheckRepository repository) {
        this.repository = repository;
    }

    // --- 一覧取得 ---

    /** 全項目を返す */
    public List<DeployCheckItem> listAll() {
        return repository.findAll();
    }

    /** カテゴリで絞り込んだ一覧を返す */
    public List<DeployCheckItem> listByCategory(CheckCategory category) {
        return repository.findByCategory(category);
    }

    /** 状態で絞り込んだ一覧を返す */
    public List<DeployCheckItem> listByStatus(CheckStatus status) {
        return repository.findAll().stream()
                .filter(item -> item.getStatus() == status)
                .collect(Collectors.toList());
    }

    // --- 状態更新 ---

    /**
     * 指定IDの項目を「確認済み（OK）」にする。
     *
     * @param id チェック項目のID
     * @return 更新後のDeployCheckItem
     * @throws IllegalArgumentException IDが存在しない場合
     */
    public DeployCheckItem markAsOk(int id) {
        DeployCheckItem item = findByIdOrThrow(id);
        item.markOk();
        return item;
    }

    /**
     * 指定IDの項目を「未確認（NG）」にリセットする。
     *
     * @param id チェック項目のID
     * @return 更新後のDeployCheckItem
     * @throws IllegalArgumentException IDが存在しない場合
     */
    public DeployCheckItem markAsNg(int id) {
        DeployCheckItem item = findByIdOrThrow(id);
        item.markNg();
        return item;
    }

    // --- デプロイ判定 ---

    /**
     * 全項目がOKかどうかを判定し、結果を返す。
     * 1つでもNGがあれば「公開不可」、全てOKなら「公開可能」。
     *
     * @return JudgeResult（合否 + NG項目リスト）
     */
    public JudgeResult judge() {
        List<DeployCheckItem> allItems = repository.findAll();

        // NG項目を抽出
        List<DeployCheckItem> ngItems = allItems.stream()
                .filter(item -> !item.isOk())
                .collect(Collectors.toList());

        // 全項目OKなら公開可能
        boolean deployable = ngItems.isEmpty();

        return new JudgeResult(deployable, ngItems);
    }

    // --- 内部ヘルパー ---

    /**
     * IDで項目を検索し、見つからなければ例外をthrowする。
     * 原因が追えるよう、メッセージにIDを含める。
     */
    private DeployCheckItem findByIdOrThrow(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ID=" + id + " のチェック項目は存在しません。"
                ));
    }
}
