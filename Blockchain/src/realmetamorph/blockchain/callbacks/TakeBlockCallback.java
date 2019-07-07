/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 7.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.blockchain.callbacks;

import realmetamorph.blockchain.block.Block;

public interface TakeBlockCallback {

    void TakeBlock(int blockIndex, Block block);

}
