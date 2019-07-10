package realmetamorph.blockchain.network;

import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.*;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.util.ArrayList;

public interface INetMonitor {

    // Сервисное
    void start(Blockchain.WorkMode workMode);

    void stop();

    // Исходящие запросы
    Block getBlock(int blockIndex);

    int getHeight();

    ArrayList<SignedTransaction> getTransactionsByPublicKey(String publicKey);

    ArrayList<SignedTransaction> getTransactionsByType(int type);

    // Отправка транзакции
    void sendTransaction(SignedTransaction transaction);

    // Получение транзакции
    void setCallbackTakeNewTransaction(TakeNewTransactionCallback callback);

    // Входящие запросы
    void setCallbackAskBlock(AskBlockCallback callback);

    void setCallbackAskHeight(AskHeightCallback callback);

    void setCallbackAskNewBlock(AskNewBlockCallback callback);

    // Получение нового блока
    void setCallbackTakeNewBlock(TakeNewBlockCallback callback);


}
