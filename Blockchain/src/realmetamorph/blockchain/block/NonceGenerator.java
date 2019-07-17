/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 9.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.blockchain.block;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static realmetamorph.blockchain.Blockchain.KEY_SIZE;
import static realmetamorph.blockchain.Blockchain.completeShaString;

class NonceGenerator {

    private byte[] data;
    private boolean interrupted;
    private int threadsCount;
    private long nonce;

    NonceGenerator(byte[] psByte, byte[] pkByte, byte[] tsByte, byte[] bhByte, byte[] tcByte, byte[] bsByte, byte[] mrByte) {
        this.data = new byte[92 + KEY_SIZE];
        System.arraycopy(psByte, 0, data, 0, 32);
        System.arraycopy(pkByte, 0, data, 40, KEY_SIZE);
        System.arraycopy(tsByte, 0, data, 40 + KEY_SIZE, 8);
        System.arraycopy(bhByte, 0, data, 48 + KEY_SIZE, 4);
        System.arraycopy(tcByte, 0, data, 52 + KEY_SIZE, 4);
        System.arraycopy(bsByte, 0, data, 56 + KEY_SIZE, 4);
        System.arraycopy(mrByte, 0, data, 60 + KEY_SIZE, 32);
        this.interrupted = false;
        this.threadsCount = Runtime.getRuntime().availableProcessors() * 8;
        this.nonce = 0;
    }

    long generateNonce(IBlockGenerator generator) {
        Thread thread = null;
        long startNonce = Math.max(generator.getStartNonce(), 0);
        for (int i = 0; i < threadsCount; i++) {
            int srt = i;
            thread = new Thread(new Runnable() {
                private final long start = srt + startNonce + 1;
                private final long offset = threadsCount;

                @Override
                public void run() {
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        for (long j = start; j < Long.MAX_VALUE; j += offset) {
                            byte[] nonceByte = ByteBuffer.allocate(8).putLong(j).array();
                            System.arraycopy(nonceByte, 0, data, 32, 8);
                            String shaHex = completeShaString(new BigInteger(1, digest.digest(digest.digest(data))).toString(16));
                            if (generator.checkSHAHex(shaHex, j)) {
                                nonce = j;
                                interrupted = true;
                                break;
                            }
                            if (interrupted)
                                break;
                        }
                    } catch (NoSuchAlgorithmException ignored) {
                    }
                }
            });
            thread.start();
        }
        try {
            if (thread != null)
                thread.join();
        } catch (InterruptedException ignored) {
        }
        return nonce;
    }

}
