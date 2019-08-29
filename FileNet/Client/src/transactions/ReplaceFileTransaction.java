/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 29.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package transactions;

import realmetamorph.blockchain.transactions.ITransaction;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class ReplaceFileTransaction implements ITransaction {
    private String filename;
    private String sha;
    private Date date;

    public ReplaceFileTransaction() {
    }

    public ReplaceFileTransaction(String filename, String sha, Date date) {
        this.filename = filename;
        this.sha = sha;
        this.date = date;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public void parseData(byte[] data) {
        String[] strings = new String(data, StandardCharsets.UTF_8).split("\n");
        filename = strings[0];
        sha = strings[1];
        date = new Date(Long.parseLong(strings[2]));
    }

    @Override
    public byte[] getData() {
        String builder = filename + "\n" + sha + "\n" + date.getTime();
        return builder.getBytes(StandardCharsets.UTF_8);
    }

    public String getFilename() {
        return filename;
    }

    public String getSha() {
        return sha;
    }

    public Date getDate() {
        return date;
    }
}
