package realmetamorph.blockchain.transactions;

import realmetamorph.blockchain.Blockchain;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignedTransaction {
    private final String publicKey;
    private final String receiverKey;
    private final String shaHex;
    private final String signature;
    private final byte[] transaction;
    private final ITransaction iTransaction;
    static final int KEY_SIZE = 20; // TODO: Задать константный размер ключа.
    static final int SIGN_SIZE = 20; // TODO: Задать правильный размер подписи

    public SignedTransaction(byte[] transaction) throws NoSuchAlgorithmException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.transaction = transaction;
        byte[] typeBytes = new byte[4];
        byte[] lenByte = new byte[4];
        byte[] pkBytes = new byte[KEY_SIZE];
        byte[] rcBytes = new byte[KEY_SIZE];
        System.arraycopy(transaction, 0, typeBytes, 0, 4);
        System.arraycopy(transaction, 4, lenByte, 0, 4);
        System.arraycopy(transaction, 8, pkBytes, 0, KEY_SIZE);
        System.arraycopy(transaction, 8 + KEY_SIZE, rcBytes, 0, KEY_SIZE);
        int type = ByteBuffer.wrap(typeBytes).getInt();
        int lenAll = ByteBuffer.wrap(lenByte).getInt();
        this.publicKey = new String(pkBytes, StandardCharsets.US_ASCII);
        this.receiverKey = new String(rcBytes, StandardCharsets.US_ASCII);
        int dataLen = lenAll - KEY_SIZE * 2 - SIGN_SIZE;
        byte[] data = new byte[dataLen];
        byte[] signBytes = new byte[SIGN_SIZE];
        System.arraycopy(transaction, 8 + KEY_SIZE * 2, data, 0, dataLen);
        System.arraycopy(transaction, 8 + KEY_SIZE * 2 + dataLen, signBytes, 0, SIGN_SIZE);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(typeBytes);
        digest.update(lenByte);
        digest.update(pkBytes);
        digest.update(data);
        byte[] shaBytes = digest.digest();
        this.shaHex = new BigInteger(1, shaBytes).toString(16);
        Class<?> transactionClass = Blockchain.getTransactionClass(type);
        if (transactionClass == null) {
            this.iTransaction = null;
        } else {
            this.iTransaction = (ITransaction) transactionClass.getConstructor().newInstance();
            this.iTransaction.parseData(data);
        }
        this.signature = new String(signBytes, StandardCharsets.US_ASCII);
        ;
    }

    public SignedTransaction(ITransaction transaction, String publicKey, String privateKey, String receiverKey) throws NoSuchAlgorithmException {
        this.publicKey = publicKey;
        this.receiverKey = receiverKey;
        byte[] typeBytes = ByteBuffer.allocate(4).putInt(transaction.getType()).array();
        byte[] pkBytes = publicKey.getBytes(StandardCharsets.US_ASCII);
        byte[] rcBytes = receiverKey.getBytes(StandardCharsets.US_ASCII);
        byte[] data = transaction.getData();
        int allLen = KEY_SIZE * 2 + data.length + SIGN_SIZE;
        byte[] lenByte = ByteBuffer.allocate(4).putInt(allLen).array();
        allLen += 8;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(typeBytes);
        digest.update(lenByte);
        digest.update(pkBytes);
        digest.update(data);
        byte[] shaBytes = digest.digest();
        this.shaHex = new BigInteger(1, shaBytes).toString(16);
        this.signature = Blockchain.getSignature(publicKey, privateKey, shaHex);
        byte[] signBytes = signature.getBytes(StandardCharsets.US_ASCII);
        this.transaction = new byte[allLen];
        System.arraycopy(typeBytes, 0, this.transaction, 0, 4);
        System.arraycopy(lenByte, 0, this.transaction, 4, 4);
        System.arraycopy(pkBytes, 0, this.transaction, 8, KEY_SIZE);
        System.arraycopy(rcBytes, 0, this.transaction, 8 + KEY_SIZE, KEY_SIZE);
        System.arraycopy(data, 0, this.transaction, 8 + KEY_SIZE * 2, data.length);
        System.arraycopy(signBytes, 0, this.transaction, 8 + KEY_SIZE * 2 + data.length, SIGN_SIZE);
        iTransaction = transaction;
    }

    public final byte[] getTransactionBytes() {
        return transaction;
    }

    public ITransaction getTransaction() {
        return iTransaction;
    }

    public boolean isValid() {
        return Blockchain.checkSignature(publicKey, signature, shaHex);
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getReceiverKey() {
        return receiverKey;
    }

    public String getShaHex() {
        return shaHex;
    }

    public String getSignature() {
        return signature;
    }
}
