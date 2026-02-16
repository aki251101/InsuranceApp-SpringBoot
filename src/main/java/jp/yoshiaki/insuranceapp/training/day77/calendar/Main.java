package jp.yoshiaki.insuranceapp.training.day77.calendar;

import java.util.Scanner;

/**
 * イベント予約管理ミニアプリのエントリーポイント。
 *
 * FakeCalendarClient を生成し、ReservationService に注入する。
 * コマンド入力でイベントの登録・削除・一覧・失敗モード切替を操作する。
 */
public class Main {

    public static void main(String[] args) {
        // ① Fake実装を生成し、Serviceに注入（ここが差し替えポイント）
        FakeCalendarClient fakeClient = new FakeCalendarClient();
        ReservationService service = new ReservationService(fakeClient);

        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("  イベント予約管理（CalendarClient Fake版）");
        System.out.println("========================================");
        System.out.println();
        printHelp();

        // ② メインループ
        while (true) {
            System.out.println();
            System.out.print("操作を入力してください > ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // ③ コマンドと引数を分割
            String[] parts = line.split("\\s+", 3);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "create": {
                    // create <タイトル> <日付>
                    if (parts.length < 3) {
                        System.out.println("使い方：create <タイトル> <日付(yyyy-MM-dd)>");
                        System.out.println("例：create 会議 2026-03-01");
                        break;
                    }
                    String title = parts[1];
                    String dateStr = parts[2];
                    String result = service.createReservation(title, dateStr);
                    System.out.println(result);
                    break;
                }

                case "delete": {
                    // delete <イベントID>
                    if (parts.length < 2) {
                        System.out.println("使い方：delete <イベントID>");
                        System.out.println("例：delete EVT-0001");
                        break;
                    }
                    String eventId = parts[1];
                    String result = service.deleteReservation(eventId);
                    System.out.println(result);
                    break;
                }

                case "list": {
                    // list（引数なし）
                    String result = service.listReservations();
                    System.out.println(result);
                    break;
                }

                case "fail": {
                    // fail on / fail off
                    if (parts.length < 2) {
                        System.out.println("現在の失敗モード："
                                + (fakeClient.isFailMode() ? "ON（障害状態）" : "OFF（正常）"));
                        System.out.println("使い方：fail on / fail off");
                        break;
                    }
                    String mode = parts[1].toLowerCase();
                    if ("on".equals(mode)) {
                        fakeClient.setFailMode(true);
                        System.out.println("失敗モードを ON にしました（以降のAPI呼び出しは全て失敗します）");
                    } else if ("off".equals(mode)) {
                        fakeClient.setFailMode(false);
                        System.out.println("失敗モードを OFF にしました（正常動作に戻ります）");
                    } else {
                        System.out.println("使い方：fail on / fail off");
                    }
                    break;
                }

                case "help": {
                    printHelp();
                    break;
                }

                case "exit": {
                    System.out.println("アプリを終了します。");
                    scanner.close();
                    return;
                }

                default: {
                    System.out.println("不明なコマンドです。helpで操作一覧を表示できます。");
                    break;
                }
            }
        }
    }

    /**
     * ヘルプ表示（操作一覧）
     */
    private static void printHelp() {
        System.out.println("【操作一覧】");
        System.out.println("  create <タイトル> <日付>  ：予約を登録（例：create 会議 2026-03-01）");
        System.out.println("  delete <イベントID>       ：予約を削除（例：delete EVT-0001）");
        System.out.println("  list                      ：予約一覧を表示");
        System.out.println("  fail on / fail off        ：失敗モードの切替（API障害シミュレーション）");
        System.out.println("  help                      ：この操作一覧を表示");
        System.out.println("  exit                      ：終了");
    }
}
