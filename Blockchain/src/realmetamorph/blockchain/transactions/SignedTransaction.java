package realmetamorph.blockchain.transactions;

import com.sun.istack.internal.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static realmetamorph.blockchain.Blockchain.*;

public class SignedTransaction {
    private final PublicKey publicKey;
    private final String receiverKey;
    private final String shaHex;
    private final String signature;
    private final int size;
    private final ITransaction iTransaction;

    public SignedTransaction(byte[] transaction) throws NoSuchAlgorithmException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, InvalidKeySpecException {
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
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pkBytes);
        this.publicKey = keyFactory.generatePublic(publicKeySpec);
        this.receiverKey = bytes2hex(rcBytes);
        this.size = lenAll + 8 + KEY_SIZE * 2 + SIGN_SIZE;
        byte[] data = new byte[lenAll];
        byte[] signBytes = new byte[SIGN_SIZE];
        System.arraycopy(transaction, 8 + KEY_SIZE * 2, data, 0, lenAll);
        System.arraycopy(transaction, 8 + KEY_SIZE * 2 + lenAll, signBytes, 0, SIGN_SIZE);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(typeBytes);
        digest.update(lenByte);
        digest.update(pkBytes);
        digest.update(rcBytes);
        digest.update(data);
        byte[] shaBytes = digest.digest(digest.digest());
        this.shaHex = completeShaString(bytes2hex(shaBytes));
        Class<?> transactionClass = getTransactionClass(type);
        if (transactionClass == null) {
            this.iTransaction = null;
        } else {
            this.iTransaction = (ITransaction) transactionClass.getConstructor().newInstance();
            this.iTransaction.parseData(data);
        }
        this.signature = bytes2hex(signBytes);
    }

    public SignedTransaction(@NotNull ITransaction iTransaction, KeyPair keys, String receiverKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        this.publicKey = keys.getPublic();
        this.receiverKey = receiverKey;
        byte[] typeBytes = ByteBuffer.allocate(4).putInt(iTransaction.getType()).array();
        byte[] pkBytes = publicKey.getEncoded();
        byte[] rcBytes = hex2bytes(receiverKey, false);
        byte[] data = iTransaction.getData();
        int allLen = data.length;
        byte[] lenByte = ByteBuffer.allocate(4).putInt(allLen).array();
        this.size = allLen + 8 + KEY_SIZE * 2 + SIGN_SIZE;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(typeBytes);
        digest.update(lenByte);
        digest.update(pkBytes);
        digest.update(rcBytes);
        digest.update(data);
        byte[] shaBytes = digest.digest(digest.digest());
        this.shaHex = completeShaString(bytes2hex(shaBytes));
        this.signature = createSignature(keys, shaHex);
        this.iTransaction = iTransaction;
    }

    public final byte[] getTransactionBytes() {
        byte[] typeBytes = ByteBuffer.allocate(4).putInt(iTransaction.getType()).array();
        byte[] pkBytes = publicKey.getEncoded();
        byte[] rcBytes = hex2bytes(receiverKey, false);
        byte[] data = iTransaction.getData();
        int allLen = data.length;
        byte[] lenByte = ByteBuffer.allocate(4).putInt(allLen).array();
        allLen += 8 + KEY_SIZE * 2 + SIGN_SIZE;
        byte[] signBytes = hex2bytes(signature, true);
        byte[] transaction = new byte[allLen];
        System.arraycopy(typeBytes, 0, transaction, 0, 4);
        System.arraycopy(lenByte, 0, transaction, 4, 4);
        System.arraycopy(pkBytes, 0, transaction, 8, KEY_SIZE);
        System.arraycopy(rcBytes, 0, transaction, 8 + KEY_SIZE, KEY_SIZE);
        System.arraycopy(data, 0, transaction, 8 + KEY_SIZE * 2, data.length);
        System.arraycopy(signBytes, 0, transaction, 8 + KEY_SIZE * 2 + data.length + (SIGN_SIZE - signBytes.length), signBytes.length);
        return transaction;
    }

    public ITransaction getTransaction() {
        return iTransaction;
    }

    public boolean isValid() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return checkSignature(publicKey, signature, shaHex);
    }

    public PublicKey getPublicKey() {
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

    public int getByteSize() {
        return size;
    }

    public int getHeaderSize() {
        return 8 + KEY_SIZE * 2 + SIGN_SIZE;
    }
}
