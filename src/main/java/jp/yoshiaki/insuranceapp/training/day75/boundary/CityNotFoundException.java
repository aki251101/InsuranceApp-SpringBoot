package jp.yoshiaki.insuranceapp.training.day75.boundary;

/**
 * 指定された都市が見つからない場合にスローする業務例外。
 *
 * RuntimeException を継承する理由：
 * - 「都市が見つからない」はビジネスルール上のエラー（業務例外）
 * - 呼び出し元に try-catch を強制しない（RuntimeException = 非検査例外）
 * - Service や Main が「キャッチするかどうか」を選べる
 *
 * 損保アプリでの対応：
 * - PolicyNotFoundException（契約が見つからない）と同じパターン
 */
public class CityNotFoundException extends RuntimeException {

    // ① メッセージ付きコンストラクタ：エラー原因を伝える
    public CityNotFoundException(String cityName) {
        super("都市が見つかりません：" + cityName);
    }
}
