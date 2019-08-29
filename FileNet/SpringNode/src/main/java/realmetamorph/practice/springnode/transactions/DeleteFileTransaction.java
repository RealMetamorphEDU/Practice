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

public class DeleteFileTransaction implements ITransaction {
    private String filename;
    private Date date;

    public DeleteFileTransaction() {
    }

    public DeleteFileTransaction(String filename, Date date) {
        this.filename = filename;
        this.date = date;
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public void parseData(byte[] data) {
        String[] strings = new String(data, StandardCharsets.UTF_8).split("\n");
        filename = strings[0];
        date = new Date(Long.parseLong(strings[1]));
    }

    @Override
    public byte[] getData() {
        String builder = filename + "\n" + date.getTime();
        return builder.getBytes(StandardCharsets.UTF_8);
    }

    public String getFilename() {
        return filename;
    }

    public Date getDate() {
        return date;
    }
}
