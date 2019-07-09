package realmetamorph.blockchain.block;

import realmetamorph.blockchain.transactions.SignedTransaction;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

public class Block {
    private final String prevShaHex;
    private final String publicKey;
    private final Date timestamp;
    private final long nonce;
    private final int blockHeight;
    private final String mercleRoot;
    private final String signature;
    private final ArrayList<SignedTransaction> transactions;

    public Block(byte[] blockBytes) {
        transactions = null;
        blockHeight = 0;
        prevShaHex = null;
        publicKey = null;
        timestamp = null;
        nonce = 0;
        mercleRoot = null;
        signature = null;
    }

    public Block(ArrayList<SignedTransaction> transactionsPool, String publicKey, String privateKey, IBlockGenerator generator, int transactionCount, int blockHeight, String prevShaHex) throws NoSuchAlgorithmException {
        this.blockHeight = blockHeight;
        this.prevShaHex = prevShaHex;
        this.publicKey = publicKey;
        this.timestamp = new Date();
        int len = Math.min(transactionCount, transactionsPool.size());
        this.transactions = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            transactions.add(transactionsPool.get(i));
        }
        transactionsPool.removeAll(transactions);
        mercleRoot = createMercleRoot(transactions);
        nonce = 0;

        signature = null;
    }


    private static String createMercleRoot(ArrayList<SignedTransaction> transactions) throws NoSuchAlgorithmException {
        ArrayList<String> shas = new ArrayList<>(transactions.size());
        for (SignedTransaction transaction : transactions) {
            shas.add(transaction.getShaHex());
        }
        int count = transactions.size();
        int offset = 0;
        int newCount;
        boolean last;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        while (count != 1) {
            newCount = 0;
            last = (count & 1) == 1;
            if (last)
                count--;
            for (int i = offset; i < count + offset; i += 2) {
                BigInteger dblSha = new BigInteger(shas.get(i) + shas.get(i + 1), 16);
                shas.add(new BigInteger(1, digest.digest(digest.digest(dblSha.toByteArray()))).toString(16));
                newCount++;
            }
            if (last) {
                BigInteger dblSha = new BigInteger(shas.get(count + offset) + shas.get(count + offset), 16);
                shas.add(new BigInteger(1, digest.digest(digest.digest(dblSha.toByteArray()))).toString(16));
                newCount++;
                count++;
            }

            offset += count;
            count = newCount;
        }
        return shas.get(shas.size() - 1);
    }


}
