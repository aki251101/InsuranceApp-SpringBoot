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
@ControllerAdvice("insuranceGlobalExceptionHandler") // ① 全Controllerの例外をキャッチする「見張り番」
@Slf4j              // ② Lombokが log 変数を自動生成（log.warn(), log.error() が使える）
public class GlobalExceptionHandler {

    /**
     * NotFoundException をキャッチ → HTTP 404 を返す
     *
     * 呼ばれるケース：
     *   ServiceやControllerで throw new NotFoundException("...") された場合
     */
    @ExceptionHandler(NotFoundException.class)      // ③ この例外型を担当する
    @ResponseStatus(HttpStatus.NOT_FOUND)            // ④ HTTPステータスを404に設定
    public String handleNotFoundException(NotFoundException e, Model model) {
        // ⑤ ログに警告（warn）を記録 ─ 「見つからない」は想定内なのでwarn
        log.warn("リソースが見つかりません: {}", e.getMessage());
        // ⑥ 画面にエラーメッセージを渡す
        model.addAttribute("errorMessage", e.getMessage());
        // ⑦ error/404.html テンプレートを表示
        return "error/404";
    }

    /**
     * IllegalStateException / IllegalArgumentException をキャッチ → トップページへリダイレクト
     *
     * 呼ばれるケース：
     *   業務ロジック違反（例：解約済み契約の更新操作、不正な入力値）
     *
     * 補足：BusinessException も将来ここに追加可能。
     *       現時点では標準のIllegalState/IllegalArgumentで十分な範囲を想定。
     */
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public String handleBusinessException(RuntimeException e, RedirectAttributes redirectAttributes) {
        // ⑧ ログに警告を記録
        log.warn("ビジネスロジック違反: {}", e.getMessage());
        // ⑨ リダイレクト先にエラーメッセージを渡す（Flash属性：1回だけ表示）
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        // ⑩ トップページへリダイレクト
        return "redirect:/";
    }

    /**
     * その他すべての例外をキャッチ → HTTP 500 を返す
     *
     * 呼ばれるケース：
     *   上記のどれにも該当しない想定外のエラー（NullPointerException等）
     *
     * 重要：想定外エラーはスタックトレースごとログに残す（原因調査に必須）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // ⑪ HTTPステータスを500に設定
    public String handleException(Exception e, Model model) {
        // ⑫ ログにエラー（error）を記録 ─ 想定外なのでerror＋スタックトレース
        log.error("予期しないエラー", e);
        // ⑬ 画面には詳細を見せない（セキュリティ上、内部情報は隠す）
        model.addAttribute("errorMessage", "システムエラーが発生しました。");
        // ⑭ error/500.html テンプレートを表示
        return "error/500";
    }
}
