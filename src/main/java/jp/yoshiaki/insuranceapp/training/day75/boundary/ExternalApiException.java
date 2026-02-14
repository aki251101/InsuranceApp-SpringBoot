package jp.yoshiaki.insuranceapp.training.day75.boundary;

/**
 * 外部API呼び出しが失敗した場合にスローする例外。
 *
 * 「境界での例外翻訳」パターン：
 * - 外部ライブラリの例外（IOException, HttpException 等）を
 *   アプリ独自の例外にラップ（包む）して投げ直す
 * - こうすることで、Service やMain は「外部のどのライブラリを使っているか」を
 *   知らなくて済む（疎結合）
 *
 * 損保アプリでの対応：
 * - CalendarApiException（Googleカレンダー連携失敗）
 * - AiApiException（Gemini AI連携失敗）
 * と同じパターン。
 */
public class ExternalApiException extends RuntimeException {

    // ① メッセージのみ：原因が特定できない場合
    public ExternalApiException(String message) {
        super(message);
    }

    // ② メッセージ＋原因例外：元の例外を保持して原因追跡を可能にする
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
