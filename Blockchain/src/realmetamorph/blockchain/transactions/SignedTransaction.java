package realmetamorph.blockchain.transactions;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignedTransaction {
    private final String publicKey;
    private final String shaHex;
    private final String signature;
    private final byte[] transaction;
    private final ITransaction iTransaction;
    static final int KEY_SIZE = 20; // TODO: Задать константный размер ключа.
    static final int SIGN_SIZE = 20; // TODO: Задать правильный размер подписи

    public SignedTransaction(byte[] transaction) {
        this.publicKey = null;
        this.shaHex = "";
        this.signature = "";
        this.transaction = transaction;
        this.iTransaction = null;
        // TODO: Добавить обратное преобразование
    }

    public SignedTransaction(ITransaction transaction, String publicKey, String privateKey) throws NoSuchAlgorithmException {
        this.publicKey = publicKey;
        byte[] typeBytes = ByteBuffer.allocate(4).putInt(transaction.getType()).array();
        byte[] pkBytes = publicKey.getBytes(StandardCharsets.US_ASCII);
        byte[] data = transaction.getData();
        int allLen = KEY_SIZE + data.length + SIGN_SIZE;
        byte[] lenByte = ByteBuffer.allocate(4).putInt(allLen).array();
        allLen += 8;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(typeBytes);
        digest.update(lenByte);
        digest.update(pkBytes);
        digest.update(data);
        byte[] shaBytes = digest.digest();
        this.shaHex = new BigInteger(1, shaBytes).toString(16);
        this.signature = ITransaction.getSignature(publicKey, privateKey, shaHex);
        byte[] signBytes = signature.getBytes(StandardCharsets.US_ASCII);
        this.transaction = new byte[allLen];
        System.arraycopy(typeBytes, 0, this.transaction, 0, 4);
        System.arraycopy(lenByte, 0, this.transaction, 4, 4);
        System.arraycopy(pkBytes, 0, this.transaction, 8, KEY_SIZE);
        System.arraycopy(data, 0, this.transaction, 8 + KEY_SIZE, data.length);
        System.arraycopy(signBytes, 0, this.transaction, 8 + KEY_SIZE + data.length, SIGN_SIZE);
        iTransaction = transaction;
    }

    public final byte[] getTransactionBytes() {
        return transaction;
    }

    public ITransaction getTransaction() {
        return iTransaction;
    }

    public boolean isValid() {
        return ITransaction.checkSignature(publicKey, signature, shaHex);
    }

}
