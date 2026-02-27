package jp.yoshiaki.insuranceapp.training.day88.deploy;

import java.util.List;
import java.util.Scanner;

/**
 * デプロイ判定チェックリストアプリのエントリーポイント。
 * ユーザーからコマンドを受け取り、チェック項目の管理・デプロイ判定を行う。
 */
public class Main {

    public static void main(String[] args) {
        // ① 依存関係の組み立て（Repository → Service）
        DeployCheckRepository repository = new InMemoryDeployCheckRepository();
        DeployCheckService service = new DeployCheckService(repository);

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== デプロイ判定チェックリスト（AWS公開用） ===");
        System.out.println("操作を入力してください（helpで一覧、exitで終了）");
        System.out.println();

        // ② メインループ：ユーザー入力を受け取り、コマンドに応じて処理する
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // コマンドと引数を分割（例："check 3" → command="check", arg="3"）
            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String arg = (parts.length > 1) ? parts[1].trim() : "";

            try {
                switch (command) {
                    case "list":
                        handleList(service, arg);
                        break;
                    case "check":
                        handleCheck(service, arg);
                        break;
                    case "uncheck":
                        handleUncheck(service, arg);
                        break;
                    case "judge":
                        handleJudge(service);
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                        System.out.println("終了します。");
                        return;
                    default:
                        System.out.println("不明なコマンドです: " + command);
                        System.out.println("helpで使い方を確認してください。");
                        break;
                }
            } catch (IllegalArgumentException e) {
                // ③ 業務例外（ID不正・カテゴリ不正など）はメッセージを表示して続行
                System.out.println("エラー: " + e.getMessage());
            }

            System.out.println();
        }
    }

    /**
     * listコマンドの処理。引数によって全件/カテゴリ別/状態別に振り分ける。
     * 例: "list" → 全件、"list server" → サーバー設定、"list 確認済み" → OK項目
     */
    private static void handleList(DeployCheckService service, String arg) {
        List<DeployCheckItem> items;

        if (arg.isEmpty()) {
            // 引数なし → 全件表示
            items = service.listAll();
            System.out.println("--- 全チェック項目 ---");

        } else if (isStatusFilter(arg)) {
            // 状態で絞り込み
            CheckStatus status = parseStatus(arg);
            items = service.listByStatus(status);
            System.out.println("--- " + status.getLabel() + "の項目 ---");

        } else {
            // カテゴリで絞り込み
            CheckCategory category = CheckCategory.parse(arg);
            items = service.listByCategory(category);
            System.out.println("--- " + category.getLabel() + "の項目 ---");
        }

        if (items.isEmpty()) {
            System.out.println("  （該当する項目はありません）");
        } else {
            for (DeployCheckItem item : items) {
                System.out.println("  " + item);
            }
        }

        // サマリー：OK/NG件数を表示
        List<DeployCheckItem> all = service.listAll();
        long okCount = all.stream().filter(DeployCheckItem::isOk).count();
        System.out.printf("  [進捗: %d/%d 確認済み]%n", okCount, all.size());
    }

    /**
     * checkコマンドの処理。指定IDの項目を「確認済み（OK）」にする。
     */
    private static void handleCheck(DeployCheckService service, String arg) {
        int id = parseId(arg);
        DeployCheckItem item = service.markAsOk(id);
        System.out.println("✔ " + item.getName() + " を確認済みにしました。");
    }

    /**
     * uncheckコマンドの処理。指定IDの項目を「未確認（NG）」にリセットする。
     */
    private static void handleUncheck(DeployCheckService service, String arg) {
        int id = parseId(arg);
        DeployCheckItem item = service.markAsNg(id);
        System.out.println("✘ " + item.getName() + " を未確認に戻しました。");
    }

    /**
     * judgeコマンドの処理。全項目の状態を確認し、デプロイ可否を判定する。
     */
    private static void handleJudge(DeployCheckService service) {
        JudgeResult result = service.judge();

        System.out.println("=== デプロイ判定結果 ===");

        if (result.isDeployable()) {
            System.out.println("✔ 全項目が確認済みです。公開可能です！");
        } else {
            System.out.println("✘ 公開不可：以下の項目が未確認です。");
            for (DeployCheckItem ngItem : result.getNgItems()) {
                System.out.println("  " + ngItem);
            }
            System.out.printf("  [未確認: %d件]%n", result.getNgItems().size());
        }
    }

    /**
     * helpコマンドの処理。利用可能なコマンド一覧を表示する。
     */
    private static void printHelp() {
        System.out.println("=== 使い方 ===");
        System.out.println("  list                   : 全チェック項目を表示");
        System.out.println("  list <カテゴリ>        : カテゴリで絞り込み（server/network/app または日本語）");
        System.out.println("  list <状態>            : 状態で絞り込み（ok/ng または 確認済み/未確認）");
        System.out.println("  check <ID>             : 指定IDを確認済み（OK）にする");
        System.out.println("  uncheck <ID>           : 指定IDを未確認（NG）に戻す");
        System.out.println("  judge                  : デプロイ判定（全項目OKなら公開可能）");
        System.out.println("  help                   : この使い方を表示");
        System.out.println("  exit                   : 終了");
    }

    // --- 内部ヘルパー ---

    /**
     * 引数を整数IDに変換する。空や数字以外の場合は例外をthrowする。
     */
    private static int parseId(String arg) {
        if (arg.isEmpty()) {
            throw new IllegalArgumentException("IDを指定してください（例: check 1）");
        }
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("IDは数字で指定してください: " + arg);
        }
    }

    /**
     * 入力文字列が状態フィルター（ok/ng/確認済み/未確認）かどうかを判定する。
     */
    private static boolean isStatusFilter(String arg) {
        String lower = arg.toLowerCase();
        return lower.equals("ok") || lower.equals("ng")
                || arg.equals("確認済み") || arg.equals("未確認");
    }

    /**
     * 入力文字列をCheckStatusに変換する。
     */
    private static CheckStatus parseStatus(String arg) {
        String lower = arg.toLowerCase();
        if (lower.equals("ok") || arg.equals("確認済み")) {
            return CheckStatus.OK;
        }
        if (lower.equals("ng") || arg.equals("未確認")) {
            return CheckStatus.NG;
        }
        throw new IllegalArgumentException("不明な状態です: " + arg);
    }
}
