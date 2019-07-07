package realmetamorph.blockchain.transactions;

public interface ITransaction {

    // Возвращает тип транзакции.
    int getType();

    // Восстанавливает данные из массива байтов.
    void parseData(byte[] data);

    // Сохраняет данные в массив байтов, тип транзакции и публичный ключ НЕ сохраняются.
    byte[] getData();

    // Создание подписи, на вход передаётся публичный ключ, приватный ключ и хеш транзакции
    static String getSignature(String publicKey, String privateKey, String shaHex) {
        // TODO: Реализовать создание подписи.
        return null;
    }

    // Проверка подписи, на вход передаётся публичный ключ, подпись и хеш транзакции
    static boolean checkSignature(String publicKey, String signature, String shaHex) {
        // TODO: Реализовать проверку подписи.
        return false;
    }

}
