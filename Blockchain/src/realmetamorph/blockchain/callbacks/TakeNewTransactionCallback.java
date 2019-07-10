/*******************************************************************************
 * Группа: БВТ1703.
 * Студент: Тимчук А.В.
 * Создано: 7.7.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.blockchain.callbacks;

import realmetamorph.blockchain.transactions.SignedTransaction;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface TakeNewTransactionCallback {

    boolean takeNewTransaction(SignedTransaction transaction) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException;

}

