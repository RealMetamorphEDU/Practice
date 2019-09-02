/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 17.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.IBlockGenerator;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Scanner;

public class Main {

    private Blockchain blockchain;
    private IBlockGenerator generator;
    private NetMonitor net;
    private FilesMonitor filesMonitor;
    private PublicKey key;
    private boolean running;


    private Main() throws UnknownHostException {
        generator = new IBlockGenerator() {
            @Override
            public boolean checkSHAHex(String shaHex, long nonce) {
                return false;
            }

            @Override
            public long getStartNonce() {
                return 0;
            }

            @Override
            public int maxTransactionCount() {
                return 0;
            }
        };
        net = new NetMonitor();
        running = true;
    }

    public static void main(String[] args) throws UnknownHostException {
        Main main = new Main();
        main.run();
    }

    private void run() {
        int state = 0;
        Scanner scanner = new Scanner(System.in);
        net.presetFile(new File("test.txt"));
        net.sendTransaction(null);
        while (running) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                printTitle();
            } catch (InterruptedException | IOException ignored) {
            }
            state = printChooseTable(scanner, state);
        }

    }


    private void printTitle() {
        System.out.println("FileNet console client.");
    }

    private int printChooseTable(Scanner scanner, int state) {
        int nState = 0;
        switch (state) {
            case 0: { // Инициализация
                System.out.println("Choose work mode:");
                System.out.println("1) View");
                System.out.println("2) Work");
                int choose = scanner.nextInt();
                if (choose < 1 || choose > 2)
                    choose = 1;
                nState = choose;
                filesMonitor = new FilesMonitor(net);
                System.out.println("Checking blockchain...");
                filesMonitor.update();
                if (choose == 2) {
                    System.out.println("Input your secret seed:");
                    scanner.skip("[\n\r]+");
                    String seed = scanner.nextLine();
                    try {
                        KeyPair keys = Blockchain.createKeys(seed);
                        key = keys.getPublic();
                        blockchain = new Blockchain(Blockchain.WorkMode.SEND_MODE, null, net, generator, keys);
                    } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
                        System.out.println("Can't start work mode. Used view mode.");
                        nState = 1;
                    }
                }
            }
            break;
            case 1: { // view mode, choose option
                System.out.println("Choose options:");
                System.out.println("1) Print files for public key");
                System.out.println("2) Quit");
                int choose = scanner.nextInt();
                if (choose < 1 || choose > 2)
                    choose = 1;
                if (choose == 2)
                    nState = 10;
                else
                    nState = 3;
            }
            break;
            case 2: {
                System.out.println("Choose options:");
                System.out.println("1) Print files");
                System.out.println("2) Download all files");
                System.out.println("3) Download file");
                System.out.println("4) Upload file");
                System.out.println("5) Upload files from dir");
                System.out.println("6) Delete file");
                System.out.println("7) Share file");
                System.out.println("8) Quit");
                int choose = scanner.nextInt();
                if (choose < 1 || choose > 2)
                    choose = 1;
                nState = choose + 2;
            }
            break;
            case 3: { // Print files
                if (blockchain == null) {
                    System.out.println("Input public key:");
                    scanner.skip("[\n\r]+");
                    filesMonitor.printFiles(scanner.nextLine());
                    System.out.println("Press ENTER...");
                    scanner.nextLine();
                } else {
                    filesMonitor.printFiles(key);
                    scanner.skip("[\n\r]+");
                    System.out.println("Press ENTER...");
                    scanner.nextLine();
                }
            }
            break;
            case 4: {

            }
            break;
            case 10: { // Quit

            }
            break;
        }
        return nState;
    }

}
