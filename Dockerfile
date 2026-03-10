# ==============================================================
# 損保業務支援アプリ - Dockerfile
# マルチステージビルド：ビルド用(JDK) → 実行用(JRE)
# ==============================================================

# ─── ステージ1：ビルド（JDK入りの大きいイメージ）───
FROM eclipse-temurin:21-jdk-alpine AS build

# 作業ディレクトリを /app に設定
WORKDIR /app

# ① Maven Wrapper と pom.xml を先にコピー（依存DLのキャッシュ活用）
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# ② 依存ライブラリだけ先にダウンロード
#    → ソースコードを変えても、ここまでのレイヤーはキャッシュされる
RUN chmod +x mvnw && ./mvnw dependency:resolve

# ③ ソースコードをコピー
COPY src ./src

# ④ アプリをビルド（テストはスキップ：CIで別途実行する想定）
RUN ./mvnw clean package -DskipTests

# ─── ステージ2：実行（JREのみの軽量イメージ）───
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# ⑤ ビルドステージから jar だけをコピー（ビルドツールは持ち込まない）
COPY --from=build /app/target/*.jar app.jar

# ⑥ タイムゾーンを日本に設定（ログの時刻がJSTになる）
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Tokyo /etc/localtime && \
    echo "Asia/Tokyo" > /etc/timezone && \
    apk del tzdata

# ⑦ Spring Boot のデフォルトポートを公開（ドキュメント目的）
EXPOSE 8080

# ⑧ コンテナ起動時に jar を実行
ENTRYPOINT ["java", "-Xms64m", "-Xmx256m", "-jar", "app.jar"]

