import realmetamorph.blockchain.transactions.ITransaction;

import java.nio.charset.StandardCharsets;

/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 10.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class MessageTransaction implements ITransaction {

    private String msg;

    public MessageTransaction() {
        msg = "";
    }

    public MessageTransaction(String message) {
        msg = message;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void parseData(byte[] bytes) {
        msg = new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getData() {
        return msg.getBytes(StandardCharsets.UTF_8);
    }
}
