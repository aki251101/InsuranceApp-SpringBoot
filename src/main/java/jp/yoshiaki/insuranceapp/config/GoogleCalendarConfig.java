package jp.yoshiaki.insuranceapp.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

/**
 * Google Calendar API の認証設定クラス
 *
 * OAuth2でログイン済みユーザーのアクセストークンを取得し、
 * Google Calendar API のサービスオブジェクトを構築する。
 *
 * 本番（profile=production）でのみ有効化。
 * Stub環境ではこのクラスは読み込まれない。
 *
 * 完成版コード（insurance-app-java/java-source/config/GoogleCalendarConfig.java）に準拠。
 */
@Configuration
@Profile("production")  // ① Stub環境では不要（Stubは外部通信しない）
@Slf4j
public class GoogleCalendarConfig {

    // ② アプリケーション名（Google API管理画面に表示される名前）
    private static final String APPLICATION_NAME = "Insurance App";

    // ③ JSONパーサー（Google APIのレスポンス解析に使用）
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // ④ Spring Security の OAuth2 クライアント管理サービス
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleCalendarConfig(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * OAuth認証済みの Calendar サービスオブジェクトを取得する
     *
     * 処理の流れ：
     *   1. SecurityContext からログイン中ユーザーの認証情報を取得
     *   2. OAuth2のアクセストークンを取り出す
     *   3. トークンを使ってGoogleCredentialsを作成
     *   4. Calendar.Builder でサービスオブジェクトを構築
     *
     * @return Calendar サービスオブジェクト（これでイベント作成/削除ができる）
     * @throws IOException 通信エラー
     * @throws GeneralSecurityException セキュリティエラー
     */
    public Calendar getCalendarService() throws IOException, GeneralSecurityException {
        // ⑤ HTTPトランスポート（Google APIとの通信路）を構築
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // ⑥ ログイン中ユーザーのOAuth2アクセストークンを取得
        String accessToken = getAccessToken();

        if (accessToken == null) {
            throw new IllegalStateException("Googleアカウントでログインしてください");
        }

        // ⑦ アクセストークンから GoogleCredentials（認証情報）を作成
        GoogleCredentials credentials = GoogleCredentials.create(
                new AccessToken(accessToken, new Date()));

        // ⑧ Calendar サービスオブジェクトを構築して返す
        return new Calendar.Builder(httpTransport, JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * SecurityContext から OAuth2 アクセストークンを取得する
     *
     * @return アクセストークン文字列（未認証の場合はnull）
     */
    private String getAccessToken() {
        // ⑨ 現在のリクエストの認証情報を取得
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // ⑩ OAuth2認証でなければトークンは取れない
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.warn("OAuth2認証されていません");
            return null;
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        // ⑪ 認証済みクライアント情報を取得
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName());

        if (client == null) {
            log.warn("OAuth2クライアントが見つかりません");
            return null;
        }

        // ⑫ アクセストークンの文字列を返す
        return client.getAccessToken().getTokenValue();
    }
}
