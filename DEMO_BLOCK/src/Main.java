import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;

import java.io.IOException;
import java.security.*;
import java.util.Date;
import java.util.Scanner;

/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 10.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        //ProcessBuilder cleaner = new ProcessBuilder("cmd", "/c", "cls").inheritIO();
        //cleaner.start().waitFor();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Демо. Работа с движком блокчейн.");
        System.out.println("Введите ваш SEED:");
        System.out.print("> ");
        String seed = scanner.nextLine().replaceAll("[\n\r]*", "");
        KeyPair keys = Blockchain.createKeys(seed);
        Blockchain blockchain = new Blockchain(Blockchain.WorkMode.SINGLE_MODE, new FileWork(), null, new Generator(), keys);
        Blockchain.registerTransaction(MessageTransaction.class);
        Blockchain.registerTransaction(TimeTransaction.class);
        blockchain.start();
        boolean running = true;
        System.out.println("Инициализация завершена, доступен ввод.");
        System.out.print("> ");
        while (running) {
            String message = scanner.next();
            switch (message) {
                case "msg":
                    MessageTransaction messageTransaction = new MessageTransaction(scanner.nextLine());
                    if (blockchain.addTransaction(messageTransaction)) {
                        System.out.println("Message sent.");
                    }
                    System.out.println("Message didnt send.");
                    System.out.print("> ");
                    break;
                case "date":
                    TimeTransaction timeTransaction = new TimeTransaction(new Date(scanner.nextLong()));
                    blockchain.addTransaction(timeTransaction);
                    System.out.println("Time sent.");
                    System.out.print("> ");
                    break;
                case "block":
                    int index = Integer.parseInt(scanner.next());
                    Block block = blockchain.getBlockByIndex(index, false);
                    if (block == null) {
                        System.out.println("Hasn't block!");
                        break;
                    }
                    System.out.println("Block info:");
                    System.out.println("Previous SHA:" + block.getPrevShaHex());
                    System.out.println("SHA:" + block.getShaHex());
                    System.out.println("Height:" + block.getBlockHeight());
                    System.out.println("Mercle root:" + block.getMercleRoot());
                    System.out.println("Nonce:" + block.getNonce());
                    System.out.println("Timestamp:" + block.getTimestamp().toString());
                    System.out.println("Transactions count:" + block.getTransactionsCount());
                    System.out.println("Transactions size:" + block.getTransactionsByteSize());
                    System.out.println("Signature:" + block.getSignature());
                    System.out.println("Valid:" + block.isValid());
                    break;
                case "quit":
                    System.out.println("Выход из системы...");
                    running = false;
                    break;
            }
        }
        blockchain.stop();
        System.out.println("Завершено.");
    }


}
