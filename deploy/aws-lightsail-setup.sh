#!/usr/bin/env bash
# ============================================================
# 損保アプリ Lightsail 初期セットアップスクリプト
# ============================================================
# 対象OS: Ubuntu 22.04（AWS Lightsail）
#
# このスクリプトの目的:
# - Docker / Docker Compose / Git の導入
# - Docker サービスの有効化
# - プロジェクト配置先ディレクトリの作成
#
# 実行例:
#   chmod +x aws-lightsail-setup.sh
#   ./aws-lightsail-setup.sh
#
# 注意:
# - 実行ユーザーは通常 "ubuntu"（Lightsail標準）
# - 実行後、docker グループ反映のため「再ログイン」が必要
# ============================================================

set -euo pipefail

# --- 0) 事前に使う変数を定義 ---
# 現在ログイン中のユーザー（通常は ubuntu）
CURRENT_USER="$(id -un)"
# プロジェクト配置先（このプロジェクト構成に合わせる）
PROJECT_DIR="$HOME/InsuranceApp-SpringBoot"

echo "=========================================="
echo " 損保アプリ Lightsail セットアップ開始"
echo "=========================================="
echo "実行ユーザー: ${CURRENT_USER}"

# --- 1) システムパッケージ更新 ---
echo ""
echo "[1/7] システムパッケージを更新しています..."
sudo apt update -y
sudo apt upgrade -y

# --- 2) Docker導入に必要な基本ツールをインストール ---
echo ""
echo "[2/7] 必要ツール（curl / gnupg / git など）をインストールしています..."
sudo apt install -y ca-certificates curl gnupg git

# --- 3) Docker公式リポジトリを追加 ---
echo ""
echo "[3/7] Docker公式リポジトリを設定しています..."
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# --- 4) Docker / Composeプラグインをインストール ---
echo ""
echo "[4/7] Docker Engine / Docker Compose をインストールしています..."
sudo apt update -y
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# --- 5) Dockerサービスを有効化し、一般ユーザー実行を許可 ---
echo ""
echo "[5/7] Dockerサービスの有効化とユーザー権限の設定をしています..."
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker "${CURRENT_USER}"

# --- 6) プロジェクトディレクトリを作成 ---
echo ""
echo "[6/7] プロジェクト配置先ディレクトリを作成しています..."
mkdir -p "${PROJECT_DIR}"

# --- 7) バージョン確認 ---
echo ""
echo "[7/7] インストール結果を確認しています..."
echo ""
echo "--- Docker ---"
sudo docker --version
echo ""
echo "--- Docker Compose ---"
sudo docker compose version
echo ""
echo "--- Git ---"
git --version

echo ""
echo "=========================================="
echo " セットアップ完了"
echo "=========================================="
echo ""
echo "【次のステップ（このプロジェクト向け）】"
echo "1. いったんログアウトして再ログイン（dockerグループ反映）"
echo "   exit"
echo "   ssh -i <LightsailKey>.pem ubuntu@<LightsailのパブリックIP>"
echo ""
echo "2. ローカルPCからプロジェクトをサーバーへ転送"
echo "   scp -i <LightsailKey>.pem -r ./InsuranceApp-SpringBoot ubuntu@<LightsailのパブリックIP>:~/"
echo ""
echo "3. サーバー上で .env を本番向けに調整（特に DB_URL）"
echo "   cd ~/InsuranceApp-SpringBoot"
echo "   cp .env .env.backup"
echo "   # 例: DB_URL は mysql サービス名宛てにする"
echo "   # DB_URL=jdbc:mysql://mysql:3306/insuranceapp?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Tokyo"
echo ""
echo "4. アプリ起動（プロジェクトルートの docker-compose.yaml を使用）"
echo "   cd ~/InsuranceApp-SpringBoot"
echo "   docker compose up -d --build"
echo ""
echo "5. 動作確認"
echo "   http://<LightsailのパブリックIP>:8080"
echo ""
echo "補足: Lightsailネットワーク設定で 8080/TCP を許可してください。"
