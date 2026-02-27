package jp.yoshiaki.insuranceapp.training.day88.deploy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DeployCheckRepositoryのメモリ実装。
 * LinkedHashMapで順序を保持しつつ、IDで高速に検索できるようにしている。
 */
public class InMemoryDeployCheckRepository implements DeployCheckRepository {

    private final Map<Integer, DeployCheckItem> store = new LinkedHashMap<>();

    /** コンストラクタ：AWSデプロイ用の初期チェック項目を投入 */
    public InMemoryDeployCheckRepository() {
        // --- サーバー設定 ---
        addItem(new DeployCheckItem(1, "EC2/Lightsailインスタンス作成", CheckCategory.SERVER));
        addItem(new DeployCheckItem(2, "SSHキーペア設定", CheckCategory.SERVER));

        // --- ネットワーク設定 ---
        addItem(new DeployCheckItem(3, "セキュリティグループ：ポート22(SSH)許可", CheckCategory.NETWORK));
        addItem(new DeployCheckItem(4, "セキュリティグループ：ポート8080(アプリ)許可", CheckCategory.NETWORK));
        addItem(new DeployCheckItem(5, "セキュリティグループ：ポート443(HTTPS)許可", CheckCategory.NETWORK));

        // --- アプリ設定 ---
        addItem(new DeployCheckItem(6, "JARファイルのビルド", CheckCategory.APP));
        addItem(new DeployCheckItem(7, "環境変数（DB接続先・APIキー）設定", CheckCategory.APP));
        addItem(new DeployCheckItem(8, "application.ymlのプロファイル切替", CheckCategory.APP));
    }

    /** 内部ヘルパー：項目をMapに追加する */
    private void addItem(DeployCheckItem item) {
        store.put(item.getId(), item);
    }

    @Override
    public List<DeployCheckItem> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<DeployCheckItem> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<DeployCheckItem> findByCategory(CheckCategory category) {
        return store.values().stream()
                .filter(item -> item.getCategory() == category)
                .collect(Collectors.toList());
    }
}
