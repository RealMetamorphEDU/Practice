import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.transactions.ITransaction;
import realmetamorph.blockchain.transactions.SignedTransaction;
import realmetamorph.blockchain.transactions.TestTransaction;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        SignedTransaction signedTransaction = new SignedTransaction(new TestTransaction(), "32147658093214765809", "32147658093214765809", "32147658093214765809");
        System.out.println(signedTransaction.getPublicKey());
        System.out.println(signedTransaction.getShaHex());
        System.out.println(signedTransaction.getSignature());
        System.out.println(signedTransaction.isValid());
        System.out.println(signedTransaction.getReceiverKey());

        System.out.println(Arrays.toString(signedTransaction.getTransactionBytes()));
        SignedTransaction anotherTransaction = new SignedTransaction(signedTransaction.getTransactionBytes());
        System.out.println(anotherTransaction.getPublicKey());
        System.out.println(anotherTransaction.getShaHex());
        System.out.println(anotherTransaction.getSignature());
        System.out.println(anotherTransaction.isValid());
        System.out.println(signedTransaction.getReceiverKey());
        /*
        Blockchain.registerTransaction(TestTransaction.class);
        ITransaction transaction = (ITransaction) Blockchain.getTransactionClass(0).getConstructor().newInstance();
        System.out.println(transaction.getType());
        System.out.println(Arrays.toString(transaction.getData()));
*/

    }

}
