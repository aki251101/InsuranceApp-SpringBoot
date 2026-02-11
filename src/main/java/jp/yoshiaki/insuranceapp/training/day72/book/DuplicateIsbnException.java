package jp.yoshiaki.insuranceapp.training.day72.book;

/**
 * ISBN が既に登録済みの場合の業務例外。
 * Controller で catch して 409 Conflict に変換する。
 */
public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException(String message) {
        super(message);
    }
}
