package jp.yoshiaki.insuranceapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全体例外ハンドラ（GlobalExceptionHandler）
 *
 * @ControllerAdvice を付けることで、アプリ内の全 Controller で throw された例外を
 * この1か所でキャッチ・処理できる。
 *
 * 【設計意図】
 *   各 Controller に try-catch をバラバラに書く必要がなくなる。
 *   例外の種類ごとに「どのHTTPステータスを返すか」「どの画面に遷移するか」を集約管理する。
 *
 * 【ログの個人情報対策】
 *   ログには policyId / policyNumber / accidentId 等の業務IDのみ出力する。
 *   customerName（契約者名）など個人情報はログに含めない。
 *   例外メッセージ自体にも個人情報を入れない設計とする。
 */
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
    public String handleBusinessException(
            RuntimeException e,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        log.warn("ビジネスロジック違反: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return resolveRedirectPath(request);
    }

    @ExceptionHandler({CalendarApiException.class, AiApiException.class})
    public String handleExternalApiException(
            RuntimeException e,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        log.error("外部APIエラー: {}", e.getMessage(), e);
        redirectAttributes.addFlashAttribute("errorMessage",
                "外部サービスとの連携でエラーが発生しました。時間をおいて再試行してください。");
        return resolveRedirectPath(request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        log.error("予期しないエラー", e);
        model.addAttribute("errorMessage", "システムエラーが発生しました。");
        return "error/500";
    }

    private String resolveRedirectPath(HttpServletRequest request) {
        if (request == null || request.getRequestURI() == null) {
            return "redirect:/";
        }

        String uri = request.getRequestURI();
        if (uri.startsWith("/policies/")) {
            return "redirect:" + toDetailPath(uri, "/policies/");
        }
        if (uri.startsWith("/accidents/")) {
            return "redirect:" + toDetailPath(uri, "/accidents/");
        }

        return "redirect:/";
    }

    private String toDetailPath(String uri, String prefix) {
        String idPart = uri.substring(prefix.length());
        int slashIndex = idPart.indexOf('/');
        if (slashIndex >= 0) {
            idPart = idPart.substring(0, slashIndex);
        }

        try {
            long id = Long.parseLong(idPart);
            return prefix + id;
        } catch (NumberFormatException ex) {
            return "/";
        }
    }
}
