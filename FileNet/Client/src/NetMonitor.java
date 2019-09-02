import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.*;
import realmetamorph.blockchain.network.INetMonitor;
import realmetamorph.blockchain.transactions.SignedTransaction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 1.9.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

public class NetMonitor implements INetMonitor {

    private static final String ORIGIN = "http://localhost:8080";
    private CloseableHttpClient http;
    private File file;


    NetMonitor() {
        http = HttpClients.createDefault();
    }

    @Override
    public Block getBlock(int blockIndex, boolean onlyHeader) {
        HttpGet get;
        if (onlyHeader)
            get = new HttpGet(ORIGIN + "/api/block/header/" + blockIndex);
        else
            get = new HttpGet(ORIGIN + "/api/block/all/" + blockIndex);
        try {
            HttpResponse response = http.execute(get);
            // {"error":"Not exist."}
            String content = EntityUtils.toString(response.getEntity());
            if (content.startsWith("{\"error"))
                return null;
            BsonDocument document = BsonDocument.parse(content);
            BsonBinary binary = document.getBinary("block_data");
            return new Block(binary.getData(), onlyHeader);
        } catch (IOException | NoSuchAlgorithmException | InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InvalidKeySpecException ignored) {
        }
        return null;
    }

    @Override
    public int getHeight() {
        HttpGet get = new HttpGet(ORIGIN + "/api/block/height");
        try {
            HttpResponse response = http.execute(get);
            // {"height":0}
            String content = EntityUtils.toString(response.getEntity());
            content = content.substring(10, content.length() - 1);
            return Integer.parseInt(content);
        } catch (IOException ignored) {
        }
        return 0;
    }

    void presetFile(File file) {
        this.file = file;
    }

    @Override
    public void sendTransaction(SignedTransaction transaction) {
        HttpPost post = new HttpPost(ORIGIN + "/api/transaction");
        BsonBinary binary = new BsonBinary(transaction.getTransactionBytes());
        BsonDocument document = new BsonDocument();
        document.put("data", binary);
        MultipartEntityBuilder build = MultipartEntityBuilder.create();
        build.addTextBody("transaction", document.toJson());
        if (file != null)
            build.addBinaryBody("file", file, ContentType.create("application/octet-stream"), null);
        HttpEntity entity = build.build();
        post.setEntity(entity);
        try {
            http.execute(post);
        } catch (IOException ignored) {
        }
        file = null;
    }


    // Not used

    @Override
    public void start(Blockchain.WorkMode workMode) {
        // IGNORED
    }

    @Override
    public void stop() {
        // IGNORED
    }

    //IGNORED
    @Override
    public ArrayList<SignedTransaction> getTransactionsByPublicKey(String publicKey) {
        return null;
    }

    //IGNORED
    @Override
    public ArrayList<SignedTransaction> getTransactionsByType(int type) {
        return null;
    }

    // IGNORED
    @Override
    public void setCallbackTakeNewTransaction(TakeNewTransactionCallback callback) {

    }

    @Override
    public void setCallbackAskBlock(AskBlockCallback callback) {

    }

    @Override
    public void setCallbackAskHeight(AskHeightCallback callback) {

    }

    @Override
    public void setCallbackAskNewBlock(AskNewBlockCallback callback) {

    }

    @Override
    public void setCallbackTakeNewBlock(TakeNewBlockCallback callback) {

    }
}
