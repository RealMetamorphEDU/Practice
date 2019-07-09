import realmetamorph.blockchain.transactions.SignedTransaction;
import realmetamorph.blockchain.transactions.TestTransaction;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        SignedTransaction signedTransaction = new SignedTransaction(new TestTransaction(), "32147658093214765809", "32147658093214765809", "32147658093214765809");
        SignedTransaction anotherTransaction = new SignedTransaction(signedTransaction.getTransactionBytes());
        /*
        Blockchain.registerTransaction(TestTransaction.class);
        ITransaction transaction = (ITransaction) Blockchain.getTransactionClass(0).getConstructor().newInstance();
        System.out.println(transaction.getType());
        System.out.println(Arrays.toString(transaction.getData()));
*/
    /*    ArrayList<SignedTransaction> testList = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            testList.add(signedTransaction);
        }
        System.out.println(createMercleRoot(testList));*/

    }

/*    private static String createMercleRoot(ArrayList<SignedTransaction> transactions) throws NoSuchAlgorithmException {
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
                BigInteger dblSha = new BigInteger(shas.get(i) + shas.get(i + 1), 16);
                shas.add(new BigInteger(1, digest.digest(digest.digest(dblSha.toByteArray()))).toString(16));
                newCount++;
            }
            if (last) {
                BigInteger dblSha = new BigInteger(shas.get(count + offset) + shas.get(count + offset), 16);
                shas.add(new BigInteger(1, digest.digest(digest.digest(dblSha.toByteArray()))).toString(16));
                newCount++;
                count++;
            }

            offset += count;
            count = newCount;
        }
        return shas.get(shas.size() - 1);
    }*/

}
