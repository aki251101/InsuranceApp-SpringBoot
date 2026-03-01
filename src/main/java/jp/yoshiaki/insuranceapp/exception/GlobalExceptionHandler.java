package jp.yoshiaki.insuranceapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 全Controllerに共通の例外処理を適用するクラス
 *
 * 仕組み：
 *   Springが @ControllerAdvice を検出すると、
 *   どのControllerで例外が発生しても、このクラスの該当メソッドが呼ばれる。
 *   → 各Controllerに try/catch を書かなくてよくなる（例外処理の一元化）
 *
 * たとえ話：
 *   会社の「クレーム対応窓口」のようなもの。
 *   営業部でも技術部でも、クレーム（例外）が出たら窓口が対応方法を決める。
 */
@ControllerAdvice // 全Controllerに適用（※文字列指定は basePackages 扱いになり得るので外す）
@Slf4j            // Lombokが log 変数を自動生成（log.warn(), log.error() が使える）
public class GlobalExceptionHandler {

    /**
     * NotFoundException をキャッチ → HTTP 404 を返す
     *
     * 呼ばれるケース：
     *   ServiceやControllerで throw new NotFoundException("...") された場合
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException e, Model model) {
        log.warn("リソースが見つかりません: {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/404";
    }

    /**
     * IllegalStateException / IllegalArgumentException をキャッチ → トップページへリダイレクト
     *
     * 呼ばれるケース：
     *   業務ロジック違反（例：解約済み契約の更新操作、不正な入力値）
     */
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public String handleBusinessException(RuntimeException e, RedirectAttributes redirectAttributes) {
        log.warn("ビジネスロジック違反: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/";
    }

    /**
     * その他すべての例外をキャッチ → HTTP 500 を返す
     *
     * 呼ばれるケース：
     *   上記のどれにも該当しない想定外のエラー（NullPointerException等）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        log.error("予期しないエラー", e);
        model.addAttribute("errorMessage", "システムエラーが発生しました。");
        return "error/500";
    }
}