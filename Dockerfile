# ベースイメージ：Java 21の実行環境（JRE）
# eclipse-temurin は無償のJava実行環境。"21-jre"はJava 21のランタイムのみ（コンパイラは不要）
FROM eclipse-temurin:21-jre

# 作業ディレクトリを /app に設定（コンテナ内のフォルダ）
WORKDIR /app

# ホスト（自分のPC）のtarget/内のJARファイルをコンテナ内にコピー
# ※ファイル名は自分のプロジェクトに合わせて変更してください
COPY target/insuranceapp-0.0.1-SNAPSHOT.jar app.jar

# ドキュメント：このコンテナは8080番ポートを使います
EXPOSE 8080

# コンテナ起動時に実行するコマンド
# java -jar app.jar でSpring Bootを起動する
ENTRYPOINT ["java", "-jar", "app.jar"]