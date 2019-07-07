package realmetamorph.blockchain;

import com.sun.istack.internal.NotNull;
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
    private INetMonitor monitor;

    private HashMap<Integer, Class<?>> registeredTransactions;
    private ArrayList<SignedTransaction> transactionsPool;


    private boolean started;

    public Blockchain(@NotNull WorkMode workMode, IFileMonitor file, INetMonitor monitor) {
        this.workMode = workMode;
        transactionsPool = new ArrayList<>();
        this.monitor = monitor;
        this.file = file;
        if (monitor != null) {
            //TODO: Добавить инициализацию колбеков.
        }
    }

    public void start() {
        if (!started) {
            file.start(workMode);
            if (workMode != WorkMode.SINGLE_MODE) {
                monitor.start(workMode);
            }
            started = true;
        }
    }

    public void stop() {
        if (started) {
            file.stop();
            if (workMode != WorkMode.SINGLE_MODE) {
                monitor.stop();
            }
            started = false;
        }
    }

    public void setBlockInterval(int minutes) {
        file.setNewBlockInterval(minutes);
        monitor.askNewBlockInterval(minutes);
    }

    private void generateBlock() {

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
            //broadcastTransaction(signedTransaction);
        }
        if (workMode == WorkMode.SEND_MODE)
            ;//broadcastTransaction(signedTransaction);
        return true;
    }

    public ArrayList<SignedTransaction> getTransactionsByPublicKey(String publicKey) {
        // TODO: Поиск транзакций в блокчейне
        return null;
    }

    public enum WorkMode {
        SINGLE_MODE, NODE_MODE, SEND_MODE
    }
}
