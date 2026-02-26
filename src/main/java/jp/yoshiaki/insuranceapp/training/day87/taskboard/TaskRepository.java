package jp.yoshiaki.insuranceapp.training.day87.taskboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * タスクの永続化を担当するリポジトリ。
 * JpaRepository を継承するだけで基本的な CRUD メソッドが使える。
 *
 * Bean名に day87 接頭辞を付けて、他の Day との衝突を防止する。
 */
@Repository("day87TaskRepository")
public interface TaskRepository extends JpaRepository<Task, Long> {
    // 基本 CRUD（save / findAll / findById / deleteById）は
    // JpaRepository が提供するため、追加定義は不要
}
