package jp.yoshiaki.insuranceapp.training.day86.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * リソースが見つからない場合にスローする例外。
 * @ResponseStatus(NOT_FOUND) により、この例外がスローされると
 * Spring が自動で HTTPステータス 404 を返す。
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // この例外 → 自動で404レスポンス
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message); // 例外メッセージをセット（ログやレスポンスに表示される）
    }
}
