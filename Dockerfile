# ==========================================================
# Day87 Dockerfile
# Spring Boot アプリを Docker コンテナ化する。
# Day86 の復習 + 環境変数・ボリュームの対応を追加。
# ==========================================================

# ① ベースイメージ：Java 21 が入った軽量 Linux（Eclipse Temurin）
FROM eclipse-temurin:21-jre-alpine

# ② 作業ディレクトリを /app に設定（この中にアプリを置く）
WORKDIR /app

# ③ ビルド済みの jar ファイルをコンテナ内にコピー
#    Maven の場合: target/*.jar
#    ※ビルドは事前に mvn package で行っておく
COPY target/insuranceapp-0.0.1-SNAPSHOT.jar app.jar

# ④ データ永続化用のディレクトリを作成
#    H2 ファイル DB の保存先として /data を使う
#    docker run -v で外部にマウントすればデータが残る
RUN mkdir -p /data

# ⑤ VOLUME 宣言：/data が永続化対象であることを示す
#    （docker run 時に -v を指定しなくても、匿名ボリュームが自動で作られる）
VOLUME /data

# ⑥ コンテナがリッスンするポート（ドキュメント的な意味合い）
#    実際のポートは SERVER_PORT 環境変数で変更可能
EXPOSE 8080

# ⑦ コンテナ起動時に実行するコマンド
#    java -jar で Spring Boot アプリを起動する
ENTRYPOINT ["java", "-jar", "app.jar"]
