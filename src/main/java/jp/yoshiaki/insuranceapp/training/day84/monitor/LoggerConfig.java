package jp.yoshiaki.insuranceapp.training.day84.monitor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * ログ出力のフォーマットとレベルを設定するユーティリティ。
 *
 * java.util.logging（JUL）のデフォルト出力は情報が多すぎて読みにくいため、
 * 「[レベル] 時刻 クラス名 - メッセージ」の見やすい形式にカスタマイズする。
 *
 * 実務では SLF4J + Logback を使うが、今回は標準ライブラリで概念を学ぶ。
 */
public class LoggerConfig {

    // ① 時刻のフォーマット（年-月-日 時:分:秒）
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 指定したロガーにカスタムフォーマットとレベルを設定する。
     *
     * @param logger    設定対象のLogger
     * @param level     ログレベルのしきい値（これ以上のレベルのみ出力）
     */
    public static void configure(Logger logger, Level level) {
        // ② 親ロガーのハンドラーを無効化（デフォルト出力の二重表示を防ぐ）
        logger.setUseParentHandlers(false);

        // ③ 既存のハンドラーを全て除去（再設定時の重複防止）
        for (var handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        // ④ コンソール出力用のハンドラーを作成
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);

        // ⑤ カスタムフォーマッターを設定
        consoleHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                // [WARNING] 2026-02-23 14:30:15 WeatherService - リトライ: city=osaka
                String time = LocalDateTime.now().format(TIME_FORMAT);
                String levelName = record.getLevel().getName();
                String className = extractSimpleClassName(record.getSourceClassName());
                String message = formatMessage(record);

                return String.format("[%-7s] %s %s - %s%n",
                        levelName, time, className, message);
            }
        });

        // ⑥ ロガーにハンドラーとレベルを設定
        logger.addHandler(consoleHandler);
        logger.setLevel(level);
    }

    /**
     * 完全修飾クラス名から単純クラス名を抽出する。
     * 例："jp.yoshiaki.insuranceapp.training.day84.monitor.WeatherService" → "WeatherService"
     */
    private static String extractSimpleClassName(String fullClassName) {
        if (fullClassName == null) {
            return "Unknown";
        }
        int lastDot = fullClassName.lastIndexOf('.');
        if (lastDot >= 0) {
            return fullClassName.substring(lastDot + 1);
        }
        return fullClassName;
    }
}
