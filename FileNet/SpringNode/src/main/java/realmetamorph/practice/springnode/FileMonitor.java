/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 18.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.practice.springnode;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.AskNewBlockCallback;
import realmetamorph.blockchain.filework.IFileMonitor;
import realmetamorph.blockchain.transactions.SignedTransaction;
import realmetamorph.practice.springnode.transactions.AddFileTransaction;
import realmetamorph.practice.springnode.transactions.DeleteFileTransaction;
import realmetamorph.practice.springnode.transactions.ShareFileTransaction;

import java.util.ArrayList;

public class FileMonitor implements IFileMonitor {

    private MongoDatabase db;
    private MongoCollection<BsonDocument> blocks;

    FileMonitor(MongoDatabase db) {
        this.db = db;
    }

    boolean validAddOperation(String keeperKey, String askKey, String filename) {
        if (!keeperKey.equals(askKey))
            return false;
        int height = getHeight();
        while (height != 0) {
            Block block = getBlock(height, false);
            ArrayList<SignedTransaction> add = new ArrayList<>();
            ArrayList<SignedTransaction> delete = new ArrayList<>();
            for (int i = 0; i < block.getTransactionsCount(); i++) {
                SignedTransaction signedTransaction = block.getTransaction(i);
                switch (signedTransaction.getTransaction().getType()) {
                    case 0:
                        add.add(signedTransaction);
                        break;
                    case 2:
                        delete.add(signedTransaction);
                        break;
                }
            }
            for (SignedTransaction signedTransaction : delete) {
                DeleteFileTransaction deleteFileTransaction = (DeleteFileTransaction) signedTransaction.getTransaction();
                if (deleteFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey))
                    return true;
            }
            for (SignedTransaction signedTransaction : add) {
                AddFileTransaction addFileTransaction = (AddFileTransaction) signedTransaction.getTransaction();
                if (addFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey)) {
                    return false;
                }
            }
            height--;
        }
        return true;
    }

    boolean validReplaceOperation(String keeperKey, String askKey, String filename) {
        int height = getHeight();
        while (height != 0) {
            Block block = getBlock(height, false);
            ArrayList<SignedTransaction> add = new ArrayList<>();
            ArrayList<SignedTransaction> delete = new ArrayList<>();
            ArrayList<SignedTransaction> share = new ArrayList<>();
            for (int i = 0; i < block.getTransactionsCount(); i++) {
                SignedTransaction signedTransaction = block.getTransaction(i);
                switch (signedTransaction.getTransaction().getType()) {
                    case 0:
                        add.add(signedTransaction);
                        break;
                    case 2:
                        delete.add(signedTransaction);
                        break;
                    case 3:
                        share.add(signedTransaction);
                        break;
                }
            }
            share.sort((o1, o2) -> {
                ShareFileTransaction share1 = (ShareFileTransaction) o1.getTransaction();
                ShareFileTransaction share2 = (ShareFileTransaction) o2.getTransaction();
                return -share1.getDate().compareTo(share2.getDate());
            });
            for (SignedTransaction signedTransaction : delete) {
                DeleteFileTransaction deleteFileTransaction = (DeleteFileTransaction) signedTransaction.getTransaction();
                if (deleteFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey))
                    return false;
            }
            for (SignedTransaction signedTransaction : share) {
                ShareFileTransaction shareFileTransaction = (ShareFileTransaction) signedTransaction.getTransaction();
                if (shareFileTransaction.getFilename().equals(filename) && shareFileTransaction.getKeeperKey().equals(keeperKey) && signedTransaction.getReceiverKey().equals(askKey)) {
                    return (shareFileTransaction.getMode() & ShareFileTransaction.REPLACE_MODE) == ShareFileTransaction.REPLACE_MODE;
                }
            }
            for (SignedTransaction signedTransaction : add) {
                AddFileTransaction addFileTransaction = (AddFileTransaction) signedTransaction.getTransaction();
                if (addFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey)) {
                    return keeperKey.equals(askKey);
                }
            }
            height--;
        }
        return false;
    }

    boolean validDeleteOperation(String keeperKey, String askKey, String filename) {
        int height = getHeight();
        while (height != 0) {
            Block block = getBlock(height, false);
            ArrayList<SignedTransaction> add = new ArrayList<>();
            ArrayList<SignedTransaction> delete = new ArrayList<>();
            ArrayList<SignedTransaction> share = new ArrayList<>();
            for (int i = 0; i < block.getTransactionsCount(); i++) {
                SignedTransaction signedTransaction = block.getTransaction(i);
                switch (signedTransaction.getTransaction().getType()) {
                    case 0:
                        add.add(signedTransaction);
                        break;
                    case 2:
                        delete.add(signedTransaction);
                        break;
                    case 3:
                        share.add(signedTransaction);
                        break;
                }
            }
            share.sort((o1, o2) -> {
                ShareFileTransaction share1 = (ShareFileTransaction) o1.getTransaction();
                ShareFileTransaction share2 = (ShareFileTransaction) o2.getTransaction();
                return -share1.getDate().compareTo(share2.getDate());
            });
            for (SignedTransaction signedTransaction : delete) {
                DeleteFileTransaction deleteFileTransaction = (DeleteFileTransaction) signedTransaction.getTransaction();
                if (deleteFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey))
                    return false;
            }
            for (SignedTransaction signedTransaction : share) {
                ShareFileTransaction shareFileTransaction = (ShareFileTransaction) signedTransaction.getTransaction();
                if (shareFileTransaction.getFilename().equals(filename) && shareFileTransaction.getKeeperKey().equals(keeperKey) && signedTransaction.getReceiverKey().equals(askKey)) {
                    return (shareFileTransaction.getMode() & ShareFileTransaction.DELETE_MODE) == ShareFileTransaction.DELETE_MODE;
                }
            }
            for (SignedTransaction signedTransaction : add) {
                AddFileTransaction addFileTransaction = (AddFileTransaction) signedTransaction.getTransaction();
                if (addFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey)) {
                    return keeperKey.equals(askKey);
                }
            }
            height--;
        }
        return false;
    }

    byte getShareMode(String keeperKey, String askKey, String filename) {
        int height = getHeight();
        while (height != 0) {
            Block block = getBlock(height, false);
            ArrayList<SignedTransaction> add = new ArrayList<>();
            ArrayList<SignedTransaction> delete = new ArrayList<>();
            ArrayList<SignedTransaction> share = new ArrayList<>();
            for (int i = 0; i < block.getTransactionsCount(); i++) {
                SignedTransaction signedTransaction = block.getTransaction(i);
                switch (signedTransaction.getTransaction().getType()) {
                    case 0:
                        add.add(signedTransaction);
                        break;
                    case 2:
                        delete.add(signedTransaction);
                        break;
                    case 3:
                        share.add(signedTransaction);
                        break;
                }
            }
            share.sort((o1, o2) -> {
                ShareFileTransaction share1 = (ShareFileTransaction) o1.getTransaction();
                ShareFileTransaction share2 = (ShareFileTransaction) o2.getTransaction();
                return -share1.getDate().compareTo(share2.getDate());
            });
            for (SignedTransaction signedTransaction : delete) {
                DeleteFileTransaction deleteFileTransaction = (DeleteFileTransaction) signedTransaction.getTransaction();
                if (deleteFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey))
                    return -1;
            }
            for (SignedTransaction signedTransaction : share) {
                ShareFileTransaction shareFileTransaction = (ShareFileTransaction) signedTransaction.getTransaction();
                if (shareFileTransaction.getFilename().equals(filename) && shareFileTransaction.getKeeperKey().equals(keeperKey) && signedTransaction.getReceiverKey().equals(askKey)) {
                    return shareFileTransaction.getMode();
                }
            }
            for (SignedTransaction signedTransaction : add) {
                AddFileTransaction addFileTransaction = (AddFileTransaction) signedTransaction.getTransaction();
                if (addFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey)) {
                    if (keeperKey.equals(askKey))
                        return ShareFileTransaction.REPLACE_MODE | ShareFileTransaction.DELETE_MODE | ShareFileTransaction.SHARE_MODE;
                    else
                        return -1;
                }
            }
            height--;
        }
        return -1;
    }

    boolean validShareMode(byte validMode, byte requireMode) {
        boolean result = true;
        if ((ShareFileTransaction.REPLACE_MODE & validMode) < (ShareFileTransaction.REPLACE_MODE & requireMode))
            result = false;
        if ((ShareFileTransaction.DELETE_MODE & validMode) < (ShareFileTransaction.DELETE_MODE & requireMode))
            result = false;
        if ((ShareFileTransaction.SHARE_MODE & validMode) < (ShareFileTransaction.SHARE_MODE & requireMode))
            result = false;
        return result;
    }

    boolean validFileGetOperation(String keeperKey, String askKey, String filename) {
        int height = getHeight();
        while (height != 0) {
            Block block = getBlock(height, false);
            ArrayList<SignedTransaction> add = new ArrayList<>();
            ArrayList<SignedTransaction> delete = new ArrayList<>();
            ArrayList<SignedTransaction> share = new ArrayList<>();
            for (int i = 0; i < block.getTransactionsCount(); i++) {
                SignedTransaction signedTransaction = block.getTransaction(i);
                switch (signedTransaction.getTransaction().getType()) {
                    case 0:
                        add.add(signedTransaction);
                        break;
                    case 2:
                        delete.add(signedTransaction);
                        break;
                    case 3:
                        share.add(signedTransaction);
                        break;
                }
            }
            share.sort((o1, o2) -> {
                ShareFileTransaction share1 = (ShareFileTransaction) o1.getTransaction();
                ShareFileTransaction share2 = (ShareFileTransaction) o2.getTransaction();
                return -share1.getDate().compareTo(share2.getDate());
            });
            for (SignedTransaction signedTransaction : delete) {
                DeleteFileTransaction deleteFileTransaction = (DeleteFileTransaction) signedTransaction.getTransaction();
                if (deleteFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey))
                    return false;
            }
            for (SignedTransaction signedTransaction : share) {
                ShareFileTransaction shareFileTransaction = (ShareFileTransaction) signedTransaction.getTransaction();
                if (shareFileTransaction.getFilename().equals(filename) && shareFileTransaction.getKeeperKey().equals(keeperKey) && signedTransaction.getReceiverKey().equals(askKey)) {
                    return true;
                }
            }
            for (SignedTransaction signedTransaction : add) {
                AddFileTransaction addFileTransaction = (AddFileTransaction) signedTransaction.getTransaction();
                if (addFileTransaction.getFilename().equals(filename) && signedTransaction.getReceiverKey().equals(keeperKey)) {
                    return keeperKey.equals(askKey);
                }
            }
            height--;
        }
        return false;
    }

    @Override
    public void start(Blockchain.WorkMode workMode) {
        blocks = db.getCollection("blocks", BsonDocument.class);
    }

    @Override
    public void stop() {
        // ignored
    }

    @Override
    public Block getBlock(int i, boolean b) {
        MongoCursor<BsonDocument> cursor = blocks.find(Filters.eq("height", i)).iterator();
        if (cursor.hasNext()) {
            BsonDocument document = cursor.next();
            BsonBinary binary = document.getBinary("block_data", null);
            try {
                return new Block(binary.getData(), b);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public int getHeight() {
        return (int) blocks.countDocuments();
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByPublicKey(String s) {
        // TODO
        return null;
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByType(int i) {
        // TODO
        return null;
    }

    @Override
    public void addNewBlock(Block block) {
        BsonBinary binary = new BsonBinary(block.getBlockData());
        BsonDocument document = new BsonDocument("block_data", binary);
        document.put("height", new BsonInt32(block.getBlockHeight()));
        blocks.insertOne(document);
    }

    @Override
    public void setCallbackAskNewBlock(AskNewBlockCallback askNewBlockCallback) {
        //ignored
    }

    @Override
    public boolean validator(SignedTransaction signedTransaction) {
        // TODO
        return true;
    }
}
