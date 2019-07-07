/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 7.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.blockchain.callbacks;

import com.sun.istack.internal.NotNull;
import realmetamorph.blockchain.block.Block;

public interface AskNewBlockCallback {

    @NotNull
    Block askNewBlock(int transactionCount);

}
