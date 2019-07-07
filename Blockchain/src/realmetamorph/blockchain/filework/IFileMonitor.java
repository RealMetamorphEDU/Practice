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
import realmetamorph.blockchain.callbacks.AskedTransactionsCallback;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.util.ArrayList;

public interface IFileMonitor {

    void start(Blockchain.WorkMode workMode);

    void stop();

    void setNewBlockInterval(int minutes);

    ArrayList<SignedTransaction> getTransactionsByPublicKey(String publicKey);

    Block getBlock(int blockIndex);

    void putNewBlock(Block block);

    void setCallbackAskNewBlock(AskNewBlockCallback callback);

}
