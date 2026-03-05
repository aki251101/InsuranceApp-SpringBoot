package jp.yoshiaki.insuranceapp.training.day69.product.web;

import jp.yoshiaki.insuranceapp.training.day69.product.domain.Product;
import jp.yoshiaki.insuranceapp.training.day69.product.exception.ProductNotFoundException;
import jp.yoshiaki.insuranceapp.training.day69.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProductControllerのMockMvcテスト。
 *
 * 【3点セットの解説（Spring Boot 4.0版）】
 * 1. @WebMvcTest → Controller層だけをSpringに起動させる（Service/Repositoryは起動しない）
 *    ※ Spring Boot 4.0ではパッケージが org.springframework.boot.webmvc.test.autoconfigure に変更
 * 2. @MockitoBean → ServiceをMockitoの偽物に差し替える
 *    ※ Spring Boot 4.0では @MockBean → @MockitoBean に名称変更
 * 3. MockMvc     → HTTPリクエストを模擬してレスポンスを検証する
 *
 * 配置先：src/test/java/jp/yoshiaki/insuranceapp/training/day69/product/web/
 *        ※ test ディレクトリ（main ではない）に置く
 */
@WebMvcTest(ProductController.class)  // ① Controller層だけを起動
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;  // ② HTTPリクエストを模擬するツール

    @MockitoBean
    private ProductService service;  // ③ Serviceを偽物に差し替え（Spring Boot 4.0では@MockitoBean）

    // --- テスト1：全商品一覧を取得できる ---
    @Test
    @DisplayName("GET /api/day69/products → 200 OK + 商品リスト")
    void findAll_正常系_商品リストが返る() throws Exception {
        // 【準備】Serviceの台本をセット：findAll()が呼ばれたらリストを返す
        List<Product> mockProducts = List.of(
                new Product(1L, "ノートPC", 120000),
                new Product(2L, "マウス", 3500)
        );
        given(service.findAll()).willReturn(mockProducts);

        // 【実行＆検証】GETリクエストを送り、レスポンスをチェック
        mockMvc.perform(get("/api/day69/products"))
                .andExpect(status().isOk())                                  // ステータス200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // JSON形式
                .andExpect(jsonPath("$").isArray())                           // 配列である
                .andExpect(jsonPath("$.length()").value(2))                   // 要素数2
                .andExpect(jsonPath("$[0].name").value("ノートPC"))           // 1件目の名前
                .andExpect(jsonPath("$[0].price").value(120000))              // 1件目の価格
                .andExpect(jsonPath("$[1].name").value("マウス"));            // 2件目の名前
    }

    // --- テスト2：IDで商品を1件取得できる ---
    @Test
    @DisplayName("GET /api/day69/products/1 → 200 OK + 商品詳細")
    void findById_正常系_商品が返る() throws Exception {
        // 【準備】ID=1で呼ばれたら商品を返す
        Product mockProduct = new Product(1L, "ノートPC", 120000);
        given(service.findById(1L)).willReturn(mockProduct);

        // 【実行＆検証】
        mockMvc.perform(get("/api/day69/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ノートPC"))
                .andExpect(jsonPath("$.price").value(120000));
    }

    // --- テスト3：存在しないIDで404が返る ---
    @Test
    @DisplayName("GET /api/day69/products/999 → 404 NOT_FOUND")
    void findById_異常系_存在しないIDで404() throws Exception {
        // 【準備】ID=999で呼ばれたら例外をスロー
        given(service.findById(999L))
                .willThrow(new ProductNotFoundException(999L));

        // 【実行＆検証】
        mockMvc.perform(get("/api/day69/products/999"))
                .andExpect(status().isNotFound())                             // ステータス404
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))            // エラーコード
                .andExpect(jsonPath("$.message").value("商品が見つかりません（ID: 999）")); // メッセージ
    }

    // --- テスト4：新規商品を登録できる ---
    @Test
    @DisplayName("POST /api/day69/products → 201 Created + 登録された商品")
    void create_正常系_商品が登録される() throws Exception {
        // 【準備】createが呼ばれたら、ID採番済みの商品を返す
        Product saved = new Product(1L, "キーボード", 8000);
        given(service.create(anyString(), anyInt())).willReturn(saved);

        // 【実行＆検証】POSTリクエストにJSON本文を付けて送信
        String requestJson = """
                {
                    "name": "キーボード",
                    "price": 8000
                }
                """;

        mockMvc.perform(post("/api/day69/products")
                        .contentType(MediaType.APPLICATION_JSON)  // JSON形式で送信
                        .content(requestJson))                     // リクエストボディ
                .andExpect(status().isCreated())                    // ステータス201
                .andExpect(jsonPath("$.id").value(1))               // ID採番済み
                .andExpect(jsonPath("$.name").value("キーボード"))   // 名前
                .andExpect(jsonPath("$.price").value(8000));        // 価格
    }
}