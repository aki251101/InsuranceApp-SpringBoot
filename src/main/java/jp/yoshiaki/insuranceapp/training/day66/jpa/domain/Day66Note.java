package jp.yoshiaki.insuranceapp.training.day66.jpa.domain;

import jakarta.persistence.*;

@Entity(name = "Day66Note")        // ① 「このクラスはDBに保存する対象」
@Table(name = "day66_notes")       // ② 「対応するテーブル名はday66_notes」
public class Day66Note {

    @Id                            // ③ 「このフィールドが主キー」
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ④ 「自動採番する」
    private Long id;

    @Column(nullable = false, length = 100)   // ⑤ カラムの制約
    private String title;

    @Column(nullable = false, length = 2000)
    private String body;

    // ⑥ JPA必須：デフォルトコンストラクタ（protected推奨）
    protected Day66Note() {
    }

    // ⑦ アプリから使うコンストラクタ
    public Day66Note(String title, String body) {
        this.title = title;
        this.body = body;
    }

    // Getter（Setterは不要：不変にしておく）
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
}