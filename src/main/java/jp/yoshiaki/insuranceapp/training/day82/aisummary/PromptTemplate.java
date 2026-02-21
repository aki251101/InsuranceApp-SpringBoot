package jp.yoshiaki.insuranceapp.training.day82.aisummary;

import java.util.List;

/**
 * プロンプト（AIへの指示文）の雛形を管理するクラス。
 *
 * ポイント：プロンプトを毎回手書きすると品質がバラつく。
 * テンプレート化することで「入力が変わっても指示の形式は統一」できる。
 * → AIの応答品質が安定する。
 */
public class PromptTemplate {

    // ① 要約用プロンプトを組み立てる
    public static String buildSummaryPrompt(Product product, List<String> reviews) {
        StringBuilder sb = new StringBuilder();

        // ② AIへの役割指定（ロール設定）
        sb.append("あなたは商品レビューの分析専門家です。\n");
        sb.append("以下の商品情報とレビューを読み、2〜3文で簡潔に要約してください。\n\n");

        // ③ 商品情報セクション（変数部分）
        sb.append("【商品情報】\n");
        sb.append("商品名: ").append(product.getName()).append("\n");
        sb.append("カテゴリ: ").append(product.getCategory()).append("\n");
        sb.append("説明: ").append(product.getDescription()).append("\n\n");

        // ④ レビュー一覧セクション（変数部分）
        sb.append("【レビュー一覧】\n");
        for (int i = 0; i < reviews.size(); i++) {
            sb.append("レビュー").append(i + 1).append(": ").append(reviews.get(i)).append("\n");
        }
        sb.append("\n");

        // ⑤ 出力形式の指定（AIに「どう答えてほしいか」を明示）
        sb.append("【出力形式】\n");
        sb.append("要約（2〜3文）:\n");

        return sb.toString();
    }

    // ⑥ 注意喚起用プロンプトを組み立てる
    public static String buildAlertPrompt(Product product, List<String> reviews) {
        StringBuilder sb = new StringBuilder();

        sb.append("あなたは商品品質の分析専門家です。\n");
        sb.append("以下の商品情報とレビューを読み、注意すべき点を箇条書きで3つ挙げてください。\n\n");

        sb.append("【商品情報】\n");
        sb.append("商品名: ").append(product.getName()).append("\n");
        sb.append("カテゴリ: ").append(product.getCategory()).append("\n\n");

        sb.append("【レビュー一覧】\n");
        for (int i = 0; i < reviews.size(); i++) {
            sb.append("レビュー").append(i + 1).append(": ").append(reviews.get(i)).append("\n");
        }
        sb.append("\n");

        sb.append("【出力形式】\n");
        sb.append("注意点（箇条書き3つ）:\n");

        return sb.toString();
    }
}
