import com.mongodb.MongoClient;
import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.*;
import realmetamorph.blockchain.network.INetMonitor;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.util.ArrayList;

/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 17.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class NetMonitor implements INetMonitor {

    private MongoClient client;
    private TakeNewBlockCallback takeNewBlockCallback;
    private AskNewBlockCallback askNewBlockCallback;
    private AskHeightCallback askHeightCallback;
    private AskBlockCallback askBlockCallback;
    private TakeNewTransactionCallback takeNewTransactionCallback;

    public NetMonitor(MongoClient client) {
        this.client = client;
    }

    @Override
    public void start(Blockchain.WorkMode workMode) {
        // TODO
    }

    @Override
    public void stop() {
        // TODO
    }

    @Override
    public Block getBlock(int i, boolean b) {
        // ignored
        return null;
    }

    @Override
    public int getHeight() {
        // ignored
        return 0;
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByPublicKey(String s) {
        // ignored
        return null;
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByType(int i) {
        // ignored
        return null;
    }

    @Override
    public void sendTransaction(SignedTransaction signedTransaction) {
        // ignored
    }

    @Override
    public void setCallbackTakeNewTransaction(TakeNewTransactionCallback takeNewTransactionCallback) {
        this.takeNewTransactionCallback = takeNewTransactionCallback;
    }

    @Override
    public void setCallbackAskBlock(AskBlockCallback askBlockCallback) {
        this.askBlockCallback = askBlockCallback;
    }

    @Override
    public void setCallbackAskHeight(AskHeightCallback askHeightCallback) {
        this.askHeightCallback = askHeightCallback;
    }

    @Override
    public void setCallbackAskNewBlock(AskNewBlockCallback askNewBlockCallback) {
        this.askNewBlockCallback = askNewBlockCallback;
    }

    @Override
    public void setCallbackTakeNewBlock(TakeNewBlockCallback takeNewBlockCallback) {
        this.takeNewBlockCallback = takeNewBlockCallback;
    }
}
