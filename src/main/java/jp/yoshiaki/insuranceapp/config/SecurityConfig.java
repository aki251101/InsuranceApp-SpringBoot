package jp.yoshiaki.insuranceapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 開発用（stubプロファイル）
     * - Postman等の手動テストを阻害しないよう、全リクエストを許可
     * - CSRFは無効化（POSTを簡単に）
     * - H2 Console を使う場合のために frameOptions を無効化
     */
    @Bean
    @Profile("stub")
    public SecurityFilterChain stubSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    /**
     * 本番/通常プロファイル（stub以外）
     * - ログイン必須（OAuth2ログインを想定）
     * - 静的リソースとトップ、ログイン関連のみ許可
     */
    @Bean
    @Profile("!stub")
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login**",
                    "/css/**", "/js/**", "/images/**", "/webjars/**",
                    "/actuator/health", "/actuator/info"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/", true))
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
