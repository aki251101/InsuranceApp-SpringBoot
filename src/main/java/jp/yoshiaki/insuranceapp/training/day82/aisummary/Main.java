package jp.yoshiaki.insuranceapp.training.day82.aisummary;

import java.util.List;
import java.util.Scanner;

/**
 * コンソールアプリのエントリーポイント。
 *
 * 商品登録・レビュー追加・AI要約・AI注意点のコマンドを解釈する。
 * AiClientはFakeAiClientを注入（開発用：固定文を返す）。
 */
public class Main {

    public static void main(String[] args) {
        // ① 依存の組み立て（本番ではDIコンテナが行う）
        ProductRepository repository = new InMemoryProductRepository();
        AiClient aiClient = new FakeAiClient();  // ← 本番ではGeminiAiClientに差し替え
        ReviewSummaryService service = new ReviewSummaryService(repository, aiClient);

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== 商品レビューAI要約アプリ（Day82） ===");
        System.out.println("操作を入力してください（helpで一覧、exitで終了）");
        System.out.println();

        // ② サンプルデータ投入（動作確認用）
        setupSampleData(service);

        // ③ コマンドループ
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "help":
                        printHelp();
                        break;

                    case "list":
                        handleList(service);
                        break;

                    case "add":
                        handleAdd(scanner, service);
                        break;

                    case "review":
                        handleReview(parts, scanner, service);
                        break;

                    case "summary":
                        handleSummary(parts, service);
                        break;

                    case "alert":
                        handleAlert(parts, service);
                        break;

                    case "prompt":
                        handleShowPrompt(parts, service);
                        break;

                    case "exit":
                        System.out.println("アプリを終了します。お疲れさまでした！");
                        scanner.close();
                        return;

                    default:
                        System.out.println("不明なコマンドです。helpで一覧を確認してください。");
                        break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("エラー: " + e.getMessage());
            } catch (AiApiException e) {
                System.out.println("AI連携エラー: " + e.getMessage());
            }

            System.out.println();
        }
    }

    // ④ ヘルプ表示
    private static void printHelp() {
        System.out.println("【コマンド一覧】");
        System.out.println("  list            … 商品一覧を表示");
        System.out.println("  add             … 商品を新規登録（対話形式）");
        System.out.println("  review <ID>     … 指定商品にレビューを追加");
        System.out.println("  summary <ID>    … 指定商品のレビューをAIで要約");
        System.out.println("  alert <ID>      … 指定商品のレビューからAIで注意点を抽出");
        System.out.println("  prompt <ID>     … 要約用プロンプトの中身を確認（学習用）");
        System.out.println("  help            … このヘルプを表示");
        System.out.println("  exit            … アプリを終了");
    }

    // ⑤ 商品一覧
    private static void handleList(ReviewSummaryService service) {
        List<Product> products = service.listAll();
        if (products.isEmpty()) {
            System.out.println("商品が登録されていません。");
            return;
        }
        System.out.println("【商品一覧】");
        for (Product p : products) {
            System.out.println("  " + p);
        }
    }

    // ⑥ 商品追加（対話形式）
    private static void handleAdd(Scanner scanner, ReviewSummaryService service) {
        System.out.print("商品名: ");
        String name = scanner.nextLine().trim();
        System.out.print("カテゴリ: ");
        String category = scanner.nextLine().trim();
        System.out.print("商品説明: ");
        String description = scanner.nextLine().trim();

        Product saved = service.registerProduct(name, category, description);
        System.out.println("登録しました: " + saved);
    }

    // ⑦ レビュー追加
    private static void handleReview(String[] parts, Scanner scanner,
                                     ReviewSummaryService service) {
        if (parts.length < 2) {
            System.out.println("使い方: review <商品ID>");
            return;
        }
        long id = Long.parseLong(parts[1]);
        System.out.print("レビュー内容: ");
        String review = scanner.nextLine().trim();
        service.addReview(id, review);
        System.out.println("レビューを追加しました（商品ID: " + id + "）");
    }

    // ⑧ AI要約
    private static void handleSummary(String[] parts, ReviewSummaryService service) {
        if (parts.length < 2) {
            System.out.println("使い方: summary <商品ID>");
            return;
        }
        long id = Long.parseLong(parts[1]);
        System.out.println("AIに要約を問い合わせ中...");
        String result = service.summarizeReviews(id);
        System.out.println();
        System.out.println(result);
    }

    // ⑨ AI注意点
    private static void handleAlert(String[] parts, ReviewSummaryService service) {
        if (parts.length < 2) {
            System.out.println("使い方: alert <商品ID>");
            return;
        }
        long id = Long.parseLong(parts[1]);
        System.out.println("AIに注意点を問い合わせ中...");
        String result = service.alertFromReviews(id);
        System.out.println();
        System.out.println(result);
    }

    // ⑩ プロンプト確認（学習用：AIに送るプロンプトの中身を見る）
    private static void handleShowPrompt(String[] parts, ReviewSummaryService service) {
        if (parts.length < 2) {
            System.out.println("使い方: prompt <商品ID>");
            return;
        }
        long id = Long.parseLong(parts[1]);

        // Serviceを経由せず、直接プロンプトの中身を確認する（学習目的）
        // 本来はServiceの内部処理だが、プロンプト設計を学ぶために公開
        List<Product> all = service.listAll();
        Product target = null;
        for (Product p : all) {
            if (p.getId() == id) {
                target = p;
                break;
            }
        }
        if (target == null) {
            System.out.println("商品が見つかりません（ID: " + id + "）");
            return;
        }
        if (target.getReviews().isEmpty()) {
            System.out.println("レビューがまだありません。");
            return;
        }

        String prompt = PromptTemplate.buildSummaryPrompt(target, target.getReviews());
        System.out.println("【AIに送るプロンプト（要約用）の中身】");
        System.out.println("────────────────────────────────");
        System.out.println(prompt);
        System.out.println("────────────────────────────────");
        System.out.println("※このプロンプトがAI（Gemini等）に送られ、応答が返ってきます。");
    }

    // ⑪ サンプルデータ投入
    private static void setupSampleData(ReviewSummaryService service) {
        // 商品1: ワイヤレスイヤホン
        Product p1 = service.registerProduct(
                "ワイヤレスイヤホン Pro", "家電", "ノイズキャンセリング搭載の高音質イヤホン");
        service.addReview(p1.getId(), "音質がとても良く、ノイキャンも効果抜群です。買ってよかった！");
        service.addReview(p1.getId(), "装着感が少しきつい。長時間使うと耳が痛くなる。");
        service.addReview(p1.getId(), "バッテリーの持ちが良い。通勤の往復で十分使えます。");

        // 商品2: プログラミング入門書
        Product p2 = service.registerProduct(
                "Java入門 実践ガイド", "書籍", "初心者向けJavaプログラミング教本");
        service.addReview(p2.getId(), "図が多くてわかりやすい。初心者の私でも理解できました。");
        service.addReview(p2.getId(), "演習問題の解答がWebにしかなく、オフラインで確認できないのが不便。");

        System.out.println("サンプルデータを登録しました（商品2件、レビュー計5件）");
        System.out.println();
    }
}
