package realmetamorph.blockchain;

import realmetamorph.blockchain.monitors.NetMonitor;
import realmetamorph.blockchain.monitors.NetMonitorTCP;
import realmetamorph.blockchain.monitors.NetMonitorUDP;
import realmetamorph.blockchain.transactions.ITransaction;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.*;

public class Blockchain {

    private final String blockchainFilename; // Файл блокчейна
    private ArrayList<InetAddress> blockchainNodes; // Другие узлы блокчейна
    private ArrayList<InetAddress> bannedNodes; // Плохие ноды.
    private WorkMode workMode;
    private NetMode netMode;
    private NetMonitor monitor;

    private HashMap<Integer, Class<?>> registeredTransactions;
    private ArrayList<SignedTransaction> transactionsPool;

    private int blockInterval; // minutes
    private Timer timer;
    private boolean started;

    public Blockchain(String blockchainFilename, WorkMode workMode, NetMode netMode) {
        this.blockchainFilename = blockchainFilename;
        this.workMode = workMode;
        this.netMode = netMode;
        blockchainNodes = new ArrayList<>();
        bannedNodes = new ArrayList<>();
        transactionsPool = new ArrayList<>();
        blockInterval = 300;
        if (workMode == WorkMode.NODE_MODE)
            if (netMode == NetMode.TCP_MODE) {
                monitor = new NetMonitorTCP();
            } else {
                monitor = new NetMonitorUDP();
            }
    }

    public void start() {
        if (!started)
            if (workMode == WorkMode.SINGLE_MODE) {
                timer = new Timer("nextBlock", true);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        generateBlock();
                    }
                }, blockInterval * 1000, blockInterval * 1000);
                started = true;
            } else if (workMode == WorkMode.NODE_MODE) {
                timer = new Timer("nextBlock", true);
                monitor.start();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        voteBlock(() -> {
                            generateBlock();
                        });
                    }
                }, blockInterval * 1000, blockInterval * 1000);
                started = true;
            }
    }

    public void stop() {
        if (started)
            if (workMode == WorkMode.SINGLE_MODE) {
                timer.cancel();
                timer = null;
                started = false;
            } else if (workMode == WorkMode.NODE_MODE) {
                monitor.stop();
                timer.cancel();
                timer = null;
                started = false;
            }
    }

    public void setBlockInterval(int minutes) {
        blockInterval = minutes;
    }

    private void generateBlock() {

    }

    private void voteBlock(Runnable callback) {

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

    public boolean addTransaction(ITransaction transaction) {
        if (!registeredTransactions.containsKey(transaction.getType()))
            return false;
        SignedTransaction signedTransaction = new SignedTransaction(transaction);
        if (!signedTransaction.isValid())
            return false;
        if (workMode == WorkMode.SINGLE_MODE) {
            transactionsPool.add(signedTransaction);
        }
        if (workMode == WorkMode.NODE_MODE) {
            transactionsPool.add(signedTransaction);
            broadcastTransaction(signedTransaction);
        }
        if (workMode == WorkMode.SEND_MODE)
            broadcastTransaction(signedTransaction);
        return true;
    }

    public ArrayList<SignedTransaction> getTransactionsByPublicKey(String publicKey) {

        return null;
    }

    public void setBlockchainNodes(ArrayList<InetAddress> blockchainNodes) {
        this.blockchainNodes = blockchainNodes;
    }

    public void addBlockchainNode(InetAddress blockchainNode) {
        if (!blockchainNodes.contains(blockchainNode))
            blockchainNodes.add(blockchainNode);
    }

    public void removeBlockchainNode(InetAddress blockchainNode) {
        blockchainNodes.remove(blockchainNode);
        bannedNodes.remove(blockchainNode);
    }

    private void broadcastTransaction(SignedTransaction transaction) {
        if (netMode == NetMode.TCP_MODE) {
            // TODO: Рассылка транзакции по TCP
        } else {
            // TODO: Рассылка транзакции по UDP
        }
    }

    public enum WorkMode {
        SINGLE_MODE,
        NODE_MODE,
        SEND_MODE
    }

    public enum NetMode {
        TCP_MODE,
        UDP_MODE
    }
}
