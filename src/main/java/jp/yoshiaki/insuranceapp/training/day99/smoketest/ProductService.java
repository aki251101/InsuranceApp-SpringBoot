package jp.yoshiaki.insuranceapp.training.day99.smoketest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品Service（業務操作）
 * スモークテストの「書き込み/読み取り疎通」を確認するための業務層。
 */
@Service("day99ProductService")
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    // コンストラクタインジェクション
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 全商品を取得する
     * @return 商品リスト
     */
    public List<Product> findAll() {
        log.debug("商品一覧を取得します");
        return productRepository.findAll();
    }

    /**
     * IDで商品を取得する
     * @param id 商品ID
     * @return 商品
     * @throws IllegalArgumentException 見つからない場合
     */
    public Product findById(Long id) {
        log.debug("商品を取得します: id={}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("商品が見つかりません: id={}", id);
                    return new IllegalArgumentException("商品が見つかりません: id=" + id);
                });
    }

    /**
     * 商品を新規作成する
     * @param name  商品名
     * @param price 価格
     * @return 作成された商品
     */
    public Product create(String name, int price) {
        log.info("商品を作成します: name={}, price={}", name, price);
        Product product = new Product(null, name, price);
        Product saved = productRepository.save(product);
        log.info("商品を作成しました: id={}", saved.getId());
        return saved;
    }

    /**
     * 商品件数を返す（ヘルスチェック用の委譲メソッド）
     * @return 件数
     */
    public long count() {
        return productRepository.count();
    }
}
