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
    private int prevLen;
    private boolean interrupted;
    private int threadsCount;
    private long[] nonces;

    NonceGenerator(byte[] psByte, byte[] pkByte, byte[] tsByte, byte[] bhByte, byte[] tcByte, byte[] mrByte) {
        this.data = new byte[psByte.length + 8 + KEY_SIZE + tsByte.length + bhByte.length + tcByte.length + mrByte.length];
        int offset = 0;
        this.prevLen = psByte.length;
        System.arraycopy(psByte, 0, data, offset, psByte.length);
        offset += psByte.length + 8; // nonce size 8
        System.arraycopy(pkByte, 0, data, offset, pkByte.length);
        offset += pkByte.length;
        System.arraycopy(tsByte, 0, data, offset, tsByte.length);
        offset += tsByte.length;
        System.arraycopy(bhByte, 0, data, offset, bhByte.length);
        offset += bhByte.length;
        System.arraycopy(tcByte, 0, data, offset, tcByte.length);
        offset += tcByte.length;
        System.arraycopy(mrByte, 0, data, offset, mrByte.length);
        this.interrupted = false;
        this.threadsCount = Runtime.getRuntime().availableProcessors() * 8;
        this.nonces = new long[threadsCount];
    }

    long generateNonce(IBlockGenerator generator) {
        Thread[] threads = new Thread[threadsCount];
        long startNonce = Math.max(generator.getStartNonce(), 0);
        for (int i = 0; i < threadsCount; i++) {
            int srt = i;
            threads[i] = new Thread(new Runnable() {
                private final int id = srt;
                private final long start = srt + startNonce + 1;
                private final long offset = threadsCount;

                @Override
                public void run() {
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        for (long j = start; j < Long.MAX_VALUE; j += offset) {
                            byte[] nonceByte = ByteBuffer.allocate(8).putLong(j).array();
                            System.arraycopy(nonceByte, 0, data, prevLen, 8);
                            String shaHex = completeShaString(new BigInteger(1, digest.digest(digest.digest(data))).toString(16));
                            if (generator.checkSHAHex(shaHex)) {
                                nonces[id] = j;
                                break;
                            }
                            if (interrupted)
                                break;
                        }
                    } catch (NoSuchAlgorithmException ignored) {
                    }
                }
            });
            threads[i].start();
        }
        int index = 0;

        while (threads[index++].isAlive()) {
            if (index == threadsCount)
                index = 0;
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
        interrupted = true;
        for (Thread thread : threads) {
            thread.interrupt();
        }
        return nonces[index - 1];
    }

}
