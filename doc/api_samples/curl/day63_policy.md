# Day63 Policy API（手動確認：PowerShell curl）

## 前提
- Spring Boot アプリが起動していること
- ログに `Tomcat started on port 8080` が出ていること

## ベースURL
- http://localhost:8080

## エンドポイント
- POST /policies（新規登録）
- GET  /policies（一覧取得）

---

## 1) 一覧取得（GET）
### PowerShell
```powershell
curl "http://localhost:8080/policies"
````

期待結果：

* 初回は `[]`（空配列）

---

## 2) 新規登録（POST）

### PowerShell（正常系）

```powershell
curl -Method POST "http://localhost:8080/policies" `
  -ContentType "application/json" `
  -Body '{ "customerName":"高橋健一", "startDate":"2026-02-02", "premium":12000 }'
```

期待結果：

* 201 Created（または作成したデータのJSONが返る）

登録後に GET を実行して、1件入っていることを確認する：

```powershell
curl "http://localhost:8080/policies"
```

---

## 3) 入力検証（400を出す）

### 3-1) customerName を空にする（NotBlank想定）

```powershell
curl -Method POST "http://localhost:8080/policies" `
  -ContentType "application/json" `
  -Body '{ "customerName":"", "startDate":"2026-02-02", "premium":12000 }'
```

期待結果：

* 400 Bad Request

### 3-2) premium を 0 以下にする（Positive想定）

```powershell
curl -Method POST "http://localhost:8080/policies" `
  -ContentType "application/json" `
  -Body '{ "customerName":"高橋健一", "startDate":"2026-02-02", "premium":0 }'
```

期待結果：

* 400 Bad Request

---

## よくある失敗と切り分け

* 404になる：`/policies` のスペル、`@RequestMapping("/policies")` を確認
* 500になる：例外ログを確認（バリデーション依存や型変換など）
* startDateで失敗：日付は `YYYY-MM-DD` 形式で送る（例：`2026-02-02`）
* 400でも内容が見づらい：現状はデフォルト形式の可能性あり（後日エラー形式統一で改善）

