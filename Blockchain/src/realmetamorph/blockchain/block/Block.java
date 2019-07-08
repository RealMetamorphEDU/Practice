package realmetamorph.blockchain.block;

import realmetamorph.blockchain.transactions.SignedTransaction;

import java.util.ArrayList;
import java.util.Date;

public class Block {
    private final String prevShaHex = null;
    private final String publicKey = null;
    private final Date timestamp = null;
    private final long nonce = 0;
    private final ArrayList<SignedTransaction> transactions = null;
    private final String mercleRoot = null;
    private final int height = 0;
    private final String signature = null;
    private final int blockSize = 0;


    public Block(byte[] blockBytes) {

    }

    public Block(ArrayList<SignedTransaction> transactionsPool, String publicKey, String privateKey, IBlockGenerator generator, int transactionCount, int blockHeight) {

    }


}
