package jp.yoshiaki.insuranceapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ホーム画面Controller
 *
 * 役割：
 *   ブラウザで http://localhost:8080/ にアクセスされたとき、
 *   トップページ（home.html）を返す。
 *
 * @Controller：
 *   このクラスが「HTTPリクエストを受け取って画面を返す係」であることをSpringに伝える。
 *   @RestController と違い、戻り値はテンプレート名（HTML）として解釈される。
 */
@Controller
public class HomeController {

    /**
     * トップページを表示
     *
     * @GetMapping("/")：
     *   HTTP GETメソッドで "/" にアクセスが来たら、このメソッドが呼ばれる。
     *
     * @return "home" → Thymeleafが templates/home.html を探して描画する
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }
}
