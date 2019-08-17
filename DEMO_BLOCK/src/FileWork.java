import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.AskNewBlockCallback;
import realmetamorph.blockchain.filework.IFileMonitor;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 10.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class FileWork implements IFileMonitor {
    private AskNewBlockCallback askNewBlockCallback;
    private Timer asker;
    private int currentHeight;

    public FileWork() {
        asker = new Timer("Block asker", true);
    }

    @Override
    public void start(Blockchain.WorkMode workMode) {
        if (!new File("blocks").exists())
            new File("blocks").mkdir();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (!new File("blocks/block_" + i + ".blc").exists()) {
                currentHeight = i - 1;
                System.out.println(currentHeight);
                break;
            }
        }
        if (workMode == Blockchain.WorkMode.SINGLE_MODE) {
            asker.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Block block = askNewBlockCallback.askNewBlock(5);
                    if (block != null) {
                        try {
                            writeBlock(block);
                            currentHeight++;
                            System.out.println("Block created!");
                            System.out.print("> ");
                        } catch (IOException ignored) {
                        }
                    }
                }
            }, 30000, 30000);
        }
    }

    @Override
    public void stop() {
        asker.cancel();
    }

    private void writeBlock(Block block) throws IOException {
        FileOutputStream outputStream = new FileOutputStream("blocks/block_" + block.getBlockHeight() + ".blc");
        outputStream.write(block.getBlockData());
        outputStream.close();
    }

    private Block readBlock(int index, boolean onlyHeader) throws IOException, NoSuchMethodException, NoSuchAlgorithmException, InstantiationException, IllegalAccessException, InvocationTargetException, InvalidKeySpecException {
        File blockFile = new File("blocks/block_" + index + ".blc");
        if (blockFile.exists()) {
            FileInputStream inputStream = new FileInputStream(blockFile);
            byte[] bytes = null;
            if (onlyHeader)
                bytes = new byte[Block.getHeaderSize()];
            else
                bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            inputStream.close();
            return new Block(bytes, onlyHeader);
        }
        return null;
    }

    @Override
    public Block getBlock(int i, boolean onlyHeader) {
        try {
            return readBlock(i, onlyHeader);
        } catch (IOException | InvalidKeySpecException | InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchAlgorithmException | NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public int getHeight() {
        return currentHeight;
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
