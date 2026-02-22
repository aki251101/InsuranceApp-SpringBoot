package jp.yoshiaki.insuranceapp.training.day83.ai;

import java.util.Scanner;

/**
 * AI要約サービスのコンソールアプリ（エントリーポイント）。
 *
 * 【このクラスの責務】
 * - ユーザーからのコマンド入力を受け付ける
 * - コマンドに応じてAiSummaryServiceに処理を委譲する
 * - 結果やエラーメッセージを表示する
 *
 * 【ポイント】
 * - MainがFakeAiClientを生成してServiceに渡す（＝DIの手動版）
 * - 本番切り替え時は、ここのnew FakeAiClient()をnew GeminiAiClient()に変えるだけ
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ① 依存オブジェクトの組み立て（手動DI）
        // ここでFakeを本番に差し替えれば、他のコードは変更不要
        AiClient aiClient = new FakeAiClient();
        PromptBuilder promptBuilder = new PromptBuilder();
        AiSummaryService service = new AiSummaryService(aiClient, promptBuilder);

        System.out.println("=== AI要約サービス（Fake版） ===");
        System.out.println("操作を入力してください（helpで一覧、exitで終了）");
        System.out.println();

        // ② メインループ（コマンド受付）
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            // 空入力はスキップ
            if (line.isEmpty()) {
                continue;
            }

            // コマンドとテキストを分離（最初のスペースで分割）
            String command;
            String text;
            int spaceIndex = line.indexOf(' ');
            if (spaceIndex == -1) {
                command = line;
                text = "";
            } else {
                command = line.substring(0, spaceIndex);
                text = line.substring(spaceIndex + 1).trim();
            }

            // ③ コマンド振り分け
            switch (command) {
                case "summarize":
                    handleSummarize(service, text);
                    break;
                case "analyzeRisk":
                    handleAnalyzeRisk(service, text);
                    break;
                case "error":
                    handleError(service);
                    break;
                case "prompt":
                    handleShowPrompt(service, text);
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    System.out.println("終了します。");
                    scanner.close();
                    return;
                default:
                    System.out.println("[エラー] 不明なコマンドです: " + command);
                    System.out.println("helpで使い方を確認してください。");
                    break;
            }

            System.out.println();
        }
    }

    /**
     * 要約コマンドの処理。
     * AiClientExceptionをcatchして、ユーザーにエラーメッセージを表示する。
     */
    private static void handleSummarize(AiSummaryService service, String text) {
        if (text.isEmpty()) {
            System.out.println("[エラー] テキストを入力してください。例: summarize 契約者は田中太郎です");
            return;
        }
        try {
            String result = service.summarize(text);
            System.out.println("[要約結果]");
            System.out.println(result);
        } catch (AiClientException e) {
            System.out.println("[AI呼び出しエラー] " + e.getMessage());
        }
    }

    /**
     * リスク分析コマンドの処理。
     */
    private static void handleAnalyzeRisk(AiSummaryService service, String text) {
        if (text.isEmpty()) {
            System.out.println("[エラー] テキストを入力してください。例: analyzeRisk 納期が迫っている案件です");
            return;
        }
        try {
            String result = service.analyzeRisk(text);
            System.out.println("[リスク分析結果]");
            System.out.println(result);
        } catch (AiClientException e) {
            System.out.println("[AI呼び出しエラー] " + e.getMessage());
        }
    }

    /**
     * エラー体験コマンドの処理。
     * "__ERROR__" をトリガーにFakeが例外を投げる。
     */
    private static void handleError(AiSummaryService service) {
        System.out.println("--- AI障害シミュレーション ---");
        try {
            service.summarize("__ERROR__");
            // ここには到達しない（例外が発生するため）
            System.out.println("（想定外：エラーが発生しませんでした）");
        } catch (AiClientException e) {
            System.out.println("[AI呼び出しエラー] " + e.getMessage());
            System.out.println("→ このように、AI障害時はAiClientExceptionでキャッチして安全にエラー表示できます。");
        }
    }

    /**
     * プロンプト確認コマンドの処理（AI呼び出しなし）。
     */
    private static void handleShowPrompt(AiSummaryService service, String text) {
        if (text.isEmpty()) {
            System.out.println("[エラー] テキストを入力してください。例: prompt 契約者は田中太郎です");
            return;
        }
        String prompt = service.showPrompt(text);
        System.out.println("[プロンプト確認（実際にAIに送る文章）]");
        System.out.println("----------");
        System.out.println(prompt);
        System.out.println("----------");
    }

    /**
     * ヘルプ表示。
     */
    private static void printHelp() {
        System.out.println("--- 使い方 ---");
        System.out.println("  summarize <テキスト>     : テキストを要約します");
        System.out.println("  analyzeRisk <テキスト>   : テキストのリスクを分析します");
        System.out.println("  error                    : AI障害のシミュレーションを体験します");
        System.out.println("  prompt <テキスト>        : AIに送るプロンプト文を確認します（AI呼び出しなし）");
        System.out.println("  help                     : この一覧を表示します");
        System.out.println("  exit                     : 終了します");
    }
}
