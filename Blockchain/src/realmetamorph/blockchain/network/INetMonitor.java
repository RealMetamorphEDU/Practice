package realmetamorph.blockchain.network;

import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.callbacks.*;
import realmetamorph.blockchain.transactions.SignedTransaction;

public interface INetMonitor {

    void start(Blockchain.WorkMode workMode);

    void stop();

    void askNewBlockInterval(int minutes);

    void sendTransaction(SignedTransaction transaction);

    void askBlockchain();

    void askBlock(int blockIndex);

    void askBlockCount();

    void askTransactionsByPublicKey(String publicKey);

    void setCallbackAskBlockchain(AskBlockchainCallback callback);

    void setCallbackTakeBlockCount(AskBlockchainCallback callback);

    void setCallbackTakeBlock(TakeBlockCallback callback);

    void setCallbackPutBlock(PutBlockCallback callback);

    void setCallbackTakeNewTransaction(TakeNewTransactionCallback callback);

    void setCallbackAskedTransactions(AskedTransactionsCallback callback);

    void setCallbackAskNewBlock(AskNewBlockCallback callback);

}
