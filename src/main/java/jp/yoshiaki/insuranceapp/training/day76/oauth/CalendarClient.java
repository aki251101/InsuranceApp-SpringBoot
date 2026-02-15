package jp.yoshiaki.insuranceapp.training.day76.oauth;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * トークン検証付きでカレンダーイベントの登録・一覧を行うクライアントクラス。
 *
 * 実務での対応：
 * - 完成版コードではCalendarServiceがこの役割を担う
 * - GoogleCalendarConfig.getCalendarService()でOAuth済みのCalendarオブジェクトを取得し、
 *   service.events().insert(...)でイベントを作成する
 * - このクラスでは「トークンが有効でないとAPI呼び出しが失敗する」ことを体験する
 */
public class CalendarClient {

    private final TokenStore tokenStore;              // トークン検証に使用
    private final List<CalendarEvent> events;         // 登録済みイベント（インメモリ）
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ① コンストラクタ
    public CalendarClient(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
        this.events = new ArrayList<>();
    }

    /**
     * ② イベントを登録する。
     *    トークンが有効でなければ例外をスローする。
     *
     * @param title イベントタイトル
     * @param date  イベント日付（YYYY-MM-DD形式）
     * @return 登録されたCalendarEvent
     * @throws UnauthorizedException 未認証の場合
     */
    public CalendarEvent registerEvent(String title, String date) {
        // トークンの検証（未認証チェック）
        validateToken();

        // イベントを作成して保管
        String eventId = UUID.randomUUID().toString();
        String createdAt = LocalDateTime.now().format(FMT);
        CalendarEvent event = new CalendarEvent(eventId, title, date, createdAt);
        events.add(event);

        System.out.println("  【模擬API呼び出し】Google Calendar API → events.insert()");
        System.out.println("  使用トークン: " + tokenStore.getTokenInfo().getAccessToken());
        System.out.println("  イベント登録成功！");
        System.out.println("  - イベントID: " + eventId.substring(0, 8) + "...");
        System.out.println("  - タイトル: " + title);
        System.out.println("  - 日付: " + date);

        return event;
    }

    /**
     * ③ 登録済みイベントの一覧を返す。
     *    トークンが有効でなければ例外をスローする。
     *
     * @return イベントリスト
     * @throws UnauthorizedException 未認証の場合
     */
    public List<CalendarEvent> listEvents() {
        validateToken();
        return List.copyOf(events); // 防御的コピーで返す
    }

    /**
     * ④ トークンの有効性を検証する（private）。
     *    未認証 → UnauthorizedException
     *    期限切れ → 警告を出しつつUnauthorizedException
     */
    private void validateToken() {
        // 未認証チェック
        if (!tokenStore.isAuthenticated()) {
            throw new UnauthorizedException(
                    "未認証です。先に auth コマンドで認証してください。");
        }

        // 期限切れチェック
        TokenInfo info = tokenStore.getTokenInfo();
        if (info.isExpired()) {
            throw new UnauthorizedException(
                    "アクセストークンが期限切れです（有効期限: 60秒）。\n"
                    + "  → auth コマンドで再認証してください。\n"
                    + "  ※ 実務ではリフレッシュトークン（" + info.getRefreshToken()
                    + "）で自動更新されます。");
        }
    }
}
