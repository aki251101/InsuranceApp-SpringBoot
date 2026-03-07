package jp.yoshiaki.insuranceapp.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * トレースIDフィルタ（TraceIdFilter）
 *
 * 【役割】
 *   全HTTPリクエストに対して「追跡ID（traceId）」を自動付与する。
 *   この traceId は MDC（Mapped Diagnostic Context）に格納され、
 *   以降のすべてのログ出力に自動的に含まれる。
 *
 * 【なぜ必要か？】
 *   本番環境では複数のリクエストが同時に処理される。
 *   ログに traceId がないと「どのログがどのリクエストのものか」が分からない。
 *   traceId があれば、grep traceId でそのリクエストに関する全ログを抽出できる。
 *
 * 【MDC（Mapped Diagnostic Context）とは？】
 *   SLF4J / Logback が提供する「スレッドローカルなキー/値ストア」。
 *   MDC.put("traceId", "a1b2c3d4") すると、同じスレッド内の全ログに
 *   %X{traceId} として自動出力される。
 *   リクエスト完了後に必ず MDC.remove() すること（メモリリーク防止）。
 *
 * 【ログの個人情報対策】
 *   このフィルタでは HTTPメソッド と URI のみログ出力する。
 *   リクエストパラメータやリクエストボディ（個人情報を含む可能性あり）は出力しない。
 */
@Component // ① Spring Bean として登録 → 自動的に Filter として動作する
@Slf4j
public class TraceIdFilter implements Filter {

    // ② MDC に格納するキー名（logback-spring.xml の %X{traceId} と一致させる）
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * フィルタ処理本体
     *
     * @param request  リクエスト
     * @param response レスポンス
     * @param chain    フィルタチェーン（次の処理へ渡す）
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // ③ UUID の先頭8文字を traceId として使用（短くて読みやすい）
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);

        try {
            // ④ リクエスト開始ログ（HTTPメソッド + URI のみ。パラメータは出さない）
            log.info("リクエスト開始: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

            // ⑤ 次のフィルタ or Controller へ処理を渡す
            chain.doFilter(request, response);

            // ⑥ リクエスト終了ログ
            log.info("リクエスト終了: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
        } finally {
            // ⑦ 必ず MDC から traceId を削除する（メモリリーク防止）
            //    finally ブロックで削除するため、例外発生時も確実に実行される
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
