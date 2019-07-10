import realmetamorph.blockchain.block.IBlockGenerator;

/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 10.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class Generator implements IBlockGenerator {
    @Override
    public boolean checkSHAHex(String s) {
        return s.startsWith("00000");
    }

    @Override
    public long getStartNonce() {
        return 0;
    }

    @Override
    public int maxTransactionCount() {
        return 5;
    }
}
