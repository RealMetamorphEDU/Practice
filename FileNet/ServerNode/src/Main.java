/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 17.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.IBlockGenerator;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

public class Main implements Runnable {

    private Blockchain blockchain;

    private Main() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        blockchain = new Blockchain(Blockchain.WorkMode.NODE_MODE, null, null, new IBlockGenerator() {
            @Override
            public boolean checkSHAHex(String s, long l) {
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
        }, Blockchain.createKeys(""));
    }

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        Main main = new Main();
        main.run();
    }

    @Override
    public void run() {

    }
}
