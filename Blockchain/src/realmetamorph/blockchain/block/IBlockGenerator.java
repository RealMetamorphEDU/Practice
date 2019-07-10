/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 7.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.blockchain.block;

public interface IBlockGenerator {

    boolean checkSHAHex(String shaHex);

    long getStartNonce();

    int maxTransactionCount();

}
