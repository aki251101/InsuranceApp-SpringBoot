package jp.yoshiaki.insuranceapp.training.day79.deadline;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * サブスクリプション期限管理ミニアプリのエントリーポイント。
 * コンソールからコマンドを受け取り、DeadlineService に処理を委譲する。
 */
public class Main {

    public static void main(String[] args) {
        // ① 依存関係の組み立て（DI：手動でインスタンスを渡す）
        InMemorySubscriptionRepository repository = new InMemorySubscriptionRepository();
        DeadlineService service = new DeadlineService(repository);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== サブスクリプション期限管理 ===");
        System.out.println("操作を入力してください（helpで一覧、exitで終了）");
        System.out.println();

        // ② サンプルデータを登録（動作確認用）
        registerSampleData(service);

        // ③ コマンドループ
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+", 4); // 最大4分割
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "add":
                        handleAdd(parts, service);
                        break;
                    case "list":
                        handleList(service);
                        break;
                    case "check":
                        handleCheck(parts, service);
                        break;
                    case "renew":
                        handleRenew(parts, service);
                        break;
                    case "approaching":
                        handleApproaching(service);
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                        System.out.println("終了します。");
                        return;
                    default:
                        System.out.println("不明なコマンドです。helpで一覧を確認してください。");
                }
            } catch (SubscriptionNotFoundException e) {
                // ④ 業務例外：IDが見つからない場合
                System.out.println("[エラー] " + e.getMessage());
            } catch (IllegalArgumentException e) {
                // ⑤ 入力不正（日付パースエラー、バリデーションエラー等）
                System.out.println("[入力エラー] " + e.getMessage());
            } catch (Exception e) {
                // ⑥ 想定外のエラー
                System.out.println("[システムエラー] 予期しないエラーが発生しました: " + e.getMessage());
            }

            System.out.println(); // コマンド間に空行を入れて見やすくする
        }
    }

    // --- コマンドハンドラ ---

    private static void handleAdd(String[] parts, DeadlineService service) {
        // 書式: add <名前> <開始日> <終了日>
        if (parts.length < 4) {
            System.out.println("使い方: add <契約名> <開始日(yyyy-MM-dd)> <終了日(yyyy-MM-dd)>");
            System.out.println("  例: add Netflix 2025-04-01 2026-03-31");
            return;
        }
        String name = parts[1];
        LocalDate startDate = parseDate(parts[2]);
        LocalDate endDate = parseDate(parts[3]);
        Subscription sub = service.register(name, startDate, endDate);
        System.out.println("登録しました: " + sub);
    }

    private static void handleList(DeadlineService service) {
        List<String> list = service.listAll();
        if (list.isEmpty()) {
            System.out.println("契約がありません。");
            return;
        }
        System.out.println("--- 契約一覧（期限状態付き）---");
        for (String item : list) {
            System.out.println("  " + item);
        }
    }

    private static void handleCheck(String[] parts, DeadlineService service) {
        if (parts.length < 2) {
            System.out.println("使い方: check <契約ID>");
            return;
        }
        long id = parseLong(parts[1]);
        String result = service.checkDeadline(id);
        System.out.println("期限チェック結果: " + result);
    }

    private static void handleRenew(String[] parts, DeadlineService service) {
        if (parts.length < 2) {
            System.out.println("使い方: renew <契約ID>");
            return;
        }
        long id = parseLong(parts[1]);
        String result = service.renew(id);
        System.out.println(result);
    }

    private static void handleApproaching(DeadlineService service) {
        List<Subscription> approaching = service.findApproaching(DeadlineConstants.APPROACHING_DAYS);
        if (approaching.isEmpty()) {
            System.out.println("期限が" + DeadlineConstants.APPROACHING_DAYS + "日以内に迫っている契約はありません。");
            return;
        }
        System.out.println("--- 期限接近（" + DeadlineConstants.APPROACHING_DAYS + "日以内）---");
        for (Subscription sub : approaching) {
            System.out.println("  " + sub);
        }
    }

    private static void printHelp() {
        System.out.println("--- コマンド一覧 ---");
        System.out.println("  add <名前> <開始日> <終了日>  : 契約を登録（日付はyyyy-MM-dd形式）");
        System.out.println("  list                         : 全契約を一覧表示（期限状態付き）");
        System.out.println("  check <ID>                   : 指定契約の期限状態を確認");
        System.out.println("  renew <ID>                   : 指定契約を更新（早期/通常を判定）");
        System.out.println("  approaching                  : 期限が近い契約を表示");
        System.out.println("  help                         : この一覧を表示");
        System.out.println("  exit                         : 終了");
    }

    // ⑦ サンプルデータ登録（今日の日付を基準に、さまざまな期限状態の契約を作る）
    private static void registerSampleData(DeadlineService service) {
        LocalDate today = LocalDate.now();

        // 期限がかなり先（更新可能期間外）
        service.register("Netflix", today.minusMonths(6), today.plusMonths(6));

        // 更新可能期間内＋早期更新期間内（満期が約1ヶ月後）
        service.register("Spotify", today.minusMonths(11), today.plusDays(30));

        // 更新可能期間内＋通常期間（満期が15日後 → 21日前を過ぎている）
        service.register("AmazonPrime", today.minusMonths(11), today.plusDays(15));

        // 期限切れ（満期が10日前）
        service.register("Hulu", today.minusMonths(12).minusDays(10), today.minusDays(10));

        System.out.println("サンプルデータを4件登録しました（list で確認できます）。");
        System.out.println();
    }

    // --- ユーティリティ ---

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "日付の形式が正しくありません（yyyy-MM-dd形式で入力してください）: " + text);
        }
    }

    private static long parseLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "IDは数値で入力してください: " + text);
        }
    }
}
