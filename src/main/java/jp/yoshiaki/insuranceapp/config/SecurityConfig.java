package jp.yoshiaki.insuranceapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Profile("stub")
    public SecurityFilterChain stubSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

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
                .requestMatchers(HttpMethod.GET, "/policies", "/policies/*", "/accidents", "/accidents/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/policies/*/calendar-toggle").authenticated()
                .requestMatchers(HttpMethod.POST, "/policies/*/ai-summarize", "/accidents/*/ai-suggest").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/", true))
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
