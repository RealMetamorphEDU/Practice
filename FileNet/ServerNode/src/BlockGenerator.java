import realmetamorph.blockchain.block.IBlockGenerator;

/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 17.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class BlockGenerator implements IBlockGenerator {
    @Override
    public boolean checkSHAHex(String s, long l) {
        return s.startsWith("00000");
    }

    @Override
    public long getStartNonce() {
        return 12345;
    }

    @Override
    public int maxTransactionCount() {
        return 30;
    }
}
