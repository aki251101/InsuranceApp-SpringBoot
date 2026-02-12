package jp.yoshiaki.insuranceapp.training.day73.deadline;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * リソースが見つからない場合の業務例外
 *
 * @ResponseStatus(HttpStatus.NOT_FOUND)
 *   → この例外がthrowされると、Springが自動的にHTTP 404を返す
 *   → Controllerで try-catch しなくても404になる
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
