package jp.yoshiaki.insuranceapp.training.day79.deadline;

/**
 * 指定IDの契約が見つからない場合にスローする独自例外。
 *
 * RuntimeException を継承しているため、try-catch の強制はない（非検査例外）。
 * メッセージに「何が見つからなかったか」を含めることで、原因究明が容易になる。
 *
 * 損保アプリでの対応：NotFoundException（exception パッケージ）
 */
public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(String message) {
        super(message);
    }
}
