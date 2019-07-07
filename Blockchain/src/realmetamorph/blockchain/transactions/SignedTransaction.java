package realmetamorph.blockchain.transactions;

public class SignedTransaction {

    private final String shaHex = "";
    private final String signature = "";
    private final byte[] transaction = null;
    private final ITransaction iTransaction = null;

    public SignedTransaction(byte[] transaction) {

    }

    public SignedTransaction(ITransaction transaction) {

    }

    public byte[] getTransactionBytes() {
        return null;
    }

    public ITransaction getTransaction() {
        return iTransaction;
    }

    public boolean isValid(){
        return false;
    }

}
