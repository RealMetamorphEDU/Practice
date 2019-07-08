package realmetamorph.blockchain;

import com.sun.istack.internal.NotNull;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.block.IBlockGenerator;
import realmetamorph.blockchain.filework.IFileMonitor;
import realmetamorph.blockchain.network.INetMonitor;
import realmetamorph.blockchain.transactions.ITransaction;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
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

    private String publicKey;
    private String privateKey;

    public Blockchain(@NotNull WorkMode workMode, IFileMonitor file, INetMonitor net, @NotNull IBlockGenerator blockGenerator) {
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

        transactionsPool = new ArrayList<>();
        if (file != null) {
            file.setCallbackAskNewBlock((int count) -> {
                return null;
            });
        }
        if (net != null) {
            //TODO: Добавить инициализацию колбеков.
        }
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
            signedTransaction = new SignedTransaction(transaction, publicKey, privateKey, receiverKey);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        if (!signedTransaction.isValid())
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

    public Block getBlockByIndex(int indexBlock) {
        if (workMode != WorkMode.SEND_MODE)
            return file.getBlock(indexBlock);
        else
            return net.getBlock(indexBlock);
    }

    // Создание подписи, на вход передаётся публичный ключ, приватный ключ и хеш транзакции
    public static String getSignature(String publicKey, String privateKey, String shaHex) {
        // TODO: Реализовать создание подписи.
        return "00002222890000222289";
    }

    // Проверка подписи, на вход передаётся публичный ключ, подпись и хеш транзакции
    public static boolean checkSignature(String publicKey, String signature, String shaHex) {
        // TODO: Реализовать проверку подписи.
        return false;
    }

    public enum WorkMode {
        SINGLE_MODE, NODE_MODE, SEND_MODE
    }
}
