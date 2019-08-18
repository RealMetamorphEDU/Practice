package realmetamorph.blockchain.block;

import realmetamorph.blockchain.transactions.SignedTransaction;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;

import static realmetamorph.blockchain.Blockchain.*;

public class Block {
    private final String prevShaHex;
    private final PublicKey publicKey;
    private final Date timestamp;
    private final long nonce;
    private final int blockHeight;
    private final String mercleRoot;
    private final String shaHex;
    private final String signature;
    private final ArrayList<SignedTransaction> transactions;
    private final int transactionsCount;
    private final int transactionsByteSize;


    public Block(byte[] blockBytes, boolean onlyHeader) throws NoSuchAlgorithmException, InvalidKeySpecException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        byte[] psByte = new byte[32]; // prev sha
        byte[] ncByte = new byte[8]; // nonce
        byte[] pkByte = new byte[KEY_SIZE]; // publick key from block creator
        byte[] tsByte = new byte[8]; // timestamp
        byte[] bhByte = new byte[4]; // blockchain height
        byte[] tcByte = new byte[4]; // transaction count
        byte[] bsByte = new byte[4]; // transactionSize
        byte[] mrByte = new byte[32]; // mercle root
        byte[] snByte = new byte[SIGN_SIZE]; // sign

        System.arraycopy(blockBytes, 0, psByte, 0, 32);
        System.arraycopy(blockBytes, 32, ncByte, 0, 8);
        System.arraycopy(blockBytes, 40, pkByte, 0, KEY_SIZE);
        System.arraycopy(blockBytes, 40 + KEY_SIZE, tsByte, 0, 8);
        System.arraycopy(blockBytes, 48 + KEY_SIZE, bhByte, 0, 4);
        System.arraycopy(blockBytes, 52 + KEY_SIZE, tcByte, 0, 4);
        System.arraycopy(blockBytes, 56 + KEY_SIZE, bsByte, 0, 4);
        System.arraycopy(blockBytes, 60 + KEY_SIZE, mrByte, 0, 32);
        System.arraycopy(blockBytes, 92 + KEY_SIZE, snByte, 0, SIGN_SIZE);
        this.prevShaHex = bytes2hex(psByte);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pkByte);
        this.publicKey = keyFactory.generatePublic(publicKeySpec);
        this.timestamp = new Date(ByteBuffer.wrap(tsByte).getLong());
        this.nonce = ByteBuffer.wrap(ncByte).getLong();
        this.blockHeight = ByteBuffer.wrap(bhByte).getInt();
        this.mercleRoot = bytes2hex(mrByte);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(psByte);
        digest.update(ncByte);
        digest.update(pkByte);
        digest.update(tsByte);
        digest.update(bhByte);
        digest.update(tcByte);
        digest.update(bsByte);
        digest.update(mrByte);
        byte[] shaByte = digest.digest(digest.digest());
        this.shaHex = completeShaString(bytes2hex(shaByte));
        this.signature = bytes2hex(snByte);
        this.transactionsByteSize = ByteBuffer.wrap(bsByte).getInt();
        this.transactions = onlyHeader ? null : new ArrayList<>();
        this.transactionsCount = ByteBuffer.wrap(tcByte).getInt();
        int offset = 0;
        int lastSize = transactionsByteSize;
        while (lastSize != 0 && !onlyHeader) {
            byte[] size = new byte[4];
            System.arraycopy(blockBytes, 96 + KEY_SIZE + SIGN_SIZE + offset, size, 0, 4);
            int len = ByteBuffer.wrap(size).getInt() + 8 + KEY_SIZE * 2 + SIGN_SIZE;
            byte[] data = new byte[len];
            System.arraycopy(blockBytes, 92 + KEY_SIZE + SIGN_SIZE + offset, data, 0, len);
            offset += len;
            lastSize -= len;
            transactions.add(new SignedTransaction(data));
        }
    }

    public Block(ArrayList<SignedTransaction> transactionsPool, KeyPair keys, IBlockGenerator generator, int transactionCount, int blockHeight, String prevShaHex) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        this.prevShaHex = prevShaHex;
        this.publicKey = keys.getPublic();
        this.timestamp = new Date();
        this.blockHeight = blockHeight;
        int len = Math.min(transactionCount, transactionsPool.size());
        if (len == 0)
            throw new IllegalArgumentException("Transaction count mast be > 0!");
        this.transactions = new ArrayList<>(len);
        this.transactionsCount = len;
        int middleLen = 0;
        for (int i = 0; i < len; i++) {
            transactions.add(transactionsPool.get(i));
            middleLen += transactionsPool.get(i).getByteSize();
        }
        transactionsPool.removeAll(transactions);
        transactionsByteSize = middleLen;
        mercleRoot = createMercleRoot(transactions);
        byte[] psByte = hex2bytes(prevShaHex, false); // prev sha
        //nonce is here
        byte[] pkByte = publicKey.getEncoded(); // publick key from block creator
        byte[] tsByte = ByteBuffer.allocate(8).putLong(timestamp.getTime()).array(); // timestamp
        byte[] bhByte = ByteBuffer.allocate(4).putInt(blockHeight).array(); // blockchain height
        byte[] tcByte = ByteBuffer.allocate(4).putInt(len).array(); // transaction count
        byte[] bsByte = ByteBuffer.allocate(4).putInt(transactionsByteSize).array();
        byte[] mrByte = hex2bytes(mercleRoot, false); // mercle root
        nonce = new NonceGenerator(psByte, pkByte, tsByte, bhByte, tcByte, bsByte, mrByte).generateNonce(generator);
        byte[] ncByte = ByteBuffer.allocate(8).putLong(nonce).array();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(psByte);
        digest.update(ncByte);
        digest.update(pkByte);
        digest.update(tsByte);
        digest.update(bhByte);
        digest.update(tcByte);
        digest.update(bsByte);
        digest.update(mrByte);
        byte[] shaByte = digest.digest(digest.digest());
        this.shaHex = completeShaString(bytes2hex(shaByte));
        signature = createSignature(keys, shaHex);
    }

    private static String createMercleRoot(ArrayList<SignedTransaction> transactions) throws NoSuchAlgorithmException {
        if (transactions.size() == 0)
            return completeShaString("");
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
                shas.add(completeShaString(bytes2hex(digest.digest(digest.digest(hex2bytes(shas.get(i) + shas.get(i + 1), false))))));
                newCount++;
            }
            if (last) {
                shas.add(completeShaString(bytes2hex(digest.digest(digest.digest(hex2bytes(shas.get(count + offset) + shas.get(count + offset), false))))));
                newCount++;
                count++;
            }

            offset += count;
            count = newCount;
        }
        return shas.get(shas.size() - 1);
    }

    public String getPrevShaHex() {
        return prevShaHex;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public long getNonce() {
        return nonce;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public String getMercleRoot() {
        return mercleRoot;
    }

    public String getShaHex() {
        return shaHex;
    }

    public String getSignature() {
        return signature;
    }

    public int getTransactionsCount() {
        return transactionsCount;
    }

    public SignedTransaction getTransaction(int index) {
        return transactions.get(index);
    }

    public byte[] getBlockData() {
        byte[] psByte = hex2bytes(prevShaHex, false); // prev sha
        byte[] ncByte = ByteBuffer.allocate(8).putLong(nonce).array(); // nonce
        byte[] pkByte = publicKey.getEncoded(); // publick key from block creator
        byte[] tsByte = ByteBuffer.allocate(8).putLong(timestamp.getTime()).array(); // timestamp
        byte[] bhByte = ByteBuffer.allocate(4).putInt(blockHeight).array(); // blockchain height
        byte[] tcByte = ByteBuffer.allocate(4).putInt(transactionsCount).array(); // transaction count
        byte[] bsByte = ByteBuffer.allocate(4).putInt(transactionsByteSize).array();
        byte[] mrByte = hex2bytes(mercleRoot, false); // mercle root
        byte[] snByte = hex2bytes(signature, true); // sign

        int allLen = 92 + KEY_SIZE + SIGN_SIZE + transactionsByteSize;

        byte[] data = new byte[allLen];
        System.arraycopy(psByte, 0, data, 0, 32);
        System.arraycopy(ncByte, 0, data, 32, 8);
        System.arraycopy(pkByte, 0, data, 40, KEY_SIZE);
        System.arraycopy(tsByte, 0, data, 40 + KEY_SIZE, 8);
        System.arraycopy(bhByte, 0, data, 48 + KEY_SIZE, 4);
        System.arraycopy(tcByte, 0, data, 52 + KEY_SIZE, 4);
        System.arraycopy(bsByte, 0, data, 56 + KEY_SIZE, 4);
        System.arraycopy(mrByte, 0, data, 60 + KEY_SIZE, 32);
        System.arraycopy(snByte, 0, data, 92 + KEY_SIZE + (SIGN_SIZE - snByte.length), snByte.length);
        int offset = 0;
        for (SignedTransaction trn : transactions) {
            System.arraycopy(trn.getTransactionBytes(), 0, data, 92 + KEY_SIZE + SIGN_SIZE + offset, trn.getByteSize());
            offset += trn.getByteSize();
        }
        return data;
    }


    public boolean isValid() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return checkSignature(publicKey, signature, shaHex);
    }

    public static int getHeaderSize() {
        return 92 + KEY_SIZE + SIGN_SIZE;
    }

    public int getTransactionsByteSize() {
        return transactionsByteSize;
    }
}
