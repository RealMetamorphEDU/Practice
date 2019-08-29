/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 29.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.practice.springnode.transactions;

import realmetamorph.blockchain.transactions.ITransaction;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class ShareFileTransaction implements ITransaction {
    private String filename;
    private String keeperKey;
    private byte mode;
    private Date date;

    public static final byte ONLY_READ_MODE = 0b000;
    public static final byte REPLACE_MODE = 0b001;
    public static final byte DELETE_MODE = 0b010;
    public static final byte SHARE_MODE = 0b100;

    public ShareFileTransaction() {
    }

    public ShareFileTransaction(String filename, String keeperKey, byte mode, Date date) {
        this.filename = filename;
        this.keeperKey = keeperKey;
        this.mode = mode;
        this.date = date;
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public void parseData(byte[] data) {
        String[] strings = new String(data, StandardCharsets.UTF_8).split("\n");
        filename = strings[0];
        keeperKey = strings[1];
        mode = Byte.parseByte(strings[2]);
        date = new Date(Long.parseLong(strings[3]));
    }

    @Override
    public byte[] getData() {
        String builder = filename + "\n" + keeperKey + "\n" + mode + "\n" + date.getTime();
        return builder.getBytes(StandardCharsets.UTF_8);
    }

    public String getFilename() {
        return filename;
    }

    public String getKeeperKey() {
        return keeperKey;
    }

    public byte getMode() {
        return mode;
    }

    public Date getDate() {
        return date;
    }
}
