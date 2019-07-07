/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 7.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.blockchain.callbacks;

import realmetamorph.blockchain.transactions.SignedTransaction;

public interface TakeNewTransactionCallback {


    void takeNewTransaction(SignedTransaction transaction);


}

