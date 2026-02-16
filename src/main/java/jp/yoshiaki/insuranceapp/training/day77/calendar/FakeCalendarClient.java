package jp.yoshiaki.insuranceapp.training.day77.calendar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CalendarClient のテスト用実装（Fake）。
 *
 * 本番のGoogleカレンダーAPIの代わりに、メモリ上のMapにイベントを保存する。
 * failMode を ON にすると、全メソッドで CalendarApiException を投げて
 * 「外部APIが壊れた状態」をシミュレーションできる。
 *
 * Fakeは「本番と同じinterfaceを実装するが、中身が簡易版」という位置づけ。
 * Stub（固定値を返すだけ）より賢く、実際にデータの追加・削除も再現する。
 */
public class FakeCalendarClient implements CalendarClient {

    // ① イベントの保管庫（LinkedHashMap = 挿入順を保持するMap）
    private final Map<String, CalendarEvent> store = new LinkedHashMap<>();

    // ② 失敗モード（true = 全メソッドで例外を投げる）
    private boolean failMode = false;

    // ③ ID採番用の連番カウンター
    private int sequence = 0;

    /**
     * 失敗モードを切り替える。
     * true にすると、以降の全メソッド呼び出しで CalendarApiException が発生する。
     */
    public void setFailMode(boolean failMode) {
        this.failMode = failMode;
    }

    public boolean isFailMode() {
        return failMode;
    }

    @Override
    public String createEvent(String title, LocalDate date) {
        // ④ 失敗モードONなら、外部API障害をシミュレーション
        if (failMode) {
            throw new CalendarApiException(
                    "【Fake】カレンダーAPI接続に失敗しました（failMode=ON）");
        }

        // ⑤ 正常系：IDを採番してMapに保存
        sequence++;
        String eventId = String.format("EVT-%04d", sequence);
        CalendarEvent event = new CalendarEvent(eventId, title, date);
        store.put(eventId, event);
        return eventId;
    }

    @Override
    public void deleteEvent(String eventId) {
        // ⑥ 失敗モードONなら例外
        if (failMode) {
            throw new CalendarApiException(
                    "【Fake】カレンダーAPI接続に失敗しました（failMode=ON）");
        }

        // ⑦ 正常系：Mapから削除（存在しなくてもエラーにはしない）
        CalendarEvent removed = store.remove(eventId);
        if (removed == null) {
            System.out.println("  ※ イベントID「" + eventId + "」は存在しませんでした");
        }
    }

    @Override
    public List<CalendarEvent> listEvents() {
        // ⑧ 失敗モードONなら例外
        if (failMode) {
            throw new CalendarApiException(
                    "【Fake】カレンダーAPI接続に失敗しました（failMode=ON）");
        }

        // ⑨ 正常系：保管庫の全イベントをリストで返す
        return new ArrayList<>(store.values());
    }
}
