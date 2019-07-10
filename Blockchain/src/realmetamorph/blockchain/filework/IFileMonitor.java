/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 7.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.blockchain.filework;

import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.AskNewBlockCallback;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.util.ArrayList;

public interface IFileMonitor {

    void start(Blockchain.WorkMode workMode);

    void stop();

    // Исходящие запросы
    Block getBlock(int blockIndex);

    int getHeight();

    ArrayList<SignedTransaction> getTransactionsByPublicKey(String publicKey);

    ArrayList<SignedTransaction> getTransactionsByType(int type);

    void addNewBlock(Block block);

    // Входящие запросы
    void setCallbackAskNewBlock(AskNewBlockCallback callback);

    boolean validator(SignedTransaction transaction);

}
