import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.AskNewBlockCallback;
import realmetamorph.blockchain.filework.IFileMonitor;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;

/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 10.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class FileWork implements IFileMonitor {
    private AskNewBlockCallback askNewBlockCallback;
    private Blockchain blockchain;
    private KeyPair keys;

    public FileWork(KeyPair keys) {
        this.keys = keys;
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    boolean execCommand(String[] command) {
        if (command.length == 0)
            return false;
        switch (command[0]) {
            case "help":
                if (command.length == 1) {
                    System.out.println();
                } else
                    switch (command[1]) {
                        case "help":
                            System.out.println();
                            break;
                        case "send":
                            System.out.println();
                            break;
                        case "block":
                            System.out.println();
                            break;
                        case "find":
                            System.out.println();
                            break;
                    }
                break;
            case "send":
                if (command.length < 3) {
                    System.out.println();
                } else
                    switch (command[1]) {
                        case "message":
                            blockchain.addTransaction(new MessageTransaction(command[2]), Blockchain.bytes2hex(keys.getPublic().getEncoded()));
                            System.out.println();
                            break;
                        case "date":
                            blockchain.addTransaction(new TimeTransaction(new Date(Long.parseLong(command[2]))), Blockchain.bytes2hex(keys.getPublic().getEncoded()));
                            System.out.println();
                            break;
                    }
                break;
            case "block":
                if (command.length < 2) {
                    System.out.println();
                } else
                    switch (command[1]) {
                        case "create":
                            System.out.println();
                            break;
                        case "get":
                            System.out.println();
                            break;
                    }
                break;
            case "find":

                break;
        }
        return false;
    }

    @Override
    public void start(Blockchain.WorkMode workMode) {

    }

    @Override
    public void stop() {

    }

    @Override
    public Block getBlock(int i) {
        return null;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByPublicKey(String s) {
        return null;
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByType(int i) {
        return null;
    }

    @Override
    public void addNewBlock(Block block) {
        //Nothing
    }

    @Override
    public void setCallbackAskNewBlock(AskNewBlockCallback askNewBlockCallback) {
        this.askNewBlockCallback = askNewBlockCallback;
    }

    @Override
    public boolean validator(SignedTransaction signedTransaction) {
        return true;
    }
}
