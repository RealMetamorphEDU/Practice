/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 18.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.practice.springnode;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.*;
import realmetamorph.blockchain.network.INetMonitor;
import realmetamorph.blockchain.transactions.SignedTransaction;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Controller
@RequestMapping("/api")
public class APIController implements INetMonitor {

    private Blockchain blockchain;

    private static final String SEED = "Spring node No 1 in blockchain!";

    @Autowired
    private MongoTemplate mongo;

    private TakeNewBlockCallback takeNewBlockCallback;
    private AskNewBlockCallback askNewBlockCallback;
    private AskHeightCallback askHeightCallback;
    private AskBlockCallback askBlockCallback;
    private TakeNewTransactionCallback takeNewTransactionCallback;

    @PostConstruct
    private void init(){
        try {
            blockchain = new Blockchain(Blockchain.WorkMode.NODE_MODE, new FileMonitor(mongo.getDb()),this, new BlockGenerator(), Blockchain.createKeys(SEED));
            blockchain.start();
        } catch (UnknownHostException | InvalidAlgorithmParameterException | NoSuchAlgorithmException ignored) {
        }
    }


    @GetMapping("/block/all/{id}")
    @ResponseBody
    public String getBlock(@PathVariable("id") int id) {
        Block block = blockchain.getBlockByIndex(id, false);
        return "";
    }

    @GetMapping("/block/header/{id}")
    @ResponseBody
    public String getBlockHeader(@PathVariable("id") int id) {
        Block block = blockchain.getBlockByIndex(id, true);
        return "";
    }

    @GetMapping("/transaction/get/type={type}")
    @ResponseBody
    public String getTransaction() {
        return "";
    }

    @Override
    public void start(Blockchain.WorkMode workMode) {
        // TODO
    }

    @Override
    public void stop() {
        // TODO
    }

    @Override
    public Block getBlock(int i, boolean b) {
        // ignored
        return null;
    }

    @Override
    public int getHeight() {
        // ignored
        return 0;
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByPublicKey(String s) {
        // ignored
        return null;
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByType(int i) {
        // ignored
        return null;
    }

    @Override
    public void sendTransaction(SignedTransaction signedTransaction) {
        // ignored
    }

    @Override
    public void setCallbackTakeNewTransaction(TakeNewTransactionCallback takeNewTransactionCallback) {
        this.takeNewTransactionCallback = takeNewTransactionCallback;
    }

    @Override
    public void setCallbackAskBlock(AskBlockCallback askBlockCallback) {
        this.askBlockCallback = askBlockCallback;
    }

    @Override
    public void setCallbackAskHeight(AskHeightCallback askHeightCallback) {
        this.askHeightCallback = askHeightCallback;
    }

    @Override
    public void setCallbackAskNewBlock(AskNewBlockCallback askNewBlockCallback) {
        this.askNewBlockCallback = askNewBlockCallback;
    }

    @Override
    public void setCallbackTakeNewBlock(TakeNewBlockCallback takeNewBlockCallback) {
        this.takeNewBlockCallback = takeNewBlockCallback;
    }
}
