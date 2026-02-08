package jp.yoshiaki.insuranceapp.training.day69.product.service;

import jp.yoshiaki.insuranceapp.training.day69.product.domain.Product;
import jp.yoshiaki.insuranceapp.training.day69.product.exception.ProductNotFoundException;
import jp.yoshiaki.insuranceapp.training.day69.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品に関する業務操作を担当するService。
 * Controllerから呼ばれ、Repositoryにデータ操作を委譲する。
 *
 * テスト時は @MockBean でこのクラスが「偽物」に差し替わる。
 * つまり、テストではこのクラスの中身は一切動かない。
 */
@Service("day69ProductService")
public class ProductService {

    private final ProductRepository repository;

    // コンストラクタインジェクション（DIでRepositoryを受け取る）
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    /** 全商品を取得する */
    public List<Product> findAll() {
        return repository.findAll();
    }

    /**
     * IDで商品を取得する。
     * 見つからなければ ProductNotFoundException をスローする。
     */
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /** 新規商品を作成して保存する */
    public Product create(String name, int price) {
        Product product = new Product(null, name, price);
        return repository.save(product);
    }
}
