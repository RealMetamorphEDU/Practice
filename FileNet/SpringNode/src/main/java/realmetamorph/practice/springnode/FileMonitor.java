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

import java.net.UnknownHostException;
import java.util.ArrayList;

public class FileMonitor implements IFileMonitor {

    private MongoDatabase db;
    private MongoCollection<BsonDocument> blocks;

    FileMonitor(MongoDatabase db) throws UnknownHostException {
        this.db = db;
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
