package jp.yoshiaki.insuranceapp.training.day82.aisummary;

import java.util.List;

/**
 * レビュー要約・注意喚起の業務ロジック。
 *
 * 商品情報を取得 → レビューからプロンプトを組み立て → AiClientに投げる。
 * AiClientはinterfaceなので、Fake/本番を差し替え可能。
 */
// @Service("day82ReviewSummaryService")  ← Spring Boot利用時はこのBean名で登録
public class ReviewSummaryService {

    private final ProductRepository productRepository;
    private final AiClient aiClient;

    // ① コンストラクタ注入（依存をinterfaceで受け取る）
    public ReviewSummaryService(ProductRepository productRepository, AiClient aiClient) {
        this.productRepository = productRepository;
        this.aiClient = aiClient;
    }

    // ② 商品のレビューを要約する
    public String summarizeReviews(long productId) {
        // 商品を検索（見つからなければ例外）
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "商品が見つかりません（ID: " + productId + "）"));

        // レビューが0件なら要約不可
        List<String> reviews = product.getReviews();
        if (reviews.isEmpty()) {
            return "レビューがまだありません。要約するにはレビューを追加してください。";
        }

        // プロンプトを組み立てる（テンプレートを使用）
        String prompt = PromptTemplate.buildSummaryPrompt(product, reviews);

        // AIに問い合わせる（Fake or 本番）
        try {
            return aiClient.ask(prompt);
        } catch (AiApiException e) {
            // AI呼び出し失敗時はメッセージを付けて再スロー
            throw new AiApiException(
                    "商品ID=" + productId + " の要約取得に失敗しました: " + e.getMessage(), e);
        }
    }

    // ③ 商品のレビューから注意点を抽出する
    public String alertFromReviews(long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "商品が見つかりません（ID: " + productId + "）"));

        List<String> reviews = product.getReviews();
        if (reviews.isEmpty()) {
            return "レビューがまだありません。注意点を抽出するにはレビューを追加してください。";
        }

        // 注意喚起用のプロンプトを組み立てる
        String prompt = PromptTemplate.buildAlertPrompt(product, reviews);

        try {
            return aiClient.ask(prompt);
        } catch (AiApiException e) {
            throw new AiApiException(
                    "商品ID=" + productId + " の注意点取得に失敗しました: " + e.getMessage(), e);
        }
    }

    // ④ 商品を登録する
    public Product registerProduct(String name, String category, String description) {
        Product product = new Product(0, name, category, description);
        return productRepository.save(product);
    }

    // ⑤ 商品にレビューを追加する
    public void addReview(long productId, String review) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "商品が見つかりません（ID: " + productId + "）"));
        product.addReview(review);
    }

    // ⑥ 全商品を一覧で返す
    public List<Product> listAll() {
        return productRepository.findAll();
    }
}
