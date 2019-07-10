package realmetamorph.blockchain;

import com.sun.istack.internal.NotNull;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.block.IBlockGenerator;
import realmetamorph.blockchain.filework.IFileMonitor;
import realmetamorph.blockchain.network.INetMonitor;
import realmetamorph.blockchain.transactions.ITransaction;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Blockchain {

    private WorkMode workMode;
    private IFileMonitor file;
    private INetMonitor net;
    private IBlockGenerator blockGenerator;

    private static HashMap<Integer, Class<?>> registeredTransactions = new HashMap<>();
    private ArrayList<SignedTransaction> transactionsPool;

    private boolean started;

    private KeyPair keys;

    public static final int KEY_SIZE = 91; // Public key byte size. Private byte size 67.
    public static final int SIGN_SIZE = 72; // sign

    public Blockchain(@NotNull WorkMode workMode, IFileMonitor file, INetMonitor net, @NotNull IBlockGenerator blockGenerator, @NotNull KeyPair keys) {
        if (workMode == null)
            throw new NullPointerException("WorkMode is required!");
        if (blockGenerator == null)
            throw new NullPointerException("BlockGenerator is required!");
        if (workMode == WorkMode.SINGLE_MODE && file == null)
            throw new NullPointerException("FileMonitor is required for single mode!");
        if (workMode == WorkMode.NODE_MODE && file == null)
            throw new NullPointerException("FileMonitor is required for node mode!");
        if (workMode == WorkMode.NODE_MODE && net == null)
            throw new NullPointerException("NetMonitor is required for node mode!");
        if (workMode == WorkMode.SEND_MODE && net == null)
            throw new NullPointerException("NetMonitor is required for send mode!");
        this.workMode = workMode;
        this.net = net;
        this.file = file;
        this.blockGenerator = blockGenerator;
        this.keys = keys;
        transactionsPool = new ArrayList<>();
        if (workMode == WorkMode.SINGLE_MODE)
            file.setCallbackAskNewBlock((int count) -> {
                Block prevBlock = file.getBlock(file.getHeight());
                try {
                    return new Block(transactionsPool, keys, blockGenerator, count < 0 ? 30 : count, prevBlock.getBlockHeight(), prevBlock.getShaHex());
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                    return null;
                }
            });
        if (workMode == WorkMode.NODE_MODE)
            net.setCallbackAskBlock(file::getBlock);
        if (workMode == WorkMode.NODE_MODE)
            net.setCallbackAskHeight(file::getHeight);
        if (workMode == WorkMode.NODE_MODE)
            net.setCallbackAskNewBlock((int count) -> {
                Block prevBlock = file.getBlock(file.getHeight());
                try {
                    Block block = new Block(transactionsPool, keys, blockGenerator, count < 0 ? blockGenerator.maxTransactionCount() : count, prevBlock.getBlockHeight(), prevBlock.getShaHex());
                    file.addNewBlock(block);
                    return block;
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                    return null;
                }
            });
        if (workMode == WorkMode.NODE_MODE)
            net.setCallbackTakeNewBlock((Block block) -> {
                if (block.isValid()) {
                    file.addNewBlock(block);
                    return true;
                }
                return false;
            });
        if (workMode == WorkMode.NODE_MODE)
            net.setCallbackTakeNewTransaction((SignedTransaction signedTransaction) -> {
                if (signedTransaction.isValid() && file.validator(signedTransaction)) {
                    transactionsPool.add(signedTransaction);
                    return true;
                }
                return false;
            });
    }

    public void start() {
        if (!started) {
            file.start(workMode);
            if (workMode != WorkMode.SINGLE_MODE) {
                net.start(workMode);
            }
            started = true;
        }
    }

    public void stop() {
        if (started) {
            file.stop();
            if (workMode != WorkMode.SINGLE_MODE) {
                net.stop();
            }
            started = false;
        }
    }

    public static boolean registerTransaction(Class<?> transaction) {
        ArrayList<Class<?>> arrayList = new ArrayList<>(transaction.getInterfaces().length);
        Collections.addAll(arrayList, transaction.getInterfaces());
        if (arrayList.contains(ITransaction.class)) {
            try {
                ITransaction ta = (ITransaction) transaction.getConstructor().newInstance();
                registeredTransactions.put(ta.getType(), transaction);
                return true;
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ignored) {
                //Oops
                return false;
            }
        }
        return false;
    }

    public static Class<?> getTransactionClass(int type) {
        return registeredTransactions.getOrDefault(type, null);
    }

    public boolean addTransaction(ITransaction transaction, String receiverKey) {
        if (!registeredTransactions.containsKey(transaction.getType()))
            return false;
        SignedTransaction signedTransaction = null;
        try {
            signedTransaction = new SignedTransaction(transaction, keys, receiverKey);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            return false;
        }
        if (file != null)
            if (!file.validator(signedTransaction))
                return false;
        if (workMode == WorkMode.SINGLE_MODE) {
            transactionsPool.add(signedTransaction);
        }
        if (workMode == WorkMode.NODE_MODE) {
            transactionsPool.add(signedTransaction);
            net.sendTransaction(signedTransaction);
        }
        if (workMode == WorkMode.SEND_MODE)
            net.sendTransaction(signedTransaction);
        return true;
    }

    public ArrayList<SignedTransaction> getTransactionsByPublicKey(String publicKey) {
        if (workMode == WorkMode.SEND_MODE)
            return net.getTransactionsByPublicKey(publicKey);
        return file.getTransactionsByPublicKey(publicKey);
    }

    public ArrayList<SignedTransaction> getTransactionsByType(int type) {
        if (workMode == WorkMode.SEND_MODE)
            return net.getTransactionsByType(type);
        return file.getTransactionsByType(type);
    }

    public Block getBlockByIndex(int indexBlock) {
        if (workMode != WorkMode.SEND_MODE)
            return file.getBlock(indexBlock);
        else
            return net.getBlock(indexBlock);
    }

    // Создание подписи, на вход передаётся публичный ключ, приватный ключ и хеш транзакции
    public static String createSignature(KeyPair keys, String shaHex) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(keys.getPrivate());
        signature.update(hex2bytes(shaHex, false));
        byte[] sign = signature.sign();
        return bytes2hex(sign);
    }

    // Проверка подписи, на вход передаётся публичный ключ, подпись и хеш транзакции
    public static boolean checkSignature(PublicKey publicKey, String sign, String shaHex) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);
        signature.update(hex2bytes(shaHex, false));
        return signature.verify(hex2bytes(sign, true));
    }

    public static String completeShaString(String sha) {
        return completeStringWith(sha, 64);
    }

    private static String completeStringWith(String string, int len) {
        int need = len - string.length();
        if (need == 0)
            return string;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < need; i++) {
            builder.append("0");
        }
        return builder.append(string).toString();
    }

    public static String bytes2hex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (byte aByte : bytes) {
            int c1 = (aByte >> 4) & 0xF;
            int c2 = aByte & 0xF;
            stringBuilder.append(chars[c1]).append(chars[c2]);
        }
        return stringBuilder.toString();//new BigInteger(1, bytes).toString(16);
    }

    public static byte[] hex2bytes(String hex, boolean skipZeros) {
        if ((hex.length() & 1) == 1) {
            hex = "0" + hex;
        }
        while (hex.startsWith("00") && skipZeros) {
            hex = hex.substring(2);
        }
        byte[] bytes = new byte[hex.length() / 2];
        String str = "0123456789abcdef";
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ((str.indexOf(hex.charAt(i * 2)) << 4) | str.indexOf(hex.charAt(i * 2 + 1)));
        }
        return bytes;//new BigInteger(hex, 16).toByteArray();
    }

    public static KeyPair createKeys(String seed) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        byte[] seedBytes = seed.getBytes(StandardCharsets.UTF_16);
        keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom(seedBytes));
        return keyGen.generateKeyPair();
    }

    public enum WorkMode {
        SINGLE_MODE, NODE_MODE, SEND_MODE
    }
}
