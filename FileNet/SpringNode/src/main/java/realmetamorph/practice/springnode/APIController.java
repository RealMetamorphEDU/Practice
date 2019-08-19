/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 18.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.practice.springnode;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.*;
import realmetamorph.blockchain.filework.IFileMonitor;
import realmetamorph.blockchain.network.INetMonitor;
import realmetamorph.blockchain.transactions.SignedTransaction;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

@Controller
@RequestMapping("/api")
public class APIController implements INetMonitor {

    private Blockchain blockchain;

    private static final String SEED = "Spring node No 1 in blockchain!";

    @Autowired
    private MongoTemplate mongo;
    @Autowired
    private GridFsTemplate gridFsTemplate;


    private TakeNewBlockCallback takeNewBlockCallback;
    private AskNewBlockCallback askNewBlockCallback;
    private AskHeightCallback askHeightCallback;
    private AskBlockCallback askBlockCallback;
    private TakeNewTransactionCallback takeNewTransactionCallback;

    @PostConstruct
    private void init() {
        try {
            IFileMonitor fileMonitor = new FileMonitor(mongo.getDb());
            blockchain = new Blockchain(Blockchain.WorkMode.NODE_MODE, fileMonitor, this, new BlockGenerator(), Blockchain.createKeys(SEED));
            blockchain.start();
        } catch (UnknownHostException | InvalidAlgorithmParameterException | NoSuchAlgorithmException ignored) {
        }
    }


    @GetMapping("/block/all/{id}")
    @ResponseBody
    public String getBlock(@PathVariable("id") int id) {
        Block block = blockchain.getBlockByIndex(id, false);
        if (block == null)
            return "{\"error\":\"Not exist.\"}";
        BsonBinary binary = new BsonBinary(block.getBlockData());
        return new BsonDocument("block_data", binary).toJson();
    }

    @GetMapping("/block/header/{id}")
    @ResponseBody
    public String getBlockHeader(@PathVariable("id") int id) {
        Block block = blockchain.getBlockByIndex(id, true);
        if (block == null)
            return "{\"error\":\"Not exist.\"}";
        BsonBinary binary = new BsonBinary(block.getBlockData());
        return new BsonDocument("block_header", binary).toJson();
    }

    /*@GetMapping("/transaction/get/type={type}")
    @ResponseBody
    public String getTransaction(@PathVariable("type") int type) {
        return "{\"error\":\"Not implemented.\"}";
    }*/

    @PutMapping(value = "/transaction/put", headers = "Content-Type: application/json")
    @ResponseBody
    public String putTransaction() {
        return "";
    }

    @PostMapping(value = "/file/add")
    @ResponseBody
    public String postFile(@RequestParam("name") String name, @RequestParam("key") String key, @RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            Document metadata = new Document();
            metadata.put("user_key", key);
            metadata.put("content_type", file.getContentType());
            metadata.put("original_name", file.getOriginalFilename());
            try {
                gridFsTemplate.store(new ByteArrayInputStream(file.getBytes()), name, metadata);
                return "{\"status\":\"OK\"}";
            } catch (Exception e) {
                return "{\"error\":\"File is not uploaded.\"}";
            }
        }
        return "{\"error\":\"File is empty.\"}";
    }

    @GetMapping(value = "/file/get")
    @ResponseBody
    public ResponseEntity<InputStream> getFile(@RequestParam(value = "filename") String filename, @RequestParam(value = "key") String key, @RequestParam(value = "sign") String sign) {
        try {
            byte[] filenameByte = filename.getBytes(StandardCharsets.UTF_8);
            byte[] keyByte = Blockchain.hex2bytes(key, false);
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("EC");
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyByte);
                PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(filenameByte);
                digest.update(keyByte);
                String sha = Blockchain.bytes2hex(digest.digest(digest.digest()));
                if (!Blockchain.checkSignature(publicKey, sign, sha)) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } catch (NoSuchAlgorithmException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (SignatureException | InvalidKeyException | InvalidKeySpecException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            GridFSFile fsFile = gridFsTemplate.findOne(new Query().addCriteria(GridFsCriteria.whereMetaData().elemMatch(new Criteria("user_key").is(key))));
            if (fsFile == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            GridFsResource resource = gridFsTemplate.getResource(fsFile);
            if (resource == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            try {
                return new ResponseEntity<>(resource.getInputStream(), HttpStatus.OK);
            } catch (IOException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
