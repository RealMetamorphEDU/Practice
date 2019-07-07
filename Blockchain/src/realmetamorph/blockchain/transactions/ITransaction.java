package realmetamorph.blockchain.transactions;

public interface ITransaction {

    // Возвращает тип транзакции.
    int getType();
    // Публичный ключ того, кто отправляет транзакцию.
    String getPublicKey();
    // Приватный ключ того, кто отправляет транзакцию.
    String getPrivateKey();
    // Восстанавливает данные из массива байтов.
    void parseData(byte[] data);
    // Сохраняет данные в массив байтов, тип транзакции и публичный ключ НЕ сохраняются.
    byte[] getData();

    // Создание подписи, на вход передаётся публичный ключ, приватный ключ и транзакция
    static String getSignature(String publicKey, String privateKey, byte[] transaction){

        return null;
    }
    // Проверка подписи, на вход передаётся публичный ключ, подпись и транзакия
    static boolean checkSignature(String publicKey, String signature, byte[] transaction){

        return false;
    }

}
