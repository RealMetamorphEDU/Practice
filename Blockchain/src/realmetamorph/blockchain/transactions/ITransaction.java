package realmetamorph.blockchain.transactions;

public interface ITransaction {

    // Возвращает тип транзакции.
    int getType();

    // Восстанавливает данные из массива байтов.
    void parseData(byte[] data);

    // Сохраняет данные в массив байтов, тип транзакции и публичный ключ НЕ сохраняются.
    byte[] getData();

}
