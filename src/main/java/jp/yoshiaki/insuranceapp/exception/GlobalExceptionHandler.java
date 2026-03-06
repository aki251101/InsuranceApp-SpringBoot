package jp.yoshiaki.insuranceapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException e, Model model) {
        log.warn("リソースが見つかりません: {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(NoResourceFoundException e, Model model) {
        log.warn("静的リソースが見つかりません: {}", e.getMessage());
        model.addAttribute("errorMessage", "ページが見つかりませんでした。");
        return "error/404";
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public String handleBusinessException(RuntimeException e, RedirectAttributes redirectAttributes) {
        log.warn("ビジネスロジック違反: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        log.error("予期しないエラー", e);
        model.addAttribute("errorMessage", "システムエラーが発生しました。");
        return "error/500";
    }
}
