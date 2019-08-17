import realmetamorph.blockchain.transactions.ITransaction;

import java.nio.ByteBuffer;
import java.util.Date;

/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 10.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class TimeTransaction implements ITransaction {

    private Date date;

    public TimeTransaction() {
        date = null;
    }

    public TimeTransaction(Date date) {
        this.date = date;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public void parseData(byte[] bytes) {
        date = new Date(ByteBuffer.wrap(bytes).getLong());
    }

    @Override
    public byte[] getData() {
        return ByteBuffer.allocate(8).putLong(date.getTime()).array();
    }

}
