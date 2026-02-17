package jp.yoshiaki.insuranceapp.training.day67.book;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * グローバル例外ハンドラ（Day67用）
 * 
 * 【役割】
 * - Controller層で発生した例外をキャッチし、適切なHTTPレスポンスに変換する
 * - @RestControllerAdvice：全Controllerに適用される例外ハンドラを定義
 * - @ExceptionHandler：特定の例外をハンドリングするメソッドを指定
 * 
 * 【なぜ必要か】
 * - 例外がそのまま伝播すると、500 Internal Server Errorになってしまう
 * - 「見つからない」は500ではなく404で返すべき（HTTP仕様に準拠）
 * - 統一されたエラーレスポンス形式を提供できる
 */
@Profile("training")
@RestControllerAdvice(basePackages = "jp.yoshiaki.insuranceapp.training.day67.book")
public class GlobalExceptionHandler {

    /**
     * NotFoundExceptionを404 Not Foundに変換
     * 
     * @param e 発生した例外
     * @return 404レスポンス（JSON形式のエラー情報）
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException e) {
        // エラーレスポンスのボディを構築
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 404);
        body.put("error", "Not Found");
        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * その他の例外を500 Internal Server Errorに変換
     * 
     * @param e 発生した例外
     * @return 500レスポンス
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", "予期しないエラーが発生しました");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
