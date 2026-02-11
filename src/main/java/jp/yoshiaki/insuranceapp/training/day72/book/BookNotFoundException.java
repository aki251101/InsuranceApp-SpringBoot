package jp.yoshiaki.insuranceapp.training.day72.book;

/**
 * 書籍が見つからない場合の業務例外。
 * Controller で catch して 404 Not Found に変換する。
 */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super(message);
    }
}
