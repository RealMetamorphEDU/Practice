import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.filework.IFileMonitor;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 10.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        ProcessBuilder cleaner = new ProcessBuilder("cmd", "/c", "cls").inheritIO();
        cleaner.start().waitFor();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Демо. Работа с движком блокчейн.");
        System.out.println("Введите ваш SEED:");
        System.out.print("> ");
        String seed = scanner.nextLine().replaceAll("[\n\r]*", "");
        KeyPair keys = Blockchain.createKeys(seed);
        FileWork file = new FileWork(keys);
        Blockchain blockchain = new Blockchain(Blockchain.WorkMode.SINGLE_MODE, file, null, new Generator(), keys);
        file.setBlockchain(blockchain);
        while (true){



        }
    }


}
