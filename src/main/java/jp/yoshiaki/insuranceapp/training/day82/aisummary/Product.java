package jp.yoshiaki.insuranceapp.training.day82.aisummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 商品情報とレビュー一覧を保持するドメインクラス。
 *
 * レビューはList<String>で管理し、addReviewで追加する。
 * getReviewsは防御的コピー（変更不可リスト）を返す。
 */
public class Product {

    private long id;
    private String name;
    private String category;
    private String description;
    private final List<String> reviews;  // レビュー一覧

    // ① コンストラクタ（ID採番はRepository側で行う）
    public Product(long id, String name, String category, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.reviews = new ArrayList<>();
    }

    // ② レビューを追加する
    public void addReview(String review) {
        if (review == null || review.isBlank()) {
            throw new IllegalArgumentException("レビュー内容が空です");
        }
        this.reviews.add(review);
    }

    // ③ レビュー一覧を取得（防御的コピー：外部から変更できないようにする）
    public List<String> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    // ④ レビュー件数を返す
    public int getReviewCount() {
        return reviews.size();
    }

    // ⑤ getter
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    // ⑥ 表示用
    @Override
    public String toString() {
        return String.format("[ID:%d] %s（%s）- レビュー %d件",
                id, name, category, reviews.size());
    }
}
