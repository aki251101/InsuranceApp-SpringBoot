package jp.yoshiaki.insuranceapp.exception;

public class CalendarApiException extends RuntimeException {
    
    public CalendarApiException(String message) {
        super(message);
    }
    
    public CalendarApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
