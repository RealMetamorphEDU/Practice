package realmetamorph.blockchain;

import com.sun.istack.internal.NotNull;
import realmetamorph.blockchain.block.IBlockGenerator;
import realmetamorph.blockchain.filework.IFileMonitor;
import realmetamorph.blockchain.network.INetMonitor;
import realmetamorph.blockchain.transactions.ITransaction;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Blockchain {

    private WorkMode workMode;
    private IFileMonitor file;
    private INetMonitor net;
    private IBlockGenerator blockGenerator;

    private HashMap<Integer, Class<?>> registeredTransactions;
    private ArrayList<SignedTransaction> transactionsPool;

    private boolean started;

    public Blockchain(@NotNull WorkMode workMode, IFileMonitor file, INetMonitor net, @NotNull IBlockGenerator blockGenerator) {
        assert blockGenerator != null;

        assert workMode != WorkMode.SINGLE_MODE || file != null;
        assert workMode != WorkMode.NODE_MODE || (file != null && net != null);
        assert workMode != WorkMode.SEND_MODE || net != null;

        this.workMode = workMode;
        this.net = net;
        this.file = file;
        this.blockGenerator = blockGenerator;

        transactionsPool = new ArrayList<>();
        registeredTransactions = new HashMap<>();
        if (file != null){
            //TODO: Добавить инициализацию колбеков.
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

    public boolean registerTransaction(Class<?> transaction) {
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

    public Class<?> getTransactionClass(int type) {
        return registeredTransactions.getOrDefault(type, null);
    }

    public boolean addTransaction(ITransaction transaction, String privateKey) {
        if (!registeredTransactions.containsKey(transaction.getType()))
            return false;
        SignedTransaction signedTransaction = new SignedTransaction(transaction, privateKey);
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

    public enum WorkMode {
        SINGLE_MODE, NODE_MODE, SEND_MODE
    }
}
